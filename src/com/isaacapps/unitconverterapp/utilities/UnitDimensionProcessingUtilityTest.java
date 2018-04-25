package com.isaacapps.unitconverterapp.utilities;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

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
        Map<String, Double> secondDimensionMap = firstDimensionMap;
        secondDimensionMap = new HashMap<>(firstDimensionMap);
        secondDimensionMap.put("item4", 0.0);

        secondDimensionMapList.add(secondDimensionMap);

        //
        for(Map<String, Double> dimensionMap:secondDimensionMapList){
            assertTrue(String.format("Dimension maps incorrectly not equal. Map1:%s, Map2:%s."
                    , representMapAsString(firstDimensionMap)
                    , representMapAsString(secondDimensionMap))
                    ,UnitDimensionProcessingUtility.equalsGenericDimension(firstDimensionMap
                            , dimensionMap, 0.00001));
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
                    ,UnitDimensionProcessingUtility.equalsGenericDimension(firstDimensionMap
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
        unitDefinition = "(meter)^5.123*meter^3";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put("meter", 8.123);

        //
        unitDefinition = "(meter)^5.123*meter^-3.0";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put("meter", 2.123);

        //
        unitDefinition = "((meter)^5*meter^-3.0)^2";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put("meter", 4.0);

        for(Map.Entry<String, Map<String, Double>> unitDefinitionEntry:unitComponentDimensionByUnitDefinitionMap.entrySet()){
            Map<String, Double> parsedUnitComponentDimensionResult = UnitDimensionProcessingUtility
                    .parseToComponentUnitsDimensionFromString(unitDefinitionEntry.getKey(), false);

            assertTrue(String.format("Expected parsed unit definition dimension not correct. Input unit definition %s. "
                            + "Expected unit definition dimension: %s. Parsed unit definition dimension: %s."
                        , unitDefinitionEntry.getKey() , representMapAsString(unitDefinitionEntry.getValue())
                        , representMapAsString(parsedUnitComponentDimensionResult))
                    , UnitDimensionProcessingUtility.equalsGenericDimension(parsedUnitComponentDimensionResult
                            , unitDefinitionEntry.getValue()
                            , 0.0001));
        }
    }

    @Test
    public void parseToComponentUnitsDimensionFromString_Should_Provide_Component_Units_Map_With_Unknown_Dimension_When_Strict_Parsing_False() {

    }

    @Test(expected = ParsingException.class)
    public void parseToComponentUnitsDimensionFromString_Should_Throw_ParsingEception_When_Strict_Parsing_True() {

    }

    @Test
    public void convertComponentUnitsDimensionToString_Should_Provide_Correct_String_From_Component_Units_Dimension(){

    }

    //Fundamental Units Parsing Tests
    @Test
    public void parseToFundamentalUnitTypesDimensionFromString_Should_Provide_Correct_Fundamental_Unit_Types_Map_From_String()
            throws ParsingException{

    }

    @Test
    public void parseToFundamentalUnitTypesDimensionFromString_Should_Provide_Fundamental_Unit_Types_Map_With_Unknown_Dimension_When_Strict_Parsing_False()
            throws ParsingException {

    }

    @Test(expected = ParsingException.class)
    public void parseToFundamentalUnitTypesDimensionFromString_Should_Throw_ParsingEception_When_Strict_Parsing_True() {

    }

    @Test
    public void convertFundamentalUnitTypesDimensionToString_Should_Provide_Correct_String_From_Fundamental_Unit_Types_Dimension(){

    }

    //
    private <K, V> String representMapAsString(Map<K, V> map){
        StringBuilder representationBuilder = new StringBuilder();

        for(Map.Entry<K, V> entry:map.entrySet()) {
            representationBuilder.append("[").append(entry.getKey()).append(",")
                    .append(entry.getValue()).append("]").append(" ");
        }

        return representationBuilder.insert(0, "{ ").append(" }").toString();
    }
}