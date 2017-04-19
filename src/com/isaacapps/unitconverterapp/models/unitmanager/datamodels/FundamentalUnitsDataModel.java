package com.isaacapps.unitconverterapp.models.unitmanager.datamodels;

import java.util.*;

import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager.UNIT_TYPE;

public class FundamentalUnitsDataModel {
	private Map<String, Map<String, UNIT_TYPE>> fundamentalUnitsMap; //Associates a fundamental unit type with particular unit full name. The first String key is the unit system name and second internal String key is the unit name.
	
	///
	public FundamentalUnitsDataModel(){
		fundamentalUnitsMap = new HashMap<String, Map<String,UNIT_TYPE>>();
	}
	
	///
	public void addFundamentalUnit(String unitSystem, String unitName, UNIT_TYPE unitType){
		if(!fundamentalUnitsMap.containsKey(unitSystem)){
			fundamentalUnitsMap.put(unitSystem, new HashMap<String, UnitManager.UNIT_TYPE>());
		}
		fundamentalUnitsMap.get(unitSystem).put(unitName, unitType);
	}
	public void removeAllItems(){
		fundamentalUnitsMap.clear();
	}
	
	///
	public UNIT_TYPE getUnitTypeByUnitSystemNUnitName(String unitSystem, String unitName){
		return fundamentalUnitsMap.get(unitSystem).get(unitName);
	}

	///
	public boolean containsUnitSystem(String unitSystem){
		return fundamentalUnitsMap.containsKey(unitSystem);
	}
	public boolean containsUnitNameInUnitSystem(String unitSystem, String unitName){
		return fundamentalUnitsMap.containsKey(unitSystem) && fundamentalUnitsMap.get(unitSystem).containsKey(unitName);
	}
	public boolean containsUnitName(String unitName){
		for(String unitSystem:fundamentalUnitsMap.keySet()){
			if(containsUnitNameInUnitSystem(unitSystem, unitName))
				return true;
		}
		return false;
	}
	
	///
	public void combineWith(FundamentalUnitsDataModel otherDataModel){
		this.fundamentalUnitsMap.putAll(otherDataModel.fundamentalUnitsMap);
	}
}
