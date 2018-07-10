package com.isaacapps.unitconverterapp.processors.formatters.grouping;

import com.isaacapps.unitconverterapp.processors.parsers.measurables.quantity.QuantityGroupingDefiner;

import java.util.Locale;

public class ValuesGroupingFormatter extends GroupingFormatter {
    private Double emptyGroupingDefaultValue;

    ///
    public ValuesGroupingFormatter(Locale locale, QuantityGroupingDefiner quantityGroupingDefiner){
        this(locale, quantityGroupingDefiner, null);
    }
    public ValuesGroupingFormatter(Locale locale, QuantityGroupingDefiner quantityGroupingDefiner, Double emptyGroupingDefaultValue) {
        super(locale, quantityGroupingDefiner);
        this.emptyGroupingDefaultValue = emptyGroupingDefaultValue;
    }

    /**
     * Along with base grouping formatting, empty groupings are replaced with an a grouping containing a default value if such a default value had been defined
     */
    @Override
    public String format(String grouping) {
        String baseFormattedGrouping = super.format(grouping);

        if(emptyGroupingDefaultValue != null){
            return replaceEmptyGroupingsWithDefaultGrouping(baseFormattedGrouping, emptyGroupingDefaultValue.toString());
        }
        else{
            return baseFormattedGrouping;
        }
    }

}
