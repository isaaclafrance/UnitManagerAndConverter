package com.isaacapps.unitconverterapp.processors.parsers.dimension.fundamentalunits;

import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel.UNIT_TYPE;
import com.isaacapps.unitconverterapp.processors.formatters.ChainedFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.numbers.MixedFractionToDecimalFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.numbers.RoundingFormatter;
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

    public FundamentalUnitTypesDimensionParser(Locale locale, IFormatter atomicTypeFormatter, IFormatter exponentialValueFormatter) throws ParsingException {
        super(locale, new DimensionComponentDefiner(FUNDAMENTAL_UNIT_NAME_REGEX));

        IFormatter augmentedAtomicTypeFormatter = new ChainedFormatter(locale)
                .AddFormatter(new AllUpperCaseFormatter(locale))
                .AddFormatter(atomicTypeFormatter);

        IFormatter augmentedExponentialValueFormatter = new ChainedFormatter(locale)
                .AddFormatter(new MixedFractionToDecimalFormatter(locale))
                .AddFormatter(exponentialValueFormatter);

        dimensionParserBuilder.setParsedDimensionUpdater(new FundamentalUnitTypesParsedDimensionUpdater())
                .setTemplateDimensionMap(new EnumMap<>(UNIT_TYPE.class))
                .setAtomicTypeFormatter(augmentedAtomicTypeFormatter)
                .setExponentValueFormatter(augmentedExponentialValueFormatter);
    }
    public FundamentalUnitTypesDimensionParser(Locale locale) throws ParsingException {
        this(locale, new GeneralTextFormatter(locale), new RoundingFormatter(locale, 5));
    }

    @Override
    public Map<UNIT_TYPE, Double> parse(String dimensionString) throws ParsingException {
        return super.parse(dimensionString);
    }
}
