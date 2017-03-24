package com.isaacapps.unitconverterapp.models.unitmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.isaacapps.unitconverterapp.models.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager.UNIT_TYPE;

class DataMaps {
	private Map<String, Unit> coreBaseUnitsMap;
	private Map<String, Unit> coreUnitsMap; //Store permanent frequently used units
	private Map<String, Double> corePrefixValuesMap;
	private Map<String, String> corePrefixAbbreviationsMap;
	
	private Map<String, Unit> dynamicBaseUnitsMap;
	private Map<String, Unit> dynamicUnitsMap;
	private Map<String, Double> dynamicPrefixValuesMap;
	private Map<String, String> dynamicPrefixAbbreviationsMap;
	
	private Map<String, Unit> unitsWithUnknownBaseOrUnknownFundDimensionMap;	
	
	private Map<String, Map<String, UNIT_TYPE>> fundamentalUnitsMap; //Associates a fundamental unit type with particular unitClass stored in the core or dynamic units dictionaries. The first String key is the unit system name and second internal String key is the unit name.
			      
	private Map<String, Map<String, ArrayList<String>>> unitsClassificationHierarchy; //Unit System --> Category --> Group of Units

	DataMaps(){
		coreBaseUnitsMap = new HashMap<String, Unit>();
		coreUnitsMap = new HashMap<String, Unit>();
		corePrefixValuesMap = new HashMap<String, Double>();
		corePrefixAbbreviationsMap = new HashMap<String, String>();
		
		dynamicBaseUnitsMap = new HashMap<String, Unit>();
		dynamicUnitsMap = new HashMap<String, Unit>();
		dynamicPrefixValuesMap = new HashMap<String, Double>();
		dynamicPrefixAbbreviationsMap = new HashMap<String, String>();
		
		unitsWithUnknownBaseOrUnknownFundDimensionMap = new HashMap<String, Unit>();
		
		fundamentalUnitsMap = new HashMap<String, Map<String,UNIT_TYPE>>();
		
		unitsClassificationHierarchy = new HashMap<String, Map<String,ArrayList<String>>>();
	}

	///
	public Map<String, Unit> getCoreBaseUnitsMap() {
		return coreBaseUnitsMap;
	}
	public Map<String, Unit> getCoreUnitsMap() {
		return coreUnitsMap;
	}
	public Map<String, Double> getCorePrefixValuesMap() {
		return corePrefixValuesMap;
	}
	public Map<String, String> getCorePrefixAbbreviationsMap() {
		return corePrefixAbbreviationsMap;
	}

	public Map<String, Unit> getDynamicBaseUnitsMap() {
		return dynamicBaseUnitsMap;
	}
	public Map<String, Unit> getDynamicUnitsMap() {
		return dynamicUnitsMap;
	}
	public Map<String, Double> getDynamicPrefixValuesMap() {
		return dynamicPrefixValuesMap;
	}
	public Map<String, String> getDynamicPrefixAbbreviationsMap() {
		return dynamicPrefixAbbreviationsMap;
	}

	public Map<String, Unit> getUnitsWithUnknownBaseOrUnknownFundDimensionMap() {
		return unitsWithUnknownBaseOrUnknownFundDimensionMap;
	}

	public Map<String, Map<String, UNIT_TYPE>> getFundamentalUnitsMap() {
		return fundamentalUnitsMap;
	}

	public Map<String, Map<String, ArrayList<String>>> getUnitsClassificationHierarchy() {
		return unitsClassificationHierarchy;
	}
}
