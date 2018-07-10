package com.isaacapps.unitconverterapp.processors.formatters.numbers;

import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DecimalToMixedFractionFormatter implements IFormatter {
    private Locale locale;
    private Pattern decimalPartPattern = Pattern.compile("(?<=[.,])\\d+\\Z");
    private Pattern nonDecimalPartPattern = Pattern.compile("\\d+(?=[.,])");
    private int nearestDenominator;

    public DecimalToMixedFractionFormatter(Locale locale) {
        this.locale = locale;
        nearestDenominator = 1;
    }
    public DecimalToMixedFractionFormatter(Locale locale, int nearestDenominator) {
        this.locale = locale;
        this.nearestDenominator = nearestDenominator;
    }

    @Override
    public String format(String numWithDecimal) {
        return convertToMixedNumber(numWithDecimal.trim());
    }

    private String convertToMixedNumber(String numWithDecimal){
        return String.format(locale, "%s %s", extractNonDecimalPart(numWithDecimal)
                , calculateReducedFraction(extractDecimalAsNumerator(numWithDecimal)
                        , extractDecimalAsNumerator(numWithDecimal)));
    }

    private String extractNonDecimalPart(String numWithDecimal){
        Matcher nonDecimalPartMatcher = nonDecimalPartPattern.matcher(numWithDecimal);
        if(nonDecimalPartMatcher.find()){
            return nonDecimalPartMatcher.group();
        }
        else{
            return "";
        }
    }

    private String extractDecimalPart(String numWithDecimal){
        Matcher decimalPartMatcher = decimalPartPattern.matcher(numWithDecimal);
        if(decimalPartMatcher.find()){
            return decimalPartMatcher.group();
        }
        else{
            return "";
        }
    }
    private int extractDecimalAsNumerator(String numWithDecimal){
        String decimalPart = extractDecimalPart(numWithDecimal);
        if(!decimalPart.isEmpty()){
            return Integer.parseInt(decimalPart);
        }
        else{
            return 0;
        }
    }
    private int extractDecimalAsDenominator(String numWithDecimal){
        String decimalPart = extractDecimalPart(numWithDecimal);
        if(!decimalPart.isEmpty()){
            return (int)Math.pow(10,decimalPart.length());
        }
        else{
            return 1;
        }
    }

    private int determineGreatestCommonFactor(int num1, int num2){
        int remainder = num1 % num2;
        while(remainder != 0){
            num1 = num2;
            num2 = remainder;
            remainder = num1 % num2;
        }
        return num2;
    }

    private String calculateReducedFraction(int numerator, int denominator){
        int gcd = determineGreatestCommonFactor(numerator, denominator);
        double reducedNumerator = numerator/gcd;
        double reducedDenominator = denominator/gcd;

        int closestNumeratorForNearestDenominator = (int)Math.round(((nearestDenominator) * reducedNumerator)/reducedDenominator);

        return String.format(locale, "%s / %s", closestNumeratorForNearestDenominator, nearestDenominator);
    }

    ///
    /**
     * Demoninator of the nearest fraction to which the formatted value will be rounded to.
     * @param nearestDenominator
     */
    public void setNearestDenominator(int nearestDenominator) {
        int absNearestDenominator = Math.abs(nearestDenominator);
        this.nearestDenominator = absNearestDenominator == 0 ? 1 : absNearestDenominator;
    }

    ///
    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}
