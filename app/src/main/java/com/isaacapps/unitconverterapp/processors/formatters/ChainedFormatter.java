package com.isaacapps.unitconverterapp.processors.formatters;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChainedFormatter implements IFormatter {
    private Locale locale;
    private List<IFormatter> formatters;

    public ChainedFormatter(Locale locale){
        formatters = new ArrayList<>();
        this.locale = locale;
    }

    @Override
    public String format(String text) {
        String finalFormattedText = text;
        for(IFormatter formatter:formatters)
            finalFormattedText = formatter.format(finalFormattedText);
        return finalFormattedText;
    }

    public ChainedFormatter AddFormatter(IFormatter formatter){
        formatters.add(formatter);
        return this;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
        for(IFormatter formatter:formatters)
            formatter.setLocale(locale);
    }
}
