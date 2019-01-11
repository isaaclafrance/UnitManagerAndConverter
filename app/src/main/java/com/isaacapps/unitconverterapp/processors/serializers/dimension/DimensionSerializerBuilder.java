package com.isaacapps.unitconverterapp.processors.serializers.dimension;

import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;
import com.isaacapps.unitconverterapp.processors.serializers.ISerializer;
import com.isaacapps.unitconverterapp.processors.serializers.SerializingException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class DimensionSerializerBuilder<T> implements ISerializer<Map<T, Double>> {
    private Locale locale;

    private ISerializer<T> dimensionItemSerializer;

    private String multiplicationSymbol, divisionSymbol, exponentSymbol;
    private boolean includeParenthesesInExponentials;

    private IFormatter dimensionKeyFormatter;
    private IFormatter dimensionValueFormatter;

    public enum OPERATOR_DISPLAY_CONFIGURATION {
        /**
         * Determines whether to represent each instance of negative exponents with a division operator and a positive exponent.
         * ie. a^2 * c^-1 instead becomes a^2 / c.
         */
        DIVISION_PER_NEGATIVE_EXPONENT
        /**
         * Determines whether to wrap all negative exponential items under one division.
         */
        , SINGLE_DIVISION
        /**
         * Negative exponents are maintained.
         */
        , NO_DIVISION
    }
    private OPERATOR_DISPLAY_CONFIGURATION operatorDisplayConfiguration;

    private Comparator<Map.Entry<T, Double>> dimensionEntrySetComparator;

    ///
    public DimensionSerializerBuilder(){
        operatorDisplayConfiguration = OPERATOR_DISPLAY_CONFIGURATION.NO_DIVISION;
    }

    ///
    public String serialize(Map<T, Double> dimensionMap) throws SerializingException {
        if(dimensionMap.isEmpty())
            return "";

        SerializingException.validateRequiredComponentsCollection(determineMissingSerializationComponentsNeededForStringGeneration());

        StringBuilder dimensionStringBuilder = new StringBuilder();

        List<Map.Entry<T, Double>> dimensionMapEntries = new ArrayList<>(dimensionMap.entrySet());
        if(dimensionEntrySetComparator != null){
            Collections.sort(dimensionMapEntries, dimensionEntrySetComparator);
        }

        boolean hasDivision = false;
        boolean hasMultiplication = false;
        boolean someDimensionItemsHavePositiveExponents = false;
        for (Map.Entry<T, Double> dimensionEntry : dimensionMapEntries) {
            //
            if( dimensionEntry.getValue() < 0 && (operatorDisplayConfiguration == OPERATOR_DISPLAY_CONFIGURATION.DIVISION_PER_NEGATIVE_EXPONENT || operatorDisplayConfiguration == OPERATOR_DISPLAY_CONFIGURATION.SINGLE_DIVISION && !hasDivision)){
                dimensionStringBuilder.append(divisionSymbol).append(" ");
                hasDivision = true;
            }
            else if(dimensionStringBuilder.length() > 0 && !( dimensionEntry.getValue() < 0 && operatorDisplayConfiguration == OPERATOR_DISPLAY_CONFIGURATION.DIVISION_PER_NEGATIVE_EXPONENT )) {
                dimensionStringBuilder.append(multiplicationSymbol).append(" ");
                hasMultiplication = true;
            }

            //
            String dimensionEntryKeyName = dimensionKeyFormatter.format(dimensionItemSerializer.serialize(dimensionEntry.getKey()));

            if (dimensionEntry.getValue() == 1 || ( operatorDisplayConfiguration == OPERATOR_DISPLAY_CONFIGURATION.DIVISION_PER_NEGATIVE_EXPONENT && dimensionEntry.getValue() == -1 )) {
                dimensionStringBuilder.append(dimensionEntryKeyName);
            } else if (Math.abs(dimensionEntry.getValue()) > 0) { //Ignore calculated dimensions raised to zero in string representation.
                if(includeParenthesesInExponentials) {
                    dimensionStringBuilder.append("(").append(dimensionEntryKeyName).append(")");
                }else {
                    dimensionStringBuilder.append(dimensionEntryKeyName);
                }

                boolean exponentShouldBePositive = operatorDisplayConfiguration == OPERATOR_DISPLAY_CONFIGURATION.SINGLE_DIVISION || operatorDisplayConfiguration == OPERATOR_DISPLAY_CONFIGURATION.DIVISION_PER_NEGATIVE_EXPONENT && dimensionEntry.getValue() < 0;
                Double exponentValue = exponentShouldBePositive ? Math.abs(dimensionEntry.getValue()):dimensionEntry.getValue();
                dimensionStringBuilder.append(exponentSymbol).append(dimensionValueFormatter.format(exponentValue.toString()));
            }

            someDimensionItemsHavePositiveExponents = someDimensionItemsHavePositiveExponents || dimensionEntry.getValue() > 0;
        }

        //For reciprocal proper grouping purposes, parenthesis will be included in this case regardless if setIncludeParenthesesInExponentials was specified as true.
        if(dimensionMapEntries.size() != 1 && operatorDisplayConfiguration == OPERATOR_DISPLAY_CONFIGURATION.SINGLE_DIVISION  && hasDivision) {
            int index = dimensionStringBuilder.indexOf(divisionSymbol);
            dimensionStringBuilder.insert(index+1, "(");
            dimensionStringBuilder.append(" )");
        }

        //If any dimension items of negative exponents, then show as reciprocal. ie. 1/x or 1/(x * y * z)
        if((operatorDisplayConfiguration == OPERATOR_DISPLAY_CONFIGURATION.SINGLE_DIVISION && !someDimensionItemsHavePositiveExponents)
                || (operatorDisplayConfiguration == OPERATOR_DISPLAY_CONFIGURATION.DIVISION_PER_NEGATIVE_EXPONENT && (!hasMultiplication && dimensionStringBuilder.indexOf(divisionSymbol) == 0))){
            dimensionStringBuilder.insert(0, "1");
        }

        return dimensionStringBuilder.toString();
    }

    ///
    public List<String> determineMissingSerializationComponentsNeededForStringGeneration() {
        List<String> missingStringGenerationComponents = new ArrayList<>();

        missingStringGenerationComponents.addAll(determineMissingFormatterComponentsNeededForStringGeneration());
        missingStringGenerationComponents.addAll(determineMissingOperationComponentsNeededForStringGeneration());

        if(dimensionItemSerializer == null)
            missingStringGenerationComponents.add("{ Dimension Item Serializer }");

        return missingStringGenerationComponents;
    }
    public List<String> determineMissingOperationComponentsNeededForStringGeneration() {
        List<String> missingStringGenerationComponents = new ArrayList<>();

        if (multiplicationSymbol == null || multiplicationSymbol.isEmpty())
            missingStringGenerationComponents.add("{ Multiplication symbol }");

        if (operatorDisplayConfiguration != OPERATOR_DISPLAY_CONFIGURATION.NO_DIVISION && ( divisionSymbol == null || divisionSymbol.isEmpty()))
            missingStringGenerationComponents.add("{ Division symbol }");

        if (exponentSymbol == null || exponentSymbol.isEmpty())
            missingStringGenerationComponents.add("{ Exponent symbol }");

        return missingStringGenerationComponents;
    }
    public List<String> determineMissingFormatterComponentsNeededForStringGeneration() {
        List<String> missingStringGenerationComponents = new ArrayList<>();

        if(dimensionKeyFormatter == null)
            missingStringGenerationComponents.add("{ Dimension Key Formatter }");

        if(dimensionValueFormatter == null)
            missingStringGenerationComponents.add("{ Dimension Value Formatter }");

        if(locale == null)
            missingStringGenerationComponents.add("{ Locale }");

        return missingStringGenerationComponents;
    }

    ///
    public DimensionSerializerBuilder<T> setDimensionItemSerializer(ISerializer<T> dimensionItemSerializer) {
        this.dimensionItemSerializer = dimensionItemSerializer;
        return this;
    }

    ///
    public DimensionSerializerBuilder<T> setMultiplicationSymbol(String multiplicationSymbol) {
        this.multiplicationSymbol = multiplicationSymbol.trim();
        return this;
    }
    public DimensionSerializerBuilder<T> setDivisionSymbol(String divisionSymbol) {
        this.divisionSymbol = divisionSymbol.trim();
        return this;
    }
    public DimensionSerializerBuilder<T> setExponentSymbol(String exponentSymbol) {
        this.exponentSymbol = exponentSymbol;
        return this;
    }

    public DimensionSerializerBuilder<T> setIncludeParenthesesInExponentials(boolean includeParentheses) {
        this.includeParenthesesInExponentials = includeParentheses;
        return this;
    }
    public boolean isIncludeParenthesesInExponentials(){
        return includeParenthesesInExponentials;
    }

    ///
    public DimensionSerializerBuilder<T> setDimensionKeyFormatter(IFormatter dimensionKeyFormatter) {
        this.dimensionKeyFormatter = dimensionKeyFormatter;
        this.dimensionKeyFormatter.setLocale(locale);
        return this;
    }
    public DimensionSerializerBuilder<T> setDimensionValueFormatter(IFormatter dimensionValueFormatter) {
        this.dimensionValueFormatter = dimensionValueFormatter;
        this.dimensionValueFormatter.setLocale(locale);
        return this;
    }

    /**
     * Determines how to order the appearance of dimension items
     */
    public DimensionSerializerBuilder<T> setDimensionEntrySetComparator(Comparator<Map.Entry<T, Double>> dimensionEntrySetComparator) {
        this.dimensionEntrySetComparator = dimensionEntrySetComparator;
        return this;
    }
    public Comparator<Map.Entry<T, Double>> getDimensionEntrySetComparator(){
        return dimensionEntrySetComparator;
    }

    public DimensionSerializerBuilder<T> setOperatorDisplayConfiguration(OPERATOR_DISPLAY_CONFIGURATION operatorDisplayConfiguration){
        this.operatorDisplayConfiguration = operatorDisplayConfiguration;
        return this;
    }
    public OPERATOR_DISPLAY_CONFIGURATION getOperatorDisplayConfiguration(){
        return operatorDisplayConfiguration;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;

        if(dimensionValueFormatter != null)
            dimensionValueFormatter.setLocale(locale);
        if(dimensionKeyFormatter != null)
            dimensionKeyFormatter.setLocale(locale);
    }
    public Locale getLocale() {
        return locale;
    }
}
