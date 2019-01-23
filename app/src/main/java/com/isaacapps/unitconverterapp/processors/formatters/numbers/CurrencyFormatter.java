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


    /**
     * Produces a new formatted text where each instance of a double is transformed into a currency representation with a currency symbol that is local specific.
     * If there are no suitable instances, then the string is returned as is.
     */
    @Override
    public String format(String textWithDouble) {
        String formattedCurrencyNumber = textWithDouble;

        Matcher currencyInputMatcher = SIGNED_DOUBLE_VALUE_REGEX_PATTERN.matcher(textWithDouble);
        while(currencyInputMatcher.find())
            formattedCurrencyNumber = textWithDouble.replace(currencyInputMatcher.group(), currencyNumberFormat.format(Double.valueOf(currencyInputMatcher.group())));

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

    public static Locale guessCurrencyLocaleBasedOnCode(String code){
        for(Locale locale:Locale.getAvailableLocales()){
            if(code.startsWith(locale.toString().split("_")[0])){
                return locale;
            }
        }
        return null;
    }
}
