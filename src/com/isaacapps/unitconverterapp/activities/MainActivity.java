package com.isaacapps.unitconverterapp.activities;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.app.AlertDialog;
import android.content.*;
import android.graphics.Color;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.widget.*;

import com.isaacapps.unitconverterapp.activities.R;
import com.isaacapps.unitconverterapp.dao.xml.readers.local.*;
import com.isaacapps.unitconverterapp.dao.xml.readers.online.*;
import com.isaacapps.unitconverterapp.models.*;
import com.isaacapps.unitconverterapp.models.unitmanager.*;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager.*;

public class MainActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<UnitManagerBuilder>{
	public final int LOCAL_UNITS_LOADER = 1, FUND_UNITS_LOADER = 2, ONLINE_CURRENCY_UNITS_LOADER = 3, LOCAL_PREFIXES_LOADER= 4, ONLINE_PREFIXES_N_UNITS_LOADER = 5, POST_LOADER = 6;
	
	PersistentSharablesApplication pSharablesApplication;
	Quantity fromQuantity, toQuantity;

	ProgressBar unitManagerLoaderProgressBar;
	TextView progressBarTextView;

	Button fromUnitBrowseButton;
	Button toUnitBrowseButton;
	Button convertButton;	
	Button fromUnitViewInfoButton;
	Button toUnitViewInfoButton;
	
	Animation convertButtonAnimation;
	
	MultiAutoCompleteTextView fromUnitText;
	TextView fromValueText;
	MultiAutoCompleteTextView toUnitText;
	TextView conversionValueText;

	AlertDialog unitInfoDialog;
	
	///
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		pSharablesApplication = (PersistentSharablesApplication)this.getApplication();
		
		fromQuantity = pSharablesApplication.getFromQuantity();
		toQuantity = pSharablesApplication.getToQuantity();
			
		//
		setupUIComponents();
		
		setupAnimations();
		
		setListenersOnMainButtons();
		setListenersOnTextViews();
		
		loadUnitManager();
		
	}
	@Override
	protected void onPause(){
		super.onPause();;
	}
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		
		if(hasFocus){
			if (!fromQuantity.getUnit().getName().equalsIgnoreCase(fromUnitText.toString())
				&& !fromQuantity.getUnit().getName().equalsIgnoreCase(Unit.UNKNOWN_UNIT_NAME)	){
				fromUnitText.setText(fromQuantity.getUnit().getName());				
				setFromUnit();
			}
			
			if (!toQuantity.getUnit().getName().equalsIgnoreCase(toUnitText.toString())
				&& !toQuantity.getUnit().getName().equalsIgnoreCase(Unit.UNKNOWN_UNIT_NAME)){
				toUnitText.setText(toQuantity.getUnit().getName());			
				setToUnit();
			}		
			checkUnits();
			
			setFromValue();
		}
		else{
			pSharablesApplication.saveUnits();
			pSharablesApplication.saveConversionFavorites();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case R.id.addToFavoritesItem:
			if(pSharablesApplication.getUnitManager().getConversionFavoritesDataModel().addConversion(fromQuantity.getUnit(), toQuantity.getUnit()) ){
				fromUnitText.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.slide_out_right));
				toUnitText.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.slide_out_right));
			}	
			return true;
		case R.id.viewFavoritesItem:
			Intent i = new Intent(MainActivity.this, ConversionFavoritesActivity.class);
			startActivity(i);
			return true;
		case R.id.flipItem:
			String tempText = fromUnitText.getText().toString();
			
			fromUnitText.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in));
			fromUnitText.setText(toUnitText.getText().toString());
			
			toUnitText.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in));
			toUnitText.setText(tempText);
			
			setFromUnit();
			setToUnit();
			checkUnits();
			
			getConversion();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
		
	}
	
	///Setup UI Components Methods
	private void setupUIComponents(){
		unitManagerLoaderProgressBar = (ProgressBar) findViewById(R.id.unitManagerLoaderProgressBar);
		progressBarTextView = (TextView) findViewById(R.id.progressBarTextView);
		
		//
		fromUnitBrowseButton = (Button) findViewById(R.id.fromUnitBrowseButton);
		toUnitBrowseButton = (Button) findViewById(R.id.toUnitBrowseButton);
		convertButton = (Button) findViewById(R.id.convertButton);	
		fromUnitViewInfoButton = (Button) findViewById(R.id.fromUnitViewDescButton);
		toUnitViewInfoButton = (Button) findViewById(R.id.toUnitViewDescButton);
		
		//
		fromUnitText = (MultiAutoCompleteTextView) findViewById(R.id.fromUnitTextView);
		fromValueText = (TextView) findViewById(R.id.fromValueTextView);
		fromValueText.setText("1");
		toUnitText = (MultiAutoCompleteTextView) findViewById(R.id.toUnitTextView);
		conversionValueText = (TextView) findViewById(R.id.conversionValueTextView);
		
		//
		unitInfoDialog = new AlertDialog.Builder(MainActivity.this).create();
		unitInfoDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {		
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
	}
	
	///Setup Animation Methods
	private void setupAnimations(){
		convertButtonAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.conversion_button_animation);
	}
	
	///DataMaps Loading Methods
	private void loadUnitManager(){
		if(!pSharablesApplication.isUnitManagerPreReqLoadingComplete()){
			getSupportLoaderManager().initLoader(ONLINE_PREFIXES_N_UNITS_LOADER, null, this).forceLoad();
			//getSupportLoaderManager().initLoader(LOCAL_UNITS_LOADER, null, this).forceLoad();
			//getSupportLoaderManager().initLoader(LOCAL_PREFIXES_LOADER, null, this).forceLoad();
			getSupportLoaderManager().initLoader(FUND_UNITS_LOADER, null, this).forceLoad();	
			//getSupportLoaderManager().initLoader(ONLINE_CURRENCY_UNITS_LOADER, null, this).forceLoad();
			
			//
			((LinearLayout)findViewById(R.id.progressBarLinearLayout)).setVisibility(View.VISIBLE);
			((LinearLayout)findViewById(R.id.fromLinearLayout)).setVisibility(View.INVISIBLE);
			((LinearLayout)findViewById(R.id.switchLinearLayout)).setVisibility(View.INVISIBLE);
			((LinearLayout)findViewById(R.id.toLinearLayout)).setVisibility(View.INVISIBLE);
		}
	}
	private void postLoadSetup(){
		//
		((LinearLayout)findViewById(R.id.progressBarLinearLayout)).setVisibility(View.GONE);
		((LinearLayout)findViewById(R.id.fromLinearLayout)).setVisibility(View.VISIBLE);
		((LinearLayout)findViewById(R.id.switchLinearLayout)).setVisibility(View.VISIBLE);
		((LinearLayout)findViewById(R.id.toLinearLayout)).setVisibility(View.VISIBLE);
	}
	
	
	///Listeners Methods
	private void setListenersOnMainButtons(){

		fromUnitBrowseButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				Intent i = new Intent(MainActivity.this, UnitBrowserActivity.class);
				i.putExtra("buttonName", "from");
				startActivity(i);
			}
		});
		
		toUnitBrowseButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				Intent i = new Intent(MainActivity.this, UnitBrowserActivity.class);
				i.putExtra("buttonName", "to");
				startActivity(i);
			}
		});
		
		fromUnitViewInfoButton.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				unitInfoDialog.setTitle("From Unit Details");				
				unitInfoDialog.setMessage(getUnitDetailsMessage(fromQuantity.getUnit()));
				unitInfoDialog.show();
			}
		});
		
		toUnitViewInfoButton.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				unitInfoDialog.setTitle("To Unit Details");
				unitInfoDialog.setMessage(getUnitDetailsMessage(toQuantity.getUnit()));
				unitInfoDialog.show();
			}
		});
				
		convertButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				setFromUnit();
				setToUnit();
				checkUnits();
				
				setFromValue();
				
				getConversion();
			}
		});
	}
	private String getUnitDetailsMessage(Unit unit){
		String category = unit.getCategory();
		String fundamentalTypesDim = Utility.getFundamentalUnitTypesDimensionAsString(unit.getFundamentalUnitTypesDimension());
		String description = unit.getDescription();
		
		return (category.equals(fundamentalTypesDim.toLowerCase())?"":"Category: "+ category)
								  +(description.length()==0?"":"\n\nDescription: "+description)
								  +"\n\nFundamental Types Dimension: "+fundamentalTypesDim;
		
	}
	
	private void setListenersOnTextViews(){
		fromUnitText.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus){
					setFromUnit();
					checkUnits();
				}
			}
			
		});
	
		toUnitText.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus){
					setToUnit();
					checkUnits();
				}
			}
		});
		
		fromValueText.setOnFocusChangeListener(new OnFocusChangeListener(){
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus){
					setFromValue();
				}
			}
		});
	}
	
	///
	private void setFromUnit(){
		setUnit(false);
	}
	private void setToUnit(){
		setUnit(true);
	}
	private void setUnit(boolean isToUnit){
		String unitNameOrDimension = (isToUnit?toUnitText:fromUnitText).getText().toString();
		unitNameOrDimension = unitNameOrDimension.trim();
		
		ArrayList<Unit> matchedUnits =  new ArrayList<Unit>();	
		
		if(!((isToUnit)?toQuantity.getUnit():fromQuantity.getUnit()).getName().equalsIgnoreCase(unitNameOrDimension)){
			if(unitNameOrDimension.equals("") ){
				matchedUnits.add(pSharablesApplication.getUnitManager().getUnitsDataModel().getUnit(Unit.UNKNOWN_UNIT_NAME));
			}	
			else{
				matchedUnits.add(pSharablesApplication.getUnitManager().getUnitsDataModel().getUnit(unitNameOrDimension));
			}
		}
		else{
			matchedUnits.add(isToUnit?toQuantity.getUnit():fromQuantity.getUnit());
		}
		
		if(!matchedUnits.isEmpty()){
			if(isToUnit){
				toQuantity.setUnit(matchedUnits.get(0));
			}
			else{
				fromQuantity.setUnit(matchedUnits.get(0));
			}
		}
		
		//Update conversion rank
		pSharablesApplication.getUnitManager().getConversionFavoritesDataModel()
											  .modifySignificanceRankOfConversions(isToUnit?toQuantity.getUnit():fromQuantity.getUnit(), true);
	}
	
	private void setFromValue(){
		if(!fromValueText.getText().toString().equalsIgnoreCase("")){
			fromQuantity.setValue(Double.parseDouble(fromValueText.getText().toString()));			
		}
	}
	
	///
	private void checkUnits(){
		if(!fromQuantity.getUnit().getFundamentalUnitTypesDimension().keySet().contains(UNIT_TYPE.UNKNOWN) 
		   && !toQuantity.getUnit().getFundamentalUnitTypesDimension().keySet().contains(UNIT_TYPE.UNKNOWN)){
			if(fromQuantity.getUnit().equalsDimension(toQuantity.getUnit())){	
				conversionValueText.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in));
				conversionValueText.setText("##:Units Match");				
				
				//
				fromUnitText.setTextColor(Color.BLACK);
				toUnitText.setTextColor(Color.BLACK);
				conversionValueText.setTextColor(Color.BLACK);
				
				//	
				convertButton.setTextColor(Color.BLUE);
				convertButton.startAnimation(convertButtonAnimation);
			}
			else{
				conversionValueText.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in));
				conversionValueText.setText("XX:Units Don't Match");
				
				//
				fromUnitText.setTextColor(Color.rgb(180, 0, 0));
				toUnitText.setTextColor(Color.rgb(180, 0, 0));
				conversionValueText.setTextColor(Color.rgb(200, 0, 0));
				
				//
				convertButton.setTextColor(Color.BLACK);
				convertButton.clearAnimation();
			}
		}
		else{	
			boolean isToUnk = toQuantity.getUnit().getFundamentalUnitTypesDimension().keySet().contains(UNIT_TYPE.UNKNOWN);
			boolean isFromUkn = fromQuantity.getUnit().getFundamentalUnitTypesDimension().keySet().contains(UNIT_TYPE.UNKNOWN);
		
			if(isToUnk && isFromUkn){
				conversionValueText.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in));
				conversionValueText.setText("??:'FROM'&'TO' Units Unknown");
				
				//
				fromUnitText.setTextColor(Color.rgb(180, 0, 0));
				toUnitText.setTextColor(Color.rgb(180, 0, 0));
				conversionValueText.setTextColor(Color.rgb(200, 0, 0));				
			}
			else if(isToUnk){
				conversionValueText.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in));
				conversionValueText.setText("#?:'TO' Unit Unknown");
				
				//
				fromUnitText.setTextColor(Color.rgb(0, 0, 0));				
				
				toUnitText.setTextColor(Color.rgb(180, 0, 0));
				conversionValueText.setTextColor(Color.rgb(200, 0, 0));
			}		
			else if(isFromUkn){
				conversionValueText.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in));
				conversionValueText.setText("?#:'FROM' Unit Unknown");
				
				//
				fromUnitText.setTextColor(Color.rgb(180, 0, 0));
				conversionValueText.setTextColor(Color.rgb(200, 0, 0));	
				
				toUnitText.setTextColor(Color.rgb(0, 0, 0));					
			}
			else{
				toUnitText.setTextColor(Color.rgb(0, 0, 0));
				fromUnitText.setTextColor(Color.rgb(0, 0, 0));
			}
			
			//
			convertButton.setTextColor(Color.BLACK);
			convertButton.clearAnimation();
		}		
	}
	private void getConversion(){
		if(!fromQuantity.getUnit().getFundamentalUnitTypesDimension().keySet().contains(UNIT_TYPE.UNKNOWN) 
		   && !toQuantity.getUnit().getFundamentalUnitTypesDimension().keySet().contains(UNIT_TYPE.UNKNOWN)){
			
			if(fromQuantity.getUnit().equalsDimension(toQuantity.getUnit())){
				
				toQuantity.setValue(fromQuantity.convertToUnit(toQuantity.getUnit()).getValue());	
				
				conversionValueText.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.slide_in_left));
				conversionValueText.setText(String.valueOf(toQuantity.getValue()));
				
				//Update conversion rank
				pSharablesApplication.getUnitManager().getConversionFavoritesDataModel()
													  .modifySignificanceRankOfConversions(fromQuantity.getUnit(), true);
				pSharablesApplication.getUnitManager().getConversionFavoritesDataModel()
				  									  .modifySignificanceRankOfConversions(toQuantity.getUnit(), true);
			}
		}
		
		//
		convertButton.setTextColor(Color.BLACK);
		convertButton.clearAnimation();
	}
	
	///Loader Manager methods
	@Override
	public Loader<UnitManagerBuilder> onCreateLoader(int id, Bundle arg1) {
		if(id == LOCAL_UNITS_LOADER){
			return new UnitsMapXmlLocalReader(this);
		}else if(id == LOCAL_PREFIXES_LOADER){
			return new PrefixesMapXmlLocalReader(this);	
		}else if(id == FUND_UNITS_LOADER){
			return new FundamentalUnitsMapXmlLocalReader(this);
		}else if(id == ONLINE_CURRENCY_UNITS_LOADER){
			return new CurrencyUnitsMapXmlOnlineReader(this);
		} else if(id == ONLINE_PREFIXES_N_UNITS_LOADER){
			return new PrefixesNUnitsMapXmlOnlineReader(this);
		} else if(id == POST_LOADER){
			Loader loader = new AsyncTaskLoader<Void>(pSharablesApplication){

				@Override
				public Void loadInBackground() {
					((PersistentSharablesApplication)getContext().getApplicationContext()).recreateUnitManager();
					return null;
				}
				
			};
			return loader;
		}else{
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<UnitManagerBuilder> loader, UnitManagerBuilder loadedUnitManagerBuilder) {
		if(loadedUnitManagerBuilder != null){	
			pSharablesApplication.getUnitManagerBuilder().combineWith(loadedUnitManagerBuilder);	 
			pSharablesApplication.numOfLoaderCompleted++;
		}
	  	
		if(pSharablesApplication.isUnitManagerPreReqLoadingComplete()){

  			if(loadedUnitManagerBuilder == null)
  				postLoadSetup();
  			else{
  				getSupportLoaderManager().initLoader(POST_LOADER, null, this).forceLoad();
  			}
  		}
	}

	@Override
	public void onLoaderReset(Loader<UnitManagerBuilder> arg0) {	}	
}
