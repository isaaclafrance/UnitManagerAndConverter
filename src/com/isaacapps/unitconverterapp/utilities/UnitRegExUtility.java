package com.isaacapps.unitconverterapp.utilities;

import com.florianingerl.util.regex.Pattern;

public final class UnitRegExUtility {
	public static final String UNIT_NAME_REGEX = "(?:([a-zA-Z]+)?\\w+)"; //ex. h20, meter, 10
	public static final String FUNDAMENTAL_UNIT_TYPE_REGEX = "(?:[a-zA-Z]+)";
	public static final String SIGNED_DOUBLE_VALUE_REGEX = "(?:[-+]?(?:\\d*[.])?\\d+)";
	public static final String[] EXPONENT_SYMBOLS = new String[]{"^", "**", " raised to "};
	public static final String[] DIVISION_SYMBOLS = new String[]{"/", " per ", " divided by "};
	public static final String[] MULTIPLICATION_SYMBOLS = new String[]{"*", " x ", " times ", " ", " multiplied by "};
	
	/**
	 * Converts groups of symbols into a regular expression with alternation.
	 * @param symbolGroups Array of symbol groups. Each group in the array is a string
	 *                     that can contain one or more symbol characters or text.
	 *                     Ex. '*', '**', ' x ', ' some word '
	 * @return Regular expression string that captures symbol groups.
	 */
	public static String createMultipleSymbolsRegEx(String[] symbolGroups){
		Pattern reservedSymbolPattern = Pattern.compile("[*+^?<>$.|]");
		StringBuilder regexStringBuilder = new StringBuilder();
		
		for(String symbolGroup:symbolGroups){
            //If there are multiple symbol groups, then they are joined with an alternation symbol.
            regexStringBuilder.append(regexStringBuilder.length() == 0 ? "" : "|");

		    //Finds reserved characters in the symbol group and places them is brackets
			for(char character:symbolGroup.toCharArray()){
				if(reservedSymbolPattern.matcher(String.valueOf(character)).matches())
					regexStringBuilder.append("\\");

				regexStringBuilder.append(character);
			}
		}
		
		return regexStringBuilder.insert(0, "(?:").append(")").toString();
	}

    /**
     * Converts groups of multiplication and division symbols into a regular expression with alternation.
     * @param divisionSymbols Array of symbol groups. Each group in the array is a string
	 *                        that can contain one or more symbol characters.
     * @param multiplicationSymbols Array of symbol groups. Each group in the array is a string
	 *                              that can contain one or more symbol characters.
     * @return Regular expression string that captures groups.
     */
    public static String createOperationComponentRegEx(String[] divisionSymbols, String[] multiplicationSymbols){
    	return String.format("(?:[\\s]*(?:%s|%s)[\\s]*)", createMultipleSymbolsRegEx(multiplicationSymbols)
                , createMultipleSymbolsRegEx(divisionSymbols));
    }

    /**
     * Modifies the atomic type regular expression to ignore white spaces.
     * Serves as the smallest non exponential parsable component of a unit definition.
     */
    public static String createInteriorGroupComponentRegEx(String atomicTypeRegEx){
    	return String.format("(?:[\\s]*%s[\\s]*)", atomicTypeRegEx);
    }
    /**
     * Converts groups of symbols and value regex into a another regular expression with alternation that can capture text in exponential form.
     * @param exponentSymbols Array of symbol groups. Each group in the array is string that can contain one or more symbol characters.
     * @param exponentValueRegEx Regular expression that is able to capture the numerical exponent part.
     * @return Regular expression string that captures an exponent construct.
     */
    public static String createExponentGroupRegex(String[] exponentSymbols, String exponentValueRegEx){
    	return String.format("(?:[\\s]*%s[\\s]*([\\s]*%s[\\s]*))", createMultipleSymbolsRegEx(exponentSymbols)
                , exponentValueRegEx);
    }
    
    /**
     * Recursively and greedily searches for the existence nested multigroups. All multigroups must satisfy the complex dimension criteria.
     * Form can be satisfied when obviously nested such as '( ((a)^2 * (a)^3)^8 /(b)^4 )^5',  '(a^3/b)^3'
     * When trivially nested and consisting of multiple single groups such as '(a*b*c/d)^5' which is the same as ((a)^1*(b)^1*(c)^1/(d)^1)^5.
	 * The regex also  unfortunately extends to trivial cases where a single atomic type is inclosed in parenthesis
	 * ,which is the only case where the single group and multigroup regex's both have matches
	 *  but its not worth it to make the nested regex ignore that case.
     */
	public static Pattern createMultiGroupRegExPattern(String atomicTypeRegEx, String exponentGroupRegEx
			, String operationComponentRegEx){
		String optionalOperationComponent = "(?:"+operationComponentRegEx+")?";
		String optionalExponentialComponent = "(?:"+exponentGroupRegEx+")?";

		String singleGroupComponent = createSingleGroupRegExPattern(atomicTypeRegEx
				, exponentGroupRegEx, operationComponentRegEx).pattern();

		String nestedMultiGroup = String.format("(?<group>%s[(][\\s]*(?:(?'group')|%s)+[\\s]*[)]%s)"
                , optionalOperationComponent, singleGroupComponent, optionalExponentialComponent);

		return Pattern.compile(nestedMultiGroup);
	}
	
	/**
	 * Search for a single group that has one of the three simple formats 'a', 'a^2', '(a)^3', with and without operation symbols.
     * May or may not be complex depending on if there is an exponent.
	 */
	public static Pattern createSingleGroupRegExPattern(String atomicTypeRegEx, String exponentGroupRegEx
            , String operationComponentRegEx){
        String optionalExponentialComponent = "(?:"+exponentGroupRegEx+")?";
        String optionalOperationalComponent = "(?:"+operationComponentRegEx+")?";

	    return Pattern.compile(String.format("%s(?<p>[(][\\s]*)?%s(?(p)[\\s]*[)])%s"
				, optionalOperationalComponent, createInteriorGroupComponentRegEx(atomicTypeRegEx)
                , optionalExponentialComponent));
	}
	
	/**
	 * Dimension is complex if it has multiple division or multiplication symbols, has nested parenthesis, or uses exponents.
	 * Uses default multiplication, division, and exponent symbols.
	 */
	public static boolean hasComplexDimensions(String unitDefinition){
		return hasComplexDimensions(unitDefinition, createExponentGroupRegex(EXPONENT_SYMBOLS, SIGNED_DOUBLE_VALUE_REGEX)
				,createOperationComponentRegEx(DIVISION_SYMBOLS, MULTIPLICATION_SYMBOLS));
	}
	
	/**
	 * Dimension is complex if it has at least one division or multiplication symbol separating other token
	 * , has nested or multiple side by side parenthesized groups, or uses exponents.
	 */
	public static boolean hasComplexDimensions(String unitDefinition, String exponentGroupRegex
			, String operationComponentRegex){
		return unitDefinition.matches("(?:([(].*[)]){2,})")
				|| Pattern.compile(operationComponentRegex).matcher(unitDefinition).find()
				|| Pattern.compile(exponentGroupRegex).matcher(unitDefinition).find()
				|| hasNestedParentheses(unitDefinition);
	}
		
	/**
	 * Quickly determines if for every open brace, there is a corresponding closing brace.
	 * Text with no parentheses is trivially balanced...
	 */
	public static boolean hasBalancedParentheses(String unitDefinition){
		unitDefinition = Pattern.compile("(\\((?:[^()]|(?1))*\\))").matcher(unitDefinition).replaceAll(""); //remove all individual and nested balanced parentheses
		return !(unitDefinition.contains("(") || unitDefinition.contains(")")); //If the text was originally balanced, then should be no remaining parentheses

		//Alternate intuitive Approach that can also capture all nestings
		//Example Transition Diagram: ( ( g ( ( (a)b ) ((c)) ) f ) ) -- remove nonparentheses characters and whitespaces --> ((((())(())))) -- two nested chunks '(())' removed --> ((())) -- fit balanced criteria? --> true 	
		/*
		return text.trim().replaceAll("[^()]", "") //Only leave behind parentheses for easier processing, stripping everything else
				   .replaceAll("<?block>[(](?block)?[)]","") //Remove nested seperate balanced chucks, if there are any
				   .matches("<?block>[(](?block)?[)]"); // after all individual inner balanced chunks are removed and if the text was originally balanced overall, then only zero or one balanced chunks should remai		
		*/
	}
	
	/**
	 * Quickly determines if there is a nesting of parentheses, but only if the text is balanced
	 * If there is at least one instance of ' ( [anything not ')'] ( ', then there is some nesting of some kind.
	 */
	public static boolean hasNestedParentheses(String unitDefinition){
		if(hasBalancedParentheses(unitDefinition)){
			return Pattern.compile("[(][^)]*[(]").matcher(unitDefinition.trim()).find();
		}
		else{
			return false;
		}
	}
	
	/**
	 *Quickly determines if there is an obivious nesting of exponents, ie. '(a^2/(b)^4)^2'
	 */
	public static boolean hasNestedExponents(String unitDefinition, String exponentGroupRegEx){
		return Pattern.compile(String.format("(?:[(](?:.+%s.+)[)]%<s)",exponentGroupRegEx))
				.matcher(unitDefinition.trim()).find();
	}
}
