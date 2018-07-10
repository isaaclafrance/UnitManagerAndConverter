package com.isaacapps.unitconverterapp.processors.parsers.dimension.fundamentalunits;

import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel.UNIT_TYPE;
import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.BaseDimensionParser;

import java.util.Locale;

public class FundamentalUnitTypesDimensionParser extends BaseDimensionParser<UNIT_TYPE> {
    public static final String FUNDAMENTAL_UNIT_NAME_REGEX = "\\w+";

    public FundamentalUnitTypesDimensionParser() {
        dimensionParserBuilder = dimensionParserBuilder
                .setAtomicTypeRegEx(FUNDAMENTAL_UNIT_NAME_REGEX)
                .setParsedDimensionUpdater(new FundamentalUnitTypesParsedDimensionUpdater())
                .setAtomicTypeFormatter(new IFormatter() {
                    private Locale locale;
                    @Override
                    public String format(String text) {
                        return text.toUpperCase();
                    }

                    @Override
                    public Locale getLocale() {
                        return locale;
                    }

                    @Override
                    public void setLocale(Locale locale) {
                        this.locale = locale;
                    }
                });
    }
}
