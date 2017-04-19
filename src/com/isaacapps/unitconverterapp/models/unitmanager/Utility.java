package com.isaacapps.unitconverterapp.models.unitmanager;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.isaacapps.unitconverterapp.models.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager.UNIT_TYPE;

public class Utility {
	private UnitManager unitManagerRef;
	
	///
	Utility(){}
		
	///
	public Map<UNIT_TYPE, Double> calculateFundmtUnitTypesFromCompUnitsDimension(Map<String, Double> compUnitsDimension){
		Map<UNIT_TYPE, Double> fundMap = new HashMap<UnitManager.UNIT_TYPE, Double>();
				
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

	///TODO: Modified version of the Command Design Pattern using generic programming. Need to upgrade from Java 7 to Java 8 in order to be make use of lambda functions for less verbosity when using methods as parameters to pass behavior.
	public static Map<String, Double> getComponentUnitsDimensionFromString(String componentUnitsDimension){
		//Format: a, prefix'a', (a)^(+-#.##) * or / (b)^(+-#.##), where 'a' and 'b' are word characters. Prefix can only alphabetical. Accounts for the optional presence of parenthesis and discounts the number of white space characters. 
		return getDimensionFromString(componentUnitsDimension, Pattern.compile("(([a-zA-Z]+)?(\\w+))"), Pattern.compile("[-+]?(\\d*[.])?\\d+")
                , Pattern.compile("([\\*|\\/]?[\\s]*)((([\\(]?[\\s]*(([a-zA-Z]+)?(\\w+))[\\s]*[\\)]?)([\\s]*\\^[\\s]*([\\(]?[\\s]*[-+]?(\\d*[.])?\\d+[\\s]*[\\)]?)))|(([a-zA-Z]+)?(\\w+)))")
                , '/'
                , new ComponentUnitsDimensionUpdater(), new HashMap<String, Double>());
	}
	public static Map<UNIT_TYPE, Double> getFundamentalUnitTypesDimensionFromString(String fundamentalUnitTypesDimension){
		return getDimensionFromString(fundamentalUnitTypesDimension, Pattern.compile("[a-zA-Z]+"), Pattern.compile("[-+]?(\\d*[.])?\\d+")
				                      , Pattern.compile("([\\*|\\/]?[\\s]*)((([\\(]?[\\s]*[a-zA-Z]+[\\s]*[\\)]?)([\\s]*\\^[\\s]*([\\(]?[\\s]*[-+]?(\\d*[.])?\\d+[\\s]*[\\)]?)))|[a-zA-Z]+)")
				                      , '/'
				                      , new FundamentalUnitTypesDimensionUpdater(), new HashMap<UNIT_TYPE, Double>());
	}
	
	public static String getComponentUnitsDimensionAsString(Map<String, Double> componentUnitsDimension){
		return getDimensionAsString(componentUnitsDimension, new ComponentUnitsDimensionUpdater());
	}
	public static String getFundamentalUnitTypesDimensionAsString(Map<UNIT_TYPE, Double> fundamentalUnitTypesDimension){
		return getDimensionAsString(fundamentalUnitTypesDimension, new FundamentalUnitTypesDimensionUpdater());
	}
	
	public static <T> Map<T, Double> getDimensionFromString(String dimensionString, Pattern typeRegExPattern, Pattern exponentRegExPattern, Pattern groupRegExPattern
			      ,char divisionSymbol, DimensionUpdater<T> dimensionUpdater, Map<T, Double> dimensionMap){	

		//RegEx pattern to parse string into groups of unit type and exponents. The pattern must account for the multiplication symbol being the first item in the group
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

			 	dimensionUpdater.updateDimension(type, exponent, group, divisionSymbol, dimensionMap);
			}while(groupRegExMatcher.find());
		}
		else{
			dimensionUpdater.updateWithUnknownDimension(dimensionMap);
		}
		return dimensionMap;
	}
	public static <T> String getDimensionAsString(Map<T, Double> dimensionMap, DimensionUpdater<T> dimensionUpdater){
		String dimensionString = "";
		String dimensionEntryKeyName;

		for(Entry<T, Double> dimensionEntry:dimensionMap.entrySet()){
			dimensionString = dimensionString.equals("")? dimensionString:dimensionString+" * ";
			
			dimensionEntryKeyName = dimensionUpdater.dimensionToString(dimensionEntry.getKey());
			
			if(dimensionEntry.getValue() == 1){
				dimensionString += dimensionEntryKeyName;
			}
			else if(Math.abs(dimensionEntry.getValue())>0){ //Remove calculated dimensions raised to zero. 
				dimensionString += "("+dimensionEntryKeyName+")^"+"("+dimensionEntry.getValue()+")";
			}
		}
		
		return dimensionString;
	}
	
	private interface DimensionUpdater<T>{
		public void updateDimension(String type, String exponent, String group, char divisionSymbol, Map<T, Double> dimensionMap);
		public void updateWithUnknownDimension(Map<T, Double> dimensionMap);
		public String dimensionToString(T dimensionItem);
	}
	public static class ComponentUnitsDimensionUpdater implements DimensionUpdater<String>{

		@Override
		public void updateDimension(String componentUnitName, String exponent, String group, char divisionSymbol,
				Map<String, Double> componentUnitsDimensionMap) {
			updateDimensionMap(componentUnitName, exponent, group, divisionSymbol, componentUnitsDimensionMap);
		}	
		@Override
		public void updateWithUnknownDimension(Map<String, Double> dimensionMap){
			dimensionMap.put(Unit.UNKNOWN_UNIT_NAME, 1.0);
		}
		@Override
		public String dimensionToString(String dimensionItem){
			return dimensionItem;
		}
	}
	public static class FundamentalUnitTypesDimensionUpdater implements DimensionUpdater<UNIT_TYPE>{

		@Override
		public void updateDimension(String fundamentalUnitType, String exponent, String group, char divisionSymbol,
				Map<UNIT_TYPE, Double> fundamentalUnitsDimensionMap) {					
			updateDimensionMap(UNIT_TYPE.valueOf(fundamentalUnitType), exponent, group,divisionSymbol, fundamentalUnitsDimensionMap);
		}		
		@Override
		public void updateWithUnknownDimension(Map<UNIT_TYPE, Double> dimensionMap){
			dimensionMap.put(UNIT_TYPE.UNKNOWN, 1.0);
		}
		@Override
		public String dimensionToString(UNIT_TYPE dimensionItem){
			return dimensionItem.name();
		}
	}
	
	private static <T> void updateDimensionMap(T type, String exponent, String group, char divisionSymbol, Map<T, Double> dimensionMap){
		if(dimensionMap.containsKey(type)){
			dimensionMap.put(type, Double.valueOf(exponent) + ((group.charAt(0) == divisionSymbol)?-dimensionMap.get(type):dimensionMap.get(type)));	
		}else{
			dimensionMap.put(type, (group.charAt(0) == divisionSymbol?-1:1)*Double.valueOf(exponent));						
		}
	}
	
	///
	public static boolean unitNameHasComplexDimensions(String unitName){
		return unitName.contains("*") || unitName.contains("/") || unitName.contains("^");
	}
	
	///
	void setUnitManagerRef(UnitManager unitManagerRef){
		this.unitManagerRef = unitManagerRef; 
	}
}
