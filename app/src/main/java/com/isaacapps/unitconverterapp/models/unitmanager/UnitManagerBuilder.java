package com.isaacapps.unitconverterapp.models.unitmanager;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.measurables.unit.UnitException;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.ConversionFavoritesDataModel;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel.UNIT_TYPE;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.PrefixesDataModel;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.UnitsClassifierDataModel;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.IDualKeyNCategoryRepository;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.hashdriven.ConversionFavoritesDualKeyNCategoryHashedRepository;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.hashdriven.FundamentalUnitsHashDualKeyNCategoryHashedRepository;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.hashdriven.PrefixesDualKeyNCategoryHashedRepository;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.hashdriven.SignificanceRankHashedRepository;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.hashdriven.UnitsClassifierDualKeyNCategoryHashedRepository;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.hashdriven.UnitsDualKeyNCategoryHashedRepository;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsContentDeterminer;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsContentMainRetriever;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsContentModifier;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsContentQuerier;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsDataModel;
import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.text.AbbreviationFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.text.GeneralTextFormatter;
import com.isaacapps.unitconverterapp.processors.parsers.ParsingException;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.componentunits.ComponentUnitsDimensionParser;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.fundamentalunits.FundamentalUnitTypesDimensionParser;
import com.isaacapps.unitconverterapp.processors.parsers.generaltext.EnglishPluralTextParser;
import com.isaacapps.unitconverterapp.processors.parsers.generaltext.PluralTextParser;
import com.isaacapps.unitconverterapp.processors.parsers.measurables.unit.UnitParser;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.componentnunit.ComponentUnitsDimensionItemSerializer;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.componentnunit.ComponentUnitsDimensionSerializer;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.fundamentalunit.FundamentalUnitTypesDimensionItemSerializer;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.fundamentalunit.FundamentalUnitTypesDimensionSerializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Responsible for assembling the structural components and valid content needed to construct a well formed unit manager
 */
public class UnitManagerBuilder {
    private Locale locale;

    private final boolean[] componentContentStates; //Keeps track of component content that have been added or removed. [0] -> base units maps, [1] -> non-base units map, [2] -> core prefixes map, [3] -> dynamic prefixes map, [4] -> fundamental units map

    private final Collection<Unit> baseUnits;
    private final Collection<Unit> nonBaseUnits;

    private UnitsDataModel unitsDataModel;
    private PrefixesDataModel prefixesDataModel;
    private UnitsClassifierDataModel unitsClassifierDataModel;
    private FundamentalUnitsDataModel fundamentalUnitsDataModel;
    private ConversionFavoritesDataModel conversionFavoritesDataModel;

    private SignificanceRankHashedRepository significanceRankHashedRepository;
    private IDualKeyNCategoryRepository<String, UNIT_TYPE, String> fundamentalUnitsRepositoryWithDualKeyNCategory;
    private IDualKeyNCategoryRepository<String, String, String> conversionFavoritesRepositoryWithDualKeyNCategory;
    private IDualKeyNCategoryRepository<String, Collection<String>, String> unitsClassifierRepositoryWithDualKeyNCategory;
    private IDualKeyNCategoryRepository<String, Unit, UnitsContentDeterminer.DATA_MODEL_CATEGORY> unitsRepositoryWithDualKeyNCategory;
    private IDualKeyNCategoryRepository<String, Double, UnitsContentDeterminer.DATA_MODEL_CATEGORY> prefixsRepositoryWithDualKeyNCategory;

    private UnitsContentQuerier unitsDataModelUnitsContentQuerier;
    private UnitsContentModifier unitsDataModelUnitsContentModifier;
    private UnitsContentDeterminer unitsDataModelUnitsContentDeterminer;
    private UnitsContentMainRetriever unitsDataModelUnitsContentMainRetriever;

    private UnitParser unitParser;
    private PluralTextParser pluralTextParser;
    private IFormatter abbreviationFormatter;
    private ComponentUnitsDimensionParser componentUnitsDimensionParser;
    private ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer;
    private FundamentalUnitTypesDimensionParser fundamentalUnitTypesDimensionParser;
    private FundamentalUnitTypesDimensionSerializer fundamentalUnitTypesDimensionSerializer;

    private boolean reUpdateAssociationsOfUnknownUnits;

    ///
    public UnitManagerBuilder(){
        this(Locale.getDefault());
    }
    public UnitManagerBuilder(Locale locale) {
        this.locale = locale;

        componentContentStates = new boolean[5];

        baseUnits = new ArrayList<>();
        nonBaseUnits = new ArrayList<>();

        //Since there are many structural components that need to be setup, start with some default structural implementations that are most commonly used
        initializeAllStructuralDefaults();
    }

    ///Default structural implementations
    public UnitManagerBuilder initializeDefaultDataModelWithHashedRepositories(){
        return setPrefixesRepositoryWithDualKeyNCategory(new PrefixesDualKeyNCategoryHashedRepository())
                .setUnitsRepositoryWithDualKeyNCategory(new UnitsDualKeyNCategoryHashedRepository())
                .setFundamentalUnitsRepositoryWithDualKeyNCategory(new FundamentalUnitsHashDualKeyNCategoryHashedRepository())
                .setUnitsClassifierRepositoryWithDualKeyNCategory(new UnitsClassifierDualKeyNCategoryHashedRepository())
                .setConversionFavoritesRepositoryWithDualKeyNCategory(new ConversionFavoritesDualKeyNCategoryHashedRepository())
                .setSignificanceRankRepository(new SignificanceRankHashedRepository());
    }
    public UnitManagerBuilder initializeDefaultUnitsDataModelComponents(){
        return setUnitsDataModelUnitsContentQuerier(new UnitsContentQuerier())
                .setUnitsDataModelUnitsContentMainRetriever(new UnitsContentMainRetriever())
                .setUnitsDataModelUnitsContentModifier(new UnitsContentModifier())
                .setUnitsDataModelUnitsContentDeterminer(new UnitsContentDeterminer());
    }
    public UnitManagerBuilder initializeDefaultDataModels(){
        return setUnitsDataModel(new UnitsDataModel())
                .setPrefixesDataModel(new PrefixesDataModel())
                .setFundamentalUnitsDataModel(new FundamentalUnitsDataModel())
                .setUnitsClassifierDataModel(new UnitsClassifierDataModel())
                .setConversionFavoritesDataModel(new ConversionFavoritesDataModel());
    }
    public UnitManagerBuilder initializeDefaultDataModelDependencies(){
        try {
            return setPluralTextParser(new EnglishPluralTextParser())
                    .setAbbreviationFormatter(new AbbreviationFormatter(locale))
                    .setUnitParser(new UnitParser(componentUnitsDimensionParser))
                    .setComponentUnitsDimensionParser(new ComponentUnitsDimensionParser())
                    .setComponentUnitsDimensionSerializer(new ComponentUnitsDimensionSerializer(locale))
                    .setFundamentalUnitTypesDimensionParser(new FundamentalUnitTypesDimensionParser())
                    .setFundamentalUnitTypesDimensionSerializer( new FundamentalUnitTypesDimensionSerializer(locale));
        } catch (ParsingException e) {
            return this;
        }
    }
    public UnitManagerBuilder initializeAllStructuralDefaults(){
        initializeDefaultDataModels();
        initializeDefaultDataModelDependencies();
        initializeDefaultDataModelWithHashedRepositories();
        return initializeDefaultUnitsDataModelComponents();
    }


    ///
    public UnitManagerBuilder setPrefixesRepositoryWithDualKeyNCategory(IDualKeyNCategoryRepository<String, Double, UnitsContentDeterminer.DATA_MODEL_CATEGORY> repositoryWithDualKeyNCategory){
        prefixsRepositoryWithDualKeyNCategory = repositoryWithDualKeyNCategory;
        if(prefixesDataModel != null)
            prefixesDataModel.setRepositoryWithDualKeyNCategory(repositoryWithDualKeyNCategory);
        return this;
    }
    public UnitManagerBuilder setUnitsRepositoryWithDualKeyNCategory(IDualKeyNCategoryRepository<String, Unit, UnitsContentDeterminer.DATA_MODEL_CATEGORY> repositoryWithDualKeyNCategory){
        unitsRepositoryWithDualKeyNCategory = repositoryWithDualKeyNCategory;
        if(unitsDataModel != null)
            unitsDataModel.setRepositoryWithDualKeyNCategory(repositoryWithDualKeyNCategory);
        return this;
    }
    public UnitManagerBuilder setFundamentalUnitsRepositoryWithDualKeyNCategory(IDualKeyNCategoryRepository<String, UNIT_TYPE, String> repositoryWithDualKeyNCategory){
        fundamentalUnitsRepositoryWithDualKeyNCategory = repositoryWithDualKeyNCategory;
        if(fundamentalUnitsDataModel != null)
            fundamentalUnitsDataModel.setRepositoryWithDualKeyNCategory(repositoryWithDualKeyNCategory);
        return this;
    }
    public UnitManagerBuilder setUnitsClassifierRepositoryWithDualKeyNCategory(IDualKeyNCategoryRepository<String, Collection<String>, String> repositoryWithDualKeyNCategory){
        unitsClassifierRepositoryWithDualKeyNCategory = repositoryWithDualKeyNCategory;
        if(unitsClassifierDataModel != null)
            unitsClassifierDataModel.setRepositoryWithDualKeyNCategory(repositoryWithDualKeyNCategory);
        return this;
    }
    public UnitManagerBuilder setConversionFavoritesRepositoryWithDualKeyNCategory(IDualKeyNCategoryRepository<String, String, String> repositoryWithDualKeyNCategory){
        conversionFavoritesRepositoryWithDualKeyNCategory = repositoryWithDualKeyNCategory;
        if(conversionFavoritesDataModel != null)
            conversionFavoritesDataModel.setRepositoryWithDualKeyNCategory(repositoryWithDualKeyNCategory);
        return this;
    }
    public UnitManagerBuilder setSignificanceRankRepository(SignificanceRankHashedRepository significanceRankRepository){
        this.significanceRankHashedRepository = significanceRankRepository;
        if(conversionFavoritesDataModel != null)
            conversionFavoritesDataModel.setSignificanceRankRepository(significanceRankRepository);
        return this;
    }

    ///
    public UnitManagerBuilder setUnitsDataModel(UnitsDataModel unitsDataModel) {
        this.unitsDataModel = unitsDataModel;
        return this;
    }
    public UnitManagerBuilder setPrefixesDataModel(PrefixesDataModel prefixesDataModel) {
        this.prefixesDataModel = prefixesDataModel;
        return this;
    }
    public UnitManagerBuilder setUnitsClassifierDataModel(UnitsClassifierDataModel unitsClassifierDataModel) {
        this.unitsClassifierDataModel = unitsClassifierDataModel;
        return this;
    }
    public UnitManagerBuilder setFundamentalUnitsDataModel(FundamentalUnitsDataModel fundamentalUnitsDataModel) {
        this.fundamentalUnitsDataModel = fundamentalUnitsDataModel;
        return this;
    }
    public UnitManagerBuilder setConversionFavoritesDataModel(ConversionFavoritesDataModel conversionFavoritesDataModel) {
        this.conversionFavoritesDataModel = conversionFavoritesDataModel;
        return this;
    }

    ///
    public UnitManagerBuilder setUnitsDataModelUnitsContentMainRetriever(UnitsContentMainRetriever unitsContentMainRetriever){
        this.unitsDataModelUnitsContentMainRetriever = unitsContentMainRetriever;
        unitsContentMainRetriever.setLocale(locale);
        return this;
    }
    public UnitManagerBuilder setUnitsDataModelUnitsContentModifier(UnitsContentModifier unitsContentModifier){
        this.unitsDataModelUnitsContentModifier = unitsContentModifier;
        return this;
    }
    public UnitManagerBuilder setUnitsDataModelUnitsContentQuerier(UnitsContentQuerier unitsContentQuerier){
        this.unitsDataModelUnitsContentQuerier = unitsContentQuerier;
        return this;
    }
    public UnitManagerBuilder setUnitsDataModelUnitsContentDeterminer(UnitsContentDeterminer unitsContentDeterminer){
        this.unitsDataModelUnitsContentDeterminer = unitsContentDeterminer;
        return this;
    }

    ///
    public UnitManagerBuilder setPluralTextParser(PluralTextParser pluralTextParser){
        this.pluralTextParser = pluralTextParser;
        return this;
    }
    public UnitManagerBuilder setAbbreviationFormatter(IFormatter abbreviationFormatter){
        this.abbreviationFormatter = abbreviationFormatter;
        return this;
    }
    public UnitManagerBuilder setUnitParser(UnitParser unitParser) {
        this.unitParser = unitParser;
        return this;
    }
    public UnitManagerBuilder setComponentUnitsDimensionParser(ComponentUnitsDimensionParser componentUnitsDimensionParser) {
        this.componentUnitsDimensionParser = componentUnitsDimensionParser;
        return this;
    }
    public UnitManagerBuilder setComponentUnitsDimensionSerializer(ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer) {
        this.componentUnitsDimensionSerializer = componentUnitsDimensionSerializer;
        return this;
    }
    public UnitManagerBuilder setFundamentalUnitTypesDimensionParser(FundamentalUnitTypesDimensionParser fundamentalUnitTypesDimensionParser) {
        this.fundamentalUnitTypesDimensionParser = fundamentalUnitTypesDimensionParser;
        return this;
    }
    public UnitManagerBuilder setFundamentalUnitTypesDimensionSerializer(FundamentalUnitTypesDimensionSerializer fundamentalUnitTypesDimensionSerializer) {
        this.fundamentalUnitTypesDimensionSerializer = fundamentalUnitTypesDimensionSerializer;
        return this;
    }

    ///
    public UnitManagerBuilder setLocale(Locale locale){
        this.locale = locale;

        if(componentUnitsDimensionSerializer != null)
            componentUnitsDimensionSerializer.setLocale(locale);
        if(fundamentalUnitTypesDimensionSerializer != null)
            fundamentalUnitTypesDimensionSerializer.setLocale(locale);

        return this;
    }
    public Locale getLocale(){
        return locale;
    }

    public void setReUpdateAssociationsOfUnknownUnits(boolean reUpdateAssociationsOfUnknownUnits){
        this.reUpdateAssociationsOfUnknownUnits = reUpdateAssociationsOfUnknownUnits;
    }

    ///
    public UnitManagerBuilder addBaseUnits(Iterable<Unit> baseUnits) {
        if (baseUnits != null) {
            for (Unit baseUnit : baseUnits) {
                addBaseUnit(baseUnit);
            }
        }
        return this;
    }
    public UnitManagerBuilder addBaseUnit(Unit baseUnit) {
        if (baseUnit != null && baseUnit.isBaseUnit()) {
            baseUnits.add(baseUnit);
            componentContentStates[0] = true;
        }
        return this;
    }
    public UnitManagerBuilder clearBaseUnits() {
        baseUnits.clear();
        componentContentStates[0] = false;
        return this;
    }
    public UnitManagerBuilder addNonBaseUnits(Iterable<Unit> nonBaseUnits) {
        if (nonBaseUnits != null) {
            for (Unit nonBaseUnit : nonBaseUnits) {
                addNonBaseUnit(nonBaseUnit);
            }
        }
        return this;
    }
    public UnitManagerBuilder addNonBaseUnit(Unit nonBaseUnit) {
        if (nonBaseUnit != null && !nonBaseUnit.isBaseUnit()) {
            nonBaseUnits.add(nonBaseUnit);
            componentContentStates[1] = true;
        }
        return this;
    }
    public UnitManagerBuilder clearNonBaseUnits() {
        nonBaseUnits.clear();
        componentContentStates[1] = false;
        return this;
    }
    public UnitManagerBuilder clearAllUnits(){
        clearBaseUnits();
        return clearNonBaseUnits();
    }

    ///
    public UnitManagerBuilder addPrefixDataModel(PrefixesDataModel prefixesDataModel) throws UnitManagerBuilderException {
        if(!this.prefixesDataModel.combineWith(prefixesDataModel))
            throw new UnitManagerBuilderException("Some data models could not be combined due to structural incompatibility"
                    , "Make sure all the data models in both unit manager builders are structural compatiable. " +
                    "For example, data models with hashed repository can not combine with those having database repositories");

        componentContentStates[2] = this.prefixesDataModel.hasCorePrefixes();
        componentContentStates[3] = this.prefixesDataModel.hasDynamicPrefixes();
        return this;
    }
    public UnitManagerBuilder addCorePrefix(String prefixFullName, String prefixAbbreviation, Double prefixValue) {
        prefixesDataModel.addCorePrefix(prefixFullName, prefixAbbreviation, prefixValue);
        componentContentStates[2] = true;
        return this;
    }
    public UnitManagerBuilder clearCorePrefixes() {
        prefixesDataModel.removeAllCorePrefixes();
        componentContentStates[2] = false;
        return this;
    }
    public UnitManagerBuilder addDynamicPrefix(String prefixFullName, String prefixAbbreviation, Double prefixValue) {
        prefixesDataModel.addDynamicPrefix(prefixFullName, prefixAbbreviation, prefixValue);
        componentContentStates[3] = true;
        return this;
    }
    public UnitManagerBuilder clearDynamicPrefixesNAbbreviations() {
        prefixesDataModel.removeAllDynamicPrefixes();
        componentContentStates[3] = false;
        return this;
    }

    ///
    public UnitManagerBuilder addFundamentalUnitsDataModel(FundamentalUnitsDataModel fundamentalUnitsDataModel) throws UnitManagerBuilderException {
        if(!this.fundamentalUnitsDataModel.combineWith(fundamentalUnitsDataModel))
            throw new UnitManagerBuilderException("Some data models could not be combined due to structural incompatibility"
                    , "Make sure all the data models in both unit manager builders are structural compatiable. " +
                    "For example, data models with hashed repository can not combine with those having database repositories");

        componentContentStates[4] = true;
        return this;
    }
    public UnitManagerBuilder addFundamentalUnit(String unitSystem, String unitName, UNIT_TYPE unitType) {
        fundamentalUnitsDataModel.addFundamentalUnit(unitSystem, unitName, unitType);
        componentContentStates[4] = true;
        return this;
    }
    public UnitManagerBuilder clearFundUnits() {
        fundamentalUnitsDataModel.removeAllFundamentalUnits();
        componentContentStates[4] = false;
        return this;
    }

    ///
    /**
     * Attempts to merge with the CONTENT of the data models in another unit manager builder if they are structurally compatible.
     */
    public UnitManagerBuilder combineWith(UnitManagerBuilder otherBuilder) throws UnitManagerBuilderException {
        if(otherBuilder != null) {
            this.addPrefixDataModel(otherBuilder.prefixesDataModel)
                    .addFundamentalUnitsDataModel(otherBuilder.fundamentalUnitsDataModel)
                    .addBaseUnits(otherBuilder.baseUnits)
                    .addNonBaseUnits(otherBuilder.nonBaseUnits);

            this.conversionFavoritesDataModel.combineWith(otherBuilder.conversionFavoritesDataModel);
        }

        return this;
    }

    ///
    public boolean areMinComponentsForCreationAvailable() {
        //Determines if the minimum needed components are available to create an adequately functional unit manager.
        return componentContentStates[0] && componentContentStates[1] && componentContentStates[2] && componentContentStates[4] && someFundamentalUnitsAreImplemented();
    }
    private boolean someFundamentalUnitsAreImplemented() {
        //Assumption is that only base units can be be defined as fundamental units.
        for (Unit baseUnit : baseUnits) {
            if (fundamentalUnitsDataModel.containsUnitName(baseUnit.getName()))
                return true;
        }
        return false;
    }
    public boolean areAnyComponentsAvailable() {
        for (boolean componentState : componentContentStates) {
            if (componentState)
                return true;
        }
        return false;
    }

    private List<String> determineInvalidOrMissingComponentContentNeededForBuilding() {
        List<String> missingNeededComponents = new ArrayList<>();

        if (!componentContentStates[0])
            missingNeededComponents.add(" { Base Units } ");
        if (!componentContentStates[2])
            missingNeededComponents.add(" { Core Prefixes } ");
        if (!componentContentStates[4])
            missingNeededComponents.add(" { Fundamental Units } ");
        if (!someFundamentalUnitsAreImplemented())
            missingNeededComponents.add(" { All of the defined fundamental units have has not been implemented as base units }");

        return missingNeededComponents;
    }

    ///
    private void injectUnitManagerComponents(UnitManager unitManager){
        unitManager.setUnitsModelData(unitsDataModel);
        unitManager.setPrefixesModelData(prefixesDataModel);
        unitManager.setFundamentalUnitsModelData(fundamentalUnitsDataModel);
        unitManager.setUnitsClassifierDataModel(unitsClassifierDataModel);
        unitManager.setConversionFavoritesDataModel(conversionFavoritesDataModel);
    }
    private void injectUnitManagerDataModelRepositories(UnitManager unitManager){
        unitManager.getUnitsDataModel().setRepositoryWithDualKeyNCategory(unitsRepositoryWithDualKeyNCategory);
        unitManager.getPrefixesDataModel().setRepositoryWithDualKeyNCategory(prefixsRepositoryWithDualKeyNCategory);
        unitManager.getFundamentalUnitsDataModel().setRepositoryWithDualKeyNCategory(fundamentalUnitsRepositoryWithDualKeyNCategory);
        unitManager.getUnitsClassifierDataModel().setRepositoryWithDualKeyNCategory(unitsClassifierRepositoryWithDualKeyNCategory);
        unitManager.getConversionFavoritesDataModel().setRepositoryWithDualKeyNCategory(conversionFavoritesRepositoryWithDualKeyNCategory);
        unitManager.getConversionFavoritesDataModel().setSignificanceRankRepository(significanceRankHashedRepository);
    }
    private void injectUnitsDataModelDependencies(UnitsDataModel unitsDataModel){
        unitsDataModel.setPluralTextParser(pluralTextParser);
        unitsDataModel.setComponentUnitsDimensionParser(componentUnitsDimensionParser);
        unitsDataModel.setComponentUnitsDimensionSerializer(componentUnitsDimensionSerializer);
        unitsDataModel.setFundamentalUnitTypesDimensionSerializer(fundamentalUnitTypesDimensionSerializer);
        unitsDataModel.setAbbreviationFormatter(abbreviationFormatter);
    }
    private void injectUnitsDataModelSubComponents(UnitsDataModel unitsDataModel){
        unitsDataModel.setUnitsContentMainRetriever(unitsDataModelUnitsContentMainRetriever);
        unitsDataModel.setUnitsContentModifier(unitsDataModelUnitsContentModifier);
        unitsDataModel.setUnitsContentQuerier(unitsDataModelUnitsContentQuerier);
        unitsDataModel.setUnitsContentDeterminer(unitsDataModelUnitsContentDeterminer);
    }

    /**
     * Constructs a unit manager populated with user specified data.
     * @throws UnitManagerBuilderException
     */
    public UnitManager build() throws UnitManagerBuilderException, UnitException {

        UnitManagerBuilderException.validateRequiredComponentsCollection(determineInvalidOrMissingComponentContentNeededForBuilding());

        UnitManager unitManager = new UnitManager(locale);

        //
        injectUnitsDataModelDependencies(unitsDataModel);
        injectUnitsDataModelSubComponents(unitsDataModel);
        injectUnitManagerComponents(unitManager);
        injectUnitManagerDataModelRepositories(unitManager);

        //Set an unknown base unit to be returned when no other unit in data model matches a non-bulk query.
        Unit unknownUnit = null;
        try {
            unknownUnit = new Unit();
            unknownUnit.setCoreUnitState(true);
            unitsDataModel.getUnitsContentModifier().addUnit(unknownUnit);
        } catch (ParsingException e) {
            e.printStackTrace();
        }

        updateContent(unitManager);

        return unitManager;
    }

    /**
     * Updates any existing unit manager with the content of this unit manager builder.
     * But does not change the type and structure of the data models.
     */
    public boolean updateContent(UnitManager unitManager) throws UnitException {
        if ((componentContentStates[2] || componentContentStates[3])
                && unitManager.getPrefixesDataModel() != prefixesDataModel) //If 'updateContent' was called by 'build' then the prefixesDataModel would be identical
        {
            unitManager.getPrefixesDataModel().combineWith(prefixesDataModel);
        }

        if (componentContentStates[4]
                && unitManager.getFundamentalUnitsDataModel() != fundamentalUnitsDataModel)//If 'updateContent' was called by 'build' then the fundamentalUnitsDataModel would be identical
        {
            unitManager.getFundamentalUnitsDataModel().combineWith(fundamentalUnitsDataModel);
        }

        unitManager.getUnitsDataModel().getUnitsContentModifier().setUpdateAssociationsOfUnknownUnits(reUpdateAssociationsOfUnknownUnits);

        //Base units depend on the fundamental units map being set first in order to ensure that their types can be determined.
        if (componentContentStates[0])
            unitManager.getUnitsDataModel().getUnitsContentModifier().addUnits(baseUnits);

        //Non base units depend on the base units being set first in order to ensure dimensions and types are properly determined.
        if (componentContentStates[1])
            unitManager.getUnitsDataModel().getUnitsContentModifier().addUnits(nonBaseUnits);

        return areAnyComponentsAvailable();
    }
}
