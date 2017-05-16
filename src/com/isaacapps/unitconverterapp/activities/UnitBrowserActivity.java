package com.isaacapps.unitconverterapp.activities;

import java.util.*;

import com.isaacapps.unitconverterapp.activities.R;
import com.isaacapps.unitconverterapp.models.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.Utility;

import android.app.Activity;
import android.os.Bundle;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.widget.*;
import android.widget.AdapterView.*;

public class UnitBrowserActivity extends Activity {	
	private String DELIMITER = " :: ", PREFIX_IDENTITY_SYMBOL = "- - - - - - -";
	
	private PersistentSharablesApplication pSharablesApplication;	

	private Spinner dimFilterSpinner,unitSystemSpinner,categorySpinner;
	private Spinner unitSpinner, prefixSpinner;

	private Button selectUnitButton, cancelUnitButton,deleteUnitButton;

	private String oppositeUnitType, callerButtonUnitType, oppositeUnitCategoryName;
	
	private String[] generalFilters;

	///
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_unit_browse);
		
		pSharablesApplication = (PersistentSharablesApplication)this.getApplication();
		
		//Set unit category and the unit type of the opposite unit based on the current unit for which activity was invoked.
		Bundle extras = getIntent().getExtras();	
		callerButtonUnitType = extras != null? extras.getString("buttonName"):"";
		oppositeUnitType = extras != null? (callerButtonUnitType.equalsIgnoreCase("to") ? "from":"to"): "";
				
		generalFilters =  new String[]{"| All | Units", "| "+oppositeUnitType+" | "+"Units", "| Dynamic | Units", "| Currency | Units"
				                       ,"| Metric | Units", "| Special | Units"};
		
		//
		setTitle("Select "+callerButtonUnitType.toUpperCase()+" Unit");
		setupUIComponents();
		
		//
		populateSpinners();
		
		//
		addListenerOnUnitBrowserButtons();
		addListenerOnSelectionSpinners();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.unitbrowser, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case R.id.delete_all_dynamic_units:
			pSharablesApplication.getUnitManager().getUnitsDataModel().removeAllDynamicUnits();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
		
	}
	
	///Setup UI Components Methods
	private void setupUIComponents(){
		dimFilterSpinner = (Spinner) findViewById(R.id.dimFilterSpinner);
		unitSystemSpinner = (Spinner) findViewById(R.id.unitSystemSpinner);
		categorySpinner = (Spinner) findViewById(R.id.categorySpinner);
		unitSpinner = (Spinner) findViewById(R.id.unitSpinner);
		prefixSpinner = (Spinner) findViewById(R.id.prefixSpinner);
		//
		selectUnitButton = (Button) findViewById(R.id.selectUnitButton);
		cancelUnitButton = (Button) findViewById(R.id.cancelUnitButton);
		deleteUnitButton = (Button) findViewById(R.id.deleteUnitButton);
		deleteUnitButton.setVisibility(View.GONE);
	}
	
	private void showDeleteButtonIfSelectedUnitDeleteable(String selectedUnitName){
		Unit unitToBePotentiallyDeleted = pSharablesApplication.getUnitManager().getUnitsDataModel().getUnit(selectedUnitName);
		
		/*Only delete units that meet the following criteria:
		  1.Not currently being used as a 'from' or 'to' unit.
		  2.Not a core unit (ie. meter, second, currency, etc)
	    */
		if(!unitToBePotentiallyDeleted.isCoreUnit() 
		   && !pSharablesApplication.getFromQuantity().getUnits().equals(unitToBePotentiallyDeleted)
		   && !pSharablesApplication.getToQuantity().getUnits().equals(unitToBePotentiallyDeleted)){
			
			deleteUnitButton.setVisibility(View.VISIBLE);
		}
		else{
			deleteUnitButton.setVisibility(View.GONE);
		}
	}
	private void showSelectButtonIfSelectedUnitSelectable(String selectedUnitName){		
		/*Only show select button that meet the following criteria: Not a COMPLETELY unknown
	    */
		if(selectedUnitName != null && !selectedUnitName.equalsIgnoreCase("No Compatible Units")){
			
			selectUnitButton.setVisibility(View.VISIBLE);
		}
		else{
			selectUnitButton.setVisibility(View.GONE);
		}
	}
	
	
	///Button Listeners Methods
	private void addListenerOnUnitBrowserButtons(){
		selectUnitButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				String prefix = ((String)prefixSpinner.getSelectedItem()).replace(PREFIX_IDENTITY_SYMBOL, "").split(DELIMITER)[0].trim();
				String mainUnitName = ((String)unitSpinner.getSelectedItem()).split("-")[0].trim();
				
				if(mainUnitName != null){
					Unit selectedUnit = pSharablesApplication.getUnitManager().getUnitsDataModel().getUnit( prefix + ((prefix.length()!=0)?"-":"") + mainUnitName );
					
					if(callerButtonUnitType.equalsIgnoreCase("from")){
						pSharablesApplication.getFromQuantity().setUnits((Collection)Arrays.asList(selectedUnit));				
					}
					else if(callerButtonUnitType.equalsIgnoreCase("to")){
						pSharablesApplication.getToQuantity().setUnits((Collection)Arrays.asList(selectedUnit));	
					}
				}
				
				finish();
			}
		});
		
		cancelUnitButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				finish();
			}
		});	
		
		deleteUnitButton.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {
				String mainUnitName = (String)unitSpinner.getSelectedItem();	
				pSharablesApplication.getUnitManager().getUnitsDataModel().removeDynamicUnit(mainUnitName);
				
				//
				unitSpinner.startAnimation(AnimationUtils.loadAnimation(UnitBrowserActivity.this, android.R.anim.slide_out_right));
				populateUnitSpinner();
				unitSpinner.setSelection(0);
				unitSpinner.setSelected(true);
			}
		});
	}
	
	///Selection Spinner Listener Methods	
	private void addListenerOnSelectionSpinners(){
		dimFilterSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if(((String)dimFilterSpinner.getSelectedItem()).equalsIgnoreCase(generalFilters[1])){
					if(oppositeUnitType.equalsIgnoreCase("to")){
						oppositeUnitCategoryName = pSharablesApplication.getToQuantity().getLargestUnit().getCategory();
					}
					else if(oppositeUnitType.equalsIgnoreCase("from")){
						oppositeUnitCategoryName = pSharablesApplication.getFromQuantity().getLargestUnit().getCategory();
					}
				}
				
				populateUnitSystemSpinner();
				unitSystemSpinner.setSelection(0);
				unitSystemSpinner.setSelected(true);
				
				///
				unitSystemSpinner.startAnimation(AnimationUtils.loadAnimation(UnitBrowserActivity.this, android.R.anim.slide_in_left));
				categorySpinner.startAnimation(AnimationUtils.loadAnimation(UnitBrowserActivity.this, android.R.anim.slide_in_left));
				unitSpinner.startAnimation(AnimationUtils.loadAnimation(UnitBrowserActivity.this, android.R.anim.slide_in_left));
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
		
		unitSystemSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				populateCategorySpinner();
				categorySpinner.setSelection(0);
				categorySpinner.setSelected(true);
				
				///
				categorySpinner.startAnimation(AnimationUtils.loadAnimation(UnitBrowserActivity.this, android.R.anim.slide_in_left));
				unitSpinner.startAnimation(AnimationUtils.loadAnimation(UnitBrowserActivity.this, android.R.anim.slide_in_left));
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
		
		categorySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				populateUnitSpinner();
				unitSpinner.setSelection(0);
				unitSpinner.setSelected(true);
				
				///
				unitSpinner.startAnimation(AnimationUtils.loadAnimation(UnitBrowserActivity.this, android.R.anim.slide_in_left));
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
		
		unitSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				populatePrefixSpinner();
				prefixSpinner.setSelection(0);
				prefixSpinner.setSelected(true);
				
				///
				prefixSpinner.startAnimation(AnimationUtils.loadAnimation(UnitBrowserActivity.this, android.R.anim.slide_in_left));
				
				///
				showDeleteButtonIfSelectedUnitDeleteable((String)parent.getItemAtPosition(position));
				showSelectButtonIfSelectedUnitSelectable((String)parent.getItemAtPosition(position));
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	///Populate Spinners. Spinners dynamically update selection of spinner higher in hierarchy and restrictions of general filter spinner. 
	private void populateSpinners(){
		populateDimensionFilterSpinner();
		dimFilterSpinner.startAnimation(AnimationUtils.loadAnimation(UnitBrowserActivity.this, android.R.anim.slide_in_left));
		dimFilterSpinner.setSelection(0);
		dimFilterSpinner.setSelected(true);
		
		populateUnitSystemSpinner();
		unitSystemSpinner.setSelection(0);
		unitSystemSpinner.setSelected(true);
		
		populateCategorySpinner();
		categorySpinner.setSelection(0);
		categorySpinner.setSelected(true);
		
		populateUnitSpinner();
		unitSpinner.setSelection(0);
		unitSpinner.setSelected(true);
		
		populatePrefixSpinner();
		prefixSpinner.startAnimation(AnimationUtils.loadAnimation(UnitBrowserActivity.this, android.R.anim.slide_in_left));
		prefixSpinner.setSelection(0);
		prefixSpinner.setSelected(true);
	}
	private void populateDimensionFilterSpinner(){
		dimFilterSpinner.setAdapter(new ArrayAdapter<String>(this, R.layout.unit_browse_spinner_item, generalFilters));
	}
	private void populateUnitSystemSpinner(){
		Set<String> unitSystems = new TreeSet<String>(); //Naturally ordered and no duplicates
		String spinnerSelection = (String)dimFilterSpinner.getSelectedItem();

		if(spinnerSelection.equals(generalFilters[0])){
			for(String unitSystem:pSharablesApplication.getUnitManager().getUnitsClassifierDataModel().getAllUnitSystems())
				unitSystems.add(unitSystem);

			unitSystems.remove(pSharablesApplication.getUnitManager().getUnitsDataModel().getUnknownUnit().getUnitSystem());	
		}
		else if(spinnerSelection.equals(generalFilters[2])){
			for(Unit dynamicUnit:pSharablesApplication.getUnitManager().getUnitsDataModel().getDynamicUnits())
				unitSystems.add(dynamicUnit.getUnitSystem());
		}
		else{
			if(spinnerSelection.equalsIgnoreCase(generalFilters[3])){
				oppositeUnitCategoryName = "currency_unit";
			}
			else if(spinnerSelection.equalsIgnoreCase(generalFilters[4])){
				oppositeUnitCategoryName = "metric";
			}
			else if(spinnerSelection.equalsIgnoreCase(generalFilters[5])){
				oppositeUnitCategoryName = "special";
			}

			boolean oppositeUnitIsNotUnknown = pSharablesApplication.getToQuantity().getLargestUnit().getName().equalsIgnoreCase(Unit.UNKNOWN_UNIT_NAME) 
					                           		&& oppositeUnitType.equalsIgnoreCase("to") 
					                           || pSharablesApplication.getFromQuantity().getLargestUnit().getName().equalsIgnoreCase(Unit.UNKNOWN_UNIT_NAME) 
					                           		&& oppositeUnitType.equalsIgnoreCase("from");
			
			//Only add relevant unit system.
			for(String unitSystem:pSharablesApplication.getUnitManager().getUnitsClassifierDataModel().getAllUnitSystems()){	
				if(spinnerSelection.equalsIgnoreCase(generalFilters[1]) && oppositeUnitIsNotUnknown || !spinnerSelection.equalsIgnoreCase(generalFilters[1])
				   && pSharablesApplication.getUnitManager().getUnitsClassifierDataModel().containsUnitCategoryInUnitSystem(unitSystem, oppositeUnitCategoryName, true))
				{	
					unitSystems.add(unitSystem);
				}
			}
		}
		
		if(unitSystems.isEmpty())
			unitSystems.add("No Compatible Unit Systems");

		unitSystemSpinner.setAdapter(new ArrayAdapter<String>(this, R.layout.unit_browse_spinner_item, new ArrayList<String>(unitSystems)));
	}
	private void populateCategorySpinner(){
		Set<String> categories = new TreeSet<String>(); //Naturally ordered and no duplicates
		String dimSpinnerSelection = (String)dimFilterSpinner.getSelectedItem();
		String unitSystemSpinnerSelection = (String)unitSystemSpinner.getSelectedItem();
		
		if(dimSpinnerSelection.equals(generalFilters[0])||dimSpinnerSelection.equals(generalFilters[2])){
			categories.addAll(pSharablesApplication.getUnitManager().getUnitsClassifierDataModel().getUnitCategoriesInUnitSystem(unitSystemSpinnerSelection));
			
			//Remove categories not relevant to dynamic units.
			if(dimSpinnerSelection.equals(generalFilters[2])){
				for(Unit dynamicUnit:pSharablesApplication.getUnitManager().getUnitsDataModel().getDynamicUnits()){
					if(categories.contains(dynamicUnit.getCategory())){
						categories.remove(dynamicUnit.getCategory());
					}		
				}
			}
		}
		else{
			categories.add(oppositeUnitCategoryName);
		}

		categorySpinner.setAdapter(new ArrayAdapter<String>(this, R.layout.unit_browse_spinner_item, new ArrayList<String>(categories)));
	}
	private void populateUnitSpinner(){
		Set<String> unitsNames = new TreeSet<String>(); //Sort by default alphabetical  
		
		String dimFilterSelection = (String)dimFilterSpinner.getSelectedItem(),
			   unitSystemSelection = (String)unitSystemSpinner.getSelectedItem(),
		       categorySelection = (String)categorySpinner.getSelectedItem();
		
		if((pSharablesApplication.getToQuantity().getLargestUnit().getName().equalsIgnoreCase(Unit.UNKNOWN_UNIT_NAME) && oppositeUnitType.equalsIgnoreCase("to") ||
			pSharablesApplication.getFromQuantity().getLargestUnit().getName().equalsIgnoreCase(Unit.UNKNOWN_UNIT_NAME) && oppositeUnitType.equalsIgnoreCase("from")) 
			&& dimFilterSelection.equals(generalFilters[1]))
		{ 			
			unitsNames.add("No Units Have A Dimension Compatible to the "+oppositeUnitType.toUpperCase()+" Unit.");	
		}
		else if(pSharablesApplication.getUnitManager().getUnitsDataModel().getDynamicUnits().isEmpty()
				&& dimFilterSelection.equals(generalFilters[2]))
		{
			unitsNames.add("No Dynamic Units");
		}
		else{
			//Include abbreviation with unit full name
			String abbreviation;
			Unit unit;
			
			for(String unitName:pSharablesApplication.getUnitManager().getUnitsClassifierDataModel().getUnitNamesByUnitSystemNCategory(unitSystemSelection, categorySelection)){
				unit = pSharablesApplication.getUnitManager().getUnitsDataModel().getUnit(unitName);
				if((dimFilterSelection.equals(generalFilters[2]) && !unit.isCoreUnit()) || !dimFilterSelection.equals(generalFilters[2]) ){
					abbreviation = unit.getAbbreviation();
					unitsNames.add( (!unitName.equalsIgnoreCase(abbreviation))? unitName+" - "+unit.getAbbreviation(): unitName);
				}
			}
			
			if(unitsNames.isEmpty())
				unitsNames.add("Sorry, no units in category due to current \'General Filter\' selection");
		}
		
		unitSpinner.setAdapter(new ArrayAdapter<String>(this, R.layout.unit_browse_spinner_item
				               , (unitsNames!=null)?new ArrayList<String>(unitsNames):new ArrayList<String>()));
	}
	
	private void populatePrefixSpinner(){
		List<String> prefixesFullNames = new ArrayList<String>();
		prefixesFullNames.add(PREFIX_IDENTITY_SYMBOL+DELIMITER+"1.0"); //When no selected prefix is applied, the identity factor is used.
		
		/*Only display prefixes if the unit name itself does not contain a prefix. 
		 *Make sure that unit name does not contain any complex term combinations.
		 *Also make sure it is not a currency. */
		String categorySelection = (String)categorySpinner.getSelectedItem(), unitSelection = (String)unitSpinner.getSelectedItem();
		
		if( !(unitSelection).equalsIgnoreCase("No Compatible Units")
		    && pSharablesApplication.getUnitManager().getPrefixesDataModel().getPrefixMatches(unitSelection, true).isEmpty()
			&& !Utility.unitNameHasComplexDimensions(unitSelection)
			&& !(categorySelection).equalsIgnoreCase("currency_unit")){
			
			for(String prefixFullName:pSharablesApplication.getUnitManager().getPrefixesDataModel().getAllPrefixFullNames()){
				prefixesFullNames.add(prefixFullName+DELIMITER+pSharablesApplication.getUnitManager().getPrefixesDataModel().getPrefixValue(prefixFullName));
			}
			
			//Sort prefix list from least to greatest factor
			Collections.sort(prefixesFullNames, new Comparator<String>(){
				@Override
				public int compare(String arg0, String arg1) {
					return Double.compare(pSharablesApplication.getUnitManager().getPrefixesDataModel().getPrefixValue(arg0.split(DELIMITER)[0])
							              ,pSharablesApplication.getUnitManager().getPrefixesDataModel().getPrefixValue(arg1.split(DELIMITER)[0]));
				}});
		}
		
		prefixSpinner.setAdapter(new ArrayAdapter<String>(this, R.layout.unit_browse_spinner_item, prefixesFullNames));
	}	
}
