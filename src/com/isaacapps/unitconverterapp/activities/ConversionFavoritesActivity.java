package com.isaacapps.unitconverterapp.activities;

import android.graphics.Color;
import android.os.Bundle;

import java.util.ArrayList;

import com.isaacapps.unitconverterapp.activities.R;
import com.isaacapps.unitconverterapp.dao.xml.readers.local.ConversionFavoritesListXmlLocalReader;

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

	ListView conversionsListView;
	Button selectButton; //Sets up selected conversion when clicked
	Button removeButton; //Removes selected conversion from list when clicked
	Button cancelButton; //Exits activity when clicked
	ArrayAdapter<String> conversionsList_Adapter;
	String selectedConversion;
	int pastSelectedConversionPosition;

	int FAVORITES_LOADER = 1;
		
	///
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_conversion_favorites);
		
		pSharablesApplication = (PersistentSharablesApplication)this.getApplication();	
		pastSelectedConversionPosition = -1;

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
		return true;
	}
	
	///Setup UI Components Methods
	private void setupUIComponents(){
		conversionsListView = (ListView) findViewById(R.id.conversionListView);
		selectButton = (Button) findViewById(R.id.selectUnitButton); 
		removeButton = (Button) findViewById(R.id.removeButton);
		cancelButton = (Button) findViewById(R.id.cancelUnitButton); 
		
		selectButton.setVisibility(View.GONE);
		removeButton.setVisibility(View.GONE);
		
		conversionsListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	}
	private void showSelectButtonIfAppropriate(){
		if(pastSelectedConversionPosition != -1){
			selectButton.setVisibility(View.VISIBLE);
		}
		else{
			selectButton.setVisibility(View.GONE);
		}
	}
	private void showRemoveButtonIfAppropriate(){
		if(pastSelectedConversionPosition != -1){
			removeButton.setVisibility(View.VISIBLE);
		}
		else{
			removeButton.setVisibility(View.GONE);
		}
	}
	
	///Data Loading Methods
	private void loadConversionFavorites(){
		getSupportLoaderManager().initLoader(FAVORITES_LOADER, null, this).forceLoad();
	}
	
	///Button Listeners Methods
	private void addListenerOnConversionFavoritesButtons(){
		selectButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				String[] categoryNconversionUnits_names = selectedConversion.split(": ");
				String[] conversionUnitNames = categoryNconversionUnits_names[1].split(" --> ");
				
				pSharablesApplication.getFromQuantity().setUnit(pSharablesApplication.getUnitManager().getQueryExecutor().getUnit(conversionUnitNames[0]));
				pSharablesApplication.getToQuantity().setUnit(pSharablesApplication.getUnitManager().getQueryExecutor().getUnit(conversionUnitNames[1]));
				
				//
				finish();
			}
				
		});
		
		removeButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				pSharablesApplication.getConversionFavoritesList().remove(pastSelectedConversionPosition);
				pastSelectedConversionPosition = -1;
				repopulateList();	
				
				showSelectButtonIfAppropriate();
				showRemoveButtonIfAppropriate();
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
	
	///List View Listener Methods
	private void addListenerOnConversionListView(){
		conversionsListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> listView, View arg1, int currentSelectedConversionPosition,
					long arg3) {
				
				alterSelection(listView, currentSelectedConversionPosition);
				
				showSelectButtonIfAppropriate();
				showRemoveButtonIfAppropriate();
			}
			
		});
	}

	///
	private void alterSelection(AdapterView<?> listView, int currentSelectedConversionPosition){
		if(currentSelectedConversionPosition != pastSelectedConversionPosition){
			setSelection(listView, (String)listView.getItemAtPosition(currentSelectedConversionPosition), currentSelectedConversionPosition);							
			listView.getChildAt(currentSelectedConversionPosition).setBackgroundColor(Color.CYAN);	
		}
		else if(pastSelectedConversionPosition == currentSelectedConversionPosition){
			pastSelectedConversionPosition = -1;
			listView.getChildAt(currentSelectedConversionPosition).setBackgroundColor(Color.TRANSPARENT);	
		}	
	}
	private void setSelection(AdapterView<?> listView, String conversionString, int currentSelectedConversionPosition){
		selectedConversion = conversionString;
		
		 //ensures that only one item at a time can be selected, by deselecting a previously selected alternate position.
		if(pastSelectedConversionPosition != -1){
			listView.getChildAt(pastSelectedConversionPosition).setBackgroundColor(Color.TRANSPARENT);
		}
		
		pastSelectedConversionPosition = currentSelectedConversionPosition;
	}
	
	///
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

	///Loader Manager methods
	@Override
	public Loader<ArrayList<String>> onCreateLoader(int id, Bundle arg1) {
		if(id == FAVORITES_LOADER){
			return new ConversionFavoritesListXmlLocalReader(this);
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
	}		
}
