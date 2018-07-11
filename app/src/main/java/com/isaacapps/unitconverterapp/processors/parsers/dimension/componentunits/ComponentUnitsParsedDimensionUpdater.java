package com.isaacapps.unitconverterapp.processors.parsers.dimension.componentunits;

import com.isaacapps.unitconverterapp.processors.operators.dimensions.DimensionOperators;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.IParsedDimensionUpdater;

import java.util.Map;

import static com.isaacapps.unitconverterapp.models.measurables.unit.Unit.UNKNOWN_UNIT_NAME;

public class ComponentUnitsParsedDimensionUpdater implements IParsedDimensionUpdater<String> {

    @Override
    public Map<String, Double> updateDimension(String componentUnitName, double exponent, Map<String, Double> componentUnitsDimensionMap) {
        return DimensionOperators.alterExponentOfDimensionItem(componentUnitsDimensionMap, componentUnitName, exponent);
    }

    /**
     * Raises the exponent of the unknown component unit dimension item by 1.
     */
    @Override
    public Map<String, Double> updateWithUnknownDimension(Map<String, Double> componentUnitDimensionMap) {
        return DimensionOperators.alterExponentOfDimensionItem(componentUnitDimensionMap, UNKNOWN_UNIT_NAME, 1.0);
    }
}
