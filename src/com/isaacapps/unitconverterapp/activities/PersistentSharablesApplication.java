package com.isaacapps.unitconverterapp.activities;

import java.io.*;

import org.xmlpull.v1.XmlPullParserException;

import com.isaacapps.unitconverterapp.dao.xml.writers.local.*;
import com.isaacapps.unitconverterapp.models.*;
import com.isaacapps.unitconverterapp.models.unitmanager.*;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.*;

import android.app.Application;

public class PersistentSharablesApplication extends Application{
	private UnitManagerBuilder unitManagerBuilder;
	private UnitManager unitManager;
	private ConversionFavoritesDataModel conversionFavoritesDataModel;
	private Quantity fromQuantity, toQuantity;	
	public int numOfLoaderCompleted, initialUnitNum, initialConversionFavoritesNum;
		
	///
	public PersistentSharablesApplication(){
		unitManagerBuilder = new UnitManagerBuilder();
		unitManager = null;
		conversionFavoritesDataModel = new ConversionFavoritesDataModel();
		fromQuantity = new Quantity();
		toQuantity = new Quantity();
		numOfLoaderCompleted = initialUnitNum = initialConversionFavoritesNum = 0;
	}
	
	///
	public void saveUnits(){
		try {
			if(getUnitManager().getUnitsDataModel().getDynamicUnits().size() != initialUnitNum){
				new UnitsMapXmlLocalWriter(getApplicationContext()).saveToXML(getUnitManager().getUnitsDataModel().getDynamicUnits());
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
			if(conversionFavoritesDataModel.getAllFormattedConversions().size() != initialConversionFavoritesNum){
				new ConversionFavoritesListXmlLocalWriter(getApplicationContext()).saveToXML(conversionFavoritesDataModel);
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
	
	public UnitManager getUnitManager(){
		if(unitManager == null){
			recreateUnitManager();
		}
		return unitManager;
	}	
	public void recreateUnitManager(){
		if(unitManagerBuilder != null){
			unitManager = unitManagerBuilder.build();
			if(isUnitManagerPreReqLoadingComplete()){
				initialUnitNum = unitManager.getUnitsDataModel().getDynamicUnits().size();
				initialConversionFavoritesNum = unitManager.getConversionFavoritesDataModel().getAllFormattedConversions().size();
			}
		}
	}
	public boolean isUnitManagerPreReqLoadingComplete(){
		return unitManagerBuilder.areMinComponentsForCreationAvailable() && numOfLoaderCompleted >= 2;
	}
	
	///
	public Quantity getFromQuantity(){
		return fromQuantity;
	}
	public Quantity getToQuantity(){
		return toQuantity;
	}
}
