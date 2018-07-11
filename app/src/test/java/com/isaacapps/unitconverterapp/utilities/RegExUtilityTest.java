package com.isaacapps.unitconverterapp.utilities;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;

public class RegExUtilityTest {

    @Test
    public void escapeRegexReservedCharacters_Should_Only_Escape_Reserved_Regex_Characters(){
        String nonReservedCharactersInput = "abcefg";
        String reservedCharactersInput = "[*+^?<>$.|]";
        String combinedInput = nonReservedCharactersInput + reservedCharactersInput;
        String escapedReservedCharacters = "\\[\\*\\+\\^\\?\\<\\>\\$\\.\\|\\]";

        //
        String result = RegExUtility.escapeRegexReservedCharacters(combinedInput);

        ///
        assertThat("Reserved characters were not escaped", result, containsString(escapedReservedCharacters));
        assertThat("Nonreserved characters were escaped.", result, containsString(nonReservedCharactersInput));
    }

    @Test
    public void signedDoubleValueRegex_Should_Match_Real_Numbers(){
        String[] numberInputs = new String[]{"7","7.0","-7", "-7.0"};

        for(String numberInput:numberInputs){
            assertThat(String.format("SIGNED_DOUBLE_VALUE_REGEX does not match %s", numberInput)
                    ,numberInput.matches(RegExUtility.SIGNED_DOUBLE_VALUE_REGEX), is(true));
        }
    }

    @Test
    public void signedDoubleValueRegex_Should_Match_Exponential_Numbers(){
        String[] numberInputs = new String[]{"7e7", "-5.32E-10"};

        for(String numberInput:numberInputs){
            assertThat(String.format("SIGNED_DOUBLE_VALUE_REGEX \" %s \" does not match %s", RegExUtility.SIGNED_DOUBLE_VALUE_REGEX, numberInput)
                    , numberInput.matches(RegExUtility.SIGNED_DOUBLE_VALUE_REGEX), is(true));
        }
    }

}