package com.example.unitconverter.app;

import java.util.ArrayList;
import java.util.Collections;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.content.Intent;
import android.graphics.Color;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.unitconverter.app.R;
import com.example.unitconverter.Unit;
import com.example.unitconverter.UnitManager.UNIT_TYPE;
import com.example.unitconverter.UnitManagerFactory;
import com.example.unitconverter.dao.CurrencyUnitsMapXMLReader;
import com.example.unitconverter.dao.FundUnitsMapXmlReader;
import com.example.unitconverter.dao.PrefixesMapXmlReader;
import com.example.unitconverter.dao.UnitsMapXmlReader;

public class MainActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<UnitManagerFactory>{
	PersistentSharablesApplication pSharablesApplication;	
	//
	ProgressBar unitManagerLoaderProgressBar;
	TextView progressBarTextView;
	//
	Button fromUnitBrowseButton;
	Button toUnitBrowseButton;
	Button convertButton;	
	//
	MultiAutoCompleteTextView fromUnitText;
	TextView fromValueText;
	MultiAutoCompleteTextView toUnitText;
	TextView conversionValueText;
	
	//
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		pSharablesApplication = (PersistentSharablesApplication)this.getApplication();

		//
		setupUIComponents();
		
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
			if (!pSharablesApplication.getFromQuantity().getUnit().getUnitName().equalsIgnoreCase(fromUnitText.toString())
				&& !pSharablesApplication.getFromQuantity().getUnit().getUnitName().equalsIgnoreCase(Unit.UNKNOWN_UNIT_NAME)	){
				fromUnitText.setText(pSharablesApplication.getFromQuantity().getUnit().getUnitName());
				setFromUnit();
			}
			
			if (!pSharablesApplication.getToQuantity().getUnit().getUnitName().equalsIgnoreCase(toUnitText.toString())
				&& !pSharablesApplication.getToQuantity().getUnit().getUnitName().equalsIgnoreCase(Unit.UNKNOWN_UNIT_NAME)){
				toUnitText.setText(pSharablesApplication.getToQuantity().getUnit().getUnitName());
				setToUnit();
			}		
			
			checkUnits();
		}
		else{
			pSharablesApplication.savePrefixesNUnits();
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
			String conversion = pSharablesApplication.getFromQuantity().getUnit().getUnitCategory().toUpperCase()+": "+pSharablesApplication.getFromQuantity().getUnit().getUnitName() + " --> " + pSharablesApplication.getToQuantity().getUnit().getUnitName();
			if(!pSharablesApplication.getConversionFavoritesList().contains(conversion)
					&& pSharablesApplication.getFromQuantity().getUnit().equalsDimension(pSharablesApplication.getToQuantity().getUnit())){
				pSharablesApplication.addConversionToConversionFavoritesList(conversion);	
				Collections.sort(pSharablesApplication.getConversionFavoritesList());
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
	
	//Setup UI Components Methods
	private void setupUIComponents(){
		unitManagerLoaderProgressBar = (ProgressBar) findViewById(R.id.unitManagerLoaderProgressBar);
		progressBarTextView = (TextView) findViewById(R.id.progressBarTextView);
		
		//
		fromUnitBrowseButton = (Button) findViewById(R.id.fromUnitBrowseButton);
		toUnitBrowseButton = (Button) findViewById(R.id.toUnitBrowseButton);
		convertButton = (Button) findViewById(R.id.convertButton);	
		
		//
		fromUnitText = (MultiAutoCompleteTextView) findViewById(R.id.fromUnitTextView);
		fromValueText = (TextView) findViewById(R.id.fromValueTextView);
		toUnitText = (MultiAutoCompleteTextView) findViewById(R.id.toUnitTextView);
		conversionValueText = (TextView) findViewById(R.id.conversionValueTextView);
	}
	
	//Data Loading Methods
	private void loadUnitManager(){
		if(!pSharablesApplication.isUnitManagerPreReqLoadingComplete()){
			getSupportLoaderManager().initLoader(pSharablesApplication.GENERAL_UNITS_LOADER, null, this).forceLoad();
			getSupportLoaderManager().initLoader(pSharablesApplication.PREFIXES_LOADER, null, this).forceLoad();
			getSupportLoaderManager().initLoader(pSharablesApplication.FUND_UNITS_LOADER, null, this).forceLoad();	
			getSupportLoaderManager().initLoader(pSharablesApplication.CURRENCY_UNITS_LOADER, null, this).forceLoad();
			
			//
			((LinearLayout)findViewById(R.id.progressBarLinearLayout)).setVisibility(View.VISIBLE);
			((LinearLayout)findViewById(R.id.fromLinearLayout)).setVisibility(View.INVISIBLE);
			((LinearLayout)findViewById(R.id.switchLinearLayout)).setVisibility(View.INVISIBLE);
			((LinearLayout)findViewById(R.id.toLinearLayout)).setVisibility(View.INVISIBLE);
		}
	}
	
	//Listeners Methods
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
	
	//
	private void postLoadSetup(){
		pSharablesApplication.recreateUnitManager();	
		setupAutoComplete();
		
		//
		((LinearLayout)findViewById(R.id.progressBarLinearLayout)).setVisibility(View.GONE);
		((LinearLayout)findViewById(R.id.fromLinearLayout)).setVisibility(View.VISIBLE);
		((LinearLayout)findViewById(R.id.switchLinearLayout)).setVisibility(View.VISIBLE);
		((LinearLayout)findViewById(R.id.toLinearLayout)).setVisibility(View.VISIBLE);
	}
	private void setupAutoComplete(){
		ArrayList<String> unitsNamesNPrefixes = new ArrayList<String>();
		
		for(Unit unit:pSharablesApplication.getUnitManager().getCoreUnits()){
			unitsNamesNPrefixes.add(unit.getUnitName());
		}
		for(Unit unit:pSharablesApplication.getUnitManager().getDynamicUnits()){
			unitsNamesNPrefixes.add(unit.getUnitName());
		}
		unitsNamesNPrefixes.addAll(pSharablesApplication.getUnitManager().getAllPrefixValues().keySet());
		
		ArrayAdapter<String> unitsNamesNPrefixes_Adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice, unitsNamesNPrefixes);
		
		fromUnitText.setAdapter(unitsNamesNPrefixes_Adapter);
		toUnitText.setAdapter(unitsNamesNPrefixes_Adapter);		
		
		fromUnitText.setThreshold(1);
		toUnitText.setThreshold(1);
		
		unitsNamesNPrefixes_Adapter.notifyDataSetChanged();
	}
	
	//
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
		
		if(!((isToUnit)?pSharablesApplication.getToQuantity().getUnit():pSharablesApplication.getFromQuantity().getUnit()).getUnitName().equalsIgnoreCase(unitNameOrDimension)){
			if(unitNameOrDimension.equals("") ){
				matchedUnits.add(pSharablesApplication.getUnitManager().getUnit(Unit.UNKNOWN_UNIT_NAME));
			}
			else if(unitNameOrDimension.contains("*") || unitNameOrDimension.contains("/") || unitNameOrDimension.contains("^")
				   || unitNameOrDimension.contains("(") || unitNameOrDimension.contains(")")){
				
				matchedUnits = pSharablesApplication.getUnitManager().getUnitsByComponentUnitsDimension(unitNameOrDimension, false);
				
				//If unit manager does not already contains units with similar dimension, then added a newly created version of such unit and store it in the unit manager for easy future access
				if(matchedUnits.isEmpty()){
					matchedUnits.add(pSharablesApplication.getUnitManager().getUnit(unitNameOrDimension));
				}
			}	
			else{
				matchedUnits.add(pSharablesApplication.getUnitManager().getUnit(unitNameOrDimension));
			}
		}
		else{
			matchedUnits.add(isToUnit?pSharablesApplication.getToQuantity().getUnit():pSharablesApplication.getFromQuantity().getUnit());
		}
		
		if(!matchedUnits.isEmpty()){
			if(isToUnit){
				pSharablesApplication.getToQuantity().setUnit(matchedUnits.get(0));
			}
			else{
				pSharablesApplication.getFromQuantity().setUnit(matchedUnits.get(0));
			}
		}
	}
	
	private void setFromValue(){
		if(!fromValueText.getText().toString().equalsIgnoreCase("")){
			double quantValue = Double.parseDouble(fromValueText.getText().toString());
			pSharablesApplication.getFromQuantity().setValue(quantValue);			
		}
	}
	
	//
	private void checkUnits(){
		if(!pSharablesApplication.getFromQuantity().getUnit().getFundamentalUnitsExponentMap().keySet().contains(UNIT_TYPE.UNKNOWN) 
		   && !pSharablesApplication.getToQuantity().getUnit().getFundamentalUnitsExponentMap().keySet().contains(UNIT_TYPE.UNKNOWN)){
			if(pSharablesApplication.getFromQuantity().getUnit().equalsDimension(pSharablesApplication.getToQuantity().getUnit())){	
				conversionValueText.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in));
				conversionValueText.setText("##:Units Match");				
				
				//
				fromUnitText.setTextColor(Color.BLACK);
				toUnitText.setTextColor(Color.BLACK);
				conversionValueText.setTextColor(Color.BLACK);
			}
			else{
				conversionValueText.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in));
				conversionValueText.setText("XX:Units Don't Match");
				
				//
				fromUnitText.setTextColor(Color.rgb(180, 0, 0));
				toUnitText.setTextColor(Color.rgb(180, 0, 0));
				conversionValueText.setTextColor(Color.rgb(200, 0, 0));
			}
		}
		else{	
			boolean isToUnk = pSharablesApplication.getToQuantity().getUnit().getFundamentalUnitsExponentMap().keySet().contains(UNIT_TYPE.UNKNOWN);
			boolean isFromUkn = pSharablesApplication.getFromQuantity().getUnit().getFundamentalUnitsExponentMap().keySet().contains(UNIT_TYPE.UNKNOWN);
		
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
		}		
	}
	private void getConversion(){
		if(!pSharablesApplication.getFromQuantity().getUnit().getFundamentalUnitsExponentMap().keySet().contains(UNIT_TYPE.UNKNOWN) 
		   && !pSharablesApplication.getToQuantity().getUnit().getFundamentalUnitsExponentMap().keySet().contains(UNIT_TYPE.UNKNOWN)){
			
			if(pSharablesApplication.getFromQuantity().getUnit().equalsDimension(pSharablesApplication.getToQuantity().getUnit())){
				
				pSharablesApplication.getToQuantity().setValue(pSharablesApplication.getFromQuantity()
													          .convertToUnit(pSharablesApplication.getToQuantity().getUnit()).getValue());	
				
				conversionValueText.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.slide_in_left));
				conversionValueText.setText(String.valueOf(pSharablesApplication.getToQuantity().getValue()));
			}
		}
	}
	
	//Loader Manager methods
	@Override
	public Loader<UnitManagerFactory> onCreateLoader(int id, Bundle arg1) {
		if(id == pSharablesApplication.GENERAL_UNITS_LOADER){
			 return new UnitsMapXmlReader(this);
		}else if(id == pSharablesApplication.PREFIXES_LOADER){
			return new PrefixesMapXmlReader(this);	
		}else if(id == pSharablesApplication.FUND_UNITS_LOADER){
			return new FundUnitsMapXmlReader(this);
		}else if(id == pSharablesApplication.CURRENCY_UNITS_LOADER){
			return new CurrencyUnitsMapXMLReader(this);
		}else{
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<UnitManagerFactory> loader, UnitManagerFactory loadedUnitManagerFactory) {
		pSharablesApplication.setUnitManagerFactory(UnitManagerFactory.combineUnitManagerFactories(pSharablesApplication.getUnitManagerFactory()
				            		             												  ,loadedUnitManagerFactory));	
		pSharablesApplication.numOfLoaderCompleted++;
  		if(pSharablesApplication.isUnitManagerPreReqLoadingComplete()){
  			postLoadSetup();
  		}
	}

	@Override
	public void onLoaderReset(Loader<UnitManagerFactory> arg0) {
		
	}	
}
