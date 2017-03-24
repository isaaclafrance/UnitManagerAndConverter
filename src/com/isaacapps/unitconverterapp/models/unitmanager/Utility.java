package com.isaacapps.unitconverterapp.models.unitmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.isaacapps.unitconverterapp.models.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager.UNIT_TYPE;

public class Utility {
	private DataMaps dataMaps;
	private QueryExecutor queryExecutor;
	
	///
	Utility(DataMaps dataMaps, QueryExecutor queryExecutor){
		this.dataMaps = dataMaps;
		this.queryExecutor = queryExecutor;
	}
	
	///
	public Unit getBaseUnit(Unit unit){
        //Tries to match the fundamental unit with one unit in the dictionary. 
		ArrayList<Unit> unitMatches = queryExecutor.getUnitsWithMatchingFundamentalUnitDimension(unit);			
		if(unitMatches.size() > 0){
			for(Unit unitMatch:unitMatches){
				if(unitMatch.isBaseUnit() && !unitMatch.getName().equalsIgnoreCase(unit.getName())){
					return unitMatch;
				}
			}
		}
		
		return queryExecutor.getUnit(Unit.UNKNOWN_UNIT_NAME);
	}	
	public Unit getReducedUnitMatch(Unit unit){
		ArrayList<Unit> matchingUnits = queryExecutor.getUnitsByFundamentalUnitTypeDimension(unit.getFundamentalUnitTypesDimension());
		
		//If result set is large enough sort from least to greatest component unit size, then get the unit with the smallest dimension
		if(matchingUnits.size() > 0){
			Collections.sort(matchingUnits, new Comparator<Unit>() {
				@Override
				public int compare(Unit lhsUnit, Unit rhsUnit) {
					return Double.compare(lhsUnit.getComponentUnitsDimension().size(), rhsUnit.getComponentUnitsDimension().size());
				}
			});
			
			return matchingUnits.get(0);
		}
		else{
			return queryExecutor.getUnit(Unit.UNKNOWN_UNIT_NAME);
		}
	}	
	
	public Map<UNIT_TYPE, Double> calculateFundmtUnitTypesFromCompUnitsDimensionMap(Map<String, Double> compUnitsDimensionMap){
		Map<UNIT_TYPE, Double> fundMap = new HashMap<UnitManager.UNIT_TYPE, Double>();
				
		//Goes through each component unit whether derived or and sums up the recursively obtained total occurances of the fundamental units. Makes sure to multiply those totals by the exponent of the component unit.
		Unit componentUnit;
		for(String componentUnitName:compUnitsDimensionMap.keySet()){
			componentUnit = queryExecutor.getUnit(componentUnitName, true);
			
			if(componentUnit.getType() == UNIT_TYPE.DERIVED_MULTI_UNIT){
				Map<UNIT_TYPE, Double> recursedMap = calculateFundmtUnitTypesFromCompUnitsDimensionMap(((componentUnit.getComponentUnitsDimension().size() == 1)?componentUnit.getBaseUnit():componentUnit)
						                                                                       .getComponentUnitsDimension());	
				for(UNIT_TYPE unitType:UNIT_TYPE.values()){	
					if(recursedMap.containsKey(unitType)){ 
						if(!fundMap.containsKey(unitType)){
							fundMap.put(unitType, 0.0);	
						}				
						fundMap.put(unitType,  
								fundMap.get(unitType)+ compUnitsDimensionMap.get(componentUnitName)*recursedMap.get(unitType));					
					}				
				}
			}
			else{
				if(componentUnit.getType()!=UNIT_TYPE.DERIVED_SINGLE_UNIT){
					if(!fundMap.containsKey(componentUnit.getBaseUnit().getType())){
						fundMap.put(componentUnit.getBaseUnit().getType(), 0.0);	
					}				
					fundMap.put(componentUnit.getBaseUnit().getType(), fundMap.get(componentUnit.getBaseUnit().getType()) + compUnitsDimensionMap.get(componentUnitName));
				}
				else{
					for(Entry<UNIT_TYPE, Double> fundEntry:componentUnit.getBaseUnit().getFundamentalUnitTypesDimension().entrySet()){
						if(!fundMap.containsKey(fundEntry.getKey())){
							fundMap.put(fundEntry.getKey(), 0.0);	
						}	
						
						fundMap.put(fundEntry.getKey(), fundMap.get(fundEntry.getKey()) +  fundEntry.getValue() * compUnitsDimensionMap.get(componentUnitName));
					}
				}
			}
		}
		
		return fundMap;
	}
	public UNIT_TYPE determineUnitType(Unit unit){
		UNIT_TYPE type;
		
		if(unit.getComponentUnitsDimension().size()>1){
			type = UNIT_TYPE.DERIVED_MULTI_UNIT;
		}
		else if(unit.getComponentUnitsDimension().size() == 1 
				&&(Math.abs(unit.getComponentUnitsDimension().entrySet().iterator().next().getValue()) > 1 
					       || unit.getComponentUnitsDimension().entrySet().iterator().next().getValue() == -1
					       || unit.getComponentUnitsDimension().entrySet().iterator().next().getValue() == 0)){
			type = UNIT_TYPE.DERIVED_SINGLE_UNIT;
		}
		else if(unit.getBaseUnit() != null){
			if(dataMaps.getFundamentalUnitsMap().keySet().contains(unit.getBaseUnit().getUnitSystem()) 
			   && dataMaps.getFundamentalUnitsMap().get(unit.getBaseUnit().getUnitSystem()).containsKey(unit.getBaseUnit().getName())){
				
				type = dataMaps.getFundamentalUnitsMap().get(unit.getBaseUnit().getUnitSystem()).get(unit.getBaseUnit().getName());
			}
			else{
				type = unit.getBaseUnit().getType();
			}
		}
		else{
			type = UNIT_TYPE.UNKNOWN;
		}
		
		return type;
	}
	
	public static Map<String, Double> getComponentUnitsDimensionFromString(String componentUnitsDimensionString){
		return getDimensionFromString(componentUnitsDimensionString, Pattern.compile("(([a-zA-Z]+[\\-])?([a-zA-Z_]+))"), Pattern.compile("[-+]?(\\d*[.])?\\d+")
                , Pattern.compile("([\\*|\\/]?[\\s]*)((([\\(]?[\\s]*(([a-zA-Z]+[\\-])?([a-zA-Z_]+))[\\s]*[\\)]?)([\\s]*\\^[\\s]*([\\(]?[\\s]*[-+]?(\\d*[.])?\\d+[\\s]*[\\)]?)))|(([a-zA-Z]+[\\-])?([a-zA-Z_]+)))")
                , new ComponentUnitsDimensionUpdater(), new HashMap<String, Double>());
	}
	public static Map<UNIT_TYPE, Double> getFundamentalUnitTypesDimensionFromString(String fundamentalUnitTypesDimension){
		return getDimensionFromString(fundamentalUnitTypesDimension, Pattern.compile("[a-zA-Z]+"), Pattern.compile("[-+]?(\\d*[.])?\\d+")
				                      , Pattern.compile("([\\*|\\/]?[\\s]*)((([\\(]?[\\s]*[a-zA-Z]+[\\s]*[\\)]?)([\\s]*\\^[\\s]*([\\(]?[\\s]*[-+]?(\\d*[.])?\\d+[\\s]*[\\)]?)))|[a-zA-Z]+)")
				                      , new FundamentalUnitTypesDimensionUpdater(), new HashMap<UNIT_TYPE, Double>());
	}
	
	public static <T> Map<T, Double> getDimensionFromString(String dimensionString, Pattern typeRegExPattern, Pattern exponentRegExPattern, Pattern groupRegExPattern
			      ,DimensionUpdater dimensionUpdater, Map<T, Double> dimensionMap){	

		//RegEx pattern to parse string into groups of unit type and exponents.
		Matcher groupRegExMatcher = groupRegExPattern.matcher(dimensionString);
		
		//Perform extraction using RegEx patterns
		if(groupRegExMatcher.find()){
			do{
				String group = groupRegExMatcher.group();

				Matcher typeRegExMatcher = typeRegExPattern.matcher(group);
				typeRegExMatcher.find();
				String type = typeRegExMatcher.group();
				type.toLowerCase();
				
				Matcher exponentRegExMatcher = exponentRegExPattern.matcher(group);
				String exponent;
				if(exponentRegExMatcher.find()){
					exponent = exponentRegExMatcher.group();
				}else{
					exponent = "1.0"; //If the unit type is not raised by any number then it assumed to be raised by 1.
				}

			 	dimensionUpdater.updateDimension(type, exponent, group, dimensionMap);
			}while(groupRegExMatcher.find());
		}
		else{
			dimensionUpdater.updateWithUnknownDimension(dimensionMap);
		}
		return dimensionMap;
	}
	
	//TODO: Mini modified version of the Command Design Pattern. Upgrade from Java 7 to Java 8 in order to be make use of lambda functions for less verbosity when using methods a parameters to pass behavior.
	private interface DimensionUpdater{
		public <T> void updateDimension(String type, String exponent, String group, Map<T, Double> dimensionMap);
		public <T> void updateWithUnknownDimension(Map<T, Double> dimensionMap);
	}
	public static class ComponentUnitsDimensionUpdater implements DimensionUpdater{

		@Override
		public <T> void updateDimension(String componentUnitName, String exponent, String group,
				Map<T, Double> componentUnitsDimensionMap) {
			updateDimensionMap((T)componentUnitName, exponent, group, componentUnitsDimensionMap);
		}	
		@Override
		public <T> void updateWithUnknownDimension(Map<T, Double> dimensionMap){
			dimensionMap.put((T) Unit.UNKNOWN_UNIT_NAME, 1.0);
		}
	}
	public static class FundamentalUnitTypesDimensionUpdater implements DimensionUpdater{

		@Override
		public <T> void updateDimension(String fundamentalUnitType, String exponent, String group,
				Map<T, Double> fundamentalUnitsDimensionMap) {
			T unitType = (T) UNIT_TYPE.valueOf(fundamentalUnitType);					
			updateDimensionMap(unitType, exponent, group, fundamentalUnitsDimensionMap);
		}		
		@Override
		public <T> void updateWithUnknownDimension(Map<T, Double> dimensionMap){
			dimensionMap.put((T) UNIT_TYPE.UNKNOWN, 1.0);
		}
	}
	private static <T> void updateDimensionMap(T type, String exponent, String group, Map<T, Double> dimensionMap){
		if(dimensionMap.containsKey(type)){
			dimensionMap.put(type, Double.valueOf(exponent) + ((group.charAt(0) == '/')?-dimensionMap.get(type):dimensionMap.get(type)));	
		}else{
			dimensionMap.put(type, (group.charAt(0) == '/'?-1:1)*Double.valueOf(exponent));						
		}
	}
}
