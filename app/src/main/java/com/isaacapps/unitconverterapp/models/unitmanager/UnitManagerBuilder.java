package com.isaacapps.unitconverterapp.models.unitmanager;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
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
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.ContentDeterminer;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.ContentMainRetriever;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.ContentModifier;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.ContentQuerier;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsDataModel;
import com.isaacapps.unitconverterapp.processors.formatters.text.GeneralTextFormatter;
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
 * Responsible for assembling the structural components and valid content neeeded to construct a well formed unit manager
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

    private IDualKeyNCategoryRepository<String, Unit, ContentDeterminer.DATA_MODEL_CATEGORY> unitsRepositoryWithDualKeyNCategory;
    private IDualKeyNCategoryRepository<String, Double, ContentDeterminer.DATA_MODEL_CATEGORY> prefixsRepositoryWithDualKeyNCategory;
    private IDualKeyNCategoryRepository<String, UNIT_TYPE, String> fundamentalUnitsRepositoryWithDualKeyNCategory;
    private IDualKeyNCategoryRepository<String, Collection<String>, String> unitsClassifierRepositoryWithDualKeyNCategory;
    private IDualKeyNCategoryRepository<String, String, String> conversionFavoritesRepositoryWithDualKeyNCategory;
    private SignificanceRankHashedRepository significanceRankHashedRepository;

    private ContentMainRetriever unitsDataModelContentMainRetriever;
    private ContentModifier unitsDataModelContentModifier;
    private ContentQuerier unitsDataModelContentQuerier;
    private ContentDeterminer unitsDataModelContentDeterminer;

    private PluralTextParser pluralTextParser;
    private UnitParser unitParser;
    private ComponentUnitsDimensionParser componentUnitsDimensionParser;
    private ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer;
    private FundamentalUnitTypesDimensionParser fundamentalUnitTypesDimensionParser;
    private FundamentalUnitTypesDimensionSerializer fundamentalUnitTypesDimensionSerializer;

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
        initializeDataModels().initializeDataModelDependencies().initializeAllDataModelWithHashedRepositories();
        initializeUnitsDataModelComponents();
    }

    ///Default structural implementations
    public UnitManagerBuilder initializeAllDataModelWithHashedRepositories(){
        return setPrefixesHashedRepositoryWithDualKeyNCategory(new PrefixesDualKeyNCategoryHashedRepository())
                .setUnitsHashedRepositoryWithDualKeyNCategory(new UnitsDualKeyNCategoryHashedRepository())
                .setFundamentalUnitsHashedRepositoryWithDualKeyNCategory(new FundamentalUnitsHashDualKeyNCategoryHashedRepository())
                .setUnitsClassifierHashedRepositoryWithDualKeyNCategory(new UnitsClassifierDualKeyNCategoryHashedRepository())
                .setConversionFavoritesHashedRepositoryWithDualKeyNCategory(new ConversionFavoritesDualKeyNCategoryHashedRepository())
                .setSignificanceRankRepository(new SignificanceRankHashedRepository());
    }
    public UnitManagerBuilder initializeUnitsDataModelComponents(){
        return setUnitsDataModelContentQuerier(new ContentQuerier())
                .setUnitsDataModelContentMainRetriever(new ContentMainRetriever())
                .setUnitsDataModelContentModifier(new ContentModifier())
                .setUnitsDataModelContentDeterminer(new ContentDeterminer());
    }
    public UnitManagerBuilder initializeDataModels(){
        return setUnitsDataModel(new UnitsDataModel())
                .setPrefixesDataModel(new PrefixesDataModel())
                .setFundamentalUnitsDataModel(new FundamentalUnitsDataModel())
                .setUnitsClassifierDataModel(new UnitsClassifierDataModel())
                .setConversionFavoritesDataModel(new ConversionFavoritesDataModel());
    }
    public UnitManagerBuilder initializeDataModelDependencies(){
        return setPluralTextParser(new EnglishPluralTextParser())
                .setUnitParser(new UnitParser(componentUnitsDimensionParser, componentUnitsDimensionSerializer))
                .setComponentUnitsDimensionParser(new ComponentUnitsDimensionParser())
                .setComponentUnitsDimensionSerializer(new ComponentUnitsDimensionSerializer(locale, new ComponentUnitsDimensionItemSerializer(locale, new GeneralTextFormatter(locale))))
                .setFundamentalUnitTypesDimensionParser(new FundamentalUnitTypesDimensionParser())
                .setFundamentalUnitTypesDimensionSerializer( new FundamentalUnitTypesDimensionSerializer(locale, new FundamentalUnitTypesDimensionItemSerializer(locale, new GeneralTextFormatter(locale))));
    }

    ///
    public UnitManagerBuilder setPrefixesHashedRepositoryWithDualKeyNCategory(IDualKeyNCategoryRepository<String, Double, ContentDeterminer.DATA_MODEL_CATEGORY> repositoryWithDualKeyNCategory){
        this.prefixsRepositoryWithDualKeyNCategory = repositoryWithDualKeyNCategory;
        return this;
    }

    public UnitManagerBuilder setUnitsHashedRepositoryWithDualKeyNCategory(IDualKeyNCategoryRepository<String, Unit, ContentDeterminer.DATA_MODEL_CATEGORY> repositoryWithDualKeyNCategory){
        this.unitsRepositoryWithDualKeyNCategory = repositoryWithDualKeyNCategory;
        return this;
    }

    public UnitManagerBuilder setFundamentalUnitsHashedRepositoryWithDualKeyNCategory(IDualKeyNCategoryRepository<String, UNIT_TYPE, String> repositoryWithDualKeyNCategory){
        this.fundamentalUnitsRepositoryWithDualKeyNCategory = repositoryWithDualKeyNCategory;
        return this;
    }

    public UnitManagerBuilder setUnitsClassifierHashedRepositoryWithDualKeyNCategory(IDualKeyNCategoryRepository<String, Collection<String>, String> repositoryWithDualKeyNCategory){
        this.unitsClassifierRepositoryWithDualKeyNCategory = repositoryWithDualKeyNCategory;
        return this;
    }

    public UnitManagerBuilder setConversionFavoritesHashedRepositoryWithDualKeyNCategory(IDualKeyNCategoryRepository<String, String, String> repositoryWithDualKeyNCategory){
        this.conversionFavoritesRepositoryWithDualKeyNCategory = repositoryWithDualKeyNCategory;
        return this;
    }

    public UnitManagerBuilder setSignificanceRankRepository(SignificanceRankHashedRepository significanceRankRepository){
        this.significanceRankHashedRepository = significanceRankRepository;
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
    public UnitManagerBuilder setUnitsDataModelContentMainRetriever(ContentMainRetriever contentMainRetriever){
        this.unitsDataModelContentMainRetriever = contentMainRetriever;
        return this;
    }
    public UnitManagerBuilder setUnitsDataModelContentModifier(ContentModifier contentModifier){
        this.unitsDataModelContentModifier = contentModifier;
        return this;
    }
    public UnitManagerBuilder setUnitsDataModelContentQuerier(ContentQuerier contentQuerier){
        this.unitsDataModelContentQuerier = contentQuerier;
        return this;
    }
    public UnitManagerBuilder setUnitsDataModelContentDeterminer(ContentDeterminer contentDeterminer){
        this.unitsDataModelContentDeterminer = contentDeterminer;
        return this;
    }

    ///
    public UnitManagerBuilder setPluralTextParser(PluralTextParser pluralTextParser){
        this.pluralTextParser = pluralTextParser;
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

    ///
    public UnitManagerBuilder addPrefixDataModel(PrefixesDataModel prefixesDataModel) {
        this.prefixesDataModel.combineWith(prefixesDataModel);
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

    public UnitManagerBuilder cleaDynamicPrefixesNAbbreviations() {
        prefixesDataModel.removeAllDynamicPrefixes();
        componentContentStates[3] = false;
        return this;
    }

    ///
    public UnitManagerBuilder addFundamentalUnitsDataModel(FundamentalUnitsDataModel fundamentalUnitsDataModel) {
        this.fundamentalUnitsDataModel.combineWith(fundamentalUnitsDataModel);
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
    public UnitManagerBuilder clearAll() {
        clearBaseUnits();
        clearNonBaseUnits();
        clearCorePrefixes();
        cleaDynamicPrefixesNAbbreviations();
        return clearFundUnits();
    }

    ///

    /**
     * Attempts to merge with the CONTENT of the data models in another unit manager builder if they are structurally compatible.
     */
    public UnitManagerBuilder combineWith(UnitManagerBuilder otherBuilder) throws UnitManagerBuilderException {
        this.addBaseUnits(otherBuilder.baseUnits);
        this.addNonBaseUnits(otherBuilder.nonBaseUnits);

        boolean contentOfDataModelsCouldBeCombined = this.prefixesDataModel.combineWith(otherBuilder.prefixesDataModel) &&
                this.fundamentalUnitsDataModel.combineWith(otherBuilder.fundamentalUnitsDataModel)
                && this.conversionFavoritesDataModel.combineWith(otherBuilder.conversionFavoritesDataModel);

        if(!contentOfDataModelsCouldBeCombined)
            throw new UnitManagerBuilderException("Some data models could not be combined due to structural incomatibility"
                    , "Make sure all the data models in both unit manager builders are structural compatiable. " +
                    "For example, data models with hashed repositiory can not combine with those having database repositiories");

        return this;
    }

    ///
    public boolean areMinComponentsForCreationAvailable() {
        //Determines if the minimum needed components are available to create an adequately functional unit manager.
        return componentContentStates[0] && componentContentStates[2] && componentContentStates[4] && fundamentalUnitsAreImplemented();
    }

    private boolean fundamentalUnitsAreImplemented() {
        //Assumption is that only base units can be be defined as fundamental units.
        for (Unit baseUnit : baseUnits) {
            if (!fundamentalUnitsDataModel.containsUnitName(baseUnit.getName()))
                return false;
        }
        return true;
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
        if (!fundamentalUnitsAreImplemented())
            missingNeededComponents.add(" { All of the defined fundamental units have has not been implemented as base units }");

        return missingNeededComponents;
    }

    ///

    /**
     * Constructs a unit manager and populated with user specified data.
     * @throws UnitManagerBuilderException
     */
    public UnitManager build() throws UnitManagerBuilderException {

        UnitManagerBuilderException.validateRequiredComponentsCollection(determineInvalidOrMissingComponentContentNeededForBuilding());

        UnitManager unitManager = new UnitManager(locale);

        //Inject unit manager components
        unitManager.setUnitsModelData(unitsDataModel);
        unitManager.setPrefixesModelData(prefixesDataModel);
        unitManager.setFundamentalUnitsModelData(fundamentalUnitsDataModel);
        unitManager.setUnitsClassifierDataModel(unitsClassifierDataModel);
        unitManager.setConversionFavoritesDataModel(conversionFavoritesDataModel);

        //Inject repositories
        unitsDataModel.setRepositoryWithDualKeyNCategory(unitsRepositoryWithDualKeyNCategory);
        prefixesDataModel.setRepositoryWithDualKeyNCategory(prefixsRepositoryWithDualKeyNCategory);
        fundamentalUnitsDataModel.setRepositoryWithDualKeyNCategory(fundamentalUnitsRepositoryWithDualKeyNCategory);
        unitsClassifierDataModel.setRepositoryWithDualKeyNCategory(unitsClassifierRepositoryWithDualKeyNCategory);
        conversionFavoritesDataModel.setRepositoryWithDualKeyNCategory(conversionFavoritesRepositoryWithDualKeyNCategory);
        conversionFavoritesDataModel.setSignificanceRankRepository(significanceRankHashedRepository);

        //Inject units datamodel components
        unitsDataModel.setContentMainRetriever(unitsDataModelContentMainRetriever);
        unitsDataModel.setContentModifier(unitsDataModelContentModifier);
        unitsDataModel.setContentQuerier(unitsDataModelContentQuerier);
        unitsDataModel.setContentDeterminer(unitsDataModelContentDeterminer);

        unitsDataModel.setPluralTextParser(pluralTextParser);
        unitsDataModel.setUnitParser(unitParser);
        unitsDataModel.setComponentUnitsDimensionParser(componentUnitsDimensionParser);
        unitsDataModel.setComponentUnitsDimensionSerializer(componentUnitsDimensionSerializer);
        unitsDataModel.setFundamentalUnitTypesDimensionSerializer(fundamentalUnitTypesDimensionSerializer);

        //Set an unknown base unit to be returned when no other unit in data model matches a query.
        Unit unknownUnit = new Unit();
        unknownUnit.setCoreUnitState(true);
        unitManager.getUnitsDataModel().getContentModifier().addUnit(unknownUnit);

        update(unitManager);

        return unitManager;
    }

    /**
     * Updates any existing unit manager with the content of this unit manager builder.
     * But does not change the type and structure of the data models.
     */
    public boolean update(UnitManager unitManager) {
        if ((componentContentStates[2] || componentContentStates[3])
                && unitManager.getPrefixesDataModel() != prefixesDataModel) //If 'update' was called by 'build' then the prefixesDataModel would be identical
        {
            unitManager.getPrefixesDataModel().combineWith(prefixesDataModel);
        }

        if (componentContentStates[4]
                && unitManager.getFundamentalUnitsDataModel() != fundamentalUnitsDataModel)//If 'update' was called by 'build' then the fundamentalUnitsDataModel would be identical
        {
            unitManager.getFundamentalUnitsDataModel().combineWith(fundamentalUnitsDataModel);
        }

        //Base units depend on the fundamental units map being set first in order to ensure that their types can be determined.
        if (componentContentStates[0])
            unitManager.getUnitsDataModel().getContentModifier().addUnits(baseUnits);

        //Non base units depend on the base units being set first in order to ensure dimensions and types are properly determined.
        if (componentContentStates[1])
            unitManager.getUnitsDataModel().getContentModifier().addUnits(nonBaseUnits);

        return areAnyComponentsAvailable();
    }
}
