package com.isaacapps.unitconverterapp.processors.formatters.dimensions;

import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;
import com.isaacapps.unitconverterapp.processors.parsers.IParser;
import com.isaacapps.unitconverterapp.processors.serializers.ISerializer;

import java.util.Locale;
import java.util.Map;

/**
 * Parses out the tokenized parts of a provided dimension text and then recombines them into a cleaner and more standard form as specified by a serializer.
 * @param <T> The type of dimension item to be parsed and serialized..
 */
public class DimensionFormatter<T> implements IFormatter {
    private Locale locale;

    private IParser<Map<T, Double>> dimensionParser;
    private ISerializer<Map<T, Double>> dimensionSerializer;

    public DimensionFormatter(Locale locale, IParser<Map<T, Double>> dimensionParser, ISerializer<Map<T, Double>> dimensionSerializer){
        this.locale = locale;
        this.dimensionParser = dimensionParser;
        this.dimensionSerializer = dimensionSerializer;
    }

    /**
     * Cleans ups a provided dimension text into a more standard form.
     * Formatting can involve including parentheses and proper spacing where appropriate.
     */
    @Override
    public String format(String dimensionText) {
        try {
            return dimensionSerializer.serialize(dimensionParser.parse(dimensionText));
        } catch (Exception e) {
            return dimensionText;
        }
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
