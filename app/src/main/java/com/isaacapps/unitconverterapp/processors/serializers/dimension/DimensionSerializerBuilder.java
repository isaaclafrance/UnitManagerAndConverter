package com.isaacapps.unitconverterapp.processors.serializers.dimension;

import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;
import com.isaacapps.unitconverterapp.processors.serializers.ISerializer;
import com.isaacapps.unitconverterapp.processors.serializers.SerializingException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class DimensionSerializerBuilder<T> implements ISerializer<Map<T, Double>> {
    private Locale locale;

    private ISerializer<T> dimensionItemSerializer;

    private String multiplicationSymbolForStringGeneration, divisionSymbolForStringGeneration, exponentSymbolForStringGeneration;
    private boolean includeParentheses;

    private IFormatter dimensionKeyFormatter;
    private IFormatter dimensionValueFormatter;

    private boolean replaceNegativeExponentWithDivision;
    private boolean orderDimensionEntries;

    private Comparator<Map.Entry<T, Double>> dimensionEntrySetComparator;

    ///
    public DimensionSerializerBuilder(){
        //Orders dimension entries in decreasing order of exponent values
        dimensionEntrySetComparator = new Comparator<Map.Entry<T, Double>>() {
            @Override
            public int compare(Map.Entry<T, Double> lhsDimensionEntry, Map.Entry<T, Double> rhsDimensionEntry) {
                return -1 * Double.compare(lhsDimensionEntry.getValue(), rhsDimensionEntry.getValue());
            }
        };
    }

    ///
    public String serialize(Map<T, Double> dimensionMap) throws SerializingException {
        if(dimensionMap.isEmpty())
            return "";

        SerializingException.validateRequiredComponentsCollection(determineMissingSerializationComponentsNeededForStringGeneration());

        StringBuilder dimensionStringBuilder = new StringBuilder();

        Set<Map.Entry<T, Double>> dimensionMapEntrySet;
        if(orderDimensionEntries){
            dimensionMapEntrySet = new TreeSet<>(dimensionEntrySetComparator);
            dimensionMapEntrySet.addAll(dimensionMap.entrySet());
        }
        else{
            dimensionMapEntrySet = dimensionMap.entrySet();
        }

        for (Map.Entry<T, Double> dimensionEntry : dimensionMapEntrySet) {
            if(dimensionStringBuilder.length() > 0 ) {
                String  operatorSymbol = ( replaceNegativeExponentWithDivision && dimensionEntry.getValue() < 0 ) ? divisionSymbolForStringGeneration: multiplicationSymbolForStringGeneration;
                dimensionStringBuilder.append(" ").append(operatorSymbol.trim()).append(" ");
            }
            else if(replaceNegativeExponentWithDivision && dimensionEntry.getValue() < 0 && dimensionMapEntrySet.size() == 1){
                dimensionStringBuilder.append("1 ").append(divisionSymbolForStringGeneration).append(" "); // if only dimension entry, then show as reciprocal. ie. 1/x
            }

            String dimensionEntryKeyName = dimensionKeyFormatter.format(dimensionItemSerializer.serialize(dimensionEntry.getKey()));

            if (dimensionEntry.getValue() == 1 || ( replaceNegativeExponentWithDivision && dimensionEntry.getValue() == -1 )) {
                dimensionStringBuilder.append(dimensionEntryKeyName);
            } else if (Math.abs(dimensionEntry.getValue()) > 0) { //Ignore calculated dimensions raised to zero in string representation.
                if(includeParentheses) {
                    dimensionStringBuilder.append("(").append(dimensionEntryKeyName).append(")");
                }else {
                    dimensionStringBuilder.append(dimensionEntryKeyName);
                }

                Double exponentValue = (replaceNegativeExponentWithDivision && dimensionEntry.getValue() < 0 )? Math.abs(dimensionEntry.getValue()):dimensionEntry.getValue();
                dimensionStringBuilder.append(exponentSymbolForStringGeneration).append(dimensionValueFormatter.format(exponentValue.toString()));
            }
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

        if (multiplicationSymbolForStringGeneration == null || multiplicationSymbolForStringGeneration.isEmpty())
            missingStringGenerationComponents.add("{ Multiplication symbol }");

        if (divisionSymbolForStringGeneration == null || divisionSymbolForStringGeneration.isEmpty())
            missingStringGenerationComponents.add("{ Division symbol }");

        if (exponentSymbolForStringGeneration == null || exponentSymbolForStringGeneration.isEmpty())
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
    public DimensionSerializerBuilder<T> setMultiplicationSymbolForStringGeneration(String multiplicationSymbol) {
        this.multiplicationSymbolForStringGeneration = multiplicationSymbol;
        return this;
    }

    public DimensionSerializerBuilder<T> setDivisionSymbolForStringGeneration(String divisionSymbol) {
        this.divisionSymbolForStringGeneration = divisionSymbol;
        return this;
    }

    public DimensionSerializerBuilder<T> setExponentSymbolForStringGeneration(String exponentSymbol) {
        this.exponentSymbolForStringGeneration = exponentSymbol;
        return this;
    }

    public DimensionSerializerBuilder<T> includeParenthesesInStringGeneration(boolean includeParentheses) {
        this.includeParentheses = includeParentheses;
        return this;
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
     * Determines whether to order appearance of dimension items based on decreasing exponential power.
     * ie. a^3 * b^2 * c
     */
    public void setOrderDimensionEntries(boolean state){
        this.orderDimensionEntries = state;
    }

    /**
     * Determines whether to represent negative exponents with a division operator and a positive exponent.
     * ie. a^2 * c^-1 instead becomes a^2 / c.
     */
    public void setReplaceNegativeExponentWithDivision(boolean state){
        this.replaceNegativeExponentWithDivision = state;
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
