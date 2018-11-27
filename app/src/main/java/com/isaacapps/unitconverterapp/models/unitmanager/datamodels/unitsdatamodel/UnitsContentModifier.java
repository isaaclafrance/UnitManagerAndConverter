package com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.measurables.unit.UnitException;
import com.isaacapps.unitconverterapp.processors.operators.measurables.units.UnitOperators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import static com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsContentDeterminer.DATA_MODEL_CATEGORY;
import static com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsContentDeterminer.determineDataModelCategoriesGroup;
import static com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsContentDeterminer.determineGeneralDataModelCategory;

public class UnitsContentModifier {
    private Locale locale;
    private UnitsDataModel unitsDataModel;

    ///
    public UnitsContentModifier(){}

    ///Modify Data Model Content
    public void addUnits(Iterable<Unit> units) throws UnitException {
        for (Unit unit : units)
            addUnit(unit);
    }
    public Unit addUnit(Unit unit) throws UnitException {
        if (addUnitToDataStructures(unit, true, true)) {
            if (!unit.isBaseUnit() && !unit.isCoreUnit()) {
                //Make the unit be a base unit if its the only one of its kind in the unit manager or if it is a fundamental unit and not already a core base unit
                boolean isAFundamentalUnit = unitsDataModel.getUnitManagerContext().getFundamentalUnitsDataModel().containsUnitName(unit.getName());
                boolean isOneOfAKind = true;

                //No need to determine one-of-kindness if the unit already fundamental since the logical relationship is a disjunction.
                for (Unit baseUnit : isAFundamentalUnit ? new ArrayList<Unit>(0) : unitsDataModel.getUnitsContentMainRetriever().getBaseUnits()) {
                    if (UnitOperators.equalsDimension(unit, baseUnit)) {
                        isOneOfAKind = false;
                        break;
                    }
                }

                if (isAFundamentalUnit || isOneOfAKind)
                    unit.setBaseUnit(unit);
            }

            /*Attempt to see if this unit can be used to make other unknown units become known only if it has not been incorporated as a base unit already.
             This is because base unit incorporation already checks for that. */
            if (!incorporateBaseUnit(unit, false))
                updateAssociationsOfUnknownUnits();
        }

        return unit;
    }
    private boolean addUnitToDataStructures(Unit unit, boolean includeAliases, boolean includeUpdateToUnitsClassifier){
        Unit unitInDataStructureWithSameName = unitsDataModel.getUnitsContentMainRetriever().getUnit(unit.getName(), false);
        if( unitInDataStructureWithSameName != null && unitInDataStructureWithSameName != unit && unitInDataStructureWithSameName.isCoreUnit() != unit.isCoreUnit()) {
            //If a unit of a different instance exists that does not have the same compatible IsCore Unit state, then prevent replacement.
            return false;
        }

        try {
            unit.setUnitManagerContext(unitsDataModel.getUnitManagerContext());
        }
        catch(UnitException ue1) {
            try {
                unit.setUnitManagerContext(null);
            } catch (UnitException ue2) {}
            return false;
        }

        unitsDataModel.getRepositoryWithDualKeyNCategory().removeItem(unit);// Blast away previous categorizations for unit and start on clean slate so to speak

        Unit addedUnit = null;
        for (DATA_MODEL_CATEGORY dataModelCategory : determineDataModelCategoriesGroup(unit))
            addedUnit = unitsDataModel.getRepositoryWithDualKeyNCategory().addItem(dataModelCategory, unit.getName(), unit.getAbbreviation(), unit);

        boolean unitAbleToBeAdded = addedUnit != null;

        if(!unitAbleToBeAdded) {
            try {
                unit.setUnitManagerContext(null);
            } catch (UnitException ue) { }
            return false;
        }

        if (includeUpdateToUnitsClassifier)
            unitsDataModel.getUnitManagerContext().getUnitsClassifierDataModel().addToHierarchy(addedUnit, false);

        if (includeAliases) {
            for (String alias : unit.getAliases())
                mapUnidirectionalAliasToUnitName(unit.getName(), alias);
        }

        return unitAbleToBeAdded;
    }

    //
    public boolean mapUnidirectionalAliasToUnitName(String unitName, String alias) {
        return unitsDataModel.getRepositoryWithDualKeyNCategory().addUniDirectionalKeyRelation(alias.trim(), unitName.trim());
    }
    public boolean mapBidirectionalAbbreviationToUnitName(String unitName, String abbreviation) {
        return unitsDataModel.getRepositoryWithDualKeyNCategory().updateBidirectionalKeyRelations(unitName.trim(), abbreviation.trim());
    }

    //
    /**
     * Removes unit from the classification hierarchy. Removes unit from units data model.
     * Updates the base units of dependent units to unknown. Also removes a reference to this unit manager from the unit.
     * TODO: Remove reference to conversion favorites when creating a slimmed down version of unit manager in a standalone library.
     */
    public boolean removeUnit(String unitName) throws UnitException {
        Unit removedUnitCandidate = unitsDataModel.getRepositoryWithDualKeyNCategory().getFirstItemByAnyKey(unitName.toLowerCase().trim());
        if (determineGeneralDataModelCategory(removedUnitCandidate) != DATA_MODEL_CATEGORY.CORE) {
            unitsDataModel.getRepositoryWithDualKeyNCategory().removeItemByKey(unitName);

            unitsDataModel.getUnitManagerContext().getUnitsClassifierDataModel().removeFromHierarchy(removedUnitCandidate);

            if (removedUnitCandidate.isBaseUnit()) {
                removedUnitCandidate.downPropogateBaseUnitModification(unitsDataModel.getUnitsContentMainRetriever().getUnknownUnit());
            } else {
                removedUnitCandidate.getBaseUnit().getConversionsToDescendents().remove(removedUnitCandidate);
            }

            removedUnitCandidate.setUnitManagerContext(null);
            updateFundamentalUnitsDimensionOfKnownUnits();

            //Remove any conversion favorites that may be associated with this unit as well updateContent the significance rankings
            unitsDataModel.getUnitManagerContext().getConversionFavoritesDataModel().removeAllConversionsWithUnit(removedUnitCandidate);

            return true;
        }
        return false;
    }
    public boolean removeUnit(Unit unit) throws UnitException {
        return removeUnit(unit.getName());
    }

    /**
     * Also removes all UNKNOWN units since they would have been classified as DYNAMIC if some of their properties were known.
     * TODO: Remove reference to conversion favorites when creating a slimmed down version of unit manager in a standalone library.
     */
    public void removeAllDynamicUnits() {
        for (Unit unitToBeRemoved : unitsDataModel.getRepositoryWithDualKeyNCategory().getItemsByCategory(DATA_MODEL_CATEGORY.DYNAMIC)) {
            unitsDataModel.getUnitManagerContext().getUnitsClassifierDataModel().removeFromHierarchy(unitToBeRemoved);
            unitsDataModel.getUnitManagerContext().getConversionFavoritesDataModel().removeAllConversionsWithUnit(unitToBeRemoved);
        }

        for (Unit unitToBeRemoved : unitsDataModel.getRepositoryWithDualKeyNCategory().getItemsByCategory(DATA_MODEL_CATEGORY.UNKNOWN)) {
            unitsDataModel.getUnitManagerContext().getUnitsClassifierDataModel().removeFromHierarchy(unitToBeRemoved);
            //Unknown units do not have conversions therefore there is no need to remove them from the conversion favorites
        }

        unitsDataModel.getRepositoryWithDualKeyNCategory().removeCategory(DATA_MODEL_CATEGORY.DYNAMIC);
        unitsDataModel.getRepositoryWithDualKeyNCategory().removeCategory(DATA_MODEL_CATEGORY.UNKNOWN);
    }
    public void removeAllUnitNameAliases() {
        Collection<Unit> allUnits = unitsDataModel.getUnitsContentMainRetriever().getAllUnits();
        for (Unit unit : allUnits) {
            unitsDataModel.getRepositoryWithDualKeyNCategory().removeAllKeyRelationsForKeys(unit.getAliases());
        }
    }

    ///Update Existing Data Model Content

    public void updateAssociationsOfUnknownUnits() throws UnitException {
        updateAssociationsOfUnknownUnits(new ArrayList<>(), new ArrayList<>(unitsDataModel.getRepositoryWithDualKeyNCategory().getItemsByCategory(DATA_MODEL_CATEGORY.UNKNOWN)));
    }
    private void updateAssociationsOfUnknownUnits(Collection<Unit> unitsNoLongerUnknown, Collection<Unit> currentUnknownUnits) throws UnitException {
        for (Unit unit : currentUnknownUnits) {
            if (unit != unitsDataModel.getUnitsContentMainRetriever().getUnknownUnit() && unit.setAutomaticBaseUnit()
                    && UnitsContentDeterminer.determineGeneralDataModelCategory(unit) != DATA_MODEL_CATEGORY.UNKNOWN) {
                unitsNoLongerUnknown.add(unit);
                //Reclassify this unit in the data structure with its newly acquired category
                addUnitToDataStructures(unit, false, true);
            }
        }

		/*Recursively accounts for the edge case where identifiability of one unit is dependent on the identifiability of another unit in the same unknown set.
		 Prevents the ordering of the unknown identification from preventing a dependent unknown unit from being identified. */
        Collection<Unit> unknownUnitsToBeReAnalyzed = unitsNoLongerUnknown.isEmpty() || !unitsNoLongerUnknown.containsAll(currentUnknownUnits)
                ? Collections.emptyList() : unknownUnitsToBeReAnalyzed(currentUnknownUnits, unitsNoLongerUnknown);

        if (!unknownUnitsToBeReAnalyzed.isEmpty())
            updateAssociationsOfUnknownUnits(unitsNoLongerUnknown, unknownUnitsToBeReAnalyzed);
    }
    private Collection<Unit> unknownUnitsToBeReAnalyzed(Collection<Unit> currentUnknownUnits, Iterable<Unit> unitsNoLongerUnknown) {
        Set<String> namesOfUnitsNoLongerUnknown = new HashSet<>();
        for (Unit unit : unitsNoLongerUnknown) {
            namesOfUnitsNoLongerUnknown.add(unit.getName());
        }

        //
        for (Iterator<Unit> currentUnknownUnitsIterator = currentUnknownUnits.iterator(); currentUnknownUnitsIterator.hasNext(); ) {
            if (!unknownUnitShouldBeReAnalyzed(currentUnknownUnitsIterator.next(), namesOfUnitsNoLongerUnknown))
                currentUnknownUnitsIterator.remove();
        }

        return currentUnknownUnits;
    }
    private boolean unknownUnitShouldBeReAnalyzed(Unit currentUnknownUnit, Set<String> namesOfUnitsNoLongerUnknown) {
        if (namesOfUnitsNoLongerUnknown.contains(currentUnknownUnit.getBaseUnit().getName()))
            return true;

        for (String componentUnitName : currentUnknownUnit.getComponentUnitsDimension().keySet()) {
            if (namesOfUnitsNoLongerUnknown.contains(componentUnitName))
                return true;
        }
        return false;
    }

    public void updateFundamentalUnitsDimensionOfKnownUnits(){
        for (Unit unit : unitsDataModel.getRepositoryWithDualKeyNCategory().getItemsByCategory(DATA_MODEL_CATEGORY.DYNAMIC)) {
            unit.setAutomaticUnitSystem();
            unit.setAutomaticUnitTypeNFundamentalTypesDimension();
            unit.setAutomaticCategory();

            if (determineGeneralDataModelCategory(unit) == DATA_MODEL_CATEGORY.UNKNOWN)
                addUnitToDataStructures(unit, false, true);//Readd unit so that it is recategorized as unknown in the data model.
        }
    }

    public boolean incorporateBaseUnit(Unit unit, boolean baseUnitStatusChanged) throws UnitException {
        if(baseUnitStatusChanged)
            addUnitToDataStructures(unit, false, false);

        if (!unit.isBaseUnit() || unit.getUnitManagerContext() != unitsDataModel.getUnitManagerContext())
            return false;

        for (Unit existingBaseUnit : unitsDataModel.getUnitsContentMainRetriever().getBaseUnits()) {
            /*If the core units states are compatible (Dynamic units can not replace core units),
             *then set the existing base units and its dependent units to be instead be associated with this base unit.
             *Otherwise, assign the existing base unit to this unit.
             */
            if(!unit.getName().equalsIgnoreCase(existingBaseUnit.getName()) && UnitOperators.equalsDimension(unit, existingBaseUnit)) {

                boolean existingBaseUnitAbleToBeSetAsNonBase = existingBaseUnit.isCoreUnit() == unit.isCoreUnit()
                        && !existingBaseUnit.getName().equalsIgnoreCase(unit.getName());

                if (existingBaseUnitAbleToBeSetAsNonBase) {
                    for (Unit dependentUnit : existingBaseUnit.getConversionsToDescendents().keySet())
                        dependentUnit.setBaseUnit(unit);
                } else {
                    unit.setBaseUnit(existingBaseUnit);
                }
                break; //Ideally if the data model is consistent, there should have only been one valid existing base unit present.
            }
        }

        //Determines if this unit can be associated with existing units that currently do not have base units.
        updateAssociationsOfUnknownUnits();

        return true;
    }

    ///
    void setUnitsDataModel(UnitsDataModel unitsDataModel) {
        this.unitsDataModel = unitsDataModel;
    }
    void setLocale(Locale locale){
        this.locale = locale;
    }
}
