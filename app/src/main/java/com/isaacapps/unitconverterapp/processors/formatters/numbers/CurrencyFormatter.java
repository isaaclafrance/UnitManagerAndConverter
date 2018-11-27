package com.isaacapps.unitconverterapp.processors.formatters.numbers;

import com.florianingerl.util.regex.Matcher;
import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;

import java.text.NumberFormat;
import java.util.Locale;

import static com.isaacapps.unitconverterapp.utilities.RegExUtility.SIGNED_DOUBLE_VALUE_REGEX_PATTERN;

public class CurrencyFormatter implements IFormatter {
    private Locale locale;
    private NumberFormat currencyNumberFormat;

    public CurrencyFormatter(Locale locale) {
        setLocale(locale);
    }

    @Override
    public String format(String currencyNumber) {
        String formattedCurrencyNumber = currencyNumber;

        Matcher currencyInputMatcher = SIGNED_DOUBLE_VALUE_REGEX_PATTERN.matcher(currencyNumber);
        while(currencyInputMatcher.find())
            formattedCurrencyNumber = currencyNumber.replace(currencyInputMatcher.group(), currencyNumberFormat.format(Double.valueOf(currencyInputMatcher.group())));

        return formattedCurrencyNumber;
    }

    @Override
    public Locale getLocale() {
        return this.locale;
    }

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
        currencyNumberFormat = NumberFormat.getCurrencyInstance(locale);
    }
}
