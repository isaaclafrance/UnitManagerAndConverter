package com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel;
import com.isaacapps.unitconverterapp.processors.operators.dimensions.DimensionOperators;
import com.isaacapps.unitconverterapp.processors.operators.measurables.units.UnitOperators;
import com.isaacapps.unitconverterapp.processors.serializers.SerializingException;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.componentnunit.ComponentUnitsDimensionSerializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.ContentDeterminer.DATA_MODEL_CATEGORY;
import static com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.ContentDeterminer.determineGeneralDataModelCategory;

public class ContentQuerier {
    private UnitsDataModel unitsDataModel;
    private ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer;

    public ContentQuerier() {
    }

    ///Query for Content That Match Particular Conditions

    public Collection<Unit> getUnitsByComponentUnitsDimension(Map<String, Double> componentUnitsDimension, boolean overrideToFundamentalUnitTypes) {
        List<Unit> unitsMatched = new ArrayList<>();

        for (Unit unit : unitsDataModel.getRepositoryWithDualKeyNCategory().getAllItems()) {
            if (DimensionOperators.equalsDimension(unit.getComponentUnitsDimension(), componentUnitsDimension)
                    || overrideToFundamentalUnitTypes && UnitOperators.equalsDeepComponentUnitsDimension(unit.getComponentUnitsDimension()
                    , componentUnitsDimension, unitsDataModel.getUnitManagerContext()))
            {
                unitsMatched.add(unit);
            }
        }

        return unitsMatched;
    }

    public Collection<Unit> getUnitsByFundamentalUnitTypeDimension(Map<FundamentalUnitsDataModel.UNIT_TYPE, Double> fundamentalUnitsDimension) {
        List<Unit> units = new ArrayList<>();

		/*Quicker to just access the small list of base units (has a non-unknown fundamental unit in most cases)
		 and then getting their descendent units. This allows for less dimension comparisons and more or less constant time complexity */
        for (Unit unit : unitsDataModel.getRepositoryWithDualKeyNCategory().getItemsByCategory(DATA_MODEL_CATEGORY.BASE)) {
            if (UnitOperators.equalsFundamentalUnitsDimension(unit.getFundamentalUnitTypesDimension(), fundamentalUnitsDimension))
                units.addAll(unit.getConversionsToDescendents().keySet());
        }

        return units;
    }

    public Collection<Unit> getUnitsWithMatchingFundamentalUnitDimension(Unit unit) {
        List<Unit> matchUnits = new ArrayList<>();

        if (unit.getUnitManagerContext() == unitsDataModel.getUnitManagerContext()) {
            if (unit.getBaseUnit().isBaseUnit() && determineGeneralDataModelCategory(unit.getBaseUnit()) != DATA_MODEL_CATEGORY.UNKNOWN) {
                matchUnits.addAll(unit.getBaseUnit().getConversionsToDescendents().keySet());
            } else {
                matchUnits.addAll(getUnitsByFundamentalUnitTypeDimension(unit.getFundamentalUnitTypesDimension()));
            }
        }

        return matchUnits;
    }

    public Collection<Unit> getUnitsByUnitSystem(String unitSystem) {
        List<Unit> unitMatches = new ArrayList<>();

        for (String unitName : unitsDataModel.getUnitManagerContext().getUnitsClassifierDataModel().getUnitNamesByUnitSystem(unitSystem.trim()))
            unitMatches.add(unitsDataModel.getContentMainRetriever().getUnit(unitName, false));

        return unitMatches;
    }

    ///Query for Existence of Content

    public Collection<Unit> getCorrespondingUnitsWithUnitSystem(Unit sourceUnit, String targetUnitSystemString, boolean createMissingUnit) {
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
        Collection<Unit> matchCandidateUnits = getUnitsByComponentUnitsDimension(sourceUnit.getComponentUnitsDimension(), true);

        for (Unit candidate : matchCandidateUnits) {
            if (candidate.getUnitSystem().equalsIgnoreCase(sourceUnit.getUnitSystem()))
                correspondingUnits.add(candidate);
        }

        if (!correspondingUnits.isEmpty())
            return correspondingUnits;

        //If still nothing is found, then attempt to constructed a suitable unit by finding replacements with the target unit system for each unit component
        if (createMissingUnit && (sourceUnit.getType() == FundamentalUnitsDataModel.UNIT_TYPE.DERIVED_MULTI_UNIT || sourceUnit.getType() == FundamentalUnitsDataModel.UNIT_TYPE.DERIVED_MULTI_UNIT)) {

            Map<String, Double> properComponentUnitDimension = new HashMap<>();

            for (Map.Entry<String, Double> componentUnitEntry : sourceUnit.getComponentUnitsDimension().entrySet()) {
                Iterator<Unit> replacementUnitIterator = getCorrespondingUnitsWithUnitSystem(unitsDataModel.getContentMainRetriever().getUnit(componentUnitEntry.getKey(), createMissingUnit)
                        , targetUnitSystemString, true).iterator();

                if (replacementUnitIterator.hasNext())
                    properComponentUnitDimension.put(replacementUnitIterator.next().getName(), componentUnitEntry.getValue());
            }

            correspondingUnits.add(unitsDataModel.getContentModifier().addUnit(new Unit(properComponentUnitDimension, false)));
        }

        return correspondingUnits;
    }

    public SortedSet<Unit> getUnitsWithSimilarNames(final String providedName) {
        //TODO: Use the levenshtein distance algorithm

        //Non duplicate data structure since there are instances where full name may be the same as abbreviations.
        SortedSet<Unit> unitCandidates = new TreeSet<>(new Comparator<Unit>() {

            //Sort the candidates list by the significance of the provided name with respect to their full name and abbreviated name
            @Override
            public int compare(Unit lhsUnit, Unit rhsUnit) {
                Double lhsUnitFullNameSignificance = (double) providedName.length() / lhsUnit.getName().length();
                Double lhsUnitAbbreviationSignificance = (double) providedName.length() / lhsUnit.getAbbreviation().length();

                Double rhsUnitFullNameSignificance = (double) providedName.length() / rhsUnit.getName().length();
                Double rhsUnitAbbreviationSignificance = (double) providedName.length() / rhsUnit.getAbbreviation().length();

                /*Only select the abbreviation significance if the length of provided name is less than the length of the abbreviation,
                 *otherwise the Full Name significance is utilized.This is to ensure that abbreviations take precedence
                 *when the length of the provided name is similar to the length of the available abbreviations
                 */
                Double preferredLhsSignificance = (lhsUnitAbbreviationSignificance < 1) ? lhsUnitAbbreviationSignificance : lhsUnitFullNameSignificance;
                Double preferredRhsSignificance = (rhsUnitAbbreviationSignificance < 1) ? rhsUnitAbbreviationSignificance : rhsUnitFullNameSignificance;

                return -preferredLhsSignificance.compareTo(preferredRhsSignificance); //need to take negative to order from greatest to least.
            }
        });

        for (String unitName : unitsDataModel.getRepositoryWithDualKeyNCategory().getAllKeys()) {
            if (providedName.contains(unitName))
                unitCandidates.add(unitsDataModel.getRepositoryWithDualKeyNCategory().getFirstItemByAnyKey(unitName));
        }

        return unitCandidates;
    }

    ///

    public boolean containsUnit(String unitName) {
        //Name can match to a unit full name or an abbreviation. Must be a full match.
        return unitsDataModel.getRepositoryWithDualKeyNCategory().containsKey(unitName.toLowerCase().trim());
    }

    public boolean containsUnit(Unit unit) {
        return unitsDataModel.getRepositoryWithDualKeyNCategory().containsItem(unit);
    }

    /**
     * If the unit is not unknown and the fundamental dimension of this unit and its existing base unit match
     * ,then that base unit will be returned. Otherwise, tries to match the unit with a corresponding base unit in the data model.
     */
    public Unit getBaseUnitMatch(Unit unit) {
        if (determineGeneralDataModelCategory(unit) != DATA_MODEL_CATEGORY.UNKNOWN
                && unit.getUnitManagerContext() == unitsDataModel.getUnitManagerContext()) {
            return unit.getBaseUnit();
        } else {
            for (Unit unitMatch : unitsDataModel.getContentMainRetriever().getBaseUnits()) {
                if (!unitMatch.getName().equalsIgnoreCase(unit.getName())
                        && (unit.getCategory().equalsIgnoreCase(unitMatch.getCategory())
                        || UnitOperators.equalsFundamentalUnitsDimension(unit, unitMatch)))
                    return unitMatch;
            }
            return unitsDataModel.getContentMainRetriever().getUnknownUnit();
        }
    }

    public String getReducedUnitMatch(String unitName, String unitSystem) throws SerializingException {
        Unit unitMatch = unitsDataModel.getContentMainRetriever().getUnit(unitName.toLowerCase().trim(), true);

        if (determineGeneralDataModelCategory(unitMatch) == DATA_MODEL_CATEGORY.UNKNOWN)
            return unitName;

        return getReducedUnitMatch(unitMatch, unitSystem.trim()).getName();
    }

    public Unit getReducedUnitMatch(Unit unit, String unitSystem) throws SerializingException {
        return getReducedUnitMatchByComponentUnitsDimension(unit.getComponentUnitsDimension(), unitSystem);
    }

    ///

    public Unit getReducedUnitMatchByComponentUnitsDimension(Map<String, Double> providedComponentUnitsDimension, String unitSystem) throws SerializingException {
        Unit smallestUnitMatchByDimension = getReducedUnitMatchByFundamentalUnitsTypeDimension(unitsDataModel.getUnitManagerContext().getFundamentalUnitsDataModel()
                        .transformComponentUnitsDimensionToFundamentalUnitsDimension(providedComponentUnitsDimension)
                , unitSystem.trim());

        if (smallestUnitMatchByDimension.getComponentUnitsDimension().size() < providedComponentUnitsDimension.size()) {
            return smallestUnitMatchByDimension;
        } else {
            return unitsDataModel.getContentMainRetriever().getUnit(componentUnitsDimensionSerializer.serialize(providedComponentUnitsDimension), true);
        }
    }

    public Unit getReducedUnitMatchByFundamentalUnitsTypeDimension(Map<FundamentalUnitsDataModel.UNIT_TYPE, Double> fundamentalUnitsTypeDimension, final String unitSystem) {
        Collection<Unit> unitsWithSameFundamentalDimension = getUnitsByFundamentalUnitTypeDimension(fundamentalUnitsTypeDimension);
        Unit candidateUnit;

        if (!unitsWithSameFundamentalDimension.isEmpty()) {
            //If result set is large enough, sort such that units with the smallest dimension and exclusively of the specified unit system take precedence.
            candidateUnit = Collections.min(unitsWithSameFundamentalDimension, new Comparator<Unit>() {
                @Override
                public int compare(Unit lhsUnit, Unit rhsUnit) {
                    boolean lhsExclusivelyMatchesUnitSystem = lhsUnit.getUnitSystem().equalsIgnoreCase(unitSystem),
                            rhsExclusivelyMatchesUnitSystem = rhsUnit.getUnitSystem().equalsIgnoreCase(unitSystem);

                    return lhsExclusivelyMatchesUnitSystem && rhsExclusivelyMatchesUnitSystem
                            ? Double.compare(lhsUnit.getComponentUnitsDimension().size(), rhsUnit.getComponentUnitsDimension().size())
                            : (lhsExclusivelyMatchesUnitSystem ? -1 : (rhsExclusivelyMatchesUnitSystem ? 1 : 0));
                }
            });

            if (candidateUnit.getUnitSystem().equalsIgnoreCase(unitSystem))
                return candidateUnit;
        }

        //Get a new unit that is equivalent to the replacement of each fundamental unit type with base unit equivalent in the default unit system.
        return unitsDataModel.getContentModifier().addUnit(new Unit(unitsDataModel.getUnitManagerContext().getFundamentalUnitsDataModel()
                .transformFundamentalUnitsDimensionToComponentUnitDimension(fundamentalUnitsTypeDimension
                        , unitSystem.isEmpty() ? FundamentalUnitsDataModel.DEFAULT_UNIT_SYSTEM : unitSystem), false));
    }

    ///
    public void setUnitsDataModel(UnitsDataModel unitsDataModel) {
        this.unitsDataModel = unitsDataModel;
    }

    public void setComponentUnitsDimensionSerializer(ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer) {
        this.componentUnitsDimensionSerializer = componentUnitsDimensionSerializer;
    }
}
