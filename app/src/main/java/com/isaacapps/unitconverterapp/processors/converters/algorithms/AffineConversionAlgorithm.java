package com.isaacapps.unitconverterapp.processors.converters.algorithms;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;

public class AffineConversionAlgorithm implements IConversionAlgorithm {
    /**
     * Calculates the linear components can can be used to convert this unit to the target unit.
     * @return An array where the first element is the multiplicative coefficient
     * and the second element is additive constant coefficient.
     */
    @Override
    public double[] calculateConversionCoeffsToTargetUnit(double sourceValue, Unit sourceUnit, Unit targetUnit) {
        //Linear polynomial relationship is assumed.
        if (sourceUnit.getBaseConversionPolyCoeffs()[1] == 0.0 && targetUnit.getBaseConversionPolyCoeffs()[1] == 0.0) {
            return new double[]{sourceUnit.getBaseConversionPolyCoeffs()[0] / targetUnit.getBaseConversionPolyCoeffs()[0], 0.0};
        } else {
            return new double[]{sourceUnit.getBaseConversionPolyCoeffs()[0] / targetUnit.getBaseConversionPolyCoeffs()[0],
                    (sourceUnit.getBaseConversionPolyCoeffs()[1] - targetUnit.getBaseConversionPolyCoeffs()[1]) / targetUnit.getBaseConversionPolyCoeffs()[0]};
        }
    }

}
