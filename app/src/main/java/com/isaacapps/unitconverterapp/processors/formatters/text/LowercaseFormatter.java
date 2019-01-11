package com.isaacapps.unitconverterapp.processors.formatters.text;

import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;

import java.util.Locale;

public class LowercaseFormatter implements IFormatter {
    private Locale locale;

    public LowercaseFormatter(Locale locale){
        this.locale = locale;
    }

    @Override
    public String format(String text) {
        return text.toUpperCase(locale);
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
