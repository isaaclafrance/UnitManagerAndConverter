package com.isaacapps.unitconverterapp.models.unitmanager.datamodels;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.IDualKeyNCategoryRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Classification Map of Unit Groups Based on Their Unit Systems and Categories.
 * In this modified implementation of abstract data model, Unit System specifies the category, Unit Name specifies key1 and the Unit Category specifies key2.
 * And the item is the units group
 *
 * @author Isaac Lafrance
 */
public class UnitsClassifierDataModel extends BaseDataModel<String, Collection<String>, String> {

    ///
    public UnitsClassifierDataModel() {
    }
    public UnitsClassifierDataModel(IDualKeyNCategoryRepository<String, Collection<String>, String> repositoryWithDualKeyNCategory){
        this.repositoryWithDualKeyNCategory = repositoryWithDualKeyNCategory;
    }

    ///
    public Collection<String> addToHierarchy(Unit unit, boolean removeFromOtherUnitNamesGroups) {
        Collection<String> currentUnitNamesGroup = repositoryWithDualKeyNCategory.getItem(unit.getUnitSystem(), unit.getCategory());

        if (currentUnitNamesGroup != null) {
            currentUnitNamesGroup.add(unit.getName());

        } else {
            //Save as a Set collection  in order to ignore duplicate names in same unit group
            currentUnitNamesGroup = repositoryWithDualKeyNCategory.addItem(unit.getUnitSystem(), unit.getName(), unit.getCategory(), new HashSet<>(Collections.singletonList(unit.getName())));
        }

        if (removeFromOtherUnitNamesGroups) {
            //Exclude the current units group.
            Collection<Collection<String>> otherUnitNamesGroups = repositoryWithDualKeyNCategory.getItemsByAnyKey(unit.getName());
            otherUnitNamesGroups.remove(currentUnitNamesGroup);

            //Remove unit name from the other units groups
            for (Collection<String> unitNamesGroup : otherUnitNamesGroups)
                unitNamesGroup.remove(unit.getName());

            //Removed the other unit groups if they are empty
            for (Collection<String> unitGroupWithRemovedUnitName : otherUnitNamesGroups) {
                if (unitGroupWithRemovedUnitName.isEmpty())
                    repositoryWithDualKeyNCategory.removeItem(unitGroupWithRemovedUnitName);
            }
        }

        return currentUnitNamesGroup;
    }

    ///
    public boolean removeFromHierarchy(Unit unit) {
        Collection<String> unitsGroup = repositoryWithDualKeyNCategory.getItem(unit.getUnitSystem(), unit.getCategory());

        repositoryWithDualKeyNCategory.removeAllKeyRelationsForKey(unit.getName()); //Ideally this should not be done by descendents.

        if (unitsGroup != null && unitsGroup.remove(unit.getName()) && unitsGroup.isEmpty()) {
            repositoryWithDualKeyNCategory.removeItemFromCategoryByKey(unit.getUnitSystem(), unit.getCategory());

            if (getUnitCategoriesInUnitSystem(unit.getUnitSystem()).isEmpty())
                repositoryWithDualKeyNCategory.removeCategory(unit.getUnitSystem());

            return true;
        }
        return false;
    }

    public void clearHierarchy() {
        repositoryWithDualKeyNCategory.removeAllItems();
    }

    ///
    public Collection<String> getUnitNamesByUnitSystem(String unitSystem) {
        Collection<String> allUnitNames = new HashSet<>();
        unitSystem = unitSystem.toLowerCase();

        for (String unitCategory : getUnitCategoriesInUnitSystem(unitSystem))
            allUnitNames.addAll(getUnitNamesByUnitSystemNCategory(unitSystem, unitCategory));

        return allUnitNames;
    }

    public Collection<String> getUnitNamesByUnitSystemNCategory(String unitSystem, String unitCategory) {
        unitSystem = unitSystem.toLowerCase();
        unitCategory = unitCategory.toLowerCase();
        if (!containsUnitCategoryInUnitSystem(unitSystem, unitCategory, false))
            return new HashSet<>();

        return repositoryWithDualKeyNCategory.getItem(unitSystem, unitCategory);
    }

    public Collection<String> getAllUnitSystems() {
        return repositoryWithDualKeyNCategory.getAllAssignedCategories();
    }

    public Collection<String> getAllUnitCategories() {
        return repositoryWithDualKeyNCategory.getAllKey2s();
    }

    public Collection<String> getUnitCategoriesInUnitSystem(String unitSystem) {
        return repositoryWithDualKeyNCategory.getKey2sByCategory(unitSystem.toLowerCase());
    }

    /**
     * Uses the provided unit name or category to find groups of units that are similar in terms of category across multiple unit systems.
     * Since the data structure enforces a bijective relation between unit name and category, the unit name can also be used to return the name of units that share the same category.
     *
     * @param unitNameOrCategory Name of an existing unit or existing category in the data structure.
     * @return If the unit name or category are found in multiple unit systems then multiple groups of unit names will be returned for each unit ssystem.
     */
    public Collection<Collection<String>> getUnitGroupsWithSameCategory(String unitNameOrCategory) {
        return repositoryWithDualKeyNCategory.getItemsByAnyKey(unitNameOrCategory.toLowerCase());
    }

    /**
     * Uses the provided unit name or category to find a group of units that are similar in terms of category for a particular unit system.
     * Since the data structure enforces a bijective relation between unit name and category, the unit name can also be used to return the name of other units that share the same category.
     *
     * @param unitNameOrCategory Name of an existing unit or existing category in the data structure.
     * @param unitSystem Unit system that units in group must share.
     * @return Collection of unit names.
     */
    public Collection<String> getUnitGroupsWithSameCategory(String unitNameOrCategory, String unitSystem) {
        unitNameOrCategory = unitNameOrCategory.toLowerCase();
        unitSystem = unitSystem.toLowerCase();
        return repositoryWithDualKeyNCategory.getItem(unitSystem, unitNameOrCategory);
    }

    ///

    /**
     * Indicates whether or not the data structure contains specified unit system
     * @param fuzzyMatch Match such that specified unitSystem is at least a token in any existing unit systems.
     *                   Helps to find a match in situations where existing units systems are a combination fo multiple units, ie "SI and Metric"
     */
    public boolean containUnitSystem(String unitSystem, boolean fuzzyMatch) {
        if(unitSystem.isEmpty())
            return false;

        unitSystem = unitSystem.toLowerCase();
        if (fuzzyMatch) {
            for (String unitSystemCandidate : getAllUnitSystems()) {
                if (unitSystemCandidate.contains(unitSystem))
                    return true;
            }
            return false;
        }
        return repositoryWithDualKeyNCategory.containsCategory(unitSystem);
    }

    /**
     * Indicates whether or not the specified unit category can be found in the specified unit system.
     */
    public boolean containsUnitCategoryInUnitSystem(String unitSystem, String unitCategory, boolean fuzzyMatch) {
        if(unitSystem.isEmpty() && unitCategory.isEmpty())
            return false;

        unitSystem = unitSystem.toLowerCase();
        unitCategory = unitCategory.toLowerCase();
        if (fuzzyMatch) { // Fuzzy match such that the specified unitName is at least a token in any unit name in a unit group
            for (String unitCategoryCandidate : getUnitCategoriesInUnitSystem(unitSystem)) {
                if (unitCategoryCandidate.contains(unitCategory.toLowerCase()))
                    return true;
            }
            return false;
        }
        return repositoryWithDualKeyNCategory.containsKeyInCategory(unitSystem, unitCategory);
    }

    /**
     * Indicates whether or not the specified unit can be found in the specified unit category and unit system.
     * @param fuzzyMatch Match such that specified unit name is at least a token in existing unit names in unit category and unit system..
     *                   Helps to find a match even when unit name in data structure is prefixed.
     */
    public boolean containsUnitNameInUnitCategoryAndUnitSystem(String unitSystem, String unitCategory, String unitName, boolean fuzzyMatch) {
        if(unitSystem.isEmpty() && unitCategory.isEmpty() && unitName.isEmpty())
            return false;

        unitSystem = unitSystem.toLowerCase();
        unitCategory = unitCategory.toLowerCase();
        if (fuzzyMatch) { // Fuzzy match such that the specified unitName is at least a token in any unit name in a unit group
            for (String unitNameCandidate : repositoryWithDualKeyNCategory.getItem(unitSystem, unitCategory)) {
                if (unitNameCandidate.contains(unitName))
                    return true;
            }
            return false;
        }
        return getUnitNamesByUnitSystemNCategory(unitSystem, unitCategory).contains(unitName);
    }
}
