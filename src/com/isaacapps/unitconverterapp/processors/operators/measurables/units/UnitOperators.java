package com.isaacapps.unitconverterapp.processors.operators.measurables.units;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel;
import com.isaacapps.unitconverterapp.processors.operators.dimensions.DimensionOperators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.isaacapps.unitconverterapp.models.measurables.unit.Unit.UNKNOWN_UNIT_NAME;
import static com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel.UNIT_TYPE;

public class UnitOperators {
    public static Unit multiply(Unit firstUnit, Unit secondUnit) {
        return retrieveExistingUnitWithComparableDimension(
                DimensionOperators.multiply(firstUnit.getComponentUnitsDimension()
                        , secondUnit.getComponentUnitsDimension())
                , retrieveValidUnitManagerContexts(firstUnit, secondUnit));
    }

    public static Unit divide(Unit firstUnit, Unit secondUnit) {
        return retrieveExistingUnitWithComparableDimension(
                DimensionOperators.divide(firstUnit.getComponentUnitsDimension()
                        , secondUnit.getComponentUnitsDimension())
                , retrieveValidUnitManagerContexts(firstUnit, secondUnit));
    }

    ///
    public static Unit exponentiate(Unit baseUnit, Double exponent){
        return retrieveExistingUnitWithComparableDimension(
                DimensionOperators.exponentiate(baseUnit.getComponentUnitsDimension()
                        , exponent)
                , retrieveValidUnitManagerContexts(baseUnit));
    }

    /***
     * Retrieves a list of non null units manager context from provided units
     */
    public static List<UnitManager> retrieveValidUnitManagerContexts(Unit... units) {
        List<UnitManager> unitManagers = new ArrayList<>();

        for (Unit unit : units) {
            if (unit.getUnitManagerContext() != null)
                unitManagers.add(unit.getUnitManagerContext());
        }

        return unitManagers;
    }

    /***
     * Retrieves known base unit that matches new unit. Otherwise return a new unit with same dimension .
     */
    private static Unit retrieveExistingUnitWithComparableDimension(Map<String, Double> componentUnitsExponentMap
            , Collection<UnitManager> validUnitManagers) {
        Unit resultUnit = null;

        for (UnitManager unitManager : validUnitManagers) {
            Collection<Unit> matchedUnits = unitManager.getUnitsDataModel().getContentQuerier()
                    .getUnitsByComponentUnitsDimension(componentUnitsExponentMap, true);

            if (!matchedUnits.isEmpty()) {
                resultUnit = matchedUnits.iterator().next();
                if (!resultUnit.getBaseUnit().getName().equalsIgnoreCase(UNKNOWN_UNIT_NAME)) {
                    resultUnit = resultUnit.getBaseUnit();
                    break;
                }
            }
        }

        if(resultUnit == null && !validUnitManagers.isEmpty()){
            resultUnit = new Unit(componentUnitsExponentMap, false);
            resultUnit.setLocale(validUnitManagers.iterator().next().getLocale());
            return resultUnit;
        }
        else{
            return resultUnit;
        }
    }

    ///
    public static boolean equalsDimension(Unit firstUnit, Unit secondUnit) {
        if (firstUnit.getUnitManagerContext() == secondUnit.getUnitManagerContext()
                && !secondUnit.getFundamentalUnitTypesDimension().containsKey(FundamentalUnitsDataModel.UNIT_TYPE.UNKNOWN)
                && !firstUnit.getFundamentalUnitTypesDimension().containsKey(FundamentalUnitsDataModel.UNIT_TYPE.UNKNOWN)) {
            //Since the units belong to same unit manager context and the unit manager is assumed to be internally consistent,
            //units with the same dimension are given the same base units
            return firstUnit.getBaseUnit() == secondUnit.getBaseUnit();
        } else {
            Collection<UnitManager> validUnitManagerContext = retrieveValidUnitManagerContexts(firstUnit, secondUnit);

            return equalsDeepComponentUnitsDimension(firstUnit.getComponentUnitsDimension()
                    , secondUnit.getComponentUnitsDimension()
                    , !validUnitManagerContext.isEmpty()?validUnitManagerContext.iterator().next():null );
        }
    }

    /**
     * Initially compares the provided component unit dimensions direclty
     * , then if that fails prcoeeds to tranform the component units to fudamdnetal units dimesnions using one of the unit managers and then compares that.
     */
    public static boolean equalsDeepComponentUnitsDimension(Map<String, Double> firstComponentUnitsDimension, Map<String, Double> secondComponentUnitsDimension, UnitManager validUnitManagerContext) {
        if (!DimensionOperators.equalsDimension(firstComponentUnitsDimension, secondComponentUnitsDimension) && validUnitManagerContext != null) {
            Map<UNIT_TYPE, Double> firstFundamentalUnitsDimension = validUnitManagerContext.getFundamentalUnitsDataModel().transformComponentUnitsDimensionToFundamentalUnitsDimension(firstComponentUnitsDimension);
            Map<UNIT_TYPE, Double> secondFundamentalUnitsDimension = validUnitManagerContext.getFundamentalUnitsDataModel().transformComponentUnitsDimensionToFundamentalUnitsDimension(secondComponentUnitsDimension);

            return equalsFundamentalUnitsDimension(firstFundamentalUnitsDimension, secondFundamentalUnitsDimension);
        }
        return true;
    }

    /**
     * Stricly compares the component units dimensions of the provided units.
     */
    public static boolean equalsComponentUnitsDimension(Unit firstUnit, Unit secondUnit) {
        return !DimensionOperators.equalsDimension(firstUnit.getComponentUnitsDimension(), secondUnit.getComponentUnitsDimension());
    }

    /**
     * If either dimension does not have an unknown, then the fundamental unit type dimensions are compared for quality without regard for dimensional items raised to zero.
     */
    public static boolean equalsFundamentalUnitsDimension(Map<UNIT_TYPE, Double> firstFundamentalUnitTypesDimension, Map<UNIT_TYPE, Double> secondFundamentalUnitTypesDimension) {
        return !firstFundamentalUnitTypesDimension.containsKey(UNIT_TYPE.UNKNOWN)
                && !secondFundamentalUnitTypesDimension.containsKey(UNIT_TYPE.UNKNOWN)
                && DimensionOperators.equalsDimension(firstFundamentalUnitTypesDimension, secondFundamentalUnitTypesDimension);
    }
    public static boolean equalsFundamentalUnitsDimension(Unit firstUnit, Unit secondUnit) {
        return equalsFundamentalUnitsDimension(firstUnit.getFundamentalUnitTypesDimension(), secondUnit.getFundamentalUnitTypesDimension());
    }
}
