package com.isaacapps.unitconverterapp.processors.formatters.numbers;

import com.florianingerl.util.regex.Matcher;
import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;

import java.text.NumberFormat;
import java.util.Locale;

import static com.isaacapps.unitconverterapp.utilities.RegExUtility.SIGNED_DOUBLE_VALUE_REGEX_PATTERN;

public class PercentFormatter implements IFormatter {
    private Locale locale;
    private NumberFormat percentNumberFormat;

    public PercentFormatter(Locale locale) {
        setLocale(locale);
    }

    @Override
    /**
     * Produces a new formatted text where each instance of a double is transformed into another percent representation.
     * If there are no suitable instances, then the string is returned as is.
     */
    public String format(String textWithDoubles) {
        String formattedCurrencyNumber = textWithDoubles;

        Matcher percentInputMatcher = SIGNED_DOUBLE_VALUE_REGEX_PATTERN.matcher(textWithDoubles);
        while(percentInputMatcher.find())
            formattedCurrencyNumber = textWithDoubles.replace(percentInputMatcher.group(), percentNumberFormat.format(Double.valueOf(percentInputMatcher.group())));

        return formattedCurrencyNumber;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
        percentNumberFormat = NumberFormat.getPercentInstance(locale);
    }
}
