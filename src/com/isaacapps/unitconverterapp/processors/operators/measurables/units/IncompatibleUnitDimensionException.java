package com.isaacapps.unitconverterapp.processors.operators.measurables.units;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.processors.formatters.text.GeneralTextFormatter;
import com.isaacapps.unitconverterapp.processors.serializers.ISerializer;
import com.isaacapps.unitconverterapp.processors.serializers.SerializingException;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.componentnunit.ComponentUnitsDimensionItemSerializer;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.componentnunit.ComponentUnitsDimensionSerializer;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.fundamentalunit.FundamentalUnitTypesDimensionItemSerializer;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.fundamentalunit.FundamentalUnitTypesDimensionSerializer;

import java.util.Locale;

public class IncompatibleUnitDimensionException extends Exception {
    private final Unit firstUnit, secondUnit;
    private ISerializer componentDimensionSerializer;
    private ISerializer fundamentalUnitTypesDimensionSerializer;

    public IncompatibleUnitDimensionException(Unit firstUnit, Unit secondUnit, Locale locale) {
        this.firstUnit = firstUnit;
        this.secondUnit = secondUnit;
        componentDimensionSerializer = new ComponentUnitsDimensionSerializer(locale, new ComponentUnitsDimensionItemSerializer(locale, new GeneralTextFormatter(locale)));
        fundamentalUnitTypesDimensionSerializer = new FundamentalUnitTypesDimensionSerializer(locale, new FundamentalUnitTypesDimensionItemSerializer(locale, new GeneralTextFormatter(locale)));
    }
    public IncompatibleUnitDimensionException(Unit firstUnit, Unit secondUnit){
        this(firstUnit, secondUnit, firstUnit.getLocale());
    }

    public String getFirstComponentDimension() throws SerializingException {
        try {
            return componentDimensionSerializer.serialize(firstUnit.getComponentUnitsDimension());
        } catch (SerializingException e) {
            throw new SerializingException(String.format("%s :: %s", "First Component dimension could be converted to string.", e.getScenario()), e.getHowToFix());
        }
    }

    public String getSecondComponentDimension() throws SerializingException {
        try {
            return componentDimensionSerializer.serialize(secondUnit.getComponentUnitsDimension());
        } catch (SerializingException e) {
            throw new SerializingException(String.format("%s :: %s", "Second Component dimension could be converted to string.", e.getScenario()), e.getHowToFix());
        }
    }

    public String getFirstFundamentalDimension() throws SerializingException {
        try {
            return fundamentalUnitTypesDimensionSerializer.serialize(firstUnit.getFundamentalUnitTypesDimension());
        } catch (SerializingException e) {
            throw new SerializingException(String.format("%s :: %s", "First Fundamental dimension could be converted to string.", e.getScenario()), e.getHowToFix());
        }
    }

    public String getSecondFundamentalDimension() throws SerializingException {
        try {
            return fundamentalUnitTypesDimensionSerializer.serialize(secondUnit.getFundamentalUnitTypesDimension());
        } catch (SerializingException e) {
            throw new SerializingException(String.format("%s :: %s", "Second Fundamental dimension could be converted to string.", e.getScenario()), e.getHowToFix());
        }
    }

    @Override
    public String toString() {
        try {
            return String.format("**The following dimensions are incompatible: \n **First Component Dimension: %s \n **First Fundamental Dimension: %s \n **Second Component Dimension: %s \n **Second Fundamental Dimension: %s "
                    , getFirstComponentDimension(), getFirstFundamentalDimension(), getSecondComponentDimension(), getSecondFundamentalDimension());
        } catch (SerializingException e) {
            return "Dimensions are Incompatible.";
        }
    }
}
