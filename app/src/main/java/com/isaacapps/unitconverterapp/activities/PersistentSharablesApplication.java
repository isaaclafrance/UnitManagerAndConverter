package com.isaacapps.unitconverterapp.activities;

import android.app.Application;

import com.isaacapps.unitconverterapp.dao.xml.writers.local.ConversionFavoritesLocalXmlWriter;
import com.isaacapps.unitconverterapp.dao.xml.writers.local.UnitsMapXmlLocalWriter;
import com.isaacapps.unitconverterapp.models.measurables.quantity.Quantity;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManagerBuilder;

import org.xmlpull.v1.XmlPullParserException;

import java.io.FileNotFoundException;
import java.io.IOException;

public class PersistentSharablesApplication extends Application {
    private UnitManagerBuilder unitManagerBuilder;
    private UnitManager unitManager;
    private Quantity fromQuantity, toQuantity;
    private boolean unitManagerUpdated;

    ///
    public PersistentSharablesApplication() {
        unitManagerBuilder = new UnitManagerBuilder();
        try {
            fromQuantity = new Quantity();
            toQuantity = new Quantity();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    ///
    public void saveUnits() {
        try {
            new UnitsMapXmlLocalWriter(getApplicationContext()).saveToXML(getUnitManager().getUnitsDataModel()
                    .getUnitsContentMainRetriever().getDynamicUnits());
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
            unitManagerUpdated = true;
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean getIsUnitManagerPreReqLoadingComplete() {
        return unitManagerBuilder.areMinComponentsForCreationAvailable();
    }

    public boolean getIsUnitManagerAlreadyCreated(){
        return unitManager != null;
    }

    public boolean isUnitManagerUpdated(){
        return unitManagerUpdated;
    }
    public void setUnitManagerUpdated(boolean isUpdated){
        unitManagerUpdated = isUpdated;
    }

    ///
    public Quantity getSourceQuantity() {
        return fromQuantity;
    }

    public Quantity getTargetQuantity() {
        return toQuantity;
    }
}
