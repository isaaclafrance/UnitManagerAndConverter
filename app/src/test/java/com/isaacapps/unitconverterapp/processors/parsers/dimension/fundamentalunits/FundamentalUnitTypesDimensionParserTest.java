package com.isaacapps.unitconverterapp.processors.parsers.dimension.fundamentalunits;

import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel.UNIT_TYPE;
import com.isaacapps.unitconverterapp.processors.formatters.numbers.RoundingFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.text.GeneralTextFormatter;
import com.isaacapps.unitconverterapp.processors.operators.dimensions.DimensionOperators;
import com.isaacapps.unitconverterapp.processors.parsers.ParsingException;

import org.junit.Test;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static junit.framework.Assert.assertTrue;

public class FundamentalUnitTypesDimensionParserTest {

    @Test
    public void parseToFundamentalUnitTypesDimensionFromString_Should_Provide_Correct_Fundamental_Unit_Types_Map_From_String()
            throws ParsingException {
        String fundamentalUnitTypeDefinition;
        Map<UNIT_TYPE, Double> fundamentalUnitTypeDimension;
        Map<String, Map<UNIT_TYPE,Double>> fundamentalUnitDimensionByDefinitionMap = new HashMap<>();

        //
        fundamentalUnitTypeDefinition = "LENGTH";
        fundamentalUnitDimensionByDefinitionMap.put(fundamentalUnitTypeDefinition, new EnumMap<>(UNIT_TYPE.class));
        fundamentalUnitTypeDimension = fundamentalUnitDimensionByDefinitionMap.get(fundamentalUnitTypeDefinition);
        fundamentalUnitTypeDimension.put(UNIT_TYPE.LENGTH, 1.0);

        //
        fundamentalUnitTypeDefinition = "LENGTH^2";
        fundamentalUnitDimensionByDefinitionMap.put(fundamentalUnitTypeDefinition, new EnumMap<>(UNIT_TYPE.class));
        fundamentalUnitTypeDimension = fundamentalUnitDimensionByDefinitionMap.get(fundamentalUnitTypeDefinition);
        fundamentalUnitTypeDimension.put(UNIT_TYPE.LENGTH, 2.0);

        //
        fundamentalUnitTypeDefinition = "(MASS)^5.123";
        fundamentalUnitDimensionByDefinitionMap.put(fundamentalUnitTypeDefinition, new EnumMap<>(UNIT_TYPE.class));
        fundamentalUnitTypeDimension = fundamentalUnitDimensionByDefinitionMap.get(fundamentalUnitTypeDefinition);
        fundamentalUnitTypeDimension.put(UNIT_TYPE.MASS, 5.123);

        //
        fundamentalUnitTypeDefinition = "(MASS)^5.123 * MASS^3";
        fundamentalUnitDimensionByDefinitionMap.put(fundamentalUnitTypeDefinition, new EnumMap<>(UNIT_TYPE.class));
        fundamentalUnitTypeDimension = fundamentalUnitDimensionByDefinitionMap.get(fundamentalUnitTypeDefinition);
        fundamentalUnitTypeDimension.put(UNIT_TYPE.MASS, 8.123);

        //
        fundamentalUnitTypeDefinition = "TIME^-5.123 * TIME^-3.0";
        fundamentalUnitDimensionByDefinitionMap.put(fundamentalUnitTypeDefinition, new EnumMap<>(UNIT_TYPE.class));
        fundamentalUnitTypeDimension = fundamentalUnitDimensionByDefinitionMap.get(fundamentalUnitTypeDefinition);
        fundamentalUnitTypeDimension.put(UNIT_TYPE.TIME, -8.123);

        //
        fundamentalUnitTypeDefinition = "(TEMPERATURE)^5.123/TEMPERATURE^3.0";
        fundamentalUnitDimensionByDefinitionMap.put(fundamentalUnitTypeDefinition, new EnumMap<>(UNIT_TYPE.class));
        fundamentalUnitTypeDimension = fundamentalUnitDimensionByDefinitionMap.get(fundamentalUnitTypeDefinition);
        fundamentalUnitTypeDimension.put(UNIT_TYPE.TEMPERATURE, 2.123);

        //
        fundamentalUnitTypeDefinition = "((LENGTH)^5 x LENGTH^-3.0)**2";
        fundamentalUnitDimensionByDefinitionMap.put(fundamentalUnitTypeDefinition, new EnumMap<>(UNIT_TYPE.class));
        fundamentalUnitTypeDimension = fundamentalUnitDimensionByDefinitionMap.get(fundamentalUnitTypeDefinition);
        fundamentalUnitTypeDimension.put(UNIT_TYPE.LENGTH, 4.0);

        //
        fundamentalUnitTypeDefinition = "((LENGTH)^2*TIME^-4.0)^2 multiplied by LENGTH / TIME^2 divided by TIME raised to 5";
        fundamentalUnitDimensionByDefinitionMap.put(fundamentalUnitTypeDefinition, new EnumMap<>(UNIT_TYPE.class));
        fundamentalUnitTypeDimension = fundamentalUnitDimensionByDefinitionMap.get(fundamentalUnitTypeDefinition);
        fundamentalUnitTypeDimension.put(UNIT_TYPE.LENGTH, 5.0);
        fundamentalUnitTypeDimension.put(UNIT_TYPE.TIME, -15.0);

        //
        fundamentalUnitTypeDefinition = "( TIME / (TIME*LENGTH/TEMPERATURE) )";
        fundamentalUnitDimensionByDefinitionMap.put(fundamentalUnitTypeDefinition, new EnumMap<>(UNIT_TYPE.class));
        fundamentalUnitTypeDimension = fundamentalUnitDimensionByDefinitionMap.get(fundamentalUnitTypeDefinition);
        fundamentalUnitTypeDimension.put(UNIT_TYPE.TIME, 0.0);
        fundamentalUnitTypeDimension.put(UNIT_TYPE.LENGTH, -1.0);
        fundamentalUnitTypeDimension.put(UNIT_TYPE.TEMPERATURE, 1.0);

        //
        Locale locale = Locale.ENGLISH;
        FundamentalUnitTypesDimensionParser fundamentalUnitTypesDimensionParser = new FundamentalUnitTypesDimensionParser(locale);
        fundamentalUnitTypesDimensionParser.setStrictParsing(true);

        //
        for(Map.Entry<String, Map<UNIT_TYPE, Double>> unitDefinitionEntry:fundamentalUnitDimensionByDefinitionMap.entrySet()){
            Map<UNIT_TYPE, Double> parsedUnitComponentDimensionResult = fundamentalUnitTypesDimensionParser.parse(unitDefinitionEntry.getKey());

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
        Map<UNIT_TYPE,Double> unitComponentDimension;
        Map<String, Map<UNIT_TYPE,Double>> unitComponentDimensionByUnitDefinitionMap = new HashMap<>();

        //
        unitDefinition = "%%%";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new EnumMap<>(UNIT_TYPE.class));
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put(UNIT_TYPE.UNKNOWN, 1.0);

        //
        unitDefinition = "( $$$!% ) / TIME^2";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new EnumMap<>(UNIT_TYPE.class));
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put(UNIT_TYPE.UNKNOWN, 1.0);
        unitComponentDimension.put(UNIT_TYPE.TIME, -2.0);

        //
        Locale locale = Locale.ENGLISH;
        FundamentalUnitTypesDimensionParser fundamentalUnitTypesDimensionParser = new FundamentalUnitTypesDimensionParser(locale);
        fundamentalUnitTypesDimensionParser.setStrictParsing(false);

        //
        for(Map.Entry<String, Map<UNIT_TYPE, Double>> unitDefinitionEntry:unitComponentDimensionByUnitDefinitionMap.entrySet()){
            Map<UNIT_TYPE, Double> parsedUnitComponentDimensionResult =
                    fundamentalUnitTypesDimensionParser.parse(unitDefinitionEntry.getKey());

            assertTrue(String.format("Expected parsed unit definition dimension not correct. Input unit definition string %s. "
                            + "Expected unit definition dimension: %s. Parsed unit definition dimension: %s."
                    , unitDefinitionEntry.getKey() , unitDefinitionEntry.getValue().toString()
                    , parsedUnitComponentDimensionResult)
                    , DimensionOperators.equalsDimension(parsedUnitComponentDimensionResult, unitDefinitionEntry.getValue()));
        }
    }

    @Test(expected = ParsingException.class)
    public void parseToFundamentalUnitTypesDimensionFromString_Should_Throw_ParsingException_When_Strict_Parsing_True() throws ParsingException {
        FundamentalUnitTypesDimensionParser fundamentalUnitTypesDimensionParser = new FundamentalUnitTypesDimensionParser(Locale.ENGLISH);

        fundamentalUnitTypesDimensionParser.setStrictParsing(true);

        fundamentalUnitTypesDimensionParser.parse("Meters / LENGTH");
    }

}