package com.isaacapps.unitconverterapp.processors.serializers.measurables.quantity;

import com.isaacapps.unitconverterapp.models.measurables.quantity.Quantity;
import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;
import com.isaacapps.unitconverterapp.processors.serializers.ISerializer;

import java.util.Locale;
import java.util.Map;

public class PairedGroupingQuantitySerializer implements ISerializer<Quantity> {
    private Locale locale;
    private IFormatter unitNameFormatter;
    private IFormatter valueFormatter;

    public PairedGroupingQuantitySerializer(Locale locale, IFormatter unitNameFormatter, IFormatter valueFormatter){
        this.unitNameFormatter = unitNameFormatter;
        this.valueFormatter = valueFormatter;
        setLocale(locale);
    }

    ///
    @Override
    public String serialize(Quantity quantity){
        StringBuilder representationBuilder = new StringBuilder();
        for (Map.Entry<Unit, Double> unitValueEntry : quantity.getUnitValueMap().entrySet()) {
            representationBuilder.append(String.format("{%s %s} ", valueFormatter.format(unitValueEntry.getValue().toString())
                    , unitNameFormatter.format(unitValueEntry.getKey().getName())));
        }
        return representationBuilder.toString();
    }

    ///
    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
        unitNameFormatter.setLocale(locale);
        valueFormatter.setLocale(locale);
    }
}
