package com.isaacapps.unitconverterapp.processors.operators.dimensions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class DimensionOperators {
    public enum DIMENSION_TYPE { DERIVED_SINGLE, DERIVED_MULTI, SIMPLE }

    ///
    public static <T> ChainedDimensionOperation<T> multiply(Map<T, Double> firstDimension){
        return new ChainedDimensionOperation<T>().multiply(firstDimension);
    }
    public static <T> ChainedDimensionOperation<T> divide(Map<T, Double> firstDimension){
        return new ChainedDimensionOperation<T>().divide(firstDimension);
    }

    ///
    public static <T> Map<T, Double> multiply(Map<T, Double> firstDimension
            , Map<T, Double> secondDimension) {
        return multiply(firstDimension, secondDimension, true);
    }
    public static <T> Map<T, Double> divide(Map<T, Double> firstDimension
            , Map<T, Double> secondDimension) {
        return divide(firstDimension, secondDimension, true);
    }

    public static <T> Map<T, Double> multiply(Map<T, Double> firstDimension
            , Map<T, Double> secondDimension, boolean createNewDimension) {
        return dimensionOperation(+1, firstDimension, secondDimension, createNewDimension);
    }
    public static <T> Map<T, Double> divide(Map<T, Double> firstDimension
            , Map<T, Double> secondDimension, boolean createNewDimension) {
        return dimensionOperation(-1, firstDimension, secondDimension, createNewDimension);
    }

    private static <T> Map<T, Double> dimensionOperation(int sign, Map<T, Double> firstDimension
            , Map<T, Double> secondDimension, boolean createNewDimension) {

        Map<T, Double> finalDimension = createNewDimension ? new HashMap<>(firstDimension):firstDimension;

        //Subtracts or adds the exponents of same component units based on division or multiplication operation respectively.
        for (T key : secondDimension.keySet())
            alterExponentOfDimensionItem(finalDimension, key, sign * secondDimension.get(key));

        return finalDimension;
    }

    ///
    public static <T> Map<T, Double> exponentiate(Map<T, Double> baseDimension, Double exponentToBeRaisedTo, boolean createNewDimension){
        Map<T, Double> newDimension = createNewDimension ? new HashMap<>(baseDimension):baseDimension;

        for(T key : baseDimension.keySet())
            alterExponentOfDimensionItem(newDimension, key, (baseDimension.get(key) - 1) * exponentToBeRaisedTo);

        return newDimension;
    }
    public static <T> Map<T, Double> exponentiate(Map<T, Double> baseDimension, Double exponentToBeRaisedTo){
        return exponentiate(baseDimension, exponentToBeRaisedTo, true);
    }

    ///
    public static <T> Map<T, Double> inverse(Map<T, Double> dimension, boolean createNewDimension){
        return exponentiate(dimension, -1.0, createNewDimension);
    }
    public static <T> Map<T, Double> inverse(Map<T, Double> dimension){
        return inverse(dimension, true);
    }

    ///
    /**
     * Increases the exponent associated with dimension map by the provided exponent.
     * If the item did not already exist in map, then it first added with initial value of zero before increase.
     */
    public static <T> Map<T, Double> alterExponentOfDimensionItem(Map<T, Double> dimensionMap, T item, Double exponentDelta){
        dimensionMap.put(item, exponentDelta + (dimensionMap.containsKey(item) ? dimensionMap.get(item) : 0.0));
        return dimensionMap;
    }

    /**
     * Compares two generic map dimensions to see if they have corresponding dimension items with identical
     * dimensions values (exponents). Ignores dimension items with zero dimension values.
     * Default tolerance is 0.00001.
     */
    public static <T> boolean equalsDimension(Map<T, Double> firstDimension
            , Map<T, Double> secondDimension) {
        return equalsDimension(firstDimension, secondDimension, 0.00001);
    }
    /**
     * Compares two map dimensions to see if they have corresponding dimension items with identical
     * dimensions values (exponents). Ignores dimension items with zero dimension values.
     */
    public static <T> boolean equalsDimension(Map<T, Double> firstDimension
            , Map<T, Double> secondDimension, Double tolerance) {
        if (firstDimension.isEmpty() || secondDimension.isEmpty())
            return false;

        secondDimension = new HashMap<>(secondDimension); //clone map since it will be modified

        for (Map.Entry<T, Double> entry : firstDimension.entrySet()) {
            if (!secondDimension.containsKey(entry.getKey())) {
                if (Math.abs(entry.getValue()) > 0.0) {
                    return false;
                }
            } else if (Math.abs(secondDimension.get(entry.getKey()) - entry.getValue()) > tolerance) {
                return false;
            }
            secondDimension.remove(entry.getKey());
        }

        //Although the number of components may be the same, there maybe some components that are raised to zero and
        // not compared in the Second dimension since first dimension map was the initial basis of comparison.
        for (Map.Entry<T, Double> entry : secondDimension.entrySet()) {
            if (Math.abs(entry.getValue()) > 0.0) {
                return false;
            }
        }

        return true;
    }

    //
    public static <T> Map<T, Double> removeDimensionItemsRaisedToZero(Map<T, Double> dimension, double tolerance){
        Iterator<Map.Entry<T, Double>> dimensionEntryIterator = dimension.entrySet().iterator();
        while(dimensionEntryIterator.hasNext()){
            double exp = dimensionEntryIterator.next().getValue();
            if((exp - Math.floor(exp)) < tolerance)
                dimensionEntryIterator.remove();
        }
        return dimension;
    }
    public static <T> Map<T, Double> removeDimensionItemsRaisedToZero(Map<T, Double> dimension){
        return removeDimensionItemsRaisedToZero(dimension, 0.00001);
    }
    public static <T> boolean isDimensionless(Map<T, Double> dimension){
        return removeDimensionItemsRaisedToZero(new HashMap<>(dimension)).isEmpty();
    }

    //
    public static <T> DIMENSION_TYPE determineDimensionType(Map<T, Double> dimension){
        if (dimension.size() > 1) {
            return DIMENSION_TYPE.DERIVED_MULTI;
        }
        else if (dimension.size() == 1
                && (Math.abs(dimension.entrySet().iterator().next().getValue()) > 1
                || dimension.containsValue(-1.0)
                || dimension.containsValue(0.0)))
        {
            return DIMENSION_TYPE.DERIVED_SINGLE;
        }

        return DIMENSION_TYPE.SIMPLE;
    }
    public static <T> boolean hasComplexDimensionType(Map<T, Double> dimension){
        return determineDimensionType(dimension) != DIMENSION_TYPE.SIMPLE;
    }
}
