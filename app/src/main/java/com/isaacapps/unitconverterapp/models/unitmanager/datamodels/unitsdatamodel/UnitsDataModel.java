package com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.BaseDataModel;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.IDualKeyNCategoryRepository;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsContentDeterminer.DATA_MODEL_CATEGORY;
import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.componentunits.ComponentUnitsDimensionParser;
import com.isaacapps.unitconverterapp.processors.parsers.generaltext.PluralTextParser;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.componentnunit.ComponentUnitsDimensionSerializer;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.fundamentalunit.FundamentalUnitTypesDimensionSerializer;

import java.util.Locale;

public class UnitsDataModel extends BaseDataModel<String, Unit, DATA_MODEL_CATEGORY>{
    private Locale locale;
    private UnitManager unitManagerContext;
    private PluralTextParser pluralTextParser;
    private ComponentUnitsDimensionParser componentUnitsDimensionParser;
    private ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer;
    private FundamentalUnitTypesDimensionSerializer fundamentalUnitTypesDimensionSerializer;
    private IFormatter abbreviationFormatter;

    private UnitsContentMainRetriever unitsContentMainRetriever;
    private UnitsContentModifier unitsContentModifier;
    private UnitsContentQuerier unitsContentQuerier;
    private UnitsContentDeterminer unitsContentDeterminer;

    ///
    public UnitsDataModel() {
    }

    ///
    public UnitsContentMainRetriever getUnitsContentMainRetriever() {
        return unitsContentMainRetriever;
    }
    public void setUnitsContentMainRetriever(UnitsContentMainRetriever unitsContentMainRetriever) {
        this.unitsContentMainRetriever = unitsContentMainRetriever;
        unitsContentMainRetriever.setUnitsDataModel(this);
        unitsContentMainRetriever.setPluralTextParser(pluralTextParser);
        unitsContentMainRetriever.setComponentUnitsDimensionParser(componentUnitsDimensionParser);
        unitsContentMainRetriever.setComponentUnitsDimensionSerializer(componentUnitsDimensionSerializer);
        unitsContentMainRetriever.setFundamentalUnitTypesDimensionSerializer(fundamentalUnitTypesDimensionSerializer);
        unitsContentMainRetriever.setAbbreviationFormatter(abbreviationFormatter);
        unitsContentMainRetriever.setLocale(locale);
    }

    public UnitsContentModifier getUnitsContentModifier() {
        return unitsContentModifier;
    }
    public void setUnitsContentModifier(UnitsContentModifier unitsContentModifier) {
        this.unitsContentModifier = unitsContentModifier;
        unitsContentModifier.setUnitsDataModel(this);
        unitsContentModifier.setLocale(locale);

    }

    public UnitsContentQuerier getUnitsContentQuerier() {
        return unitsContentQuerier;
    }
    public void setUnitsContentQuerier(UnitsContentQuerier unitsContentQuerier) {
        this.unitsContentQuerier = unitsContentQuerier;
        unitsContentQuerier.setUnitsDataModel(this);
    }

    public UnitsContentDeterminer getUnitsContentDeterminer() {
        return unitsContentDeterminer;
    }
    public void setUnitsContentDeterminer(UnitsContentDeterminer unitsContentDeterminer) {
        this.unitsContentDeterminer = unitsContentDeterminer;
        unitsContentDeterminer.setUnitsDataModel(this);
        unitsContentDeterminer.setPluralTextParser(pluralTextParser);
        unitsContentDeterminer.setComponentUnitsDimensionParser(componentUnitsDimensionParser);
        unitsContentDeterminer.setComponentUnitsDimensionSerializer(componentUnitsDimensionSerializer);
    }

    ///
    IDualKeyNCategoryRepository<String, Unit, DATA_MODEL_CATEGORY> getRepositoryWithDualKeyNCategory() {
        return repositoryWithDualKeyNCategory;
    }

    public PluralTextParser getPluralTextParser() {
        return pluralTextParser;
    }
    public void setPluralTextParser(PluralTextParser pluralTextParser) {
        this.pluralTextParser = pluralTextParser;
        if(unitsContentMainRetriever != null)
            unitsContentMainRetriever.setPluralTextParser(pluralTextParser);
        if(unitsContentDeterminer != null)
            unitsContentMainRetriever.setPluralTextParser(pluralTextParser);
    }

    public void setComponentUnitsDimensionParser(ComponentUnitsDimensionParser componentUnitsDimensionParser) {
        this.componentUnitsDimensionParser = componentUnitsDimensionParser;
        if(unitsContentMainRetriever != null)
            unitsContentMainRetriever.setComponentUnitsDimensionParser(componentUnitsDimensionParser);
        if(unitsContentDeterminer != null)
            unitsContentDeterminer.setComponentUnitsDimensionParser(componentUnitsDimensionParser);
    }
    public void setComponentUnitsDimensionSerializer(ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer) {
        this.componentUnitsDimensionSerializer = componentUnitsDimensionSerializer;
        if(unitsContentMainRetriever != null)
            unitsContentMainRetriever.setComponentUnitsDimensionSerializer(componentUnitsDimensionSerializer);
        if(unitsContentDeterminer != null)
            unitsContentDeterminer.setComponentUnitsDimensionSerializer(componentUnitsDimensionSerializer);
    }
    public void setFundamentalUnitTypesDimensionSerializer(FundamentalUnitTypesDimensionSerializer fundamentalUnitTypesDimensionSerializer) {
        this.fundamentalUnitTypesDimensionSerializer = fundamentalUnitTypesDimensionSerializer;
        if(unitsContentMainRetriever != null)
            unitsContentMainRetriever.setFundamentalUnitTypesDimensionSerializer(fundamentalUnitTypesDimensionSerializer);
    }

    public void setAbbreviationFormatter(IFormatter abbreviationFormatter){
        this.abbreviationFormatter = abbreviationFormatter;
        if(unitsContentMainRetriever != null)
            unitsContentMainRetriever.setAbbreviationFormatter(abbreviationFormatter);
    }
    public void setLocale(Locale locale) {
        this.locale = locale;
        componentUnitsDimensionSerializer.setLocale(locale);
        unitsContentModifier.setLocale(locale);
    }

    ///
    public void setUnitManagerContext(UnitManager unitManagerContext) {
        this.unitManagerContext = unitManagerContext;
    }
    UnitManager getUnitManagerContext(){
        return unitManagerContext;
    }
}
