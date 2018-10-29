package com.isaacapps.unitconverterapp.processors.parsers.dimension.componentunits;

import com.isaacapps.unitconverterapp.processors.formatters.text.GeneralTextFormatter;
import com.isaacapps.unitconverterapp.processors.parsers.ParsingException;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.BaseDimensionParser;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.DimensionComponentDefiner;

import java.util.Locale;

public class ComponentUnitsDimensionParser extends BaseDimensionParser<String> {
    public static final String COMPONENT_NAME_REGEX = "(?:([a-zA-Z]+)?[_]?\\w+)"; //ex. h20, meter, 10, newton_meter

    public ComponentUnitsDimensionParser(Locale locale) throws ParsingException {
        super(locale, new DimensionComponentDefiner(COMPONENT_NAME_REGEX));

        dimensionParserBuilder.setParsedDimensionUpdater(new ComponentUnitsParsedDimensionUpdater())
                .setAtomicTypeFormatter(new GeneralTextFormatter(locale));
    }
    public ComponentUnitsDimensionParser() throws ParsingException {
        this(Locale.getDefault());
    }

}
