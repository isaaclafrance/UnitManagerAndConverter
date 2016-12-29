package com.example.unitconverter.app;

import java.util.ArrayList;

import com.example.unitconverter.R;
import com.example.unitconverter.Unit;
import com.example.unitconverter.R.id;
import com.example.unitconverter.R.layout;
import com.example.unitconverter.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class UnitBrowserActivity extends Activity {
	PersistentSharablesApplication pSharablesApplication;	
	//
	Spinner dimFilterSpinner;
	Spinner unitSystemSpinner;
	Spinner categorySpinner;
	Spinner unitSpinner;
	//
	Button selectButton;
	Button cancelButton;
	//
	String callerButtonName;
	String categoryName;

	//
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_unit_browse);
		
		pSharablesApplication = (PersistentSharablesApplication)this.getApplication();	

		setupUIComponents();

		//
		callerButtonName = "";
		categoryName = "";
		
		//Receive unit category based on the category of the unit type for which activity was invoked
		Bundle extras = getIntent().getExtras();
		if(extras != null){
			callerButtonName = extras.getString("buttonName");
		}
		
		if(callerButtonName.equalsIgnoreCase("to")){
			categoryName = pSharablesApplication.getToUnit().getUnitCategory();
		}
		else if(callerButtonName.equalsIgnoreCase("from")){
			categoryName = pSharablesApplication.getFromUnit().getUnitCategory();
		}
			
		//
		populateSpinners();
		
		//
		addListenerOnUnitBrowserButtons();
		addListenerOnSelectionSpinners();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	//Setup UI Components Methods
	private void setupUIComponents(){
		dimFilterSpinner = (Spinner) findViewById(R.id.dimFilterSpinner);
		unitSystemSpinner = (Spinner) findViewById(R.id.unitSystemSpinner);
		categorySpinner = (Spinner) findViewById(R.id.categorySpinner);
		unitSpinner = (Spinner) findViewById(R.id.unitSpinner);
		//
		selectButton = (Button) findViewById(R.id.selectButton);
		cancelButton = (Button) findViewById(R.id.cancelButton);
	}
	
	//Button Listeners Methods
	private void addListenerOnUnitBrowserButtons(){
		selectButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				Unit selectedUnit = pSharablesApplication.getUnitManager().getUnit((String)unitSpinner.getSelectedItem());
				
				if(callerButtonName.equalsIgnoreCase("from")){
					pSharablesApplication.setFromUnit(selectedUnit);				
				}
				else if(callerButtonName.equalsIgnoreCase("to")){
					pSharablesApplication.setToUnit(selectedUnit);
				}

				finish();
			}
		});
		
		cancelButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				finish();
			}
		});		
	}
	
	//Selection Spinner Listener Methods	
	private void addListenerOnSelectionSpinners(){
		dimFilterSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				populateUnitSystemSpinner();
				unitSystemSpinner.setSelection(0);
				unitSystemSpinner.setSelected(true);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		unitSystemSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				populateCategorySpinner();
				categorySpinner.setSelection(0);
				categorySpinner.setSelected(true);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		categorySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				populateUnitSpinner();
				unitSpinner.setSelection(0);
				unitSpinner.setSelected(true);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	//	
	private void populateSpinners(){
		populateDimensionFilterSpinner();
		dimFilterSpinner.setSelection(0);
		dimFilterSpinner.setSelected(true);
		
		populateUnitSystemSpinner();
		unitSystemSpinner.setSelection(0);
		unitSystemSpinner.setSelected(true);
		
		populateCategorySpinner();
		categorySpinner.setSelection(0);
		categorySpinner.setSelected(true);
		
		populateUnitSpinner();
	}
	private void populateDimensionFilterSpinner(){
		String[] filterList = new String[]{"| All | Unit Types", "| "+callerButtonName+" | "+"Unit Type"};
		
		ArrayAdapter<String> filterAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, filterList);
		dimFilterSpinner.setAdapter(filterAdapter);
	}
	private void populateUnitSystemSpinner(){
		ArrayList<String> unitSystemNamesList = new ArrayList<String>();

		if(((String)dimFilterSpinner.getSelectedItem()).equals("| All | Unit Types")){
			for(String unitSystemName:pSharablesApplication.getUnitManager().getUnitsClassificationMap().keySet()){
				unitSystemNamesList.add(unitSystemName);
			}
			unitSystemNamesList.remove(pSharablesApplication.getUnitManager().getUnit(Unit.UNKNOWN_UNIT_NAME).getUnitSystem());
		}
		else{
			if(pSharablesApplication.getFromUnit().getUnitName().equalsIgnoreCase(Unit.UNKNOWN_UNIT_NAME) && callerButtonName.equalsIgnoreCase("to") ||
			   pSharablesApplication.getToUnit().getUnitName().equalsIgnoreCase(Unit.UNKNOWN_UNIT_NAME) && callerButtonName.equalsIgnoreCase("from") ){ 
				
			   unitSystemNamesList.add("No Compatible Unit Systems");	
			}
			else{
				for(String unitSystemName:pSharablesApplication.getUnitManager().getUnitsClassificationMap().keySet()){
					if(pSharablesApplication.getUnitManager().getUnitsClassificationMap().get(unitSystemName).containsKey(categoryName)){
						unitSystemNamesList.add(unitSystemName);
					}
				}
			}
		}

		ArrayAdapter<String> unitSystemSpinner_Adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, unitSystemNamesList);
		unitSystemSpinner.setAdapter(unitSystemSpinner_Adapter);
	}
	private void populateCategorySpinner(){
		ArrayList<String> categoriesList = new ArrayList<String>();
		
		if(((String)dimFilterSpinner.getSelectedItem()).equals("| All | Unit Types")){
			for(String category:pSharablesApplication.getUnitManager().getUnitsClassificationMap().get((String)unitSystemSpinner.getSelectedItem()).keySet()){
				categoriesList.add(category);
			}	
		}
		else{
			categoriesList.add(categoryName);
		}
		
		ArrayAdapter<String> categorySpinner_Adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categoriesList);
		categorySpinner.setAdapter(categorySpinner_Adapter);
	}
	private void populateUnitSpinner(){
		ArrayList<String> unitsList = new ArrayList<String>(); 
		
		if(pSharablesApplication.getFromUnit().getUnitName().equalsIgnoreCase(Unit.UNKNOWN_UNIT_NAME) && callerButtonName.equalsIgnoreCase("to") ||
			pSharablesApplication.getToUnit().getUnitName().equalsIgnoreCase(Unit.UNKNOWN_UNIT_NAME) && callerButtonName.equalsIgnoreCase("from") ){ 
					
				   unitsList.add("No Compatible Units");	
		}
		else{
			unitsList = pSharablesApplication.getUnitManager().getUnitsClassificationMap().get((String)unitSystemSpinner.getSelectedItem()).get((String)categorySpinner.getSelectedItem());
		}
		
		ArrayAdapter<String> unitsSpinner_Adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, unitsList);
		unitSpinner.setAdapter(unitsSpinner_Adapter);
	}
}
