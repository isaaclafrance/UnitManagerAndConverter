package com.isaacapps.unitconverterapp.processors.serializers.dimension.componentnunit;

import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.numbers.RoundingFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.text.GeneralTextFormatter;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.BaseDimensionSerializer;

import java.util.Locale;

public class ComponentUnitsDimensionSerializer extends BaseDimensionSerializer<String> {

    public ComponentUnitsDimensionSerializer(Locale locale, IFormatter dimensionItemFormatter, IFormatter dimensionValueFormatter) {
        super(locale);
        dimensionSerializerBuilder = dimensionSerializerBuilder
                .setDimensionItemSerializer(new ComponentUnitsDimensionItemSerializer(locale, dimensionItemFormatter))
                .setDimensionValueFormatter(dimensionValueFormatter)
                .setDimensionKeyFormatter(dimensionItemFormatter);
        setLocale(locale);
    }
    public ComponentUnitsDimensionSerializer(Locale locale) {
        this(locale, new GeneralTextFormatter(locale), new RoundingFormatter(locale));
    }

    ///
    @Override
    public void setLocale(Locale locale) {
        super.setLocale(locale);
    }
}
