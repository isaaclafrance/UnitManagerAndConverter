package com.isaacapps.unitconverterapp.processors.parsers.dimension.componentunits;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.processors.operators.dimensions.DimensionOperators;
import com.isaacapps.unitconverterapp.processors.parsers.ParsingException;

import org.junit.Test;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static junit.framework.Assert.assertTrue;

public class ComponentUnitsDimensionParserTest {

    @Test
    public void parseToComponentUnitsDimensionFromString_Should_Provide_Correct_Component_Units_Map_From_String()
            throws ParsingException {
        String unitDefinition;
        Map<String,Double> unitComponentDimension;
        Map<String, Map<String,Double>> unitComponentDimensionByUnitDefinitionMap = new HashMap<>();

        //
        unitDefinition = "meter";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put("meter", 1.0);

        //
        unitDefinition = "meter^2";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put("meter", 2.0);

        //
        unitDefinition = "(meter)^5.123";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put("meter", 5.123);

        //
        unitDefinition = "(meter)^5.123 * meter^3";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put("meter", 8.123);

        //
        unitDefinition = "meter^-5.123 * meter^-3.0";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put("meter", -8.123);

        //
        unitDefinition = "(meter)^5.123/meter^3.0";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put("meter", 2.123);

        //
        unitDefinition = "((meter)^5 x meter^-3.0)**2";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put("meter", 4.0);

        //
        unitDefinition = "((meter)^2*second^-4.0)^2 multiplied by meter / day^2 divided by someunit raised to 5";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put("meter", 5.0);
        unitComponentDimension.put("second", -8.0);
        unitComponentDimension.put("day", -2.0);
        unitComponentDimension.put("someunit", -5.0);

        //
        unitDefinition = "( something / (meter*second/someotherthing) )";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put("something", 1.0);
        unitComponentDimension.put("meter", -1.0);
        unitComponentDimension.put("second", -1.0);
        unitComponentDimension.put("someotherthing", 1.0);

        //
        Locale locale = Locale.ENGLISH;
        ComponentUnitsDimensionParser componentUnitsDimensionParser = new ComponentUnitsDimensionParser(locale);
        componentUnitsDimensionParser.setStrictParsing(true);

        //
        for(Map.Entry<String, Map<String, Double>> unitDefinitionEntry:unitComponentDimensionByUnitDefinitionMap.entrySet()){
            Map<String, Double> parsedUnitComponentDimensionResult =
                    componentUnitsDimensionParser.parse(unitDefinitionEntry.getKey());

            assertTrue(String.format("Expected parsed unit definition dimension not correct. Input unit definition string %s. "
                            + "Expected unit definition dimension: %s. Parsed unit definition dimension: %s."
                    , unitDefinitionEntry.getKey() , unitDefinitionEntry.getValue().toString()
                    , parsedUnitComponentDimensionResult.toString())
                    , DimensionOperators.equalsDimension(parsedUnitComponentDimensionResult, unitDefinitionEntry.getValue()));
        }
    }

    @Test
    public void parseToComponentUnitsDimensionFromString_Should_Provide_Component_Units_Map_With_Unknown_Dimension_When_Strict_Parsing_False() throws ParsingException {
        String unitDefinition;
        Map<String,Double> unitComponentDimension;
        Map<String, Map<String,Double>> unitComponentDimensionByUnitDefinitionMap = new HashMap<>();

        //
        unitDefinition = "%%%";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put(Unit.UNKNOWN_UNIT_NAME, 1.0);

        //
        unitDefinition = "( $$$!% ) / second^2";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put(Unit.UNKNOWN_UNIT_NAME, 1.0);
        unitComponentDimension.put("second", -2.0);

        //
        Locale locale = Locale.ENGLISH;
        ComponentUnitsDimensionParser componentUnitsDimensionParser = new ComponentUnitsDimensionParser(locale);
        componentUnitsDimensionParser.setStrictParsing(false);

        //
        for(Map.Entry<String, Map<String, Double>> unitDefinitionEntry:unitComponentDimensionByUnitDefinitionMap.entrySet()){
            Map<String, Double> parsedUnitComponentDimensionResult =
                    componentUnitsDimensionParser.parse(unitDefinitionEntry.getKey());

            assertTrue(String.format("Expected parsed unit definition dimension not correct. Input unit definition string %s. "
                            + "Expected unit definition dimension: %s. Parsed unit definition dimension: %s."
                    , unitDefinitionEntry.getKey() , unitDefinitionEntry.getValue().toString()
                    , parsedUnitComponentDimensionResult).toString()
                    , DimensionOperators.equalsDimension(parsedUnitComponentDimensionResult, unitDefinitionEntry.getValue()));
        }
    }

    @Test(expected = ParsingException.class)
    public void parseToComponentUnitsDimensionFromString_Should_Throw_ParsingException_When_Strict_Parsing_True()
            throws ParsingException {

        ComponentUnitsDimensionParser componentUnitsDimensionParser = new ComponentUnitsDimensionParser(Locale.ENGLISH);

        componentUnitsDimensionParser.setStrictParsing(true);

        componentUnitsDimensionParser.parse("((test");
    }
}