package com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.BaseDataModel;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.IDualKeyNCategoryRepository;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.ContentDeterminer.DATA_MODEL_CATEGORY;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.componentunits.ComponentUnitsDimensionParser;
import com.isaacapps.unitconverterapp.processors.parsers.generaltext.PluralTextParser;
import com.isaacapps.unitconverterapp.processors.parsers.measurables.unit.UnitParser;
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
    private UnitParser unitParser;

    private ContentMainRetriever contentMainRetriever;
    private ContentModifier contentModifier;
    private ContentQuerier contentQuerier;
    private ContentDeterminer contentDeterminer;

    ///
    public UnitsDataModel() {
    }

    ///

    public ContentMainRetriever getContentMainRetriever() {
        return contentMainRetriever;
    }
    public void setContentMainRetriever(ContentMainRetriever contentMainRetriever) {
        this.contentMainRetriever = contentMainRetriever;
        contentMainRetriever.setUnitsDataModel(this);
        contentMainRetriever.setPluralTextParser(pluralTextParser);
        contentMainRetriever.setComponentUnitsDimensionParser(componentUnitsDimensionParser);
        contentMainRetriever.setComponentUnitsDimensionSerializer(componentUnitsDimensionSerializer);
        contentMainRetriever.setFundamentalUnitTypesDimensionSerializer(fundamentalUnitTypesDimensionSerializer);
        contentMainRetriever.setLocale(locale);
    }

    public ContentModifier getContentModifier() {
        return contentModifier;
    }
    public void setContentModifier(ContentModifier contentModifier) {
        this.contentModifier = contentModifier;
        contentModifier.setUnitsDataModel(this);
        contentModifier.setLocale(locale);

    }

    public ContentQuerier getContentQuerier() {
        return contentQuerier;
    }
    public void setContentQuerier(ContentQuerier contentQuerier) {
        this.contentQuerier = contentQuerier;
        contentQuerier.setUnitsDataModel(this);
        contentQuerier.setComponentUnitsDimensionSerializer(componentUnitsDimensionSerializer);
    }

    public ContentDeterminer getContentDeterminer() {
        return contentDeterminer;
    }
    public void setContentDeterminer(ContentDeterminer contentDeterminer) {
        this.contentDeterminer = contentDeterminer;
        contentDeterminer.setUnitsDataModel(this);
        contentDeterminer.setPluralTextParser(pluralTextParser);
        contentDeterminer.setComponentUnitsDimensionParser(componentUnitsDimensionParser);
        contentDeterminer.setComponentUnitsDimensionSerializer(componentUnitsDimensionSerializer);
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
    }

    public void setComponentUnitsDimensionParser(ComponentUnitsDimensionParser componentUnitsDimensionParser) {
        this.componentUnitsDimensionParser = componentUnitsDimensionParser;
    }

    public void setUnitParser(UnitParser unitParser) {
        this.unitParser = unitParser;
    }

    public void setComponentUnitsDimensionSerializer(ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer) {
        this.componentUnitsDimensionSerializer = componentUnitsDimensionSerializer;
    }

    public void setFundamentalUnitTypesDimensionSerializer(FundamentalUnitTypesDimensionSerializer fundamentalUnitTypesDimensionSerializer) {
        this.fundamentalUnitTypesDimensionSerializer = fundamentalUnitTypesDimensionSerializer;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
        componentUnitsDimensionSerializer.setLocale(locale);
        contentModifier.setLocale(locale);
    }

    ///
    public void setUnitManagerContext(UnitManager unitManagerContext) {
        this.unitManagerContext = unitManagerContext;
    }
    UnitManager getUnitManagerContext(){
        return unitManagerContext;
    }
}
