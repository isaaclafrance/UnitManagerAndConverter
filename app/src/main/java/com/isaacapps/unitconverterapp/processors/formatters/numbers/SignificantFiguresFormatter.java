package com.isaacapps.unitconverterapp.processors.formatters.numbers;

import com.florianingerl.util.regex.Matcher;
import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;

import java.util.Locale;

import static com.isaacapps.unitconverterapp.utilities.RegExUtility.SIGNED_DOUBLE_VALUE_REGEX_PATTERN;

public class SignificantFiguresFormatter implements IFormatter {
    private Locale locale;
    private int numberOfSignificantFigures = 6;

    public SignificantFiguresFormatter(Locale locale) {
        this.locale = locale;
    }
    public SignificantFiguresFormatter(Locale locale, int numberOfSignificantFigures){
        this(locale);
        this.numberOfSignificantFigures = numberOfSignificantFigures;
    }

    @Override
    /**
     * Produces a new formatted text where each instance of a double is transformed into another number satisfying specified significant figures.
     * If there are no suitable instances, then the string is returned as is.
     */
    public String format(String textWithDoubles) {
        String formattedText = textWithDoubles;

        Matcher decimalInputMatcher = SIGNED_DOUBLE_VALUE_REGEX_PATTERN.matcher(textWithDoubles);
        while(decimalInputMatcher.find())
            formattedText = textWithDoubles.replace(decimalInputMatcher.group(), String.format(locale,"%." + numberOfSignificantFigures + "g", decimalInputMatcher.group()));

        return formattedText;
    }

    ///
    public void setNumberOfSignificantFigures(int numberOfSignificantFigures) {
        this.numberOfSignificantFigures = numberOfSignificantFigures;
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
