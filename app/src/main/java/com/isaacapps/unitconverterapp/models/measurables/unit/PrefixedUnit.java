package com.isaacapps.unitconverterapp.models.measurables.unit;

import com.isaacapps.unitconverterapp.processors.parsers.dimension.DimensionComponentDefiner;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.componentnunit.ComponentUnitsDimensionSerializer;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.fundamentalunit.FundamentalUnitTypesDimensionSerializer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

//Encapsulates the prefixed unit creation process
public class PrefixedUnit extends Unit {
    private String prefix;
    private Double prefixValue;

    ///
    public PrefixedUnit(String prefixFullName, String prefixAbbreviation, Double prefixValue, Unit unit, boolean useAbbreviation
            , Locale locale, ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer, FundamentalUnitTypesDimensionSerializer fundamentalUnitTypesDimensionSerializer, DimensionComponentDefiner dimensionComponentDefiner) throws UnitException {
        super(prefixFullName + unit.getName(), new HashSet<String>(), unit.getCategory(), String.format("%s times the size of %s", prefixValue, unit.getName()), unit.getUnitSystem(), prefixAbbreviation + unit.getAbbreviation()
                , new HashMap<String, Double>(), unit, new double[]{prefixValue, 0.0}, locale, componentUnitsDimensionSerializer, fundamentalUnitTypesDimensionSerializer, dimensionComponentDefiner);

        addComponentUnit(useAbbreviation ? unit.getAbbreviation() : unit.getName(), 1.0, false);

        prefix = prefixFullName;
        this.prefixValue = prefixValue;
    }

    ///
    public String getPrefix() {
        return prefix;
    }
    public String getPrefixlessFullName() {
        return getName().replace(prefix, "");
    }
    public Double getPrefixValue(){
        return prefixValue;
    }

    ///
    @Override
    public String toString() {
        return String.format(locale,"Prefix: %s; Prefix Value: %s; Unit: %s", getPrefix(), getPrefixValue(), super.toString());
    }
}
