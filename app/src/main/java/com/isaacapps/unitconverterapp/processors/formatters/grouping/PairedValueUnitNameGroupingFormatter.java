package com.isaacapps.unitconverterapp.processors.formatters.grouping;

import com.florianingerl.util.regex.Matcher;
import com.isaacapps.unitconverterapp.processors.parsers.measurables.quantity.QuantityGroupingDefiner;

import java.util.Locale;

public class PairedValueUnitNameGroupingFormatter extends GroupingFormatter{

    double defaultValue;

    public PairedValueUnitNameGroupingFormatter(Locale locale, QuantityGroupingDefiner quantityGroupingDefiner) {
        this(locale, quantityGroupingDefiner, 1);
    }

    public PairedValueUnitNameGroupingFormatter(Locale locale, QuantityGroupingDefiner quantityGroupingDefiner, double defaultValue) {
        super(locale, quantityGroupingDefiner);
        this.defaultValue = defaultValue;
    }

    /**
     * Along with base grouping formatting, find isolated grouping with only units defined and attempts to add a default value.
     * Example transformation: {someUnitA} {someUnitB} { 5.0 someUnitC } --> { [defaultValue] someUnitA} { [defaultValue] someUnitB} {5.0 someUnitC}.
     */
    @Override
    public String format(String pairedGroupings) {
        return super.format(addDefaultValueToIsolatedUnitGroups(pairedGroupings));
    }

    /**
     * Attempts to perform the following transformation: {someUnitA} {someUnitB} { 5.0 someUnitC } --> { [defaultValue] someUnitA} { [defaultValue] someUnitB} {5.0 someUnitC}
     * @param groupingWithIsolatedUnit
     * @return
     */
    private String addDefaultValueToIsolatedUnitGroups(String groupingWithIsolatedUnit){
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
