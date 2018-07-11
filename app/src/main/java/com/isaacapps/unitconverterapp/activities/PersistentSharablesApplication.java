package com.isaacapps.unitconverterapp.activities;

import android.app.Application;

import com.isaacapps.unitconverterapp.dao.xml.writers.local.ConversionFavoritesLocalXmlWriter;
import com.isaacapps.unitconverterapp.dao.xml.writers.local.UnitsMapXmlLocalWriter;
import com.isaacapps.unitconverterapp.models.measurables.quantity.Quantity;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManagerBuilder;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManagerBuilderException;

import org.xmlpull.v1.XmlPullParserException;

import java.io.FileNotFoundException;
import java.io.IOException;

public class PersistentSharablesApplication extends Application {
    private UnitManagerBuilder unitManagerBuilder;
    private UnitManager unitManager;
    private Quantity fromQuantity, toQuantity;
    private int initialUnitNum, initialConversionFavoritesNum;
    private int numOfLoadersCompleted;

    ///
    public PersistentSharablesApplication() {
        unitManagerBuilder = new UnitManagerBuilder();
        numOfLoadersCompleted = initialUnitNum = initialConversionFavoritesNum = 0;
        try {
            fromQuantity = new Quantity();
            toQuantity = new Quantity();
        }
        catch(Exception e){ }
    }

    ///
    public void saveUnits() {
        try {
            new UnitsMapXmlLocalWriter(getApplicationContext()).saveToXML(getUnitManager().getUnitsDataModel().getContentMainRetriever().getDynamicUnits());
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
        if (unitManager == null) {
            recreateUnitManager();
        }
        return unitManager;
    }

    public void recreateUnitManager() {
        try {
            unitManager = unitManagerBuilder.build();
        }
        catch (UnitManagerBuilderException e){ }
    }

    public boolean isUnitManagerPreReqLoadingComplete() {
        return unitManagerBuilder.areMinComponentsForCreationAvailable() && numOfLoadersCompleted >= 2;
    }

    ///
    public Quantity getSourceQuantity() {
        return fromQuantity;
    }

    public Quantity getTargetQuantity() {
        return toQuantity;
    }

    ///
    public int getNumberOfLoadersCompleted() {
        return numOfLoadersCompleted;
    }

    public void setNumberOfLoadersCompleted(int num) {
        numOfLoadersCompleted = num;
    }
}
