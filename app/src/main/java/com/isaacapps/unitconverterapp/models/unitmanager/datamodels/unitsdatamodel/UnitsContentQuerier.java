package com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel;
import com.isaacapps.unitconverterapp.processors.operators.dimensions.DimensionOperators;
import com.isaacapps.unitconverterapp.processors.operators.measurables.units.UnitOperators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsContentDeterminer.DATA_MODEL_CATEGORY;
import static com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsContentDeterminer.determineHighestPriorityDataModelCategory;

public class UnitsContentQuerier {
    private UnitsDataModel unitsDataModel;

    public UnitsContentQuerier() {
    }

    ///Query for Content That Match Particular Conditions

    public Collection<Unit> queryUnitsByComponentUnitsDimension(Map<String, Double> componentUnitsDimension, boolean overrideToFundamentalUnitTypes) {
        List<Unit> unitsMatched = new ArrayList<>();

        Collection<Unit> limitedSearchableSet = new ArrayList(unitsDataModel.getRepositoryWithDualKeyNCategory().getItemsByCategory(DATA_MODEL_CATEGORY.BASE));
        limitedSearchableSet.addAll(unitsDataModel.getRepositoryWithDualKeyNCategory().getItemsByCategory(DATA_MODEL_CATEGORY.UNKNOWN));

        for (Unit unit : limitedSearchableSet ) {
            if (DimensionOperators.equalsDimension(unit.getComponentUnitsDimension(), componentUnitsDimension)
                    || overrideToFundamentalUnitTypes && UnitOperators.equalsDeepComponentUnitsDimension(unit.getComponentUnitsDimension(), componentUnitsDimension, unit.getUnitSystemsCollection(), unitsDataModel.getUnitManagerContext()))
            {
                unitsMatched.add(unit);
                unitsMatched.addAll(unit.getConversionsToDescendents().keySet());
            }
        }

        return unitsMatched;
    }

    public List<Unit> queryUnitsByFundamentalUnitTypeDimension(Map<FundamentalUnitsDataModel.UNIT_TYPE, Double> fundamentalUnitsDimension) {
        List<Unit> unitsMatched = new ArrayList<>();

		/*Quicker to just access the small list of base units (has a non-unknown fundamental unit in most cases)
		 and then getting their descendent units, which means less dimension comparisons.*/
        for (Unit baseUnit : unitsDataModel.getRepositoryWithDualKeyNCategory().getItemsByCategory(DATA_MODEL_CATEGORY.BASE)) {
            if (UnitOperators.equalsFundamentalUnitsDimension(baseUnit.getFundamentalUnitTypesDimension(), fundamentalUnitsDimension)){
                unitsMatched.add(baseUnit);
                unitsMatched.addAll(baseUnit.getConversionsToDescendents().keySet());
            }
        }

        return unitsMatched;
    }

    public List<Unit> queryUnitsWithMatchingFundamentalUnitDimension(Unit unit) {
        List<Unit> unitsMatched = new ArrayList<>();

        if (unit.getUnitManagerContext() == unitsDataModel.getUnitManagerContext()) {
            if (unit.getBaseUnit().isBaseUnit() && determineHighestPriorityDataModelCategory(unit.getBaseUnit()) != DATA_MODEL_CATEGORY.UNKNOWN) {
                unitsMatched.add(unit.getBaseUnit());
                unitsMatched.addAll(unit.getBaseUnit().getConversionsToDescendents().keySet());
            } else {
                unitsMatched.addAll(queryUnitsByFundamentalUnitTypeDimension(unit.getFundamentalUnitTypesDimension()));
            }
        }

        return unitsMatched;
    }

    public List<Unit> queryUnitsByUnitSystem(String unitSystem) {
        List<Unit> unitMatches = new ArrayList<>();

        for (String unitName : unitsDataModel.getUnitManagerContext().getUnitsClassifierDataModel().getUnitNamesByUnitSystem(unitSystem.trim()))
            unitMatches.add(unitsDataModel.getUnitsContentMainRetriever().getUnit(unitName, false));

        return unitMatches;
    }

    public List<Unit> queryUnitsByUnitCategory(String unitCategory) {
        List<Unit> unitMatches = new ArrayList<>();

        Collection<Collection<String>> unitNameGroupsWithSameCategory = unitsDataModel.getUnitManagerContext().getUnitsClassifierDataModel().getUnitGroupsWithSameCategory(unitCategory);
        for(Collection<String> unitNameGroup:unitNameGroupsWithSameCategory){
            for (String unitName : unitNameGroup){
                unitMatches.add(unitsDataModel.getUnitsContentMainRetriever().getUnit(unitName, false));
            }
        }

        return unitMatches;
    }

    /**
     * Queries data models to find units with same dimension but with all component units in the specified target unit system.
     * @param createMissingValidComplexUnit Indicates whether to attempt creating a new unit with component units in specified target unit system if none exists already.
     * @return
     */
    public List<Unit> queryCorrespondingUnitsWithUnitSystem(Unit sourceUnit, String targetUnitSystemString, boolean createMissingValidComplexUnit) {
        List<Unit> correspondingUnits = new ArrayList<>();

        if (sourceUnit.getUnitManagerContext() != unitsDataModel.getUnitManagerContext())
            return correspondingUnits;

        //First use the unit classifier data model to find corresponding unit target unit system
        Collection<String> matchCandidateNames = unitsDataModel.getUnitManagerContext().getUnitsClassifierDataModel()
                .getUnitNamesByUnitSystemNCategory(targetUnitSystemString, sourceUnit.getCategory());

        for (String unitName : matchCandidateNames) {
            correspondingUnits.add(unitsDataModel.getRepositoryWithDualKeyNCategory().getFirstItemByAnyKey(unitName));
        }

        if (!correspondingUnits.isEmpty())
            return correspondingUnits;

        //Otherwise perform direct dimension comparison.
        Collection<Unit> matchCandidateUnits = queryUnitsByComponentUnitsDimension(sourceUnit.getComponentUnitsDimension(), true);

        for (Unit candidate : matchCandidateUnits) {
            if (candidate.getUnitSystem().equalsIgnoreCase(sourceUnit.getUnitSystem()))
                correspondingUnits.add(candidate);
        }

        if (!correspondingUnits.isEmpty())
            return correspondingUnits;

        //If still nothing is found, then attempt to constructed a suitable unit by finding replacements with the target unit system for each unit component
        if (createMissingValidComplexUnit && sourceUnit.getUnitType() == FundamentalUnitsDataModel.UNIT_TYPE.COMPLEX) {

            Map<String, Double> properComponentUnitDimension = new HashMap<>();

            for (Map.Entry<String, Double> componentUnitEntry : sourceUnit.getComponentUnitsDimension().entrySet()) {
                Iterator<Unit> replacementUnitIterator = queryCorrespondingUnitsWithUnitSystem(unitsDataModel.getUnitsContentMainRetriever().getUnit(componentUnitEntry.getKey(), createMissingValidComplexUnit)
                        , targetUnitSystemString, true).iterator();

                if (replacementUnitIterator.hasNext())
                    properComponentUnitDimension.put(replacementUnitIterator.next().getName(), componentUnitEntry.getValue());
            }

            try {
                Unit newUnit = new Unit(properComponentUnitDimension);
                unitsDataModel.getUnitsContentModifier().addUnit(newUnit);
                correspondingUnits.add(newUnit);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return correspondingUnits;
    }

    /**
     * Sort the candidates list by the significance of the provided name with respect to their full name and abbreviated name
     */
    private class UnitsWithSimilarNameComparator implements Comparator<Unit>{
        private int providedNameLength;

        public UnitsWithSimilarNameComparator(int providedNameLength){
            this.providedNameLength = providedNameLength;
        }

        @Override
        public int compare(Unit lhsUnit, Unit rhsUnit) {
            Double lhsUnitFullNameSignificance = (double) providedNameLength / lhsUnit.getName().length();
            Double lhsUnitAbbreviationSignificance = (double) providedNameLength / lhsUnit.getAbbreviation().length();

            Double rhsUnitFullNameSignificance = (double) providedNameLength / rhsUnit.getName().length();
            Double rhsUnitAbbreviationSignificance = (double) providedNameLength / rhsUnit.getAbbreviation().length();

            /*Only select the abbreviation significance if the length of provided name is less than the length of the abbreviation,
             *otherwise the Full Name significance is utilized.This is to ensure that abbreviations take precedence
             *when the length of the provided name is similar to the length of the available abbreviations
             */
            Double preferredLhsSignificance = (lhsUnitAbbreviationSignificance < 1) ? lhsUnitAbbreviationSignificance : lhsUnitFullNameSignificance;
            Double preferredRhsSignificance = (rhsUnitAbbreviationSignificance < 1) ? rhsUnitAbbreviationSignificance : rhsUnitFullNameSignificance;

            return -preferredLhsSignificance.compareTo(preferredRhsSignificance); //need to take negative to order from greatest to least.
        }
    }
    /**
     * Finds a set of units sorted by the degree of similarity of their names to the provided name
     */
    public SortedSet<Unit> queryUnitsOrderedBySimilarNames(final String providedName) {
        //TODO: Incorporate the levenshtein distance algorithm, soundex, metaphone ????

        //Non duplicate data structure since there are instances where full name may be the same as abbreviations.
        SortedSet<Unit> unitCandidates = new TreeSet<>(new UnitsWithSimilarNameComparator(providedName.length()));

        for (String unitName : unitsDataModel.getRepositoryWithDualKeyNCategory().getAllKeys()) {
            if (unitName.contains(providedName))
                unitCandidates.add(unitsDataModel.getRepositoryWithDualKeyNCategory().getFirstItemByAnyKey(unitName));
        }

        return unitCandidates;
    }

    ///Query for Existence of Content

    /**
     * Name can match to a unit full name or an abbreviation. Must be a full text match.
     */
    public boolean containsUnit(String unitName) {
        //Name can match to a unit full name or an abbreviation. Must be a full match.
        return unitsDataModel.getRepositoryWithDualKeyNCategory().containsKey(unitName.toLowerCase().trim());
    }
    public boolean containsUnit(Unit unit) {
        return unitsDataModel.getRepositoryWithDualKeyNCategory().containsItem(unit);
    }

    ///
    public void setUnitsDataModel(UnitsDataModel unitsDataModel) {
        this.unitsDataModel = unitsDataModel;
    }

}
