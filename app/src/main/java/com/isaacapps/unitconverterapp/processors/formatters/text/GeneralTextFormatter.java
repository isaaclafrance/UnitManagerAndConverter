package com.isaacapps.unitconverterapp.processors.formatters.text;

import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;

import java.util.Locale;
import java.util.regex.Pattern;

public class GeneralTextFormatter implements IFormatter{
    private Locale locale;
    private final static Pattern multipleWhiteSpacesPattern = Pattern.compile("\\s{2,}");

    public GeneralTextFormatter(Locale locale){
        this.locale = locale;
    }

    /**
     * Trims white spaces from beginning and end. Reduces unnecessary white spaces between words.
     */
    @Override
    public String format(String text) {
        return multipleWhiteSpacesPattern.matcher(String.format(locale,"%s", text).trim()).replaceAll(" ");
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
