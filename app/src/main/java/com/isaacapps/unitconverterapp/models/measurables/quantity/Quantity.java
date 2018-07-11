package com.isaacapps.unitconverterapp.models.measurables.quantity;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager;
import com.isaacapps.unitconverterapp.processors.converters.QuantityConverter;
import com.isaacapps.unitconverterapp.processors.formatters.grouping.UnitNamesGroupingFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.grouping.ValuesGroupingFormatter;
import com.isaacapps.unitconverterapp.processors.parsers.measurables.quantity.QuantityGroupingDefiner;
import com.isaacapps.unitconverterapp.processors.serializers.ISerializer;
import com.isaacapps.unitconverterapp.processors.serializers.SerializingException;
import com.isaacapps.unitconverterapp.processors.serializers.measurables.quantity.PairedGroupingQuantitySerializer;
import com.isaacapps.unitconverterapp.processors.serializers.measurables.quantity.UnitsGroupingCollectionSerializer;
import com.isaacapps.unitconverterapp.processors.serializers.measurables.quantity.ValuesGroupingCollectionSerializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;

public class Quantity {
    private Locale locale;
    private SortedMap<Unit, Double> unitValueMap;
    private ISerializer<Collection<Unit>> unitsGroupingCollectionSerializer;
    private ISerializer<Collection<Double>> valuesGroupingCollectionSerializer;
    private ISerializer<Quantity> quantitySerializer;
    private QuantityAttributesValidator quantityAttributesValidator;

    ///
    public Quantity(Map<Unit, Double> unitValueMap, Locale locale, ISerializer<Collection<Unit>> unitsGroupingCollectionSerializer, ISerializer<Collection<Double>> valuesGroupingCollectionSerializer, ISerializer<Quantity> quantitySerializer, QuantityAttributesValidator quantityAttributesValidator) throws QuantityException {
        this.quantityAttributesValidator = quantityAttributesValidator;
        this.unitValueMap = quantityAttributesValidator.validatedSortedUnitValueMap(unitValueMap);

        this.unitsGroupingCollectionSerializer = unitsGroupingCollectionSerializer;
        this.valuesGroupingCollectionSerializer = valuesGroupingCollectionSerializer;
        this.quantitySerializer = quantitySerializer;

        setLocale(locale);
    }

    public Quantity(Map<Unit, Double> unitValueMap) throws QuantityException {
        this(unitValueMap, Locale.getDefault()
                , new UnitsGroupingCollectionSerializer(Locale.getDefault(), new UnitNamesGroupingFormatter(Locale.getDefault(), new QuantityGroupingDefiner()))
                , new ValuesGroupingCollectionSerializer(Locale.getDefault(), new ValuesGroupingFormatter(Locale.getDefault(), new QuantityGroupingDefiner()))
                , new PairedGroupingQuantitySerializer(Locale.getDefault(), new UnitNamesGroupingFormatter(Locale.getDefault(), new QuantityGroupingDefiner())
                        , new ValuesGroupingFormatter(Locale.getDefault(), new QuantityGroupingDefiner()))
                , new QuantityAttributesValidator());
    }

    public Quantity() throws QuantityException {
        this(Collections.EMPTY_MAP);
    }

    public Quantity(List<Double> values, List<Unit> units) throws QuantityException {
        this();
        setUnitsNValues(units, values);
    }

    public Quantity(double value, Unit unit) throws QuantityException {
        this(Collections.singletonList(value), Collections.singletonList(unit));
    }

    public Quantity(double value) throws QuantityException {
        this(Collections.singletonList(value), Collections.singletonList(new Unit()));
    }

    public Quantity(Unit unit) throws QuantityException {
        this(Collections.singletonList(0.0), Collections.singletonList(unit));
    }

    ///
    public Double getWeightedValueWithRespectToLargestUnit() {
        return QuantityConverter.calculateReducedValueWithRespectToLargestUnit(unitValueMap);
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
    public void setValue(Double value) throws QuantityException {
        setValues(Collections.singletonList(value));
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
     * Wipes out the existing unit value associated. Set the specified units with values set to 0.
     */
    public void setUnits(List<Unit> units) throws QuantityException {
        this.unitValueMap = quantityAttributesValidator.validatedSortedUnitValueMap(units, Arrays.asList(new Double[units.size()]));
    }
    public void setUnit(Unit unit) throws QuantityException {
        setUnits(Collections.singletonList(unit));
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
