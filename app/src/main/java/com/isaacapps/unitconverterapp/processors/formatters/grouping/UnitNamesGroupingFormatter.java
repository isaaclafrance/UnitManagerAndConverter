package com.isaacapps.unitconverterapp.processors.formatters.grouping;

import com.isaacapps.unitconverterapp.processors.parsers.measurables.quantity.QuantityGroupingDefiner;

import java.util.Locale;

public class UnitNamesGroupingFormatter extends GroupingFormatter {

    public UnitNamesGroupingFormatter(Locale locale, QuantityGroupingDefiner quantityGroupingDefiner) {
        super(locale, quantityGroupingDefiner);
    }

    /**
     * Along with base grouping formatting, empty groupings are removed.
     */
    @Override
    public String format(String grouping) {
        return super.format(grouping).replaceAll(quantityGroupingDefiner.getEmptyGroupingPattern().pattern(), "" );
    }
}
