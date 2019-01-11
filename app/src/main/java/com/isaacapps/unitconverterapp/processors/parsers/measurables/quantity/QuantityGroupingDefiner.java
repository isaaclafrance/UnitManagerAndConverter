package com.isaacapps.unitconverterapp.processors.parsers.measurables.quantity;

import com.florianingerl.util.regex.Pattern;
import com.isaacapps.unitconverterapp.processors.parsers.ParsingException;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.DimensionComponentDefiner;
import com.isaacapps.unitconverterapp.processors.parsers.measurables.unit.UnitParser;
import com.isaacapps.unitconverterapp.utilities.RegExUtility;

import static com.isaacapps.unitconverterapp.utilities.RegExUtility.SIGNED_DOUBLE_VALUE_REGEX_PATTERN;

public class QuantityGroupingDefiner {
    public static final String DEFAULT_GROUP_OPENING_SYMBOL = "{";
    public static final String DEFAULT_GROUP_CLOSING_SYMBOL = "}";

    private String groupOpeningSymbol;
    private String groupClosingSymbol;
    private String regexEscapedGroupOpeningSymbol;
    private String regexEscapedGroupClosingSymbol;

    private Pattern anyGroupingPattern;
    private Pattern singleUnitGroupingPattern, serialUnitsGroupingsPattern;
    private Pattern singleValueGroupingPattern, serialValuesGroupingsPattern;
    private Pattern singlePairedValueUnitNameGroupingPattern, pairedValueUnitNameGroupingPattern;
    private Pattern emptyGroupingPattern;

    private DimensionComponentDefiner dimensionComponentDefiner;

    public QuantityGroupingDefiner() throws ParsingException {
        this(new DimensionComponentDefiner(UnitParser.UNIT_NAME_REGEX), DEFAULT_GROUP_OPENING_SYMBOL, DEFAULT_GROUP_CLOSING_SYMBOL);
    }

    public QuantityGroupingDefiner(DimensionComponentDefiner dimensionComponentDefiner, String groupOpeningSymbol, String groupClosingSymbol){
        this.dimensionComponentDefiner = dimensionComponentDefiner;

        this.groupOpeningSymbol = groupOpeningSymbol;
        this.groupClosingSymbol = groupClosingSymbol;
        compileGroupingIdentificationPatterns();
    }

    ///
    private void compileGroupingIdentificationPatterns(){
        regexEscapedGroupOpeningSymbol = RegExUtility.escapeRegexReservedCharacters(groupOpeningSymbol);
        regexEscapedGroupClosingSymbol = RegExUtility.escapeRegexReservedCharacters(groupClosingSymbol);
        anyGroupingPattern = Pattern.compile(String.format("%s.+%s", regexEscapedGroupOpeningSymbol, regexEscapedGroupClosingSymbol));
        emptyGroupingPattern = Pattern.compile(String.format("%s\\s*%s", regexEscapedGroupOpeningSymbol, regexEscapedGroupClosingSymbol));
        compileSerialGroupingPatterns();
        compilePairedGroupingPatterns();
    }
    private void compileSerialGroupingPatterns(){
        singleUnitGroupingPattern = Pattern.compile(String.format("%s(?:[\\s]*%s|%s+[\\s]*)%s", regexEscapedGroupOpeningSymbol, dimensionComponentDefiner.getMultiGroupRegExPattern(), dimensionComponentDefiner.getSingleGroupRegExPattern(), regexEscapedGroupClosingSymbol));
        serialUnitsGroupingsPattern = Pattern.compile(String.format("(?:[\\s]*%s[\\s]*)+", singleUnitGroupingPattern.pattern()));
        singleValueGroupingPattern = Pattern.compile(String.format("%s[\\s]*%s[\\s]*%s", regexEscapedGroupOpeningSymbol, SIGNED_DOUBLE_VALUE_REGEX_PATTERN.pattern(), regexEscapedGroupClosingSymbol));
        serialValuesGroupingsPattern = Pattern.compile(String.format("(?:[\\s]*%s[\\s]*)+", singleValueGroupingPattern.pattern()));
    }
    private void compilePairedGroupingPatterns(){
        singlePairedValueUnitNameGroupingPattern = Pattern.compile(String.format("%s[\\s]*%s[\\s]+(?:%s|%s)[\\s]*%s", regexEscapedGroupOpeningSymbol, SIGNED_DOUBLE_VALUE_REGEX_PATTERN.pattern(), dimensionComponentDefiner.getMultiGroupRegExPattern(), dimensionComponentDefiner.getSingleGroupRegExPattern(), regexEscapedGroupClosingSymbol));
        pairedValueUnitNameGroupingPattern = Pattern.compile(String.format("([\\s]*%s[\\s]*)+", singlePairedValueUnitNameGroupingPattern.pattern()));
    }

    ///
    public String removeGroupingSymbol(String grouping){
        return grouping.replaceAll(regexEscapedGroupOpeningSymbol, "")
                .replaceAll(regexEscapedGroupClosingSymbol, "");
    }

    ///
    public boolean hasValueUnitPairGrouping(String potentialGroupings) {
        return pairedValueUnitNameGroupingPattern.matcher(potentialGroupings).find();
    }

    public boolean hasValuesGrouping(String potentialGroupings) {
        return singleValueGroupingPattern.matcher(potentialGroupings).find();
    }

    public boolean hasUnitsGrouping(String potentialGroupings) {
        return singleUnitGroupingPattern.matcher(potentialGroupings).find();
    }

    public boolean hasAnyGrouping(String potentialGrouping){
        return anyGroupingPattern.matcher(potentialGrouping).find();
    }

    ///
    public String getGroupOpeningSymbol() {
        return groupOpeningSymbol;
    }
    public void setGroupOpeningSymbol(String groupOpeningSymbol){
        this.groupOpeningSymbol = groupOpeningSymbol;
        compileGroupingIdentificationPatterns();
    }
    public String getRegexEscapedGroupOpeningSymbol() {
        return regexEscapedGroupOpeningSymbol;
    }

    ///
    public String getGroupClosingSymbol() {
        return groupClosingSymbol;
    }
    public void setGroupClosingSymbol(String groupClosingSymbol){
        this.groupClosingSymbol = groupClosingSymbol;
        compileGroupingIdentificationPatterns();
    }
    public String getRegexEscapedGroupClosingSymbol() {
        return regexEscapedGroupClosingSymbol;
    }

    ///
    public Pattern getAnyGroupingPattern() {
        return anyGroupingPattern;
    }
    public Pattern getEmptyGroupingPattern(){
        return emptyGroupingPattern;
    }

    ///
    public Pattern getSingleUnitGroupingPattern() {
        return singleUnitGroupingPattern;
    }
    public Pattern getSerialUnitsGroupingsPattern() {
        return serialUnitsGroupingsPattern;
    }
    public Pattern getSingleValueGroupingPattern() {
        return singleValueGroupingPattern;
    }
    public Pattern getSerialValuesGroupingsPattern() {
        return serialValuesGroupingsPattern;
    }

    ///
    public Pattern getSinglePairedValueUnitNameGroupingPattern() {
        return singlePairedValueUnitNameGroupingPattern;
    }
    public Pattern getPairedValueUnitNameGroupingPattern() {
        return pairedValueUnitNameGroupingPattern;
    }

}
