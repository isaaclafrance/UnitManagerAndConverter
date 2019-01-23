package com.isaacapps.unitconverterapp.processors.operators.dimensions;

import java.util.Collection;
import java.util.Collections;
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
    public ChainedDimensionOperation<T> multiply(Map<T, Double>... dimensions) {
        for(Map<T, Double> dimension:dimensions)
            DimensionOperators.multiply(resultantDimension, dimension, false);
        return this;
    }

    public ChainedDimensionOperation<T> divide(Map<T, Double>... dimensions) {
        for(Map<T, Double> dimension:dimensions)
            DimensionOperators.divide(resultantDimension, dimension, false);
        return this;
    }

    public ChainedDimensionOperation<T> exponentiate(Double exponentToBeRaisedTo){
        DimensionOperators.exponentiate(resultantDimension, exponentToBeRaisedTo, false);
        return this;
    }

    ///
    /**
     * Compares two generic map dimensions to see if they have corresponding dimension items with identical
     * dimensions values (exponents). Ignores dimension items with zero dimension values.
     * Default tolerance is 0.00001.
     */
    public boolean equalsDimension(Map<T, Double> secondDimension) {
        return DimensionOperators.equalsDimension(resultantDimension, secondDimension);
    }

    public boolean equalsDimension(Map<T, Double> secondDimension, Collection<T> dimensionItemsToIgnore) {
        return DimensionOperators.equalsDimension(resultantDimension, secondDimension, dimensionItemsToIgnore);
    }

    ///
    /**
     * Dimension resulting from the accumulation of all the applied dimension operations.
     */
    public Map<T, Double> getResultantDimension() {
        return resultantDimension;
    }
}
