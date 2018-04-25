package com.isaacapps.unitconverterapp.utilities;

import com.florianingerl.util.regex.Matcher;
import com.florianingerl.util.regex.Pattern;
import com.isaacapps.unitconverterapp.models.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel.UNIT_TYPE;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static com.isaacapps.unitconverterapp.utilities.UnitRegExUtility.*;

/**
 * Provides the means for converting strings into component unit and fundamental unit dimensions and vice versa.
 * Implementation uses a modified version of the Command Design Pattern.
 * 
 * @author Isaac Lafrance
 *
 */
public final class UnitDimensionProcessingUtility {
		
	/**
	 * Converts a string into a dimension map of component unit types and exponents.
	 * 
	 * @param componentUnitsDimension Format: a, a^#.## (a)^+-#.##  (* or /) (b)^+-#.##.
	 * 		  Discounts the number of white space characters between operators, words, and numbers. Can be nested with parentheses. 
	 * 		  Multiplication(*) or division(/) symbol can optionally appear as first items in the group.
	 * 
	 * @param strictParsing Determines whether to ignore and mark as unknown when text does not fit a predefined format.
	 * 
	 * @return Map of parsed component units names and their exponents
	 * @throws ParsingException
	 */
	public static Map<String, Double> parseToComponentUnitsDimensionFromString(String componentUnitsDimension
			, boolean strictParsing) throws ParsingException{
		String exponentGroupRegex = createExponentGroupRegex(EXPONENT_SYMBOLS, SIGNED_DOUBLE_VALUE_REGEX);

		return parseToGenericDimensionFromString(componentUnitsDimension
				, UNIT_NAME_REGEX
				, exponentGroupRegex
				, SIGNED_DOUBLE_VALUE_REGEX
				, DIVISION_SYMBOLS
				, MULTIPLICATION_SYMBOLS
				, new ComponentUnitsDimensionUpdater()
				, new HashMap<String, Double>()
				, strictParsing);
	}
	
	/**
	 * Converts a string into a dimension map of fundamental unit types and exponents.
	 * 
	 * @param fundamentalUnitTypesDimension Format: a, a^#.## (a)^+-#.##  (* or /) (b)^+-#.##.
	 * 		  Discounts the number of white space characters between operators, words, and numbers. Can be nested with parentheses. 
	 * 		  Multiplication(*) or division(/) symbol can optionally appear as first items in the group.
	 * 
	 * @param strictParsing Determine whether to ignore and mark as unknown, text that do not fit a predefined format
	 * 
	 * @return Map of parsed unit types and their exponents.
	 * @throws ParsingException
	 */
	public static Map<UNIT_TYPE, Double> parseToFundamentalUnitTypesDimensionFromString(String fundamentalUnitTypesDimension
			, boolean strictParsing) throws ParsingException{
		String exponentGroupRegex = createExponentGroupRegex(EXPONENT_SYMBOLS, SIGNED_DOUBLE_VALUE_REGEX);
		
		return parseToGenericDimensionFromString(fundamentalUnitTypesDimension
				, FUNDAMENTAL_UNIT_TYPE_REGEX
				, exponentGroupRegex
				, SIGNED_DOUBLE_VALUE_REGEX
				, DIVISION_SYMBOLS
				, MULTIPLICATION_SYMBOLS
				, new FundamentalUnitTypesDimensionUpdater()
				, new HashMap<UNIT_TYPE, Double>()
				, strictParsing);
	}
	
	/**
	 * Parses a dimension string by extracting and processing multigroups and single groups. Updates the passed in dimension map.
	 * @param divisionSymbols Array of symbol groups. Each group in the array is string that can contain one or more symbol characters.
	 * @param multiplicationSymbols Array of symbol groups. Each group in the array is string that can contain one or more symbol characters.
	 * @throws ParsingException 
	 */
	public static <T> Map<T, Double> parseToGenericDimensionFromString(String dimensionString
			, String atomicTypeRegEx, String exponentGroupRegEx, String exponentValueRegEx
			, String[] divisionSymbols, String[] multiplicationSymbols, DimensionUpdater<T> dimensionUpdater
			, Map<T, Double> dimensionMap, boolean strictParsing) throws ParsingException{
		String operationsComponentRegEx = UnitRegExUtility.createOperationComponentRegEx(divisionSymbols,multiplicationSymbols);

		return parseToGenericDimensionFromString(dimensionString
				, Pattern.compile(atomicTypeRegEx)
				, Pattern.compile(exponentGroupRegEx+"$")
				, Pattern.compile(exponentValueRegEx+"$")
				, createMultiGroupRegExPattern(atomicTypeRegEx, exponentGroupRegEx, operationsComponentRegEx)
				, createSingleGroupRegExPattern(atomicTypeRegEx, exponentGroupRegEx, operationsComponentRegEx)
				, Pattern.compile(createMultipleSymbolsRegEx(divisionSymbols))
				, dimensionUpdater
				, dimensionMap
				, strictParsing);
	}
	
	/**
	 * If the provided dimension string satisfies the balanced on the surface level
     * , then proceeds to extracting and processing multigroups and single groups.
	 * Updates the passed in dimension map.
	 * @throws ParsingException 
	 */
	private static <T> Map<T, Double> parseToGenericDimensionFromString(String dimensionString, Pattern atomicTypeRegExPattern
            , Pattern exponentGroupRegExPattern, Pattern exponentValueRegExPattern, Pattern multiGroupRegExPattern
            , Pattern singleGroupRegExPattern, Pattern divisionSymbolsRegExPattern, DimensionUpdater<T> dimensionUpdater
				  , Map<T, Double> dimensionMap, boolean strictParsing) throws ParsingException{	
		
		//
		if(dimensionString.isEmpty()){
			if(strictParsing){
				throw new ParsingException("Nothing was available to be parsed."
                        , "Make dimension string non-empty by including well formatted content.");
			}
			else{
				dimensionUpdater.updateWithUnknownDimension(dimensionMap);
				return dimensionMap;
			}
		}
	
		//
		if(hasBalancedParentheses(dimensionString)){
	       //Processes all multigroups first if there are any, leaving behind single groups that are returned as a truncated dimension string
			return parseNestedMultiGroups(dimensionString, 1.0, atomicTypeRegExPattern
					, exponentGroupRegExPattern, exponentValueRegExPattern, multiGroupRegExPattern, singleGroupRegExPattern
					, divisionSymbolsRegExPattern, dimensionUpdater, dimensionMap, strictParsing);
		}
		else{
			//Having balanced parenthesis is critical to proper parsing and must always throw an exception.
			throw new ParsingException(dimensionString
                    , "Make sure the number of open parenthesis braces equals the number of closing parenthesis braces.");
		}
	}
	
	/**
	 * Processes multi-groups using a recursive structure that greedily parses nested unit groups bounded by parentheses. ie. ( ((a)^2 * (a)^3)^8 /(b)^4 )^5.
	 * Use single group construct as base case.
	 * Updates the passed in dimension map with extracted results.
	 * @return Dimension map representing string that satifies type requirement of passed in dimension updater.
	 * @throws ParsingException 
	 */
	private static <T> Map<T, Double> parseNestedMultiGroups(String dimensionString, double recursedExponent
			, Pattern atomicTypeRegExPattern, Pattern exponentGroupRegExPattern, Pattern exponentValueRegExPattern
			, Pattern multiGroupRegExPattern, Pattern singleGroupDimensionRegExPattern
			, Pattern divisionSymbolsRegExPattern, DimensionUpdater<T> dimensionUpdater
			, Map<T, Double> dimensionMap, boolean strictParsing) throws ParsingException{

		String truncatedDimensionString = dimensionString; // During the recursion process matching multigroups will be greedily matched and successively excised from the dimension string.

		Matcher multiGroupDimensionRegExMatcher = multiGroupRegExPattern.matcher(dimensionString);

		/*There is exists an edgecase where a unit definition classified as a single groups is also classified as
         *  a mutltigroup by the respective regexes. This edgecase is explicitly when an atomic type
         *  is bounded by parentheses and raised to an exponent, ie '(meter)^2'.
         *  Rather than modify and over complicate the mutligroup regex to not match this special,
         *  there will just be a second to make sure that single group regex does not also match the multigroup.
         */
		while(multiGroupDimensionRegExMatcher.find()
                && !singleGroupDimensionRegExPattern.matcher(multiGroupDimensionRegExMatcher.group()).matches())
        {
			String multiGroupDimension = multiGroupDimensionRegExMatcher.group();
			
			String[] extractedExponentGroup = parseExponentGroup(multiGroupDimension
					, exponentGroupRegExPattern, exponentValueRegExPattern, divisionSymbolsRegExPattern);
			String exponentGroup = extractedExponentGroup.length == 0 ? "" : extractedExponentGroup[0];
			double currentExponent = extractedExponentGroup.length == 0 ? 1.0 : Double.valueOf(extractedExponentGroup[1]); //If not raised to anything, then assume 1.
						
			//Force a new nested multigroup search to occur in preparation for recursion.
            String multiGroupDimensionToBeRecursed = multiGroupDimension.substring(0, multiGroupDimension.lastIndexOf(exponentGroup))
                    .replaceFirst("^[^(]*\\(","").replaceFirst("\\)$", "")
                    .trim();
			
			//Passes on multigroup into next level of recursion making sure exponents are properly accumulated and successively passed on.
            parseNestedMultiGroups(multiGroupDimensionToBeRecursed
                    , recursedExponent * currentExponent
                    , atomicTypeRegExPattern, exponentGroupRegExPattern, exponentValueRegExPattern
                    , multiGroupRegExPattern, singleGroupDimensionRegExPattern, divisionSymbolsRegExPattern
                    , dimensionUpdater, dimensionMap, strictParsing);

			truncatedDimensionString = truncatedDimensionString.replace(multiGroupDimension, "");				
		}

		/*If for some reason the remaining truncated dimension string still appears to have multi groups,
         *then multigroup parsing had failed due some incorrectly formatted token somewhere.
         */
		if(hasNestedParentheses(truncatedDimensionString)
                || hasNestedExponents(truncatedDimensionString, exponentGroupRegExPattern.pattern()
                        .replace("$", "")))
		{
		    if(strictParsing){
                throw new ParsingException("This text appears to be an incorrectly formatted multigroup:"
                            + truncatedDimensionString
                        , "Change text format to fit regulae expression requirement for multigroups:"
                        +multiGroupRegExPattern.pattern());
            }
            else{
                dimensionUpdater.updateWithUnknownDimension(dimensionMap);
                return dimensionMap;
            }
        }

        if(!truncatedDimensionString.isEmpty()){
            return parseSingleGroups(truncatedDimensionString, recursedExponent, atomicTypeRegExPattern
                    , exponentGroupRegExPattern, exponentValueRegExPattern
                    , singleGroupDimensionRegExPattern, divisionSymbolsRegExPattern
                    , dimensionUpdater, dimensionMap, strictParsing);
        }
        else{
		   return dimensionMap;
        }
	}
	/**
	 * Processes collection of nonnested single groups. ie. a, a^2, (b), (b)^2,  a*b*c^2.
     * Updates the passed in dimension map with extracted results.
	 * @throws ParsingException 
	 */
	private static <T> Map<T, Double> parseSingleGroups(String dimensionString, double outerExponent
            , Pattern atomicTypeRegExPattern, Pattern exponentGroupRegExPattern
            , Pattern exponentValueRegExPattern, Pattern singleGroupDimensionRegExPattern
            , Pattern divisionSymbolsRegExPattern , DimensionUpdater<T> dimensionUpdater
            , Map<T, Double> dimensionMap, boolean strictParsing) throws ParsingException{
		
		String truncatedDimensionString = dimensionString; //Single groups will be successively excised until hopefully there is nothing left.
		
		Matcher singleGroupDimensionRegExMatcher = singleGroupDimensionRegExPattern.matcher(dimensionString);
		
		while(singleGroupDimensionRegExMatcher.find()){
			String singleGroupDimension = singleGroupDimensionRegExMatcher.group();
			
			//
			String atomicType = parseAtomicTypeGroup(singleGroupDimension, atomicTypeRegExPattern);
			if(atomicType.isEmpty())
				throw new ParsingException(String.format("No atomic type can be extracted using atomicTypeRegExPattern '%s'" +
								" from the group '%s' that was produced by the singleGroupDimensionRegExPattern '%s'."
								, exponentValueRegExPattern.pattern(), singleGroupDimension, exponentGroupRegExPattern.pattern())
				  		, "The group regular expression must corresponding with atomic type regular expressions.");

			//
			String[] extractedExponentGroup = parseExponentGroup(singleGroupDimension
					, exponentGroupRegExPattern, exponentValueRegExPattern, divisionSymbolsRegExPattern);
			double currentExponent = extractedExponentGroup.length == 0 ? 1.0 : Double.valueOf(extractedExponentGroup[1]); //If not raised to anything, then assume 1.
				
			//
			dimensionUpdater.updateDimension(atomicType, outerExponent*currentExponent, dimensionMap);
			
			truncatedDimensionString = truncatedDimensionString.replace(singleGroupDimension, "");
		}
		
		if(!truncatedDimensionString.isEmpty()){
		    if(strictParsing){
                throw new ParsingException(truncatedDimensionString
                        ,"Change remaining text to fit regular expression requirement for single groups: "
                        +singleGroupDimensionRegExPattern.pattern());
            }
            else{
                dimensionUpdater.updateWithUnknownDimension(dimensionMap);
            }
		}
			
		return dimensionMap;
	}
	/**
	 * Extracts extract an exponent group and the contained exponent value
	 * 
	 * @param exponentGroupRegExPattern Regular expression that finds if an exponential format exists and exponent group.
	 * 		  Consisting of exponent symbol, parenthesis (if present), and plus or minus exponent value. ie. '^(-1.233)' 
	 * @param exponentValueRegExPattern Regular expression that extracts exponent value. Must correspond with the exponentRegExPattern parameters
	 * 
	 * @return Array of strings. First element is extracted full exponent group.
	 * The second element is exponent value. If both can not be extracted then an empty array is returned.
	 * @throws ParsingException 
	 */
	private static String[] parseExponentGroup(String dimension, Pattern exponentGroupRegExPattern
			, Pattern exponentValueRegExPattern, Pattern divisionSymbolsPattern) throws ParsingException{
		Matcher exponentRegExMatcher = exponentGroupRegExPattern.matcher(dimension);
		
		if(exponentRegExMatcher.find()){
			String exponentGroup = exponentRegExMatcher.group();
			Matcher exponentValueRegExMatcher = exponentValueRegExPattern.matcher(exponentGroup);
			
			if(exponentValueRegExMatcher.find()){
				//By this point the parsing tree any present operation token was already validated as being suitable for division or multiplication
				String exponentValue = ((divisionSymbolsPattern.matcher(dimension).find()?"-":"")
                        + exponentValueRegExMatcher.group()).replace("--", "");
				return new String[] {exponentGroup, exponentValue};
			}
			
			throw new ParsingException(String.format("No value can be extracted using exponentValueRegExPattern '%s' " +
							"from the group '%s' that was produced by the exponentRegExPattern '%s'."
							, exponentValueRegExPattern.pattern(), exponentGroup, exponentGroupRegExPattern.pattern())
					, "The group regular expression must corresponding with value regular expressions.");
		}
		else{
			return new String[0];
		}
	}
	/**
	 * Identifies and extract one atomic type. ex. 'meter' if a unit name, LENGTH if a fundamental dimension
	 * @return String of extracted atomic type or an empty string.
	 */
	private static String parseAtomicTypeGroup(String dimension, Pattern atomicTypeRegExPattern){
		Matcher atomicTypeRegExMatcher = atomicTypeRegExPattern.matcher(dimension);
		
		if(atomicTypeRegExMatcher.find())
			return atomicTypeRegExMatcher.group().toLowerCase();
		
		return "";
	}
	
	//
	/**
	 * Converts a component dimension map to string equivalent. 
	 * Format: a, (a)^2, (a)^4*(b)^-3
	 */
	public static String convertComponentUnitsDimensionToString(Map<String, Double> componentUnitsDimension){
		return convertGenericDimensionToString(componentUnitsDimension, new ComponentUnitsDimensionUpdater());
	}
	/**
	 * Converts a unit type dimension map to string equivalent. 
	 * Ex. format: LENGTH, (METER)^2, (GRAM)*(SECOND)^-2
	 */
	public static String convertFundamentalUnitTypesDimensionToString(Map<UNIT_TYPE, Double> fundamentalUnitTypesDimension){
		return convertGenericDimensionToString(fundamentalUnitTypesDimension
                , new FundamentalUnitTypesDimensionUpdater());
	}	
	
	public static <T> String convertGenericDimensionToString(Map<T, Double> dimensionMap, DimensionUpdater<T> dimensionUpdater
			, String multiplicationSymbol, String exponentSymbol, boolean includeParentheses)
	{
		StringBuilder dimensionStringBuilder = new StringBuilder();

		for(Entry<T, Double> dimensionEntry:dimensionMap.entrySet()){
			dimensionStringBuilder.append(dimensionStringBuilder.length()>0? " "+multiplicationSymbol.trim()+" ": "");
			
			String dimensionEntryKeyName = dimensionUpdater.dimensionItemToString(dimensionEntry.getKey());
			
			if(dimensionEntry.getValue() == 1){
				dimensionStringBuilder.append(dimensionEntryKeyName);
			}
			else if(Math.abs(dimensionEntry.getValue())>1){ //Remove calculated dimensions raised to zero in string representation. 
				dimensionStringBuilder.append(includeParentheses ? "(" + dimensionEntryKeyName + ")" : dimensionEntryKeyName)
                        .append(exponentSymbol).append(dimensionEntry.getValue());
			}
		}
		
		return dimensionStringBuilder.toString();
	}
	public static <T> String convertGenericDimensionToString(Map<T, Double> dimensionMap, DimensionUpdater<T> dimensionUpdater){
		return convertGenericDimensionToString(dimensionMap, dimensionUpdater, MULTIPLICATION_SYMBOLS[0], EXPONENT_SYMBOLS[0], true);
	}
	
	//
    /**
     * Interface contract that specifies how a generic dimension updater can parse and process dimension maps.
     * @param <T> Dimension types
     */
	public interface DimensionUpdater<T>{
		void updateDimension(String type, double exponent, Map<T, Double> dimensionMap) throws ParsingException;
		void updateWithUnknownDimension(Map<T, Double> dimensionMap);
		String dimensionItemToString(T dimensionItem);
	}

    /**
     * Specifes how to parse and process component unit dimension maps.
     */
	public static class ComponentUnitsDimensionUpdater implements DimensionUpdater<String>{
		@Override
		public void updateDimension(String componentUnitName, double exponent, Map<String, Double> componentUnitsDimensionMap) {
			componentUnitsDimensionMap.put(componentUnitName
					, exponent + (componentUnitsDimensionMap.containsKey(componentUnitName)
							?componentUnitsDimensionMap.get(componentUnitName):0.0));
		}	
		@Override
		public void updateWithUnknownDimension(Map<String, Double> dimensionMap){
		    if(!dimensionMap.containsKey(Unit.UNKNOWN_UNIT_NAME))
			    dimensionMap.put(Unit.UNKNOWN_UNIT_NAME, 1.0);
		}
		@Override
		public String dimensionItemToString(String dimensionItem){
			return dimensionItem;
		}
	}
    /**
     * Specifies how to parse and process fundamental unit type dimension maps.
     */
	public static class FundamentalUnitTypesDimensionUpdater implements DimensionUpdater<UNIT_TYPE>{
		@Override
		public void updateDimension(String fundamentalUnitTypeString, double exponent, Map<UNIT_TYPE
				, Double> fundamentalUnitsDimensionMap) throws ParsingException {
			try{
				UNIT_TYPE fundamentalUnitType = UNIT_TYPE.valueOf(fundamentalUnitTypeString);
				
				fundamentalUnitsDimensionMap.put(fundamentalUnitType
						, exponent + (fundamentalUnitsDimensionMap.containsKey(fundamentalUnitType)
								?fundamentalUnitsDimensionMap.get(fundamentalUnitType):0.0));
			}
			catch(IllegalArgumentException e){
				throw new ParsingException("Failed to convert "+fundamentalUnitTypeString
						+" to enum UNIT_TYPE", "Change token to exactly match a UNIT_TYPE enum. ie. LENGTH, MASS, etc.");
			}
		}		
		@Override
		public void updateWithUnknownDimension(Map<UNIT_TYPE, Double> dimensionMap){
		    if(!dimensionMap.containsKey(UNIT_TYPE.UNKNOWN))
			    dimensionMap.put(UNIT_TYPE.UNKNOWN, 1.0);
		}
		@Override
		public String dimensionItemToString(UNIT_TYPE dimensionItem){
			return dimensionItem.name();
		}
	}	

    //
    /**
     * Compares two generic map dimensions to see if they have corresponding dimension items with identical
     * dimensions values (exponents). Ignores dimension items with zero dimension values.
     * Default tolerance is 0.00001.
     */
    public static <T> boolean equalsGenericDimension(Map<T, Double> firstGenericDimension
            , Map<T, Double> secondGenericDimension)
    {
        return equalsGenericDimension(firstGenericDimension, secondGenericDimension, 0.00001);
    }

    /**
     * Compares two generic map dimensions to see if they have corresponding dimension items with identical
     * dimensions values (exponents). Ignores dimension items with zero dimension values.
     */
    public static <T> boolean equalsGenericDimension(Map<T, Double> firstGenericDimension
            , Map<T, Double> secondGenericDimension, Double tolerance)
    {
        if(firstGenericDimension.isEmpty() || secondGenericDimension.isEmpty())
            return false;

        secondGenericDimension = new HashMap<T, Double>(secondGenericDimension); //clone map since it will be modified

        for(Map.Entry<T, Double> entry:firstGenericDimension.entrySet()){
            if(!secondGenericDimension.containsKey(entry.getKey())){
                if(Math.abs(entry.getValue().doubleValue())>0.0){
                    return false;
                }
            }
            else if(Math.abs(secondGenericDimension.get(entry.getKey()).doubleValue() - entry.getValue()) > tolerance ){
                return false;
            }
            secondGenericDimension.remove(entry.getKey());
        }

        if(secondGenericDimension.size()>0){
            //Although the number of components may be the same, there maybe some components that are raised to zero and
            //or some components that were not compared in the OTHER map since THIS unit's map was the initial basis of comparison.
            for(Map.Entry<T, Double> entry:secondGenericDimension.entrySet()){
                if(Math.abs(entry.getValue()) > 0.0){
                    return false;
                }
            }
        }

        return true;
    }
}
