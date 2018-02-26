package com.isaacapps.unitconverterapp.utilities;

import static com.isaacapps.unitconverterapp.utilities.UnitRegExUtility.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.isaacapps.unitconverterapp.models.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel.UNIT_TYPE;

/**
 * Provides the means for converting strings into component unit and fundamental units dimensions and vice versa.
 * Implementation uses a modified version of the Command Design Pattern.
 * 
 * @author Isaac Lafrance
 *
 */
public final class UnitDimensionProcessingUtility {
		
	/**
	 * Converts a string into a dimension map of component unit types and exponents.
	 * 
	 * @param fundamentalUnitTypesDimension Format: a, a^#.## (a)^+-#.##  (* or /) (b)^+-#.##, where 'a' and 'b' are word characters. 
	 * 		  Discounts the number of white space characters between operators, words, and numbers. Can be nested with parentheses. 
	 * 		  Multiplication(*) or division(/) symbol can optionally appear as first items in the group.
	 * 
	 * @param strictParsing Determines whether to ignore and mark as unknown, text that do not fit a predefined format
	 * 
	 * @return
	 * @throws ParsingException
	 */
	public static Map<String, Double> getComponentUnitsDimensionFromString(String componentUnitsDimension, boolean strictParsing) throws ParsingException{
		//Format: a, prefix'a', (a)^+-#.##  (* or /) (b)^+-#.##, where 'a' and 'b' are word characters. Discounts the number of white space characters between operators, words, and numbers. Can be nested with parentheses.
		//Multiplication or division symbol can optionally appear as first items in the group.
		return getDimensionFromString(componentUnitsDimension, Pattern.compile(UNIT_NAME_REGEX), Pattern.compile(EXPONENT_REGEX+"$"), Pattern.compile(SIGNED_DOUBLE_VALUE_REGEX)
				, getMultiGroupRegExPattern(UNIT_NAME_REGEX, EXPONENT_REGEX, DIVISION_SYMBOL, MULTIPLICTION_SYMBOL)
				, getSingleGroupRegExPattern(UNIT_NAME_REGEX, DIVISION_SYMBOL, MULTIPLICTION_SYMBOL, EXPONENT_REGEX)
                , DIVISION_SYMBOL
                , new ComponentUnitsDimensionUpdater()
                , new HashMap<String, Double>()
                , strictParsing);
	}
	
	/**
	 * Converts a string into a dimension map of fundamental unit types and exponents.
	 * 
	 * @param fundamentalUnitTypesDimension Format: a, a^#.## (a)^+-#.##  (* or /) (b)^+-#.##, where 'a' and 'b' are word characters. 
	 * 		  Discounts the number of white space characters between operators, words, and numbers. Can be nested with parentheses. 
	 * 		  Multiplication(*) or division(/) symbol can optionally appear as first items in the group.
	 * 
	 * @param strictParsing Determine whether to ignore and mark as unknown, text that do not fit a predefined format
	 * 
	 * @return
	 * @throws ParsingException
	 */
	public static Map<UNIT_TYPE, Double> getFundamentalUnitTypesDimensionFromString(String fundamentalUnitTypesDimension, boolean strictParsing) throws ParsingException{
		return getDimensionFromString(fundamentalUnitTypesDimension, Pattern.compile(UNIT_TYPE_REGEX), Pattern.compile(EXPONENT_REGEX+"$"), Pattern.compile(SIGNED_DOUBLE_VALUE_REGEX)
				                      , getMultiGroupRegExPattern(UNIT_TYPE_REGEX, EXPONENT_REGEX, DIVISION_SYMBOL, MULTIPLICTION_SYMBOL)
				                      , getSingleGroupRegExPattern(UNIT_TYPE_REGEX, DIVISION_SYMBOL, MULTIPLICTION_SYMBOL, EXPONENT_REGEX)
				                      , DIVISION_SYMBOL
				                      , new FundamentalUnitTypesDimensionUpdater()
				                      , new HashMap<UNIT_TYPE, Double>()
				                      , strictParsing);
	}
	
	/**
	 * Parses a dimension string by extracting and processing multigroups and single groups. Updates the passed in dimension map.
	 * @throws ParsingException 
	 */
	public static <T> Map<T, Double> getDimensionFromString(String dimensionString, String atomicTypeRegEx, String exponentRegEx, String exponentValueRegEx
			, String divisionSymbol, String multiplicationSymbol, DimensionUpdater<T> dimensionUpdater, Map<T, Double> dimensionMap
			, boolean strictParsing) throws ParsingException{
			return getDimensionFromString(dimensionString, Pattern.compile(atomicTypeRegEx)
					, Pattern.compile(exponentRegEx)
					, Pattern.compile(exponentValueRegEx)
					, getMultiGroupRegExPattern(atomicTypeRegEx, exponentRegEx, divisionSymbol, multiplicationSymbol)
					, getSingleGroupRegExPattern(atomicTypeRegEx, divisionSymbol, multiplicationSymbol, exponentRegEx)
					, divisionSymbol, dimensionUpdater, dimensionMap, strictParsing);
	}
	
	/**
	 * Parses a dimension string by extracting and processing multigroups and single groups. Updates the passed in dimension map.
	 * @throws ParsingException 
	 */
	private static <T> Map<T, Double> getDimensionFromString(String dimensionString, Pattern atomicTypeRegExPattern
				  , Pattern exponentRegExPattern, Pattern exponentValueRegExPattern, Pattern multiGroupRegExPattern
				  , Pattern singleGroupRegExPattern, String divisionSymbol, DimensionUpdater<T> dimensionUpdater
				  , Map<T, Double> dimensionMap, boolean strictParsing) throws ParsingException{	
		
		String truncatedDimensionString = dimensionString.trim();
		
		//
		if(dimensionString.isEmpty()){
			if(strictParsing){
				throw new ParsingException("Nothing was available to be parsed.", "Make dimension string non-empty by including well formatted content.");
			}
			else{
				dimensionUpdater.updateWithUnknownDimension(dimensionMap);
				return dimensionMap;
			}
		}
	
		//
		if(hasBalancedParanetheses(dimensionString)){
	       //Processes all multigroups first if there are any, leaving behind single groups that are returned as a truncated dimension string
			truncatedDimensionString = processNestedMultiGroups(dimensionString, 1.0, atomicTypeRegExPattern
					, exponentRegExPattern, exponentValueRegExPattern, multiGroupRegExPattern, singleGroupRegExPattern
					, divisionSymbol, dimensionUpdater, dimensionMap);
					
			//Next, process remaining single groups in the remaining truncated dimension string
			return processSingleGroups(truncatedDimensionString, 1.0, atomicTypeRegExPattern, exponentRegExPattern
					, exponentValueRegExPattern, multiGroupRegExPattern, singleGroupRegExPattern, divisionSymbol
					, dimensionUpdater, dimensionMap, strictParsing);
		}
		else{
			//Having balanced parenthesis is critical to proper parsing and must always throw an exception.
			throw new ParsingException(dimensionString, "Make sure the number of open parenthesis braces equals the number of closing parenthesis braces.");
		}
	}
	
	/**
	 * Processes multi-groups using a recursive structure that greedily parses nested unit groups bounded by parentheses. ie. ( ((a)^2 * (a)^3)^8 /(b)^4 )^5
	 * Updates the passed in dimension map with extracted results.
	 * @return Truncated version of passed in dimension string with extracted multigroups removed.
	 * @throws ParsingException 
	 */
	private static <T> String processNestedMultiGroups(String dimensionString, double recursedExponent, Pattern atomicTypeRegExPattern
			, Pattern exponentRegExPattern, Pattern exponentValueRegExPattern, Pattern multiGroupRegExPattern
			, Pattern singleGroupDimensionRegExPattern, String divisionSymbol, DimensionUpdater<T> dimensionUpdater
			, Map<T, Double> dimensionMap) throws ParsingException{	

		String truncatedDimensionString = dimensionString; // During the recursion process matching multigroups will be greedily matched and successively excised from the dimension string.
		
		Matcher multiGroupDimensionRegExMatcher = multiGroupRegExPattern.matcher(dimensionString);
		
		if( ( hasNestedExponents(dimensionString, exponentRegExPattern.pattern()) || hasNestedExponents(dimensionString, exponentRegExPattern.pattern()) )
			  && multiGroupDimensionRegExMatcher.find()){
		
			while(multiGroupDimensionRegExMatcher.find()){
				String multiGroupDimension = multiGroupDimensionRegExMatcher.group();
				
				String[] processExponentGroup = extractExponentGroup(multiGroupDimension, exponentRegExPattern, exponentValueRegExPattern, divisionSymbol);
				String exponentGroup = processExponentGroup.length == 0 ? "" : processExponentGroup[0];
				double currentExponent = processExponentGroup.length == 0 ? 1.0 : Double.valueOf(processExponentGroup[1]); //If not raised to anything, then assume 1.
							
				//Force a new nested multigroup search to occur in preparation for possible recursion.
				Matcher recursedMultiGroupDimensionRegExMatcher = multiGroupRegExPattern.matcher(multiGroupDimension.replaceFirst("\\"+exponentGroup+"$", "")
						                                                                 .replaceFirst("^[^\\(]*\\(","").replaceFirst("\\)$", ""));
				
				//Passes on multigroup into next level of recursion making sure exponents are properly accumulated and successively passed on.
				if(recursedMultiGroupDimensionRegExMatcher.find()){					
					processNestedMultiGroups(recursedMultiGroupDimensionRegExMatcher.group(), recursedExponent * currentExponent
							, atomicTypeRegExPattern, exponentRegExPattern, exponentValueRegExPattern
							, multiGroupRegExPattern, singleGroupDimensionRegExPattern , divisionSymbol
							, dimensionUpdater, dimensionMap);
				}
				
				truncatedDimensionString = truncatedDimensionString.replace(multiGroupDimension, "");				
			}
		}

		/*The recursion base case of nested multi-group must lead to one or more single groups. And each single groups will always have a valid
		  because the regular expression of the single group is sort of built on top of the single group.*/
		processSingleGroups(truncatedDimensionString, recursedExponent, atomicTypeRegExPattern
				            , exponentRegExPattern, exponentValueRegExPattern, multiGroupRegExPattern
				            , singleGroupDimensionRegExPattern, divisionSymbol, dimensionUpdater, dimensionMap, true);
			
		return truncatedDimensionString;
	}
	/**
	 * Processes single groups. ie. a, a^2, (b), (b)^2,  a*b*c^2. Updates the passed in dimension map with extracted results.
	 * @throws ParsingException 
	 */
	private static <T> Map<T, Double> processSingleGroups(String dimensionString, double outerExponent, Pattern atomicTypeRegExPattern
			      , Pattern exponentRegExPattern, Pattern exponentValueRegExPattern
			      , Pattern multiGroupDimensionRegExPattern, Pattern singleGroupDimensionRegExPattern, String divisionSymbol
			      , DimensionUpdater<T> dimensionUpdater, Map<T, Double> dimensionMap
			      , boolean strictParsing) throws ParsingException{	
		
		String truncatedDimensionString = dimensionString;  // Single groups will be successively excised until hopefully there is nothing left.
		
		Matcher singleGroupDimensionRegExMatcher = singleGroupDimensionRegExPattern.matcher(dimensionString);
		
		while(singleGroupDimensionRegExMatcher.find()){
			String singleGroupDimension = singleGroupDimensionRegExMatcher.group();
			
			//
			String atomicType = extractAtomicTypeGroup(singleGroupDimension, atomicTypeRegExPattern);
			if(atomicType.isEmpty())
				throw new ParsingException("No atomic type can be extracted using atomicTypeRegExPattern '"+exponentValueRegExPattern.pattern()
				  +"' from the group '"+singleGroupDimension+"' that was produced by the singleGroupDimensionRegExPattern '"+exponentRegExPattern.pattern()+"'."
				  , "The group regular expression must corresponding with atomic type regular expressions.");

			//
			String[] processExponentGroup = extractExponentGroup(singleGroupDimension, exponentRegExPattern, exponentValueRegExPattern, divisionSymbol);
			double currentExponent = processExponentGroup.length == 0 ? 1.0 : Double.valueOf(processExponentGroup[1]); //If not raised to anything, then assume 1.
				
			//
			dimensionUpdater.updateDimension(atomicType, outerExponent*currentExponent, dimensionMap);
			
			truncatedDimensionString = truncatedDimensionString.replace(singleGroupDimension, "");
		}
		
		if(!truncatedDimensionString.isEmpty() && strictParsing){
			throw new ParsingException(truncatedDimensionString
						,"Change remaining text to fit regular expression requirement for single groups: "
								+singleGroupDimensionRegExPattern.pattern());
		}
		else{
			dimensionUpdater.updateWithUnknownDimension(dimensionMap);
		}
			
		return dimensionMap;
	}
	/**
	 * Extracts extract an exponent group and the contained exponent value
	 * 
	 * @param exponentRegExPattern Regular expression that finds if an exponential format exists and exponent group.
	 * 		  Consisting of exponent symbol, parenthesis (if present), and plus or minus exponent value. ie. '^(-1.233)' 
	 * @param exponentValueRegExPattern Regular expression that extracts exponent value. Must correspond with the exponentRegExPattern parameters
	 * 
	 * @return Array of strings. First element is extracted full exponent group.
	 * The second element is exponent value. If both can not be extracted then an empty array is returned.
	 * @throws ParsingException 
	 */
	private static String[] extractExponentGroup(String dimension, Pattern exponentRegExPattern
			, Pattern exponentValueRegExPattern, String divisionSymbol) throws ParsingException{
		Matcher exponentRegExMatcher = exponentRegExPattern.matcher(dimension);
		
		if(exponentRegExMatcher.find()){
			String exponentGroup = exponentRegExMatcher.group();
			Matcher exponentValueRegExMatcher = exponentValueRegExPattern.matcher(exponentGroup);
			
			if(exponentValueRegExMatcher.find()){
				//The absence of any symbol can also be regarded as multiplication.
				String exponentValue = (dimension.substring(0, 1).equals(divisionSymbol)?"-1":"+1") + exponentValueRegExMatcher.group();				
				return new String[] {exponentGroup, exponentValue};
			}
			
			throw new ParsingException("No value can be extracted using exponentValueRegExPattern '"+exponentValueRegExPattern.pattern()
            				  +"' from the group '"+exponentGroup+"' that was produced by the exponentRegExPattern '"+exponentRegExPattern.pattern()+"'."
            				  , "The group regular expression must corresponding with value regular expressions.");
		}
		else{
			return new String[0];
		}
	}
	/**
	 * Identifies and extract one atomic type.
	 * @return String of extracted atomic type of an empty string.
	 */
	private static String extractAtomicTypeGroup(String dimension, Pattern atomicTypeRegExPattern){
		Matcher atomicTypeRegExMatcher = atomicTypeRegExPattern.matcher(dimension);
		
		if(atomicTypeRegExMatcher.find())
			return atomicTypeRegExMatcher.group().toLowerCase();
		
		return "";
	}
	
	//
	public static String getComponentUnitsDimensionAsString(Map<String, Double> componentUnitsDimension){
		return getDimensionAsString(componentUnitsDimension, new ComponentUnitsDimensionUpdater());
	}
	public static String getFundamentalUnitTypesDimensionAsString(Map<UNIT_TYPE, Double> fundamentalUnitTypesDimension){
		return getDimensionAsString(fundamentalUnitTypesDimension, new FundamentalUnitTypesDimensionUpdater());
	}	
	
	public static <T> String getDimensionAsString(Map<T, Double> dimensionMap, String multiplicationSymbol, DimensionUpdater<T> dimensionUpdater){
		String dimensionString = "";

		for(Entry<T, Double> dimensionEntry:dimensionMap.entrySet()){
			dimensionString = dimensionString + (!dimensionString.isEmpty()? " "+multiplicationSymbol+" ": "");
			
			String dimensionEntryKeyName = dimensionUpdater.dimensionItemToString(dimensionEntry.getKey());
			
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
		public void updateDimension(String type, double exponent, Map<T, Double> dimensionMap) throws ParsingException;
		public void updateWithUnknownDimension(Map<T, Double> dimensionMap);
		public String dimensionItemToString(T dimensionItem);
	}
	public static class ComponentUnitsDimensionUpdater implements DimensionUpdater<String>{

		@Override
		public void updateDimension(String componentUnitName, double exponent, Map<String, Double> componentUnitsDimensionMap) {
			componentUnitsDimensionMap.put(componentUnitName
					, exponent + (componentUnitsDimensionMap.containsKey(componentUnitName)?componentUnitsDimensionMap.get(componentUnitName):0.0));
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
		public void updateDimension(String fundamentalUnitTypeString, double exponent, Map<UNIT_TYPE, Double> fundamentalUnitsDimensionMap) throws ParsingException {	
			try{
				UNIT_TYPE fundamentalUnitType = UNIT_TYPE.valueOf(fundamentalUnitTypeString);
				
				fundamentalUnitsDimensionMap.put(fundamentalUnitType
						, exponent + (fundamentalUnitsDimensionMap.containsKey(fundamentalUnitType)?fundamentalUnitsDimensionMap.get(fundamentalUnitType):0.0));
			}
			catch(IllegalArgumentException e){
				throw new ParsingException("Failed to convert "+fundamentalUnitTypeString+" to enum UNIT_TYPE", "Change token to exactly match a UNIT_TYPE enum. ie. LENGTH, MASS, etc.");
			}
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
}
