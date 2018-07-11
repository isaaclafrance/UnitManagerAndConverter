package com.isaacapps.unitconverterapp.processors.parsers.dimension;

import com.isaacapps.unitconverterapp.processors.parsers.ParsingException;

import java.util.Map;

/**
 * Interface contract that specifies how a generic dimension updater can process dimension maps during the parsing process.
 *
 * @param <T> Dimension Type
 */
public interface IParsedDimensionUpdater<T> {
    Map<T, Double> updateDimension(String type, double exponent, Map<T, Double> dimensionMap) throws ParsingException;

    Map<T, Double> updateWithUnknownDimension(Map<T, Double> dimensionMap);
}
