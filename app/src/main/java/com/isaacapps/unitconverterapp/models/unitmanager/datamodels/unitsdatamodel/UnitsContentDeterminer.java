package com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel;
import com.isaacapps.unitconverterapp.processors.operators.dimensions.DimensionOperators;
import com.isaacapps.unitconverterapp.processors.operators.measurables.units.UnitOperators;
import com.isaacapps.unitconverterapp.processors.parsers.ParsingException;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.componentunits.ComponentUnitsDimensionParser;
import com.isaacapps.unitconverterapp.processors.parsers.generaltext.PluralTextParser;
import com.isaacapps.unitconverterapp.processors.serializers.SerializingException;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.componentnunit.ComponentUnitsDimensionSerializer;
import static com.isaacapps.unitconverterapp.processors.operators.dimensions.DimensionOperators.DIMENSION_TYPE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel.UNIT_TYPE;

public class UnitsContentDeterminer {
    public enum DATA_MODEL_CATEGORY {CORE, DYNAMIC, BASE, NON_BASE, UNKNOWN}

    private UnitsDataModel unitsDataModel;
    private PluralTextParser pluralTextParser;
    private ComponentUnitsDimensionParser componentUnitsDimensionParser;
    private ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer;

    public UnitsContentDeterminer() { }

    /**
     * Simplifies a data model category group into one category based on a specific order of precedence.
     * If a unit is explicitly defined unknown, no other categories matter.
     * Whether or not a unit is core or dynamic has greater functional implications than whether or not it is a base unit.
     * If the unit does not fit any predefined data model categories, then it it implicitly defined as unknown.
     */
    public static DATA_MODEL_CATEGORY determineHighestPriorityDataModelCategory(Unit unit) {
        Collection<DATA_MODEL_CATEGORY> dataModelCategoryGroup = determineDataModelCategories(unit);

        if (dataModelCategoryGroup.contains(DATA_MODEL_CATEGORY.UNKNOWN)) {
            return DATA_MODEL_CATEGORY.UNKNOWN;
        } else if (dataModelCategoryGroup.contains(DATA_MODEL_CATEGORY.CORE)) {
            return DATA_MODEL_CATEGORY.CORE;
        } else if(dataModelCategoryGroup.contains(DATA_MODEL_CATEGORY.DYNAMIC)){
            return DATA_MODEL_CATEGORY.DYNAMIC;
        } else{
            return DATA_MODEL_CATEGORY.UNKNOWN;
        }
    }

    /**
     *
     */
    public static Collection<DATA_MODEL_CATEGORY> determineDataModelCategories(Unit unit) {
        Collection<DATA_MODEL_CATEGORY> dataModelCategories = new HashSet<>();

        //
        if (unit.getBaseUnit() == null || unit.getBaseUnit().getUnitType() == FundamentalUnitsDataModel.UNIT_TYPE.UNKNOWN
                || unit.getFundamentalUnitTypesDimension().containsKey(FundamentalUnitsDataModel.UNIT_TYPE.UNKNOWN)
                || unit.getUnitType() == FundamentalUnitsDataModel.UNIT_TYPE.UNKNOWN) {
            dataModelCategories.add(DATA_MODEL_CATEGORY.UNKNOWN);
        }

        //
        if (unit.isCoreUnit()) {
            dataModelCategories.add(DATA_MODEL_CATEGORY.CORE);
        } else {
            dataModelCategories.add(DATA_MODEL_CATEGORY.DYNAMIC);
        }

        //
        if (unit.isBaseUnit()) {
            dataModelCategories.add(DATA_MODEL_CATEGORY.BASE);
        } else {
            dataModelCategories.add(DATA_MODEL_CATEGORY.NON_BASE);
        }

        return dataModelCategories;
    }

    ///

    /**
     * Finds the corresponding singularized unit definition for a provided unit definition containing plural component units.
     * @param pluralUnitDefinition Complex (ie. a^2*b^2) or atomic unit definition with a plural component.
     * @return New formatted unit definition with all potential plural components singualrized to an existing unit name.
     * If nothing can be singularized, then the whole provided unit definition is returned as is.
     */
    public String determineSingularOfUnitName(String pluralUnitDefinition) {
        try {
            if (!componentUnitsDimensionParser.getDimensionParserBuilder().getDimensionComponentDefiner().hasComplexDimensions(pluralUnitDefinition)) {
                Collection<String> unitNameCandidates = pluralTextParser.parse(pluralUnitDefinition);

                for (String unitNameCandidate : unitNameCandidates) {
                    if (unitsDataModel.getUnitsContentQuerier().containsUnit(unitNameCandidate))
                        return unitNameCandidate;
                }

                return pluralUnitDefinition;
            } else {
                Map<String, Double> oldComplexComponentUnitDimension = componentUnitsDimensionParser.parse(pluralUnitDefinition);

                Map<String, Double> singularizedComplexComponentUnitDimension = new HashMap<>();

                for (Map.Entry<String, Double> componentUnitEntry : oldComplexComponentUnitDimension.entrySet()) {
                    singularizedComplexComponentUnitDimension.put(determineSingularOfUnitName(componentUnitEntry.getKey())
                            , componentUnitEntry.getValue());
                }

                return componentUnitsDimensionSerializer.serialize(singularizedComplexComponentUnitDimension);
            }
        }
        catch(Exception e){
            return pluralUnitDefinition;
        }
    }

    ///

    /**
     *  Uses heuristic when trying to find a corresponding base unit in the data model.
     *  @see #determineBaseUnit(Unit, boolean)
     */
    public Unit determineBaseUnit(Unit unit) {
        return determineBaseUnit(unit, true);
    }
    /**
     * If using the heuristics and the unit is not unknown and the fundamental dimension of this unit and its existing base unit match
     * ,then that base unit will be returned. Otherwise, tries to match the unit with a corresponding base unit in the data model.
     */
    public Unit determineBaseUnit(Unit unit, boolean initiallyUseHeuristic) {
        boolean shortCircuitToBase = initiallyUseHeuristic && determineHighestPriorityDataModelCategory(unit) != DATA_MODEL_CATEGORY.UNKNOWN
                && unit.getUnitManagerContext() == unitsDataModel.getUnitManagerContext();

        //shortCircuitToBase = shortCircuitToBase || unit.isBaseUnit() && unit.getDimensionType() == DIMENSION_TYPE.SIMPLE
               // && unit.getComponentUnitsDimension().containsKey(unit.getName().toLowerCase());

        if (shortCircuitToBase) {
            return unit.getBaseUnit();
        } else {
            for (Unit unitMatch : unitsDataModel.getUnitsContentMainRetriever().getBaseUnits()) {
                if (!unitMatch.getName().equalsIgnoreCase(Unit.UNKNOWN_UNIT_NAME)
                        && !unitMatch.getName().equalsIgnoreCase(unit.getName())
                        && (UnitOperators.equalsFundamentalUnitsDimension(unit, unitMatch, true) || UnitOperators.equalsFundamentalUnitsDimension(unit, unitMatch, false)))
                    return unitMatch;
            }
            return unitsDataModel.getUnitsContentMainRetriever().getUnknownUnit();
        }
    }

    ///

    /**
     * TODO: Create unit tests testing both the heuristic and non-heuristic code paths.
     * Tries to find a matching unit with a specific unit system that has a smaller component units dimension.
     * @param initiallyUseQuickerHeuristic Indicates whether to initially use a less computationally intensive method first.
     *                                     The trade-off is that return unit may not have the absolute smallest dimension out of all possible compatible units.
     * @throws SerializingException
     */
    public Unit determineReducedUnit(Unit unit, String unitSystem, boolean initiallyUseQuickerHeuristic) throws SerializingException {
        if (determineHighestPriorityDataModelCategory(unit) == DATA_MODEL_CATEGORY.UNKNOWN)
            return unit;

        unitSystem = (unitSystem.isEmpty() ? FundamentalUnitsDataModel.DEFAULT_UNIT_SYSTEM : unitSystem).trim();

        if(initiallyUseQuickerHeuristic) {
            Collection<Unit> searchableSet =  new HashSet<>();
            searchableSet.add(unit.getBaseUnit());
            searchableSet.addAll(unit.getBaseUnit().getConversionsToDescendents().keySet());

            Unit candidateUnitWithSmallestDimension = findSmallestDimensionUnitWithSameUnitSystem(searchableSet, unitSystem);
            if(candidateUnitWithSmallestDimension != null && candidateUnitWithSmallestDimension.getComponentUnitsDimension().size() < unit.getComponentUnitsDimension().size())
                return candidateUnitWithSmallestDimension;
        }

        return determineReducedUnitByComponentUnitsDimension(unit.getComponentUnitsDimension(), unitSystem);
    }
    /**
     * Tries to find a matching unit with a specific unit system that has a smaller component units dimension.
     * If possible, attempts to create a new unit with a smaller dimension when none currently exists.
     * @throws SerializingException
     * @throws ParsingException
     */
    public Unit determineReducedUnitByComponentUnitsDimension(String providedComponentUnitsDimension, String unitSystem) throws SerializingException, ParsingException {
        return determineReducedUnitByComponentUnitsDimension(componentUnitsDimensionParser.parse(providedComponentUnitsDimension), unitSystem);
    }
    /**
     * Tries to find a matching unit with a specific unit system that has a smaller component units dimension.
     * If possible, attempts to create a new unit with a smaller dimension when none currently exists.
     * @throws SerializingException
     */
    public Unit determineReducedUnitByComponentUnitsDimension(Map<String, Double> providedComponentUnitsDimension, String unitSystem) throws SerializingException {
        Map<String, Double> potentialSmallestComponentUnitDimension = determineReducedComponentUnitsDimension(providedComponentUnitsDimension, unitSystem);

        String wellFormattedDimension = componentUnitsDimensionSerializer.serialize(potentialSmallestComponentUnitDimension);

        return unitsDataModel.getUnitsContentMainRetriever().getUnit(wellFormattedDimension, true);
    }

    /**
     * Attempts to find the first smallest equivalent component unit dimension for provided fundamental unit type dimension
     * by using a permutated partitioning and then recomposition method. Short circuits to the first found smallest dimension
     * and does not attempt to find the absolute smallest possible due to possible time complexity implications.
     * @return Smallest component unit dimension unit that meet criteria or otherwise just the provided component unit dimension.
     */
    public Map<String, Double> determineReducedComponentUnitsDimension(Map<String, Double> providedComponentUnitsDimension, String unitSystem){
        providedComponentUnitsDimension = DimensionOperators.removeDimensionItemsRaisedToZero(providedComponentUnitsDimension);

        unitSystem = (unitSystem.isEmpty() ? FundamentalUnitsDataModel.DEFAULT_UNIT_SYSTEM : unitSystem).trim();

        Map<FundamentalUnitsDataModel.UNIT_TYPE, Double> calculatedFundamentalUnitsTypeDimension = unitsDataModel.getUnitManagerContext().getFundamentalUnitsDataModel()
                .transformComponentUnitsDimensionToFundamentalUnitsDimension(providedComponentUnitsDimension, Unit.getUnitSystemsCollection(unitSystem), true);

        calculatedFundamentalUnitsTypeDimension = DimensionOperators.removeDimensionItemsRaisedToZero(calculatedFundamentalUnitsTypeDimension);

        Map<String, Double> potentialSmallestComponentUnitDimension = determineReducedComponentUnitDimensionByPartitioningFundamentalUnitsTypeDimension(calculatedFundamentalUnitsTypeDimension, unitSystem);

        return potentialSmallestComponentUnitDimension.size() < providedComponentUnitsDimension.size()? potentialSmallestComponentUnitDimension:providedComponentUnitsDimension;
    }
    private Map<String, Double> determineReducedComponentUnitDimensionByPartitioningFundamentalUnitsTypeDimension(Map<UNIT_TYPE, Double> fundamentalUnitsTypeDimension, final String unitSystem) {
        List<Unit> unitsMatchedFromPartitionChunks = new ArrayList<>();
        List<FundamentalUnitsDataModel.UNIT_TYPE> excludedDimensionItems = new ArrayList<>();
        Map<UNIT_TYPE, Double> subsetFundamentalUnitTypeDimension;

        while(!fundamentalUnitsTypeDimension.isEmpty()) {

            Unit unitResultFromPermutationOfPartition = null;
            excludedDimensionItems.clear();

            for (FundamentalUnitsDataModel.UNIT_TYPE unitType : fundamentalUnitsTypeDimension.keySet()) {
                excludedDimensionItems.add(unitType);
                subsetFundamentalUnitTypeDimension = new EnumMap<>(fundamentalUnitsTypeDimension);

                for (UNIT_TYPE excludedDimensionItem : excludedDimensionItems)
                    subsetFundamentalUnitTypeDimension.remove(excludedDimensionItem);

                unitResultFromPermutationOfPartition = determineReducedMatchedUnitByFundamentalUnitsTypeDimensionUsingUsingPermutations(fundamentalUnitsTypeDimension, unitSystem, new ArrayList<>(subsetFundamentalUnitTypeDimension.keySet()), 0);
                break;
            }

            if(unitResultFromPermutationOfPartition != null) {
                //Reduce exponent based on dimensions that were matched. The remaining dimension items will be reprocessed for additional matches.
                fundamentalUnitsTypeDimension = DimensionOperators.removeDimensionItemsRaisedToZero(DimensionOperators.divide(fundamentalUnitsTypeDimension, unitResultFromPermutationOfPartition.getFundamentalUnitTypesDimension()));
                unitsMatchedFromPartitionChunks.add(unitResultFromPermutationOfPartition);
            }
        }

        Map<String, Double> componentUnitDimensionFromRecombinationOfPartitions = new HashMap<>();
        for(Unit componentUnitFromPartitions:unitsMatchedFromPartitionChunks)
            DimensionOperators.alterExponentOfDimensionItem(componentUnitDimensionFromRecombinationOfPartitions, componentUnitFromPartitions.getName(), 1.0);

        return componentUnitDimensionFromRecombinationOfPartitions;
    }
    private Unit determineReducedMatchedUnitByFundamentalUnitsTypeDimensionUsingUsingPermutations(Map<UNIT_TYPE, Double> fundamentalUnitsTypeDimension, final String unitSystem, List<UNIT_TYPE> keyList, int currentKeyListIndex){
       //TODO: Make this more efficient by attempting to make use of better dynamic programming techniques and memoization where appropriate.
       //TODO: Future, create a custom class that inherits from iterator that keeps track of the successive permutation matches

        if(currentKeyListIndex < keyList.size()){
            UNIT_TYPE currentUnitType = keyList.get(currentKeyListIndex);
            int nextKeyListIndex = currentKeyListIndex++;
            for(Double permutatedExponent = fundamentalUnitsTypeDimension.get(currentUnitType); permutatedExponent > 0; permutatedExponent--){
                fundamentalUnitsTypeDimension.put(currentUnitType, permutatedExponent);
                Unit matchedUnit = determineReducedMatchedUnitByFundamentalUnitsTypeDimensionUsingUsingPermutations(fundamentalUnitsTypeDimension, unitSystem, keyList, nextKeyListIndex);
                if(matchedUnit != null)
                    return  matchedUnit;
            }
            return null;
        }
        else{
            Collection<Unit> unitsWithSameFundamentalDimension = unitsDataModel.getUnitsContentQuerier().queryUnitsByFundamentalUnitTypeDimension(fundamentalUnitsTypeDimension);
            return findSmallestDimensionUnitWithSameUnitSystem(unitsWithSameFundamentalDimension, unitSystem);
        }
    }
    /**
     * Finds the unit with smallest dimension and that is exclusively of the specified unit system.
     * For example, if "SI" was the specified unit system, then a unit with a combined unit system of "SI and Metric" would not be selected.
     * @return Smallest dimension unit that meet criteria or null
     */
    private Unit findSmallestDimensionUnitWithSameUnitSystem(Collection<Unit> unitsToBeCompared, String unitSystem){
        if(unitsToBeCompared.isEmpty())
            return null;

        //If result set is large enough, sort such that units with the smallest dimension and exclusively of the specified unit system bubble up and take precedence.
        Unit candidateUnit = Collections.min(unitsToBeCompared, new Comparator<Unit>() {
            @Override
            public int compare(Unit lhsUnit, Unit rhsUnit) {
                boolean lhsExclusivelyMatchesUnitSystem = lhsUnit.getUnitSystem().equalsIgnoreCase(unitSystem);
                boolean rhsExclusivelyMatchesUnitSystem = rhsUnit.getUnitSystem().equalsIgnoreCase(unitSystem);

                if (lhsExclusivelyMatchesUnitSystem && rhsExclusivelyMatchesUnitSystem) {
                    boolean lhsDimensionIsComplex = componentUnitsDimensionParser.getDimensionParserBuilder().getDimensionComponentDefiner().hasComplexDimensions(lhsUnit.getName());
                    boolean rhsDimensionIsComplex = componentUnitsDimensionParser.getDimensionParserBuilder().getDimensionComponentDefiner().hasComplexDimensions(rhsUnit.getName());

                    int dimensionComplexityComparison = Boolean.compare(lhsDimensionIsComplex, rhsDimensionIsComplex);

                    if (dimensionComplexityComparison != 0)
                        return Double.compare(lhsUnit.getComponentUnitsDimension().size(), rhsUnit.getComponentUnitsDimension().size());
                    else
                        return dimensionComplexityComparison;
                } else {
                    return Boolean.compare(lhsExclusivelyMatchesUnitSystem, rhsExclusivelyMatchesUnitSystem);
                }
            }
        });

        if (candidateUnit.getUnitSystem().equalsIgnoreCase(unitSystem) )
            return candidateUnit;
        else
            return null;
    }

    ///

    /**
     * Determine if no other units in data model have the same category, or component or fundamental units dimension.
     */
    public boolean determineIfUnitIsUnique(Unit unit){
       if(unitsDataModel.getUnitsContentQuerier().queryUnitsByUnitCategory(unit.getCategory()).isEmpty())
           return true;

       if(unitsDataModel.getUnitsContentQuerier().queryUnitsByComponentUnitsDimension(unit.getComponentUnitsDimension(), true).isEmpty())
           return true;

       return false;
    }

    ///
    public void setUnitsDataModel(UnitsDataModel unitsDataModel) {
        this.unitsDataModel = unitsDataModel;
    }
    public void setPluralTextParser(PluralTextParser pluralTextParser) {
        this.pluralTextParser = pluralTextParser;
    }
    public void setComponentUnitsDimensionParser(ComponentUnitsDimensionParser componentUnitsDimensionPatser) {
        this.componentUnitsDimensionParser = componentUnitsDimensionPatser;
    }
    public void setComponentUnitsDimensionSerializer(ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer) {
        this.componentUnitsDimensionSerializer = componentUnitsDimensionSerializer;
    }
}
