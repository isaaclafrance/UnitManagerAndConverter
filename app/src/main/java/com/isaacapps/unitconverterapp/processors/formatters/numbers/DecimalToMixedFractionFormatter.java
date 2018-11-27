package com.isaacapps.unitconverterapp.processors.formatters.numbers;

import com.florianingerl.util.regex.Matcher;
import com.florianingerl.util.regex.Pattern;
import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;

import java.util.Locale;

import static com.isaacapps.unitconverterapp.utilities.RegExUtility.SIGNED_DOUBLE_VALUE_REGEX_PATTERN;

public class DecimalToMixedFractionFormatter implements IFormatter {
    /*Product of all successive primes less than and equal to 43. Due to precision limitations chose 43 as cut-off, but the large the number the better
     since it would make it would make it somewhat easier to find reduced fractions for nonterminating and irregular decimals. */
    public final double HIGH_DIVISIBLE_DENOMINATOR = 6541380665835015.0;
    public final double GCD_TOLERANCE = 0.000001; // used as a precision parameter.

    private Locale locale;
    private final Pattern decimalFractionPartPattern = Pattern.compile("(?<=[.,])\\d+\\Z");
    private final Pattern wholeNumberPartPattern = Pattern.compile("\\d+(?=[.,])");
    private final Pattern decimalPattern = SIGNED_DOUBLE_VALUE_REGEX_PATTERN;
    private double nearestDenominator;
    private double gcdTolerance;

    ///
    public DecimalToMixedFractionFormatter(Locale locale) {
        this.locale = locale;
        nearestDenominator = HIGH_DIVISIBLE_DENOMINATOR;
        gcdTolerance = GCD_TOLERANCE;
    }
    public DecimalToMixedFractionFormatter(Locale locale, int nearestDenominator, double gcdTolerance) {
        this.locale = locale;
        this.nearestDenominator = nearestDenominator;
        this.gcdTolerance = gcdTolerance;
    }

    ///
    @Override
    public String format(String decimalInput) {
        String formattedDecimal = decimalInput;

        Matcher decimalInputMatcher = decimalPattern.matcher(decimalInput);
        while(decimalInputMatcher.find())
            formattedDecimal = decimalInput.replace(decimalInputMatcher.group(), convertToMixedNumber(decimalInputMatcher.group()));

        return formattedDecimal;
    }

    ///
    private String convertToMixedNumber(String numWithDecimal){
        try {
            String nonDecimalPart = extractNonDecimalPart(numWithDecimal);
            return String.format(locale, "%s %s", Integer.parseInt(nonDecimalPart) == 0 ? "" : nonDecimalPart
                    , calculateEquivalentFractionString(extractDecimalAsNumerator(numWithDecimal), extractDecimalAsDenominator(numWithDecimal)));
        }
        catch(NumberFormatException e){
            return "";
        }
    }

    private String extractNonDecimalPart(String numWithDecimal){
        Matcher nonDecimalPartMatcher = wholeNumberPartPattern.matcher(numWithDecimal);
        if(nonDecimalPartMatcher.find()){
            return nonDecimalPartMatcher.group();
        }
        else{
            return "";
        }
    }

    private String extractDecimalPart(String numWithDecimal){
        Matcher decimalPartMatcher = decimalFractionPartPattern.matcher(numWithDecimal);
        if(decimalPartMatcher.find()){
            return decimalPartMatcher.group();
        }
        else{
            return "";
        }
    }
    private double extractDecimalAsNumerator(String numWithDecimal){
        String decimalPart = extractDecimalPart(numWithDecimal);
        if(!decimalPart.isEmpty()){
            return Double.parseDouble(decimalPart);
        }
        else{
            return 0.0;
        }
    }
    private double extractDecimalAsDenominator(String numWithDecimal){
        String decimalPart = extractDecimalPart(numWithDecimal);
        if(!decimalPart.isEmpty()){
            return Math.pow(10,decimalPart.length());
        }
        else{
            return 1.0;
        }
    }

    ///
    private double determineGreatestCommonFactor(double num1, double num2){
        double remainder = num1 % num2;
        while(remainder != 0){
            if(remainder/num2 <= gcdTolerance)
                return num2 - remainder;

            num1 = num2;
            num2 = remainder;
            remainder = num1 % num2;
        }
        return num2;
    }
    private String calculateEquivalentFractionString(double numerator, double denominator){
        double[] finalFraction = calculateFractionToNearestDenominator(numerator, denominator, this.nearestDenominator);

        if(this.nearestDenominator == HIGH_DIVISIBLE_DENOMINATOR)
            finalFraction = calculateReducedFractions(finalFraction[0], finalFraction[1]);

        return String.format(locale, "%.0f / %.0f", finalFraction[0], finalFraction[1]);
    }
    private double[] calculateReducedFractions(double numerator, double denominator){
        double gcd = determineGreatestCommonFactor(numerator, denominator);
        return new double[]{numerator/gcd, denominator/gcd};
    }
    private double[] calculateFractionToNearestDenominator(double numerator, double denominator, double currentNearestDenominator){
        double closestNumeratorForNearestDenominator = Math.round((currentNearestDenominator * numerator)/denominator);
        return new double[]{closestNumeratorForNearestDenominator, currentNearestDenominator};
    }

    ///
    /**
     * Denominator of the nearest fraction to which the formatted value will be rounded to.
     */
    public void setNearestDenominator(double nearestDenominator) {
        double absNearestDenominator = Math.abs(nearestDenominator);
        this.nearestDenominator = absNearestDenominator == 0 ? HIGH_DIVISIBLE_DENOMINATOR : absNearestDenominator;
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
