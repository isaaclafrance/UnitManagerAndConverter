package com.isaacapps.unitconverterapp.models.measurables.unit;

import com.isaacapps.unitconverterapp.processors.serializers.dimension.componentnunit.ComponentUnitsDimensionSerializer;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.fundamentalunit.FundamentalUnitTypesDimensionSerializer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

//Encapsulates the prefixed unit creation process
public class PrefixedUnit extends Unit {
    private String prefix;

    ///
    public PrefixedUnit(String prefixFullName, String prefixAbbreviation, Double prefixValue, Unit unit, boolean useAbbreviation
            ,Locale locale, ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer, FundamentalUnitTypesDimensionSerializer fundamentalUnitTypesDimensionSerializer) {
        super(prefixFullName + unit.getName(), new HashSet<String>(), unit.getCategory(), unit.getDescription(), unit.getUnitSystem(), prefixAbbreviation + unit.getAbbreviation()
                , new HashMap<String, Double>(), unit, new double[]{prefixValue, unit.getBaseConversionPolyCoeffs()[1]}, locale, componentUnitsDimensionSerializer, fundamentalUnitTypesDimensionSerializer);
        addComponentUnit(useAbbreviation ? unit.getAbbreviation() : unit.getName(), 1.0, false);
        prefix = prefixFullName;
    }

    ///
    public String getPrefix() {
        return prefix;
    }

    public String getPrefixlessFullName() {
        return getName().replace(prefix, "");
    }

    ///
    @Override
    public String toString() {
        return String.format("Prefix: %s; PrefixlessFullName: %s", getPrefix(), getPrefixlessFullName());
    }
}
