package com.isaacapps.unitconverterapp.processors.formatters.text;

import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;

import java.util.Locale;
import java.util.regex.Pattern;

public class SnakeCaseFormatter implements IFormatter {
    private Locale locale;
    private Pattern oneOrMoreWhiteSpacesPattern = Pattern.compile("\\s+");

    public SnakeCaseFormatter(Locale locale){
        this.locale = locale;
    }

    @Override
    public String format(String text) {
        return oneOrMoreWhiteSpacesPattern.matcher(text).replaceAll("_");
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
