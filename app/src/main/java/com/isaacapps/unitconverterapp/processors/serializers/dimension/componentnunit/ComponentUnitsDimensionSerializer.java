package com.isaacapps.unitconverterapp.processors.serializers.dimension.componentnunit;

import com.isaacapps.unitconverterapp.processors.serializers.dimension.BaseDimensionSerializer;

import java.util.Locale;

public class ComponentUnitsDimensionSerializer extends BaseDimensionSerializer<String> {
    private ComponentUnitsDimensionItemSerializer componentUnitsDimensionItemSerializer;

    public ComponentUnitsDimensionSerializer(Locale locale, ComponentUnitsDimensionItemSerializer componentUnitsDimensionItemSerializer) {
        super(locale);
        this.componentUnitsDimensionItemSerializer = componentUnitsDimensionItemSerializer;
        dimensionSerializerBuilder = dimensionSerializerBuilder
                .setDimensionItemSerializer(componentUnitsDimensionItemSerializer);
        setLocale(locale);
    }

    ///
    @Override
    public void setLocale(Locale locale) {
        super.setLocale(locale);
        componentUnitsDimensionItemSerializer.setLocale(locale);
    }
}
