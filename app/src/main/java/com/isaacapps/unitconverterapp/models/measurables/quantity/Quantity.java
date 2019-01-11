package com.isaacapps.unitconverterapp.models.measurables.quantity;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager;
import com.isaacapps.unitconverterapp.processors.converters.QuantityConverter;
import com.isaacapps.unitconverterapp.processors.formatters.grouping.UnitNamesGroupingFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.grouping.ValuesGroupingFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.numbers.RoundingFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.text.GeneralTextFormatter;
import com.isaacapps.unitconverterapp.processors.parsers.ParsingException;
import com.isaacapps.unitconverterapp.processors.parsers.measurables.quantity.QuantityGroupingDefiner;
import com.isaacapps.unitconverterapp.processors.serializers.ISerializer;
import com.isaacapps.unitconverterapp.processors.serializers.SerializingException;
import com.isaacapps.unitconverterapp.processors.serializers.measurables.quantity.PairedGroupingQuantitySerializer;
import com.isaacapps.unitconverterapp.processors.serializers.measurables.quantity.UnitsGroupingCollectionSerializer;
import com.isaacapps.unitconverterapp.processors.serializers.measurables.quantity.ValuesGroupingCollectionSerializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class Quantity {
    private Locale locale;
    private SortedMap<Unit, Double> unitValueMap;
    private ISerializer<Collection<Unit>> unitsGroupingCollectionSerializer;
    private ISerializer<Collection<Double>> valuesGroupingCollectionSerializer;
    private ISerializer<Quantity> quantitySerializer;
    private QuantityAttributesValidator quantityAttributesValidator;

    ///
    public Quantity(Map<Unit, Double> unitValueMap, Locale locale, ISerializer<Collection<Unit>> unitsGroupingCollectionSerializer
            , ISerializer<Collection<Double>> valuesGroupingCollectionSerializer, ISerializer<Quantity> quantitySerializer, QuantityAttributesValidator quantityAttributesValidator) throws QuantityException {
        this.quantityAttributesValidator = quantityAttributesValidator;
        this.unitValueMap = quantityAttributesValidator.validatedSortedUnitValueMap(unitValueMap);

        this.unitsGroupingCollectionSerializer = unitsGroupingCollectionSerializer;
        this.valuesGroupingCollectionSerializer = valuesGroupingCollectionSerializer;
        this.quantitySerializer = quantitySerializer;

        setLocale(locale);
    }
    public Quantity() throws QuantityException {
        locale = Locale.getDefault();

        quantityAttributesValidator = new QuantityAttributesValidator();

        try {
            unitsGroupingCollectionSerializer = new UnitsGroupingCollectionSerializer(locale, new UnitNamesGroupingFormatter(locale, new QuantityGroupingDefiner()));
            valuesGroupingCollectionSerializer = new ValuesGroupingCollectionSerializer(locale, new ValuesGroupingFormatter(locale, new QuantityGroupingDefiner()));
            quantitySerializer = new PairedGroupingQuantitySerializer(locale, new GeneralTextFormatter(locale), new RoundingFormatter(locale));

            unitValueMap = new TreeMap<>();

            setUnit(new Unit(), true);
        } catch (ParsingException e) {
            e.printStackTrace();
        }
    }
    public Quantity(Map<Unit, Double> unitValueMap) throws QuantityException {
        this();
        this.unitValueMap = quantityAttributesValidator.validatedSortedUnitValueMap(unitValueMap);
    }
    public Quantity(List<Double> values, List<Unit> units) throws QuantityException {
        this();
        setUnitsNValues(units, values);
    }
    public Quantity(double value, Unit unit) throws QuantityException {
        this(Collections.singletonList(value), Collections.singletonList(unit));
    }
    public Quantity(double value) throws QuantityException, ParsingException {
        this(Collections.singletonList(value), Collections.singletonList(new Unit()));
    }
    public Quantity(Unit unit) throws QuantityException {
        this(Collections.singletonList(0.0), Collections.singletonList(unit));
    }

    ///
    public Double getWeightedValueWithRespectToLargestUnit() {
        return QuantityConverter.calculateWeightedValueWithRespectToLargestUnit(unitValueMap);
    }
    public Collection<Double> getValues() {
        return unitValueMap.values();
    }
    public String getValuesString() throws SerializingException {
        return valuesGroupingCollectionSerializer.serialize(unitValueMap.values());
    }
    public void setValues(List<Double> values) throws QuantityException {
        this.unitValueMap = quantityAttributesValidator.validatedSortedUnitValueMap(new ArrayList<>(unitValueMap.keySet()), values);
    }
    public void setAllToValue(Double value) throws QuantityException {
        setValues(Collections.singletonList(value));
    }

    /**
     * Set a value to be associated with a pre-existing unit.
     */
    public void setValue(Unit unit, Double value){
        if(unitValueMap.containsKey(unit))
            unitValueMap.put(unit, value);
    }

    public Unit getLargestUnit() {
        return unitValueMap.firstKey();
    }
    public String getUnitNames() throws SerializingException {
        return unitsGroupingCollectionSerializer.serialize(unitValueMap.keySet());
    }
    public Collection<Unit> getUnits() {
        return unitValueMap.keySet();
    }
    /**
     * Wipes out the existing sorted unit value. Set the specified units with initial default values of zero..
     */
    public void setUnits(List<Unit> units) throws QuantityException {
        setUnits(units, true);
    }

    /**
     *Set the units in the sorted unit-value map. Units will be automatically sorted from largest to least based on base conversion.
     *
     * @param units Compatible units. ie same unit manager reference, same dimension, etc.
     * @param setExistingValuesToDefaults Indicates whether to replace with defaults the existing values that were associated with the previous units.
     * @throws QuantityException
     */
    public void setUnits(List<Unit> units, boolean setExistingValuesToDefaults) throws QuantityException {
        this.unitValueMap = quantityAttributesValidator.validatedSortedUnitValueMap(units
                , setExistingValuesToDefaults ? Collections.nCopies(units.size(), 0.0): new ArrayList<>(unitValueMap.values()));
    }
    public void setUnit(Unit unit, boolean setExistingValuesToDefaults) throws QuantityException {
        setUnits(Collections.singletonList(unit), setExistingValuesToDefaults);
    }

    public SortedMap<Unit, Double> getUnitValueMap() {
        return unitValueMap;
    }
    private void setUnitsNValues(List<Unit> units, List<Double> values) throws QuantityException {
        this.unitValueMap = quantityAttributesValidator.validatedSortedUnitValueMap(units, values);
    }

    ///
    public UnitManager getUnitManagerContext() {
        return !unitValueMap.isEmpty() ? unitValueMap.firstKey().getUnitManagerContext() : null;
    }

    public Locale getLocale() {
        return locale;
    }
    public void setLocale(Locale locale) {
        this.locale = locale;

        unitsGroupingCollectionSerializer.setLocale(locale);
        valuesGroupingCollectionSerializer.setLocale(locale);
        quantitySerializer.setLocale(locale);

        for(Unit unit:unitValueMap.keySet())
            unit.setLocale(locale);
    }

    ///
    @Override
    public String toString() {
        try {
            return quantitySerializer.serialize(this);
        } catch (SerializingException e) {
            return "Unserializable Quantity";
        }
    }
}
