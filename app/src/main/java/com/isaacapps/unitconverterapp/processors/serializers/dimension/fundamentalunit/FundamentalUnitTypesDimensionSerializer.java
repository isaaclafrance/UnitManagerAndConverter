package com.isaacapps.unitconverterapp.processors.serializers.dimension.fundamentalunit;

import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.BaseDimensionSerializer;


import java.util.Locale;

public class FundamentalUnitTypesDimensionSerializer extends BaseDimensionSerializer<FundamentalUnitsDataModel.UNIT_TYPE> {
    private Locale locale;
    private FundamentalUnitTypesDimensionItemSerializer fundamentalUnitTypesDimensionItemSerializer;

    public FundamentalUnitTypesDimensionSerializer(Locale locale, FundamentalUnitTypesDimensionItemSerializer fundamentalUnitTypesDimensionItemSerializer) {
        super(locale);
        this.fundamentalUnitTypesDimensionItemSerializer = fundamentalUnitTypesDimensionItemSerializer;
        dimensionSerializerBuilder = dimensionSerializerBuilder
                .setDimensionItemSerializer(fundamentalUnitTypesDimensionItemSerializer);
        setLocale(locale);
    }

    ///
    @Override
    public void setLocale(Locale locale) {
        super.setLocale(locale);
        fundamentalUnitTypesDimensionItemSerializer.setLocale(locale);
    }
}
