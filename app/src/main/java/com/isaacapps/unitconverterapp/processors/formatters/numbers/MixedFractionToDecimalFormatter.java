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
    public static final Pattern MIXED_PATTERN = Pattern.compile(String.format("%1$s\\s+%2$s|%2$s", INTEGER_PATTERN.pattern(), FRACTION_PATTERN.pattern()));
    private static final Pattern WHOLE_NUMBER_PART_PATTERN = Pattern.compile(String.format("%s(?=\\s*%s\\s*)", INTEGER_PATTERN.pattern(), FRACTION_PATTERN.pattern()));

    private IFormatter resultantDecimalFormatter;

    ///
    public MixedFractionToDecimalFormatter(Locale locale) {
        this.locale = locale;
    }

    ///
    @Override
    public String format(String mixedInput) {
        String formattedMixedInput = mixedInput;

        Matcher mixedInputMatcher = MIXED_PATTERN.matcher(mixedInput);
        while(mixedInputMatcher.find())
            formattedMixedInput = mixedInput.replace(mixedInputMatcher.group(), convertMixedNumberToDecimal(mixedInputMatcher.group()));

        return formattedMixedInput;
    }

    ///
    private String convertMixedNumberToDecimal(String mixedInput){
        String fractionPart = extractFractionPart(mixedInput);
        String numerator = extractNumeratorOfFractionPart(fractionPart);
        String denominator = extractDenominatorOfFractionPart(fractionPart);
        String fractionAsDecimal = (!(numerator+denominator).isEmpty())
                ? Double.toString(Double.parseDouble(numerator)/Double.parseDouble(denominator))
                :"0.0";

        String wholeNumberPart = extractWholeNumberPart(mixedInput);

        return String.format("%s%s",wholeNumberPart.isEmpty()?"0":wholeNumberPart,fractionAsDecimal.replaceFirst("\\d+(?=\\.)", ""));
    }

    private String extractFractionPart(String mixedInput){
        Matcher fractionPartMatcher = FRACTION_PATTERN.matcher(mixedInput);
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
    public boolean hasWholeNumber(String numberWithPossiblePart){
        return WHOLE_NUMBER_PART_PATTERN.matcher(numberWithPossiblePart).find();
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
