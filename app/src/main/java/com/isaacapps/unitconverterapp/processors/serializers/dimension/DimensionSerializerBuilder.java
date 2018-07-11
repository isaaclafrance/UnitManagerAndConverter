package com.isaacapps.unitconverterapp.processors.serializers.dimension;

import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;
import com.isaacapps.unitconverterapp.processors.serializers.ISerializer;
import com.isaacapps.unitconverterapp.processors.serializers.SerializingException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DimensionSerializerBuilder<T>  {
    private Locale locale;

    private ISerializer<T> dimensionItemSerializer;

    private String multiplicationSymbolForStringGeneration, divisionSymbolForStringGeneration, exponentSymbolForStringGeneration;
    private boolean includeParentheses;

    private IFormatter dimensionKeyFormatter;
    private IFormatter dimensionValueFormatter;

    ///
    public String serialize(Map<T, Double> dimensionMap) throws SerializingException {

        SerializingException.validateRequiredComponentsCollection(determineMissingSerializationComponentsNeededForStringGeneration());

        StringBuilder dimensionStringBuilder = new StringBuilder();

        for (Map.Entry<T, Double> dimensionEntry : dimensionMap.entrySet()) {
            dimensionStringBuilder.append(dimensionStringBuilder.length() > 0 ? " " + multiplicationSymbolForStringGeneration.trim() + " " : "");

            String dimensionEntryKeyName = dimensionKeyFormatter.format(dimensionItemSerializer.serialize(dimensionEntry.getKey()));

            if (dimensionEntry.getValue() == 1) {
                dimensionStringBuilder.append(dimensionEntryKeyName);
            } else if (Math.abs(dimensionEntry.getValue()) > 1) { //Ignore calculated dimensions raised to zero in string representation.
                dimensionStringBuilder.append(includeParentheses ? "(" + dimensionEntryKeyName + ")" : dimensionEntryKeyName)
                        .append(exponentSymbolForStringGeneration).append(dimensionValueFormatter.format(dimensionEntry.getValue().toString()));
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

    public DimensionSerializerBuilder<T> setDivisionSymbolGroupsForStringGeneration(String divisionSymbol) {
        this.divisionSymbolForStringGeneration = divisionSymbol;
        return this;
    }

    public DimensionSerializerBuilder<T> setExponentSymbolGroupsForStringGeneration(String exponentSymbol) {
        this.exponentSymbolForStringGeneration = exponentSymbol;
        return this;
    }

    public DimensionSerializerBuilder<T> includeParenthesesInStringGeneration(boolean includeParenthesis) {
        this.includeParentheses = includeParenthesis;
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

    public DimensionSerializerBuilder<T> setLocale(Locale locale) {
        this.locale = locale;

        if(dimensionValueFormatter != null)
            dimensionValueFormatter.setLocale(locale);
        if(dimensionKeyFormatter != null)
            dimensionKeyFormatter.setLocale(locale);

        return this;
    }

    ///
    public Locale getLocale() {
        return locale;
    }
}
