package com.isaacapps.unitconverterapp.processors.serializers.dimension;

import com.isaacapps.unitconverterapp.processors.formatters.numbers.RoundingFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.text.GeneralTextFormatter;
import com.isaacapps.unitconverterapp.processors.serializers.ISerializer;
import com.isaacapps.unitconverterapp.processors.serializers.SerializingException;

import java.util.Locale;
import java.util.Map;

import static com.isaacapps.unitconverterapp.processors.parsers.dimension.DimensionComponentDefinerBuilder.DEFAULT_DIVISION_SYMBOL_GROUPS;
import static com.isaacapps.unitconverterapp.processors.parsers.dimension.DimensionComponentDefinerBuilder.DEFAULT_EXPONENT_SYMBOL_GROUPS;
import static com.isaacapps.unitconverterapp.processors.parsers.dimension.DimensionComponentDefinerBuilder.DEFAULT_MULTIPLICATION_SYMBOL_GROUPS;

public class BaseDimensionSerializer<T> implements ISerializer<Map<T, Double>> {
    protected Locale locale;
    protected boolean includeParentheses;
    protected DimensionSerializerBuilder<T> dimensionSerializerBuilder;

    ///
    public BaseDimensionSerializer(Locale locale) {
        this.locale = locale;
        dimensionSerializerBuilder = new DimensionSerializerBuilder<T>()
                .setDivisionSymbolGroupsForStringGeneration(DEFAULT_DIVISION_SYMBOL_GROUPS[0])
                .setMultiplicationSymbolForStringGeneration(DEFAULT_MULTIPLICATION_SYMBOL_GROUPS[0])
                .setExponentSymbolGroupsForStringGeneration(DEFAULT_EXPONENT_SYMBOL_GROUPS[0])
                .setDimensionKeyFormatter(new GeneralTextFormatter(locale))
                .setDimensionValueFormatter(new RoundingFormatter(locale))
                .setLocale(locale);
    }
    public BaseDimensionSerializer(Locale locale, DimensionSerializerBuilder dimensionSerializerBuilder) {
        this.dimensionSerializerBuilder = dimensionSerializerBuilder;
        dimensionSerializerBuilder.setLocale(locale);
    }

    ///
    @Override
    public String serialize(Map<T, Double> dimensionMap) throws SerializingException {
        return dimensionSerializerBuilder.includeParenthesesInStringGeneration(includeParentheses)
                .serialize(dimensionMap);
    }

    //
    public boolean isIncludeParentheses() {
        return includeParentheses;
    }

    public void setIncludeParentheses(boolean includeParentheses) {
        this.includeParentheses = includeParentheses;
    }

    ///
    public Locale getLocale(){
        return locale;
    }

    public void setLocale(Locale locale){
        this.locale = locale;
        dimensionSerializerBuilder.setLocale(locale);
    }
}
