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

    @Override
    public String format(String number) {
        String formattedNumber = number;

        Matcher decimalInputMatcher = SIGNED_DOUBLE_VALUE_REGEX_PATTERN.matcher(number);
        while(decimalInputMatcher.find())
            formattedNumber = number.replace(decimalInputMatcher.group(), String.format(locale,"%." + numOfDecimalPlaces + "f", Double.valueOf(decimalInputMatcher.group())));

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
