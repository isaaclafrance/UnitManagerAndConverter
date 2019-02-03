package com.isaacapps.unitconverterapp.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.ConversionFavoritesDataModel;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.PrefixesDataModel;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsContentQuerier;
import com.isaacapps.unitconverterapp.processors.operators.dimensions.DimensionOperators;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.DimensionComponentDefiner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MultiAutoCompleteUnitsDefinitionArrayAdapter extends ArrayAdapter {
    public static final String DEFAULT_MULTI_AUTO_COMPLETE_UNIT_DISPLAY_DELIMITER = " :: ";

    private final UnitsContentQuerier unitsContentQuerier;
    private final PrefixesDataModel prefixesDataModel;
    private final DimensionComponentDefiner dimensionComponentDefiner;
    private final Comparator<Unit> conversionFavoritesRankComparator;
    private final Filter unitDefinitionFilter;

    private int maxResults;
    private int prefixTriggerLength;

    public MultiAutoCompleteUnitsDefinitionArrayAdapter(Context context, int resource, UnitsContentQuerier unitsContentQuerier
            , DimensionComponentDefiner dimensionComponentDefiner, ConversionFavoritesDataModel conversionFavoritesDataModel, PrefixesDataModel prefixesDataModel) {
        super(context, resource);

        this.unitsContentQuerier = unitsContentQuerier;
        this.prefixesDataModel = prefixesDataModel;
        this.dimensionComponentDefiner = dimensionComponentDefiner;
        this.unitDefinitionFilter = new UnitDefinitionFilter();

        this.conversionFavoritesRankComparator = new Comparator<Unit>() {
            @Override
            public int compare(Unit lhsUnit, Unit rhsUnit) {
                Collection<String> lhsConversionFavorites = conversionFavoritesDataModel.getFormattedConversionsAssociatedWithUnit(lhsUnit);
                Collection<String> rhsConversionFavorites = conversionFavoritesDataModel.getFormattedConversionsAssociatedWithUnit(rhsUnit);

                Collection<Integer> lhsConversionRanks = new ArrayList<>();
                for(String conversionFavorite:lhsConversionFavorites){
                    lhsConversionRanks.add(conversionFavoritesDataModel.getSignificanceRankOfConversion(conversionFavorite));
                }
                int sumOfLhsConversionRanks = 0;
                for(Integer conversionRank:lhsConversionRanks){
                    sumOfLhsConversionRanks += conversionRank;
                }

                Collection<Integer> rhsConversionRanks = new ArrayList<>();
                for(String conversionFavorite:rhsConversionFavorites){
                    lhsConversionRanks.add(conversionFavoritesDataModel.getSignificanceRankOfConversion(conversionFavorite));
                }
                int sumOfRhsConversionRanks = 0;
                for(Integer conversionRank:rhsConversionRanks){
                    sumOfRhsConversionRanks += conversionRank;
                }

                return -1 * Integer.compare(sumOfLhsConversionRanks, sumOfRhsConversionRanks);
            }
        };

        this.maxResults = 0;
        this.prefixTriggerLength = 2;
    }

    public int getMaxResults(){
        return maxResults;
    }

    /**
     * Max number of query results to return. If set to the value of zero or less than zero, then all results will return.
     */
    public void setMaxResults(int maxResults){
        this.maxResults = maxResults < 0 ? 0 : maxResults;
    }

    public int getPrefixTriggerLength(){
        return prefixTriggerLength;
    }
    /**
     * Prefixes how may characters after a successful prefix identification that unit name partial search should begin.
     */
    public void setPrefixTriggerLength(int prefixTriggerLength){
        this.prefixTriggerLength = prefixTriggerLength;
    }

    @Override
    public Filter getFilter(){
        return unitDefinitionFilter;
    }

    /**
     * Retrieves a sorted list of unit names that satisfy the provided reference property text.
     *
     * The following is the search order of precedence: by name, then by category, and then by unit system.
     * If searching by category or unit system, then the result is order by sum of relevant conversion favorite ranks.
     *
     * If there any prefixes (full or abbreviated) are identified and then the text following the prefix is put through a unit name similarity search
     * and the resulting name is recombined with the prefix. Make sure to only combine prefix fullnames with unit fullnames and prefix abbreviations with unit abbreviations.
     * @param referenceUnitPropertyText Text that can stand for various unit properties. It can be a unit name, unit category, or unit system.
     */
    private List<String> retrieveFormattedUnitNameDisplaysForSimilarUnits(String referenceUnitPropertyText){
        List<String> validFormattedUnitNameDisplays = new ArrayList<>();

        Collection<Unit> unitCandidatesByName = unitsContentQuerier.queryUnitsOrderedBySimilarNames(referenceUnitPropertyText); //already stored based on ratios of length and similarity
        validFormattedUnitNameDisplays.addAll(transformCandidateUnitsToFormattedUnitNameAbbreviationDisplay(unitCandidatesByName));

        //
        if(validFormattedUnitNameDisplays.isEmpty()){
            Collection<String[]> prefixMatches = prefixesDataModel.findAllPrefixPairMatches(referenceUnitPropertyText, false);
            Collection<Unit> unitCandidatesBySubpartName = new ArrayList<>();
            for(String[] prefixMatch : prefixMatches){

                String prefixFullName = prefixMatch[0];
                String prefixAbbreviation = prefixMatch[1];
                String unitNameSubpart = prefixMatch[2];

                if(unitNameSubpart.length() >= prefixTriggerLength){
                    unitCandidatesBySubpartName.addAll(unitsContentQuerier.queryUnitsOrderedBySimilarNames(unitNameSubpart));
                    validFormattedUnitNameDisplays.addAll(transformCandidateUnitsToFormattedPrefixedUnitNameAbbreviationDisplay(unitCandidatesBySubpartName, prefixFullName, prefixAbbreviation));
                }
            }
        }

        //
        if(validFormattedUnitNameDisplays.isEmpty()){
            List<Unit> unitCandidatesByUnitCategory = unitsContentQuerier.queryUnitsByUnitCategory(referenceUnitPropertyText);
            Collections.sort(unitCandidatesByUnitCategory, conversionFavoritesRankComparator);
            validFormattedUnitNameDisplays.addAll(transformCandidateUnitsToFormattedUnitNameCategoryDisplay(unitCandidatesByUnitCategory));
        }

        //
        if(validFormattedUnitNameDisplays.isEmpty()){
            List<Unit> unitCandidatesByUnitSystem = unitsContentQuerier.queryUnitsByUnitSystem(referenceUnitPropertyText);
            Collections.sort(unitCandidatesByUnitSystem, conversionFavoritesRankComparator);
            validFormattedUnitNameDisplays.addAll(transformCandidateUnitsToFormattedUnitNameUnitSystemDisplay(unitCandidatesByUnitSystem));
        }

        return validFormattedUnitNameDisplays;
    }

    /**
     * Using a provided list of units, attempts to return a list unit name display of the following  format "{unit full name} :: {unit abbreviation}".
     * Inorder to save display space, the abbreviation dimension is only included if unit name is not complex OR it is complex and the number of component units dimension items is less than 4.
     */
    private Collection<String> transformCandidateUnitsToFormattedUnitNameAbbreviationDisplay(Collection<Unit> unitCandidates){
        Collection<String> formattedUnitNameAbbreviationDisplays = new ArrayList<>();
        for(Unit unit:unitCandidates) {
            formattedUnitNameAbbreviationDisplays.add(constructFormattedUnitNameDisplay(unit, unit.getAbbreviation(), unit.getAbbreviation(), 4));
        }
        return formattedUnitNameAbbreviationDisplays;
    }
    /**
     * Using a provided list of units, attempts to return a dynamically constructed list unit name displays of the following  format "{full prefix}{unit full name} :: {abbreviated prefix}{unit abbreviation}".
     * Only units with non-complex names are used.
     */
    private Collection<String> transformCandidateUnitsToFormattedPrefixedUnitNameAbbreviationDisplay(Collection<Unit> unitCandidates, String fullPrefix, String abbreviatedPrefix){
        Collection<String> formattedPrefixedUnitNameAbbreviationDisplays = new ArrayList<>();
        for(Unit unit:unitCandidates){
            if(!dimensionComponentDefiner.hasComplexDimensions(unit.getName())){
                String formattedDisplay = String.format("%s%s%s%s%s", fullPrefix, unit.getName(), DEFAULT_MULTI_AUTO_COMPLETE_UNIT_DISPLAY_DELIMITER, abbreviatedPrefix, unit.getAbbreviation());
                formattedPrefixedUnitNameAbbreviationDisplays.add(formattedDisplay);
            }
        }
        return formattedPrefixedUnitNameAbbreviationDisplays;
    }
    /**
     * Using a provided list of units, attempts to return a list unit name display of the following  format "{unit full name} :: {unit category}".
     * Inorder to save display space, the abbreviation dimension is only included if unit name is not complex OR it is complex and the number of component units dimension items is less than 5.
     */
    private Collection<String> transformCandidateUnitsToFormattedUnitNameCategoryDisplay(Collection<Unit> unitCandidates){
        Collection<String> formattedUnitNameCategoryDisplays = new ArrayList<>();
        for(Unit unit:unitCandidates) {
            formattedUnitNameCategoryDisplays.add(constructFormattedUnitNameDisplay(unit, unit.getCategory(), unit.getAbbreviation(), 3));
        }
        return formattedUnitNameCategoryDisplays;
    }
    /**
     * Using a provided list of units, attempts to return a list unit name display of the following  format "{unit full name} :: {unit system}".
     * Inorder to save display space, the abbreviation dimension is only included if unit name is not complex OR it is complex and the number of component units dimension items is less than 5.
     */
    private Collection<String> transformCandidateUnitsToFormattedUnitNameUnitSystemDisplay(Collection<Unit> unitCandidates){
        Collection<String> formattedUnitNameUnitSystemDisplays = new ArrayList<>();
        for(Unit unit:unitCandidates) {
            formattedUnitNameUnitSystemDisplays.add(constructFormattedUnitNameDisplay(unit, unit.getUnitSystem(), unit.getAbbreviation(), 3));
        }
        return formattedUnitNameUnitSystemDisplays;
    }
    private String constructFormattedUnitNameDisplay(Unit unit, String secondaryUnitProperty, String replacementProperty, int dimensionLimit){
        if(!dimensionComponentDefiner.hasComplexDimensions(unit.getName()) || unit.getComponentUnitsDimension().size() < dimensionLimit) {
            return String.format("%s%s%s", unit.getName(), DEFAULT_MULTI_AUTO_COMPLETE_UNIT_DISPLAY_DELIMITER, secondaryUnitProperty);
        }else {
            return replacementProperty;
        }
    }

    private class UnitDefinitionFilter extends Filter{

        @Override
        protected FilterResults performFiltering(CharSequence constraintSequence) {
            FilterResults filterResults = new FilterResults();

            if(constraintSequence != null) {
                List<String> results = retrieveFormattedUnitNameDisplaysForSimilarUnits(constraintSequence.toString());
                List<String> subresult = maxResults < 1 ? results : results.subList(0, maxResults);
                filterResults.values = subresult;
                filterResults.count = subresult.size();
            }

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraintSequence, FilterResults results) {
            //
            clear();
            if(results.values != null) {
                addAll((Collection<String>) results.values);
            }
        }
    }
}
