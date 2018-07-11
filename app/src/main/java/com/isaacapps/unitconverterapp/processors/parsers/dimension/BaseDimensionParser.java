package com.isaacapps.unitconverterapp.processors.parsers.dimension;

import com.isaacapps.unitconverterapp.processors.formatters.numbers.MixedFractionToDecimalFormatter;
import com.isaacapps.unitconverterapp.processors.parsers.IParser;
import com.isaacapps.unitconverterapp.processors.parsers.ParsingException;
import com.isaacapps.unitconverterapp.utilities.RegExUtility;

import java.util.Locale;
import java.util.Map;

import static com.isaacapps.unitconverterapp.processors.parsers.dimension.DimensionComponentDefinerBuilder.DEFAULT_DIVISION_SYMBOL_GROUPS;
import static com.isaacapps.unitconverterapp.processors.parsers.dimension.DimensionComponentDefinerBuilder.DEFAULT_EXPONENT_SYMBOL_GROUPS;
import static com.isaacapps.unitconverterapp.processors.parsers.dimension.DimensionComponentDefinerBuilder.DEFAULT_MULTIPLICATION_SYMBOL_GROUPS;

public class BaseDimensionParser<T> implements IParser<Map<T, Double>> {
    protected DimensionParserBuilder<T> dimensionParserBuilder;
    protected boolean strictParsing;

    ///
    public BaseDimensionParser() {
        this(new DimensionParserBuilder<T>()
                .setDivisionSymbols(DEFAULT_DIVISION_SYMBOL_GROUPS)
                .setMultiplicationSymbols(DEFAULT_MULTIPLICATION_SYMBOL_GROUPS)
                .setExponentSymbols(DEFAULT_EXPONENT_SYMBOL_GROUPS)
                .setExponentValueRegEx(String.format("(?:%s)|(?:%s)", RegExUtility.SIGNED_DOUBLE_VALUE_REGEX, MixedFractionToDecimalFormatter.MIXED_NUMBER_PATTERN ))
                .setExponentValueFormatter(new MixedFractionToDecimalFormatter(Locale.ENGLISH)));
    }
    public BaseDimensionParser(DimensionParserBuilder dimensionParserBuilder) {
        this.dimensionParserBuilder = dimensionParserBuilder;
    }

    ///
    @Override
    public Map<T, Double> parse(String dimensionString)
            throws ParsingException {
        return dimensionParserBuilder.setStrictParsing(strictParsing).parse(dimensionString);
    }

    ///
    public boolean isStrictParsing() {
        return strictParsing;
    }

    public void setStrictParsing(boolean strictParsing) {
        this.strictParsing = strictParsing;
    }

    ///
    public DimensionParserBuilder<T> getDimensionParserBuilder() {
        return dimensionParserBuilder;
    }
}
