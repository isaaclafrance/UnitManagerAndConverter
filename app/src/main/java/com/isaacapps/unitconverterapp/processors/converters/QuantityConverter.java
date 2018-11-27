package com.isaacapps.unitconverterapp.processors.converters;

import com.isaacapps.unitconverterapp.models.measurables.quantity.Quantity;
import com.isaacapps.unitconverterapp.models.measurables.quantity.QuantityException;
import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

public class QuantityConverter {

    public static Quantity determineConversionQuantityToTargetUnit(Quantity sourceQuantity, Unit targetUnit, boolean ensureValueOfLargestUnitIsLargerThanOne) throws QuantityException {
        return determineConversionQuantityToTargetUnitsGroup(sourceQuantity, Collections.singletonList(targetUnit), ensureValueOfLargestUnitIsLargerThanOne);
    }

    public static Quantity determineConversionQuantityToTargetUnitsGroup(Quantity sourceQuantity, List<Unit> targetUnitsGroup, boolean ensureValueOfLargestUnitIsLargerThanOne) throws QuantityException {
        return new Quantity(calculateConversionValuesToTargetUnitsGroup(sourceQuantity.getUnitValueMap()
                , targetUnitsGroup, ensureValueOfLargestUnitIsLargerThanOne), targetUnitsGroup);
    }

    public static Quantity determineConversionQuantityToTargetUnitSystem(Quantity sourceQuantity, String targetUnitSystem, boolean ensureValueOfLargestUnitIsLargerThanOne) throws QuantityException {
        return determineConversionQuantityToTargetUnitsGroup(sourceQuantity
                , Collections.singletonList(UnitConverter.determineConversionUnitToTargetUnitSystem(sourceQuantity.getLargestUnit(), targetUnitSystem))
                , ensureValueOfLargestUnitIsLargerThanOne);
    }

    ///
    private static List<Double> calculateConversionValuesToTargetUnitsGroup(SortedMap<Unit, Double> sourceUnitValueMap, List<Unit> targetUnits
            , boolean ensureValueOfLargestUnitIsLargerThanOne) {

        if(sourceUnitValueMap.size() == 0 || targetUnits.size() == 0)
            return Collections.emptyList();

        List<Unit> sortedTargetUnits = new ArrayList<>(targetUnits);
        Collections.sort(sortedTargetUnits, sourceUnitValueMap.comparator()); //Sort from largest to least unit.

        return calculateValuesGroupForTargetUnitsWithRespectToUnitBasis(sourceUnitValueMap.firstKey()
                , calculateWeightedValueWithRespectToLargestUnit(sourceUnitValueMap)
                , sortedTargetUnits
                , ensureValueOfLargestUnitIsLargerThanOne);
    }

    /**
     * Reduces the all the unit values to one value with the largest unit as the basis.
     * Uses the weighted sum of each of the Source values to find the distributed values associated with the target units.
     */
    public static double calculateWeightedValueWithRespectToLargestUnit(SortedMap<Unit, Double> unitValueMap) {
        if (unitValueMap.size() > 1) {
            double[] conversionCoeffs;
            Double weightedSumOfValues = 0.0;

            for (Map.Entry<Unit, Double> unitValueEntry : unitValueMap.entrySet()) {
                conversionCoeffs = UnitConverter.calculateConversionCoeffsToTargetUnit(unitValueEntry.getValue(), unitValueEntry.getKey(), unitValueMap.firstKey());
                weightedSumOfValues += unitValueEntry.getValue() * conversionCoeffs[0] + conversionCoeffs[1];
            }

            return weightedSumOfValues;
        } else {
            return unitValueMap.isEmpty() ? 0.0 : unitValueMap.entrySet().iterator().next().getValue();
        }
    }

    public static List<Double> calculateValuesGroupForTargetUnitsWithRespectToUnitBasis(Unit basisUnit
            , Double valueOfBasisUnit, List<Unit> sortedTargetUnits, boolean ensureValueOfLargestUnitIsLargerThanOne) {

        double[] conversionCoeffs = UnitConverter.calculateConversionCoeffsToTargetUnit(valueOfBasisUnit, basisUnit, sortedTargetUnits.get(0));
        List<Double> targetValues = new ArrayList(Collections.nCopies(sortedTargetUnits.size(), 0.0));

        double conversionValue = valueOfBasisUnit * conversionCoeffs[0] + conversionCoeffs[1];
        if( !ensureValueOfLargestUnitIsLargerThanOne || (conversionValue > 1 || sortedTargetUnits.size() == 1) )
            targetValues.set(0, conversionValue);

        for (int i = 1; i < sortedTargetUnits.size(); i++) {
            boolean previousSkipped = targetValues.get(i - 1) == 0.0 && ensureValueOfLargestUnitIsLargerThanOne;
            double cascadedRemainder = previousSkipped ? valueOfBasisUnit : (targetValues.get(i - 1) - (targetValues.get(i - 1) > 1 ? Math.floor(targetValues.get(i - 1)) : targetValues.get(i - 1)) );
            conversionCoeffs = UnitConverter.calculateConversionCoeffsToTargetUnit(cascadedRemainder, previousSkipped ? basisUnit : sortedTargetUnits.get(i - 1), sortedTargetUnits.get(i));

            conversionValue = cascadedRemainder * conversionCoeffs[0] + conversionCoeffs[1];
            if(!ensureValueOfLargestUnitIsLargerThanOne || (conversionValue > 1 || i == sortedTargetUnits.size() -1) )
                targetValues.set(i, conversionValue);
        }

        return targetValues;
    }

}
