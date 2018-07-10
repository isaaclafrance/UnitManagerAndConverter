package com.isaacapps.unitconverterapp.processors.formatters.numbers;

import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;
import com.isaacapps.unitconverterapp.utilities.RegExUtility;

import java.util.Locale;

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
        if(number.matches(RegExUtility.SIGNED_DOUBLE_VALUE_REGEX))
            return String.format(locale,"%." + decimalPlaces + "f", Double.valueOf(number)).toString();
        else
            return number;
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
