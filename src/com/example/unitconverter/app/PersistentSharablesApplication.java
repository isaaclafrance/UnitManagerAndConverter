package com.example.unitconverter.app;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParserException;

import com.example.unitconverter.Quantity;
import com.example.unitconverter.Unit;
import com.example.unitconverter.UnitManager;
import com.example.unitconverter.UnitManagerFactory;
import com.example.unitconverter.dao.ConversionFavoritesListXMLWriter;
import com.example.unitconverter.dao.UnitsMapXmlWriter;

import android.app.Application;

public class PersistentSharablesApplication extends Application{
	private boolean conversionFavoriteListChanged;
	private UnitManagerFactory unitManagerFactory;
	private UnitManager unitManager;
	private ArrayList<String> conversionFavoritesList;
	private Quantity fromQuantity;
	private Quantity toQuantity;	
	public int numOfLoaderCompleted; 
	public int initialUnitNum;
	
	
	public final int GENERAL_UNITS_LOADER = 1, FUND_UNITS_LOADER = 2, CURRENCY_UNITS_LOADER = 3, PREFIXES_LOADER= 4;
	
	public PersistentSharablesApplication(){
		unitManager = null;
		conversionFavoriteListChanged = false;
		conversionFavoritesList = new ArrayList<String>();
		setUnitManagerFactory(new UnitManagerFactory());
		fromQuantity = new Quantity();
		toQuantity = new Quantity();
		numOfLoaderCompleted = 0;
		initialUnitNum = 0;
	}
		
	public void saveUnits(){
		try {
			if(unitManager.getDynamicUnits().size() != initialUnitNum){
				ArrayList<Unit> allUnits = new ArrayList<Unit>();
				allUnits.addAll(getUnitManager().getDynamicUnits());
				
				UnitsMapXmlWriter.saveUnitsToXML(getApplicationContext(), allUnits);
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
				ConversionFavoritesListXMLWriter.saveToXML(this, conversionFavoritesList);
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
	
	//	
	public UnitManagerFactory getUnitManagerFactory(){
		return unitManagerFactory;
	}
	public void setUnitManagerFactory(UnitManagerFactory unitManagerFactory){
		this.unitManagerFactory = unitManagerFactory;
	}	
	
	public UnitManager getUnitManager(){
		if(unitManager == null){
			recreateUnitManager();
		}
		return unitManager;
	}	
	public void recreateUnitManager(){
		if(unitManagerFactory != null){
			unitManager = unitManagerFactory.createUnitManager();
			if(isUnitManagerPreReqLoadingComplete()){
				initialUnitNum = unitManager.getDynamicUnits().size();
			}
		}
	}
	public boolean isUnitManagerPreReqLoadingComplete(){
		return unitManagerFactory.areMinComponentsForCreationAvailable() && numOfLoaderCompleted == 4;
	}
	
	//
	public Quantity getFromQuantity(){
		return fromQuantity;
	}
	public Quantity getToQuantity(){
		return toQuantity;
	}
		
	//
	public ArrayList<String> getConversionFavoritesList(){
		return conversionFavoritesList;
	}
	public void addConversionToConversionFavoritesList(String conversion){
		conversionFavoritesList.add(conversion);
		conversionFavoriteListChanged = true;
	}
}
