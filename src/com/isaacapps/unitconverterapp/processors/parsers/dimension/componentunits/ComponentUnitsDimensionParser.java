package com.isaacapps.unitconverterapp.processors.parsers.dimension.componentunits;

import com.isaacapps.unitconverterapp.processors.formatters.text.GeneralTextFormatter;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.BaseDimensionParser;

import java.util.Locale;

public class ComponentUnitsDimensionParser extends BaseDimensionParser<String> {
    public static final String COMPONENT_NAME_REGEX = "(?:([a-zA-Z]+)?[_]?\\w+)"; //ex. h20, meter, 10, newton_meter

    public ComponentUnitsDimensionParser() {
        dimensionParserBuilder = dimensionParserBuilder
                .setAtomicTypeRegEx(COMPONENT_NAME_REGEX)
                .setParsedDimensionUpdater(new ComponentUnitsParsedDimensionUpdater())
                .setAtomicTypeFormatter(new GeneralTextFormatter(Locale.ENGLISH));
    }

}
