package com.isaacapps.unitconverterapp.processors.operators.measurables.quantity;

import com.isaacapps.unitconverterapp.models.measurables.quantity.Quantity;
import com.isaacapps.unitconverterapp.models.measurables.quantity.QuantityException;
import com.isaacapps.unitconverterapp.processors.converters.QuantityConverter;
import com.isaacapps.unitconverterapp.processors.operators.measurables.units.IncompatibleUnitDimensionException;
import com.isaacapps.unitconverterapp.processors.operators.measurables.units.UnitOperators;

import java.util.Collections;

public class QuantityOperators {

    ///
    public static Quantity add(Quantity firstQuantity, Quantity secondQuantity) throws IncompatibleUnitDimensionException, QuantityException {
        return quantityOperation(1, firstQuantity, secondQuantity);
    }

    public static Quantity subtract(Quantity firstQuantity, Quantity secondQuantity) throws IncompatibleUnitDimensionException, QuantityException {
        return quantityOperation(-1, firstQuantity, secondQuantity);
    }

    private static Quantity quantityOperation(int sign, Quantity firstQuantity, Quantity secondQuantity) throws IncompatibleUnitDimensionException, QuantityException {
        if ( QuantityOperators.equalsUnitDimensionOf(firstQuantity,secondQuantity)) {
            //Coerce the unit of the second quantity to be that of the largest associated with the first quantity.
            Quantity coercedSecondQuantity = QuantityConverter.determineConversionQuantityToTargetUnit(secondQuantity, firstQuantity.getLargestUnit());
            coercedSecondQuantity.setValues(Collections.singletonList(firstQuantity.getWeightedValueWithRespectToLargestUnit() + sign * coercedSecondQuantity.getWeightedValueWithRespectToLargestUnit()));

            return coercedSecondQuantity;
        } else {
            throw new IncompatibleUnitDimensionException(firstQuantity.getLargestUnit(), secondQuantity.getLargestUnit());
        }
    }

    ///
    public static Quantity multiply(Quantity firstQuantity, Quantity secondQuantity) throws QuantityException {
        //To reduce complexity only accounts for the largest unit and corresponding weighted value
        return new Quantity(firstQuantity.getWeightedValueWithRespectToLargestUnit() * secondQuantity.getWeightedValueWithRespectToLargestUnit()
                , UnitOperators.multiply(firstQuantity.getLargestUnit(), secondQuantity.getLargestUnit()));
    }

    public static Quantity divide(Quantity firstQuantity, Quantity secondQuantity) throws QuantityException {
        //To reduce complexity only accounts for the largest unit and corresponding weighted value
        return new Quantity(firstQuantity.getWeightedValueWithRespectToLargestUnit() / secondQuantity.getWeightedValueWithRespectToLargestUnit()
                , UnitOperators.divide(firstQuantity.getLargestUnit(), secondQuantity.getLargestUnit()));
    }

    ///
    public static Quantity exponentiate(Quantity baseQuantity, Double exponent) throws QuantityException {
        //To reduce complexity only accounts for the largest unit and corresponding weighted value
        return new Quantity(Math.pow(baseQuantity.getWeightedValueWithRespectToLargestUnit(), exponent)
                , UnitOperators.exponentiate(baseQuantity.getLargestUnit(), exponent));
    }

    ///
    /**
     * Compares the equality of the largest unit of two quantities
     */
    public static boolean equalsUnitDimensionOf(Quantity firstQuantity, Quantity secondQuantity) {
        return UnitOperators.equalsDimension(firstQuantity.getLargestUnit(), secondQuantity.getLargestUnit());
    }

    public static boolean equalsValueNUnitDimensionWithRespectToLargestUnitOf(Quantity firstQuantity, Quantity secondQuantity) {
        return firstQuantity.getWeightedValueWithRespectToLargestUnit().equals(secondQuantity.getWeightedValueWithRespectToLargestUnit())
                && equalsUnitDimensionOf(firstQuantity, secondQuantity);
    }

    ///
    public static boolean greaterThan(Quantity firstQuantity, Quantity secondQuantity) throws IncompatibleUnitDimensionException, QuantityException {
        if ( QuantityOperators.equalsUnitDimensionOf(firstQuantity,secondQuantity)) {
            //Coerce the unit of the second quantity to be that of the largest associated with the first quantity.
            Quantity coercedSecondQuantity = QuantityConverter.determineConversionQuantityToTargetUnit(secondQuantity, firstQuantity.getLargestUnit());
            return firstQuantity.getWeightedValueWithRespectToLargestUnit() > coercedSecondQuantity.getWeightedValueWithRespectToLargestUnit();
        } else {
            throw new IncompatibleUnitDimensionException(firstQuantity.getLargestUnit(), secondQuantity.getLargestUnit());
        }
    }

    public static boolean lessThan(Quantity firstQuantity, Quantity secondQuantity) throws IncompatibleUnitDimensionException, QuantityException {
        if ( QuantityOperators.equalsUnitDimensionOf(firstQuantity,secondQuantity)) {
            //Coerce the unit of the second quantity to be that of the largest associated with the first quantity.
            Quantity coercedSecondQuantity = QuantityConverter.determineConversionQuantityToTargetUnit(secondQuantity, firstQuantity.getLargestUnit());
            return firstQuantity.getWeightedValueWithRespectToLargestUnit() < coercedSecondQuantity.getWeightedValueWithRespectToLargestUnit();
        } else {
            throw new IncompatibleUnitDimensionException(firstQuantity.getLargestUnit(), secondQuantity.getLargestUnit());
        }
    }
}
