package com.isaacapps.unitconverterapp.processors.serializers.measurables.quantity;

import com.isaacapps.unitconverterapp.models.measurables.quantity.Quantity;
import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.processors.serializers.ISerializer;
import com.isaacapps.unitconverterapp.processors.serializers.SerializingException;

import java.util.Collection;
import java.util.Locale;

public class SerialGroupingQuantitySerializer implements ISerializer<Quantity> {
    private Locale locale;
    private ISerializer<Collection<Unit>> unitsGroupingCollectionSerializer;
    private ISerializer<Collection<Double>> valuesGroupingCollectionSerializer;

    public SerialGroupingQuantitySerializer(Locale locale, ISerializer<Collection<Unit>> unitsGroupingCollectionSerializer
            , ISerializer<Collection<Double>> valuesGroupingCollectionSerializer){
        this.unitsGroupingCollectionSerializer = unitsGroupingCollectionSerializer;
        this.valuesGroupingCollectionSerializer = valuesGroupingCollectionSerializer;
        setLocale(locale);
    }

    @Override
    public String serialize(Quantity quantity) throws SerializingException {
        return new StringBuilder(valuesGroupingCollectionSerializer.serialize(quantity.getValues()))
                .append(" ")
                .append(unitsGroupingCollectionSerializer.serialize(quantity.getUnits())).toString();
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
        unitsGroupingCollectionSerializer.setLocale(locale);
        valuesGroupingCollectionSerializer.setLocale(locale);
    }


}
