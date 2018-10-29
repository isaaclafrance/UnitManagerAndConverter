package com.isaacapps.unitconverterapp.models.unitmanager.datamodels;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.measurables.unit.UnitException;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.IDualKeyNCategoryRepository;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsDataModel;
import com.isaacapps.unitconverterapp.processors.operators.dimensions.DimensionOperators;

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
        MASS, LENGTH, TIME, AMT_OF_SUBSTANCE, NUMBER, ANGLE, TEMPERATURE, CHARGE, LUMINOUS_INTENSITY, DERIVED_SINGLE_UNIT, DERIVED_MULTI_UNIT, UNKNOWN, CURRENCY
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
            unitsDataModel.getUnitsContentModifier().updateFundamentalUnitsDimensionOfKnownUnits();
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
            if(componentUnit == null || componentUnit.getType() == UNIT_TYPE.UNKNOWN){
                DimensionOperators.alterExponentOfDimensionItem(fundamentalUnitMap, getFirstSuitableUnitTypeWithinUnitSystemCollection(componentUnitName, failSafeUnitSystems), componentUnitExponent);
                continue;
            }

            ///
            if (componentUnit.getType() == UNIT_TYPE.DERIVED_MULTI_UNIT) {
                Map<String, Double> selectedInnerComponentUnitsMap = ((componentUnit.getComponentUnitsDimension().size() == 1) ? componentUnit.getBaseUnit() : componentUnit).getComponentUnitsDimension();

                if(selectedInnerComponentUnitsMap.containsKey(componentUnitName)) {  //Prevent infinite recursion
                    DimensionOperators.alterExponentOfDimensionItem(fundamentalUnitMap, UNIT_TYPE.UNKNOWN, componentUnitExponent);
                    continue;
                }

                Map<UNIT_TYPE, Double> recursedFundUnitMap = transformComponentUnitsDimensionToFundamentalUnitsDimension(selectedInnerComponentUnitsMap, failSafeUnitSystems, createMissingComplexUnits);

                for (UNIT_TYPE unitType : UNIT_TYPE.values()) {
                    if (recursedFundUnitMap.containsKey(unitType)) {
                        DimensionOperators.alterExponentOfDimensionItem(fundamentalUnitMap, unitType, componentUnitExponent * recursedFundUnitMap.get(unitType));
                    }
                }
            } else {
                if (componentUnit.getType() != UNIT_TYPE.DERIVED_SINGLE_UNIT) {
                    DimensionOperators.alterExponentOfDimensionItem(fundamentalUnitMap, componentUnit.getBaseUnit().getType(), componentUnitExponent);

                } else {
                    for (Entry<UNIT_TYPE, Double> fundEntry : componentUnit.getBaseUnit().getFundamentalUnitTypesDimension().entrySet()) {
                        DimensionOperators.alterExponentOfDimensionItem(fundamentalUnitMap, fundEntry.getKey(), fundEntry.getValue() * componentUnitExponent);
                    }
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
    public void setUnitsDataModel(UnitsDataModel unitsDataModel) {
        this.unitsDataModel = unitsDataModel;
    }
}
