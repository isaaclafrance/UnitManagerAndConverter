package com.isaacapps.unitconverterapp.processors.serializers.dimension.fundamentalunit;

import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel;
import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;
import com.isaacapps.unitconverterapp.processors.serializers.SerializingException;

import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FundamentalUnitTypesDimensionSerializerTest {

    @Test
    public void serializeFundamentalUnitTypesDimensionToString_Should_Provide_Correct_String_From_Fundamental_Unit_Types_Dimension() throws SerializingException{
        String fundamentalUnitDefinition;
        Map<FundamentalUnitsDataModel.UNIT_TYPE,Double> unitComponentDimension;
        Map<String, Map<FundamentalUnitsDataModel.UNIT_TYPE,Double>> unitComponentDimensionByUnitDefinitionMap = new HashMap<>();

        //
        fundamentalUnitDefinition = "LENGTH";
        unitComponentDimensionByUnitDefinitionMap.put(fundamentalUnitDefinition, new LinkedHashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(fundamentalUnitDefinition);
        unitComponentDimension.put(FundamentalUnitsDataModel.UNIT_TYPE.LENGTH, 1.0);

        //
        fundamentalUnitDefinition = "(TIME)^2.000000";
        unitComponentDimensionByUnitDefinitionMap.put(fundamentalUnitDefinition, new LinkedHashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(fundamentalUnitDefinition);
        unitComponentDimension.put(FundamentalUnitsDataModel.UNIT_TYPE.TIME, 2.0);

        //
        fundamentalUnitDefinition = "(TEMPERATURE)^3.000000 * (CHARGE)^-5.000000";
        unitComponentDimensionByUnitDefinitionMap.put(fundamentalUnitDefinition, new LinkedHashMap<>());
        unitComponentDimension = unitComponentDimensionByUnitDefinitionMap.get(fundamentalUnitDefinition);
        unitComponentDimension.put(FundamentalUnitsDataModel.UNIT_TYPE.TEMPERATURE, 3.0);
        unitComponentDimension.put(FundamentalUnitsDataModel.UNIT_TYPE.CHARGE, -5.0);

        //
        IFormatter mockFormatter = mock(IFormatter.class);
        when(mockFormatter.format(anyString())).thenAnswer(returnsFirstArg());

        Locale locale = Locale.ENGLISH;
        FundamentalUnitTypesDimensionItemSerializer fundamentalUnitTypesDimensionItemSerializer = new FundamentalUnitTypesDimensionItemSerializer(locale, mockFormatter);
        FundamentalUnitTypesDimensionSerializer fundamentalUnitTypesDimensionSerializer = new FundamentalUnitTypesDimensionSerializer(locale, fundamentalUnitTypesDimensionItemSerializer);
        fundamentalUnitTypesDimensionSerializer.setIncludeParentheses(true);

        //
        for(Map.Entry<String, Map<FundamentalUnitsDataModel.UNIT_TYPE, Double>> unitDefinitionEntry:unitComponentDimensionByUnitDefinitionMap.entrySet()){
            String constructedUnitDefinition = fundamentalUnitTypesDimensionSerializer.serialize(unitDefinitionEntry.getValue());

            assertEquals(String.format("Expected parsed fundamental unit definition dimension not correct. Input fundamental unit definition dimension: %s. "
                            + "Expected fundamental unit definition string %s. Constructed fundamental unit definition string: %s."
                    , unitDefinitionEntry.getValue().toString(), unitDefinitionEntry.getKey()
                    , constructedUnitDefinition),unitDefinitionEntry.getKey(), constructedUnitDefinition);
        }
    }

}