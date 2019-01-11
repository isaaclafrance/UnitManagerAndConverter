package com.isaacapps.unitconverterapp.processors.serializers.dimension;

import com.isaacapps.unitconverterapp.processors.formatters.numbers.RoundingFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.text.GeneralTextFormatter;
import com.isaacapps.unitconverterapp.processors.serializers.ISerializer;
import com.isaacapps.unitconverterapp.processors.serializers.SerializingException;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.DimensionSerializerBuilder.OPERATOR_DISPLAY_CONFIGURATION;

import java.util.Comparator;
import java.util.Locale;
import java.util.Map;

import static com.isaacapps.unitconverterapp.processors.parsers.dimension.DimensionComponentDefiner.DEFAULT_DIVISION_SYMBOL_GROUPS;
import static com.isaacapps.unitconverterapp.processors.parsers.dimension.DimensionComponentDefiner.DEFAULT_EXPONENT_SYMBOL_GROUPS;
import static com.isaacapps.unitconverterapp.processors.parsers.dimension.DimensionComponentDefiner.DEFAULT_MULTIPLICATION_SYMBOL_GROUPS;

/**
 * Provides base pre-configurations upon which higher abstractions of dimension serializers can be built upon.
 * @param <T> Type of dimension item
 */
public class BaseDimensionSerializer<T> implements ISerializer<Map<T, Double>> {
    protected DimensionSerializerBuilder<T> dimensionSerializerBuilder;
    protected Comparator<Map.Entry<T, Double>> dimensionEntrySetComparator;

    ///
    public BaseDimensionSerializer(Locale locale) {
        dimensionSerializerBuilder = new DimensionSerializerBuilder<T>()
                .setDivisionSymbol(DEFAULT_DIVISION_SYMBOL_GROUPS[0])
                .setMultiplicationSymbol(DEFAULT_MULTIPLICATION_SYMBOL_GROUPS[0])
                .setExponentSymbol(DEFAULT_EXPONENT_SYMBOL_GROUPS[0])
                .setDimensionKeyFormatter(new GeneralTextFormatter(locale))
                .setDimensionValueFormatter(new RoundingFormatter(locale))
                .setOperatorDisplayConfiguration(OPERATOR_DISPLAY_CONFIGURATION.DIVISION_PER_NEGATIVE_EXPONENT);

        dimensionSerializerBuilder.setLocale(locale);

        //Orders dimension entries in decreasing order of exponent values
        dimensionEntrySetComparator = new Comparator<Map.Entry<T, Double>>() {
            @Override
            public int compare(Map.Entry<T, Double> lhsDimensionEntry, Map.Entry<T, Double> rhsDimensionEntry) {
                return -1 * Double.compare(lhsDimensionEntry.getValue(), rhsDimensionEntry.getValue());
            }
        };

        setOrderDimensionItemsByExponentialValue(true);
    }
    public BaseDimensionSerializer(Locale locale, DimensionSerializerBuilder dimensionSerializerBuilder) {
        this.dimensionSerializerBuilder = dimensionSerializerBuilder;
        dimensionSerializerBuilder.setLocale(locale);
    }

    ///

    /**
     * Transforms a dimension map into string representations. Orders dimension items based on decreasing exponential power. ie. a^3 * b^2 * c.
     * @throws SerializingException
     */
    @Override
    public String serialize(Map<T, Double> dimensionMap) throws SerializingException {
        return dimensionSerializerBuilder.serialize(dimensionMap);
    }

    //

    /**
     * Indicates whether parentheses should be included in exponentials. ie. x^2 * y^3 becomes (x)^2 * (y)^3
     */
    public boolean isIncludeParenthesesInExponentials() {
        return dimensionSerializerBuilder.isIncludeParenthesesInExponentials();
    }
    public void setIncludeParenthesesInExponentials(boolean includeParentheses) {
        dimensionSerializerBuilder.setIncludeParenthesesInExponentials(includeParentheses);
    }

    public OPERATOR_DISPLAY_CONFIGURATION getOperatorDisplayConfiguration(){
        return dimensionSerializerBuilder.getOperatorDisplayConfiguration();
    }
    public void setOperatorDisplayConfiguration(OPERATOR_DISPLAY_CONFIGURATION operatorDisplayConfiguration){
        dimensionSerializerBuilder.setOperatorDisplayConfiguration(operatorDisplayConfiguration);
    }

    public void setOrderDimensionItemsByExponentialValue(boolean isOrdered){
        if(isOrdered)
            dimensionSerializerBuilder.setDimensionEntrySetComparator(this.dimensionEntrySetComparator);
        else
            dimensionSerializerBuilder.setDimensionEntrySetComparator(null);
    }
    public boolean isDimensionItemsOrderedByExponentialValue(){
        return dimensionSerializerBuilder.getDimensionEntrySetComparator() != null;
    }

    ///
    public Locale getLocale(){
        return dimensionSerializerBuilder.getLocale();
    }

    public void setLocale(Locale locale){
        dimensionSerializerBuilder.setLocale(locale);
    }
}
