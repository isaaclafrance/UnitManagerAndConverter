package com.isaacapps.unitconverterapp.utilities;

import java.util.regex.Pattern;


public final class UnitRegExUtility {
	public static final String UNIT_NAME_REGEX = "([a-zA-Z]+)?(\\w+)";
	public static final String UNIT_TYPE_REGEX = "[a-zA-Z]+";
	public static final String SIGNED_DOUBLE_VALUE_REGEX = "[-+]?(\\d*[.])?\\d+";
	public static final String EXPONENT_SYMBOL = "^";
	public static final String EXPONENT_REGEX = "([\\s]*\\"+EXPONENT_SYMBOL+"[\\s]*([\\s]*"+SIGNED_DOUBLE_VALUE_REGEX+"[\\s]*))"; //accounts for variable number of white spaces
	public static final String DIVISION_SYMBOL = "/";
	public static final String MULTIPLICTION_SYMBOL = "*";
	
	
	//
    public static String getOperationComponent(String divisionSymbol, String multiplicationSymbol){
    	return "([\\s]*[\\"+multiplicationSymbol+"\\"+divisionSymbol+"]?[\\s]*)";
    }
    public static String getInteriorGroupComponent(String typeRegEx){
    	return "([\\s]*"+typeRegEx+"[\\s]*)";
    }
    
    /**
     * Recursively and greedily searches for the existence of nested groups
     */
	public static Pattern getMultiGroupRegExPattern(String typeRegEx, String exponentRegEx, String divisionSymbol, String multiplicationSymbol){
		String operationComponent = getOperationComponent(divisionSymbol, multiplicationSymbol);
		String interiorGroupComponent = getInteriorGroupComponent(typeRegEx);
		
		return Pattern.compile("(?<group>"+operationComponent+"[\\(]((?'group')|"+interiorGroupComponent+")*[\\)]"+exponentRegEx+")");
	}
	public static Pattern getSingleGroupRegExPattern(String typeRegEx, String divisionSymbol, String multiplicationSymbol){
		return Pattern.compile(getOperationComponent(divisionSymbol,multiplicationSymbol)+getInteriorGroupComponent(typeRegEx));
	}
	
	//
	public static boolean unitNameHasComplexDimensions(String unitName){
		return unitName.contains(MULTIPLICTION_SYMBOL) || unitName.contains(DIVISION_SYMBOL) || unitName.contains(EXPONENT_SYMBOL)
				|| numberOfParenthsisPairs(unitName) > 1;
	}
	
	/**
	 * Quickly determines if for every open brace, there is a corresponding closing brace. For simplicity sake, does not account got correct position.
	 * For instance "{}{}" is just as valid as "}{}{"
	 */
	public static boolean hasBalancedParanethesis(String text){
		if(text.contains("(") && text.contains(")")
		   || text.matches("[^\\(\\]+")) // text have no parenthesis is trivially balanced
		{
			return text.split("(").length == text.split(")").length;
		}
		return false;
	}
	/**
	 * Quickly calculates the number of the balanced pairings. For simplicity sake, does not account got correct position.
	 * For instance "{}{}" is just as valid as "}{}{"
	 * @return Number of parenthesis pairings or -1 if parenthesis pairings are unbalanced.
	 */
	public static int numberOfParenthsisPairs(String text){
		if(hasBalancedParanethesis(text)){
			return text.split("(").length - 1;
		}
		else{
			return -1;
		}
	}
}
