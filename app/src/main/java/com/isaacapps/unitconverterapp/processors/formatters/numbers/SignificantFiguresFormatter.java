package com.isaacapps.unitconverterapp.processors.formatters.numbers;

import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;

import java.util.Locale;
import java.util.regex.Matcher;

import static com.isaacapps.unitconverterapp.utilities.RegExUtility.SIGNED_DOUBLE_VALUE_REGEX_PATTERN;

public class SignificantFiguresFormatter implements IFormatter {
    private Locale locale;
    private int numberOfSignificantFigures = 6;

    public SignificantFiguresFormatter(Locale locale) {
        this.locale = locale;
    }
    public SignificantFiguresFormatter(Locale locale, int numberOfSignificantFigures){
        this(locale);
        this.numberOfSignificantFigures = numberOfSignificantFigures;
    }

    @Override
    public String format(String decimal) {
        String formattedNumber = decimal;

        Matcher decimalInputMatcher = SIGNED_DOUBLE_VALUE_REGEX_PATTERN.matcher(decimal);
        while(decimalInputMatcher.find())
            formattedNumber = decimal.replaceAll(decimalInputMatcher.group(), String.format(locale,"%." + numberOfSignificantFigures + "g", decimalInputMatcher.group()));

        return formattedNumber;
    }

    ///
    public void setNumberOfSignificantFigures(int numberOfSignificantFigures) {
        this.numberOfSignificantFigures = numberOfSignificantFigures;
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
