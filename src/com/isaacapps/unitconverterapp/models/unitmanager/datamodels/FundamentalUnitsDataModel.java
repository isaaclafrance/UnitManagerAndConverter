package com.isaacapps.unitconverterapp.models.unitmanager.datamodels;

import java.util.*;
import java.util.Map.Entry;

import com.isaacapps.unitconverterapp.models.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager;

//Associates a unit full name with a particular unit type within a unit system
public class FundamentalUnitsDataModel extends AbstractDataModelWithDualKeyNCategory<String, FundamentalUnitsDataModel.UNIT_TYPE, String>{
	//This only makes sense within the context of a unit manager. A unit may be of type mass in one unit manager but something else in another unit manager 
	public static enum UNIT_TYPE{MASS, LENGTH, TIME, AMT_OF_SUBSTANCE, ANGLE, TEMPERATURE, CHARGE, LUMINOUS_INTENSITY, DERIVED_SINGLE_UNIT, DERIVED_MULTI_UNIT, UNKNOWN, CURRENCY};
	private UnitManager unitManagerRef;
	
	///
	public FundamentalUnitsDataModel(){
		super(true);
	}
	
	///
	public void addFundamentalUnit(String unitSystem, String unitName, UNIT_TYPE unitType){
		addItem(unitSystem, unitName, unitName, unitType, false);
	}
	public void removeAllFundamentalUnits(){
		removeAllItems();
	}
	
	///
	public UNIT_TYPE getUnitTypeByUnitSystemNUnitName(String unitSystem, String unitName){
		return getItem(unitSystem, unitName);
	}

	///
	public boolean containsUnitSystem(String unitSystem){
		return containsCategory(unitSystem);
	}
	public boolean containsUnitNameInUnitSystem(String unitSystem, String unitName){
		return containsKeyInCategory(unitSystem, unitName);
	}
	public boolean containsUnitName(String unitName){
		return containsKey(unitName);
	}
	
	///
	public Map<UNIT_TYPE, Double> calculateFundmtUnitTypesFromCompUnitsDimension(Map<String, Double> compUnitsDimension){
		Map<UNIT_TYPE, Double> fundMap = new HashMap<UNIT_TYPE, Double>();
				
		//Goes through each component unit whether derived or and sums up the recursively obtained total occurrences of the fundamental units. Makes sure to multiply those totals by the exponent of the component unit.
		Unit componentUnit;
		for(String componentUnitName:compUnitsDimension.keySet()){
			componentUnit = unitManagerRef.getUnitsDataModel().getUnit(componentUnitName, true);
			
			if(componentUnit.getType() == UNIT_TYPE.DERIVED_MULTI_UNIT){
				Map<UNIT_TYPE, Double> recursedMap = calculateFundmtUnitTypesFromCompUnitsDimension(((componentUnit.getComponentUnitsDimension().size() == 1)?componentUnit.getBaseUnit():componentUnit)
						                                                                       .getComponentUnitsDimension());	
				for(UNIT_TYPE unitType:UNIT_TYPE.values()){	
					if(recursedMap.containsKey(unitType)){ 
						if(!fundMap.containsKey(unitType)){
							fundMap.put(unitType, 0.0);	
						}	
						
						fundMap.put(unitType,  
								fundMap.get(unitType)+ compUnitsDimension.get(componentUnitName)*recursedMap.get(unitType));					
					}				
				}
			}
			else{
				if(componentUnit.getType()!=UNIT_TYPE.DERIVED_SINGLE_UNIT){
					if(!fundMap.containsKey(componentUnit.getBaseUnit().getType())){
						fundMap.put(componentUnit.getBaseUnit().getType(), 0.0);	
					}				
					fundMap.put(componentUnit.getBaseUnit().getType(), fundMap.get(componentUnit.getBaseUnit().getType()) + compUnitsDimension.get(componentUnitName));
				}
				else{
					for(Entry<UNIT_TYPE, Double> fundEntry:componentUnit.getBaseUnit().getFundamentalUnitTypesDimension().entrySet()){
						if(!fundMap.containsKey(fundEntry.getKey())){
							fundMap.put(fundEntry.getKey(), 0.0);	
						}	
						
						fundMap.put(fundEntry.getKey(), fundMap.get(fundEntry.getKey()) +  fundEntry.getValue() * compUnitsDimension.get(componentUnitName));
					}
				}
			}
		}
		
		return fundMap;
	}
		
	///
	public void setUnitManagerRef(UnitManager unitManager){
		this.unitManagerRef = unitManager;
	}
}
