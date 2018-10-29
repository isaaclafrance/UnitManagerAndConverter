package com.isaacapps.unitconverterapp.processors.parsers.dimension;

import com.isaacapps.unitconverterapp.processors.formatters.ChainedFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.numbers.MixedFractionToDecimalFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.numbers.RoundingFormatter;
import com.isaacapps.unitconverterapp.processors.parsers.ParsingException;

import java.util.Locale;
import java.util.Map;

import static com.isaacapps.unitconverterapp.utilities.RegExUtility.SIGNED_DOUBLE_VALUE_REGEX_PATTERN;

public class BaseDimensionParser<T> implements com.isaacapps.unitconverterapp.processors.parsers.IParser {
    protected DimensionParserBuilder<T> dimensionParserBuilder;
    protected DimensionComponentDefiner dimensionComponentDefiner;

    ///
    public BaseDimensionParser(Locale locale, DimensionComponentDefiner dimensionComponentDefiner) throws ParsingException {
        this(locale, new DimensionParserBuilder<T>(), dimensionComponentDefiner);
    }
    public BaseDimensionParser(Locale locale, DimensionParserBuilder<T> dimensionParserBuilder, DimensionComponentDefiner dimensionComponentDefiner) throws ParsingException {
        this.dimensionComponentDefiner = dimensionComponentDefiner;
        this.dimensionParserBuilder = dimensionParserBuilder;

        dimensionComponentDefiner.setExponentValueRegEx(String.format("(?:%s)|(?:%s)", SIGNED_DOUBLE_VALUE_REGEX_PATTERN.pattern(), MixedFractionToDecimalFormatter.MIXED_PATTERN));
        dimensionParserBuilder.setDimensionComponentDefiner(dimensionComponentDefiner);
        dimensionParserBuilder.setExponentValueFormatter(new ChainedFormatter(locale).AddFormatter(new MixedFractionToDecimalFormatter(locale)).AddFormatter(new RoundingFormatter(locale)));
    }

    ///
    @Override
    public Map<T, Double> parse(String dimensionString) throws ParsingException {
        return dimensionParserBuilder.parse(dimensionString);
    }

    ///
    public boolean isStrictParsing() {
        return dimensionParserBuilder.isStrictParsing();
    }
    public void setStrictParsing(boolean strictParsing) {
        dimensionParserBuilder.setStrictParsing(strictParsing);
    }

    ///
    public DimensionParserBuilder<T> getDimensionParserBuilder(){
        return dimensionParserBuilder;
    }
}
