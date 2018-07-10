package com.isaacapps.unitconverterapp.processors.formatters.numbers;

import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyFormatter implements IFormatter {
    private Locale locale;

    public CurrencyFormatter(Locale locale) {
        this.locale = locale;
    }

    @Override
    public String format(String number) {
        return NumberFormat.getCurrencyInstance(locale).format(number);
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
