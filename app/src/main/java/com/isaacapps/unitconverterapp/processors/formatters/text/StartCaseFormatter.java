package com.isaacapps.unitconverterapp.processors.formatters.text;

import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StartCaseFormatter implements IFormatter {
    private Locale locale;
    private final Pattern firstLowerCaseLetterPattern = Pattern.compile("(?<=^|[^a-zA-Z])[a-z]");

    public StartCaseFormatter(Locale locale){
        this.locale = locale;
    }

    @Override
    public String format(String text) {
        char[] textArray = text.toCharArray();

        Matcher firstLowerCaseLetterMatcher = firstLowerCaseLetterPattern.matcher(text);
        while(firstLowerCaseLetterMatcher.find())
            textArray[firstLowerCaseLetterMatcher.start()] = firstLowerCaseLetterMatcher.group().toUpperCase(locale).charAt(0);

        return new String(textArray);
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
