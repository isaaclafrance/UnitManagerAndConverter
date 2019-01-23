package com.isaacapps.unitconverterapp.activities;

import android.app.Application;

import com.isaacapps.unitconverterapp.dao.xml.writers.local.ConversionFavoritesLocalXmlWriter;
import com.isaacapps.unitconverterapp.dao.xml.writers.local.units.NonStandardCoreUnitsMapsXmlLocalWriter;
import com.isaacapps.unitconverterapp.dao.xml.writers.local.units.NonStandardDynamicUnitsMapXmlLocalWriter;
import com.isaacapps.unitconverterapp.models.measurables.quantity.Quantity;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManagerBuilder;

import org.xmlpull.v1.XmlPullParserException;

import java.io.FileNotFoundException;
import java.io.IOException;

public class PersistentSharablesApplication extends Application {
    public static final String MULTI_UNIT_MODE_BUNDLE_NAME = "multiUnitModeOn";

    private UnitManagerBuilder unitManagerBuilder;
    private UnitManager unitManager;
    private Quantity fromQuantity, toQuantity;
    private boolean onlineUnitsCurrentlyLoaded;
    private boolean multiUnitModeOn;

    ///
    public PersistentSharablesApplication() {
        unitManagerBuilder = new UnitManagerBuilder();
        unitManagerBuilder.setReUpdateAssociationsOfUnknownUnits(true);

        try {
            fromQuantity = new Quantity();
            toQuantity = new Quantity();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    ///
    public boolean saveUnits(boolean coreUnitsUpdated) {
        try {
            if(coreUnitsUpdated)
                new NonStandardCoreUnitsMapsXmlLocalWriter(getApplicationContext()).saveToXML(getUnitManager().getUnitsDataModel()
                        .getUnitsContentMainRetriever().getCoreUnits());

            new NonStandardDynamicUnitsMapXmlLocalWriter(getApplicationContext()).saveToXML(getUnitManager().getUnitsDataModel()
                    .getUnitsContentMainRetriever().getDynamicUnits());

            return true;
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
        return false;
    }
    public void saveConversionFavorites() {
        try {
            new ConversionFavoritesLocalXmlWriter(getApplicationContext()).saveToXML(unitManager.getConversionFavoritesDataModel());
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
    public UnitManagerBuilder getUnitManagerBuilder() {
        return unitManagerBuilder;
    }

    public UnitManager getUnitManager(){
        return unitManager;
    }
    public void createUnitManager() {
        try {
            unitManager = unitManagerBuilder.build();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public boolean updateUnitManager(){
        try {
            unitManagerBuilder.updateContent(unitManager);
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean isUnitManagerPreReqLoadingComplete() {
        return unitManagerBuilder.areMinComponentsForCreationAvailable();
    }
    public boolean isUnitManagerAlreadyCreated(){
        return unitManager != null;
    }

    ///
    public Quantity getSourceQuantity() {
        return fromQuantity;
    }
    public Quantity getTargetQuantity() {
        return toQuantity;
    }

    ///
    public boolean isOnlineUnitsCurrentlyLoaded(){
        return onlineUnitsCurrentlyLoaded;
    }
    public void setOnlineUnitsCurrentlyLoaded(boolean onlineUnitsCurrentlyLoaded){
        this.onlineUnitsCurrentlyLoaded = onlineUnitsCurrentlyLoaded;
    }

    public boolean isMultiUnitModeOn() {
        return multiUnitModeOn;
    }
    public void setMultiUnitModeOn(boolean multiUnitModeOn){
        this.multiUnitModeOn = multiUnitModeOn;
    }
}
