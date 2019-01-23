package com.isaacapps.unitconverterapp.processors.formatters.numbers;

import com.florianingerl.util.regex.Matcher;
import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;

import java.util.Locale;

import static com.isaacapps.unitconverterapp.utilities.RegExUtility.SIGNED_DOUBLE_VALUE_REGEX_PATTERN;

public class RoundingFormatter implements IFormatter {
    private Locale locale;
    private int numOfDecimalPlaces = 6;

    public RoundingFormatter(Locale locale) {
        this.locale = locale;
    }
    public RoundingFormatter(Locale locale, int numOfDecimalPlaces){
        this(locale);
        this.numOfDecimalPlaces = numOfDecimalPlaces;
    }


    /**
     * Produces a new formatted text where each instance of a double is transformed into rounded double.
     * If there are no suitable instances, then the string is returned as is.
     */
    @Override
    public String format(String textWithDoubles) {
        String formattedNumber = textWithDoubles;

        Matcher decimalInputMatcher = SIGNED_DOUBLE_VALUE_REGEX_PATTERN.matcher(textWithDoubles);
        while(decimalInputMatcher.find())
            formattedNumber = textWithDoubles.replace(decimalInputMatcher.group(), String.format(locale,"%." + numOfDecimalPlaces + "f", Double.valueOf(decimalInputMatcher.group())));

        return formattedNumber;
    }

    ///
    public void setNumOfDecimalPlaces(int numberOfDecimalPlaces) {
        this.numOfDecimalPlaces = numberOfDecimalPlaces;
    }
    public int getNumOfDecimalPlaces(){
        return numOfDecimalPlaces;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

}
