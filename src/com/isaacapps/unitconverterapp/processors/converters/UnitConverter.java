package com.isaacapps.unitconverterapp.processors.converters;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.ContentDeterminer;
import com.isaacapps.unitconverterapp.processors.converters.algorithms.AffineConversionAlgorithm;
import com.isaacapps.unitconverterapp.processors.converters.algorithms.NewtonRaphsonConversionAlgorithm;

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
            if(ContentDeterminer.determineGeneralDataModelCategory(sourceUnit) != ContentDeterminer.DATA_MODEL_CATEGORY.UNKNOWN){
                return sourceUnit.getUnitManagerContext().getFundamentalUnitsDataModel()
                        .transformFundamentalUnitsDimensionToComponentUnitDimension(sourceUnit.getFundamentalUnitTypesDimension(), targetUnitSystem);
            }
            else{
               return sourceUnit.getUnitManagerContext().getUnitsDataModel().getContentMainRetriever().getUnknownUnit().getComponentUnitsDimension();
            }
        }
        else{
            return new Unit().getComponentUnitsDimension();
        }
    }

    public static Unit determineConversionUnitToTargetUnitSystem(Unit sourceUnit, String targetUnitSystem) {
        UnitManager unitManagerContext = sourceUnit.getUnitManagerContext();

        Map<String, Double> dimensionInTargetUnitSystem = calculateDimensionTransformedToTargetUnitSystem(sourceUnit, targetUnitSystem);

        try {
            if(unitManagerContext != null){
                return unitManagerContext.getUnitsDataModel().getContentMainRetriever().getUnit(sourceUnit.getComponentUnitsDimensionSerializer()
                        .serialize(dimensionInTargetUnitSystem), true);
            }
            else{
                return new Unit(dimensionInTargetUnitSystem, false);
            }
        } catch (Exception e) {
            return unitManagerContext != null ? unitManagerContext.getUnitsDataModel().getContentMainRetriever().getUnknownUnit() : new Unit();
        }
    }

    public static double[] calculateConversionCoeffsToTargetUnit(Double sourceValue, Unit sourceUnit, Unit targetUnit) {
        if (sourceUnit.getUnitManagerContext() == targetUnit.getUnitManagerContext() && equalsDimension(sourceUnit, targetUnit)) {
            if (sourceUnit.getBaseConversionExpression() != null && targetUnit.getBaseConversionExpression() != null) {
                return new AffineConversionAlgorithm().calculateConversionCoeffsToTargetUnit(sourceValue, sourceUnit, targetUnit);
            } else {
                return new NewtonRaphsonConversionAlgorithm().calculateConversionCoeffsToTargetUnit(sourceValue, sourceUnit, targetUnit);
            }
        } else {
            return new double[2];
        }
    }
}
