package com.isaacapps.unitconverterapp.utilities;

import com.florianingerl.util.regex.Matcher;
import com.florianingerl.util.regex.Pattern;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.*;

public class UnitRegExUtilityTest {

    //Subcomponent Tests
    @Test
    public void createMultiplSymbolsRegex_WithSymbolGroupsArray_Should_Match_Only_Specified_SymbolGroup(){
        String[] partipantSymbolGroups = new String[]{" x ", "/", "multiply", "*", "^", "."};

        Pattern participantSymbolsGroupPattern = Pattern.compile(UnitRegExUtility
                .createMultipleSymbolsRegEx(partipantSymbolGroups));

        for(String participantSymbolGroup:partipantSymbolGroups){
            Matcher participantSymbolGroupMatcher = participantSymbolsGroupPattern.matcher(participantSymbolGroup);
            assertTrue(String.format("Participant symbol '%s' is not matched by regex for group %s"
                        , participantSymbolGroup, Arrays.toString(partipantSymbolGroups))
                    , participantSymbolGroupMatcher.matches());
        }
    }

    @Test
    public void createMultiplSymbolsRegex_WithSymbolGroupsArray_Should_Not_Match_Non_Specified_SymbolGroup(){
        String[] partipantSymbolGroups = new String[]{" x ", "/", "multiply", "*", "^"};
        String[] nonParticipantSymbolGroups = new String[]{"&", "mul", ""};

        Pattern participantSymbolsGroupPattern = Pattern.compile(UnitRegExUtility
                .createMultipleSymbolsRegEx(partipantSymbolGroups));

        for (String nonParticipantSymbolGroup: nonParticipantSymbolGroups) {
            Matcher participantSymbolGroupMatcher = participantSymbolsGroupPattern.matcher(nonParticipantSymbolGroup);
            assertFalse(String.format("Nonparticiapnt symbol '%s' is matched by regex for group %s " +
                            "although it is not part of group."
                        , nonParticipantSymbolGroup, Arrays.toString(partipantSymbolGroups))
                    , participantSymbolGroupMatcher.matches());
        }
    }

    @Test
    public void createOperationComponentRegex_WithMultiplicationAndDivisionSymbolArrays_Should_Only_Match_Specified_Symbol(){
        String[] participantMultiplicationSymbolGroups = new String[]{"*", " multiplied by ", "x"};
        String[] partipantDivisionSymbolGroups = new String[]{"/", " divided by ", "%", " per "};
        Matcher participantSymbolGroupMatcher;

        Pattern participantSymbolsGroupPattern = Pattern.compile(UnitRegExUtility
                .createOperationComponentRegEx(partipantDivisionSymbolGroups
                        , participantMultiplicationSymbolGroups));

        for(String participantSymbolGroup:participantMultiplicationSymbolGroups){
            participantSymbolGroupMatcher = participantSymbolsGroupPattern.matcher(participantSymbolGroup);
            assertTrue(String.format("Participant symbol '%s' is not matched by regex for group %s"
                        , participantSymbolGroup, Arrays.toString(participantMultiplicationSymbolGroups))
                    , participantSymbolGroupMatcher.matches());
        }

        for(String participantSymbolGroup:partipantDivisionSymbolGroups){
            participantSymbolGroupMatcher = participantSymbolsGroupPattern.matcher(participantSymbolGroup);
            assertTrue(String.format("Participant symbol '%s' is not matched by regex for group %s"
                        , participantSymbolGroup, Arrays.toString(partipantDivisionSymbolGroups))
                    , participantSymbolGroupMatcher.matches());
        }
    }

    @Test
    public void createOperationComponentRegex_WithMultiplicationAndDivisionSymbolArrays_Should_Not_Match_NonSpecified_Symbols(){
        String[] partipantMultiplicationSymbolGroups = new String[]{"*", " multiplied by "};
        String[] partipantDivisionSymbolGroups = new String[]{"/"};
        String[] nonParticipantSymbolGroups = new String[]{"(", "mul", "**", "", " "};
        Matcher participantSymbolGroupMatcher;

        Pattern participantSymbolsGroupPattern = Pattern.compile(UnitRegExUtility
                .createOperationComponentRegEx(partipantDivisionSymbolGroups
                        , partipantMultiplicationSymbolGroups));

        for (String nonParticipantSymbolGroup: nonParticipantSymbolGroups) {
            participantSymbolGroupMatcher = participantSymbolsGroupPattern.matcher(nonParticipantSymbolGroup);
            assertFalse(String.format("Nonparticiapnt symbol '%s' is matched by regex " +
                            "for the multiplication %s and division '%s' groups although it is not part of the groups."
                        , nonParticipantSymbolGroup, Arrays.toString(partipantMultiplicationSymbolGroups)
                        , Arrays.toString(partipantDivisionSymbolGroups))
                    , participantSymbolGroupMatcher.matches());
        }
    }

    @Test
    public void createOperationComponentRegex_WithMultiplicationAndDivisionSymbolArrays_Should_IgnoreWhiteSpace_ForMatches(){
        String[] partipantMultiplicationSymbolGroups = new String[]{"*", " "};
        String[] partipantDivisionSymbolGroups = new String[]{"/"};

        Pattern participantSymbolsGroupPattern = Pattern.compile(UnitRegExUtility
                .createOperationComponentRegEx(partipantDivisionSymbolGroups
                        , partipantMultiplicationSymbolGroups));

        List<String> inputOperationSymbols =  new ArrayList<>(Arrays.asList(partipantDivisionSymbolGroups));
        inputOperationSymbols.addAll(Arrays.asList(partipantMultiplicationSymbolGroups));

        for(String participantSymbolGroup: inputOperationSymbols){
            Matcher participantSymbolGroupMatcher = participantSymbolsGroupPattern.matcher("   "+participantSymbolGroup+"  ");
            assertTrue(String.format("Participant symbol '%s' is not matched by regex for group %s"
                        , participantSymbolGroup, Arrays.toString(partipantMultiplicationSymbolGroups))
                    , participantSymbolGroupMatcher.matches());
        }
    }

    @Test
    public void createInteriorGroupComponentRegEx_With_AtomicTypeRegEx_Should_IgnoreWhiteSpaces_ForMatches(){
        String literalAtomicTypeRegex = "test";
        String interiorGroupComponentRegex = UnitRegExUtility.createInteriorGroupComponentRegEx(literalAtomicTypeRegex);

        Pattern interiorGroupComponentPattern = Pattern.compile(interiorGroupComponentRegex);

        String input = "   "+literalAtomicTypeRegex+"  ";
        assertTrue("Input '"+input+"' with whitespaces does not match.", interiorGroupComponentPattern
                .matcher(input).matches());
    }

    @Test
    public void createExponentGroupRegEx_Should_Match_Only_Exponential_Satifying_Specified_Symbol_And_Exponent_Value_RegEx(){
        String exponentValueRegEx = "123";
        String[] exponentSymbols = new String[]{"^", "**"};

        String exponentGroupRegEx = UnitRegExUtility.createExponentGroupRegex(exponentSymbols, exponentValueRegEx);

        Pattern exponentGroupPattern = Pattern.compile(exponentGroupRegEx);

        String[] inputs = new String[]{"^123", "**123"};

        for(String input:inputs) {
            assertTrue(String.format("No match found although input has specfied symbol and value format" +
                            ". Input:%s. Specified symbols:%s. Specified exponent value pattern: %s"
                        , input, Arrays.toString(exponentSymbols), exponentValueRegEx)
                    , exponentGroupPattern.matcher(input).matches());
        }
    }

    @Test
    public void createExponentGroupRegEx_Should_Not_Match_Exponential_Not_Satifying_Specified_Symbol_And_Exponent_Value_RegEx(){
        String exponentValueRegEx = "123";
        String[] exponentSymbols = new String[]{"^", "**"};

        String exponentGroupRegEx = UnitRegExUtility.createExponentGroupRegex(exponentSymbols, exponentValueRegEx);

        Pattern exponentGroupPattern = Pattern.compile(exponentGroupRegEx);

        String[] inputs = new String[]{"#123","^abc"};

        for(String input:inputs){
            assertFalse(String.format("Nonspecified expression is matched. Input:"+input+". " +
                            "Specified symbols:%s. Specified exponent value pattern: %s"
                        , Arrays.toString(exponentSymbols), exponentValueRegEx)
                    , exponentGroupPattern.matcher(input).matches());
        }
    }

    @Test
    public void createExponentGroupRegEx_Should_With_Symbol_And_Exponent_Value_Should_Ignore_WhiteSpaces_For_Matches(){
        String exponentValueRegEx = "123";
        String[] exponentSymbols = new String[]{"**"};

        String exponentGroupRegEx = UnitRegExUtility.createExponentGroupRegex(exponentSymbols, exponentValueRegEx);

        Pattern exponentGroupPattern = Pattern.compile(exponentGroupRegEx);

        String input = "      "+"**"+"   "+"123"+"     ";

        assertTrue(String.format("No match found although input has specfied symbol and value format. " +
                        "Input:%s. Specified symbols:%s. " +
                        "Specified exponent value pattern: %s", input, Arrays.toString(exponentSymbols), exponentValueRegEx)
                , exponentGroupPattern.matcher(input).matches());
    }

    //Main Component: Single Groups Tests
    @Test
    public void createSingleGroupRegExPattern_Should_Match_Atomic_Type(){
        String atomicTypeRegEx = "aaa";
        String exponentGroupRegEx = "\\^123";
        String operationComponentRegEx = "(/|\\*)";

        Pattern singleGroupPattern = UnitRegExUtility.createSingleGroupRegExPattern(atomicTypeRegEx
                , exponentGroupRegEx, operationComponentRegEx);

        String[] inputs = new String[]{"aaa", "(aaa)"};

        for(String input:inputs) {
            assertTrue(String.format("No Match for Input:%s. With Atomic type regex:%s, "
                            + "Exponent group regex:%s, Operation component regex:%s"
                    , input, atomicTypeRegEx, exponentGroupRegEx, operationComponentRegEx)
                    , singleGroupPattern.matcher(input).matches());
        }
    }

    @Test
    public void createSingleGroupRegExPattern_Should_Match_Atomic_Type_With_Exponent(){
        String atomicTypeRegEx = "aaa";
        String exponentGroupRegEx = "\\^123";
        String operationComponentRegEx = "(/|\\*)";

        Pattern singleGroupPattern = UnitRegExUtility.createSingleGroupRegExPattern(atomicTypeRegEx
                , exponentGroupRegEx, operationComponentRegEx);

        String[] inputs = new String[]{"aaa^123", "(aaa)^123"};

        for(String input:inputs) {
            assertTrue(String.format("No Match for Input:%s. With Atomic type regex:%s, "
                            + "Exponent group regex:%s, Operation component regex:%s"
                    , input, atomicTypeRegEx, exponentGroupRegEx, operationComponentRegEx)
                    , singleGroupPattern.matcher(input).matches());
        }
    }

    @Test
    public void createSingleGroupRegExPattern_Should_Match_Atomic_Type_With_Exponent_And_OperationSymbols(){
        String atomicTypeRegEx = "aaa";
        String exponentGroupRegEx = "\\^123";
        String operationComponentRegEx = UnitRegExUtility
                .createOperationComponentRegEx(new String[]{"/"," divided by "}, new String[]{"*"});

        Pattern singleGroupPattern = UnitRegExUtility.createSingleGroupRegExPattern(atomicTypeRegEx
                , exponentGroupRegEx, operationComponentRegEx);

        String[] inputs = new String[]{"*aaa^123", "/(aaa)^123", " * (aaa)^123", " divided by ( aaa )^123"};

        for(String input:inputs) {
            assertTrue(String.format("No Match for Input:%s. With Atomic type regex:%s, "
                            + "Exponent group regex:%s, Operation component regex:%s"
                    , input, atomicTypeRegEx, exponentGroupRegEx, operationComponentRegEx)
                    , singleGroupPattern.matcher(input).matches());
        }
    }

    @Test
    public void createSingleGroupRegExPattern_ShouldNot_Match_Incorrect_Atomic_Type_Format(){
        String atomicTypeRegEx = "aaa";
        String exponentGroupRegEx = "\\^123";
        String operationComponentRegEx = "(/|\\*)";

        Pattern singleGroupPattern = UnitRegExUtility.createSingleGroupRegExPattern(atomicTypeRegEx
                , exponentGroupRegEx, operationComponentRegEx);

        String[] inputs = new String[]{"bab^123", "baa", "(aaaaaa)", "(aaa"};

        for(String input:inputs) {
            assertFalse(String.format("Match for Input:%s. With Atomic type regex:%s, "
                            + "Exponent group regex:%s, Operation component regex:%s"
                    , input, atomicTypeRegEx, exponentGroupRegEx, operationComponentRegEx)
                    , singleGroupPattern.matcher(input).matches());
        }
    }

    @Test
    public void createSingleGroupRegExPattern_ShouldNot_Match_Incorrect_Exponent_Format(){
        String atomicTypeRegEx = "aaa";
        String exponentGroupRegEx = "\\^123";
        String operationComponentRegEx = "(/|\\*)";

        Pattern singleGroupPattern = UnitRegExUtility.createSingleGroupRegExPattern(atomicTypeRegEx
                , exponentGroupRegEx, operationComponentRegEx);

        String[] inputs = new String[]{"aaa$123"};

        for(String input:inputs) {
            assertFalse(String.format("Match for Input:%s. With Atomic type regex:%s, "
                            + "Exponent group regex:%s, Operation component regex:%s"
                    , input, atomicTypeRegEx, exponentGroupRegEx, operationComponentRegEx)
                    , singleGroupPattern.matcher(input).matches());
        }
    }

    @Test
    public void createSingleGroupRegExPattern_ShouldNot_Match_Complex_Units(){
        String atomicTypeRegEx = "aaa";
        String exponentGroupRegEx = "\\^123";
        String operationComponentRegEx = "(/|\\*)";

        Pattern singleGroupPattern = UnitRegExUtility.createSingleGroupRegExPattern(atomicTypeRegEx
                , exponentGroupRegEx, operationComponentRegEx);

        String[] inputs = new String[]{"(aaa^123)^123", "(aaa*aaa)^123"};

        for(String input:inputs) {
            assertFalse(String.format("Match for Input:%s. With Atomic type regex:%s, "
                            + "Exponent group regex:%s, Operation component regex:%s"
                    , input, atomicTypeRegEx, exponentGroupRegEx, operationComponentRegEx)
                    , singleGroupPattern.matcher(input).matches());
        }
    }

    //Main Component: Multi Groups Tests
    @Test
    public void createMultiGroupRegExPattern_Should_Match_Trivially_Nested_MultiGroup(){
        String atomicTypeRegEx = "(aaa|bbb)";
        String exponentGroupRegEx = "\\^123";
        String operationComponentRegEx = "(/|\\*|x)";

        Pattern singleGroupPattern = UnitRegExUtility.createMultiGroupRegExPattern(atomicTypeRegEx
                , exponentGroupRegEx, operationComponentRegEx);

        String[] inputs = new String[]{"(aaa*aaa x aaa/bbb)^123", "(aaa x bbb)"};

        for(String input:inputs) {
            assertTrue(String.format("No Match for Input:%s. With Atomic type regex:%s, "
                            + "Exponent group regex:%s, Operation component regex:%s"
                    , input, atomicTypeRegEx, exponentGroupRegEx, operationComponentRegEx)
                    , singleGroupPattern.matcher(input).matches());
        }
    }

    @Test
    public void createMultiGroupRegExPattern_Should_Match_Obviously_Nested_And_Complex_MultiGroup(){
        String atomicTypeRegEx = "(aaa|bbb)";
        String exponentGroupRegEx = "\\^123";
        String operationComponentRegEx = UnitRegExUtility
                .createOperationComponentRegEx(new String[]{"/"}, new String[]{"*", " x "});

        Pattern singleGroupPattern = UnitRegExUtility.createMultiGroupRegExPattern(atomicTypeRegEx
                , exponentGroupRegEx, operationComponentRegEx);

        String[] inputs = new String[]{"( (aaa^123 * bbb / (aaa^123*bbb*aaa) )^123 *aaa x aaa/bbb)^123"};

        for(String input:inputs) {
            assertTrue(String.format("No Match for Input:%s. With Atomic type regex:%s, "
                            + "Exponent group regex:%s, Operation component regex:%s"
                    , input, atomicTypeRegEx, exponentGroupRegEx, operationComponentRegEx)
                    , singleGroupPattern.matcher(input).matches());
        }
    }

    @Test
    public void createMultiGroupRegExPattern_ShouldNot_Match_Solitary_AtomicType(){
        String atomicTypeRegEx = "(aaa|bbb)";
        String exponentGroupRegEx = "\\^123";
        String operationComponentRegEx = "(/|\\*)";

        Pattern singleGroupPattern = UnitRegExUtility.createMultiGroupRegExPattern(atomicTypeRegEx
                , exponentGroupRegEx, operationComponentRegEx);

        String[] inputs = new String[]{"aaa", "bbb", "*bbb", "aaa^123"}; //Ideal (aaa) should not match but it does.

        for(String input:inputs) {
            assertFalse(String.format("Match for Input:%s. With Atomic type regex:%s, "
                            + "Exponent group regex:%s, Operation component regex:%s"
                        , input, atomicTypeRegEx, exponentGroupRegEx, operationComponentRegEx)
                    , singleGroupPattern.matcher(input).matches());
        }
    }

    //Unit Definition Format Satisfiability Tests
    @Test
    public void hasBalancedParentheses_Should_Be_True_When_Open_Braces_And_Closed_Braces_Num_Are_Equal_And_Properly_Arranged()
    {
        String[] inputs = new String[]{"()", "()()", "(()())", "(()(()(())((()))))", ""};

        for(String input:inputs)
        {
            assertTrue("Input:"+input+", is falsely be classified as having no balanced parentheses"
                    , UnitRegExUtility.hasBalancedParentheses(input));
        }
    }

    @Test
    public void hasBalancedParentheses_Should_Be_False_When_Open_Braces_And_Closed_Braces_Num_Are_Not_Equal_And_Properly_Arranged()
    {
        String[] inputs = new String[]{"(", "())", "(()()", "(()(()(())(()))))", "((((", "))(("};

        for(String input:inputs)
        {
            assertFalse("Input:"+input+", is falsely be classified as having balanced parentheses"
                    , UnitRegExUtility.hasBalancedParentheses(input));
        }
    }

    @Test
    public void hasNestedParentheses_Should_Be_True_When_Parentheses_Are_Embedded(){
        String[] inputs = new String[]{"(())", "(())()"};

        for(String input:inputs)
        {
            assertTrue("Input:"+input+", is falsely be classified as having no nested parentheses"
                    , UnitRegExUtility.hasNestedParentheses(input));
        }
    }

    @Test
    public void hasNestedParentheses_Should_Be_False_When_Parentheses_Are_Not_Embedded(){
        String[] inputs = new String[]{"()", "()() ()"};

        for(String input:inputs)
        {
            assertFalse("Input:"+input+", is falsely be classified as having nested parentheses"
                    , UnitRegExUtility.hasNestedParentheses(input));
        }
    }

    @Test
    public void hasComplexDimensions_Should_Be_True_When_Unit_Name_Has_Exponents(){
        String operationComponentRegex = "(/|\\*)";
        String exponentGroupRegex = "\\^\\d+"; ;

        String[] inputs = new String[]{"aaa^123", "bb^2"};

        for(String input:inputs)
        {
            assertTrue("Input:"+input+", is falsely be classified as having no complex dimensions"
                    , UnitRegExUtility.hasComplexDimensions(input, exponentGroupRegex, operationComponentRegex));
        }
    }

    @Test
    public void hasComplexDimensions_Should_Be_True_When_Unit_Name_Has_Nested_Or_Multiple_Parentheses(){
        String operationComponentRegex = "(/|\\*)";
        String exponentGroupRegex = "\\^\\d+"; ;

        String[] inputs = new String[]{"(aaa(aaa(bbb)))", "(aaa)(bbb)(aaa)"};

        for(String input:inputs)
        {
            assertTrue("Input:"+input+", is falsely be classified as having no complex dimensions"
                    , UnitRegExUtility.hasComplexDimensions(input, exponentGroupRegex, operationComponentRegex));
        }
    }

    @Test
    public void hasComplexDimensions_Should_Be_True_When_Unit_Name_Has_Operation_Symbols(){
        String operationComponentRegex = "(/|\\*)";
        String exponentGroupRegex = "\\^\\d+"; ;

        String[] inputs = new String[]{"aaa*bbb", "aaa*bbb*aaa", "aaa/aaa", "aaa*bbb/aaa"};

        for(String input:inputs)
        {
            assertTrue("Input:"+input+", is falsely be classified as having no complex dimensions"
                    , UnitRegExUtility.hasComplexDimensions(input, exponentGroupRegex, operationComponentRegex));
        }
    }

    @Test
    public void hasComplexDimensions_Should_Be_False_When_Unit_Name_Is_Atomic(){
        String operationComponentRegex = "(/|\\*)";
        String exponentGroupRegex = "\\^\\d+"; ;

        String[] inputs = new String[]{"(aaa)", "aaaaaaa"};

        for(String input:inputs)
        {
            assertFalse("Input:"+input+", is falsely be classified as having complex dimensions"
                    , UnitRegExUtility.hasComplexDimensions(input, exponentGroupRegex, operationComponentRegex));
        }
    }

    @Test
    public void hasNestedExponents_Should_Be_True_When_Units_Definition_Has_Explicitly_Nested_Exponents(){
        String exponentGroupRegEx = "\\^\\d+";

        String[] inputs = new String[]{"(aaa^2*aaa^12344*bbb)^3", "(aaa^234)^32123"};

        for(String input:inputs)
        {
            assertTrue("Input:"+input+", is falsely be classified as not having explicitly  nested exponents"
                    , UnitRegExUtility.hasNestedExponents(input, exponentGroupRegEx));
        }
    }

    @Test
    public void hasNestedExponents_Should_Be_False_When_Units_Definition_Has_No_Nested_Exponents(){
        String exponentGroupRegEx = "\\^\\d+";

        String[] inputs = new String[]{"aaa", "aaa^321*bbb^123", "(aaa)^123"};

        for(String input:inputs)
        {
            assertFalse("Input:"+input+", is falsely be classified as having nested exponents"
                    , UnitRegExUtility.hasNestedExponents(input, exponentGroupRegEx));
        }
    }

}