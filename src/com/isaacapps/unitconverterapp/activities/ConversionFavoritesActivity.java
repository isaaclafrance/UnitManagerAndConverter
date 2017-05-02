package com.isaacapps.unitconverterapp.activities;

import android.graphics.Color;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Arrays;

import com.isaacapps.unitconverterapp.activities.R;
import com.isaacapps.unitconverterapp.dao.xml.readers.local.ConversionFavoritesListXmlLocalReader;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.ConversionFavoritesDataModel;

import android.support.v4.app.*;
import android.support.v4.content.Loader;
import android.view.*;
import android.view.View.*;
import android.widget.AdapterView.*;
import android.widget.*;

public class ConversionFavoritesActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<ConversionFavoritesDataModel>{
	PersistentSharablesApplication pSharablesApplication;	

	ListView conversionsListView;
	Button selectButton, removeButton, cancelButton;
	ArrayAdapter<String> conversionsListAdapter;
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
				
		if(pSharablesApplication.getUnitManager().getConversionFavoritesDataModel()
				                                 .getAllFormattedConversions().isEmpty()){
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
				pSharablesApplication.getFromQuantity().setUnits(Arrays.asList(pSharablesApplication.getUnitManager().getUnitsDataModel()
						                                        		       .getUnit(ConversionFavoritesDataModel
						                                        		    		   .getSourceUnitNameFromConversion(selectedConversion))));
				pSharablesApplication.getToQuantity().setUnits(Arrays.asList(pSharablesApplication.getUnitManager().getUnitsDataModel()
						                                                     .getUnit(ConversionFavoritesDataModel
						                                                    		  .getTargetUnitNameFromConversion(selectedConversion))));
				pSharablesApplication.getUnitManager().getConversionFavoritesDataModel()
				                                      .modifySignificanceRankOfMultipleConversions(ConversionFavoritesDataModel
				                                    		                            .getUnitCategoryFromFormattedConversion(selectedConversion), true);
				//
				finish();
			}
				
		});
		
		removeButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				if(pSharablesApplication.getUnitManager().getConversionFavoritesDataModel().removeFormattedConversion(selectedConversion)){
					pastSelectedConversionPosition = -1;
					repopulateList();	
					
					showSelectButtonIfAppropriate();
					showRemoveButtonIfAppropriate();
				}
			}
		});
		
		cancelButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				pSharablesApplication.saveConversionFavorites();
				finish();
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
			setSelection(listView, null, -1);
			listView.getChildAt(currentSelectedConversionPosition).setBackgroundColor(Color.TRANSPARENT);	
		}	
	}
	private void setSelection(AdapterView<?> listView, String conversionString, int currentSelectedConversionPosition){
		selectedConversion = conversionString;
		
		 //ensures that only one item at a time can be selected, by deselecting a previously selected alternate position.
		if(pastSelectedConversionPosition != -1)
			listView.getChildAt(pastSelectedConversionPosition).setBackgroundColor(Color.TRANSPARENT);
		
		pastSelectedConversionPosition = currentSelectedConversionPosition;
	}
	
	///
	private void populateList(boolean loaderFinished){
		ArrayList<String> conversionList;
		if(loaderFinished){
			conversionList = new ArrayList<String>(pSharablesApplication.getUnitManager().getConversionFavoritesDataModel().getAllFormattedConversions()); //prevent subsequent modification
			if(conversionList.isEmpty())
				conversionList.add("No Saved Conversion Favorites Available. Please Add Some By Navigating to the Home Screen, choosing a 'TO' and 'From', then pressing the 'Add Fav' menu button.");
		}
		else{
			conversionList = new ArrayList<String>(); conversionList.add("Loading ... Please Wait ...");
		}
		
		conversionsListAdapter = new ArrayAdapter<String>(this, R.layout.conversion_faves_list, conversionList);
		conversionsListView.setAdapter(conversionsListAdapter);
		
		conversionsListView.setSelection(0);
		conversionsListView.setSelected(true);
	}
	private void repopulateList(){
		conversionsListAdapter.notifyDataSetChanged();
	}

	
	///Loader Manager Overriden Methods
	@Override
	public Loader<ConversionFavoritesDataModel> onCreateLoader(int id, Bundle bundle) {
		if(id == FAVORITES_LOADER){
			return new ConversionFavoritesListXmlLocalReader(this);
		}
		else{
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<ConversionFavoritesDataModel> loader, ConversionFavoritesDataModel conversionFavoritesDataModel) {
		if(conversionFavoritesDataModel != null){
			pSharablesApplication.getUnitManager().getConversionFavoritesDataModel().combineWith(conversionFavoritesDataModel);	
			populateList(true);
		}
	}

	@Override
	public void onLoaderReset(Loader<ConversionFavoritesDataModel> loader) {}		
}
