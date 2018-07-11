package com.isaacapps.unitconverterapp.processors.formatters;

import java.util.Locale;

public interface IFormatter {
    String format(String text);

    Locale getLocale();

    void setLocale(Locale locale);
}
