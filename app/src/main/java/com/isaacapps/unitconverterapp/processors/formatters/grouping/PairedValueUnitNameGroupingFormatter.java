package com.isaacapps.unitconverterapp.processors.formatters.grouping;

import com.isaacapps.unitconverterapp.processors.parsers.measurables.quantity.QuantityGroupingDefiner;

import java.util.Locale;
import java.util.regex.Matcher;

public class PairedValueUnitNameGroupingFormatter extends GroupingFormatter{

    double defaultValue;

    public PairedValueUnitNameGroupingFormatter(Locale locale, QuantityGroupingDefiner quantityGroupingDefiner) {
        this(locale, quantityGroupingDefiner, 1);
    }

    public PairedValueUnitNameGroupingFormatter(Locale locale, QuantityGroupingDefiner quantityGroupingDefiner, double defaultValue) {
        super(locale, quantityGroupingDefiner);
        this.defaultValue = defaultValue;
    }

    @Override
    public String format(String pairedGroupings) {
        return super.format(addDefaultValueToIsolatedUnitGroups(pairedGroupings));
    }

    /**
     * Attempts to perform the following transformation: {someUnitA} {someUnitB} --> { [defaultValue] someUnitA} { [defaultValue] someUnitB}
     * @param groupingWithIsolatedUnit
     * @return
     */
    public String addDefaultValueToIsolatedUnitGroups(String groupingWithIsolatedUnit){
        Matcher isolatedUnitGroupMatcher = quantityGroupingDefiner.getSingleUnitGroupingPattern().matcher(groupingWithIsolatedUnit);
        String groupingWithIsolatedUnitsHavingDefaultValue = groupingWithIsolatedUnit;

        while(isolatedUnitGroupMatcher.find()){
            String isolatedUnitGroup = isolatedUnitGroupMatcher.group();
            String isolatedUnitGroupHavingDefaultValue = isolatedUnitGroup.replace(quantityGroupingDefiner.getGroupOpeningSymbol()
                    , quantityGroupingDefiner.getGroupOpeningSymbol()+defaultValue+" ");

            groupingWithIsolatedUnitsHavingDefaultValue = groupingWithIsolatedUnit.replaceAll(isolatedUnitGroup
                    , isolatedUnitGroupHavingDefaultValue);
        }

        return groupingWithIsolatedUnitsHavingDefaultValue;
    }
}
