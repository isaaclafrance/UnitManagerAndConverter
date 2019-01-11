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
import static com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsContentDeterminer.determineDataModelCategories;
import static com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsContentDeterminer.determineHighestPriorityDataModelCategory;

public class UnitsContentModifier {
    private Locale locale;
    private UnitsDataModel unitsDataModel;
    private boolean reUpdateAssociationsOfUnknownUnits;

    ///
    public UnitsContentModifier(){
        this.reUpdateAssociationsOfUnknownUnits = true;
    }

    ///Modify Data Model Content
    public void addUnits(Iterable<Unit> units) throws UnitException {
        for (Unit unit : units)
            addUnit(unit);
    }
    public Unit addUnit(Unit unit) throws UnitException {

        if (addUnitToDataStructures(unit, true, true)) {
            //Make the unit be a base unit if it is a fundamental unit and unique based on dimension;
            if (!unit.isBaseUnit()) {
                boolean isUnique = unitsDataModel.getUnitsContentDeterminer().determineIfUnitIsUniqueByDimension(unit);
                boolean isAFundamentalUnit = unitsDataModel.getUnitManagerContext().getFundamentalUnitsDataModel().containsUnitName(unit.getName());

                if (isUnique || isAFundamentalUnit) {
                    unit.setAsBaseUnit();
                }
            }

            //Attempt to see if this unit can be used to make other unknown units become known.
            if(unit.isBaseUnit())
                incorporateAsBaseUnit(unit);
            updateAssociationsOfUnknownUnits();
        }

        return unit;
    }
    private boolean addUnitToDataStructures(Unit unit, boolean includeAliases, boolean includeUpdateToUnitsClassifier){
        Unit unitInDataStructureWithSameName = unitsDataModel.getUnitsContentMainRetriever().getUnit(unit.getName().toLowerCase(), false);
        boolean unitBeingAddedIsSameInstanceAsPreexisting = unit == unitInDataStructureWithSameName;

        if( unitInDataStructureWithSameName != null && unitsDataModel.getUnitsContentMainRetriever().getUnknownUnit() !=  unitInDataStructureWithSameName) {
            // Blast away previous categorizations for existing same name unit and start on clean slate so to speak
            //If preexisting unit of same name is not the same instance and does not have the same IsCoreUnit state and the new unit has invalid names , then prevent replacement.
            if(unitsDataModel.getRepositoryWithDualKeyNCategory().areKeysValid(unit.getName().toLowerCase(), unit.getAbbreviation()) && unitInDataStructureWithSameName.isCoreUnit() == unit.isCoreUnit()){
                try {
                    if(!unitBeingAddedIsSameInstanceAsPreexisting) {
                        unitsDataModel.getUnitsContentModifier().removeUnit(unitInDataStructureWithSameName.getName(), true, unit, false);
                    }
                    else{
                        unitsDataModel.getRepositoryWithDualKeyNCategory().removeItem(unitInDataStructureWithSameName);
                    }
                } catch (UnitException ue) {
                    ue.printStackTrace();
                    return false;
                }
            }
            else{
                return false;
            }
        }

        //
        try {
            if(!unitBeingAddedIsSameInstanceAsPreexisting && unit.getUnitManagerContext() != unitsDataModel.getUnitManagerContext())
                unit.setUnitManagerContext(unitsDataModel.getUnitManagerContext());
        }
        catch(UnitException ue1) {
            ue1.printStackTrace();
            try {
                unit.setUnitManagerContext(null);
            } catch (UnitException ue2) {
                ue2.printStackTrace();
            }
            return false;
        }

        //
        Unit addedUnit = null;
        for (DATA_MODEL_CATEGORY dataModelCategory : determineDataModelCategories(unit))
            addedUnit = unitsDataModel.getRepositoryWithDualKeyNCategory().addItem(dataModelCategory, unit.getName().toLowerCase(), unit.getAbbreviation(), unit);

        boolean unitAbleToBeAdded = addedUnit != null;

        try {
            if (!unitAbleToBeAdded) {
                unit.setUnitManagerContext(null);
                return false;
            }
        }
        catch(UnitException ue){
            ue.printStackTrace();
            return false;
        }

        //
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
    private boolean removeUnit(String unitName, boolean allowRemovalOfCoreUnits, Unit replacementBaseUnit, boolean resetBaseConversionPolyCoeffsOfDescendents) throws UnitException {
        Unit removedUnitCandidate = unitsDataModel.getRepositoryWithDualKeyNCategory().getFirstItemByAnyKey(unitName.toLowerCase().trim());
        if (removedUnitCandidate != null &&  (allowRemovalOfCoreUnits || determineHighestPriorityDataModelCategory(removedUnitCandidate) != DATA_MODEL_CATEGORY.CORE) ) {
            unitsDataModel.getRepositoryWithDualKeyNCategory().removeItemByKey(unitName);

            unitsDataModel.getUnitManagerContext().getUnitsClassifierDataModel().removeFromHierarchy(removedUnitCandidate);

            if (removedUnitCandidate.isBaseUnit()) {
                removedUnitCandidate.downPropogateBaseUnitModification(replacementBaseUnit == null? unitsDataModel.getUnitsContentMainRetriever().getUnknownUnit():replacementBaseUnit
                        , resetBaseConversionPolyCoeffsOfDescendents);
            } else {
                removedUnitCandidate.getBaseUnit().getConversionsToDescendents().remove(removedUnitCandidate);
            }

            removedUnitCandidate.setUnitManagerContext(null);
            updateFundamentalUnitsDimensionOfKnownUnits(!removedUnitCandidate.isCoreUnit());

            //Remove any conversion favorites that may be associated with this unit as well updateContent the significance rankings
            unitsDataModel.getUnitManagerContext().getConversionFavoritesDataModel().removeAllConversionsWithUnit(removedUnitCandidate);

            return true;
        }
        return false;
    }
    public boolean removeUnit(String unitName) throws UnitException {
        return removeUnit(unitName, false, null, true);
    }
    public boolean removeUnit(Unit unit) throws UnitException {
        if(!unitsDataModel.getRepositoryWithDualKeyNCategory().containsItem(unit))
            return false;
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
    }
    public void removeAllUnitNameAliases() {
        Collection<Unit> allUnits = unitsDataModel.getUnitsContentMainRetriever().getAllUnits();
        for (Unit unit : allUnits) {
            unitsDataModel.getRepositoryWithDualKeyNCategory().removeAllKeyRelationsForKeys(unit.getAliases());
        }
    }

    ///Update Existing Data Model Content
    public void updateAssociationsOfUnknownUnits() throws UnitException {
        Collection<Unit> knownUnits = updateAssociationsOfUnknownUnits(new ArrayList<>(), new ArrayList<>(unitsDataModel.getUnitsContentMainRetriever().getUnknownUnits()));
        for(Unit knownUnit:knownUnits) {
            if(knownUnit.isBaseUnit())
                incorporateAsBaseUnit(knownUnit);
        }
    }
    private Collection<Unit> updateAssociationsOfUnknownUnits(Collection<Unit> unitsNoLongerUnknown, Collection<Unit> currentUnknownUnits) throws UnitException {
        if(!reUpdateAssociationsOfUnknownUnits)
            return Collections.emptyList();

        int originalNumOfNoLongerUnknownUnits = unitsNoLongerUnknown.size();

        for (Unit unit : currentUnknownUnits) {

            if (unit != unitsDataModel.getUnitsContentMainRetriever().getUnknownUnit() && unit.setAutomaticBaseUnit()
                    && UnitsContentDeterminer.determineHighestPriorityDataModelCategory(unit) != DATA_MODEL_CATEGORY.UNKNOWN) {

                unitsNoLongerUnknown.add(unit);
                //Reclassify this unit in the data structure with its newly acquired category
                addUnitToDataStructures(unit, false, true);
            }
        }

        currentUnknownUnits.removeAll(unitsNoLongerUnknown);

		/*Recursively accounts for the edge case where identifiability of one unit is dependent on the identifiability of another unit in the same unknown set.
		 Prevents the ordering of the unknown identification from preventing a dependent unknown unit from being identified. */
        Collection<Unit> unknownUnitsToBeReAnalyzed = unitsNoLongerUnknown.isEmpty() || currentUnknownUnits.isEmpty()
                ? Collections.emptyList() : unknownUnitsToBeReAnalyzed(currentUnknownUnits, unitsNoLongerUnknown);

        if (unitsNoLongerUnknown.size()>originalNumOfNoLongerUnknownUnits && !unknownUnitsToBeReAnalyzed.isEmpty())
            updateAssociationsOfUnknownUnits(unitsNoLongerUnknown, unknownUnitsToBeReAnalyzed);

        return unitsNoLongerUnknown;
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
            if ( !currentUnknownUnit.getName().equalsIgnoreCase(componentUnitName) && namesOfUnitsNoLongerUnknown.contains(componentUnitName))
                return true;
        }
        return false;
    }

    public boolean areAssociationsOfUnknownUnitsReUpdated(){
        return reUpdateAssociationsOfUnknownUnits;
    }
    public void setUpdateAssociationsOfUnknownUnits(boolean updateAssociationsOfUnknownUnits){
        this.reUpdateAssociationsOfUnknownUnits = updateAssociationsOfUnknownUnits;
    }

    public void updateFundamentalUnitsDimensionOfKnownUnits(boolean onlyDynamicUnits){
        Collection<Unit> knownUnits = new ArrayList<>();
        if(onlyDynamicUnits) {
            knownUnits.addAll(unitsDataModel.getRepositoryWithDualKeyNCategory().getItemsByCategory(DATA_MODEL_CATEGORY.DYNAMIC));
        }
        else{
            knownUnits.addAll(unitsDataModel.getRepositoryWithDualKeyNCategory().getAllItems());
        }

        for (Unit unit : knownUnits) {
            DATA_MODEL_CATEGORY previousDataModelCategory = determineHighestPriorityDataModelCategory(unit);

            if(previousDataModelCategory == DATA_MODEL_CATEGORY.UNKNOWN)
                continue;

            unit.setAutomaticUnitSystem();
            unit.setAutomaticUnitTypeNFundamentalTypesDimension();
            unit.setAutomaticCategory();

            DATA_MODEL_CATEGORY currentDataModelCategory = determineHighestPriorityDataModelCategory(unit);

            if (currentDataModelCategory == DATA_MODEL_CATEGORY.UNKNOWN)
                addUnitToDataStructures(unit, false, true);//Readd unit so that it is recategorized as unknown in the data model.
        }
    }

    private void incorporateAsBaseUnit(Unit unit) throws UnitException {
        for (Unit existingBaseUnit : unitsDataModel.getUnitsContentMainRetriever().getBaseUnits()) {
            /*If the existing base unit is not a core unit  (Dynamic units can not replace core units) and dimensions are the same, then set this unit as the base unit of the existing.
             *Otherwise, assign the existing base unit to this unit.
             */
            if(!existingBaseUnit.getName().equalsIgnoreCase(unit.getName()) && UnitOperators.equalsFundamentalUnitsDimension(unit, existingBaseUnit)) {
                boolean unitIsAFundamental = unitsDataModel.getUnitManagerContext().getFundamentalUnitsDataModel().containsUnitName(unit.getName());
                boolean existingBaseUnitIsCore = existingBaseUnit.isCoreUnit();

                boolean existingBaseUnitAbleToBeSetAsNonBase = !existingBaseUnitIsCore || unitIsAFundamental;

                if (existingBaseUnitAbleToBeSetAsNonBase) {
                    existingBaseUnit.setBaseUnit(unit); //Automatically updates the base unit of the the dependent units.
                } else {
                    unit.setBaseUnit(existingBaseUnit);
                }
                break; //Ideally if the data model is consistent, there should have only been one valid existing base unit present.
            }
        }
    }

    ///
    void setUnitsDataModel(UnitsDataModel unitsDataModel) {
        this.unitsDataModel = unitsDataModel;
    }
    void setLocale(Locale locale){
        this.locale = locale;
    }
}
