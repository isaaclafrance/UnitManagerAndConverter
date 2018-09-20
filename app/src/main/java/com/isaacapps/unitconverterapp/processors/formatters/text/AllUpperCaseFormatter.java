package com.isaacapps.unitconverterapp.processors.formatters.text;

import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;

import java.util.Locale;

public class AllUpperCaseFormatter implements IFormatter {
    private Locale locale;

    public AllUpperCaseFormatter(Locale locale){ this.locale = locale; }

    @Override
    public String format(String text) {
        return text.toUpperCase();
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
