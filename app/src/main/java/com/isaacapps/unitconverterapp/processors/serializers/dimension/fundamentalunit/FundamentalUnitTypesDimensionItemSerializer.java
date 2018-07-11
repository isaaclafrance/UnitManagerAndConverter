package com.isaacapps.unitconverterapp.processors.serializers.dimension.fundamentalunit;

import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel.UNIT_TYPE;
import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;
import com.isaacapps.unitconverterapp.processors.serializers.ISerializer;

import java.util.Locale;

public class FundamentalUnitTypesDimensionItemSerializer implements ISerializer<UNIT_TYPE> {
    private Locale locale;
    private IFormatter generalTextFormatter;

    public FundamentalUnitTypesDimensionItemSerializer(Locale locale, IFormatter generalTextFormatter){
        this.generalTextFormatter = generalTextFormatter;
        setLocale(locale);
    }

    @Override
    public String serialize(UNIT_TYPE dimensionItem) {
        return generalTextFormatter.format(dimensionItem.name());
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
