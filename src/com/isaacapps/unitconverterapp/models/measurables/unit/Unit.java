package com.isaacapps.unitconverterapp.models.measurables.unit;

import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel.UNIT_TYPE;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.ContentDeterminer;
import com.isaacapps.unitconverterapp.processors.formatters.text.GeneralTextFormatter;
import com.isaacapps.unitconverterapp.processors.operators.measurables.units.UnitOperators;
import com.isaacapps.unitconverterapp.processors.serializers.SerializingException;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.componentnunit.ComponentUnitsDimensionItemSerializer;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.componentnunit.ComponentUnitsDimensionSerializer;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.fundamentalunit.FundamentalUnitTypesDimensionItemSerializer;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.fundamentalunit.FundamentalUnitTypesDimensionSerializer;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.ContentDeterminer.determineGeneralDataModelCategory;

public class Unit {
    ///
    public static final String BASE_VALUE_EXPRESSION_ARGUMENT = "{x}";
    public static final String UNKNOWN_UNIT_NAME = "unknown_unit", UNKNOWN_UNIT_SYSTEM = "unknown_system", UNKNOWN_UNIT_CATEGORY = "unknown_category", UNKNOWN_UNIT_ABBREVIATION = "unit", UNIT_SYSTEM_DELIMITER = " and ";

    protected UnitManager unitManagerContext;

    private Locale locale;
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
    public Unit() {
        this(UNKNOWN_UNIT_NAME, UNKNOWN_UNIT_ABBREVIATION);
    }

    public Unit(String name, String abbreviation){
        this(name, abbreviation, Locale.getDefault(), new ComponentUnitsDimensionSerializer(Locale.getDefault(), new ComponentUnitsDimensionItemSerializer(Locale.getDefault(), new GeneralTextFormatter(Locale.getDefault())))
                , new FundamentalUnitTypesDimensionSerializer(Locale.getDefault(), new FundamentalUnitTypesDimensionItemSerializer(Locale.getDefault(), new GeneralTextFormatter(Locale.getDefault()))));
    }

    public Unit(String name, String abbreviation, Locale locale, ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer, FundamentalUnitTypesDimensionSerializer fundamentalUnitTypesDimensionSerializer) {
        type = UNIT_TYPE.UNKNOWN;

        this.name = name.toLowerCase();
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
        fundamentalUnitTypesDimension = new HashMap<>();
        fundamentalUnitTypesDimension.put(UNIT_TYPE.UNKNOWN, 1.0);

        isCoreUnit = false;
        isBaseUnit = true;
        baseUnit = this;
        baseConversionPolyCoeffs = new double[]{1.0, 0.0};

        this.locale = locale;
        this.componentUnitsDimensionSerializer = componentUnitsDimensionSerializer;
        this.fundamentalUnitTypesDimensionSerializer = fundamentalUnitTypesDimensionSerializer;
    }

    public Unit(String name, Map<String, Double> componentUnitsDimension, boolean isBaseUnit) {
        this(name, "");
        this.componentUnitsDimension = componentUnitsDimension;
        this.isBaseUnit = isBaseUnit;
        baseUnit = isBaseUnit ? this : new Unit();
        baseConversionPolyCoeffs = new double[]{isBaseUnit ? 1.0 : 0.0, 0.0};
    }

    public Unit(Map<String, Double> componentUnitsDimension, boolean isBaseUnit) {
        this(UNKNOWN_UNIT_NAME, componentUnitsDimension, isBaseUnit);
        try {
            this.name = componentUnitsDimensionSerializer.serialize(componentUnitsDimension);
        }
        catch(Exception e){

        }
    }

    public Unit(String name, Set<String> unitNameAliases, String category, String description
            , String unitSystem, String abbreviation, Map<String, Double> componentUnitsDimension
            , Unit baseUnit, double[] baseConversionPolyCoeffs, Locale locale, ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer, FundamentalUnitTypesDimensionSerializer fundamentalUnitTypesDimensionSerializer) {
        this(name, abbreviation, locale, componentUnitsDimensionSerializer, fundamentalUnitTypesDimensionSerializer);
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
                componentUnit = getUnitManagerContext().getUnitsDataModel().getContentMainRetriever().getUnit(componentUnitName, false);
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
                    .equalsIgnoreCase(getUnitManagerContext().getUnitsDataModel().getContentMainRetriever().getUnknownUnit().getUnitSystem())) {
                if (baseUnit != null) {
                    unitSystemCandidateBuilder.append(baseUnit.getUnitSystem());
                }
            }

            setUnitSystem(unitSystemCandidateBuilder.toString(), !unitSystemCandidateBuilder.toString().equalsIgnoreCase(unitSystem));
        }
    }

    public boolean setAutomaticUnitTypeNFundmtTypesDim() {
        if (getUnitManagerContext() != null) {
            type = getUnitManagerContext().getUnitsDataModel().getContentDeterminer().determineUnitType(this);
            fundamentalUnitTypesDimension = getUnitManagerContext().getFundamentalUnitsDataModel()
                    .transformComponentUnitsDimensionToFundamentalUnitsDimension(this.componentUnitsDimension);
        } else {
            type = UNIT_TYPE.UNKNOWN;
            fundamentalUnitTypesDimension = new HashMap<>();
            fundamentalUnitTypesDimension.put(UNIT_TYPE.UNKNOWN, 1.0);
        }
        return type != UNIT_TYPE.UNKNOWN && !fundamentalUnitTypesDimension.containsKey(UNIT_TYPE.UNKNOWN);
    }

    private void setAutomaticAbbreviation() {
        Map<String, Double> abbreviatedComponentDimension = new HashMap<>();
        //Create a dimensional string with each of the component units abbreviated. For example, format would be something like 'in^2/sec^5'
        if (getUnitManagerContext() != null) {
            for (Map.Entry<String, Double> componentUnitEntry : componentUnitsDimension.entrySet())
                abbreviatedComponentDimension.put(getUnitManagerContext().getUnitsDataModel().getContentMainRetriever()
                        .getAbbreviation(componentUnitEntry.getKey()), componentUnitEntry.getValue());
        }
        try {
            setAbbreviation(componentUnitsDimensionSerializer.serialize(abbreviatedComponentDimension)
                    .replaceAll("[()]", ""));
        }
        catch(SerializingException e){ }
    }

    ///

    /**
     * Automatically finds and sets base unit from Unit Manager context
     *
     * @return Whether any new base unit was set.
     */
    public boolean setAutomaticBaseUnit() {
        //Update the unit type and fundamental type dimension so that they are both current since, they will help determine which base unit is selected
        if (getUnitManagerContext() != null && setAutomaticUnitTypeNFundmtTypesDim()) {

            Unit retrievedBaseUnit = getUnitManagerContext().getUnitsDataModel().getContentQuerier().getBaseUnitMatch(this);

            boolean alreadyHasRetrievedBaseUnit = this.getBaseUnit().equals(retrievedBaseUnit);

            if (!alreadyHasRetrievedBaseUnit
                    && determineGeneralDataModelCategory(retrievedBaseUnit) != ContentDeterminer.DATA_MODEL_CATEGORY.UNKNOWN) {

                setBaseUnit(retrievedBaseUnit, true);
                return true;
            }
            return alreadyHasRetrievedBaseUnit;
        }
        return false;
    }

    public void setBaseUnit(Unit baseUnit, double[] baseConversionPolyCoeffs) {
        if (!isCoreUnit) {
            this.baseConversionPolyCoeffs = baseConversionPolyCoeffs;
            setBaseUnit(baseUnit, false);
        }
    }

    private void setBaseUnit(Unit specifiedBaseUnit, boolean attemptToSetAutomaticBaseConversionPolyCoeffs) {
        boolean specifiedBaseUnitIsNotActuallyABaseUnit = !specifiedBaseUnit.isBaseUnit;
        boolean baseUnitCanBeChanged = this.getUnitManagerContext() == specifiedBaseUnit.getUnitManagerContext()
                && (UnitOperators.equalsDimension(this, specifiedBaseUnit) || this.getUnitManagerContext() == null)
                && !isCoreUnit;
        boolean baseUnitStatusHasChanged = false;

        //Assign base unit and cascade assignment by one level if necessary
        if (baseUnitCanBeChanged) {
            //Cascade by one level to base unit of base unit except in cases where we explicitly want to set the base unit as the unit itself.
            this.baseUnit = (specifiedBaseUnitIsNotActuallyABaseUnit && !this.equals(specifiedBaseUnit))
                    ? specifiedBaseUnit.baseUnit : specifiedBaseUnit;

            if (this.equals(this.baseUnit)) {
                baseUnitStatusHasChanged = !this.isBaseUnit;
                this.isBaseUnit = true;
            } else {
                baseUnitStatusHasChanged = this.isBaseUnit;
                this.isBaseUnit = false;
                downPropogateBaseUnitModification();
            }
        }

        //Assign base conversion and cascade conversion when appropriate
        if (baseUnitCanBeChanged && (attemptToSetAutomaticBaseConversionPolyCoeffs || specifiedBaseUnitIsNotActuallyABaseUnit)) {
            if (this.isBaseUnit) {
                baseConversionPolyCoeffs = new double[]{1.0, 0.0};
            } else {
                setAutomaticBaseConversionPolyCoeffs(specifiedBaseUnit, specifiedBaseUnitIsNotActuallyABaseUnit);
            }
        }

        /* This 'setBaseUnit' methods can only deal with one level of base unit cascading. Consequently, if newly assigned base unit is still not a base unit after one level of cascading,
         * then recursively call this method with newly assigned base unit as the specified base unit until all subsequent levels lead to an actual base unit. */
        if (!this.baseUnit.isBaseUnit)
            setBaseUnit(this.baseUnit, attemptToSetAutomaticBaseConversionPolyCoeffs);

        //Now that an actual base unit has been obtained by this point, update this base unit with this unit as its descendant
        this.baseUnit.addDescendentConversion(this, new double[]{1.0 / baseConversionPolyCoeffs[0]
                , -baseConversionPolyCoeffs[1] / baseConversionPolyCoeffs[0]});

        //Modify this unit's properties that may indirectly depend on the on the base unit
        setAutomaticUnitSystem();
        setAutomaticUnitTypeNFundmtTypesDim();
        setAutomaticCategory();

        //Get unit manager to tell the appropriate data model to notify other units without base units of this unit's base unit status.
        if (baseUnitStatusHasChanged && this.isBaseUnit && getUnitManagerContext() != null)
            getUnitManagerContext().getUnitsDataModel().getContentModifier().incorporateBaseUnit(this);

    }

    /**
     * The primary unit and its specified base are decomposed to their component units.
     * Then depending on whether primary unit is already a base unit and whether the specified base unit is actually a base unit
     * , base conversion of the decomposed component units are optionally calculated and recursively cascaded.
     */
    private void setAutomaticBaseConversionPolyCoeffs(Unit specifiedBaseUnit, boolean specifiedBaseUnitIsNotActuallyABaseUnit) {
        boolean shouldCalculateComponentFactorForThisUnit = !(this.componentUnitsDimension.size() == 1
                && this.componentUnitsDimension.containsKey(this.name));
        boolean shouldCalculateComponentFactorForBaseUnit = !(specifiedBaseUnit.componentUnitsDimension.size() == 1
                && specifiedBaseUnit.componentUnitsDimension.containsKey(specifiedBaseUnit.name));

        double baseUnitOverallFactor = (shouldCalculateComponentFactorForThisUnit
                && shouldCalculateComponentFactorForBaseUnit || specifiedBaseUnitIsNotActuallyABaseUnit)
                ? specifiedBaseUnit.calculateOverallFactor(!specifiedBaseUnitIsNotActuallyABaseUnit
                , !this.isBaseUnit && shouldCalculateComponentFactorForBaseUnit)
                : 1.0;


        this.baseConversionPolyCoeffs = new double[]{this.calculateOverallFactor(!specifiedBaseUnitIsNotActuallyABaseUnit
                , !this.isBaseUnit && (shouldCalculateComponentFactorForThisUnit || specifiedBaseUnitIsNotActuallyABaseUnit))
                * (specifiedBaseUnitIsNotActuallyABaseUnit ? baseUnitOverallFactor : 1 / baseUnitOverallFactor), 0.0};
    }

    private double calculateOverallFactor(boolean specifiedBaseUnitIsActuallyABaseUnit, boolean shouldCalculateComponentFactor) {
        double overallFactor = 1.0;

        if (shouldCalculateComponentFactor && !this.fundamentalUnitTypesDimension.containsKey(UNIT_TYPE.UNKNOWN) && getUnitManagerContext() != null) {
            Unit componentUnit;
            double componentFactor = 1.0;
            for (Map.Entry<String, Double> entry : componentUnitsDimension.entrySet()) {
                componentUnit = getUnitManagerContext().getUnitsDataModel().getContentMainRetriever().getUnit(entry.getKey(), false);
                if (componentUnit.getType() == UNIT_TYPE.DERIVED_MULTI_UNIT && componentUnit.getComponentUnitsDimension().keySet().size() > 1) {
                    componentFactor = componentUnit.calculateOverallFactor(specifiedBaseUnitIsActuallyABaseUnit, true);
                } else if (!componentUnit.isBaseUnit) {
                    componentFactor = this != componentUnit ? componentUnit.getBaseUnit()
                            .calculateOverallFactor(specifiedBaseUnitIsActuallyABaseUnit, true)
                            * componentUnit.getBaseConversionPolyCoeffs()[0] : 1.0; //recursively calculate base conversion until absolute base is reached.
                }
                overallFactor *= Math.pow(componentFactor, entry.getValue());
            }
        }

        return overallFactor * (specifiedBaseUnitIsActuallyABaseUnit ? 1.0 : baseConversionPolyCoeffs[0]);
    }

    /**
     * Called when this unit switches from being a base unit to not.
     * Changes base unit of the previously dependent units to the new base unit.
     */
    private void downPropogateBaseUnitModification() {
        for (Unit depUnit : conversionsToDescendents.keySet()) {
            depUnit.setBaseUnit(this.baseUnit);
        }
        conversionsToDescendents.clear();
    }

    ///
    private void addDescendentConversion(Unit targetUnit, double[] polynomialCoeffs) {
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
    }

    public void clearConversionsOfDescendents() {
        conversionsToDescendents.clear();
    }

    ///
    public void addComponentUnit(String componentUnitName, double exponent, boolean updateUnitName) {
        if (!isCoreUnit) {
            String potentiallySingularizedComponentUnitName = getUnitManagerContext() != null
                    ? getUnitManagerContext().getUnitsDataModel().getContentDeterminer().determineSingularOfUnitName(componentUnitName)
                    : componentUnitName;

            componentUnitsDimension.put(potentiallySingularizedComponentUnitName, exponent);

            if (updateUnitName) {
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

    public void clearComponentUnitsDimension(boolean updateName) {
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
        unitNameAliases.add(alias.toLowerCase());
        return getUnitManagerContext() != null && getUnitManagerContext()
                .getUnitsDataModel().getContentModifier().mapUnidirectionalAliasToUnitName(name, alias);
    }

    public String getUnitSystem() {
        return unitSystem;
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
            getUnitManagerContext().getUnitsDataModel().getContentModifier()
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
     * @param notifyUnitsClassfier allows for optional messaging of units classifier to update its state
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
    public void setBaseUnit(Unit baseUnit) {
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
        if (baseConversionExpression.contains(BASE_VALUE_EXPRESSION_ARGUMENT)) {
            this.baseConversionExpression = baseConversionExpression;

            //Since base conversion expression can not be accurately represented by linear equation for all cases
            //, simply set coeffiecnt of the linear conversion data structrue to invariant values.
            baseConversionPolyCoeffs[0] = 1;
            baseConversionPolyCoeffs[1] = 0;
        }
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
    public void setUnitManagerContext(UnitManager unitManager) { //Although this is public, it should ideally only be called when the unit is added or removed from a unit data model that is associated with a particular unit manager.
        this.unitManagerContext = unitManager;

        //Automatically update this unit's unimplemented properties based on unit manager if present
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

    public FundamentalUnitTypesDimensionSerializer getFundamentalUnitTypesDimensionSerializer() {
        return fundamentalUnitTypesDimensionSerializer;
    }

    ///
    /**
     * A locale specific string representation of the unit.
     */
    @Override
    public String toString() {
        return String.format(locale,"(%s, %s, %s)", name, abbreviation, category);
    }
}
