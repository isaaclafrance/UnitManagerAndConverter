package com.isaacapps.unitconverterapp.processors.parsers.dimension.fundamentalunits;

import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel.UNIT_TYPE;
import com.isaacapps.unitconverterapp.processors.formatters.ChainedFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.text.AllUpperCaseFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.text.GeneralTextFormatter;
import com.isaacapps.unitconverterapp.processors.parsers.ParsingException;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.BaseDimensionParser;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.DimensionComponentDefiner;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public class FundamentalUnitTypesDimensionParser extends BaseDimensionParser<UNIT_TYPE> {
    public static final String FUNDAMENTAL_UNIT_NAME_REGEX = "\\w+";

    public FundamentalUnitTypesDimensionParser(Locale locale) throws ParsingException {
        super(locale, new DimensionComponentDefiner(FUNDAMENTAL_UNIT_NAME_REGEX));

        dimensionParserBuilder.setParsedDimensionUpdater(new FundamentalUnitTypesParsedDimensionUpdater())
                .setTemplateDimensionMap(new EnumMap<>(UNIT_TYPE.class))
                .setAtomicTypeFormatter(new ChainedFormatter(locale).AddFormatter(new GeneralTextFormatter(locale)).AddFormatter(new AllUpperCaseFormatter(locale)));
    }
    public FundamentalUnitTypesDimensionParser() throws ParsingException {
        this(Locale.getDefault());
    }

    @Override
    public Map<UNIT_TYPE, Double> parse(String dimensionString) throws ParsingException {
        return super.parse(dimensionString);
    }
}
