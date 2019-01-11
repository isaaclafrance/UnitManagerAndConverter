package com.isaacapps.unitconverterapp.models.measurables.unit;

import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel.UNIT_TYPE;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsContentDeterminer;
import com.isaacapps.unitconverterapp.processors.formatters.text.GeneralTextFormatter;
import com.isaacapps.unitconverterapp.processors.operators.dimensions.DimensionOperators;
import com.isaacapps.unitconverterapp.processors.operators.measurables.units.UnitOperators;
import com.isaacapps.unitconverterapp.processors.operators.dimensions.DimensionOperators.DIMENSION_TYPE;
import com.isaacapps.unitconverterapp.processors.parsers.ParsingException;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.DimensionComponentDefiner;
import com.isaacapps.unitconverterapp.processors.serializers.SerializingException;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.componentnunit.ComponentUnitsDimensionItemSerializer;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.componentnunit.ComponentUnitsDimensionSerializer;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.fundamentalunit.FundamentalUnitTypesDimensionItemSerializer;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.fundamentalunit.FundamentalUnitTypesDimensionSerializer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsContentDeterminer.determineHighestPriorityDataModelCategory;
import static com.isaacapps.unitconverterapp.processors.parsers.dimension.componentunits.ComponentUnitsDimensionParser.COMPONENT_NAME_REGEX;

public class Unit {
    ///
    public static final String BASE_VALUE_EXPRESSION_ARGUMENT = "{x}";
    public static final String UNKNOWN_UNIT_NAME = "unknown_unit", UNKNOWN_UNIT_SYSTEM = "unknown_system", UNKNOWN_UNIT_CATEGORY = "unknown_category", UNKNOWN_UNIT_ABBREVIATION = "u_unit", UNIT_SYSTEM_DELIMITER = " and ";

    protected UnitManager unitManagerContext;

    protected Locale locale;
    private DimensionComponentDefiner dimensionComponentDefiner;
    private ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer;
    private FundamentalUnitTypesDimensionSerializer fundamentalUnitTypesDimensionSerializer;

    /**
     * Ensures the unit can not be deleted, or grossly modified due to its importance
     */
    private boolean isCoreUnit;
    private boolean isBaseUnit;
    private UNIT_TYPE unitType;
    private DIMENSION_TYPE dimensionType;
    private Unit baseUnit;

    private String name; //This will always be the American English spelling of a units;
    private String abbreviation; //For most units, even if the name is spelled differently by region, the abbreviation is always the same.
    private Set<String> unitNameAliases; //An example would be the plural alias of a unit or different regional spelling. For instance, 'meter' in American English vs 'metro' in Spanish.
    private String category, description, unitSystem;

    private String baseConversionExpression;
    private double[] baseConversionPolyCoeffs; //Coefficients used to convert current unit to base unit. Currently, only linear polynomial conversion functions can be modeled.
    private Map<Unit, double[]> conversionsToDescendents; //Contains functions that enable base unit to convert to units of same dimension that dependent on it.

    private Map<String, Double> componentUnitsDimension; //Map that associates derivative component unit names with the value of their exponential power
    private Map<UNIT_TYPE, Double> fundamentalUnitTypesDimension; //Map that associates derivative component fundamental units with the value of their exponential power

    ///
    public Unit() throws ParsingException {
        this(UNKNOWN_UNIT_NAME, UNKNOWN_UNIT_ABBREVIATION);
    }
    public Unit(String name, String abbreviation) throws ParsingException {
        this(name, abbreviation, Locale.getDefault(), new ComponentUnitsDimensionSerializer(Locale.getDefault())
                , new FundamentalUnitTypesDimensionSerializer(Locale.getDefault())
                , new DimensionComponentDefiner(COMPONENT_NAME_REGEX));
    }
    public Unit(String name, Map<String, Double> componentUnitsDimension) throws ParsingException {
        this(name, "");
        this.componentUnitsDimension = componentUnitsDimension;
    }
    public Unit(Map<String, Double> componentUnitsDimension) throws ParsingException{
        this(UNKNOWN_UNIT_NAME, componentUnitsDimension);
        try {
            this.name = componentUnitsDimensionSerializer.serialize(componentUnitsDimension);
        } catch (SerializingException e) {
            e.printStackTrace();
        }
    }
    public Unit(String name, String abbreviation, Locale locale, ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer, FundamentalUnitTypesDimensionSerializer fundamentalUnitTypesDimensionSerializer, DimensionComponentDefiner dimensionComponentDefiner) {
        this.locale = locale;
        this.dimensionComponentDefiner = dimensionComponentDefiner;
        this.componentUnitsDimensionSerializer = componentUnitsDimensionSerializer;
        this.fundamentalUnitTypesDimensionSerializer = fundamentalUnitTypesDimensionSerializer;

        unitType = UNIT_TYPE.UNKNOWN;

        this.name = name;
        unitNameAliases = new HashSet<>();
        setUnitSystem(UNKNOWN_UNIT_SYSTEM, false);
        setCategory(UNKNOWN_UNIT_CATEGORY, false);
        setDescription("");
        setAbbreviation(abbreviation);

        conversionsToDescendents = new HashMap<>();
        componentUnitsDimension = new HashMap<>();
        componentUnitsDimension.put(name, 1.0);
        fundamentalUnitTypesDimension = new EnumMap<>(UNIT_TYPE.class);
        fundamentalUnitTypesDimension.put(UNIT_TYPE.UNKNOWN, 1.0);

        isCoreUnit = false;
        isBaseUnit = true;
        baseUnit = this;
        baseConversionPolyCoeffs = new double[]{1.0, 0.0};
    }
    public Unit(String name, Set<String> unitNameAliases, String category, String description
            , String unitSystem, String abbreviation, Map<String, Double> componentUnitsDimension
            , Unit baseUnit, double[] baseConversionPolyCoeffs, Locale locale, ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer, FundamentalUnitTypesDimensionSerializer fundamentalUnitTypesDimensionSerializer, DimensionComponentDefiner dimensionComponentDefiner) throws UnitException {
        this(name, abbreviation, locale, componentUnitsDimensionSerializer, fundamentalUnitTypesDimensionSerializer, dimensionComponentDefiner);
        this.componentUnitsDimension = componentUnitsDimension;
        this.unitNameAliases = unitNameAliases;

        setCategory(category, false);
        setDescription(description);
        setUnitSystem(unitSystem, false);
        setBaseUnit(baseUnit, baseConversionPolyCoeffs);
    }

    ///
    public void setAutomaticCategory() {
        if (!getFundamentalUnitTypesDimension().containsKey(UNIT_TYPE.UNKNOWN) && !isCoreUnit) {
            if (!baseUnit.category.equalsIgnoreCase(UNKNOWN_UNIT_CATEGORY)) {
                setCategory(baseUnit.getCategory(), !baseUnit.getCategory().equalsIgnoreCase(category));
            } else {
                try {
                    String dimensionAsCategory = fundamentalUnitTypesDimensionSerializer.serialize(fundamentalUnitTypesDimension);
                    setCategory(dimensionAsCategory, !dimensionAsCategory.equalsIgnoreCase(category));
                }
                catch(Exception e){}
            }
        }
    }
    public void setAutomaticUnitSystem() {
        if (getUnitManagerContext() != null && !isCoreUnit) {
            //If the component units are both SI and US for example, then the unit system is "SI {delimiter} US".
            Set<String> unitSystemsOfComponents = new HashSet<>();
            Unit componentUnit;

            for (String componentUnitName : componentUnitsDimension.keySet()) {
                componentUnit = getUnitManagerContext().getUnitsDataModel().getUnitsContentMainRetriever().getUnit(componentUnitName, false);
                if (componentUnit != null) {
                    String[] unitSystems = componentUnit.getUnitSystem().split(UNIT_SYSTEM_DELIMITER);
                    Collections.addAll(unitSystemsOfComponents, unitSystems);
                }
            }

            StringBuilder unitSystemCandidateBuilder = new StringBuilder();
            for (String unitSystem : unitSystemsOfComponents) {
                if (unitSystemCandidateBuilder.length() == 0) {
                    unitSystemCandidateBuilder.append(unitSystem);
                } else {
                    unitSystemCandidateBuilder.append(UNIT_SYSTEM_DELIMITER).append(unitSystem);
                }
            }

            //Use the baseunit's unit system as an alternative if necessary
            if (unitSystemCandidateBuilder.toString()
                    .equalsIgnoreCase(getUnitManagerContext().getUnitsDataModel().getUnitsContentMainRetriever().getUnknownUnit().getUnitSystem())) {
                if (baseUnit != null) {
                    unitSystemCandidateBuilder.delete(0, unitSystemCandidateBuilder.length()).append(baseUnit.getUnitSystem());
                }
            }

            setUnitSystem(unitSystemCandidateBuilder.toString(), !unitSystemCandidateBuilder.toString().equalsIgnoreCase(unitSystem));
        }
    }
    public boolean setAutomaticUnitTypeNFundamentalTypesDimension() {
        dimensionType = DimensionOperators.determineDimensionType(componentUnitsDimension);
        if (getUnitManagerContext() != null) {
            unitType = getUnitManagerContext().getUnitsDataModel().getUnitsContentDeterminer().determineUnitType(this);
            if(unitType != UNIT_TYPE.COMPLEX && unitType != UNIT_TYPE.UNKNOWN){
                fundamentalUnitTypesDimension = new HashMap<>();
                fundamentalUnitTypesDimension.put(unitType, 1.0);
            }
            else {
                fundamentalUnitTypesDimension = getUnitManagerContext().getFundamentalUnitsDataModel()
                        .transformComponentUnitsDimensionToFundamentalUnitsDimension(this.componentUnitsDimension, getUnitSystemsCollection(), true); //!isCoreUnit
            }
        } else {
            unitType = UNIT_TYPE.UNKNOWN;
            fundamentalUnitTypesDimension = new HashMap<>();
            fundamentalUnitTypesDimension.put(UNIT_TYPE.UNKNOWN, 1.0);
        }
        return unitType != UNIT_TYPE.UNKNOWN && !fundamentalUnitTypesDimension.containsKey(UNIT_TYPE.UNKNOWN);
    }
    private void setAutomaticAbbreviation() {
        //Create a dimensional string with each of the component units abbreviated. For example, format would be something like 'in^2/sec^5'
        if (getUnitManagerContext() != null && abbreviation.equalsIgnoreCase(UNKNOWN_UNIT_ABBREVIATION) && !name.equalsIgnoreCase(UNKNOWN_UNIT_NAME)) {
            Map<String, Double> abbreviatedComponentDimension = new HashMap<>();
            for (Map.Entry<String, Double> componentUnitEntry : componentUnitsDimension.entrySet())
                abbreviatedComponentDimension.put(getUnitManagerContext().getUnitsDataModel().getUnitsContentMainRetriever()
                        .getAbbreviation(componentUnitEntry.getKey()), componentUnitEntry.getValue());

            try {
                setAbbreviation(componentUnitsDimensionSerializer.serialize(abbreviatedComponentDimension)
                        .replaceAll("[()]", ""));
            } catch (SerializingException e) {
                setAbbreviation(UNKNOWN_UNIT_ABBREVIATION);
                e.printStackTrace();
            }
        }
    }

    ///
    /**
     * Automatically finds and sets base unit from own reference of Unit Manager context
     * @return Whether any new base unit was set.
     */
    public boolean setAutomaticBaseUnit() throws UnitException {
        //Update the unit type and fundamental type dimension so that they are both current since, they will help determine which base unit is selected
        if (getUnitManagerContext() != null && setAutomaticUnitTypeNFundamentalTypesDimension()) {

            Unit retrievedBaseUnit = getUnitManagerContext().getUnitsDataModel().getUnitsContentDeterminer().determineBaseUnit(this, isCoreUnit && dimensionComponentDefiner != null && !dimensionComponentDefiner.hasComplexDimensions(name));//false

            boolean alreadyHasRetrievedBaseUnit = (retrievedBaseUnit.getName().equalsIgnoreCase(UNKNOWN_UNIT_NAME) || this.getBaseUnit() == retrievedBaseUnit) && this.isBaseUnit; //Treat as base unit if already a base unit and no other base unit in unit manager can be found.

            if (!alreadyHasRetrievedBaseUnit
                    && determineHighestPriorityDataModelCategory(retrievedBaseUnit) != UnitsContentDeterminer.DATA_MODEL_CATEGORY.UNKNOWN) {

                setBaseUnit(retrievedBaseUnit, DimensionOperators.hasComplexDimensionType(componentUnitsDimension));
                return true;
            }
            return alreadyHasRetrievedBaseUnit;
        }
        return false;
    }
    public void setAsBaseUnit() throws UnitException {
        setBaseUnit(this);
    }
    public void setBaseUnit(Unit baseUnit) throws UnitException {
        if (hasCompatibleCoreUnitStateWith(baseUnit))
            setBaseUnit(baseUnit, true);
    }
    public void setBaseUnit(Unit baseUnit, double[] baseConversionPolyCoeffs) throws UnitException {
        if (hasCompatibleCoreUnitStateWith(baseUnit)) {
            this.baseConversionPolyCoeffs = baseConversionPolyCoeffs;
            this.baseConversionExpression = null;
            setBaseUnit(baseUnit, false);
        }
    }
    public void setBaseUnit(Unit baseUnit, String baseConversionExpression) throws UnitException {
        if (hasCompatibleCoreUnitStateWith(baseUnit)) {
            this.baseConversionPolyCoeffs = new double[]{1.0, 0.0};
            this.baseConversionExpression = baseConversionExpression;
            setBaseUnit(baseUnit, false);
        }
    }
    private void setBaseUnit(Unit specifiedBaseUnit, boolean attemptToSetAutomaticBaseConversionPolyCoeffs) throws UnitException {
        if((!specifiedBaseUnit.isBaseUnit || attemptToSetAutomaticBaseConversionPolyCoeffs) && isUsingBaseConversionExpression())
            throw new UnitException(String.format("The base unit %s that was originally specified for %s was not an actual base unit, therefore there is a need to a perform a cascaded recalculation of the specified base conversion expression." +
                    " However, currently there is no capability to perform such calculation for units with arbitrary base conversion expressions", specifiedBaseUnit.getName(), this.getName())
                    , "Either directly specify a base unit that is an actual base unit or if possible use a suitable conversion that defined in terms of linear coefficients rather than an arbitrary expression.");


        //If initially assigned base unit is not a base unit, then iteratively retrieve next base unit level and successively cascades base conversion.
        Unit cascadedBaseUnit = specifiedBaseUnit;
        double[] candidateBaseConversionPolyCoeffs = new double[]{this.baseConversionPolyCoeffs[0], this.baseConversionPolyCoeffs[1]};
        while(!cascadedBaseUnit.isBaseUnit && !cascadedBaseUnit.name.equalsIgnoreCase(this.name)) {
            candidateBaseConversionPolyCoeffs[0] = candidateBaseConversionPolyCoeffs[0] * cascadedBaseUnit.baseConversionPolyCoeffs[0];
            candidateBaseConversionPolyCoeffs[1] += candidateBaseConversionPolyCoeffs[1] * cascadedBaseUnit.baseConversionPolyCoeffs[0];
            cascadedBaseUnit = cascadedBaseUnit.baseUnit;
        }

        //
        boolean baseUnitCanBeChanged = this.getUnitManagerContext() == cascadedBaseUnit.getUnitManagerContext() && UnitOperators.equalsFundamentalUnitsDimension(this, cascadedBaseUnit)
                || ( determineHighestPriorityDataModelCategory(this.baseUnit) == UnitsContentDeterminer.DATA_MODEL_CATEGORY.UNKNOWN || determineHighestPriorityDataModelCategory(cascadedBaseUnit) == UnitsContentDeterminer.DATA_MODEL_CATEGORY.UNKNOWN);

        boolean baseUnitStatusHasChanged = false;
        if (baseUnitCanBeChanged) {
            this.baseUnit = cascadedBaseUnit;

            if (this.equals(this.baseUnit)) {
                baseUnitStatusHasChanged = !this.isBaseUnit;
                this.isBaseUnit = true;
            } else {
                baseUnitStatusHasChanged = this.isBaseUnit;
                this.isBaseUnit = false;
                if(baseUnitStatusHasChanged)
                    downPropogateBaseUnitModification(cascadedBaseUnit, true);
            }

            this.baseConversionPolyCoeffs = candidateBaseConversionPolyCoeffs;
        }

        //
        if(baseUnitCanBeChanged) {
            if (attemptToSetAutomaticBaseConversionPolyCoeffs) {
                if (this.isBaseUnit) {
                    baseConversionPolyCoeffs = new double[]{1.0, 0.0};
                }
                else {
                    adjustBaseConversionUsingOverallComponentConversionFactorToFundamentalBase();
                }
            }

            //Now that an actual base unit has been obtained by this point, updateContent this base unit with this unit as its descendant
            this.baseUnit.addDescendentConversion(this, new double[]{1.0 / baseConversionPolyCoeffs[0]
                    , -baseConversionPolyCoeffs[1] / baseConversionPolyCoeffs[0]});

            //Modify this unit's properties that may indirectly depend on the base unit
            setAutomaticUnitSystem();
            setAutomaticUnitTypeNFundamentalTypesDimension();
            setAutomaticCategory();

            //If this unit became a new base unit for the recent assignment, then reintroduce this unit to the unit manager in order to have it notify other units without proper base units .
            if (baseUnitStatusHasChanged && getUnitManagerContext() != null)
                getUnitManagerContext().getUnitsDataModel().getUnitsContentModifier().addUnit(this);
        }
    }

    /**
     * The primary unit and its specified base are decomposed to their component units.
     * The overall conversion factor to a hypothetical base unit is calculated from these individual components and is used to determine the base conversion factor of the primary unit.
     */
    private void adjustBaseConversionUsingOverallComponentConversionFactorToFundamentalBase() {
        double baseUnitOverallComponentFactor = baseUnit.calculateOverallComponentConversionFactorToFundamentalBase();
        double currentUnitOverallComponentFactor = this.calculateOverallComponentConversionFactorToFundamentalBase();

        double scaledOverallComponentFactor = currentUnitOverallComponentFactor / baseUnitOverallComponentFactor;

        this.baseConversionPolyCoeffs[0] = scaledOverallComponentFactor * this.baseConversionPolyCoeffs[0];
    }
    private double calculateOverallComponentConversionFactorToFundamentalBase() {
        double overallFactor = 1.0;

        if (!this.fundamentalUnitTypesDimension.containsKey(UNIT_TYPE.UNKNOWN) && getUnitManagerContext() != null) {
            for (Map.Entry<String, Double> entry : componentUnitsDimension.entrySet()) {
                double componentFactor = 1.0;
                Unit componentUnit = getUnitManagerContext().getUnitsDataModel().getUnitsContentMainRetriever().getUnit(entry.getKey(), false);
                if (componentUnit.getUnitType() == UNIT_TYPE.COMPLEX && componentUnit.getDimensionType() == DIMENSION_TYPE.DERIVED_MULTI) {
                    componentFactor = componentUnit.calculateOverallComponentConversionFactorToFundamentalBase();
                } else if (!componentUnit.isBaseUnit) {
                    componentFactor = this != componentUnit ? componentUnit.getBaseUnit()
                            .calculateOverallComponentConversionFactorToFundamentalBase()
                            * componentUnit.getBaseConversionPolyCoeffs()[0] : 1.0; //recursively calculate component base conversion until absolute base is reached.
                }
                overallFactor *= Math.pow(componentFactor, entry.getValue());
            }
        }

        return overallFactor;
    }

    /**
     * Called when this unit switches from being a base unit to not.
     * Changes base unit of the previously dependent units to the new base unit.
     */
    public void downPropogateBaseUnitModification(Unit newBaseUnit, boolean resetBaseConversionPolyCoefs) throws UnitException {
        for (Unit depUnit : conversionsToDescendents.keySet()) {
            depUnit.setBaseUnit(newBaseUnit, resetBaseConversionPolyCoefs);
        }
        conversionsToDescendents.clear();
    }

    ///
    private boolean addDescendentConversion(Unit targetUnit, double[] polynomialCoeffs) {
        if(this == targetUnit)
            return false;

        //If the object is a base unit, then the conversion polynomial coefficients are simply stored as is. Otherwise adds appropriately modified conversion to base unit
        if (isBaseUnit) {
            conversionsToDescendents.put(targetUnit, polynomialCoeffs);
            //Sometimes depending on how the base unit unit was added, it may need category info from its conversion descendents

            if ( dimensionComponentDefiner.hasComplexDimensions(this.category) && !dimensionComponentDefiner.hasComplexDimensions(targetUnit.category)) {
                setCategory(targetUnit.category);
            }
        } else {
            baseUnit.addDescendentConversion(targetUnit, new double[]{1.0 / baseUnit.getBaseConversionPolyCoeffs()[0] * polynomialCoeffs[0],
                    1.0 / getBaseConversionPolyCoeffs()[1] * polynomialCoeffs[0] + polynomialCoeffs[1]});
        }

        return true;
    }

    ///
    public void addComponentUnit(String componentUnitName, double exponent, boolean updateUnitName) throws UnitException {
        if (!isCoreUnit) {
            String potentiallySingularizedComponentUnitName = getUnitManagerContext() != null
                    ? getUnitManagerContext().getUnitsDataModel().getUnitsContentDeterminer().determineSingularOfUnitName(componentUnitName)
                    : componentUnitName;

            componentUnitsDimension.put(potentiallySingularizedComponentUnitName, exponent);

            if (updateUnitName
                    && getUnitManagerContext() == null )  //Data structures in unit manager assume name is constant. Don't want ot break those.
            {
                try {
                    name = componentUnitsDimensionSerializer.serialize(componentUnitsDimension);
                } catch (SerializingException e) { }
                setAutomaticAbbreviation();
            }

            if (getUnitManagerContext() != null) {
                setAutomaticBaseUnit();
                setAutomaticUnitSystem();
                setAutomaticCategory();
            }
        }
    }
    public void clearComponentUnitsDimension(boolean updateName) throws UnitException {
        if (!isCoreUnit) {
            componentUnitsDimension.clear();
            if (updateName) {
                name = UNKNOWN_UNIT_NAME;
                setAbbreviation(UNKNOWN_UNIT_ABBREVIATION);
            }
            if (getUnitManagerContext() != null) {
                setAutomaticBaseUnit();
                setAutomaticUnitSystem();
                setAutomaticCategory();
            }
        }
    }

    ///
    public boolean hasCompatibleCoreUnitStateWith(Unit otherUnit){
        return otherUnit != null &&  (!this.isCoreUnit || otherUnit.isCoreUnit);
    }

    ///
    public String getName() { //Ideally one should only be able to get name. It should only be set during unit initialization
        return name;
    }

    /**
     * An example would be a nonstandard plural alias of a unit or different regional spelling. For instance, 'meter' in American English vs 'metro' in Spanish.
     */
    public Set<String> getAliases() {
        return unitNameAliases;
    }
    public boolean addUnitNameAlias(String alias) {
        unitNameAliases.add(alias);
        return getUnitManagerContext() != null && getUnitManagerContext()
                .getUnitsDataModel().getUnitsContentModifier().mapUnidirectionalAliasToUnitName(name, alias);
    }

    public String getUnitSystem() {
        return unitSystem;
    }
    public Collection<String> getUnitSystemsCollection(){
        return getUnitSystemsCollection(unitSystem);
    }
    public static Collection<String> getUnitSystemsCollection(String potentiallyMixedUnitSystem){
        return Arrays.asList(potentiallyMixedUnitSystem.split(UNIT_SYSTEM_DELIMITER));
    }
    public void setUnitSystem(String unitSystem) {
        setUnitSystem(unitSystem, true);
    }
    private void setUnitSystem(String unitSystem, boolean notifyUnitsClassifier) {
        this.unitSystem = unitSystem.trim().isEmpty()?UNIT_SYSTEM_DELIMITER: unitSystem.toLowerCase();

        //Re-add this unit to the unit classifier with its new unit system classification such that any other
        //pre-exisiting classifications for the same units name are removed.
        if (getUnitManagerContext() != null && notifyUnitsClassifier)
            getUnitManagerContext().getUnitsClassifierDataModel().addToHierarchy(this, true);
    }

    public String getAbbreviation() {
        return abbreviation;
    }
    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation.trim().isEmpty()?UNKNOWN_UNIT_ABBREVIATION: abbreviation;

        if (getUnitManagerContext() != null)
            getUnitManagerContext().getUnitsDataModel().getUnitsContentModifier()
                    .mapBidirectionalAbbreviationToUnitName(name, abbreviation);
    }

    public String getCategory() {
        return category;
    }
    public void setCategory(String unitCategory) {
        setCategory(unitCategory, true);
    }
    /**
     * Set the unit's category and optionally ee-adds this unit to the unit classifier with its new category classification such that any other
     * pre-existing classifications for the same units name are removed.
     *
     * @param unitCategory
     * @param notifyUnitsClassfier allows for optional messaging of units classifier to updateContent its state
     */
    private void setCategory(String unitCategory, boolean notifyUnitsClassfier) {
        this.category = unitCategory.trim().isEmpty()?UNKNOWN_UNIT_CATEGORY: unitCategory.toLowerCase();

        if (getUnitManagerContext() != null && notifyUnitsClassfier)
            getUnitManagerContext().getUnitsClassifierDataModel().addToHierarchy(this, true);
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Ensures the unit can not be deleted, or grossly modified due to its importance
     */
    public boolean isCoreUnit() {
        return isCoreUnit;
    }
    public void setCoreUnitState(boolean state) {
        isCoreUnit = state;
    }

    public boolean isBaseUnit() {
        return isBaseUnit;
    }
    public Unit getBaseUnit() {
        return baseUnit;
    }

    /**
     * Coefficients used to convert current unit to base unit. Currently, only linear polynomial conversion functions can be modeled.
     */
    public double[] getBaseConversionPolyCoeffs() {
        return this.baseConversionPolyCoeffs;
    }

    /**
     * A complex expression to describes how to convert from the current unit to it base unit equivalent.
     * The transformation must be represented with respect to {@link #BASE_VALUE_EXPRESSION_ARGUMENT}.
     */
    public String getBaseConversionExpression() {
        return baseConversionExpression;
    }
    private void setBaseConversionExpression(String baseConversionExpression) {
        if (baseConversionExpression != null && baseConversionExpression.contains(BASE_VALUE_EXPRESSION_ARGUMENT)) {
            this.baseConversionExpression = baseConversionExpression;

            //Since base conversion expression can not be accurately represented by linear equation for all cases
            //, simply set coefficient of the linear conversion data structuree to invariant values.
            baseConversionPolyCoeffs[0] = 1;
            baseConversionPolyCoeffs[1] = 0;
        }
    }
    public boolean isUsingBaseConversionExpression(){
        return baseConversionExpression != null;
    }

    /**
     * Contains functions that enable base unit to convert to units of same dimension that dependent on it.
     */
    public Map<Unit, double[]> getConversionsToDescendents() {
        return conversionsToDescendents;
    }

    /**
     * Map that associates derivative component unit names with the value of their exponential power.
     */
    public Map<String, Double> getComponentUnitsDimension() {
        return componentUnitsDimension;
    }

    /**
     * Map that associates derivative component fundamental units with the value of their exponential power
     */
    public Map<UNIT_TYPE, Double> getFundamentalUnitTypesDimension() {
        return fundamentalUnitTypesDimension;
    }
    public DIMENSION_TYPE getDimensionType(){
        return dimensionType;
    }
    public UNIT_TYPE getUnitType() {
        return unitType;
    }

    ///
    public UnitManager getUnitManagerContext() {
        return unitManagerContext;
    }
    public void setUnitManagerContext(UnitManager unitManager) throws UnitException { //Although this is public, it should ideally only be called when the unit is added or removed from a unit data model that is associated with a particular unit manager.
        this.unitManagerContext = unitManager;

        if(unitManager != null)
            setLocale(unitManager.getLocale());

        //Automatically updateContent this unit's unimplemented properties based on unit manager if present
        setAutomaticBaseUnit();
        setAutomaticUnitSystem();
        setAutomaticCategory();
        setAutomaticAbbreviation();
    }

    public Locale getLocale() {
        return locale;
    }
    public void setLocale(Locale locale) {
        this.locale = locale;
        componentUnitsDimensionSerializer.setLocale(locale);
        fundamentalUnitTypesDimensionSerializer.setLocale(locale);
    }

    public ComponentUnitsDimensionSerializer getComponentUnitsDimensionSerializer() {
        return componentUnitsDimensionSerializer;
    }
    public void setComponentUnitsDimensionSerializer(ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer){
        this.componentUnitsDimensionSerializer = componentUnitsDimensionSerializer;
    }

    public FundamentalUnitTypesDimensionSerializer getFundamentalUnitTypesDimensionSerializer() {
        return fundamentalUnitTypesDimensionSerializer;
    }
    public void setFundamentalUnitTypesDimensionSerializer(FundamentalUnitTypesDimensionSerializer fundamentalUnitTypesDimensionSerializer){
        this.fundamentalUnitTypesDimensionSerializer = fundamentalUnitTypesDimensionSerializer;
    }

    public DimensionComponentDefiner getDimensionComponentDefiner() {
        return dimensionComponentDefiner;
    }
    public void setDimensionComponentDefiner(DimensionComponentDefiner dimensionComponentDefiner){
        this.dimensionComponentDefiner = dimensionComponentDefiner;
    }

    ///
    /**
     * A locale specific string representation of the unit.
     */
    @Override
    public String toString() {
        return String.format(locale,"(name: %s, abbr: %s, category: %s, fundamental dimension: %s, base unit: %s, base conversion poly coeffs: %s)", name, abbreviation, category, fundamentalUnitTypesDimension, baseUnit.name, Arrays.toString(baseConversionPolyCoeffs));
    }
}
