package com.isaacapps.unitconverterapp.models.measurables.quantity;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.measurables.unit.UnitException;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager;
import com.isaacapps.unitconverterapp.processors.operators.measurables.units.UnitOperators;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class QuantityAttributesValidator {
    private Comparator<Unit> unitsInDecreasingOrderComparator;

    public QuantityAttributesValidator(Comparator<Unit> unitsInDecreasingOrderComparator){
        this.unitsInDecreasingOrderComparator = unitsInDecreasingOrderComparator;
    }

    /**
     * Uses comparator that sorts units from largest to smallest based on base conversion to the same common base unit.
     */
    public QuantityAttributesValidator(){
        this(new Comparator<Unit>() {
            //Sorts units from largest to smallest based on base conversion to the same common base unit.
            @Override
            public int compare(Unit lhsUnit, Unit rhsUnit) {
                try {
                    return -1 * UnitOperators.compareTo(lhsUnit, rhsUnit);
                } catch (UnitException e) {
                    return 0; // ideally this should not be zero, but there are no other signed states besides zero(equal), negative(less), positive(greater)
                }
            }
        });
    }

    /**
     * Makes sure collection of units belong to the same unit manager reference basis and as well as have the same dimension.
     */
    public boolean unitsAreCompatible(Collection<Unit> units) {
        if (units.size() > 1) {
            Unit firstUnit = units.iterator().next();
            UnitManager compatibleUnitManager = firstUnit.getUnitManagerContext();

            for (Unit unit : units) {
                if (unit.getUnitManagerContext() != compatibleUnitManager || !UnitOperators.equalsDimension(unit, firstUnit))
                    return false;
            }
        }
        return true; //vacously true case
    }

    /**
     * Makes sure all elements in unit list have the same dimension and refer to the same unit manager
     * @throws QuantityException
     */
    public List<Unit> validatedUnits(List<Unit> units) throws QuantityException {
        if(!unitsAreCompatible(units))
            throw new QuantityException("Units in list are not compatible", "Make sure all units have the same dimension and unit manager context.");
        return units;
    }

    /**
     *  Makes sure values collection is not empty.
     * @throws QuantityException
     */
    public List<Double> validatedValues(List<Double> values) throws QuantityException {
        if(values.isEmpty())
            throw new QuantityException("Value list is empty", "Make sure value list is not empty");
        return values;
    }

    ///
    /**
     * Makes sure map is not empty and individually validates the unit and values sets
     * @return Sorted map sorted in order of decreasing unit size with respect to base conversion coefficient.
     * @throws QuantityException
     */
    public SortedMap<Unit, Double> validatedSortedUnitValueMap(Map<Unit, Double> unitValueMap) throws QuantityException {
        if(unitValueMap.isEmpty())
            throw new QuantityException("Provided unitValueMap is empty", "Make sure unit values map is non empty with valid units and values");

        SortedMap<Unit, Double> sortedUnitValueMap = new TreeMap<>(unitsInDecreasingOrderComparator);

        for(Map.Entry<Unit, Double> unitValueEntry:unitValueMap.entrySet())
            sortedUnitValueMap.put(unitValueEntry.getKey(), unitValueEntry.getValue());

        return sortedUnitValueMap;
    }

    /**
     * Individually validates the unit and values lists. Assumption is that units and values are paired based on corresponding positions in the two lists.
     * @return Sorted map sorted in order of decreasing unit size with respect to base conversion coefficient.
     * @throws QuantityException
     */
    public SortedMap<Unit, Double> validatedSortedUnitValueMap(List<Unit> units, List<Double> values) throws QuantityException {
        if(units.size() != values.size())
            throw new QuantityException("Size of provided units and values lists are not the same", "Make sure the units and values lists have the same number items and that the items correspond based on position.");

        //Assumption is that the units and values are paired based on corresponding positions in the two lists.
        List<Unit> validatedUnits = validatedUnits(units);
        List<Double> validatedValues = validatedValues(values);

        SortedMap<Unit, Double> sortedUnitValueMap = new TreeMap<>(unitsInDecreasingOrderComparator);

        for (int i = 0; i < validatedUnits.size(); i++)
            sortedUnitValueMap.put(validatedUnits.get(i), validatedValues.get(i));

        return sortedUnitValueMap;
    }

    ///
    public Comparator<Unit> getUnitsInDecreasingOrderComparator() {
        return unitsInDecreasingOrderComparator;
    }
}
