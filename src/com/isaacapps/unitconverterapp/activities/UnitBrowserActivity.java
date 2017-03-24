package com.isaacapps.unitconverterapp.activities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.isaacapps.unitconverterapp.activities.R;
import com.isaacapps.unitconverterapp.models.Unit;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class UnitBrowserActivity extends Activity {
	PersistentSharablesApplication pSharablesApplication;	

	Spinner dimFilterSpinner;
	Spinner unitSystemSpinner;
	Spinner categorySpinner;
	Spinner unitSpinner;
	Spinner prefixSpinner;

	Button selectUnitButton;
	Button cancelUnitButton;
	Button deleteUnitButton;

	String oppositeUnitType;
	String callerButtonUnitType;
	String oppositeUnitCategoryName;

	///
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_unit_browse);
		
		pSharablesApplication = (PersistentSharablesApplication)this.getApplication();	

		setupUIComponents();

		//
		oppositeUnitType = "";
		oppositeUnitCategoryName = "";
		
		//Set unit category and the unit type of the opposite unit based on the current unit for which activity was invoked.
		Bundle extras = getIntent().getExtras();
		if(extras != null){			
			callerButtonUnitType = extras.getString("buttonName");
			oppositeUnitType = callerButtonUnitType.equalsIgnoreCase("to") ? "from":"to";
		}
		
		if(oppositeUnitType.equalsIgnoreCase("to")){
			oppositeUnitCategoryName = pSharablesApplication.getToQuantity().getUnit().getCategory();
		}
		else if(oppositeUnitType.equalsIgnoreCase("from")){
			oppositeUnitCategoryName = pSharablesApplication.getFromQuantity().getUnit().getCategory();
		}
		
		setTitle("Select "+callerButtonUnitType.toUpperCase()+" Unit");
		
		//
		populateSpinners();
		
		//
		addListenerOnUnitBrowserButtons();
		addListenerOnSelectionSpinners();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
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
		Unit unitToBePotentiallyDeleted = pSharablesApplication.getUnitManager().getQueryExecutor().getUnit(selectedUnitName);
		
		/*Only delete units that meet the following criteria:
		  1.Not currently being used as a 'from' or 'to' unit.
		  2.Not a core unit (ie. meter, second, currency, etc)
	    */
		if(!unitToBePotentiallyDeleted.isCoreUnit() 
		   && !pSharablesApplication.getFromQuantity().getUnit().equals(unitToBePotentiallyDeleted)
		   && !pSharablesApplication.getToQuantity().getUnit().equals(unitToBePotentiallyDeleted)){
			
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
				String prefix = ((String)prefixSpinner.getSelectedItem()).replace("- - - - - - -", "").split("::")[0].trim();
				String mainUnitName = (String)unitSpinner.getSelectedItem();
				
				if(mainUnitName != null){
					Unit selectedUnit = pSharablesApplication.getUnitManager().getQueryExecutor().getUnit( prefix + ((prefix.length()!=0)?"-":"") + mainUnitName );
					
					if(callerButtonUnitType.equalsIgnoreCase("from")){
						pSharablesApplication.getFromQuantity().setUnit(selectedUnit);				
					}
					else if(callerButtonUnitType.equalsIgnoreCase("to")){
						pSharablesApplication.getToQuantity().setUnit(selectedUnit);
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
				pSharablesApplication.getUnitManager().getContentModifier().removeDynamicUnit(mainUnitName);
				
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
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				populateUnitSystemSpinner();
				unitSystemSpinner.setSelection(0);
				unitSystemSpinner.setSelected(true);
				
				///
				unitSystemSpinner.startAnimation(AnimationUtils.loadAnimation(UnitBrowserActivity.this, android.R.anim.slide_in_left));
				categorySpinner.startAnimation(AnimationUtils.loadAnimation(UnitBrowserActivity.this, android.R.anim.slide_in_left));
				unitSpinner.startAnimation(AnimationUtils.loadAnimation(UnitBrowserActivity.this, android.R.anim.slide_in_left));
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
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
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
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
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
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
	
	///Populate Spinners
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
		String[] filterList = new String[]{"| All | Unit Types", "| "+oppositeUnitType+" | "+"Unit Types"};
		
		ArrayAdapter<String> dimFilterArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, filterList);
		dimFilterSpinner.setAdapter(dimFilterArrayAdapter);
	}
	private void populateUnitSystemSpinner(){
		ArrayList<String> unitSystemNamesList = new ArrayList<String>();

		if(((String)dimFilterSpinner.getSelectedItem()).equals("| All | Unit Types")){
			for(String unitSystemName:pSharablesApplication.getUnitManager().getUnitsClassifier().getHierarchy().keySet()){
				unitSystemNamesList.add(unitSystemName);
			}
			unitSystemNamesList.remove(pSharablesApplication.getUnitManager().getQueryExecutor().getUnit(Unit.UNKNOWN_UNIT_NAME).getUnitSystem());
		}
		else{
			if(pSharablesApplication.getToQuantity().getUnit().getName().equalsIgnoreCase(Unit.UNKNOWN_UNIT_NAME) && oppositeUnitType.equalsIgnoreCase("to") ||
			   pSharablesApplication.getFromQuantity().getUnit().getName().equalsIgnoreCase(Unit.UNKNOWN_UNIT_NAME) && oppositeUnitType.equalsIgnoreCase("from") ){ 
				
			   unitSystemNamesList.add("No Compatible Unit Systems");	
			}
			else{
				for(String unitSystemName:pSharablesApplication.getUnitManager().getUnitsClassifier().getHierarchy().keySet()){
					if(pSharablesApplication.getUnitManager().getUnitsClassifier().getHierarchy().get(unitSystemName).containsKey(oppositeUnitCategoryName)){
						unitSystemNamesList.add(unitSystemName);
					}
				}
			}
		}

		ArrayAdapter<String> unitSystemSpinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, unitSystemNamesList);
		unitSystemSpinner.setAdapter(unitSystemSpinnerArrayAdapter);
	}
	private void populateCategorySpinner(){
		ArrayList<String> categoriesList = new ArrayList<String>();
		
		if(((String)dimFilterSpinner.getSelectedItem()).equals("| All | Unit Types")){
			for(String category:pSharablesApplication.getUnitManager().getUnitsClassifier().getHierarchy().get((String)unitSystemSpinner.getSelectedItem()).keySet()){
				categoriesList.add(category);
			}	
		}
		else{
			categoriesList.add(oppositeUnitCategoryName);
		}
		
		ArrayAdapter<String> categorySpinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categoriesList);
		categorySpinner.setAdapter(categorySpinnerArrayAdapter);
	}
	private void populateUnitSpinner(){
		ArrayList<String> unitsList = new ArrayList<String>(); 
		
		if((pSharablesApplication.getToQuantity().getUnit().getName().equalsIgnoreCase(Unit.UNKNOWN_UNIT_NAME) && oppositeUnitType.equalsIgnoreCase("to") ||
			pSharablesApplication.getFromQuantity().getUnit().getName().equalsIgnoreCase(Unit.UNKNOWN_UNIT_NAME) && oppositeUnitType.equalsIgnoreCase("from")) 
				&& !((String)dimFilterSpinner.getSelectedItem()).equals("| All | Unit Types")){ 
					
				   unitsList.add("No Compatible Units");	
		}
		else{
			unitsList = pSharablesApplication.getUnitManager().getUnitsClassifier().getHierarchy().get((String)unitSystemSpinner.getSelectedItem()).get((String)categorySpinner.getSelectedItem());
		}
		
		ArrayAdapter<String> unitsSpinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, (unitsList!=null)?unitsList:new ArrayList<String>());
		unitSpinner.setAdapter(unitsSpinnerArrayAdapter);
	}
	
	private void populatePrefixSpinner(){
		ArrayList<String> prefixesList = new ArrayList<String>();
		prefixesList.add("- - - - - - -  ::  1.0"); //When no selected selected is applied, the identity factor is used.
		
		/*Only display prefixes if the unit name itself does not contain a prefix. 
		 Make sure that unit name does not contain any complex term combinations.
		 Also make sure it is not a currency. 
		*/
		if(!Pattern.matches("[a-zA-Z_]+-[a-zA-Z_]+",(String)unitSpinner.getSelectedItem())
			&& !Pattern.compile("[/*()]").matcher((String)unitSpinner.getSelectedItem()).find()
			&& !((String)categorySpinner.getSelectedItem()).equalsIgnoreCase("currency_unit")
			&& !((String)unitSpinner.getSelectedItem()).equalsIgnoreCase("No Compatible Units")){
			
			for(Entry<String,Double> prefixEntry:pSharablesApplication.getUnitManager().getQueryExecutor().getAllPrefixValues().entrySet()){
				prefixesList.add(prefixEntry.getKey()+"  ::  "+prefixEntry.getValue());
			}
			
			//Sort prefix list from least to greatest factor
			Collections.sort(prefixesList, new Comparator<String>(){
				@Override
				public int compare(String arg0, String arg1) {
					return Double.compare(pSharablesApplication.getUnitManager().getQueryExecutor().getPrefixValue(arg0.split("::")[0].trim())
							              ,pSharablesApplication.getUnitManager().getQueryExecutor().getPrefixValue(arg1.split("::")[0].trim()));
				}});
		}
		
		ArrayAdapter<String> prefixSpinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, prefixesList);
		prefixSpinner.setAdapter(prefixSpinnerArrayAdapter);
	}
	
}