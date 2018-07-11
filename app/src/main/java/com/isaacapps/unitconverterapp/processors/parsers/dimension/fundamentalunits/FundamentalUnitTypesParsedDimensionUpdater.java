package com.isaacapps.unitconverterapp.processors.parsers.dimension.fundamentalunits;

import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel.UNIT_TYPE;
import com.isaacapps.unitconverterapp.processors.operators.dimensions.DimensionOperators;
import com.isaacapps.unitconverterapp.processors.parsers.ParsingException;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.IParsedDimensionUpdater;

import java.util.Map;

public class FundamentalUnitTypesParsedDimensionUpdater implements IParsedDimensionUpdater<FundamentalUnitsDataModel.UNIT_TYPE> {
    @Override
    public Map<UNIT_TYPE, Double> updateDimension(String fundamentalUnitTypeString, double exponent
            , Map<UNIT_TYPE, Double> fundamentalUnitsDimensionMap) throws ParsingException {
        try {
            return DimensionOperators.alterExponentOfDimensionItem(fundamentalUnitsDimensionMap, UNIT_TYPE.valueOf(fundamentalUnitTypeString), exponent);
        } catch (IllegalArgumentException e) {
            throw new ParsingException(String.format("Failed to convert %s to enum UNIT_TYPE", fundamentalUnitTypeString
            ), "Change token to exactly match a UNIT_TYPE enum. ie. LENGTH, MASS, etc.");
        }
    }

    /**
     * Raises the exponent of the unknown component unit dimension item by 1.
     */
    @Override
    public Map<UNIT_TYPE, Double> updateWithUnknownDimension(Map<UNIT_TYPE, Double> fundamenalUnitsDimensionMap) {
        return DimensionOperators.alterExponentOfDimensionItem(fundamenalUnitsDimensionMap, UNIT_TYPE.UNKNOWN, 1.0);
    }
}
