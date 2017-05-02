package com.isaacapps.unitconverterapp.activities;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;

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
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager.DATA_MODEL_CATEGORY;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.UnitsDataModel;

public class MainActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<UnitManagerBuilder>{
	private static enum LOADER{LOCAL_UNITS_LOADER, FUND_UNITS_LOADER, ONLINE_CURRENCY_UNITS_LOADER, LOCAL_PREFIXES_LOADER, ONLINE_PREFIXES_N_UNITS_LOADER, POST_LOADER}

	PersistentSharablesApplication pSharablesApplication;
	UnitManager unitManager;
	Quantity fromQuantity, toQuantity;

	ProgressBar unitManagerLoaderProgressBar;
	TextView progressBarTextView;

	Button fromUnitBrowseButton, toUnitBrowseButton, convertButton;	
	Button fromUnitViewInfoButton, toUnitViewInfoButton;
	
	Animation convertButtonAnimation;
	
	MultiAutoCompleteTextView fromUnitTextView;
	TextView fromValueTextView;
	MultiAutoCompleteTextView toUnitTextView;
	TextView conversionValueTextView;

	AlertDialog unitInfoDialog;
	
	boolean isMultiUnitMode;
	
	///
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		pSharablesApplication = (PersistentSharablesApplication)this.getApplication();
		
		fromQuantity = pSharablesApplication.getFromQuantity();
		toQuantity = pSharablesApplication.getToQuantity();
		
		isMultiUnitMode = false;
			
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
			if (fromQuantity.isValid()){
				fromUnitTextView.setText(fromQuantity.getUnitNames());	
				fromValueTextView.setText(fromQuantity.getValuesString());
			}
			
			if (toQuantity.isValid())
				toUnitTextView.setText(toQuantity.getUnitNames());				
			
			checkUnits();	
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
			if(fromQuantity.getUnits().size() == 1 && toQuantity.getUnits().size() == 1
			   && pSharablesApplication.getUnitManager().getConversionFavoritesDataModel().addConversion(fromQuantity.getLargestUnit(), toQuantity.getLargestUnit()) )
			{
				fromUnitTextView.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.slide_out_right));
				toUnitTextView.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.slide_out_right));
			}	
			return true;
		case R.id.viewFavoritesItem:
			Intent i = new Intent(MainActivity.this, ConversionFavoritesActivity.class);
			startActivity(i);
			return true;
		case R.id.flipItem:
			String tempText = fromUnitTextView.getText().toString();
			
			fromUnitTextView.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in));
			fromUnitTextView.setText(toUnitTextView.getText().toString());
			
			toUnitTextView.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in));
			toUnitTextView.setText(tempText);
			
			setFromUnitsToQuantity();
			setToUnitToQuantity();
			
			if(checkUnits() && checkNSetFromValuesToQuantity())
				getConversion();
			
			return true;
		case R.id.multiUnitModeItem:
			if(item.isChecked()){
				//No grouping exist, then add some dummy ones for demonstration purposes
				if(calculateGroupCount(fromUnitTextView.getText().toString()) == 0)
					fromUnitTextView.setText("{a} {b} {c}");
				if(calculateGroupCount(toUnitTextView.getText().toString()) == 0)
					toUnitTextView.setText("{a} {b} {c}");
				
				fromValueTextView.setText(formatValuesGroupingTypes(fromValueTextView.getText().toString()));
				
				adjustGroupingsCount(fromValueTextView, fromQuantity, " {1}");
				
				isMultiUnitMode = true;
			}
			else{
				isMultiUnitMode = false;
			}
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
		fromUnitTextView = (MultiAutoCompleteTextView) findViewById(R.id.fromUnitTextView);
		fromValueTextView = (TextView) findViewById(R.id.fromValueTextView);
		fromValueTextView.setText("1");
		toUnitTextView = (MultiAutoCompleteTextView) findViewById(R.id.toUnitTextView);
		conversionValueTextView = (TextView) findViewById(R.id.conversionValueTextView);
		
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
	
	///Unit Manager Loading Methods
	private void loadUnitManager(){
		if(!pSharablesApplication.isUnitManagerPreReqLoadingComplete()){
			//getSupportLoaderManager().initLoader(LOADER.ONLINE_PREFIXES_N_UNITS_LOADER.ordinal(), null, this).forceLoad();
			getSupportLoaderManager().initLoader(LOADER.LOCAL_UNITS_LOADER.ordinal(), null, this).forceLoad();
			getSupportLoaderManager().initLoader(LOADER.LOCAL_PREFIXES_LOADER.ordinal(), null, this).forceLoad();
			getSupportLoaderManager().initLoader(LOADER.FUND_UNITS_LOADER.ordinal(), null, this).forceLoad();	
			//getSupportLoaderManager().initLoader(LOADER.ONLINE_CURRENCY_UNITS_LOADER.ordinal(), null, this).forceLoad();
			
			//
			((LinearLayout)findViewById(R.id.progressBarLinearLayout)).setVisibility(View.VISIBLE);
			((LinearLayout)findViewById(R.id.fromLinearLayout)).setVisibility(View.INVISIBLE);
			((LinearLayout)findViewById(R.id.switchLinearLayout)).setVisibility(View.INVISIBLE);
			((LinearLayout)findViewById(R.id.toLinearLayout)).setVisibility(View.INVISIBLE);
		}
	}
	private void postLoadSetup(){
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
				unitInfoDialog.setMessage(getUnitDetailsMessage(fromQuantity.getUnits()));
				unitInfoDialog.show();
			}
		});
		
		toUnitViewInfoButton.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				unitInfoDialog.setTitle("To Unit Details");
				unitInfoDialog.setMessage(getUnitDetailsMessage(toQuantity.getUnits()));
				unitInfoDialog.show();
			}
		});
				
		convertButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				
				setFromUnitsToQuantity();
				setToUnitToQuantity();
				
				if(checkUnits() && checkNSetFromValuesToQuantity())
					getConversion();
			}
		});
	}
	private String getUnitDetailsMessage(Collection<Unit> units){
		String category = units.iterator().next().getCategory();
		String fundamentalTypesDim = UnitsDataModel.getDataModelCategory(units.iterator().next()) != DATA_MODEL_CATEGORY.UNKNOWN
												?"": Utility.getFundamentalUnitTypesDimensionAsString(units.iterator().next().getFundamentalUnitTypesDimension());
		String description = units.size()==1?units.iterator().next().getDescription():"";
		
		return (category.equals(fundamentalTypesDim.toLowerCase())?"":"Category: "+ category) //Prevent display of duplicate data in case of complex units  
								  +(description.length()==0?"":"\n\nDescription: "+description)
								  +(fundamentalTypesDim.length()==0?"":"\n\nFundamental Types Dimension: "+fundamentalTypesDim);
		
	}
	
	private void setListenersOnTextViews(){
		fromUnitTextView.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if(!hasFocus){
					
					if(isMultiUnitMode)
						((TextView)view).setText(formatUnitsGroupingTypes(((TextView)view).getText().toString()));
					
					setFromUnitsToQuantity();
					
					if(isMultiUnitMode){
						adjustGroupingsCount(fromValueTextView, fromQuantity, " {1}");
						adjustGroupingsCount(toUnitTextView, fromQuantity, " {a}");
					}
															
					checkUnits();
					
					checkNSetFromValuesToQuantity();
				}
			}
			
		});
	
		toUnitTextView.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if(!hasFocus){
					
					if(isMultiUnitMode)
						((TextView)view).setText(formatUnitsGroupingTypes(((TextView)view).getText().toString()));
					
					setToUnitToQuantity();
					
					if(isMultiUnitMode){
						adjustGroupingsCount(fromValueTextView, toQuantity, " {1}");
						adjustGroupingsCount(fromUnitTextView, toQuantity, " {a}");
					}
															
					checkUnits();
					
					checkNSetFromValuesToQuantity();
				}
			}
		});
		
		fromValueTextView.setOnFocusChangeListener(new OnFocusChangeListener(){
			
			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if(!hasFocus){
					if(isMultiUnitMode){
					    ((TextView)view).setText(formatUnitsGroupingTypes(((TextView)view).getText().toString()));
						adjustGroupingsCount(fromValueTextView, toQuantity, " {1}");
					}
					
					checkNSetFromValuesToQuantity();
				}
			}
		});
	}
	
	///
	private int calculateGroupCount(String text){
		//Recursively find the number of '{ [anything in between] }' groupings
		if(text.indexOf("{") != -1 && text.indexOf("}") != -1 ){
			if(text.indexOf("{") < text.indexOf("}")){
				return 1 + calculateGroupCount(text.substring(text.indexOf("}")+1));
			}
			else{ // if the braces are not arranged appropriately, then do not tally count 
				return calculateGroupCount(text.substring(text.indexOf("{")+1));
			}
		}
		else{
			return 0;
		}
	}
	private boolean adjustGroupingsCount(TextView textViewToBeAdjusted, Quantity referenceQuantity, String dummyValue){
		String text = textViewToBeAdjusted.getText().toString();
		int groupCount = calculateGroupCount(text);
		int unitsGroupCount = referenceQuantity.getUnits().size();
		if(unitsGroupCount>1){
		   //Makes grouping units and value groupings are identical
		   if(groupCount > unitsGroupCount){
			   while(calculateGroupCount(text) != unitsGroupCount ){
				   text = text.replaceFirst("{[^{}]*}", ""); //remove extra groupings starting at the beginning
			   }
			   textViewToBeAdjusted.setText(text);
		   }
		   else if(groupCount < unitsGroupCount){
			   textViewToBeAdjusted.append( new String(new char[unitsGroupCount-groupCount]).replaceAll("\0", dummyValue));
		   }
		   return true; 
		}
		   return false;
	}
	private String formatAnyGroupingTypes(String text){
		//Recursively tries to format grouping since fixing one kind of may disrupt an already fixed formatting
		//There is probably a more efficient graph theory algorithmic approach to this.
		//Strict regular expression requirement that lookbacks (unlike lookaheads) have fixed lengths caused some obstacles and limitations in implementation
		
		if(Pattern.compile("(?!\\A)}(?=[^{]+})").matcher(text).find()) // Add starting or terminating brace where one is missing	
			text = formatAnyGroupingTypes(text.replaceAll("(?!\\A)}(?=[^{]+})", "} {"));
		
		if(Pattern.compile("(?<![\\A}]){(?=[^}]+{)").matcher(text).find()) // Add starting or terminating brace where one is missing
			text = formatAnyGroupingTypes(text.replaceAll("(?<![\\A}]){(?=[^}]+{)", "} {"));
		
		if(Pattern.compile("}[^{}]+{").matcher(text).find()) //Nothing should be in between groupings
			text = formatAnyGroupingTypes(text.replaceAll("}[^{}]+{", "} {"));
		
		if(Pattern.compile("{[{]+").matcher(text).find()) // Remove extra starting grouping braces ie '{{{', etc
			text = formatAnyGroupingTypes(text.replaceAll("{[{]+", "{"));

		if(Pattern.compile("}[}]+").matcher(text).find()) // Remove extra starting grouping braces ie '}}}', etc
			text = formatAnyGroupingTypes(text.replaceAll("}[}]+", "}"));
		
		if(Pattern.compile("\\A}\\s*{").matcher(text).find()) // Remove terminating bracket at beginning
			text = formatAnyGroupingTypes(text.replaceAll("\\A}\\s*{", "{"));
		
		if(Pattern.compile("{\\Z").matcher(text).find()) //In most cases when a line ends an open brace, the actual intent would be a closing brace
			text = formatAnyGroupingTypes(text.replaceAll("{\\Z", "}"));

		if(Pattern.compile("(?<=}){(?=.+{)").matcher(text).find()) //Account for edge case
			text = formatAnyGroupingTypes(text.replaceAll("(?<=}){(?=.+{)", ""));
		
				
		return text;
	}
	private String formatUnitsGroupingTypes(String text){
		if(calculateGroupCount(text)==0)
			text = "{"+text+"}";
		return formatAnyGroupingTypes(text.replaceAll("{\\s*}", "")); //empty grouping are removed
	}
	private String formatValuesGroupingTypes(String text){
		return formatAnyGroupingTypes(text.replaceAll("{\\s*}", "{1}")); //empty grouping default to one
	}
	
	///
	private void setFromUnitsToQuantity(){
		setUnitsToQuantity(false);
	}
	private void setToUnitToQuantity(){
		setUnitsToQuantity(true);
	}
	private void setUnitsToQuantity(boolean isToQuantity){
		String unitNamesOrDimension = (isToQuantity?toUnitTextView:fromUnitTextView).getText().toString();
		unitNamesOrDimension = unitNamesOrDimension.trim();

		Quantity selectedQuantity = (isToQuantity)?toQuantity:fromQuantity;
		
		if(!(selectedQuantity.getUnitNames().replaceAll(Quantity.GROUPING_REGEX, "")
				                        .equalsIgnoreCase(unitNamesOrDimension.replaceAll(Quantity.GROUPING_REGEX, "")))){ //Replace anything only if there is difference
			if(unitNamesOrDimension.isEmpty()){
				selectedQuantity.setUnitValues(Arrays.asList(pSharablesApplication.getUnitManager().getUnitsDataModel().getUnknownUnit()), Arrays.asList(0.0));
			}
			else if(unitNamesOrDimension.matches(Quantity.UNIT_GROUPING_REGEX+"+")){
				Collection<Unit> units = Quantity.toValidatedUnits(unitNamesOrDimension, pSharablesApplication.getUnitManager());
				selectedQuantity.setUnitValues(units, Collections.nCopies(units.size(), 0.0));
			}
			else{
				selectedQuantity.setUnitValues(Arrays.asList(pSharablesApplication.getUnitManager().getUnitsDataModel().getUnit(unitNamesOrDimension)), Arrays.asList(0.0));
			}	
		}
		
		//Update conversion favorites rank
		for(Unit unit:(isToQuantity?toQuantity.getUnits():fromQuantity.getUnits()))
		{
			pSharablesApplication.getUnitManager().getConversionFavoritesDataModel()
											  .modifySignificanceRankOfMultipleConversions(unit, true);
		}
	}
	
	private boolean checkNSetFromValuesToQuantity(){
		String selectedFromValue = fromValueTextView.getText().toString();
	
		if(!selectedFromValue.equalsIgnoreCase("")
		   && !selectedFromValue.matches("\\s*0+[\\.0]*\\s*") //values that are only integer or double zero values are invalid
		   && fromQuantity.setValues(Quantity.toValidatedValues(selectedFromValue))){ //values only set when size matches size of units		
		    return true;			
		}
		else{
			fromValueTextView.setTextColor(Color.rgb(180, 0, 0));	
			
			//
			setConversionButton(false);
			return false;
		}
	}
	
	private boolean checkUnits(){
		if(UnitsDataModel.getDataModelCategory(fromQuantity.getLargestUnit()) != DATA_MODEL_CATEGORY.UNKNOWN
		   && UnitsDataModel.getDataModelCategory(toQuantity.getLargestUnit()) != DATA_MODEL_CATEGORY.UNKNOWN){
			if(fromQuantity.equalsUnit(toQuantity)){	
				conversionValueTextView.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in));
				conversionValueTextView.setText("##:Units Match");				
				
				//
				fromUnitTextView.setTextColor(Color.BLACK);
				toUnitTextView.setTextColor(Color.BLACK);
				conversionValueTextView.setTextColor(Color.BLACK);
				
				//	
				setConversionButton(true);
				
				return true;
			}
			else{
				conversionValueTextView.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in));
				conversionValueTextView.setText("XX:Units Don't Match");
				
				//
				fromUnitTextView.setTextColor(Color.rgb(180, 0, 0));
				toUnitTextView.setTextColor(Color.rgb(180, 0, 0));
				conversionValueTextView.setTextColor(Color.rgb(200, 0, 0));
				
				//
				setConversionButton(false);
				
				return false;
			}
		}
		else{	
			boolean isToUnk = UnitsDataModel.getDataModelCategory(toQuantity.getLargestUnit()) == DATA_MODEL_CATEGORY.UNKNOWN;
			boolean isFromUnk = UnitsDataModel.getDataModelCategory(fromQuantity.getLargestUnit()) == DATA_MODEL_CATEGORY.UNKNOWN;
		
			if(isToUnk && isFromUnk){
				conversionValueTextView.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in));
				conversionValueTextView.setText("??:'FROM'&'TO' Units Unknown");
				
				//
				fromUnitTextView.setTextColor(Color.rgb(180, 0, 0));
				toUnitTextView.setTextColor(Color.rgb(180, 0, 0));
				conversionValueTextView.setTextColor(Color.rgb(200, 0, 0));				
			}
			else if(isToUnk){
				conversionValueTextView.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in));
				conversionValueTextView.setText( toQuantity.isValid()?"#?:'TO' Unit Unknown"
						                                             :"Unit In 'TO' Group is Unkn. or Has Wrong Dim.");
				
				//
				fromUnitTextView.setTextColor(Color.rgb(0, 0, 0));				
				
				toUnitTextView.setTextColor(Color.rgb(180, 0, 0));
				conversionValueTextView.setTextColor(Color.rgb(200, 0, 0));
			}		
			else if(isFromUnk){
				conversionValueTextView.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in));
				conversionValueTextView.setText(toQuantity.isValid()?"?#:'FROM' Unit Unknown"
						                                             :"Unit In 'FROM' Group is Unkn. or Has Wrong Dim.");
				
				//
				fromUnitTextView.setTextColor(Color.rgb(180, 0, 0));
				conversionValueTextView.setTextColor(Color.rgb(200, 0, 0));	
				
				toUnitTextView.setTextColor(Color.rgb(0, 0, 0));					
			}
			else{
				toUnitTextView.setTextColor(Color.rgb(0, 0, 0));
				fromUnitTextView.setTextColor(Color.rgb(0, 0, 0));
			}
			
			//
			setConversionButton(false);
			
			return false;
		}		
	}
	private void getConversion(){
		toQuantity.setValues(fromQuantity.toUnitsGroup(toQuantity.getUnits()).getValues());	
				
		conversionValueTextView.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.slide_in_left));
		conversionValueTextView.setText(String.valueOf(toQuantity.getValuesString()));
		
		//
		setConversionButton(false);
	}
	
	private void setConversionButton(boolean triggerFocus){
		if(triggerFocus){
			convertButton.setTextColor(Color.BLUE);
			convertButton.startAnimation(convertButtonAnimation);
		}
		else{
			convertButton.setTextColor(Color.BLACK);
			convertButton.clearAnimation();
		}
	}
	
	///Loader Manager methods
	@Override
	public Loader<UnitManagerBuilder> onCreateLoader(int id, Bundle arg1) {
		if(id == LOADER.LOCAL_UNITS_LOADER.ordinal()){
			return new UnitsMapXmlLocalReader(this);
		}else if(id == LOADER.LOCAL_PREFIXES_LOADER.ordinal()){
			return new PrefixesMapXmlLocalReader(this);	
		}else if(id == LOADER.FUND_UNITS_LOADER.ordinal()){
			return new FundamentalUnitsMapXmlLocalReader(this);
		}else if(id == LOADER.ONLINE_CURRENCY_UNITS_LOADER.ordinal()){
			return new CurrencyUnitsMapXmlOnlineReader(this);
		} else if(id == LOADER.ONLINE_PREFIXES_N_UNITS_LOADER.ordinal()){
			return new PrefixesNUnitsMapXmlOnlineReader(this);
		} else if(id == LOADER.POST_LOADER.ordinal()){
			return new AsyncTaskLoader<UnitManagerBuilder>(this){
				@Override
				public UnitManagerBuilder loadInBackground() {
					((PersistentSharablesApplication)getContext().getApplicationContext()).recreateUnitManager();
					return null;
				}	
			};
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
  				getSupportLoaderManager().initLoader(LOADER.POST_LOADER.ordinal(), null, this).forceLoad();
  			}
  		}
	}

	@Override
	public void onLoaderReset(Loader<UnitManagerBuilder> arg0) {	}	
}
