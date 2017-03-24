package com.isaacapps.unitconverterapp.models.unitmanager;

import java.util.ArrayList;
import java.util.Map.Entry;

import com.isaacapps.unitconverterapp.models.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager.UNIT_TYPE;

public class Converter {
	UnitManager unitManagerRef;
	
	///
	Converter(UnitManager unitManagerRef){
		this.unitManagerRef = unitManagerRef;
	}
	
	///
	public double[] getConversionFactorToTargetUnit(Unit sourceUnit, Unit targetUnit){
		double[] bCPC = new double[]{0.0,0.0};
		
		if(sourceUnit.getUnitManagerRef() == this.unitManagerRef && targetUnit.getUnitManagerRef() == this.unitManagerRef){
			if(sourceUnit.equalsDimension(targetUnit) && targetUnit.getBaseUnit().getType() != UNIT_TYPE.UNKNOWN){
				if(sourceUnit.getBaseConversionPolyCoeffs()[1]==0.0f && targetUnit.getBaseConversionPolyCoeffs()[1]==0.0){
					bCPC = new double[]{sourceUnit.getBaseConversionPolyCoeffs()[0] / targetUnit.getBaseConversionPolyCoeffs()[0], 0.0};				
				}
				else{
					bCPC = new double[]{sourceUnit.getBaseConversionPolyCoeffs()[0] / targetUnit.getBaseConversionPolyCoeffs()[0],
									  (sourceUnit.getBaseConversionPolyCoeffs()[1] - targetUnit.getBaseConversionPolyCoeffs()[1])/targetUnit.getBaseConversionPolyCoeffs()[0]};
				} 
			}else{
				bCPC = new double[]{0.0, 0.0};
			}
		}

		return bCPC; 
	}
	public double[] getConversionFactorToUnitSystem(Unit sourceUnit, String  targetUnitSystemString){		
		double[] conversionFactor = new double[]{0.0, 0.0};
		
		if(sourceUnit.getUnitManagerRef() == this.unitManagerRef){
			//Convert every component unit to one unit system. Then return conversion factor associated with this conversion. 
			if(sourceUnit.getUnitSystem().contains(targetUnitSystemString) && !sourceUnit.getUnitSystem().contains(" and ")){
				conversionFactor = new double[]{1.0, 0.0}; 
			}
			else if(!sourceUnit.getUnitSystem().contains(targetUnitSystemString) && !sourceUnit.getUnitSystem().contains(" and ") && sourceUnit.getComponentUnitsDimension().size() == 1){
				ArrayList<Unit> candidateUnitsWithProperUnitSystem = unitManagerRef.getQueryExecutor().getUnitsByUnitSystem(targetUnitSystemString);
				
				if(candidateUnitsWithProperUnitSystem.size() != 0){
					Unit matchingUnit = null;;
					for(Unit candidateUnit:candidateUnitsWithProperUnitSystem){
						if(sourceUnit.equalsFundamentalUnitsDimension(candidateUnit.getFundamentalUnitTypesDimension())){
							matchingUnit = candidateUnit;
						}
						break;
					}
					if(matchingUnit != null){
						conversionFactor = getConversionFactorToTargetUnit(sourceUnit, matchingUnit);
					}else{
						conversionFactor = new double[]{0.0, 0.0};
					}
				}else{
					conversionFactor = new double[]{0.0, 0.0};
				}
			}else{	
				if(!sourceUnit.getFundamentalUnitTypesDimension().containsKey(UNIT_TYPE.UNKNOWN)){
					Unit componentUnit;
					conversionFactor = new double[]{1.0, 0.0};					
					for(Entry<String, Double> entry:sourceUnit.getComponentUnitsDimension().entrySet()){
						componentUnit =  unitManagerRef.getQueryExecutor().getUnit(entry.getKey(), false);
						conversionFactor = new double[]{ conversionFactor[0] * (double)Math.pow(getConversionFactorToUnitSystem(componentUnit, targetUnitSystemString)[0], entry.getValue()), 0.0f};
					}
				}
			}	
		}
		
		return conversionFactor;
	}
	
}
