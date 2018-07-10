package com.isaacapps.unitconverterapp.processors.serializers.measurables.quantity;

import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;
import com.isaacapps.unitconverterapp.processors.serializers.ISerializer;

import java.util.Collection;
import java.util.Locale;

public class ValuesGroupingCollectionSerializer implements ISerializer<Collection<Double>> {
    private Locale locale;
    private IFormatter valueFormatter;

    public ValuesGroupingCollectionSerializer(Locale locale, IFormatter valueFormatter){
        this.valueFormatter = valueFormatter;
        setLocale(locale);
    }

    @Override
    public String serialize(Collection<Double> valuesGroupingCollection){
        StringBuilder serialValuesBuilder = new StringBuilder();

        for (Double value : valuesGroupingCollection)
            serialValuesBuilder.append(valueFormatter.format(value.toString()));

        return serialValuesBuilder.toString();
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
        valueFormatter.setLocale(locale);
    }

}
