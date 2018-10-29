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
    public ChainedQuantityOperation<T> multiply(Quantity... quantities) throws QuantityException {
        for(Quantity quantity:quantities)
            resultantQuantity = QuantityOperators.multiply(resultantQuantity, quantity);
        return this;
    }

    public ChainedQuantityOperation<T> divide(Quantity... quantities) throws QuantityException {
        for(Quantity quantity:quantities)
            resultantQuantity = QuantityOperators.divide(resultantQuantity, quantity);
        return this;
    }

    ///
    public ChainedQuantityOperation<T> add(Quantity... quantities) throws IncompatibleUnitDimensionException, QuantityException {
        for(Quantity quantity:quantities)
            resultantQuantity = QuantityOperators.add(resultantQuantity, quantity);
        return this;
    }

    public ChainedQuantityOperation<T> subtract(Quantity... quantities) throws IncompatibleUnitDimensionException, QuantityException {
        for(Quantity quantity:quantities)
            resultantQuantity = QuantityOperators.subtract(resultantQuantity, quantity);
        return this;
    }

    ///
    public ChainedQuantityOperation<T> exponentiate(Double exponent) throws QuantityException {
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
