package com.isaacapps.unitconverterapp.processors.serializers.dimension.componentnunit;

import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;
import com.isaacapps.unitconverterapp.processors.serializers.SerializingException;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.DimensionSerializerBuilder;

import org.junit.Test;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ComponentUnitsDimensionSerializerTest {

    @Test
    public void serialize_ComponentUnitsDimension_Should_Provide_Parenthesized_String_From_Component_Units_Dimension_When_Parenthesis_Setting_True() throws SerializingException {
        String unitDefinition;
        Map<String,Double> unitComponentDimension;
        Map<String, Map<String,Double>> unitComponentDimensionByUnitDefinitionMap = new HashMap<>();

        //
        unitDefinition = "(meter)^2.000000";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put("meter", 2.0);

        //
        Locale locale = Locale.ENGLISH;
        ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer = new ComponentUnitsDimensionSerializer(locale);
        componentUnitsDimensionSerializer.setOperatorDisplayConfiguration(DimensionSerializerBuilder.OPERATOR_DISPLAY_CONFIGURATION.NO_DIVISION);
        componentUnitsDimensionSerializer.setIncludeParenthesesInExponentials(true);
        componentUnitsDimensionSerializer.setIncludeSpaceAfterOperator(true);

        //
        for(Map.Entry<String, Map<String, Double>> unitDefinitionEntry:unitComponentDimensionByUnitDefinitionMap.entrySet()){
            String serializedUnitDefinition =
                    componentUnitsDimensionSerializer.serialize(unitDefinitionEntry.getValue());

            assertEquals(String.format("Expected parsed unit definition dimension not correct. Input unit definition dimension: %s. "
                            + "Expected unit definition string %s. Constructed unit definition string: %s."
                    , unitDefinitionEntry.getValue().toString(), unitDefinitionEntry.getKey()
                    , serializedUnitDefinition),unitDefinitionEntry.getKey(), serializedUnitDefinition);
        }
    }

    @Test
    public void serialize_ComponentUnitsDimension_Should_Provide_NonParenthesized_String_From_Component_Units_Dimension() throws SerializingException {
        String unitDefinition;
        Map<String,Double> unitComponentDimension;
        Map<String, Map<String,Double>> unitComponentDimensionByUnitDefinitionMap = new HashMap<>();

        //
        unitDefinition = "meter";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put("meter", 1.0);

        //
        unitDefinition = "meter^3.000000";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put("meter", 3.0);

        //
        Locale locale = Locale.ENGLISH;
        ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer = new ComponentUnitsDimensionSerializer(locale);
        componentUnitsDimensionSerializer.setOperatorDisplayConfiguration(DimensionSerializerBuilder.OPERATOR_DISPLAY_CONFIGURATION.NO_DIVISION);
        componentUnitsDimensionSerializer.setIncludeParenthesesInExponentials(false);
        componentUnitsDimensionSerializer.setIncludeSpaceAfterOperator(true);

        //
        for(Map.Entry<String, Map<String, Double>> unitDefinitionEntry:unitComponentDimensionByUnitDefinitionMap.entrySet()){
            String serializedUnitDefinition =
                    componentUnitsDimensionSerializer.serialize(unitDefinitionEntry.getValue());

            assertEquals(String.format("Expected parsed unit definition dimension not correct. Input unit definition dimension: %s. "
                            + "Expected unit definition string %s. Constructed unit definition string: %s."
                    , unitDefinitionEntry.getValue().toString(), unitDefinitionEntry.getKey()
                    , serializedUnitDefinition),unitDefinitionEntry.getKey(), serializedUnitDefinition);
        }
    }

    @Test
    public void serialize_ComponentUnitsDimension_Should_Produce_Component_Dimension_String_Ordered_By_Exponent_Value() throws SerializingException {
        String unitDefinition;
        Map<String,Double> unitComponentDimension;
        Map<String, Map<String,Double>> unitComponentDimensionByUnitDefinitionMap = new HashMap<>();

        //
        unitDefinition = "eggs^8.000000* second^3.000000* meter^-5.000000";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put("meter", -5.0);
        unitComponentDimension.put("second", 3.0);
        unitComponentDimension.put("eggs", 8.0);

        //
        unitDefinition = "meter^-2.000000* second^-3.000000* eggs^-4.000000";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put("second", -3.0);
        unitComponentDimension.put("eggs", -4.0);
        unitComponentDimension.put("meter", -2.0);

        //
        Locale locale = Locale.ENGLISH;
        ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer = new ComponentUnitsDimensionSerializer(locale);
        componentUnitsDimensionSerializer.setOperatorDisplayConfiguration(DimensionSerializerBuilder.OPERATOR_DISPLAY_CONFIGURATION.NO_DIVISION);
        componentUnitsDimensionSerializer.setOrderDimensionItemsByExponentialValue(true);
        componentUnitsDimensionSerializer.setIncludeSpaceAfterOperator(true);

        //
        for(Map.Entry<String, Map<String, Double>> unitDefinitionEntry:unitComponentDimensionByUnitDefinitionMap.entrySet()){
            String serializedUnitDefinition =
                    componentUnitsDimensionSerializer.serialize(unitDefinitionEntry.getValue());

            assertEquals(String.format("Expected parsed unit definition dimension not correct. Input unit definition dimension: %s. "
                            + "Expected unit definition string %s. Constructed unit definition string: %s."
                    , unitDefinitionEntry.getValue().toString(), unitDefinitionEntry.getKey()
                    , serializedUnitDefinition),unitDefinitionEntry.getKey(), serializedUnitDefinition);
        }
    }

    @Test
    public void serialize_ComponentUnitsDimension_Should_Produce_Component_Dimension_String_Ordered_With_Negative_Exponents_Replaced_With_Multiple_Division() throws SerializingException {
        String unitDefinition;
        Map<String,Double> unitComponentDimension;
        Map<String, Map<String,Double>> unitComponentDimensionByUnitDefinitionMap = new HashMap<>();

        //
        unitDefinition = "eggs^8.000000/ second^3.000000/ meter^5.000000";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put("meter", -5.0);
        unitComponentDimension.put("second", -3.0);
        unitComponentDimension.put("eggs", 8.0);

        //
        unitDefinition = "1/ meter^2.000000/ second^3.000000/ eggs^4.000000";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put("second", -3.0);
        unitComponentDimension.put("eggs", -4.0);
        unitComponentDimension.put("meter", -2.0);

        //
        Locale locale = Locale.ENGLISH;
        ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer = new ComponentUnitsDimensionSerializer(locale);
        componentUnitsDimensionSerializer.setOperatorDisplayConfiguration(DimensionSerializerBuilder.OPERATOR_DISPLAY_CONFIGURATION.DIVISION_PER_NEGATIVE_EXPONENT);
        componentUnitsDimensionSerializer.setOrderDimensionItemsByExponentialValue(true);
        componentUnitsDimensionSerializer.setIncludeSpaceAfterOperator(true);

        //
        for(Map.Entry<String, Map<String, Double>> unitDefinitionEntry:unitComponentDimensionByUnitDefinitionMap.entrySet()){
            String serializedUnitDefinition =
                    componentUnitsDimensionSerializer.serialize(unitDefinitionEntry.getValue());

            assertEquals(String.format("Expected parsed unit definition dimension not correct. Input unit definition dimension: %s. "
                            + "Expected unit definition string %s. Constructed unit definition string: %s."
                    , unitDefinitionEntry.getValue().toString(), unitDefinitionEntry.getKey()
                    , serializedUnitDefinition),unitDefinitionEntry.getKey(), serializedUnitDefinition);
        }
    }

    @Test
    public void serialize_ComponentUnitsDimension_Should_Produce_Component_Dimension_String_Ordered_With_One_Division() throws SerializingException {
        String unitDefinition;
        Map<String,Double> unitComponentDimension;
        Map<String, Map<String,Double>> unitComponentDimensionByUnitDefinitionMap = new HashMap<>();

        //
        unitDefinition = "eggs^8.000000/( second^3.000000* meter^5.000000 )";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put("meter", -5.0);
        unitComponentDimension.put("second", -3.0);
        unitComponentDimension.put("eggs", 8.0);

        //
        unitDefinition = "1/( meter^2.000000* second^3.000000* eggs^4.000000 )";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put("second", -3.0);
        unitComponentDimension.put("eggs", -4.0);
        unitComponentDimension.put("meter", -2.0);

        //
        Locale locale = Locale.ENGLISH;
        ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer = new ComponentUnitsDimensionSerializer(locale);
        componentUnitsDimensionSerializer.setOperatorDisplayConfiguration(DimensionSerializerBuilder.OPERATOR_DISPLAY_CONFIGURATION.SINGLE_DIVISION);
        componentUnitsDimensionSerializer.setOrderDimensionItemsByExponentialValue(true);
        componentUnitsDimensionSerializer.setIncludeSpaceAfterOperator(true);

        //
        for(Map.Entry<String, Map<String, Double>> unitDefinitionEntry:unitComponentDimensionByUnitDefinitionMap.entrySet()){
            String serializedUnitDefinition =
                    componentUnitsDimensionSerializer.serialize(unitDefinitionEntry.getValue());

            assertEquals(String.format("Expected parsed unit definition dimension not correct. Input unit definition dimension: %s. "
                            + "Expected unit definition string %s. Constructed unit definition string: %s."
                    , unitDefinitionEntry.getValue().toString(), unitDefinitionEntry.getKey()
                    , serializedUnitDefinition),unitDefinitionEntry.getKey(), serializedUnitDefinition);
        }
    }
}