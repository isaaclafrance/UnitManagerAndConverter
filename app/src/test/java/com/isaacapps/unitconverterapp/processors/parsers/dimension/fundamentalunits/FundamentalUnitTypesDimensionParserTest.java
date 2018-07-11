package com.isaacapps.unitconverterapp.processors.parsers.dimension.fundamentalunits;

import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel;
import com.isaacapps.unitconverterapp.processors.operators.dimensions.DimensionOperators;
import com.isaacapps.unitconverterapp.processors.parsers.ParsingException;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertTrue;

public class FundamentalUnitTypesDimensionParserTest {

    @Test
    public void parseToFundamentalUnitTypesDimensionFromString_Should_Provide_Correct_Fundamental_Unit_Types_Map_From_String()
            throws ParsingException {
        String fundamentalUnitTypeDefinition;
        Map<FundamentalUnitsDataModel.UNIT_TYPE, Double> fundamentalUnitTypeDimension;
        Map<String, Map<FundamentalUnitsDataModel.UNIT_TYPE,Double>> fundamentalUnitDimensionByDefinitionMap = new HashMap<>();

        //
        fundamentalUnitTypeDefinition = "LENGTH";
        fundamentalUnitDimensionByDefinitionMap.put(fundamentalUnitTypeDefinition, new HashMap<>());
        fundamentalUnitTypeDimension = fundamentalUnitDimensionByDefinitionMap.get(fundamentalUnitTypeDefinition);
        fundamentalUnitTypeDimension.put(FundamentalUnitsDataModel.UNIT_TYPE.LENGTH, 1.0);

        //
        fundamentalUnitTypeDefinition = "LENGTH^2";
        fundamentalUnitDimensionByDefinitionMap.put(fundamentalUnitTypeDefinition, new HashMap<>());
        fundamentalUnitTypeDimension = fundamentalUnitDimensionByDefinitionMap.get(fundamentalUnitTypeDefinition);
        fundamentalUnitTypeDimension.put(FundamentalUnitsDataModel.UNIT_TYPE.LENGTH, 2.0);

        //
        fundamentalUnitTypeDefinition = "(MASS)^5.123";
        fundamentalUnitDimensionByDefinitionMap.put(fundamentalUnitTypeDefinition, new HashMap<>());
        fundamentalUnitTypeDimension = fundamentalUnitDimensionByDefinitionMap.get(fundamentalUnitTypeDefinition);
        fundamentalUnitTypeDimension.put(FundamentalUnitsDataModel.UNIT_TYPE.MASS, 5.123);

        //
        fundamentalUnitTypeDefinition = "(MASS)^5.123 * MASS^3";
        fundamentalUnitDimensionByDefinitionMap.put(fundamentalUnitTypeDefinition, new HashMap<>());
        fundamentalUnitTypeDimension = fundamentalUnitDimensionByDefinitionMap.get(fundamentalUnitTypeDefinition);
        fundamentalUnitTypeDimension.put(FundamentalUnitsDataModel.UNIT_TYPE.MASS, 8.123);

        //
        fundamentalUnitTypeDefinition = "TIME^-5.123 * TIME^-3.0";
        fundamentalUnitDimensionByDefinitionMap.put(fundamentalUnitTypeDefinition, new HashMap<>());
        fundamentalUnitTypeDimension = fundamentalUnitDimensionByDefinitionMap.get(fundamentalUnitTypeDefinition);
        fundamentalUnitTypeDimension.put(FundamentalUnitsDataModel.UNIT_TYPE.TIME, -8.123);

        //
        fundamentalUnitTypeDefinition = "(TEMPERATURE)^5.123/TEMPERATURE^3.0";
        fundamentalUnitDimensionByDefinitionMap.put(fundamentalUnitTypeDefinition, new HashMap<>());
        fundamentalUnitTypeDimension = fundamentalUnitDimensionByDefinitionMap.get(fundamentalUnitTypeDefinition);
        fundamentalUnitTypeDimension.put(FundamentalUnitsDataModel.UNIT_TYPE.TEMPERATURE, 2.123);

        //
        fundamentalUnitTypeDefinition = "((LENGTH)^5 x LENGTH^-3.0)**2";
        fundamentalUnitDimensionByDefinitionMap.put(fundamentalUnitTypeDefinition, new HashMap<>());
        fundamentalUnitTypeDimension = fundamentalUnitDimensionByDefinitionMap.get(fundamentalUnitTypeDefinition);
        fundamentalUnitTypeDimension.put(FundamentalUnitsDataModel.UNIT_TYPE.LENGTH, 4.0);

        //
        fundamentalUnitTypeDefinition = "((LENGTH)^2*TIME^-4.0)^2 multiplied by LENGTH / TIME^2 divided by TIME raised to 5";
        fundamentalUnitDimensionByDefinitionMap.put(fundamentalUnitTypeDefinition, new HashMap<>());
        fundamentalUnitTypeDimension = fundamentalUnitDimensionByDefinitionMap.get(fundamentalUnitTypeDefinition);
        fundamentalUnitTypeDimension.put(FundamentalUnitsDataModel.UNIT_TYPE.LENGTH, 5.0);
        fundamentalUnitTypeDimension.put(FundamentalUnitsDataModel.UNIT_TYPE.TIME, -15.0);

        //
        fundamentalUnitTypeDefinition = "( TIME / (TIME*LENGTH/TEMPERATURE) )";
        fundamentalUnitDimensionByDefinitionMap.put(fundamentalUnitTypeDefinition, new HashMap<>());
        fundamentalUnitTypeDimension = fundamentalUnitDimensionByDefinitionMap.get(fundamentalUnitTypeDefinition);
        fundamentalUnitTypeDimension.put(FundamentalUnitsDataModel.UNIT_TYPE.TIME, 0.0);
        fundamentalUnitTypeDimension.put(FundamentalUnitsDataModel.UNIT_TYPE.LENGTH, -1.0);
        fundamentalUnitTypeDimension.put(FundamentalUnitsDataModel.UNIT_TYPE.TEMPERATURE, 1.0);

        //
        FundamentalUnitTypesDimensionParser fundamentalUnitTypesDimensionParser = new FundamentalUnitTypesDimensionParser();
        fundamentalUnitTypesDimensionParser.setStrictParsing(true);

        //
        for(Map.Entry<String, Map<FundamentalUnitsDataModel.UNIT_TYPE, Double>> unitDefinitionEntry:fundamentalUnitDimensionByDefinitionMap.entrySet()){
            Map<FundamentalUnitsDataModel.UNIT_TYPE, Double> parsedUnitComponentDimensionResult =
                    fundamentalUnitTypesDimensionParser.parse(unitDefinitionEntry.getKey());

            assertTrue(String.format("Expected parsed fundamental unit definition dimension not correct. " +
                            "Input fundamental unit definition string %s. "
                            + "Expected unit definition dimension: %s. Parsed unit definition dimension: %s."
                    , unitDefinitionEntry.getKey() , unitDefinitionEntry.getValue().toString()
                    , parsedUnitComponentDimensionResult).toString()
                    , DimensionOperators.equalsDimension(parsedUnitComponentDimensionResult, unitDefinitionEntry.getValue()));
        }
    }

    @Test
    public void parseToFundamentalUnitTypesDimensionFromString_Should_Provide_Fundamental_Unit_Types_Map_With_Unknown_Dimension_When_Strict_Parsing_False()
            throws ParsingException {
        String unitDefinition;
        Map<FundamentalUnitsDataModel.UNIT_TYPE,Double> unitComponentDimension;
        Map<String, Map<FundamentalUnitsDataModel.UNIT_TYPE,Double>> unitComponentDimensionByUnitDefinitionMap = new HashMap<>();

        //
        unitDefinition = "%%%";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put(FundamentalUnitsDataModel.UNIT_TYPE.UNKNOWN, 1.0);

        //
        unitDefinition = "( $$$!% ) / TIME^2";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put(FundamentalUnitsDataModel.UNIT_TYPE.UNKNOWN, 1.0);
        unitComponentDimension.put(FundamentalUnitsDataModel.UNIT_TYPE.TIME, -2.0);

        //
        FundamentalUnitTypesDimensionParser fundamentalUnitTypesDimensionParser = new FundamentalUnitTypesDimensionParser();
        fundamentalUnitTypesDimensionParser.setStrictParsing(false);

        //
        for(Map.Entry<String, Map<FundamentalUnitsDataModel.UNIT_TYPE, Double>> unitDefinitionEntry:unitComponentDimensionByUnitDefinitionMap.entrySet()){
            Map<FundamentalUnitsDataModel.UNIT_TYPE, Double> parsedUnitComponentDimensionResult =
                    fundamentalUnitTypesDimensionParser.parse(unitDefinitionEntry.getKey());

            assertTrue(String.format("Expected parsed unit definition dimension not correct. Input unit definition string %s. "
                            + "Expected unit definition dimension: %s. Parsed unit definition dimension: %s."
                    , unitDefinitionEntry.getKey() , unitDefinitionEntry.getValue().toString()
                    , parsedUnitComponentDimensionResult).toString()
                    , DimensionOperators.equalsDimension(parsedUnitComponentDimensionResult, unitDefinitionEntry.getValue()));
        }
    }

    @Test(expected = ParsingException.class)
    public void parseToFundamentalUnitTypesDimensionFromString_Should_Throw_ParsingException_When_Strict_Parsing_True() throws ParsingException {
        FundamentalUnitTypesDimensionParser fundamentalUnitTypesDimensionParser = new FundamentalUnitTypesDimensionParser();

        fundamentalUnitTypesDimensionParser.setStrictParsing(true);

        fundamentalUnitTypesDimensionParser.parse("Meters / LENGTH");
    }

}