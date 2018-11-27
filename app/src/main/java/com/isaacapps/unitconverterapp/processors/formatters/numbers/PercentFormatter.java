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
    public String format(String percentNumber) {
        String formattedCurrencyNumber = percentNumber;

        Matcher percentInputMatcher = SIGNED_DOUBLE_VALUE_REGEX_PATTERN.matcher(percentNumber);
        while(percentInputMatcher.find())
            formattedCurrencyNumber = percentNumber.replace(percentInputMatcher.group(), percentNumberFormat.format(Double.valueOf(percentInputMatcher.group())));

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
