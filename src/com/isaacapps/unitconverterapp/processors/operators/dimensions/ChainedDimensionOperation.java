package com.isaacapps.unitconverterapp.processors.operators.dimensions;

import java.util.HashMap;
import java.util.Map;

public class ChainedDimensionOperation<T> {
    private final Map<T, Double> resultantDimension;

    ///
    public ChainedDimensionOperation(){
        this(new HashMap<>());
    }
    public ChainedDimensionOperation(Map<T, Double>  initialDimension){
        this.resultantDimension = initialDimension;
    }

    ///
    public ChainedDimensionOperation multiply(Map<T, Double> secondDimension) {
        DimensionOperators.multiply(resultantDimension, secondDimension, false);
        return this;
    }

    public ChainedDimensionOperation divide(Map<T, Double> secondDimension) {
        DimensionOperators.divide(resultantDimension, secondDimension, false);
        return this;
    }

    ///
    public ChainedDimensionOperation exponentiate(Double exponentToBeRaisedTo){
        DimensionOperators.exponentiate(resultantDimension, exponentToBeRaisedTo, false);
        return this;
    }

    /**
     * Compares two generic map dimensions to see if they have corresponding dimension items with identical
     * dimensions values (exponents). Ignores dimension items with zero dimension values.
     * Default tolerance is 0.00001.
     */
    public boolean equalsDimension(Map<T, Double> firstDimension
            , Map<T, Double> secondDimension) {
        return DimensionOperators.equalsDimension(resultantDimension, secondDimension, 0.00001);
    }

    ///
    public Map<T, Double> getResultantDimension() {
        return resultantDimension;
    }
}
