package com.isaacapps.unitconverterapp.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.ConversionFavoritesDataModel;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsContentQuerier;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.DimensionComponentDefiner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MultiAutoCompleteUnitsDefinitionArrayAdapter extends ArrayAdapter {
    public static final String MULTI_AUTO_COMPLETE_UNIT_NAME_DELIMITER = " :: ";

    private final UnitsContentQuerier unitsContentQuerier;
    private final DimensionComponentDefiner dimensionComponentDefiner;
    private final Comparator<Unit> conversionFavoritesRankComparator;
    private final Filter unitDefinitionFilter;

    public MultiAutoCompleteUnitsDefinitionArrayAdapter(Context context, int resource, UnitsContentQuerier unitsContentQuerier, DimensionComponentDefiner dimensionComponentDefiner, ConversionFavoritesDataModel conversionFavoritesDataModel) {
        super(context, resource);

        this.setNotifyOnChange(false);

        this.unitsContentQuerier = unitsContentQuerier;
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
    }

    @Override
    public Filter getFilter(){
        return unitDefinitionFilter;
    }

    /**
     * Updates the content of the array adapter with the sorted list unit names that satisfy the provided reference property text.
     * The following is the search order of precedence: by name, then by category, and then by unit system.
     * If the searching by category or unit system, then the result is order by sum of relevant conversion favorite ranks.
     * @param referenceUnitPropertyText Text that can be of various unit properties. Can be a unit name, unit category, or unit system.
     */
    private Collection<String> retrieveSimilarFormattedUnitNames(String referenceUnitPropertyText){
        List<String> validFormattedUnitNames = new ArrayList<>();

        Collection<Unit> unitCandidatesByName = unitsContentQuerier.queryUnitsOrderedBySimilarNames(referenceUnitPropertyText);
        validFormattedUnitNames.addAll(transformCandidateUnitsToFormattedUnitNames(unitCandidatesByName));

        if(validFormattedUnitNames.isEmpty()){
            List<Unit> unitCandidatesByUnitCategory = unitsContentQuerier.queryUnitsByUnitCategory(referenceUnitPropertyText);
            Collections.sort(unitCandidatesByUnitCategory, conversionFavoritesRankComparator);
            validFormattedUnitNames.addAll(transformCandidateUnitsToFormattedUnitNames(unitCandidatesByUnitCategory));
        }

        if(validFormattedUnitNames.isEmpty()){
            List<Unit> unitCandidatesByUnitSystem = unitsContentQuerier.queryUnitsByUnitSystem(referenceUnitPropertyText);
            Collections.sort(unitCandidatesByUnitSystem, conversionFavoritesRankComparator);
            validFormattedUnitNames.addAll(transformCandidateUnitsToFormattedUnitNames(unitCandidatesByUnitSystem));
        }

        return validFormattedUnitNames;
    }
    /**
     * Using a provided list of units, attempts to return a list unit name display of the following  format "{unit full name} :: {unit abbreviation}".
     * Inorder to save display space, the abbreviation dimension is only included if unit name is not complex OR it is complex and the number of component units dimension items is less than 4.
     */
    private Collection<String> transformCandidateUnitsToFormattedUnitNames(Collection<Unit> unitCandidates){
        Collection<String> formattedUnitNameDisplays = new ArrayList<>();

        for(Unit unit:unitCandidates) {
            String formattedUnitNameDisplay = !dimensionComponentDefiner.hasComplexDimensions(unit.getName()) || unit.getComponentUnitsDimension().size() < 4
                    ? String.format("%s%s%s", unit.getName(), MULTI_AUTO_COMPLETE_UNIT_NAME_DELIMITER, unit.getAbbreviation())
                    : unit.getAbbreviation();
            formattedUnitNameDisplays.add(formattedUnitNameDisplay);
        }

        return formattedUnitNameDisplays;
    }

    private class UnitDefinitionFilter extends Filter{

        @Override
        protected FilterResults performFiltering(CharSequence constraintSequence) {
            FilterResults filterResults = new FilterResults();

            if(constraintSequence != null) {
                Collection<String> results = retrieveSimilarFormattedUnitNames(constraintSequence.toString());
                filterResults.values = results;
                filterResults.count = results.size();
            }

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraintSequence, FilterResults results) {
            //
            clear();
            if(results.values != null) {
                addAll((Collection<String>) results.values);
                notifyDataSetChanged();
            }
        }
    }
}
