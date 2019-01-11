package com.isaacapps.unitconverterapp.processors.serializers.dimension.fundamentalunit;

import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel;
import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.numbers.RoundingFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.text.GeneralTextFormatter;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.BaseDimensionSerializer;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.componentnunit.ComponentUnitsDimensionItemSerializer;


import java.util.Locale;

public class FundamentalUnitTypesDimensionSerializer extends BaseDimensionSerializer<FundamentalUnitsDataModel.UNIT_TYPE> {

    public FundamentalUnitTypesDimensionSerializer(Locale locale, IFormatter dimensionItemFormatter, IFormatter dimensionValueFormatter) {
        super(locale);
        dimensionSerializerBuilder = dimensionSerializerBuilder
                .setDimensionItemSerializer(new FundamentalUnitTypesDimensionItemSerializer(locale, dimensionItemFormatter))
                .setDimensionValueFormatter(dimensionValueFormatter)
                .setDimensionKeyFormatter(dimensionItemFormatter);
        setLocale(locale);
    }
    public FundamentalUnitTypesDimensionSerializer(Locale locale) {
        this(locale, new GeneralTextFormatter(locale), new RoundingFormatter(locale));
    }

    ///
    @Override
    public void setLocale(Locale locale) {
        super.setLocale(locale);
    }
}
