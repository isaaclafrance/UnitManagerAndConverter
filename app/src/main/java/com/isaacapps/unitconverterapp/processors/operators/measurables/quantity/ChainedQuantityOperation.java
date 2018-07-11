package com.isaacapps.unitconverterapp.processors.operators.measurables.quantity;

import com.isaacapps.unitconverterapp.models.measurables.quantity.Quantity;
import com.isaacapps.unitconverterapp.models.measurables.quantity.QuantityException;
import com.isaacapps.unitconverterapp.processors.operators.measurables.units.IncompatibleUnitDimensionException;

public class ChainedQuantityOperation<T> {
    private Quantity resultantQuantity;

    ///
    public ChainedQuantityOperation(Quantity initialQuantity){
        this.resultantQuantity = initialQuantity;
    }

    ///
    public ChainedQuantityOperation multiply(Quantity secondQuantity) throws QuantityException {
        resultantQuantity = QuantityOperators.multiply(resultantQuantity, secondQuantity);
        return this;
    }

    public ChainedQuantityOperation divide(Quantity secondQuantity) throws QuantityException {
        resultantQuantity = QuantityOperators.divide(resultantQuantity, secondQuantity);
        return this;
    }

    ///
    public ChainedQuantityOperation add(Quantity secondQuantity) throws IncompatibleUnitDimensionException, QuantityException {
        resultantQuantity = QuantityOperators.add(resultantQuantity, secondQuantity);
        return this;
    }

    public ChainedQuantityOperation subtract(Quantity secondQuantity) throws IncompatibleUnitDimensionException, QuantityException {
        resultantQuantity = QuantityOperators.subtract(resultantQuantity, secondQuantity);
        return this;
    }

    ///
    public ChainedQuantityOperation exponentiate(Double exponent) throws QuantityException {
        resultantQuantity = QuantityOperators.exponentiate(resultantQuantity, exponent);
        return this;
    }

    ///
    public boolean equalsUnitDimensionOf(Quantity secondQuantity) {
        return QuantityOperators.equalsUnitDimensionOf(resultantQuantity, secondQuantity);
    }

    public  boolean equalsValueNUnitDimensionWithRespectToLargestUnitOf(Quantity secondQuantity) {
        return QuantityOperators.equalsValueNUnitDimensionWithRespectToLargestUnitOf(resultantQuantity, secondQuantity);
    }

    ///
    public boolean greaterThan(Quantity secondQuantity) throws IncompatibleUnitDimensionException, QuantityException {
        return QuantityOperators.greaterThan(resultantQuantity, secondQuantity);
    }

    public boolean lessThan(Quantity secondQuantity) throws IncompatibleUnitDimensionException, QuantityException {
        return QuantityOperators.lessThan(resultantQuantity, secondQuantity);
    }

    ///

    /**
     * Quantity resulting from the accumulation of all the applied quantity operations.
     */
    public Quantity getResultantQuantity() {
        return resultantQuantity;
    }
}
