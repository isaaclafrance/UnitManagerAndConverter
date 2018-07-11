package com.isaacapps.unitconverterapp.processors.converters;

import com.isaacapps.unitconverterapp.models.measurables.quantity.Quantity;
import com.isaacapps.unitconverterapp.models.measurables.quantity.QuantityException;
import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.processors.serializers.ISerializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

public class QuantityConverter {

    public static Quantity determineConversionQuantityToTargetUnit(Quantity sourceQuantity, Unit targetUnit) throws QuantityException {
        List<Unit> targetUnitsGroup = Collections.singletonList(targetUnit);

        return new Quantity(calculateConversionValuesToTargetUnitsGroup(sourceQuantity.getUnitValueMap()
                , targetUnitsGroup), targetUnitsGroup);
    }

    public static Quantity determineConversionQuantityToTargetUnitsGroup(Quantity sourceQuantity, List<Unit> targetUnitsGroup) throws QuantityException {
        return new Quantity(calculateConversionValuesToTargetUnitsGroup(sourceQuantity.getUnitValueMap()
                , targetUnitsGroup), targetUnitsGroup);
    }

    public Quantity determineConversionQuantityToTargetUnitSystem(Quantity sourceQuantity, String targetUnitSystem, ISerializer componentDimensionSerializer) throws QuantityException {
        return determineConversionQuantityToTargetUnitsGroup(sourceQuantity
                , Collections.singletonList(UnitConverter.determineConversionUnitToTargetUnitSystem(sourceQuantity.getLargestUnit()
                        , targetUnitSystem)));
    }

    ///
    private static List<Double> calculateConversionValuesToTargetUnitsGroup(SortedMap<Unit, Double> sourceUnitValueMap, List<Unit> targetUnits) {
        if (sourceUnitValueMap.size() == targetUnits.size()) {
            List<Unit> sortedTargetUnits = new ArrayList<>(targetUnits);
            Collections.sort(sortedTargetUnits, sourceUnitValueMap.comparator()); //Sort from largest to least unit.

            return calculateValuesGroupForTargetUnitsWithRespectToUnitBasis(sourceUnitValueMap.firstKey(),
                    calculateReducedValueWithRespectToLargestUnit(sourceUnitValueMap)
                    , sortedTargetUnits);
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * Reduces the all the unit values to one value with the largest unit as the basis.
     * Uses the weighted sum of each of the Source values to find the distributed values associated with the target units.
     */
    public static double calculateReducedValueWithRespectToLargestUnit(SortedMap<Unit, Double> unitValueMap) {
        if (unitValueMap.size() > 1) {
            double[] conversionCoeffs;
            Double weightedSumOfValues = unitValueMap.get(unitValueMap.firstKey());
            for (Map.Entry<Unit, Double> unitValueEntry : unitValueMap.entrySet()) {
                conversionCoeffs = UnitConverter.calculateConversionCoeffsToTargetUnit(unitValueEntry.getValue(), unitValueEntry.getKey(), unitValueMap.firstKey());
                weightedSumOfValues += unitValueEntry.getValue() * conversionCoeffs[0] + conversionCoeffs[1];
            }
            return weightedSumOfValues;
        } else {
            return unitValueMap.isEmpty() ? 0.0 : unitValueMap.get(unitValueMap.keySet().iterator().next());
        }
    }

    public static List<Double> calculateValuesGroupForTargetUnitsWithRespectToUnitBasis(Unit basisUnit
            , Double valueOfBasisUnit, List<Unit> targetUnits) {

        double[] conversionCoeffs = UnitConverter.calculateConversionCoeffsToTargetUnit(valueOfBasisUnit, basisUnit, targetUnits.get(0));
        List<Double> targetValues = new ArrayList<>(targetUnits.size());
        targetValues.add(valueOfBasisUnit * conversionCoeffs[0] + conversionCoeffs[1]);

        for (int i = 1; i < targetValues.size(); i++) {
            double cascadedRemainder = targetValues.get(i - 1) - Math.floor(targetValues.get(i - 1));
            conversionCoeffs = UnitConverter.calculateConversionCoeffsToTargetUnit(cascadedRemainder, targetUnits.get(i - 1), targetUnits.get(i));
            targetValues.add(cascadedRemainder * conversionCoeffs[0] + conversionCoeffs[1]);
        }

        return targetValues;
    }

}
