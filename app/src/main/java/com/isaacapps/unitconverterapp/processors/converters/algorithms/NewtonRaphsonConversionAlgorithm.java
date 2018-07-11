package com.isaacapps.unitconverterapp.processors.converters.algorithms;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;

public class NewtonRaphsonConversionAlgorithm implements IConversionAlgorithm {
    /**
     * Use simplified version of the Newton-Raphson method to calculate the coeficients needed to convert this unit to the target unit.
     */
    @Override
    public double[] calculateConversionCoeffsToTargetUnit(double sourceValue, Unit sourceUnit, Unit targetUnit) {
        //TODO: Consider using the open source CHOCO solver. http://www.choco-solver.org/
        return new double[2];

    }

}
