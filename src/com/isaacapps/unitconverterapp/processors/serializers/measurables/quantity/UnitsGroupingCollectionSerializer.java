package com.isaacapps.unitconverterapp.processors.serializers.measurables.quantity;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;
import com.isaacapps.unitconverterapp.processors.serializers.ISerializer;

import java.util.Collection;
import java.util.Locale;

public class UnitsGroupingCollectionSerializer implements ISerializer<Collection<Unit>> {
    Locale locale;
    private IFormatter unitNameFormatter;

    public UnitsGroupingCollectionSerializer(Locale locale, IFormatter unitNameFormatter){
        this.unitNameFormatter = unitNameFormatter;
        setLocale(locale);
    }

    @Override
    public String serialize(Collection<Unit> unitsGroupingCollection){
        StringBuilder serialUnitNameBuilder = new StringBuilder();

        for (Unit unit : unitsGroupingCollection)
            serialUnitNameBuilder.append(unitNameFormatter.format(unit.getName()));

        return serialUnitNameBuilder.toString();
    }

    ///
    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
        unitNameFormatter.setLocale(locale);
    }
}
