package com.isaacapps.unitconverterapp.processors.operators.measurables.units;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel;

import java.util.Map;

public class ChainedUnitOperation<T> {
    private Unit resultantUnit;

    ///
    public ChainedUnitOperation(Unit  initialUnit){
        this.resultantUnit = initialUnit;
    }

    ///
    public ChainedUnitOperation multiply(Unit secondUnit) {
        resultantUnit = UnitOperators.multiply(resultantUnit, secondUnit);
        return this;
    }

    public ChainedUnitOperation divide(Unit secondUnit) {
        resultantUnit = UnitOperators.divide(resultantUnit, secondUnit);
        return this;
    }

    ///
    public ChainedUnitOperation exponentiate(Double exponent){
        resultantUnit = UnitOperators.exponentiate(resultantUnit, exponent);
        return this;
    }

    ///
    public boolean equalsDimension(Unit secondUnit) {
        return UnitOperators.equalsDimension(resultantUnit, secondUnit);
    }

    public boolean equalsFundamentalUnitsDimension(Map<FundamentalUnitsDataModel.UNIT_TYPE, Double> secondFundamentalTypesDimension) {
        return UnitOperators.equalsFundamentalUnitsDimension(resultantUnit.getFundamentalUnitTypesDimension(), secondFundamentalTypesDimension);
    }

    public boolean equalsComponentUnitsDimension(Map<String, Double> secondComponentUnitsDimension) {
        return UnitOperators.equalsDeepComponentUnitsDimension(resultantUnit.getComponentUnitsDimension()
                , secondComponentUnitsDimension, resultantUnit.getUnitManagerContext());
    }

    ///

    /**
     * Unit resulting from the accumulation of all the applied unit operations
     */
    public Unit getResultantUnit() {
        return resultantUnit;
    }
}
