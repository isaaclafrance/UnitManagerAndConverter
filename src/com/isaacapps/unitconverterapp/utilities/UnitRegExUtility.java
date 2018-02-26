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
    public static String getInteriorGroupComponent(String atomicTypeRegEx){
    	return "([\\s]*"+atomicTypeRegEx+"[\\s]*)";
    }
    
    /**
     * Recursively and greedily searches for the existence multigroups. All multigroups satisfy the complex dimension criteria.
     * They can be nested obviously nested such as '( ((a)^2 * (a)^3)^8 /(b)^4 )^5',  '(a^3/b)^3'
     * Or they can be trivially nested such as '(a*b*c*d)^5' which is the same as ((a)^1*(b)^1*(c)^1*(d)^1)^5.
     */
	public static Pattern getMultiGroupRegExPattern(String typeRegEx, String exponentRegEx, String divisionSymbol, String multiplicationSymbol){
		String operationComponent = getOperationComponent(divisionSymbol, multiplicationSymbol);
		String interiorGroupComponent = getInteriorGroupComponent(typeRegEx);
		
		return Pattern.compile("(?<group>"+operationComponent+"[\\(]((?'group')|"+interiorGroupComponent+")*[\\)]"+exponentRegEx+")");
	}
	
	/**
	 * Search for a single group that has the following simple format 'a', 'a^2', '(a)^3'.
	 */
	public static Pattern getSingleGroupRegExPattern(String atomicTypeRegEx, String divisionSymbol, String multiplicationSymbol, String exponentRegEx){
		return Pattern.compile( "([\\(])?"+getOperationComponent(divisionSymbol,multiplicationSymbol)
		                          +getInteriorGroupComponent(atomicTypeRegEx) 
		                          + "(?(1)[\\)])"+exponentRegEx+"?)");
	}
	
	//
	public static boolean textHasComplexDimensions(String unitName){
		return unitName.contains(MULTIPLICTION_SYMBOL) || unitName.contains(DIVISION_SYMBOL) || unitName.contains(EXPONENT_SYMBOL)
				|| hasBalancedParanetheses(unitName);
	}
	
	/**
	 * Quickly determines if for every open brace, there is a corresponding closing brace. Text with no parentheses are trivially balanced...
	 */
	public static boolean hasBalancedParanetheses(String text){
		return Pattern.matches("(?<block>[\\(]((?block)|[^\\(\\)]*)[\\)])", text.trim());
	}
	
	/**
	 * Quickly determines if there is a nesting of parentheses.
	 */
	public static boolean hasNestedParentheses(String text){
		if(hasBalancedParanetheses(text)){
			return text.trim().matches("[\\(][^\\)]*[\\(]");
		}
		else{
			return false;
		}
	}
	
	/**
	 * Quickly determines if there is a nesting of exponents. Something like '(a^2/(b)^4)^2' and so on
	 */
	public static boolean hasNestedExponents(String text, String exponentRegEx){
		return Pattern.matches("(?<block>[\\(]((?block)|.*[\\)]?"+exponentRegEx+".*)[\\)]"+exponentRegEx+")", text.trim());
	}
}
