package com.isaacapps.unitconverterapp.processors.formatters.numbers;

import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;

import java.util.Locale;
import java.util.regex.Matcher;

import static com.isaacapps.unitconverterapp.utilities.RegExUtility.SIGNED_DOUBLE_VALUE_REGEX_PATTERN;

public class RoundingFormatter implements IFormatter {
    private Locale locale;
    private int decimalPlaces = 6;

    public RoundingFormatter(Locale locale) {
        this.locale = locale;
    }
    public RoundingFormatter(Locale locale, int decimalPlaces){
        this(locale);
        this.decimalPlaces = decimalPlaces;
    }

    @Override
    public String format(String number) {
        String formattedNumber = number;

        Matcher decimalInputMatcher = SIGNED_DOUBLE_VALUE_REGEX_PATTERN.matcher(number);
        while(decimalInputMatcher.find())
            formattedNumber = number.replaceAll(decimalInputMatcher.group(), String.format(locale,"%." + decimalPlaces + "f", Double.valueOf(decimalInputMatcher.group())));

        return formattedNumber;
    }

    ///
    public void setDecimalPlaces(int numberOfDecimalPlaces) {
        this.decimalPlaces = numberOfDecimalPlaces;
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
