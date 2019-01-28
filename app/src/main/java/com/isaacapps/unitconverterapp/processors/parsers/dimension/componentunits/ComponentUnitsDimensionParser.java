package com.isaacapps.unitconverterapp.processors.parsers.dimension.componentunits;

import com.isaacapps.unitconverterapp.processors.formatters.ChainedFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.numbers.MixedFractionToDecimalFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.numbers.RoundingFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.text.GeneralTextFormatter;
import com.isaacapps.unitconverterapp.processors.parsers.ParsingException;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.BaseDimensionParser;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.DimensionComponentDefiner;
import static com.isaacapps.unitconverterapp.utilities.RegExUtility.SIGNED_DOUBLE_VALUE_REGEX_PATTERN;

import java.util.Locale;

public class ComponentUnitsDimensionParser extends BaseDimensionParser<String> {
    public static final String COMPONENT_NAME_REGEX =  String.format("(?:%s|%s)", SIGNED_DOUBLE_VALUE_REGEX_PATTERN,"(?:([a-zA-Z]+)?[_]?\\w+)"); //ex. h20, meter, 10, newton_meter, 2.0

    public ComponentUnitsDimensionParser(Locale locale, IFormatter atomicTypeFormatter, IFormatter exponentialValueFormatter) throws ParsingException {
        super(locale, new DimensionComponentDefiner(COMPONENT_NAME_REGEX));

        IFormatter augmentedExponentialValueFormatter = new ChainedFormatter(locale)
                .AddFormatter(new MixedFractionToDecimalFormatter(locale))
                .AddFormatter(exponentialValueFormatter);

        dimensionParserBuilder.setParsedDimensionUpdater(new ComponentUnitsParsedDimensionUpdater())
                .setAtomicTypeFormatter(atomicTypeFormatter)
                .setExponentValueFormatter(augmentedExponentialValueFormatter);
    }
    public ComponentUnitsDimensionParser(Locale locale) throws ParsingException {
        this(locale, new GeneralTextFormatter(locale), new RoundingFormatter(locale, 5));
    }

}
