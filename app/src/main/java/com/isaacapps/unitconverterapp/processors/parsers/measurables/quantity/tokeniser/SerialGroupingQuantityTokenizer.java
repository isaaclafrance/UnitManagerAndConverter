package com.isaacapps.unitconverterapp.processors.parsers.measurables.quantity.tokeniser;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.grouping.UnitNamesGroupingFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.grouping.ValuesGroupingFormatter;
import com.isaacapps.unitconverterapp.processors.parsers.ParsingException;
import com.isaacapps.unitconverterapp.processors.parsers.measurables.quantity.QuantityGroupingDefiner;
import com.isaacapps.unitconverterapp.processors.parsers.measurables.unit.UnitParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SerialGroupingQuantityTokenizer {
    private Locale locale;
    private IFormatter valuesGroupingFormatter;
    private IFormatter unitsNameGroupingFormatter;
    private UnitParser unitParser;
    private QuantityGroupingDefiner quantityGroupingDefiner;

    public SerialGroupingQuantityTokenizer(Locale locale, UnitParser unitParser, QuantityGroupingDefiner quantityGroupingDefiner, ValuesGroupingFormatter valuesGroupingFormatter, UnitNamesGroupingFormatter unitNamesGroupingFormatter) {
        this.unitParser = unitParser;
        this.quantityGroupingDefiner = quantityGroupingDefiner;
        this.valuesGroupingFormatter = valuesGroupingFormatter;
        this.unitsNameGroupingFormatter = unitNamesGroupingFormatter;

        setLocale(locale);
    }

    ///

    /**
     * Extracts an serial association of values grouping, ie {1} {2} {3}.
     * If there are paired value and unit groupings, then empty string is returned
     */
    private String extractSerialValuesGroupingString(String groupings) {
        return valuesGroupingFormatter.format(extractExclusivelySerialGrouping(quantityGroupingDefiner.getSerialValuesGroupingsPattern(), groupings));
    }

    /**
     * First attempt to clean up the provided grouping using an appropriate grouping formatter
     * , then attempts to extract a list of values from a grouping of serial value association, ie {1} {2}.
     * If there are pairing grouping ie. "{1 meter} {2 inch}", then empty list is returned
     */
    public List<Double> parseSerialGroupingToValuesList(String groupings) {
        List<Double> valueGroupingList = new ArrayList<>();

        Matcher valueGroupingMatcher = quantityGroupingDefiner.getSingleValueGroupingPattern()
                .matcher(extractSerialValuesGroupingString(groupings));

        while (valueGroupingMatcher.find())
            valueGroupingList.add(Double.parseDouble(quantityGroupingDefiner.removeGroupingSymbol(valueGroupingMatcher.group())));

        return valueGroupingList;
    }

    /**
     * First attempt to clean up the provided grouping using an appropriate grouping formatter
     * , then attempts to extract an serial association of units grouping ie "{meter} {inch}".
     * If there are pairing groupings ie. "{1 meter} {2 inch}", then empty string is returned
     */
    private String extractSerialUnitGroupingsString(String groupings) {
        return unitsNameGroupingFormatter.format(
                extractExclusivelySerialGrouping(quantityGroupingDefiner
                        .getSerialUnitsGroupingsPattern(), groupings));
    }

    /**
     * First attempt to clean up the provided grouping using an appropriate grouping formatter
     * , then attempts to extracts a list of unit names from grouping of serial unit association "{meter} {inch}".
     * If there are pairing grouping ie. "{1 meter} {2 inch}", then empty list is returned
     */
    public List<String> parseSerialGroupingToUnitsNamesList(String serialGrouping) {
        List<String> unitGroupingList = new ArrayList<>();

        Matcher unitGroupingMatcher = quantityGroupingDefiner.getSingleUnitGroupingPattern()
                .matcher(extractSerialUnitGroupingsString(serialGrouping));

        while (unitGroupingMatcher.find())
            unitGroupingList.add(unitGroupingMatcher.group());


        return unitGroupingList;
    }

    public List<Unit> parseUnitNamesToUnitsList(List<String> unitsNames) {
        List<Unit> unitsList = new ArrayList<>();

        for (String unitName : unitsNames) {
            try {
                unitsList.add(unitParser.parse(quantityGroupingDefiner.removeGroupingSymbol(unitName)));
            } catch (ParsingException e) {
                return Collections.emptyList();
            }
        }

        return unitsList;
    }

    public List<Unit> parseSerialGroupingToUnitsList(String serialGrouping){
        return parseUnitNamesToUnitsList(parseSerialGroupingToUnitsNamesList(serialGrouping));
    }

    /**
     * Extracts an adjacent association of groupings. If there are pairing grouping, then empty string is returned
     */
    private String extractExclusivelySerialGrouping(Pattern groupingPattern, String groupings) {
        if (quantityGroupingDefiner.getPairedValueUnitNameGroupingPattern().matcher(groupings).find())
            return "";

        Matcher groupingMatcher = groupingPattern.matcher(groupings);
        if (groupingMatcher.find())
            return groupingMatcher.group();
        else
            return "";
    }

    ///
    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
        unitsNameGroupingFormatter.setLocale(locale);
        valuesGroupingFormatter.setLocale(locale);
    }
}
