package com.isaacapps.unitconverterapp.processors.formatters.numbers;

import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;

import java.util.Locale;

public class ScientificNotationFormatter implements IFormatter {
    private Locale locale;
    private int numberOfDecimalPlaces;

    @Override
    /**
     * Produces a new formatted text where each instance of a double is transformed into a scientific notation representation.
     * If there are no suitable instances, then the string is returned as is.
     */
    public String format(String textWithDoubles) {
        //TODO: Implement......
        return null;
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
