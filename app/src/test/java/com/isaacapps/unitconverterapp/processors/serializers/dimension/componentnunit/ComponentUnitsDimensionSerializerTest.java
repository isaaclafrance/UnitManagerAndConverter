package com.isaacapps.unitconverterapp.processors.serializers.dimension.componentnunit;

import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;
import com.isaacapps.unitconverterapp.processors.serializers.SerializingException;

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
    public void serializeComponentUnitsDimensionToString_Should_Provide_Parenthesized_String_From_Component_Units_Dimension_When_Parenthesis_Setting_True() throws SerializingException {
        String unitDefinition;
        Map<String,Double> unitComponentDimension;
        Map<String, Map<String,Double>> unitComponentDimensionByUnitDefinitionMap = new HashMap<>();

        //
        unitDefinition = "(meter)^2.000000";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put("meter", 2.0);

        //
        unitDefinition = "(meter)^3.000000 * (second)^-5.000000";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put("meter", 3.0);
        unitComponentDimension.put("second", -5.0);

        //
        IFormatter mockFormatter = mock(IFormatter.class);
        when(mockFormatter.format(anyString())).thenAnswer(returnsFirstArg());

        Locale locale = Locale.ENGLISH;
        ComponentUnitsDimensionItemSerializer componentUnitsDimensionItemSerializer = new ComponentUnitsDimensionItemSerializer(locale, mockFormatter);
        ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer = new ComponentUnitsDimensionSerializer(locale, componentUnitsDimensionItemSerializer);
        componentUnitsDimensionSerializer.setIncludeParentheses(true);

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
    public void serializeComponentUnitsDimensionToString_Should_Provide_NonParenthesized_String_From_Component_Units_Dimension() throws SerializingException {
        String unitDefinition;
        Map<String,Double> unitComponentDimension;
        Map<String, Map<String,Double>> unitComponentDimensionByUnitDefinitionMap = new HashMap<>();

        //
        unitDefinition = "meter";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put("meter", 1.0);

        //
        unitDefinition = "meter^3.000000 * second^-5.000000";
        unitComponentDimensionByUnitDefinitionMap.put(unitDefinition, new HashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(unitDefinition);
        unitComponentDimension.put("meter", 3.0);
        unitComponentDimension.put("second", -5.0);

        //
        IFormatter mockFormatter = mock(IFormatter.class);
        when(mockFormatter.format(anyString())).thenAnswer(returnsFirstArg());

        Locale locale = Locale.ENGLISH;
        ComponentUnitsDimensionItemSerializer componentUnitsDimensionItemSerializer = new ComponentUnitsDimensionItemSerializer(locale, mockFormatter);
        ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer = new ComponentUnitsDimensionSerializer(locale, componentUnitsDimensionItemSerializer);
        componentUnitsDimensionSerializer.setIncludeParentheses(false);

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