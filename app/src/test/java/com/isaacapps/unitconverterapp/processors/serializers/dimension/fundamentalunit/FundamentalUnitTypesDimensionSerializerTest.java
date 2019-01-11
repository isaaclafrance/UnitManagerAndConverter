package com.isaacapps.unitconverterapp.processors.serializers.dimension.fundamentalunit;

import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel.UNIT_TYPE;
import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;
import com.isaacapps.unitconverterapp.processors.serializers.SerializingException;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.DimensionSerializerBuilder;

import org.junit.Test;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FundamentalUnitTypesDimensionSerializerTest {
    @Test
    public void serialize_FundamentalUnitsDimension_Should_Provide_Parenthesized_String_From_Component_Units_Dimension_When_Parenthesis_Setting_True() throws SerializingException {
        String fundamentalUnitTypeDefinition;
        Map<UNIT_TYPE,Double> fundamentalUnitDimension;
        Map<String, Map<UNIT_TYPE,Double>> fundamentalUnitDimensionByUnitTypeDefinitionMap = new HashMap<>();

        //
        fundamentalUnitTypeDefinition = "(LENGTH)^2.000000";
        fundamentalUnitDimensionByUnitTypeDefinitionMap.put(fundamentalUnitTypeDefinition, new EnumMap(UNIT_TYPE.class));
        fundamentalUnitDimension = fundamentalUnitDimensionByUnitTypeDefinitionMap.get(fundamentalUnitTypeDefinition);
        fundamentalUnitDimension.put(UNIT_TYPE.LENGTH, 2.0);

        //
        IFormatter mockFormatter = mock(IFormatter.class);
        when(mockFormatter.format(anyString())).thenAnswer(returnsFirstArg());

        Locale locale = Locale.ENGLISH;
        FundamentalUnitTypesDimensionItemSerializer componentUnitsDimensionItemSerializer = new FundamentalUnitTypesDimensionItemSerializer(locale, mockFormatter);
        FundamentalUnitTypesDimensionSerializer fundamentalUnitTyesDimensionSerializer = new FundamentalUnitTypesDimensionSerializer(locale, componentUnitsDimensionItemSerializer);
        fundamentalUnitTyesDimensionSerializer.setOperatorDisplayConfiguration(DimensionSerializerBuilder.OPERATOR_DISPLAY_CONFIGURATION.NO_DIVISION);
        fundamentalUnitTyesDimensionSerializer.setIncludeParenthesesInExponentials(true);

        //
        for(Map.Entry<String, Map<UNIT_TYPE, Double>> unitTypesDefinitionEntry:fundamentalUnitDimensionByUnitTypeDefinitionMap.entrySet()){
            String serializedUnitTypesDefinition = fundamentalUnitTyesDimensionSerializer.serialize(unitTypesDefinitionEntry.getValue());

            assertEquals(String.format("Expected parsed unit types definition dimension not correct. Input unit types definition dimension: %s. "
                            + "Expected unit types definition string %s. Constructed unit definition string: %s."
                    , unitTypesDefinitionEntry.getValue().toString(), unitTypesDefinitionEntry.getKey()
                    , serializedUnitTypesDefinition),unitTypesDefinitionEntry.getKey(), serializedUnitTypesDefinition);
        }
    }

    @Test
    public void serializeFundamentalUnitTypesDimensionToString_Should_Provide_NonParenthesized_String_From_Component_Units_Dimension() throws SerializingException {
        String fundamentalUnitTypesDefinition;
        Map<UNIT_TYPE,Double> fundamentalUnitTypesDimension;
        Map<String, Map<UNIT_TYPE,Double>> fundamentalUnitTypesDimensionByUnitTypeDefinitionMap = new HashMap<>();

        //
        fundamentalUnitTypesDefinition = "LENGTH";
        fundamentalUnitTypesDimensionByUnitTypeDefinitionMap.put(fundamentalUnitTypesDefinition, new EnumMap(UNIT_TYPE.class));
        fundamentalUnitTypesDimension = fundamentalUnitTypesDimensionByUnitTypeDefinitionMap.get(fundamentalUnitTypesDefinition);
        fundamentalUnitTypesDimension.put(UNIT_TYPE.LENGTH, 1.0);

        //
        fundamentalUnitTypesDefinition = "LENGTH^3.000000";
        fundamentalUnitTypesDimensionByUnitTypeDefinitionMap.put(fundamentalUnitTypesDefinition, new EnumMap(UNIT_TYPE.class));
        fundamentalUnitTypesDimension = fundamentalUnitTypesDimensionByUnitTypeDefinitionMap.get(fundamentalUnitTypesDefinition);
        fundamentalUnitTypesDimension.put(UNIT_TYPE.LENGTH, 3.0);

        //
        IFormatter mockFormatter = mock(IFormatter.class);
        when(mockFormatter.format(anyString())).thenAnswer(returnsFirstArg());

        Locale locale = Locale.ENGLISH;
        FundamentalUnitTypesDimensionItemSerializer fundamentalUnitTypesDimensionItemSerializer = new FundamentalUnitTypesDimensionItemSerializer(locale, mockFormatter);
        FundamentalUnitTypesDimensionSerializer fundamentalUnitTypesDimensionSerializer = new FundamentalUnitTypesDimensionSerializer(locale, fundamentalUnitTypesDimensionItemSerializer);
        fundamentalUnitTypesDimensionSerializer.setOperatorDisplayConfiguration(DimensionSerializerBuilder.OPERATOR_DISPLAY_CONFIGURATION.NO_DIVISION);
        fundamentalUnitTypesDimensionSerializer.setIncludeParenthesesInExponentials(false);

        //
        for(Map.Entry<String, Map<UNIT_TYPE, Double>> unitTypesDefinitionEntry:fundamentalUnitTypesDimensionByUnitTypeDefinitionMap.entrySet()){
            String serializedUnitTypeDefinition =
                    fundamentalUnitTypesDimensionSerializer.serialize(unitTypesDefinitionEntry.getValue());

            assertEquals(String.format("Expected parsed unit types definition dimension not correct. Input unit types definition dimension: %s. "
                            + "Expected unit types definition string %s. Constructed unit definition string: %s."
                    , unitTypesDefinitionEntry.getValue().toString(), unitTypesDefinitionEntry.getKey()
                    , serializedUnitTypeDefinition),unitTypesDefinitionEntry.getKey(), serializedUnitTypeDefinition);
        }
    }

    @Test
    public void serializeFundamentalUnitTypesDimensionToString_Should_Produce_Component_Dimension_String_Ordered_By_Exponent_Value() throws SerializingException {
        String unitTypesDefinition;
        Map<UNIT_TYPE,Double> fundamentalUnitTypesDimension;
        Map<String, Map<UNIT_TYPE,Double>> fundamentalUnitTypesDimensionByUnitTypesDefinitionMap = new HashMap<>();

        //
        unitTypesDefinition = "MASS^8.000000* TIME^3.000000* LENGTH^-5.000000";
        fundamentalUnitTypesDimensionByUnitTypesDefinitionMap.put(unitTypesDefinition, new EnumMap(UNIT_TYPE.class));
        fundamentalUnitTypesDimension = fundamentalUnitTypesDimensionByUnitTypesDefinitionMap.get(unitTypesDefinition);
        fundamentalUnitTypesDimension.put(UNIT_TYPE.LENGTH, -5.0);
        fundamentalUnitTypesDimension.put(UNIT_TYPE.TIME, 3.0);
        fundamentalUnitTypesDimension.put(UNIT_TYPE.MASS, 8.0);

        //
        unitTypesDefinition = "LENGTH^-2.000000* TIME^-3.000000* MASS^-4.000000";
        fundamentalUnitTypesDimensionByUnitTypesDefinitionMap.put(unitTypesDefinition, new EnumMap(UNIT_TYPE.class));
        fundamentalUnitTypesDimension = fundamentalUnitTypesDimensionByUnitTypesDefinitionMap.get(unitTypesDefinition);
        fundamentalUnitTypesDimension.put(UNIT_TYPE.TIME, -3.0);
        fundamentalUnitTypesDimension.put(UNIT_TYPE.MASS, -4.0);
        fundamentalUnitTypesDimension.put(UNIT_TYPE.LENGTH, -2.0);

        //
        IFormatter mockFormatter = mock(IFormatter.class);
        when(mockFormatter.format(anyString())).thenAnswer(returnsFirstArg());

        Locale locale = Locale.ENGLISH;
        FundamentalUnitTypesDimensionItemSerializer fundamentalUnitTypesDimensionItemSerializer = new FundamentalUnitTypesDimensionItemSerializer(locale, mockFormatter);
        FundamentalUnitTypesDimensionSerializer fundamentalUnitsTypesDimensionSerializer = new FundamentalUnitTypesDimensionSerializer(locale, fundamentalUnitTypesDimensionItemSerializer);
        fundamentalUnitsTypesDimensionSerializer.setOperatorDisplayConfiguration(DimensionSerializerBuilder.OPERATOR_DISPLAY_CONFIGURATION.NO_DIVISION);
        fundamentalUnitsTypesDimensionSerializer.setOrderDimensionItemsByExponentialValue(true);

        //
        for(Map.Entry<String, Map<UNIT_TYPE, Double>> unitTypeDefinitionEntry:fundamentalUnitTypesDimensionByUnitTypesDefinitionMap.entrySet()){
            String serializedUnitTypesDefinition =
                    fundamentalUnitsTypesDimensionSerializer.serialize(unitTypeDefinitionEntry.getValue());

            assertEquals(String.format("Expected parsed unit type definition dimension not correct. Input unit type definition dimension: %s. "
                            + "Expected unit type definition string %s. Constructed unit type definition string: %s."
                    , unitTypeDefinitionEntry.getValue().toString(), unitTypeDefinitionEntry.getKey()
                    , serializedUnitTypesDefinition),unitTypeDefinitionEntry.getKey(), serializedUnitTypesDefinition);
        }
    }

    @Test
    public void serializeFundamentalUnitsDimensionToString_Should_Produce_Component_Dimension_String_Ordered_With_Negative_Exponents_Replaced_With_Multiple_Division() throws SerializingException {
        String unitTypesDefinition;
        Map<UNIT_TYPE,Double> fundamentalUnitTypesDimension;
        Map<String, Map<UNIT_TYPE,Double>> fundamentalUnitTypesDimensionByUnitTypesDefinitionMap = new HashMap<>();

        //
        unitTypesDefinition = "MASS^8.000000/ TIME^3.000000/ LENGTH^5.000000";
        fundamentalUnitTypesDimensionByUnitTypesDefinitionMap.put(unitTypesDefinition, new HashMap<>());
        fundamentalUnitTypesDimension = fundamentalUnitTypesDimensionByUnitTypesDefinitionMap.get(unitTypesDefinition);
        fundamentalUnitTypesDimension.put(UNIT_TYPE.LENGTH, -5.0);
        fundamentalUnitTypesDimension.put(UNIT_TYPE.TIME, -3.0);
        fundamentalUnitTypesDimension.put(UNIT_TYPE.MASS, 8.0);

        //
        unitTypesDefinition = "1/ LENGTH^2.000000/ TIME^3.000000/ MASS^4.000000";
        fundamentalUnitTypesDimensionByUnitTypesDefinitionMap.put(unitTypesDefinition, new HashMap<>());
        fundamentalUnitTypesDimension = fundamentalUnitTypesDimensionByUnitTypesDefinitionMap.get(unitTypesDefinition);
        fundamentalUnitTypesDimension.put(UNIT_TYPE.TIME, -3.0);
        fundamentalUnitTypesDimension.put(UNIT_TYPE.MASS, -4.0);
        fundamentalUnitTypesDimension.put(UNIT_TYPE.LENGTH, -2.0);

        //
        IFormatter mockFormatter = mock(IFormatter.class);
        when(mockFormatter.format(anyString())).thenAnswer(returnsFirstArg());

        Locale locale = Locale.ENGLISH;
        FundamentalUnitTypesDimensionItemSerializer fundamentalUnitTypesDimensionItemSerializer = new FundamentalUnitTypesDimensionItemSerializer(locale, mockFormatter);
        FundamentalUnitTypesDimensionSerializer fundamentalUnitTypesDimensionSerializer = new FundamentalUnitTypesDimensionSerializer(locale, fundamentalUnitTypesDimensionItemSerializer);
        fundamentalUnitTypesDimensionSerializer.setOperatorDisplayConfiguration(DimensionSerializerBuilder.OPERATOR_DISPLAY_CONFIGURATION.DIVISION_PER_NEGATIVE_EXPONENT);
        fundamentalUnitTypesDimensionSerializer.setOrderDimensionItemsByExponentialValue(true);

        //
        for(Map.Entry<String, Map<UNIT_TYPE, Double>> unitTypesDefinitionEntry:fundamentalUnitTypesDimensionByUnitTypesDefinitionMap.entrySet()){
            String serializedUnitTypesDefinition =
                    fundamentalUnitTypesDimensionSerializer.serialize(unitTypesDefinitionEntry.getValue());

            assertEquals(String.format("Expected parsed unit types definition dimension not correct. Input unit types definition dimension: %s. "
                            + "Expected unit types definition string %s. Constructed unit types definition string: %s."
                    , unitTypesDefinitionEntry.getValue().toString(), unitTypesDefinitionEntry.getKey()
                    , serializedUnitTypesDefinition),unitTypesDefinitionEntry.getKey(), serializedUnitTypesDefinition);
        }
    }

    @Test
    public void serializeFundamentalUnitTypesDimensionToString_Should_Produce_Component_Dimension_String_Ordered_With_One_Division() throws SerializingException {
        String unitTypesDefinition;
        Map<UNIT_TYPE,Double> fundamentalUnitTypesDimension;
        Map<String, Map<UNIT_TYPE,Double>> fundamentalUnitTypesDimensionByUnitTypesDefinitionMap = new HashMap<>();

        //
        unitTypesDefinition = "MASS^8.000000/( TIME^3.000000* LENGTH^5.000000 )";
        fundamentalUnitTypesDimensionByUnitTypesDefinitionMap.put(unitTypesDefinition, new HashMap<>());
        fundamentalUnitTypesDimension = fundamentalUnitTypesDimensionByUnitTypesDefinitionMap.get(unitTypesDefinition);
        fundamentalUnitTypesDimension.put(UNIT_TYPE.LENGTH, -5.0);
        fundamentalUnitTypesDimension.put(UNIT_TYPE.TIME, -3.0);
        fundamentalUnitTypesDimension.put(UNIT_TYPE.MASS, 8.0);

        //
        unitTypesDefinition = "1/( LENGTH^2.000000* TIME^3.000000* MASS^4.000000 )";
        fundamentalUnitTypesDimensionByUnitTypesDefinitionMap.put(unitTypesDefinition, new HashMap<>());
        fundamentalUnitTypesDimension = fundamentalUnitTypesDimensionByUnitTypesDefinitionMap.get(unitTypesDefinition);
        fundamentalUnitTypesDimension.put(UNIT_TYPE.TIME, -3.0);
        fundamentalUnitTypesDimension.put(UNIT_TYPE.MASS, -4.0);
        fundamentalUnitTypesDimension.put(UNIT_TYPE.LENGTH, -2.0);

        //
        IFormatter mockFormatter = mock(IFormatter.class);
        when(mockFormatter.format(anyString())).thenAnswer(returnsFirstArg());

        Locale locale = Locale.ENGLISH;
        FundamentalUnitTypesDimensionItemSerializer componentUnitsDimensionItemSerializer = new FundamentalUnitTypesDimensionItemSerializer(locale, mockFormatter);
        FundamentalUnitTypesDimensionSerializer fundamentalUnitTypesDimensionSerializer = new FundamentalUnitTypesDimensionSerializer(locale, componentUnitsDimensionItemSerializer);
        fundamentalUnitTypesDimensionSerializer.setOperatorDisplayConfiguration(DimensionSerializerBuilder.OPERATOR_DISPLAY_CONFIGURATION.SINGLE_DIVISION);
        fundamentalUnitTypesDimensionSerializer.setOrderDimensionItemsByExponentialValue(true);

        //
        for(Map.Entry<String, Map<UNIT_TYPE, Double>> unitTypeDefinitionEntry:fundamentalUnitTypesDimensionByUnitTypesDefinitionMap.entrySet()){
            String serializedUnitTypesDefinition =
                    fundamentalUnitTypesDimensionSerializer.serialize(unitTypeDefinitionEntry.getValue());

            assertEquals(String.format("Expected parsed unit type definition dimension not correct. Input unit type definition dimension: %s. "
                            + "Expected unit type definition string %s. Constructed unit type definition string: %s."
                    , unitTypeDefinitionEntry.getValue().toString(), unitTypeDefinitionEntry.getKey()
                    , serializedUnitTypesDefinition),unitTypeDefinitionEntry.getKey(), serializedUnitTypesDefinition);
        }
    }

}