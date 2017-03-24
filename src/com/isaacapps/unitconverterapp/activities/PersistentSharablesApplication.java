package com.isaacapps.unitconverterapp.activities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParserException;

import com.isaacapps.unitconverterapp.dao.xml.writers.local.ConversionFavoritesListXmlLocalWriter;
import com.isaacapps.unitconverterapp.dao.xml.writers.local.UnitsMapXmlLocalWriter;
import com.isaacapps.unitconverterapp.models.Quantity;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManagerBuilder;

import android.app.Application;

public class PersistentSharablesApplication extends Application{
	private boolean conversionFavoriteListChanged;
	private UnitManagerBuilder unitManagerBuilder;
	private UnitManager unitManager;
	private ArrayList<String> conversionFavoritesList;
	private Quantity fromQuantity;
	private Quantity toQuantity;	
	public int numOfLoaderCompleted; 
	public int initialUnitNum;
		
	///
	public PersistentSharablesApplication(){
		conversionFavoriteListChanged = false;
		unitManagerBuilder = new UnitManagerBuilder();
		unitManager = null;
		conversionFavoritesList = new ArrayList<String>();
		fromQuantity = new Quantity();
		toQuantity = new Quantity();
		numOfLoaderCompleted = 0;
		initialUnitNum = 0;
	}
	
	///
	public void saveUnits(){
		try {
			if(getUnitManager().getQueryExecutor().getDynamicUnits().size() != initialUnitNum){
				new UnitsMapXmlLocalWriter(getApplicationContext()).saveToXML(getUnitManager().getQueryExecutor().getDynamicUnits());
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void saveConversionFavorites(){
		try {
			if(conversionFavoriteListChanged){
				new ConversionFavoritesListXmlLocalWriter(getApplicationContext()).saveToXML(conversionFavoritesList);
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	///	
	public UnitManagerBuilder getUnitManagerBuilder(){
		return unitManagerBuilder;
	}
	public void setUnitManagerBuilder(UnitManagerBuilder unitManagerBuilder){
		this.unitManagerBuilder = unitManagerBuilder;
	}	
	
	public UnitManager getUnitManager(){
		if(unitManager == null){
			recreateUnitManager();
		}
		return unitManager;
	}	
	public void recreateUnitManager(){
		if(unitManagerBuilder != null){
			unitManager = unitManagerBuilder.createUnitManager();
			if(isUnitManagerPreReqLoadingComplete()){
				initialUnitNum = unitManager.getQueryExecutor().getDynamicUnits().size();
			}
		}
	}
	public boolean isUnitManagerPreReqLoadingComplete(){
		return unitManagerBuilder.areMinComponentsForCreationAvailable() && numOfLoaderCompleted == 4;
	}
	
	///
	public Quantity getFromQuantity(){
		return fromQuantity;
	}
	public Quantity getToQuantity(){
		return toQuantity;
	}
		
	///
	public ArrayList<String> getConversionFavoritesList(){
		return conversionFavoritesList;
	}
	public void addConversionToConversionFavoritesList(String conversion){
		conversionFavoritesList.add(conversion);
		conversionFavoriteListChanged = true;
	}
}
