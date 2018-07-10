package com.isaacapps.unitconverterapp.processors.serializers.dimension.componentnunit;

import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;
import com.isaacapps.unitconverterapp.processors.serializers.ISerializer;

import java.util.Locale;

public class ComponentUnitsDimensionItemSerializer implements ISerializer<String> {
    private Locale locale;
    private IFormatter generalTextFormatter;

    public ComponentUnitsDimensionItemSerializer(Locale locale, IFormatter generalTextFormatter){
        this.generalTextFormatter = generalTextFormatter;
        setLocale(locale);
    }

    @Override
    public String serialize(String dimensionItem) {
        return generalTextFormatter.format(dimensionItem);
    }

    ///
    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
        generalTextFormatter.setLocale(locale);
    }
}
