package com.isaacapps.unitconverterapp.processors.operators.dimensions;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.isaacapps.unitconverterapp.processors.operators.dimensions.DimensionOperators.*;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DimensionOperatorsTest {

    ///
    @Test
    public void alterExponentOfDimensionItem_Should_Increment_Exponent_Of_Specified_Dimensional_Item_By_Delta(){
        Map<String, Double> templateDimensionMap = new HashMap<>();
        templateDimensionMap.put("item1", 1.0);

        Map<String, Double> testableDimensionMap;
        Double delta;

        //
        delta = 10.0;
        testableDimensionMap = new HashMap<>(templateDimensionMap);
        DimensionOperators.alterExponentOfDimensionItem(testableDimensionMap, "item1", delta);

        assertThat("Dimension item1 is not altered by specified delta", testableDimensionMap.get("item1"), is(templateDimensionMap.get("item1")+delta));

        //
        delta = -10.0;
        testableDimensionMap = new HashMap<>(templateDimensionMap);
        DimensionOperators.alterExponentOfDimensionItem(testableDimensionMap, "item1", delta);

        assertThat("Dimension item1 is not altered by specified delta", testableDimensionMap.get("item1"), is(templateDimensionMap.get("item1")+delta));

        //Add dimension item to be altered if missing
        delta = 5.0;
        testableDimensionMap = new HashMap<>(templateDimensionMap);
        DimensionOperators.alterExponentOfDimensionItem(testableDimensionMap, "item2", delta);

        assertThat("Dimension item1 should not be altered by specified delta", testableDimensionMap.get("item1"), is(templateDimensionMap.get("item1")));
        assertThat("Dimension item2 should be altered by specified delta", testableDimensionMap.get("item2"), is(delta));

    }

    ///
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
                    , firstDimensionMap.toString()
                    , secondDimensionMap.toString())
                    , equalsDimension(firstDimensionMap, dimensionMap, 0.00001));
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
                    , firstDimensionMap.toString()
                    , secondDimensionMap.toString())
                    , equalsDimension(firstDimensionMap
                            , dimensionMap, 0.00001));
        }
    }

}