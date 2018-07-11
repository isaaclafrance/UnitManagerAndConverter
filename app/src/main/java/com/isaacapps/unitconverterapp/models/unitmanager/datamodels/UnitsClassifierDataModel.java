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
        } else {
            //Save as a Set collection  in order to ignore duplicate names in same unit group
            return repositoryWithDualKeyNCategory.addItem(unit.getUnitSystem(), unit.getName(), unit.getCategory(), new HashSet<>(Collections.singletonList(unit.getName())));
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

        for (String unitCategory : getUnitCategoriesInUnitSystem(unitSystem))
            allUnitNames.addAll(getUnitNamesByUnitSystemNCategory(unitSystem, unitCategory));

        return allUnitNames;
    }

    public Collection<String> getUnitNamesByUnitSystemNCategory(String unitSystem, String unitCategory) {
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
        return repositoryWithDualKeyNCategory.getKey2sByCategory(unitSystem);
    }

    /**
     * Since the data structure enforces a relations between unitname and category, the unit name can be used to return unit name groups that share the same category.
     *
     * @param unitName
     * @return If the unit name is found in multiple unit systems then multiple groups will be returned
     */
    public Collection<Collection<String>> getUnitGroupsOfUnit(String unitName) {
        return repositoryWithDualKeyNCategory.getItemsByAnyKey(unitName);
    }

    ///
    public boolean containUnitSystem(String unitSystem, boolean fuzzyMatch) {
        if (fuzzyMatch) { // Fuzzy match such that specified unitSystem is at least a token in any existing unit system
            for (String unitSystemCandidate : repositoryWithDualKeyNCategory.getAllAssignedCategories()) {
                if (unitSystemCandidate.contains(unitSystem))
                    return true;
            }
            return false;
        }
        return repositoryWithDualKeyNCategory.containsCategory(unitSystem);
    }

    public boolean containsUnitCategoryInUnitSystem(String unitSystem, String unitCategory, boolean fuzzyMatch) {
        if (fuzzyMatch) { // Fuzzy match such that the specified unitName is at least a token in any unit name in a unit group
            for (String unitCategoryCandidate : repositoryWithDualKeyNCategory.getKey2sByCategory(unitSystem)) {
                if (unitCategoryCandidate.contains(unitCategory))
                    return true;
            }
            return false;
        }
        return repositoryWithDualKeyNCategory.containsKeyInCategory(unitSystem, unitCategory);
    }

    public boolean containsUnitNameInUnitCategoryAndUnitSystem(String unitSystem, String unitCategory, String unitName, boolean fuzzyMatch) {
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
