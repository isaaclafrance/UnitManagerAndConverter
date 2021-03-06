package com.isaacapps.unitconverterapp.processors.converters.algorithms;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;

public interface IConversionAlgorithm {
    /**
     * Calculates the linear components can can be used to convert this unit to the target unit.
     * @param sourceValue In cases where the base conversion is not constant, the conversion cofficient will be a function of source value.
     * @return An array where the first element is the multiplicative coefficient
     * and the second element is additive constant coefficient.
     */
    double[] calculateConversionCoeffsToTargetUnit(double sourceValue, Unit sourceUnit, Unit targetUnit);
}
