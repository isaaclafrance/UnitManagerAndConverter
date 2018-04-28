package com.isaacapps.unitconverterapp.utilities;

import com.isaacapps.unitconverterapp.models.Unit;

import org.junit.Test;

import java.util.*;

import static com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel.UNIT_TYPE;
import static com.isaacapps.unitconverterapp.utilities.UnitDimensionProcessingUtility.*;
import static junit.framework.Assert.*;

public class UnitDimensionProcessingUtilityTest {

    //Subcomponent Tests
    @Test
    public void equalsGenericDimension_Should_Return_True_When_Provided_Dimension_Maps_Have_Corresponding_Dimension_Items_And_Dimension_Values(){
        Map<String, Double> firstDimensionMap = new HashMap<>();
        firstDimensionMap.put("item1", 1.0);
        firstDimensionMap.put("item2", 2.0);
        firstDimensionMap.put("item3", 3.0);

        List<Map<String, Double>> secondDimensionMapList = new ArrayList<>();

        //
        secondDimensionMapList.add(firstDimensionMap);

        //
        Map<String, Double> secondDimensionMap = new HashMap<>(firstDimensionMap);
        secondDimensionMap.put("item4", 0.0);

        secondDimensionMapList.add(secondDimensionMap);

        //
        for(Map<String, Double> dimensionMap:secondDimensionMapList){
            assertTrue(String.format("Dimension maps incorrectly not equal. Map1:%s, Map2:%s."
                    , representMapAsString(firstDimensionMap)
                    , representMapAsString(secondDimensionMap))
                    , equalsGenericDimension(firstDimensionMap, dimensionMap, 0.00001));
        }
    }

    @Test
    public void equalsGenericDimension_Should_Return_False_When_Provided_Dimension_Maps_Dont_Have_Corresponding_Dimension_Items_And_Dimension_Values(){
        Map<String, Double> firstDimensionMap = new HashMap<>();
        firstDimensionMap.put("item1", 1.0);
        firstDimensionMap.put("item2", 2.0);
        firstDimensionMap.put("item3", 3.0);

        List<Map<String, Double>> secondDimensionMapList = new ArrayList<>();

        //
        Map<String, Double> secondDimensionMap = new HashMap<>(firstDimensionMap);
        secondDimensionMap.put("item4", 4.0);

        secondDimensionMapList.add(secondDimensionMap);

        //
        secondDimensionMap = new HashMap<>(firstDimensionMap);
        secondDimensionMap.remove("item3");

        secondDimensionMapList.add(secondDimensionMap);

        //
        secondDimensionMap = new HashMap<>(firstDimensionMap);
        secondDimensionMap.remove("item2");
        secondDimensionMap.put("item5", 7.0);

        secondDimensionMapList.add(secondDimensionMap);

        //
        secondDimensionMap = new HashMap<>(firstDimensionMap);
        secondDimensionMap.replace("item1", 9.0);

        secondDimensionMapList.add(secondDimensionMap);

        //
        secondDimensionMap = new HashMap<>();

        secondDimensionMapList.add(secondDimensionMap);

        //
        for(Map<String, Double> dimensionMap:secondDimensionMapList){
            assertFalse(String.format("Dimension maps incorrectly equal. Map1:%s, Map2:%s."
                    , representMapAsString(firstDimensionMap)
                    , representMapAsString(secondDimensionMap))
                    , equalsGenericDimension(firstDimensionMap
                            , dimensionMap, 0.00001));
        }
    }

    //Component Units Parsing Tests
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

        for(Map.Entry<String, Map<String, Double>> unitDefinitionEntry:unitComponentDimensionByUnitDefinitionMap.entrySet()){
            Map<String, Double> parsedUnitComponentDimensionResult =
                    parseToComponentUnitsDimensionFromString(unitDefinitionEntry.getKey(), false);

            assertTrue(String.format("Expected parsed unit definition dimension not correct. Input unit definition string %s. "
                            + "Expected unit definition dimension: %s. Parsed unit definition dimension: %s."
                        , unitDefinitionEntry.getKey() , representMapAsString(unitDefinitionEntry.getValue())
                        , representMapAsString(parsedUnitComponentDimensionResult))
                    , equalsGenericDimension(parsedUnitComponentDimensionResult
                            , unitDefinitionEntry.getValue()
                            , 0.0001));
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
        unitDefinition = "( ^^eters ) / second^2";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put(Unit.UNKNOWN_UNIT_NAME, 1.0);
        unitComponentDimension.put("second", -2.0);

        //
        for(Map.Entry<String, Map<String, Double>> unitDefinitionEntry:unitComponentDimensionByUnitDefinitionMap.entrySet()){
            Map<String, Double> parsedUnitComponentDimensionResult =
                    parseToComponentUnitsDimensionFromString(unitDefinitionEntry.getKey(), false);

            assertTrue(String.format("Expected parsed unit definition dimension not correct. Input unit definition string %s. "
                            + "Expected unit definition dimension: %s. Parsed unit definition dimension: %s."
                    , unitDefinitionEntry.getKey() , representMapAsString(unitDefinitionEntry.getValue())
                    , representMapAsString(parsedUnitComponentDimensionResult))
                    , equalsGenericDimension(parsedUnitComponentDimensionResult
                            , unitDefinitionEntry.getValue()
                            , 0.0001));
        }
    }

    @Test(expected = ParsingException.class)
    public void parseToComponentUnitsDimensionFromString_Should_Throw_ParsingException_When_Strict_Parsing_True()
            throws ParsingException {
        parseToComponentUnitsDimensionFromString("((test", true);
    }

    @Test
    public void convertComponentUnitsDimensionToString_Should_Provide_Correct_String_From_Component_Units_Dimension(){
        String unitDefinition;
        Map<String,Double> unitComponentDimension;
        Map<String, Map<String,Double>> unitComponentDimensionByUnitDefinitionMap = new HashMap<>();

        //
        unitDefinition = "meter";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put("meter", 1.0);

        //
        unitDefinition = "(meter)^2.0";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put("meter", 2.0);

        //
        unitDefinition = "(meter)^3.0 * (second)^-5.0";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put("meter", 3.0);
        unitComponentDimension.put("second", -5.0);

        //
        for(Map.Entry<String, Map<String, Double>> unitDefinitionEntry:unitComponentDimensionByUnitDefinitionMap.entrySet()){
            String constructedUnitDefinition =
                    convertComponentUnitsDimensionToString(unitDefinitionEntry.getValue());

            assertEquals(String.format("Expected parsed unit definition dimension not correct. Input unit definition dimension: %s. "
                            + "Expected unit definition string %s. Constructed unit definition string: %s."
                    , representMapAsString(unitDefinitionEntry.getValue()), unitDefinitionEntry.getKey()
                    , constructedUnitDefinition),unitDefinitionEntry.getKey(), constructedUnitDefinition);
        }
    }

    //Fundamental Unit Types Parsing Tests
    @Test
    public void parseToFundamentalUnitTypesDimensionFromString_Should_Provide_Correct_Fundamental_Unit_Types_Map_From_String()
            throws ParsingException{
        String fundamentalUnitTypeDefinition;
        Map<UNIT_TYPE, Double> fundamentalUnitTypeDimension;
        Map<String, Map<UNIT_TYPE,Double>> fundamentalUnitDimensionByDefinitionMap = new HashMap<>();

        //
        fundamentalUnitTypeDefinition = "LENGTH";
        fundamentalUnitDimensionByDefinitionMap.put(fundamentalUnitTypeDefinition, new HashMap<>());
        fundamentalUnitTypeDimension = fundamentalUnitDimensionByDefinitionMap.get(fundamentalUnitTypeDefinition);
        fundamentalUnitTypeDimension.put(UNIT_TYPE.LENGTH, 1.0);

        //
        fundamentalUnitTypeDefinition = "LENGTH^2";
        fundamentalUnitDimensionByDefinitionMap.put(fundamentalUnitTypeDefinition, new HashMap<>());
        fundamentalUnitTypeDimension = fundamentalUnitDimensionByDefinitionMap.get(fundamentalUnitTypeDefinition);
        fundamentalUnitTypeDimension.put(UNIT_TYPE.LENGTH, 2.0);

        //
        fundamentalUnitTypeDefinition = "(MASS)^5.123";
        fundamentalUnitDimensionByDefinitionMap.put(fundamentalUnitTypeDefinition, new HashMap<>());
        fundamentalUnitTypeDimension = fundamentalUnitDimensionByDefinitionMap.get(fundamentalUnitTypeDefinition);
        fundamentalUnitTypeDimension.put(UNIT_TYPE.MASS, 5.123);

        //
        fundamentalUnitTypeDefinition = "(MASS)^5.123 * MASS^3";
        fundamentalUnitDimensionByDefinitionMap.put(fundamentalUnitTypeDefinition, new HashMap<>());
        fundamentalUnitTypeDimension = fundamentalUnitDimensionByDefinitionMap.get(fundamentalUnitTypeDefinition);
        fundamentalUnitTypeDimension.put(UNIT_TYPE.MASS, 8.123);

        //
        fundamentalUnitTypeDefinition = "TIME^-5.123 * TIME^-3.0";
        fundamentalUnitDimensionByDefinitionMap.put(fundamentalUnitTypeDefinition, new HashMap<>());
        fundamentalUnitTypeDimension = fundamentalUnitDimensionByDefinitionMap.get(fundamentalUnitTypeDefinition);
        fundamentalUnitTypeDimension.put(UNIT_TYPE.TIME, -8.123);

        //
        fundamentalUnitTypeDefinition = "(TEMPERATURE)^5.123/TEMPERATURE^3.0";
        fundamentalUnitDimensionByDefinitionMap.put(fundamentalUnitTypeDefinition, new HashMap<>());
        fundamentalUnitTypeDimension = fundamentalUnitDimensionByDefinitionMap.get(fundamentalUnitTypeDefinition);
        fundamentalUnitTypeDimension.put(UNIT_TYPE.TEMPERATURE, 2.123);

        //
        fundamentalUnitTypeDefinition = "((LENGTH)^5 x LENGTH^-3.0)**2";
        fundamentalUnitDimensionByDefinitionMap.put(fundamentalUnitTypeDefinition, new HashMap<>());
        fundamentalUnitTypeDimension = fundamentalUnitDimensionByDefinitionMap.get(fundamentalUnitTypeDefinition);
        fundamentalUnitTypeDimension.put(UNIT_TYPE.LENGTH, 4.0);

        //
        fundamentalUnitTypeDefinition = "((LENGTH)^2*TIME^-4.0)^2 multiplied by LENGTH / TIME^2 divided by TIME raised to 5";
        fundamentalUnitDimensionByDefinitionMap.put(fundamentalUnitTypeDefinition, new HashMap<>());
        fundamentalUnitTypeDimension = fundamentalUnitDimensionByDefinitionMap.get(fundamentalUnitTypeDefinition);
        fundamentalUnitTypeDimension.put(UNIT_TYPE.LENGTH, 5.0);
        fundamentalUnitTypeDimension.put(UNIT_TYPE.TIME, -15.0);

        //
        fundamentalUnitTypeDefinition = "( TIME / (TIME*LENGTH/TEMPERATURE) )";
        fundamentalUnitDimensionByDefinitionMap.put(fundamentalUnitTypeDefinition, new HashMap<>());
        fundamentalUnitTypeDimension = fundamentalUnitDimensionByDefinitionMap.get(fundamentalUnitTypeDefinition);
        fundamentalUnitTypeDimension.put(UNIT_TYPE.TIME, 0.0);
        fundamentalUnitTypeDimension.put(UNIT_TYPE.LENGTH, -1.0);
        fundamentalUnitTypeDimension.put(UNIT_TYPE.TEMPERATURE, 1.0);

        for(Map.Entry<String, Map<UNIT_TYPE, Double>> unitDefinitionEntry:fundamentalUnitDimensionByDefinitionMap.entrySet()){
            Map<UNIT_TYPE, Double> parsedUnitComponentDimensionResult =
                    parseToFundamentalUnitTypesDimensionFromString(unitDefinitionEntry.getKey(), false);

            assertTrue(String.format("Expected parsed fundamental unit definition dimension not correct. " +
                            "Input fundamental unit definition string %s. "
                            + "Expected unit definition dimension: %s. Parsed unit definition dimension: %s."
                    , unitDefinitionEntry.getKey() , representMapAsString(unitDefinitionEntry.getValue())
                    , representMapAsString(parsedUnitComponentDimensionResult))
                    , equalsGenericDimension(parsedUnitComponentDimensionResult, unitDefinitionEntry.getValue()
                            , 0.0001));
        }
    }

    @Test
    public void parseToFundamentalUnitTypesDimensionFromString_Should_Provide_Fundamental_Unit_Types_Map_With_Unknown_Dimension_When_Strict_Parsing_False()
            throws ParsingException {
        String unitDefinition;
        Map<UNIT_TYPE,Double> unitComponentDimension;
        Map<String, Map<UNIT_TYPE,Double>> unitComponentDimensionByUnitDefinitionMap = new HashMap<>();

        //
        unitDefinition = "%%%";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put(UNIT_TYPE.UNKNOWN, 1.0);

        //
        unitDefinition = "( $$$!% ) / TIME^2";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put(UNIT_TYPE.UNKNOWN, 1.0);
        unitComponentDimension.put(UNIT_TYPE.TIME, -2.0);

        //
        for(Map.Entry<String, Map<UNIT_TYPE, Double>> unitDefinitionEntry:unitComponentDimensionByUnitDefinitionMap.entrySet()){
            Map<UNIT_TYPE, Double> parsedUnitComponentDimensionResult =
                    parseToFundamentalUnitTypesDimensionFromString(unitDefinitionEntry.getKey(), false);

            assertTrue(String.format("Expected parsed unit definition dimension not correct. Input unit definition string %s. "
                            + "Expected unit definition dimension: %s. Parsed unit definition dimension: %s."
                    , unitDefinitionEntry.getKey() , representMapAsString(unitDefinitionEntry.getValue())
                    , representMapAsString(parsedUnitComponentDimensionResult))
                    , equalsGenericDimension(parsedUnitComponentDimensionResult
                            , unitDefinitionEntry.getValue()
                            , 0.0001));
        }
    }

    @Test(expected = ParsingException.class)
    public void parseToFundamentalUnitTypesDimensionFromString_Should_Throw_ParsingException_When_Strict_Parsing_True() throws ParsingException {
        parseToFundamentalUnitTypesDimensionFromString("Meters / LENGTH", true);
    }

    @Test
    public void convertFundamentalUnitTypesDimensionToString_Should_Provide_Correct_String_From_Fundamental_Unit_Types_Dimension(){
        String unitDefinition;
        Map<UNIT_TYPE,Double> unitComponentDimension;
        Map<String, Map<UNIT_TYPE,Double>> unitComponentDimensionByUnitDefinitionMap = new HashMap<>();

        //
        unitDefinition = "LENGTH";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put(UNIT_TYPE.LENGTH, 1.0);

        //
        unitDefinition = "(TIME)^2.0";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put(UNIT_TYPE.TIME, 2.0);

        //
        unitDefinition = "(TEMPERATURE)^3.0 * (CHARGE)^-5.0";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put(UNIT_TYPE.TEMPERATURE, 3.0);
        unitComponentDimension.put(UNIT_TYPE.CHARGE, -5.0);

        //
        for(Map.Entry<String, Map<UNIT_TYPE, Double>> unitDefinitionEntry:unitComponentDimensionByUnitDefinitionMap.entrySet()){
            String constructedUnitDefinition =
                    convertFundamentalUnitTypesDimensionToString(unitDefinitionEntry.getValue());

            assertEquals(String.format("Expected parsed unit definition dimension not correct. Input unit definition dimension: %s. "
                            + "Expected unit definition string %s. Constructed unit definition string: %s."
                    , representMapAsString(unitDefinitionEntry.getValue()), unitDefinitionEntry.getKey()
                    , constructedUnitDefinition),unitDefinitionEntry.getKey(), constructedUnitDefinition);
        }
    }

    //Test helper methods
    /**
     * Represents a map in the following format: "{[key1, value1] [key2, value2]}"
     */
    private <K, V> String representMapAsString(Map<K, V> map){
        StringBuilder representationBuilder = new StringBuilder();

        for(Map.Entry<K, V> entry:map.entrySet()) {
            representationBuilder.append("[").append(entry.getKey()).append(",")
                    .append(entry.getValue()).append("]").append(" ");
        }

        return representationBuilder.insert(0, "{ ").append(" }").toString();
    }
}