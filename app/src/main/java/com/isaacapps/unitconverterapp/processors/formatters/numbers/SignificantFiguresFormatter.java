package com.isaacapps.unitconverterapp.processors.formatters.numbers;

import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;
import com.isaacapps.unitconverterapp.utilities.RegExUtility;

import java.util.Locale;

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
    public String format(String number) {
        if(number.matches(RegExUtility.SIGNED_DOUBLE_VALUE_REGEX))
            return String.format(locale,"%." + numberOfSignificantFigures + "g", number).toString();
        else
            return number;
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
