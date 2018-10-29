package com.isaacapps.unitconverterapp.processors.parsers.measurables.quantity;

import com.isaacapps.unitconverterapp.processors.parsers.measurables.unit.UnitParser;
import com.isaacapps.unitconverterapp.utilities.RegExUtility;

import java.util.regex.Pattern;

import static com.isaacapps.unitconverterapp.utilities.RegExUtility.SIGNED_DOUBLE_VALUE_REGEX_PATTERN;

public class QuantityGroupingDefiner {
    public static final String DEFAULT_OPENING_SYMBOL = "{";
    public static final String DEFAULT_CLOSING_SYMBOL = "}";

    private String groupOpeningSymbol;
    private String groupClosingSymbol;
    private String regexEscapedGroupOpeningSymbol;
    private String regexEscapedGroupClosingingSymbol;

    private Pattern anyGroupingPattern;
    private Pattern singleUnitGroupingPattern, serialUnitsGroupingsPattern;
    private Pattern singleValueGroupingPattern, serialValuesGroupingsPattern;
    private Pattern singlePairedValueUnitNameGroupingPattern, pairedValueUnitNameGroupingPattern;
    private Pattern emptyGroupingPattern;

    public QuantityGroupingDefiner(){
        this(DEFAULT_OPENING_SYMBOL, DEFAULT_CLOSING_SYMBOL);
    }

    public QuantityGroupingDefiner(String groupOpeningSymbol, String groupClosingSymbol){
        this.groupOpeningSymbol = groupOpeningSymbol;
        this.groupClosingSymbol = groupClosingSymbol;
        compileGroupingIdentificationPatterns();
    }

    ///
    private void compileGroupingIdentificationPatterns(){
        regexEscapedGroupOpeningSymbol = RegExUtility.escapeRegexReservedCharacters(groupOpeningSymbol);
        regexEscapedGroupClosingingSymbol = RegExUtility.escapeRegexReservedCharacters(groupClosingSymbol);
        anyGroupingPattern = Pattern.compile(String.format("%s.+%s", regexEscapedGroupOpeningSymbol, regexEscapedGroupClosingingSymbol));
        emptyGroupingPattern = Pattern.compile(String.format("%s\\s*%s", regexEscapedGroupOpeningSymbol, regexEscapedGroupClosingingSymbol));
        compileSerialGroupingPatterns();
        compilePairedGroupingPatterns();
    }
    private void compileSerialGroupingPatterns(){
        singleUnitGroupingPattern = Pattern.compile(String.format("%s[\\s]*%s[\\s]*%s", regexEscapedGroupOpeningSymbol, UnitParser.UNIT_NAME_REGEX, regexEscapedGroupClosingingSymbol));
        serialUnitsGroupingsPattern = Pattern.compile(String.format("([\\s]*%s[\\s]*)+", singleUnitGroupingPattern.pattern()));
        singleValueGroupingPattern = Pattern.compile(String.format("%s[\\s]*%s[\\s]*%s", regexEscapedGroupOpeningSymbol, SIGNED_DOUBLE_VALUE_REGEX_PATTERN.pattern(), regexEscapedGroupClosingingSymbol));
        serialValuesGroupingsPattern = Pattern.compile(String.format("([\\s]*%s[\\s]*)+", singleValueGroupingPattern.pattern()));
    }
    private void compilePairedGroupingPatterns(){
        singlePairedValueUnitNameGroupingPattern = Pattern.compile(String.format("%s[\\s]*%s[\\s]+%s[\\s]*%s", regexEscapedGroupOpeningSymbol, SIGNED_DOUBLE_VALUE_REGEX_PATTERN.pattern(), UnitParser.UNIT_NAME_REGEX, regexEscapedGroupClosingingSymbol));
        pairedValueUnitNameGroupingPattern = Pattern.compile(String.format("([\\s]*%s[\\s]*)+", singlePairedValueUnitNameGroupingPattern.pattern()));
    }

    ///
    public String removeGroupingSymbol(String grouping){
        return grouping.replaceAll(regexEscapedGroupOpeningSymbol, "")
                .replaceAll(regexEscapedGroupClosingingSymbol, "");
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

    public String getRegexEscapedGroupClosingingSymbol() {
        return regexEscapedGroupClosingingSymbol;
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
