package com.isaacapps.unitconverterapp.processors.parsers.measurables.quantity;

import com.isaacapps.unitconverterapp.models.measurables.quantity.Quantity;
import com.isaacapps.unitconverterapp.models.measurables.quantity.QuantityException;
import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.processors.parsers.IParser;
import com.isaacapps.unitconverterapp.processors.parsers.measurables.quantity.tokeniser.PairedGroupingQuantityTokenizer;
import com.isaacapps.unitconverterapp.processors.parsers.measurables.quantity.tokeniser.SerialGroupingQuantityTokenizer;

import java.util.List;
import java.util.Locale;

/**
 * Parses text for quantity related tokens ie number groupings, unit groupings, the constructs a multiunit quantity from those tokens.
 */
public class QuantityParser implements IParser<Quantity>{
    private Locale locale;
    private PairedGroupingQuantityTokenizer pairedGroupingQuantityTokenizer;
    private SerialGroupingQuantityTokenizer serialGroupingQuantityTokenizer;
    private QuantityGroupingDefiner quantityGroupingDefiner;

    public QuantityParser(Locale locale, QuantityGroupingDefiner quantityGroupingDefiner, PairedGroupingQuantityTokenizer pairedGroupingQuantityTokenizer, SerialGroupingQuantityTokenizer serialGroupingQuantityTokenizer ) {
        this.quantityGroupingDefiner = quantityGroupingDefiner;
        this.pairedGroupingQuantityTokenizer = pairedGroupingQuantityTokenizer;
        this.serialGroupingQuantityTokenizer = serialGroupingQuantityTokenizer;

        setLocale(locale);
    }

    ///

    /**
     * Parses text with quantity groupings into Quantity objects. A quantity grouping can be formatted in two mutually exclusive ways.
     * One way to format is in pairs "{1 meter} {1 inch}" or "{meter} {inch}" where (one is assumed as value).
     * Another way to format is serially "{1}{1}{meter}{inches}".
     * The provided text can not have a mix of serial and paired quantity grouping format types, otherwise an invalid quantity will be returned.
     */
    @Override
    public Quantity parse(String quantityGroupingsText){
        boolean hasValueUnitPairGrouping = quantityGroupingDefiner.hasValueUnitPairGrouping(quantityGroupingsText);
        boolean hasValuesGrouping = quantityGroupingDefiner.hasValuesGrouping(quantityGroupingsText);

        try {
            //There can not be a mix of grouping format types
            if (quantityGroupingDefiner.hasValueUnitPairGrouping(quantityGroupingsText) && quantityGroupingDefiner.hasValuesGrouping(quantityGroupingsText))
                return new Quantity();

            if (hasValueUnitPairGrouping) {
                return new Quantity(pairedGroupingQuantityTokenizer.convertToQuantityUnitValueMap(pairedGroupingQuantityTokenizer.parsePairedValueUnitNameGroupingsToNameValueMap(quantityGroupingsText)));
            }

            if (hasValuesGrouping) {
                List<Double> valuesList = serialGroupingQuantityTokenizer.parseSerialGroupingToValuesList(quantityGroupingsText);
                List<Unit> unitsList = serialGroupingQuantityTokenizer.parseSerialGroupingToUnitsList(quantityGroupingsText);

                return new Quantity(valuesList, unitsList);
            }

            return new Quantity();
        }
        catch(QuantityException e){
            return null;
        }
    }

    ///
    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
        serialGroupingQuantityTokenizer.setLocale(locale);
    }

}
