package com.example.unitconverter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Application;

public class PersistentSharablesApplication extends Application{
	private boolean unitManagerUnitsContentChanged; //Indicates if units and/or prefixed have been added to unit manager instance since first load
	private boolean unitManagerPrefixesContentChanged; //Indicates if units and/or prefixed have been added to unit manager instance since first load
	private boolean conversionFavoriteListChanged;
	private UnitManagerFactory unitManagerFactory;
	private UnitManager unitManager;
	private ArrayList<String> conversionFavoritesList;
	private Unit fromUnit;
	private Unit toUnit;	
	
	public PersistentSharablesApplication(){
		unitManager = null;
		unitManagerUnitsContentChanged = false;
		unitManagerPrefixesContentChanged = false;
		conversionFavoriteListChanged = false;
		conversionFavoritesList = new ArrayList<String>();
		setUnitManagerFactory(new UnitManagerFactory());
		setFromUnit(getUnitManager().getUnit(Unit.UNKNOWN_UNIT_NAME));
		setToUnit(getUnitManager().getUnit(Unit.UNKNOWN_UNIT_NAME));
	}
		
	public void savePrefixesNUnits(){
		try {
			if(unitManagerUnitsContentChanged){
				ArrayList<Unit> allUnits = new ArrayList<Unit>();
				allUnits.addAll(getUnitManager().getCoreUnits());
				//allUnits.addAll(getUnitManager().getDynamicUnits());
				
				new UnitsMapXmlWriter().saveUnitsToXML(getApplicationContext(), allUnits);
			}
			if(unitManagerPrefixesContentChanged){
				new PrefixesMapXmlWriter().savePrefixToXML(getApplicationContext(), getUnitManager().getAllPrefixValues());
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
				new ConversionFavoritesListXMLWriter().saveToXML(this, conversionFavoritesList);
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
	UnitManagerFactory getUnitManagerFactory(){
		return unitManagerFactory;
	}
	void setUnitManagerFactory(UnitManagerFactory unitManagerFactory){
		this.unitManagerFactory = unitManagerFactory;
	}	
	
	public UnitManager getUnitManager(){
		if(unitManager == null){
			recreateUnitManager();
		}
		return unitManager;
	}	
	void recreateUnitManager(){
		if(unitManagerFactory != null){
			unitManager = unitManagerFactory.createUnitManager();
		}
	}
	//Wrapper for modifying Unit Manager
	public void addUnitToUnitManager(Unit unit){
		getUnitManager().addUnit(unit);		
		unitManagerUnitsContentChanged = true;
	}
	public void addPrefixToUnitManager(String prefixName, String abbreviation, Float prefixValue){
		getUnitManager().addDynamicPrefix(prefixName, abbreviation, prefixValue);;		
		unitManagerPrefixesContentChanged = true;
	}
	
	//
	public Unit getFromUnit(){
		return fromUnit;
	}
	public void setFromUnit(Unit fromUnit){
		this.fromUnit = fromUnit;
	}	
	
	public Unit getToUnit(){
		return toUnit;
	}
	public void setToUnit(Unit toUnit){
		this.toUnit = toUnit;
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
