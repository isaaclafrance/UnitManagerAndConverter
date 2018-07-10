package com.isaacapps.unitconverterapp.processors.formatters.text;

import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;

import java.util.Locale;

public class GeneralTextFormatter implements IFormatter{
    private Locale locale;

    public GeneralTextFormatter(Locale locale){
        this.locale = locale;
    }

    /**
     * Trims white spaces from begining and end. Reduces unecessary white spaces between words.
     */
    @Override
    public String format(String text) {
        return String.format(locale,"%s", text).trim().replaceAll("\\s{2,}", " ");
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
