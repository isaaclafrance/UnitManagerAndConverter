package com.isaacapps.unitconverterapp.processors.operators.measurables.units;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.measurables.unit.UnitException;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsContentDeterminer;
import com.isaacapps.unitconverterapp.processors.operators.dimensions.DimensionOperators;
import com.isaacapps.unitconverterapp.processors.parsers.ParsingException;

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

    public static Unit exponentiate(Unit baseUnit, Double exponent){
        return retrieveExistingUnitWithComparableDimension(
                DimensionOperators.exponentiate(baseUnit.getComponentUnitsDimension()
                        , exponent)
                , retrieveValidUnitManagerContexts(baseUnit));
    }

    ///

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
        Unit suitableUnit = null;

        for (UnitManager unitManager : validUnitManagers) {
            Collection<Unit> matchedUnits = unitManager.getUnitsDataModel().getUnitsContentQuerier()
                    .queryUnitsByComponentUnitsDimension(componentUnitsExponentMap, true);

            if (!matchedUnits.isEmpty()) {
                suitableUnit = matchedUnits.iterator().next();
                if (!suitableUnit.getBaseUnit().getName().equalsIgnoreCase(UNKNOWN_UNIT_NAME)) {
                    suitableUnit = suitableUnit.getBaseUnit();
                    break;
                }
            }
        }

        if(suitableUnit == null){
            try {
                suitableUnit = new Unit(componentUnitsExponentMap);
                if( !validUnitManagers.isEmpty())
                    suitableUnit.setLocale(validUnitManagers.iterator().next().getLocale());
            } catch (ParsingException e) {
                e.printStackTrace();
            }
        }

        return suitableUnit;
    }

    ///
    public static boolean equalsDimension(Unit firstUnit, Unit secondUnit) {
        if (firstUnit.getUnitManagerContext() == secondUnit.getUnitManagerContext()
                && UnitsContentDeterminer.determineGeneralDataModelCategory(firstUnit) != UnitsContentDeterminer.DATA_MODEL_CATEGORY.UNKNOWN
                && UnitsContentDeterminer.determineGeneralDataModelCategory(secondUnit) != UnitsContentDeterminer.DATA_MODEL_CATEGORY.UNKNOWN) {
            //Since the units belong to same unit manager context and the unit manager is assumed to be internally consistent,
            //units with the same dimension are given the same base units
            return firstUnit.getBaseUnit() == secondUnit.getBaseUnit();
        } else {
            return equalsComponentUnitsDimension(firstUnit, secondUnit) || equalsFundamentalUnitsDimension(firstUnit, secondUnit);
        }
    }

    /**
     * Initially compares the provided component unit dimensions directly
     * , then if that fails proceeds to transform the component units to fundamental units dimensions using one of the unit managers and/or the fail-safe unit systems and then compares that.
     */
    public static boolean equalsDeepComponentUnitsDimension(Map<String, Double> firstComponentUnitsDimension, Map<String, Double> secondComponentUnitsDimension, Collection<String> failSafeUnitSystems, UnitManager validUnitManagerContext) {
        if (!DimensionOperators.equalsDimension(firstComponentUnitsDimension, secondComponentUnitsDimension) && validUnitManagerContext != null) {
            Map<UNIT_TYPE, Double> firstFundamentalUnitsDimension = validUnitManagerContext.getFundamentalUnitsDataModel().transformComponentUnitsDimensionToFundamentalUnitsDimension(firstComponentUnitsDimension, failSafeUnitSystems, true);
            Map<UNIT_TYPE, Double> secondFundamentalUnitsDimension = validUnitManagerContext.getFundamentalUnitsDataModel().transformComponentUnitsDimensionToFundamentalUnitsDimension(secondComponentUnitsDimension, failSafeUnitSystems, true);

            return equalsFundamentalUnitsDimension(firstFundamentalUnitsDimension, secondFundamentalUnitsDimension);
        }
        return true;
    }

    /**
     * Strictly compares the component units dimensions of the provided units.
     */
    public static boolean equalsComponentUnitsDimension(Unit firstUnit, Unit secondUnit) {
        return DimensionOperators.equalsDimension(firstUnit.getComponentUnitsDimension(), secondUnit.getComponentUnitsDimension());
    }

    /**
     * If neither dimension has an unknown, then the fundamental unit type dimensions are compared for equality without regard for dimensional items raised to zero.
     */
    public static boolean equalsFundamentalUnitsDimension(Map<UNIT_TYPE, Double> firstFundamentalUnitTypesDimension, Map<UNIT_TYPE, Double> secondFundamentalUnitTypesDimension) {
        return !firstFundamentalUnitTypesDimension.containsKey(UNIT_TYPE.UNKNOWN)
                && !secondFundamentalUnitTypesDimension.containsKey(UNIT_TYPE.UNKNOWN)
                && DimensionOperators.equalsDimension(firstFundamentalUnitTypesDimension, secondFundamentalUnitTypesDimension);
    }
    public static boolean equalsFundamentalUnitsDimension(Unit firstUnit, Unit secondUnit) {
        return equalsFundamentalUnitsDimension(firstUnit.getFundamentalUnitTypesDimension(), secondUnit.getFundamentalUnitTypesDimension());
    }

    ///

    /**
     * Compares two units base of the magnitude of their conversion coefficients to the same base unit.
     * Smaller conversion magnitude signifies a unit is smaller.
     * @return -1 if left hand side unit is smaller than right hand side unit, + if vice versa. 0 of units are the same
     * @throws UnitException
     */
    public static int compareTo(Unit lhsUnit, Unit rhsUnit) throws UnitException {
        List<String> requirements = new ArrayList<>();

        if( lhsUnit.getUnitManagerContext() != null && lhsUnit.getUnitManagerContext() != rhsUnit.getUnitManagerContext())
            requirements.add(String.format("Units (%s, %s) do not have the same unit manager context.", lhsUnit, rhsUnit));
        if( lhsUnit.getBaseUnit() != rhsUnit.getBaseUnit())
            requirements.add(String.format("Units (%s, %s) do not have the same base unit.", lhsUnit, rhsUnit));
        if(lhsUnit.isUsingBaseConversionExpression() || rhsUnit.isUsingBaseConversionExpression())
            requirements.add(String.format("At least one of units (%s, %s) is using a base conversion expression.", lhsUnit, rhsUnit));

        UnitException.validateRequiredComponentsCollection(requirements);

        int firstCoeffComparison = Double.compare(lhsUnit.getBaseConversionPolyCoeffs()[1], rhsUnit.getBaseConversionPolyCoeffs()[1]);
        if(firstCoeffComparison == 0) {
            int secondCoeffComparison = Double.compare(lhsUnit.getBaseConversionPolyCoeffs()[0], rhsUnit.getBaseConversionPolyCoeffs()[0]);
            return secondCoeffComparison;
        }
        else {
            return firstCoeffComparison;
        }
    }

    public static boolean lessThan(Unit lhsUnit, Unit rhsUnit) throws UnitException {
        return compareTo(lhsUnit, rhsUnit) < 0;
    }

    public static boolean greaterThan(Unit lhsUnit, Unit rhsUnit) throws UnitException {
        return compareTo(lhsUnit, rhsUnit) > 0;
    }
}

