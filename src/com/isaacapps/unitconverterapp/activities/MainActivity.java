package com.isaacapps.unitconverterapp.activities;

import java.util.*;
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
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.isaacapps.unitconverterapp.activities.R;
import com.isaacapps.unitconverterapp.dao.xml.readers.local.*;
import com.isaacapps.unitconverterapp.dao.xml.readers.online.*;
import com.isaacapps.unitconverterapp.models.*;
import com.isaacapps.unitconverterapp.models.unitmanager.*;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager.DATA_MODEL_CATEGORY;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.UnitsDataModel;

public class MainActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<UnitManagerBuilder>{
	private static enum LOADER{LOCAL_UNITS_LOADER, FUND_UNITS_LOADER, ONLINE_CURRENCY_UNITS_LOADER
		                          , LOCAL_PREFIXES_LOADER, ONLINE_PREFIXES_N_UNITS_LOADER, POST_LOADER}

	PersistentSharablesApplication pSharablesApplication;
	UnitManager unitManager;
	Quantity fromQuantity, toQuantity;

	ProgressBar unitManagerLoaderProgressBar;
	TextView progressBarTextView;

	Button fromUnitBrowseButton, toUnitBrowseButton, convertButton;	
	Button fromUnitViewInfoButton, toUnitViewInfoButton;
	ToggleButton fromValueExpressionToggleButton;
	
	Animation convertButtonAnimation;
	
	MultiAutoCompleteTextView fromUnitTextView;
	TextView fromValueTextView;
	MultiAutoCompleteTextView toUnitTextView;
	TextView conversionValueTextView;

	AlertDialog unitInfoDialog;
	
	MenuItem multiModeMenuItem;
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
			if (fromQuantity.hasValidGroup()){
				fromUnitTextView.setText(fromQuantity.getUnitNames());	
				fromValueTextView.setText(fromQuantity.getValuesString());
			}
			
			if (toQuantity.hasValidGroup())
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
		multiModeMenuItem = menu.findItem(R.id.multiUnitModeItem);
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
			
			setFromUnitsIntoQuantity();
			setToUnitIntoQuantity();
			
			if(checkUnits() && checkNSetFromValuesIntoQuantity())
				getConversion();
			
			return true;
		case R.id.multiUnitModeItem:
			isMultiUnitMode = !item.isChecked();
			item.setChecked(isMultiUnitMode);
			item.setTitle(item.getTitle().toString().replaceFirst(":.+", isMultiUnitMode?":ON":":OFF"));
			item.setTitleCondensed(item.getTitleCondensed().toString().replaceFirst(":.+", isMultiUnitMode?":ON":":OFF"));
			
			if(isMultiUnitMode){
				//No grouping exist, then add some dummy ones for demonstration purposes
				if(calculateGroupCount(fromUnitTextView.getText().toString()) == 0)
					fromUnitTextView.setText("{a} {b} {c}");
				if(calculateGroupCount(toUnitTextView.getText().toString()) == 0)
					toUnitTextView.setText("{a} {b} {c}");
				
				fromValueTextView.setText(formatValuesGroupingTypes(fromValueTextView.getText().toString()));
				
				adjustGroupingsCount(fromValueTextView, fromQuantity.getUnits().size(), " {1}");
				
				fromValueExpressionToggleButton.setChecked(false); //Grouping are not recognized by the expression evaluator
			}
			
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
		fromValueExpressionToggleButton = (ToggleButton) findViewById(R.id.fromValueExpressionToggleButton);
		
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
			((LinearLayout)findViewById(R.id.convertLinearLayout)).setVisibility(View.INVISIBLE);
			((LinearLayout)findViewById(R.id.toLinearLayout)).setVisibility(View.INVISIBLE);
		}
	}
	private void postLoadSetup(){
		((LinearLayout)findViewById(R.id.progressBarLinearLayout)).setVisibility(View.GONE);
		((LinearLayout)findViewById(R.id.fromLinearLayout)).setVisibility(View.VISIBLE);
		((LinearLayout)findViewById(R.id.convertLinearLayout)).setVisibility(View.VISIBLE);
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
		
		fromValueExpressionToggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				//TODO: Make necessary preparation to use the expression evaluator some third party library. Possibily, the JEL third party library
				
				if(multiModeMenuItem.isChecked())//Disable multi-unit group to prevent interference with expression evalutation 
					onOptionsItemSelected(multiModeMenuItem);
				
				//
			}
		});
		
		convertButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				
				setFromUnitsIntoQuantity();
				setToUnitIntoQuantity();
				
				if(checkUnits() && checkNSetFromValuesIntoQuantity())
					getConversion();
			}
		});
	}
	private String getUnitDetailsMessage(Collection<Unit> units){
		Unit firstUnit = units.iterator().next(); //Assumption is that all units in collection are similar
		
		String category = firstUnit.getCategory();
		String fundamentalTypesDim = UnitsDataModel.getDataModelCategory(firstUnit) != DATA_MODEL_CATEGORY.UNKNOWN
												?"": Utility.getFundamentalUnitTypesDimensionAsString(firstUnit.getFundamentalUnitTypesDimension());
		String description = units.size()==1?firstUnit.getDescription():"";
		
		return (category.equals(firstUnit.getUnitSystem()
								 +fundamentalTypesDim.toLowerCase())?"":"\n\nCategory: "+ category) //Prevent display of duplicate data in case of complex units  
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
					
					setFromUnitsIntoQuantity();
					
					if(isMultiUnitMode){
						adjustGroupingsCount(fromValueTextView, fromQuantity.getUnits().size(), " {1}");
					}
															
					checkUnits();
					
					checkNSetFromValuesIntoQuantity();
				}
			}
			
		});
	
		toUnitTextView.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if(!hasFocus){
					
					if(isMultiUnitMode)
						((TextView)view).setText(formatUnitsGroupingTypes(((TextView)view).getText().toString()));
					
					setToUnitIntoQuantity();
															
					checkUnits();
					
					checkNSetFromValuesIntoQuantity();
				}
			}
		});
		
		fromValueTextView.setOnFocusChangeListener(new OnFocusChangeListener(){
			
			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if(!hasFocus){
					if(isMultiUnitMode){
						String fromValueText = formatValuesGroupingTypes(((TextView)view).getText().toString());
					    ((TextView)view).setText(fromValueText);
						adjustGroupingsCount(fromUnitTextView, calculateGroupCount(fromValueText), " {a}");
					}
					
					checkNSetFromValuesIntoQuantity();
				}
			}
		});
	}
	
	///Multi-Unit Grouping Formatting Methods
	private int calculateGroupCount(String text){
		//Recursively find the number of '{ [anything in between] }' groupings. Only accounts for kernel groupings if groups are nested.
		if(text.indexOf("{") != -1 && text.indexOf("}") != -1 ){
			if(text.indexOf("{") < text.indexOf("}")){
				return 1 + calculateGroupCount(text.substring(text.indexOf("}")+1));
			}
			else{ // if the braces are not arranged appropriately, then do not tally count , ie '}[anything in between]{'
				return calculateGroupCount(text.substring(text.indexOf("}")+1));
			}
		}
		else{
			return 0;
		}
	}
	private boolean adjustGroupingsCount(TextView textViewToBeAdjusted, int referenceGroupCount, String dummyValue){
		String text = textViewToBeAdjusted.getText().toString();
		int groupCount = calculateGroupCount(text);
		if(referenceGroupCount>1){
		   //Makes grouping units and value groupings are identical
		   if(groupCount > referenceGroupCount){
			   while(calculateGroupCount(text) != referenceGroupCount ){
				   text = text.replaceFirst("\\{[^\\{\\}]*\\}", ""); //remove extra groupings starting at the beginning
			   }
			   textViewToBeAdjusted.setText(text);
		   }
		   else if(groupCount < referenceGroupCount){
			   textViewToBeAdjusted.append( new String(new char[referenceGroupCount-groupCount]).replaceAll("\0", dummyValue));
		   }
		   return true; 
		}
		   return false;
	}
	private String formatAnyGroupingTypes(String text){
		/*Recursively tries to format grouping since fixing one kind of may disrupt an already fixed formatting
		 There is probably a more efficient graph theory algorithmic approach to this.
		 Strict regular expression restrictions that lookbacks (unlike lookaheads) have fixed lengths caused some obstacles and limitations in implementation */
		
		String missingStartBraceRegEx = "(?!\\A)\\}(?=[^\\{]+\\})"
			   ,missingEndBraceRegEx = "(?<!\\A|\\})\\{(?=[^\\}]+\\{)"
			   ,betweenGroupingRegEx = "\\}[^\\{\\}\\s]+\\{"
			   ,extraStartBraceRegEx = "\\{[\\{]+"
			   ,extraEndBraceRegEx = "\\}[\\}]+"
			   ,endBraceAtBeginningRegEx = "\\A\\}\\s*\\{"
			   ,startingBraceAtEnd = "\\{\\Z"
			   ,edgecase = "(?<=\\})\\{(?=.+\\{)";
		
		
		 // Add missing starting brace as long as the adjacent terminating brace is not in the beginning of the string, ie {aa}bb}... --> {aa}{bb}
		if(Pattern.compile(missingStartBraceRegEx).matcher(text).find())	
			text = formatAnyGroupingTypes(text.replaceAll(missingStartBraceRegEx, "} {"));
		
		// Add missing terminating brace as long as the adjacent beginning brace is not in the beginning of string or immediately to the right of a terminating brace , ie. {aa{bb}... --> {aa}{bb}
		if(Pattern.compile(missingEndBraceRegEx).matcher(text).find())   
			text = formatAnyGroupingTypes(text.replaceAll(missingEndBraceRegEx, "} {"));
		
		if(Pattern.compile(betweenGroupingRegEx).matcher(text).find()) //Nothing should be in between groupings, ie. '} dkfj [too many space characters] {' --> '} {'
			text = formatAnyGroupingTypes(text.replaceAll(betweenGroupingRegEx, "} {"));
		
		if(Pattern.compile(extraStartBraceRegEx).matcher(text).find()) // Remove extra starting braces ie '{{{' --> '{'
			text = formatAnyGroupingTypes(text.replaceAll(extraStartBraceRegEx, "{"));

		if(Pattern.compile(extraEndBraceRegEx).matcher(text).find()) // Remove extra starting braces ie '}}}' --> '}'
			text = formatAnyGroupingTypes(text.replaceAll(extraEndBraceRegEx, "}"));
		
		if(Pattern.compile(endBraceAtBeginningRegEx).matcher(text).find()) // Remove terminating bracket at beginning, ie '} {asnd}' --> '{asnd}'
			text = formatAnyGroupingTypes(text.replaceAll(endBraceAtBeginningRegEx, "{"));
		
		if(Pattern.compile(startingBraceAtEnd).matcher(text).find()) //In most cases when a line ends an starting brace, the actual intent would be a closing brace
			text = formatAnyGroupingTypes(text.replaceAll(startingBraceAtEnd, "}"));

		if(Pattern.compile(edgecase).matcher(text).find()) //Account for some edge case.
			text = formatAnyGroupingTypes(text.replaceAll(edgecase, ""));
		
		return text;
	}
	private String formatUnitsGroupingTypes(String text){
		if(calculateGroupCount(text)==0)
			text = "{"+text+"}";
		return formatAnyGroupingTypes(text.replaceAll("\\{\\s*\\}", "")); //empty grouping are removed
	}
	private String formatValuesGroupingTypes(String text){
		return formatAnyGroupingTypes(text.replaceAll("\\{\\s*\\}", "{1}")); //empty grouping default to one
	}
	
	///Quantity Setting and Validation Methods
	private void setFromUnitsIntoQuantity(){
		setUnitsIntoQuantity(false);
	}
	private void setToUnitIntoQuantity(){
		setUnitsIntoQuantity(true);
	}
	private void setUnitsIntoQuantity(boolean isToQuantity){
		String unitNamesOrDimension = (isToQuantity?toUnitTextView:fromUnitTextView).getText().toString();
		unitNamesOrDimension = unitNamesOrDimension.trim();

		Quantity selectedQuantity = (isToQuantity)?toQuantity:fromQuantity;
		
		//User are allowed to enter a singular unit name without grouping brackets if multi-unit mode is not on.
		if(!(selectedQuantity.getUnitNames().replaceAll(Quantity.GROUPING_REGEX, "")
				                        .equalsIgnoreCase(unitNamesOrDimension.replaceAll(Quantity.GROUPING_REGEX, "")))){ //Replace anything only if there is difference
			if(unitNamesOrDimension.isEmpty()){
				selectedQuantity.setUnitValues(Arrays.asList(pSharablesApplication.getUnitManager().getUnitsDataModel().getUnknownUnit()), Arrays.asList(1.0));
			}
			else{
				selectedQuantity.setUnitValues(unitNamesOrDimension
						           ,new String(new char[calculateGroupCount(unitNamesOrDimension)]).replaceAll("\0", "{1}")  //Associate units with placeholder values, since values will be set and checked later on.
						           ,pSharablesApplication.getUnitManager()); 
			}
		}
		
		//Update conversion favorites rank
		for(Unit unit:(isToQuantity?toQuantity.getUnits():fromQuantity.getUnits()))
		{
			pSharablesApplication.getUnitManager().getConversionFavoritesDataModel()
											  .modifySignificanceRankOfMultipleConversions(unit, true);
		}
	}
	
	private boolean checkNSetFromValuesIntoQuantity(){
		String selectedFromValue = fromValueTextView.getText().toString();
	
		if(!selectedFromValue.equalsIgnoreCase("")
		   && !selectedFromValue.matches("\\s*0+[\\.0]*\\s*") //values that are only integer or double zero values are invalid
		   && fromQuantity.setValues(Quantity.toValidatedValues(!selectedFromValue
				                                                   .matches(Quantity.VALUES_GROUPING_REGEX+"+")
				                                                                   ?"{"+selectedFromValue+"}"
				                                                                		   :selectedFromValue)))
		{ 	
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
		boolean FromUnitIsUnknown = UnitsDataModel.getDataModelCategory(fromQuantity.getLargestUnit()) == DATA_MODEL_CATEGORY.UNKNOWN;
		boolean ToUnitIsUnknown = UnitsDataModel.getDataModelCategory(toQuantity.getLargestUnit()) == DATA_MODEL_CATEGORY.UNKNOWN;
		boolean UnitsMatch = fromQuantity.equalsUnit(toQuantity);
		boolean UnitsAreOK = !(FromUnitIsUnknown || ToUnitIsUnknown) && UnitsMatch;

		fromUnitTextView.setTextColor(Color.rgb(FromUnitIsUnknown?180:0, 0, 0));
		toUnitTextView.setTextColor(Color.rgb(ToUnitIsUnknown?180:0, 0, 0));	
		conversionValueTextView.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in));
		conversionValueTextView.setTextColor(Color.rgb(UnitsAreOK? 0:200, 0, 0));
		
		setConversionButton(UnitsAreOK);
		
		//
		if(UnitsAreOK){		
			conversionValueTextView.setText("##:Units Match");	
		}
		else if(!(FromUnitIsUnknown || ToUnitIsUnknown) && !UnitsMatch){
			conversionValueTextView.setText("XX:Units Don't Match");
		}
		else{				
			String unknownsName = (FromUnitIsUnknown?"'FROM', ":"")+(ToUnitIsUnknown?"'TO', ":"");
			String unknownsSymbol = (FromUnitIsUnknown?"?":"#")+(ToUnitIsUnknown?"?":"#");
			
			conversionValueTextView.setText(unknownsSymbol+": Unit In "+unknownsName+" Group(s) is Unkn. or Has Wrong Dim.");
		}
		
		return UnitsAreOK;
	}
	
	///Conversion Methods
	private void getConversion(){
		toQuantity.setValues(fromQuantity.convertToUnitsGroup(toQuantity.getUnits()).getValues());	
				
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
	
	///Loader Manager Methods
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
