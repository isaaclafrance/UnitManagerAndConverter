package com.isaacapps.unitconverterapp.models.measurables.unit;

import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel.UNIT_TYPE;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsContentDeterminer;
import com.isaacapps.unitconverterapp.processors.formatters.text.GeneralTextFormatter;
import com.isaacapps.unitconverterapp.processors.operators.measurables.units.UnitOperators;
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

import static com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsContentDeterminer.determineGeneralDataModelCategory;
import static com.isaacapps.unitconverterapp.processors.parsers.dimension.componentunits.ComponentUnitsDimensionParser.COMPONENT_NAME_REGEX;

public class Unit {
    ///
    public static final String BASE_VALUE_EXPRESSION_ARGUMENT = "{x}";
    public static final String UNKNOWN_UNIT_NAME = "unknown_unit", UNKNOWN_UNIT_SYSTEM = "unknown_system", UNKNOWN_UNIT_CATEGORY = "unknown_category", UNKNOWN_UNIT_ABBREVIATION = "unit", UNIT_SYSTEM_DELIMITER = " and ";

    protected UnitManager unitManagerContext;

    protected Locale locale;
    private DimensionComponentDefiner dimensionComponentDefiner;
    private ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer;
    private FundamentalUnitTypesDimensionSerializer fundamentalUnitTypesDimensionSerializer;

    private boolean isCoreUnit; //Ensures the unit can not be deleted, or grossly modified due to its importance
    private boolean isBaseUnit;
    private UNIT_TYPE type;
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
        this(name, abbreviation, Locale.getDefault(), new ComponentUnitsDimensionSerializer(Locale.getDefault(), new ComponentUnitsDimensionItemSerializer(Locale.getDefault(), new GeneralTextFormatter(Locale.getDefault())))
                , new FundamentalUnitTypesDimensionSerializer(Locale.getDefault(), new FundamentalUnitTypesDimensionItemSerializer(Locale.getDefault(), new GeneralTextFormatter(Locale.getDefault())))
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

        type = UNIT_TYPE.UNKNOWN;

        this.name = name;
        unitNameAliases = new HashSet<>();
        setUnitSystem(UNKNOWN_UNIT_SYSTEM, false);
        setCategory(UNKNOWN_UNIT_CATEGORY, false);
        setDescription("");
        if (!abbreviation.isEmpty()) {
            setAbbreviation(abbreviation);
        } else {
            setAutomaticAbbreviation();
        }

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
                    unitSystemCandidateBuilder.append(baseUnit.getUnitSystem());
                }
            }

            setUnitSystem(unitSystemCandidateBuilder.toString(), !unitSystemCandidateBuilder.toString().equalsIgnoreCase(unitSystem));
        }
    }

    public boolean setAutomaticUnitTypeNFundamentalTypesDimension() {
        if (getUnitManagerContext() != null) {
            type = getUnitManagerContext().getUnitsDataModel().getUnitsContentDeterminer().determineUnitType(this);
            if(type != UNIT_TYPE.DERIVED_MULTI_UNIT && type != UNIT_TYPE.DERIVED_SINGLE_UNIT && type != UNIT_TYPE.UNKNOWN){
                fundamentalUnitTypesDimension = new HashMap<>();
                fundamentalUnitTypesDimension.put(type, 1.0);
            }
            else {
                fundamentalUnitTypesDimension = getUnitManagerContext().getFundamentalUnitsDataModel()
                        .transformComponentUnitsDimensionToFundamentalUnitsDimension(this.componentUnitsDimension, getUnitSystemsCollection(), !isCoreUnit);
            }
        } else {
            type = UNIT_TYPE.UNKNOWN;
            fundamentalUnitTypesDimension = new HashMap<>();
            fundamentalUnitTypesDimension.put(UNIT_TYPE.UNKNOWN, 1.0);
        }
        return type != UNIT_TYPE.UNKNOWN && !fundamentalUnitTypesDimension.containsKey(UNIT_TYPE.UNKNOWN);
    }

    private void setAutomaticAbbreviation() {
        //Create a dimensional string with each of the component units abbreviated. For example, format would be something like 'in^2/sec^5'
        if (getUnitManagerContext() != null) {
            Map<String, Double> abbreviatedComponentDimension = new HashMap<>();
            for (Map.Entry<String, Double> componentUnitEntry : componentUnitsDimension.entrySet())
                abbreviatedComponentDimension.put(getUnitManagerContext().getUnitsDataModel().getUnitsContentMainRetriever()
                        .getAbbreviation(componentUnitEntry.getKey()), componentUnitEntry.getValue());

            try {
                setAbbreviation(componentUnitsDimensionSerializer.serialize(abbreviatedComponentDimension)
                        .replaceAll("[()]", ""));
            } catch (SerializingException e) {
                e.printStackTrace();
            }
        }
        else{
            setAbbreviation(UNKNOWN_UNIT_ABBREVIATION);
        }
    }

    ///

    /**
     * Automatically finds and sets base unit from own reference of Unit Manager context
     *
     * @return Whether any new base unit was set.
     */
    public boolean setAutomaticBaseUnit() throws UnitException {
        //Update the unit type and fundamental type dimension so that they are both current since, they will help determine which base unit is selected
        if (getUnitManagerContext() != null && setAutomaticUnitTypeNFundamentalTypesDimension()) {

            Unit retrievedBaseUnit = getUnitManagerContext().getUnitsDataModel().getUnitsContentDeterminer().determineBaseUnit(this);

            boolean alreadyHasRetrievedBaseUnit = this.getBaseUnit().equals(retrievedBaseUnit);

            if (!alreadyHasRetrievedBaseUnit
                    && determineGeneralDataModelCategory(retrievedBaseUnit) != UnitsContentDeterminer.DATA_MODEL_CATEGORY.UNKNOWN) {

                setBaseUnit(retrievedBaseUnit, true);
                return true;
            }
            return alreadyHasRetrievedBaseUnit;
        }
        return false;
    }


    public void setAsBaseUnit() throws UnitException {
        setBaseUnit(this);
    }
    public void setBaseUnit(Unit baseUnit, double[] baseConversionPolyCoeffs) throws UnitException {
        if (!isCoreUnit && baseUnit != null) {
            this.baseConversionPolyCoeffs = baseConversionPolyCoeffs;
            this.baseConversionExpression = null;
            setBaseUnit(baseUnit, false);
        }
    }
    public void setBaseUnit(Unit baseUnit, String baseConversionExpression) throws UnitException {
        if (!isCoreUnit && baseUnit != null) {
            this.baseConversionPolyCoeffs = new double[]{1.0, 0.0};
            this.baseConversionExpression = baseConversionExpression;
            setBaseUnit(baseUnit, false);
        }
    }

    private void setBaseUnit(Unit specifiedBaseUnit, boolean attemptToSetAutomaticBaseConversionPolyCoeffs) throws UnitException {
        if((!specifiedBaseUnit.isBaseUnit || attemptToSetAutomaticBaseConversionPolyCoeffs) && isUsingBaseConversionExpression())
            throw new UnitException(String.format("The base unit %s that was originally specified for %s was not an actual base unit, therefore there is a need to a perform a cascaded recalculation of the specified base conversion expression." +
                    " However, currently there is no capability to perform such calculation for units with arbitrary base conversion expressions", specifiedBaseUnit.getName(), this.getName())
                    , "Either directly specify a base unit that is an actual base unit or if possible use a suitable conversion that defined in terms of linear coefficients rather than an abitrary expression.");


        //If initially assigned base unit is not a base unit, then iteratively retrieve next base unit level and successively cascades base conversion.
        Unit cascadedBaseUnit = specifiedBaseUnit;
        while(!cascadedBaseUnit.isBaseUnit && !cascadedBaseUnit.name.equalsIgnoreCase(this.name)) {
            this.baseConversionPolyCoeffs[0] = this.baseConversionPolyCoeffs[0] * cascadedBaseUnit.baseConversionPolyCoeffs[0];
            this.baseConversionPolyCoeffs[1] += this.baseConversionPolyCoeffs[1] * cascadedBaseUnit.baseConversionPolyCoeffs[0];
            cascadedBaseUnit = cascadedBaseUnit.baseUnit;
        }

        boolean baseUnitCanBeChanged = this.getUnitManagerContext() == cascadedBaseUnit.getUnitManagerContext()
                && (UnitOperators.equalsDimension(this, cascadedBaseUnit) || this.getUnitManagerContext() == null)
                && !isCoreUnit;

        boolean baseUnitStatusHasChanged = false;
        if (baseUnitCanBeChanged) {
            this.baseUnit = cascadedBaseUnit;

            if (this.equals(this.baseUnit)) {
                baseUnitStatusHasChanged = !this.isBaseUnit;
                this.isBaseUnit = true;
            } else {
                baseUnitStatusHasChanged = this.isBaseUnit;
                this.isBaseUnit = false;
                downPropogateBaseUnitModification(cascadedBaseUnit);
            }
        }

        //
        if (baseUnitCanBeChanged && (attemptToSetAutomaticBaseConversionPolyCoeffs || !specifiedBaseUnit.isBaseUnit)) {
            if (this.isBaseUnit) {
                baseConversionPolyCoeffs = new double[]{1.0, 0.0};
            } else {
                if(baseConversionPolyCoeffs[0] == 1.0 && baseConversionPolyCoeffs[0] == 0.0 && dimensionComponentDefiner != null && dimensionComponentDefiner.hasComplexDimensions(name))
                    adjustBaseConversionPolyCoeffsByOverallComponentFactor();
            }
        }

        //Now that an actual base unit has been obtained by this point, updateContent this base unit with this unit as its descendant
        this.baseUnit.addDescendentConversion(this, new double[]{1.0 / baseConversionPolyCoeffs[0]
                , -baseConversionPolyCoeffs[1] / baseConversionPolyCoeffs[0]});

        //Modify this unit's properties that may indirectly depend on the on the base unit
        setAutomaticUnitSystem();
        setAutomaticUnitTypeNFundamentalTypesDimension();
        setAutomaticCategory();

        //Get unit manager to tell the appropriate data model to notify other units without base units of this unit's base unit status.
        if (baseUnitStatusHasChanged && getUnitManagerContext() != null)
            getUnitManagerContext().getUnitsDataModel().getUnitsContentModifier().incorporateBaseUnit(this, baseUnitStatusHasChanged);

    }

    /**
     * The primary unit and its specified base are decomposed to their component units.
     * The overall factor is calculated from these individual components and is used to determine the base conversion factor of the primary unit.
     */
    private void adjustBaseConversionPolyCoeffsByOverallComponentFactor() {
        boolean shouldCalculateComponentFactorForThisUnit = !(this.componentUnitsDimension.size() == 1
                && this.componentUnitsDimension.containsKey(this.name));
        boolean shouldCalculateComponentFactorForBaseUnit = !(baseUnit.componentUnitsDimension.size() == 1
                && baseUnit.componentUnitsDimension.containsKey(baseUnit.name));

        double baseUnitOverallComponentFactor = (shouldCalculateComponentFactorForThisUnit && shouldCalculateComponentFactorForBaseUnit)
                ? baseUnit.calculateOverallFactor() : 1.0;

        double currentUnitOverallComponentFactor = shouldCalculateComponentFactorForThisUnit ? this.calculateOverallFactor() : 1.0;

        double scaledOverallComponentFactor = currentUnitOverallComponentFactor / baseUnitOverallComponentFactor;

        this.baseConversionPolyCoeffs[0] = scaledOverallComponentFactor * this.baseConversionPolyCoeffs[0];
    }
    private double calculateOverallFactor() {
        double overallFactor = 1.0;

        if (!this.fundamentalUnitTypesDimension.containsKey(UNIT_TYPE.UNKNOWN) && getUnitManagerContext() != null) {
            Unit componentUnit;
            double componentFactor = 1.0;
            for (Map.Entry<String, Double> entry : componentUnitsDimension.entrySet()) {
                componentUnit = getUnitManagerContext().getUnitsDataModel().getUnitsContentMainRetriever().getUnit(entry.getKey(), false);
                if (componentUnit.getType() == UNIT_TYPE.DERIVED_MULTI_UNIT && componentUnit.getComponentUnitsDimension().keySet().size() > 1) {
                    componentFactor = componentUnit.calculateOverallFactor();
                } else if (!componentUnit.isBaseUnit) {
                    componentFactor = this != componentUnit ? componentUnit.getBaseUnit()
                            .calculateOverallFactor()
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
    public void downPropogateBaseUnitModification(Unit newBaseUnit) throws UnitException {
        for (Unit depUnit : conversionsToDescendents.keySet()) {
            depUnit.setBaseUnit(this.baseUnit);
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
            if (this.category.contains("^") && !targetUnit.category.contains("^")) {
                setCategory(targetUnit.category);
            }
        } else {
            baseUnit.addDescendentConversion(targetUnit, new double[]{1.0 / baseUnit.getBaseConversionPolyCoeffs()[0] * polynomialCoeffs[0],
                    1.0 / getBaseConversionPolyCoeffs()[1] * polynomialCoeffs[0] + polynomialCoeffs[1]});
        }

        return true;
    }

    public void clearConversionsOfDescendents() {
        conversionsToDescendents.clear();
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
    public String getName() { //Ideally one should only be able to get name. It should only be set during unit initialization
        return name;
    }

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
        this.unitSystem = unitSystem.toLowerCase();

        //Re-add this unit to the unit classifier with its new unit system classification such that any other
        //pre-exisiting classifications for the same units name are removed.
        if (getUnitManagerContext() != null && notifyUnitsClassifier)
            getUnitManagerContext().getUnitsClassifierDataModel().addToHierarchy(this, true);
    }

    public String getAbbreviation() {
        return abbreviation;
    }
    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;

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
        this.category = unitCategory.toLowerCase();

        if (getUnitManagerContext() != null && notifyUnitsClassfier)
            getUnitManagerContext().getUnitsClassifierDataModel().addToHierarchy(this, true);
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

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
    public void setBaseUnit(Unit baseUnit) throws UnitException {
        if (!isCoreUnit)
            setBaseUnit(baseUnit, true);
    }

    public double[] getBaseConversionPolyCoeffs() {
        return this.baseConversionPolyCoeffs;
    }
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

    public Map<Unit, double[]> getConversionsToDescendents() {
        return conversionsToDescendents;
    }
    public Map<String, Double> getComponentUnitsDimension() {
        return componentUnitsDimension;
    }
    public Map<UNIT_TYPE, Double> getFundamentalUnitTypesDimension() {
        return fundamentalUnitTypesDimension;
    }
    public UNIT_TYPE getType() {
        return type;
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
        return String.format(locale,"(name: %s, abbr: %s, category: %s, base unit: %s, base conversion poly coeffs: %s)", name, abbreviation, category, baseUnit.name, Arrays.toString(baseConversionPolyCoeffs));
    }
}
