package com.isaacapps.unitconverterapp.processors.formatters.text;

import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;

import java.util.Locale;

public class AbbreviationFormatter implements IFormatter {
    Locale locale;

    public AbbreviationFormatter(Locale locale){
        this.locale = locale;
    }

    /**
     * Takes the first letter of every word following a white space or an underscore
     * @param text
     * @return
     */
    @Override
    public String format(String text) {
        return text.replaceAll("(?<=[a-zA-Z])[a-zA-Z]+", "").replaceAll("[ _]+", "");
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
