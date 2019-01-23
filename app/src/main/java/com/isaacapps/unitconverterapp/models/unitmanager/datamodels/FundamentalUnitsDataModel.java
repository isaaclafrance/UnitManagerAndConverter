package com.isaacapps.unitconverterapp.models.unitmanager.datamodels;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.measurables.unit.UnitException;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.IDualKeyNCategoryRepository;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsDataModel;
import com.isaacapps.unitconverterapp.processors.operators.dimensions.DimensionOperators;
import static com.isaacapps.unitconverterapp.utilities.RegExUtility.SIGNED_DOUBLE_VALUE_REGEX_PATTERN;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Associates a unit full name with a particular unit type within a unit system
 *
 * @author Isaac Lafrance
 */
public class FundamentalUnitsDataModel extends BaseDataModel<String, FundamentalUnitsDataModel.UNIT_TYPE, String> {
    public enum UNIT_TYPE {
        MASS, LENGTH, TIME, AMT_OF_SUBSTANCE, NUMBER, ANGLE, TEMPERATURE, CHARGE, LUMINOUS_INTENSITY, CURRENCY
        , UNKNOWN, COMPLEX
    }

    public static final String DEFAULT_UNIT_SYSTEM = "si";

    private UnitsDataModel unitsDataModel;

    ///
    public FundamentalUnitsDataModel() {
    }
    public FundamentalUnitsDataModel(IDualKeyNCategoryRepository<String, UNIT_TYPE, String> repositoryWithDualKeyNCategory){
        super(repositoryWithDualKeyNCategory);
    }

    ///
    public void addFundamentalUnit(String unitSystem, String unitName, UNIT_TYPE unitType) {
        repositoryWithDualKeyNCategory.addItem(unitSystem.toLowerCase(), unitName.toLowerCase(), unitName.toLowerCase(), unitType);
        //Ideally, since the fundamental units data model is so integral as basis to the other data models, everything should reinitalize event for a minor change.
        if(unitsDataModel != null) {
            unitsDataModel.getUnitsContentModifier().updateFundamentalUnitsDimensionOfKnownUnits(false);
            try {
                unitsDataModel.getUnitsContentModifier().updateAssociationsOfUnknownUnits();
            } catch (UnitException e) {
                e.printStackTrace();
            }
        }
    }

    public void removeAllFundamentalUnits() {
        repositoryWithDualKeyNCategory.removeAllItems();
    }

    ///
    public UNIT_TYPE getUnitTypeByUnitSystemNUnitName(String unitSystem, String unitName) {
        if(Character.isDigit(unitName.charAt(0)) && SIGNED_DOUBLE_VALUE_REGEX_PATTERN.matcher(unitName).matches()){
            return UNIT_TYPE.NUMBER;
        }
        return repositoryWithDualKeyNCategory.getItem(unitSystem.toLowerCase(), unitName.toLowerCase());
    }
    /**
     * Tries to find the first encountered unit type based on the provided unit name and unit system collection otherwise a unit type of unknown is returned
     */
    private UNIT_TYPE getFirstSuitableUnitTypeWithinUnitSystemCollection(String unitName, Collection<String> unitSystems){
        UNIT_TYPE unitType;
        for(String unitSystem:unitSystems){
            unitType = getUnitTypeByUnitSystemNUnitName(unitSystem, unitName);
            if(unitType != null)
                return unitType;
        }
        return UNIT_TYPE.UNKNOWN;
    }
    public String getUnitNameByUnitSystemNUnitType(String unitSystem, UNIT_TYPE unitType) {
        return repositoryWithDualKeyNCategory.getKey1(unitSystem.toLowerCase(), unitType);
    }

    public Collection<String> getUnitNamesByUnitSystem(String unitSystem){
        return repositoryWithDualKeyNCategory.getKey2sByCategory(unitSystem.toLowerCase());
    }

    ///
    public boolean containsUnitSystem(String unitSystem) {
        return repositoryWithDualKeyNCategory.containsCategory(unitSystem.toLowerCase());
    }
    public boolean containsUnitNameInUnitSystem(String unitSystem, String unitName) {
        return repositoryWithDualKeyNCategory.containsKeyInCategory(unitSystem.toLowerCase(), unitName.toLowerCase());
    }
    public boolean containsUnitName(String unitName) {
        return repositoryWithDualKeyNCategory.containsKey(unitName.toLowerCase());
    }

    ///

    /**
     * Analyzes each component unit whether derived or not with reference to the unitManager context
     * and sums up the recursively obtained total occurrences of the fundamental units.
     * Makes sure to multiply those totals by the exponent of the component unit.
     * If unit manager context has no object instances of one of the component units
     * , then the fundamental unit types repository is searched for the first encountered match within the provided fail-safe unit systems
     */
    public Map<UNIT_TYPE, Double> transformComponentUnitsDimensionToFundamentalUnitsDimension(Map<String, Double> componentUnitsDimension, Collection<String> failSafeUnitSystems, boolean createMissingComplexUnits) {
        Map<UNIT_TYPE, Double> fundamentalUnitMap = new EnumMap<>(UNIT_TYPE.class);

        Unit componentUnit;
        Double componentUnitExponent;
        for (String componentUnitName : componentUnitsDimension.keySet()) {
            componentUnitExponent = componentUnitsDimension.get(componentUnitName);
            componentUnit = unitsDataModel.getUnitsContentMainRetriever().getUnit(componentUnitName, createMissingComplexUnits);

            ///
            if(componentUnit == null || componentUnit.getUnitType() == UNIT_TYPE.UNKNOWN){
                DimensionOperators.alterExponentOfDimensionItem(fundamentalUnitMap, getFirstSuitableUnitTypeWithinUnitSystemCollection(componentUnitName, failSafeUnitSystems), componentUnitExponent);
                continue;
            }

            ///
            if (componentUnit.getUnitType() == UNIT_TYPE.COMPLEX) {
                Map<String, Double> selectedInnerComponentUnitsMap = ((componentUnit.getDimensionType() == DimensionOperators.DIMENSION_TYPE.SIMPLE) ? componentUnit.getBaseUnit() : componentUnit).getComponentUnitsDimension();

                if(selectedInnerComponentUnitsMap.containsKey(componentUnitName)) {  //Prevent infinite recursion
                    DimensionOperators.alterExponentOfDimensionItem(fundamentalUnitMap, UNIT_TYPE.UNKNOWN, componentUnitExponent);
                    continue;
                }

                Map<UNIT_TYPE, Double> recursedFundUnitMap = transformComponentUnitsDimensionToFundamentalUnitsDimension(selectedInnerComponentUnitsMap, failSafeUnitSystems, createMissingComplexUnits);
                for (UNIT_TYPE unitType : recursedFundUnitMap.keySet())
                    DimensionOperators.alterExponentOfDimensionItem(fundamentalUnitMap, unitType, componentUnitExponent * recursedFundUnitMap.get(unitType));

            } else {
                if(componentUnit.getBaseUnit().getUnitType() == UNIT_TYPE.NUMBER){
                    // A number raised to whatever power is still just a number, there no need ot increase the unit type exponent...
                    if(!fundamentalUnitMap.containsKey(UNIT_TYPE.NUMBER)) {
                        DimensionOperators.alterExponentOfDimensionItem(fundamentalUnitMap, componentUnit.getBaseUnit().getUnitType(), 1.0);
                    }
                }
                else {
                    DimensionOperators.alterExponentOfDimensionItem(fundamentalUnitMap, componentUnit.getBaseUnit().getUnitType(), componentUnitExponent);
                }
            }
        }

        return fundamentalUnitMap;
    }

    public Map<String, Double> transformFundamentalUnitsDimensionToComponentUnitDimension(Map<UNIT_TYPE, Double> fundamentalUnitTypesDimension
            , String unitSystem) {
        Map<String, Double> componentUnitsDimension = new HashMap<>();

        for (Entry<UNIT_TYPE, Double> entry : fundamentalUnitTypesDimension.entrySet())
            componentUnitsDimension.put(getUnitNameByUnitSystemNUnitType(unitSystem, entry.getKey()), entry.getValue());

        return componentUnitsDimension;
    }

    ///

    /**
     * Returns the appropriate derived type when there are multiple component units or the fundamental unit type when
     * there is only one component unit. Even if the unit is determined to be derived that does not necessarily mean that it is entirely 'known'
     * within the context of the units data model since its base unit may be unknown or any one of its component units may also be unknown.
     */
    public FundamentalUnitsDataModel.UNIT_TYPE determineUnitType(Unit unit) {
        DimensionOperators.DIMENSION_TYPE unitDimensionType = unit.getDimensionType() != DimensionOperators.DIMENSION_TYPE.UNKNOWN ? unit.getDimensionType(): DimensionOperators.determineDimensionType(unit.getComponentUnitsDimension());

        if (unitDimensionType != DimensionOperators.DIMENSION_TYPE.SIMPLE) {
            return UNIT_TYPE.COMPLEX;
        }
        else if (unit.getBaseUnit() != null && !unit.getComponentUnitsDimension().isEmpty()) //If base unit is available it might provide some insight into some properties.
        {
            FundamentalUnitsDataModel.UNIT_TYPE type = getUnitTypeByUnitSystemNUnitName(unit.getBaseUnit().getUnitSystem()
                    , unit.getBaseUnit().getName());

            if (type == null)
            {
                type = unit.getBaseUnit().getUnitType();
                if (type != FundamentalUnitsDataModel.UNIT_TYPE.UNKNOWN) {
                    return type;
                } else { //If still unknown, then try using the singular component unit.
                    Unit componentUnit = unitsDataModel.getUnitsContentMainRetriever().getUnit(unit.getComponentUnitsDimension().keySet().iterator().next());
                    if(componentUnit != null)
                        return componentUnit.getUnitType();
                }
            }
            return type;
        }
        return FundamentalUnitsDataModel.UNIT_TYPE.UNKNOWN;
    }

    ///
    public void setUnitsDataModel(UnitsDataModel unitsDataModel) {
        this.unitsDataModel = unitsDataModel;
    }
}
