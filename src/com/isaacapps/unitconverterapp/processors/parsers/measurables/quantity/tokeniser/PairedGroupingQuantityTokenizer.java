package com.isaacapps.unitconverterapp.processors.parsers.measurables.quantity.tokeniser;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.processors.parsers.ParsingException;
import com.isaacapps.unitconverterapp.processors.parsers.measurables.quantity.QuantityGroupingDefiner;
import com.isaacapps.unitconverterapp.processors.parsers.measurables.unit.UnitParser;
import com.isaacapps.unitconverterapp.utilities.RegExUtility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PairedGroupingQuantityTokenizer {
    private UnitParser unitParser;
    private QuantityGroupingDefiner quantityGroupingDefiner;

    public PairedGroupingQuantityTokenizer(UnitParser unitParser, QuantityGroupingDefiner quantityGroupingDefiner) {
        this.unitParser = unitParser;
        this.quantityGroupingDefiner = quantityGroupingDefiner;
    }

    ///

    /**
     * Extracts a grouping list where each group is a pairing of number followed by unit name
     * or just a unit with one assumed as the preceding value.
     * If there are serial groupings ie "{1}{2}{meter}{inch}", then an empty list is returned.
     */
    public Collection<String> extractPairedValueUnitGroupingList(String pairedValueUnitGroupings) {
        if (quantityGroupingDefiner.getSingleValueGroupingPattern().matcher(pairedValueUnitGroupings).find())
            return Collections.EMPTY_LIST;

        List<String> pairedValueGroupingsList = new ArrayList<>();
        Matcher pairedValueUnitGroupingMatcher = quantityGroupingDefiner.getSinglePairedValueUnitNameGroupingPattern()
                .matcher(pairedValueUnitGroupings);
        while(pairedValueUnitGroupingMatcher.find())
            pairedValueGroupingsList.add(pairedValueUnitGroupingMatcher.group());

        return pairedValueGroupingsList;
    }

    /**
     * Converts a paired value unit name grouping list to map of unit name and value.
     * If there are serial groupings ie "{1}{2}{meter}{inch}", then an empty map is returned.
     */
    public Map<String, Double> parsePairedValueUnitNameGroupingsToNameValueMap(String pairedValueUnitNameGroupings) {
        Pattern unitNamePattern = Pattern.compile(UnitParser.UNIT_NAME_REGEX);
        Pattern valuePattern = Pattern.compile(RegExUtility.SIGNED_DOUBLE_VALUE_REGEX);

        Map<String, Double> nameValueMap = new HashMap<>();

        for(String pairedValueUnitNameGrouping:extractPairedValueUnitGroupingList(pairedValueUnitNameGroupings)){
            nameValueMap.put(unitNamePattern.matcher(pairedValueUnitNameGrouping).group()
                    , Double.parseDouble(valuePattern.matcher(pairedValueUnitNameGrouping).group()));
        }

        return nameValueMap;
    }

    /**
     * Converts grouping map to map that is easily consumed by a Quantity object.
     */
    public Map<Unit, Double> convertToQuantityUnitValueMap(Map<String, Double> nameValueMap) {
        Map<Unit, Double> unitValueMap = new HashMap<>();

        for(Map.Entry<String, Double> nameValueEntry:nameValueMap.entrySet()) {
            try {
                unitValueMap.put(unitParser.parse(nameValueEntry.getKey()), nameValueEntry.getValue());
            } catch (ParsingException e) {
                return Collections.EMPTY_MAP;
            }
        }

        return unitValueMap;
    }
}
