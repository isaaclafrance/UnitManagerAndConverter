package com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.processors.operators.measurables.units.UnitOperators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import static com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.ContentDeterminer.DATA_MODEL_CATEGORY;
import static com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.ContentDeterminer.determineDataModelCategoryGroup;
import static com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.ContentDeterminer.determineGeneralDataModelCategory;

public class ContentModifier {
    private Locale locale;
    private UnitsDataModel unitsDataModel;

    ///
    public ContentModifier(){}

    ///Modify Data Model Content
    public void addUnits(Iterable<Unit> units) {
        for (Unit unit : units)
            addUnit(unit);
    }

    public Unit addUnit(Unit unit) {
        /*Unit needs to be put in data structure first (if it does not violate the data structure restrictions)
         *even if its unknown because referential execution paths in the setUnitManagerContext injector will require
         *the unit to be present in an unknown classification, even if later on it may be reclassified to something else.
         *Fortunately adding it before hand in the data structure is not too resource intensive.
         */
        if (!unitsDataModel.getRepositoryWithDualKeyNCategory().containsItem(unit) && addUnitToDataModels(unit, false, false)) {

            unit.setUnitManagerContext(unitsDataModel.getUnitManagerContext());

			/*Now assess the characteristics(type, dimension, etc) of the unit after attempting to associate it with unit manager.
			  If not unknown, then fully incorporate it (if it does not violate data structure restrictions) with data models again. */
            if (determineGeneralDataModelCategory(unit) != DATA_MODEL_CATEGORY.UNKNOWN && addUnitToDataModels(unit, true, true)) {
                if (!unit.isBaseUnit() && !unit.isCoreUnit()) {
                    //Make the unit be a base unit if its the only one of its kind in the unit manager or if it is a fundamental unit and not already a core base unit
                    boolean isAFundamentalUnit = unitsDataModel.getUnitManagerContext().getFundamentalUnitsDataModel().containsUnitName(unit.getName());
                    boolean isOneOfAKind = true;

                    //No need to determine one-of-kindness if the unit already fundamental since the logical relationship is a disjunction.
                    for (Unit baseUnit : isAFundamentalUnit ? new ArrayList<Unit>(0) : unitsDataModel.getContentMainRetriever().getBaseUnits()) {
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
                if (!incorporateBaseUnit(unit))
                    updateAssociationsOfUnknownUnits();
            }
        }

        return unit;
    }

    private boolean addUnitToDataModels(Unit unit, boolean includeAliases, boolean includeUpdateToUnitsClassifier) {
        Unit addedUnit = null;
        for (DATA_MODEL_CATEGORY dataModelCategory : determineDataModelCategoryGroup(unit))
            addedUnit = unitsDataModel.getRepositoryWithDualKeyNCategory().addItem(dataModelCategory, unit.getName(), unit.getAbbreviation(), unit);

        boolean unitAbleToBeAdded = addedUnit != null;

        if (unitAbleToBeAdded && includeUpdateToUnitsClassifier)
            unitsDataModel.getUnitManagerContext().getUnitsClassifierDataModel().addToHierarchy(addedUnit, false);

        if (unitAbleToBeAdded && includeAliases) {
            for (String alias : unit.getAliases())
                mapUnidirectionalAliasToUnitName(unit.getName(), alias);
        }

        if(unitAbleToBeAdded)
            addedUnit.setLocale(locale);

        return unitAbleToBeAdded;
    }

    public boolean mapUnidirectionalAliasToUnitName(String unitName, String alias) {
        return unitsDataModel.getRepositoryWithDualKeyNCategory().addUniDirectionalKeyRelation(alias.trim(), unitName.trim());
    }

    public boolean mapBidirectionalAbbreviationToUnitName(String unitName, String abbreviation) {
        return unitsDataModel.getRepositoryWithDualKeyNCategory().updateBidirectionalKeyRelations(unitName.trim(), abbreviation.trim());
    }

    /**
     * Removes unit from the classification hierarchy. Removes unit from units data model.
     * Updates the base units of dependent units to unknown. Also removes a reference to this unit manager from the unit.
     * TODO: Remove reference to conversion favorites when creating a slimmed down version of unit manager.
     */
    public boolean removeUnit(String unitName) {
        Unit removedUnitCandidate = unitsDataModel.getRepositoryWithDualKeyNCategory().getFirstItemByAnyKey(unitName.toLowerCase().trim());
        if (determineGeneralDataModelCategory(removedUnitCandidate) != DATA_MODEL_CATEGORY.CORE) {
            unitsDataModel.getRepositoryWithDualKeyNCategory().removeItemByKey(unitName);

            unitsDataModel.getUnitManagerContext().getUnitsClassifierDataModel().removeFromHierarchy(removedUnitCandidate);

            if (removedUnitCandidate.isBaseUnit()) {
                for (Unit dependentUnit : removedUnitCandidate.getConversionsToDescendents().keySet()) {
                    dependentUnit.setBaseUnit(unitsDataModel.getContentMainRetriever().getUnknownUnit());
                }
                removedUnitCandidate.clearConversionsOfDescendents();
            } else {
                removedUnitCandidate.getBaseUnit().getConversionsToDescendents().remove(removedUnitCandidate);
            }

            removedUnitCandidate.setUnitManagerContext(null);
            updateFundamentalUnitsDimensionOfKnownUnits();

            //Remove any conversion favorites that may be associated with this unit as well update the significance rankings
            unitsDataModel.getUnitManagerContext().getConversionFavoritesDataModel().removeAllConversionsWithUnit(removedUnitCandidate);

            return true;
        }
        return false;
    }

    public boolean removeUnit(Unit unit) {
        return removeUnit(unit.getName());
    }

    /**
     * Also removes all UNKNOWN units since they would have been classified as DYNAMIC if some of their properties were known.
     * TODO: Remove reference to conversion favorites when creating a slimmed down version of unit manager.
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
        Collection<Unit> allUnits = unitsDataModel.getContentMainRetriever().getAllUnits();
        for (Unit unit : allUnits) {
            unitsDataModel.getRepositoryWithDualKeyNCategory().removeAllKeyRelationsForKeys(unit.getAliases());
        }
    }

    ///Update Existing Data Model Content

    public void updateAssociationsOfUnknownUnits() {
        updateAssociationsOfUnknownUnits(new ArrayList<>(), unitsDataModel.getRepositoryWithDualKeyNCategory().getItemsByCategory(DATA_MODEL_CATEGORY.UNKNOWN));
    }

    private void updateAssociationsOfUnknownUnits(Collection<Unit> unitsNoLongerUnknown, Collection<Unit> currentUnknownUnits) {
        for (Unit unit : currentUnknownUnits) {
            if (unit != unitsDataModel.getContentMainRetriever().getUnknownUnit() && unit.setAutomaticBaseUnit()) {
                unitsNoLongerUnknown.add(unit);
                //Reclassify this unit in the data structure with its newly acquired category
                addUnitToDataModels(unit, false, true);
            }
        }

		/*Recursively accounts for the edge case where identifiability of one unit is dependent on the identifiability of another unit in the same unknown set.
		 Prevents the ordering of the unknown identification from preventing a dependent unknown unit from being identified. */
        Collection<Unit> unknownUnitsToBeReAnalyzed = unitsNoLongerUnknown.isEmpty() || !unitsNoLongerUnknown.containsAll(currentUnknownUnits)
                ? Collections.EMPTY_LIST : unknownUnitsToBeReAnalyzed(currentUnknownUnits, unitsNoLongerUnknown);
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

    public void updateFundamentalUnitsDimensionOfKnownUnits() {
        for (Unit unit : unitsDataModel.getRepositoryWithDualKeyNCategory().getItemsByCategory(DATA_MODEL_CATEGORY.DYNAMIC)) {
            unit.setAutomaticUnitSystem();
            unit.setAutomaticUnitTypeNFundmtTypesDim();
            unit.setAutomaticCategory();

            if (determineGeneralDataModelCategory(unit) == DATA_MODEL_CATEGORY.UNKNOWN)
                addUnitToDataModels(unit, false, true);//Readd unit so that it is recategorized as unknown in the data model.
        }
    }

    public boolean incorporateBaseUnit(Unit unit) {
        if (!unit.isBaseUnit() || unit.getUnitManagerContext() != unitsDataModel.getUnitManagerContext())
            return false;

        for (Unit existingBaseUnit : unitsDataModel.getContentMainRetriever().getBaseUnits()) {
            /*If the core units states are compatible (Dynamic units can not replace core units),
             *then set the existing base units and its dependent units to be instead be associated with this base unit.
             *Otherwise, assign the existing base unit to this unit.
             */
            if(UnitOperators.equalsDimension(unit, existingBaseUnit)) {
                boolean existingBaseUnitAbleToBeSetAsNonBase = existingBaseUnit.isCoreUnit() == unit.isCoreUnit()
                        && !existingBaseUnit.getName().equalsIgnoreCase(unit.getName());

                if (existingBaseUnitAbleToBeSetAsNonBase) {
                    /*For functional identity purposes, the base units include themselves as their dependents.
                     *As consequence, inorder to prevent concurrent modification exceptions
                     *the 'conversionsToDescendents' need to be reinitialized in a new list
                     *since it can be cleared in setBaseUnit when the base unit switches to no longer being a base unit.
                     */
                    for (Unit dependentUnit : new ArrayList<>(existingBaseUnit.getConversionsToDescendents().keySet()))
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
