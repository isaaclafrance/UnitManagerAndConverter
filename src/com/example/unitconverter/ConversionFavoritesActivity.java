package com.example.unitconverter;

import android.graphics.Color;
import android.os.Bundle;

import java.util.ArrayList;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class ConversionFavoritesActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<ArrayList<String>>{
	PersistentSharablesApplication pSharablesApplication;	
	//
	ListView conversionsListView;
	Button selectButton; //Sets up selected conversion when clicked
	Button removeButton; //Removes selected conversion from list when clicked
	Button cancelButton; //Exits activity when clicked
	ArrayAdapter<String> conversionsList_Adapter;
	String selectedConversion;
	int selectedConversionPosition = 0;
	//
	int FAVORITES_LOADER = 1;
		
	//
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_conversion_favorites);
		
		pSharablesApplication = (PersistentSharablesApplication)this.getApplication();	

		setupUIComponents();
				
		if(pSharablesApplication.getConversionFavoritesList().size() == 0){
			populateList(false);
			loadConversionFavorites();
		}
		else{
			populateList(true);
		}
	
		addListenerOnConversionFavoritesButtons();
		addListenerOnConversionListView();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	//Setup UI Components Methods
	private void setupUIComponents(){
		conversionsListView = (ListView) findViewById(R.id.conversionListView);
		selectButton = (Button) findViewById(R.id.selectButton); 
		removeButton = (Button) findViewById(R.id.removeButton);
		cancelButton = (Button) findViewById(R.id.cancelButton); 
		
		conversionsListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	}
	
	//Data Loading Methods
	private void loadConversionFavorites(){
		getSupportLoaderManager().initLoader(FAVORITES_LOADER, null, this).forceLoad();
	}
	
	//Button Listeners Methods
	private void addListenerOnConversionFavoritesButtons(){
		selectButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				String[] categoryNconversionUnits_names = selectedConversion.split(": ");
				String[] conversionUnitNames = categoryNconversionUnits_names[1].split(" --> ");
				
				pSharablesApplication.setFromUnit(pSharablesApplication.getUnitManager().getUnit(conversionUnitNames[0]));
				pSharablesApplication.setToUnit(pSharablesApplication.getUnitManager().getUnit(conversionUnitNames[1]));
				
				//
				finish();
			}
				
		});
		
		removeButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				pSharablesApplication.getConversionFavoritesList().remove(selectedConversionPosition);
				
				repopulateList();		
			}
		});
		
		cancelButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				finish();
				pSharablesApplication.saveConversionFavorites();
			}
				
		});		
	}
	
	//List View Listener Methods
	private void addListenerOnConversionListView(){
		conversionsListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				arg0.getChildAt(selectedConversionPosition).setBackgroundColor(Color.TRANSPARENT);				
				setSelection((String)arg0.getItemAtPosition(position), position);
				arg0.getChildAt(position).setBackgroundColor(Color.WHITE);		
			}
			
		});
	}

	private void setSelection(String string, int position){
		selectedConversion = string;
		selectedConversionPosition = position;
	}
	
	private void populateList(boolean loaderFinished){
		ArrayList<String> conversionList;
		if(loaderFinished){
			conversionList = pSharablesApplication.getConversionFavoritesList();
		}
		else{
			conversionList = new ArrayList<String>(); conversionList.add("Loading ... Please Wait ...");
		}
		
		conversionsList_Adapter = new ArrayAdapter<String>(this, R.layout.conversion_faves_list, conversionList);
		conversionsListView.setAdapter(conversionsList_Adapter);
		
		conversionsListView.setSelection(0);
		conversionsListView.setSelected(true);
	}
	private void repopulateList(){
		conversionsList_Adapter.notifyDataSetChanged();
	}

	//Loader Manager methods
	@Override
	public Loader<ArrayList<String>> onCreateLoader(int id, Bundle arg1) {
		if(id == FAVORITES_LOADER){
			return new ConversionFavoritesListXMLReader(this);
		}
		else{
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<ArrayList<String>> loader, ArrayList<String> conversionFavorites) {
		for(String conversion:conversionFavorites){
			pSharablesApplication.addConversionToConversionFavoritesList(conversion);
		}
		
		populateList(true);
	}

	@Override
	public void onLoaderReset(Loader<ArrayList<String>> arg0) {
		// TODO Auto-generated method stub
		
	}		
}
