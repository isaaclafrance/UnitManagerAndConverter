package com.isaacapps.unitconverterapp.processors.parsers.measurables.quantity;

import com.isaacapps.unitconverterapp.models.measurables.quantity.Quantity;
import com.isaacapps.unitconverterapp.models.measurables.quantity.QuantityException;
import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.processors.parsers.measurables.quantity.tokeniser.PairedGroupingQuantityTokenizer;
import com.isaacapps.unitconverterapp.processors.parsers.measurables.quantity.tokeniser.SerialGroupingQuantityTokenizer;

import java.util.List;
import java.util.Locale;

/**
 * Parses text for quantity related token ie number groupings, unit groupings.
 */
public class QuantityParser{
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
     * Parser texts with quantity groupings into Quantity objects. A quantity grouping can be formated in two ways.
     * One way to format is in pairs "{1 meter} {1 inch}" or "{meter} {inch}" (one is assumed as value).
     * Another way to format is serially "{1}{1}{meter}{inches}".
     * The text can not have a mix of quantity grouping format types, otherwise an invalid quanitity will be returned.
     */
    public Quantity parse(String quantityGroupings) throws QuantityException {
        boolean hasValueUnitPairGrouping = quantityGroupingDefiner.hasValueUnitPairGrouping(quantityGroupings);
        boolean hasValuesGrouping = quantityGroupingDefiner.hasValuesGrouping(quantityGroupings);

        //There can not be a mix of grouping format types
        if (quantityGroupingDefiner.hasValueUnitPairGrouping(quantityGroupings) && quantityGroupingDefiner.hasValuesGrouping(quantityGroupings))
            return new Quantity();

        if (hasValueUnitPairGrouping) {
            return new Quantity(pairedGroupingQuantityTokenizer.convertToQuantityUnitValueMap(pairedGroupingQuantityTokenizer.parsePairedValueUnitNameGroupingsToNameValueMap(quantityGroupings)));
        }

        if (hasValuesGrouping) {
            List<Double> valuesList = serialGroupingQuantityTokenizer.parseSerialGroupingToValuesList(quantityGroupings);
            List<Unit> unitsList = serialGroupingQuantityTokenizer.parseUnitNamesToUnitsList(serialGroupingQuantityTokenizer.parseSerialGroupingToUnitsNamesList(quantityGroupings));

            return new Quantity(valuesList, unitsList);
        }

        return new Quantity();
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
