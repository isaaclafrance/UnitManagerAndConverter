package com.isaacapps.unitconverterapp.processors.formatters.numbers;

import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MixedFractionToDecimalFormatter implements IFormatter {
    private Locale locale;
    private static final Pattern INTEGER_PATTERN = Pattern.compile("(-)?\\d+");
    public static final Pattern DENOMINATOR_PATTERN = Pattern.compile(String.format("(?<=\\/)s*%s", INTEGER_PATTERN.pattern()));
    public static final Pattern NUMERATOR_PATTERN = Pattern.compile(String.format("%s\\s*(?=\\/)", INTEGER_PATTERN.pattern()));
    public static final Pattern FRACTION_PATTERN = Pattern.compile(String.format("%s\\s*\\/\\s*%<s", INTEGER_PATTERN.pattern()));
    public static final Pattern MIXED_NUMBER_PATTERN = Pattern.compile(String.format("%s\\s+%s", INTEGER_PATTERN.pattern(), FRACTION_PATTERN.pattern()));
    private static final Pattern WHOLE_NUMBER_PART_PATTERN = Pattern.compile(String.format("%s(?=\\s*%s\\s*)", INTEGER_PATTERN.pattern(), FRACTION_PATTERN.pattern()));

    private IFormatter resultantDecimalFormatter;

    ///
    public MixedFractionToDecimalFormatter(Locale locale) {
        this.locale = locale;
    }

    ///
    @Override
    public String format(String mixedNumber) {
        if (!isMixedNumber(mixedNumber.trim()))
            return mixedNumber;

        return convertMixedNumberToDecimal(mixedNumber.trim());
    }

    ///
    private String convertMixedNumberToDecimal(String mixedNumber){
        String fractionPart = extractFractionPart(mixedNumber);
        String numerator = extractNumeratorOfFractionPart(fractionPart);
        String denominator = extractDenominatorOfFractionPart(fractionPart);
        String fractionAsDecimal = Double.toString(Double.parseDouble(numerator)/Double.parseDouble(denominator));

        String wholeNumberPart = extractWholeNumberPart(mixedNumber);

        return String.format("%s%s",wholeNumberPart,fractionAsDecimal.replaceFirst("\\d+(?=\\.)", ""));
    }

    private String extractFractionPart(String mixedNumber){
        Matcher fractionPartMatcher = FRACTION_PATTERN.matcher(mixedNumber);
        if(fractionPartMatcher.find()){
            return fractionPartMatcher.group();
        }
        else{
            return "";
        }
    }
    private String extractNumeratorOfFractionPart(String fraction){
        Matcher numeratorMatcher = NUMERATOR_PATTERN.matcher(fraction);
        if(numeratorMatcher.find()){
            return numeratorMatcher.group();
        }
        else{
            return "";
        }
    }
    private String extractDenominatorOfFractionPart(String fraction){
        Matcher denominatorMatcher = DENOMINATOR_PATTERN.matcher(fraction);
        if(denominatorMatcher.find()){
            return denominatorMatcher.group();
        }
        else{
            return "";
        }
    }
    private String extractWholeNumberPart(String mixedNumber){
        Matcher wholeNumberPartMatcher = WHOLE_NUMBER_PART_PATTERN.matcher(mixedNumber);
        if(wholeNumberPartMatcher.find()){
            return wholeNumberPartMatcher.group();
        }
        else{
            return "";
        }
    }

    ///
    public boolean hasFraction(String numberWithPossibleFraction){
        return FRACTION_PATTERN.matcher(numberWithPossibleFraction).find();
    }
    public boolean isMixedNumber(String possibleMixedNumber){
        return MIXED_NUMBER_PATTERN.matcher(possibleMixedNumber).find() || hasFraction(possibleMixedNumber);
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
