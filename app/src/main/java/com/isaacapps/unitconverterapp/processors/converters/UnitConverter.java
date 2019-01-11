package com.isaacapps.unitconverterapp.processors.converters;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsContentDeterminer;
import com.isaacapps.unitconverterapp.processors.converters.algorithms.AffineConversionAlgorithm;
import com.isaacapps.unitconverterapp.processors.converters.algorithms.NewtonRaphsonConversionAlgorithm;

import java.util.Collections;
import java.util.Map;

import static com.isaacapps.unitconverterapp.models.measurables.unit.Unit.UNIT_SYSTEM_DELIMITER;
import static com.isaacapps.unitconverterapp.processors.operators.measurables.units.UnitOperators.equalsDimension;

public class UnitConverter {

    ///
    private static Map<String, Double> calculateDimensionTransformedToTargetUnitSystem(Unit sourceUnit
            , String targetUnitSystem) {

        if (sourceUnit.getUnitSystem().contains(targetUnitSystem)
                && !sourceUnit.getUnitSystem().contains(UNIT_SYSTEM_DELIMITER)) {
            return sourceUnit.getComponentUnitsDimension();
        }

        if(sourceUnit.getUnitManagerContext() != null){
            if(UnitsContentDeterminer.determineHighestPriorityDataModelCategory(sourceUnit) != UnitsContentDeterminer.DATA_MODEL_CATEGORY.UNKNOWN){
                return sourceUnit.getUnitManagerContext().getFundamentalUnitsDataModel()
                        .transformFundamentalUnitsDimensionToComponentUnitDimension(sourceUnit.getFundamentalUnitTypesDimension(), targetUnitSystem);
            }
            else{
               return sourceUnit.getUnitManagerContext().getUnitsDataModel().getUnitsContentMainRetriever().getUnknownUnit().getComponentUnitsDimension();
            }
        }
        else{
            try {
                return new Unit().getComponentUnitsDimension();
            } catch (Exception e) {
                return Collections.emptyMap();
            }
        }
    }

    public static Unit determineConversionUnitToTargetUnitSystem(Unit sourceUnit, String targetUnitSystem) {
        UnitManager unitManagerContext = sourceUnit.getUnitManagerContext();

        Map<String, Double> dimensionInTargetUnitSystem = calculateDimensionTransformedToTargetUnitSystem(sourceUnit, targetUnitSystem);

        try {
            if(unitManagerContext != null){
                return unitManagerContext.getUnitsDataModel().getUnitsContentMainRetriever().getUnit(sourceUnit.getComponentUnitsDimensionSerializer()
                        .serialize(dimensionInTargetUnitSystem), false);
            }
            else{
                return new Unit(dimensionInTargetUnitSystem);
            }
        } catch (Exception e) {
            return unitManagerContext != null ? unitManagerContext.getUnitsDataModel().getUnitsContentMainRetriever().getUnknownUnit() : null;
        }
    }

    public static double[] calculateConversionCoeffsToTargetUnit(Double sourceValue, Unit sourceUnit, Unit targetUnit) {
        if (sourceUnit.getUnitManagerContext() == targetUnit.getUnitManagerContext() && equalsDimension(sourceUnit, targetUnit)) {
            if (!sourceUnit.isUsingBaseConversionExpression() && !targetUnit.isUsingBaseConversionExpression()) {
                return new AffineConversionAlgorithm().calculateConversionCoeffsToTargetUnit(sourceValue, sourceUnit, targetUnit);
            } else {
                return new NewtonRaphsonConversionAlgorithm().calculateConversionCoeffsToTargetUnit(sourceValue, sourceUnit, targetUnit);
            }
        } else {
            return new double[2];
        }
    }
}
