package com.isaacapps.unitconverterapp.models.unitmanager;

import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.ConversionFavoritesDataModel;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.PrefixesDataModel;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.UnitsClassifierDataModel;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsDataModel;

import java.util.Locale;

/**
 * This class provides access to a set of components sharing the same environment that have grouped functionalitiees allowing for the storage and manipulation of units.
 */
public class UnitManager {
    private Locale locale;
    private PrefixesDataModel prefixesDataModel;
    private UnitsDataModel unitsDataModel;
    private FundamentalUnitsDataModel fundamentalUnitsDataModel;
    private UnitsClassifierDataModel unitsClassifierDataModel;
    private ConversionFavoritesDataModel conversionFavoritesDataModel;

    /**
     * Should ideally only be invoked by the UnitManagerBuilder
     */

    UnitManager(Locale locale) {
        this.locale = locale;
    }

    ///
    public PrefixesDataModel getPrefixesDataModel() {
        return prefixesDataModel;
    }
    void setPrefixesModelData(PrefixesDataModel modelData) {
        this.prefixesDataModel = modelData;
        this.prefixesDataModel.setUnitsDataModel(unitsDataModel);
    }

    public UnitsDataModel getUnitsDataModel() {
        return unitsDataModel;
    }
    /**
     * Should ideally only be invoked by the UnitManagerBuilder
     */
    void setUnitsModelData(UnitsDataModel modelData) {
        this.unitsDataModel = modelData;
        this.unitsDataModel.setUnitManagerContext(this);
        this.unitsDataModel.setLocale(locale);
    }

    public UnitsClassifierDataModel getUnitsClassifierDataModel() {
        return unitsClassifierDataModel;
    }
    /**
     * Should ideally only be invoked by the UnitManagerBuilder
     */
    void setUnitsClassifierDataModel(UnitsClassifierDataModel unitsClassifierDataModel) {
        this.unitsClassifierDataModel = unitsClassifierDataModel;
    }

    public FundamentalUnitsDataModel getFundamentalUnitsDataModel() {
        return fundamentalUnitsDataModel;
    }
    /**
     * Should ideally only be invoked by the UnitManagerBuilder
     */
    void setFundamentalUnitsModelData(FundamentalUnitsDataModel fundamentalUnitsDataModel) {
        this.fundamentalUnitsDataModel = fundamentalUnitsDataModel;
        this.fundamentalUnitsDataModel.setUnitsDataModel(unitsDataModel);
    }

    public ConversionFavoritesDataModel getConversionFavoritesDataModel() {
        return conversionFavoritesDataModel;
    }
    void setConversionFavoritesDataModel(ConversionFavoritesDataModel conversionFavoritesDataModel) {
        this.conversionFavoritesDataModel = conversionFavoritesDataModel;
    }

    ///
    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
        this.unitsDataModel.setLocale(locale);
    }
}

