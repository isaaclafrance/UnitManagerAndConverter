package com.isaacapps.unitconverterapp.utilities;

import static com.isaacapps.unitconverterapp.utilities.UnitRegExUtility.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.isaacapps.unitconverterapp.models.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel.UNIT_TYPE;

public final class UnitDimensionProcessingUtility {
	
	///TODO: Modified version of the Command Design Pattern using generic programming. Need to upgrade from Java 7 to Java 8 in order to be make use of independent lambda functions for less verbosity when passing around behavior.
	public static Map<String, Double> getComponentUnitsDimensionFromString(String componentUnitsDimension, boolean strictParsing) throws ParsingException{
		//Format: a, prefix'a', (a)^+-#.##  (* or /) (b)^+-#.##, where 'a' and 'b' are word characters. Discounts the number of white space characters between operators, words, and numbers. Can be nested with parenthesis.
		//Multiplication or division symbol can optionally appear as first items in the group.
		return getDimensionFromString(componentUnitsDimension, Pattern.compile(UNIT_NAME_REGEX), Pattern.compile(EXPONENT_REGEX+"$"), Pattern.compile(SIGNED_DOUBLE_VALUE_REGEX)
				, getMultiGroupRegExPattern(UNIT_NAME_REGEX, EXPONENT_REGEX, DIVISION_SYMBOL, MULTIPLICTION_SYMBOL)
				, getSingleGroupRegExPattern(UNIT_NAME_REGEX, DIVISION_SYMBOL, MULTIPLICTION_SYMBOL)
                , DIVISION_SYMBOL
                , new ComponentUnitsDimensionUpdater()
                , new HashMap<String, Double>()
                , strictParsing);
	}
	public static Map<UNIT_TYPE, Double> getFundamentalUnitTypesDimensionFromString(String fundamentalUnitTypesDimension, boolean strictParsing) throws ParsingException{
		//Format: a, prefix'a', (a)^+-#.##  (* or /) (b)^+-#.##, where 'a' and 'b' are word characters. Discounts the number of white space characters between operators, words, and numbers. Can be nested with parenthesis.
		//Multiplication or division symbol can optionally appear as first items in the group.
		return getDimensionFromString(fundamentalUnitTypesDimension, Pattern.compile(UNIT_TYPE_REGEX), Pattern.compile(EXPONENT_REGEX+"$"), Pattern.compile(SIGNED_DOUBLE_VALUE_REGEX)
				                      , getMultiGroupRegExPattern(UNIT_TYPE_REGEX, EXPONENT_REGEX, DIVISION_SYMBOL, MULTIPLICTION_SYMBOL)
				                      , getSingleGroupRegExPattern(UNIT_TYPE_REGEX, DIVISION_SYMBOL, MULTIPLICTION_SYMBOL)
				                      , DIVISION_SYMBOL
				                      , new FundamentalUnitTypesDimensionUpdater()
				                      , new HashMap<UNIT_TYPE, Double>()
				                      , strictParsing);
	}
	
	public static String getComponentUnitsDimensionAsString(Map<String, Double> componentUnitsDimension){
		return getDimensionAsString(componentUnitsDimension, new ComponentUnitsDimensionUpdater());
	}
	public static String getFundamentalUnitTypesDimensionAsString(Map<UNIT_TYPE, Double> fundamentalUnitTypesDimension){
		return getDimensionAsString(fundamentalUnitTypesDimension, new FundamentalUnitTypesDimensionUpdater());
	}
	
	/**
	 * Implements a recursive structure that parses nested unit groups bounded by parenthesis in order to properly update exponents of unit items. 
	 * @throws ParsingException 
	 */
	private static <T> Map<T, Double> getDimensionFromString(String dimensionString, double recursedExponent, Pattern typeRegExPattern, Pattern exponentRegExPattern, Pattern exponentValueRegExPattern
			      , Pattern multiGroupRegExPattern, Pattern singleGroupRegExPattern, String divisionSymbol, DimensionUpdater<T> dimensionUpdater, Map<T, Double> dimensionMap
			      , boolean strictParsing) throws ParsingException{	
		
		//
		dimensionString = dimensionString.trim();
		
		if(dimensionString.isEmpty()){
			if(strictParsing){
				throw new ParsingException("Nothing was parsed.", "Make dimension non-empty by included well formatted content.");
			}
			else{
				dimensionUpdater.updateWithUnknownDimension(dimensionMap);
				return dimensionMap;
			}
		}
	
		//
		String truncatedDimensionString = dimensionString; // During the recursion process matching multigroups will be successively excised from the dimension string.
		double currentExponent = 1; //If the unit type is not raised by any number then it is assumed to be raised by 1.
		HashSet<String> multiGroupsToBeExcised = new HashSet<String>();
		
		Matcher exponentRegExMatcher;
		Matcher exponentValueRegExMatcher;
		
		//Process all multi groups first
		Matcher multiGroupRegExMatcher = multiGroupRegExPattern.matcher(dimensionString);
		if(multiGroupRegExMatcher.find()){			
			do{
				String multiGroup = multiGroupRegExMatcher.group();

				exponentRegExMatcher = exponentRegExPattern.matcher(multiGroup);
				String exponentGroup = "";
				
				if(exponentRegExMatcher.find()){
					exponentGroup = exponentRegExMatcher.group();
					exponentValueRegExMatcher = exponentValueRegExPattern.matcher(exponentGroup);
					if(exponentValueRegExMatcher.find()){
						//The absence of any symbol can also be regarded as multiplication.
						currentExponent = (multiGroup.substring(0, 1).equals(divisionSymbol)?-1:+1) * Double.valueOf(exponentValueRegExMatcher.group());
					}		
				}
					
				//Tries to recursively find nested groups and if such groups are found then exponents are properly accumulated and successively passed on.
				//Force a new nested multigroup search to occur in preparation for possible recursion.
				Matcher recursedMultiGroupRegExMatcher = multiGroupRegExPattern.matcher(multiGroup.replaceFirst("\\"+exponentGroup+"$", "")
						                                                                 .replaceFirst("^[^\\(]*\\(","").replaceFirst("\\)$", ""));
				while(recursedMultiGroupRegExMatcher.find()){
					String multiGroupToBeRecursed = recursedMultiGroupRegExMatcher.group(); 
					dimensionMap = getDimensionFromString(multiGroupToBeRecursed
							                        , recursedExponent * currentExponent
							                        , typeRegExPattern, exponentRegExPattern
							                        , exponentValueRegExPattern, multiGroupRegExPattern
							                        , singleGroupRegExPattern
							                        , divisionSymbol, dimensionUpdater, dimensionMap, strictParsing);
					multiGroupsToBeExcised.add(multiGroup);
				}
			}while(multiGroupRegExMatcher.find());
			
			for(String exciseMultiGroup:multiGroupsToBeExcised){
				truncatedDimensionString = truncatedDimensionString.replace(exciseMultiGroup, "");	
			}
		}

		//After all multi groups have been dealt with next deal with single groups
		Matcher singleGroupRegExMatcher = singleGroupRegExPattern.matcher(truncatedDimensionString);
		if(singleGroupRegExMatcher.find()){
			String singleGroup = singleGroupRegExMatcher.group();
			
			Matcher typeRegExMatcher = typeRegExPattern.matcher(singleGroup);
			typeRegExMatcher.find();
			String type = typeRegExMatcher.group().toLowerCase();

			//if multigroups were excised, then use outer recursed exponent since currentExponent attains the value of the most recent multi group that was excised.
			//But only do so if a simple truncate dimension string was left after the excising. Otherwise just use the currentExponent value		
			Double singleGroupExponent = 1.0;
			if(!multiGroupsToBeExcised.isEmpty()){
				exponentRegExMatcher = exponentRegExPattern.matcher(truncatedDimensionString);
				if(exponentRegExMatcher.find()){
					exponentValueRegExMatcher = exponentValueRegExPattern.matcher(truncatedDimensionString);
					if(exponentValueRegExMatcher.find()){
						//The absence of any symbol can also be regarded as multiplication.
						singleGroupExponent = (truncatedDimensionString.substring(0, 1).equals(divisionSymbol)?-1:+1) * Double.valueOf(exponentValueRegExMatcher.group());
					}
				}
				else{
					singleGroupExponent = recursedExponent;
				}
			}
			else{
				singleGroupExponent = currentExponent;
			}
			
			dimensionUpdater.updateDimension(type, recursedExponent*singleGroupExponent
					         , singleGroup, divisionSymbol, dimensionMap);
		}
		else{
			if(strictParsing){
				throw new ParsingException(truncatedDimensionString, "Change to fit regular expression requirement: "+singleGroupRegExPattern.pattern());
			}
			else{
				dimensionUpdater.updateWithUnknownDimension(dimensionMap);
			}	
		}
		return dimensionMap;
	}
	public static <T> Map<T, Double> getDimensionFromString(String dimensionString, Pattern typeRegExPattern, Pattern exponentRegExPattern, Pattern exponentValueRegExPattern
			, Pattern multiGroupRegExPattern, Pattern singleGroupRegExPattern, String divisionSymbol, DimensionUpdater<T> dimensionUpdater, Map<T, Double> dimensionMap
			, boolean strictParsing) throws ParsingException{
		if(hasBalancedParanethesis(dimensionString)){
			return getDimensionFromString(dimensionString, 1.0, typeRegExPattern, exponentRegExPattern, exponentValueRegExPattern
					, multiGroupRegExPattern, singleGroupRegExPattern, divisionSymbol, dimensionUpdater, dimensionMap, strictParsing);
		}
		else{
			//Having balanced parenthesis is critical to proper parsing and must always throw an exception.
			throw new ParsingException(dimensionString, "Make sure the number of open paranethesis equals the number of closing paranethesis.");
		}
	}
	public static <T> Map<T, Double> getDimensionFromString(String dimensionString, String typeRegEx, String exponentRegEx, String exponentValueRegEx
			, String divisionSymbol, String multiplicationSymbol, DimensionUpdater<T> dimensionUpdater, Map<T, Double> dimensionMap
			, boolean strictParsing) throws ParsingException{
		if(hasBalancedParanethesis(dimensionString)){
			return getDimensionFromString(dimensionString, 1.0, Pattern.compile(typeRegEx)
					, Pattern.compile(exponentRegEx)
					, Pattern.compile(exponentValueRegEx)
					, getMultiGroupRegExPattern(typeRegEx, exponentRegEx, divisionSymbol, multiplicationSymbol)
					, getSingleGroupRegExPattern(typeRegEx, divisionSymbol, multiplicationSymbol)
					, divisionSymbol, dimensionUpdater, dimensionMap, strictParsing);
		}	
		else{
			//Having balanced parenthesis is critical to proper parsing and must always throw an exception.
			throw new ParsingException(dimensionString, "Make sure the number of open paranethesis equals the number of closing paranethesis.");
		}
	}
	
	public static <T> String getDimensionAsString(Map<T, Double> dimensionMap, String multiplicationSymbol, DimensionUpdater<T> dimensionUpdater){
		String dimensionString = "";
		String dimensionEntryKeyName;

		for(Entry<T, Double> dimensionEntry:dimensionMap.entrySet()){
			dimensionString = dimensionString + (dimensionString.equals("")? "":" "+multiplicationSymbol+" ");
			
			dimensionEntryKeyName = dimensionUpdater.dimensionItemToString(dimensionEntry.getKey());
			
			if(dimensionEntry.getValue() == 1){
				dimensionString += dimensionEntryKeyName;
			}
			else if(Math.abs(dimensionEntry.getValue())>0){ //Remove calculated dimensions raised to zero in string representation. 
				dimensionString += "("+dimensionEntryKeyName+")^"+dimensionEntry.getValue();
			}
		}
		
		return dimensionString;
	}
	public static <T> String getDimensionAsString(Map<T, Double> dimensionMap, DimensionUpdater<T> dimensionUpdater){
		return getDimensionAsString(dimensionMap, MULTIPLICTION_SYMBOL, dimensionUpdater);
	}
	
	//
	public interface DimensionUpdater<T>{
		public void updateDimension(String type, double exponent, String group, String divisionSymbol, Map<T, Double> dimensionMap);
		public void updateWithUnknownDimension(Map<T, Double> dimensionMap);
		public String dimensionItemToString(T dimensionItem);
	}
	public static class ComponentUnitsDimensionUpdater implements DimensionUpdater<String>{

		@Override
		public void updateDimension(String componentUnitName, double exponent, String group, String divisionSymbol,
				Map<String, Double> componentUnitsDimensionMap) {
			updateDimensionMap(componentUnitName, exponent, group, divisionSymbol, componentUnitsDimensionMap);
		}	
		@Override
		public void updateWithUnknownDimension(Map<String, Double> dimensionMap){
			dimensionMap.put(Unit.UNKNOWN_UNIT_NAME, 1.0);
		}
		@Override
		public String dimensionItemToString(String dimensionItem){
			return dimensionItem;
		}
	}
	public static class FundamentalUnitTypesDimensionUpdater implements DimensionUpdater<UNIT_TYPE>{
		@Override
		public void updateDimension(String fundamentalUnitType, double exponent, String group, String divisionSymbol,
				Map<UNIT_TYPE, Double> fundamentalUnitsDimensionMap) {					
			updateDimensionMap(UNIT_TYPE.valueOf(fundamentalUnitType), exponent, group,divisionSymbol, fundamentalUnitsDimensionMap);
		}		
		@Override
		public void updateWithUnknownDimension(Map<UNIT_TYPE, Double> dimensionMap){
			dimensionMap.put(UNIT_TYPE.UNKNOWN, 1.0);
		}
		@Override
		public String dimensionItemToString(UNIT_TYPE dimensionItem){
			return dimensionItem.name();
		}
	}
	
	private static <T> void updateDimensionMap(T type, double exponent, String group, String divisionSymbol, Map<T, Double> dimensionMap){
		//The absence of any symbol can be regarded as multiplication.
		if(dimensionMap.containsKey(type)){
			dimensionMap.put(type, exponent + (group.substring(0, 1).equals(divisionSymbol)?-1:+1) * dimensionMap.get(type));	
		}else{
			dimensionMap.put(type, (group.substring(0, 1).equals(divisionSymbol)?-1:+1) * exponent);						
		}
	}
	
}
