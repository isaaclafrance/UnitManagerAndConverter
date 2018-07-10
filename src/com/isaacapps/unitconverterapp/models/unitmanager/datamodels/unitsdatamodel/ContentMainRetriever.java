package com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel;

import com.isaacapps.unitconverterapp.models.measurables.unit.PrefixedUnit;
import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.processors.parsers.ParsingException;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.componentunits.ComponentUnitsDimensionParser;
import com.isaacapps.unitconverterapp.processors.parsers.generaltext.PluralTextParser;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.componentnunit.ComponentUnitsDimensionSerializer;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.fundamentalunit.FundamentalUnitTypesDimensionSerializer;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ContentMainRetriever {
    private UnitsDataModel unitsDataModel;

    private Locale locale;
    private PluralTextParser pluralTextParser;
    private ComponentUnitsDimensionParser componentUnitsDimensionParser;
    private ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer;
    private FundamentalUnitTypesDimensionSerializer fundamentalUnitTypesDimensionSerializer;

    public ContentMainRetriever() {
    }

    ///
    public Unit getUnit(String unitName) {
        return getUnit(unitName, true);
    }

    public Unit getUnit(String unitName, boolean createMissingUnits) {
        Map<String, Double> unitNameAsDimensionMap;
        String wellFormattedUnitName;

        //
        try {
            unitNameAsDimensionMap = componentUnitsDimensionParser.parse(unitName);
            wellFormattedUnitName = componentUnitsDimensionSerializer.serialize(unitNameAsDimensionMap); //Cleans up the unit definition into a standardized dimension format that include parentheses where appropriate.
        }
        catch(Exception e){
            wellFormattedUnitName = unitName.trim();

            unitNameAsDimensionMap = new HashMap<>();
            unitNameAsDimensionMap.put(wellFormattedUnitName, 1.0);
        }

        //First attempt to search for unit by full name or abbreviation
        Unit unit = unitsDataModel.getRepositoryWithDualKeyNCategory().getFirstItemByAnyKey(wellFormattedUnitName);
        if (unit != null)
            return unit;

        //If the name has any plural components, then perform search again with those components singularized.
        if (pluralTextParser.hasPossiblePlural(wellFormattedUnitName, true))
            return getUnit(unitsDataModel.getContentDeterminer().determineSingularOfUnitName(wellFormattedUnitName));

        /*Fly Weight design pattern.
         *If unit manager does not already contain unit with exact complex component dimension specified by unit name,
         *then added a newly created version of such unit and store it in the unit manager for easy future access
         */
        if (componentUnitsDimensionParser.getDimensionParserBuilder().getDimensionComponentDefinerBuilder().hasComplexDimensions(wellFormattedUnitName) && createMissingUnits) {
            try {
                Collection<Unit> complexUnitMatches = unitsDataModel.getContentQuerier().getUnitsByComponentUnitsDimension(componentUnitsDimensionParser.parse(wellFormattedUnitName), false);

                if (complexUnitMatches.isEmpty()) {
                    return unitsDataModel.getContentModifier().addUnit(new Unit(unitNameAsDimensionMap, false));

                } else {
                    return complexUnitMatches.iterator().next();
                }
            } catch (ParsingException e) {
                return getUnknownUnit();
            }
        }

        /*Tries to break up the unit name into a prefix component and viable unit name component.
         *Then constructs a new prefixed unit to be added to a repository
         *so that it will be immediately recognizable and retrieved the next time such a unit is required.
         */
        List<String[]> prefixMatches = unitsDataModel.getUnitManagerContext().getPrefixesDataModel().getPrefixMatches(wellFormattedUnitName, true);
        if (!prefixMatches.isEmpty() && createMissingUnits)
        {
            String prefixFullName = prefixMatches.get(0)[0];
            String prefixAbbreviation = prefixMatches.get(0)[1];

            String prefixlessUnitName = wellFormattedUnitName.contains(prefixFullName)
                    ? wellFormattedUnitName.replace(prefixFullName, "")
                    : wellFormattedUnitName.replace(prefixAbbreviation, "");

            double prefixValue = unitsDataModel.getUnitManagerContext().getPrefixesDataModel().getPrefixValue(prefixAbbreviation);

            return unitsDataModel.getContentModifier().addUnit(new PrefixedUnit(prefixFullName, prefixAbbreviation
                    , prefixValue, getUnit(prefixlessUnitName), false
                    , locale, componentUnitsDimensionSerializer, fundamentalUnitTypesDimensionSerializer ));
        }

        return getUnknownUnit();
    }

    /**
     * Attempts to find a stored abbreviation for the specified name.
     * If not found then create a abbreviation by taking only first letter of every word following a white space or an underscore.
     */
    public String getAbbreviation(String unitName) {
        String abbreviation = unitsDataModel.getRepositoryWithDualKeyNCategory().getKey2FromKey1(unitName);

        if (abbreviation == null)
            abbreviation = unitName.replaceAll("(?<=[a-zA-Z])[a-zA-Z]+", "").replaceAll("[ _]+", "");

        return abbreviation;
    }

    ///
    public Unit getUnknownUnit() {
        return unitsDataModel.getRepositoryWithDualKeyNCategory().getFirstItemByAnyKey(Unit.UNKNOWN_UNIT_NAME);
    }

    ///Bulk Retrieval
    public Collection<Unit> getCoreUnits() {
        return unitsDataModel.getRepositoryWithDualKeyNCategory().getItemsByCategory(ContentDeterminer.DATA_MODEL_CATEGORY.CORE);
    }

    public Collection<Unit> getDynamicUnits() {
        return unitsDataModel.getRepositoryWithDualKeyNCategory().getItemsByCategory(ContentDeterminer.DATA_MODEL_CATEGORY.DYNAMIC);
    }

    public Collection<Unit> getBaseUnits() {
        return unitsDataModel.getRepositoryWithDualKeyNCategory().getItemsByCategory(ContentDeterminer.DATA_MODEL_CATEGORY.BASE);
    }

    public Collection<Unit> getAllUnits() {
        return unitsDataModel.getRepositoryWithDualKeyNCategory().getAllItems();
    }

    public Collection<String> getAllUnitFullNames(){
        return unitsDataModel.getRepositoryWithDualKeyNCategory().getAllKey1s();
    }

    public Collection<String> getAllUnitAbbreviatedNames(){
        return unitsDataModel.getRepositoryWithDualKeyNCategory().getAllKey2s();
    }

    ///
    public void setUnitsDataModel(UnitsDataModel unitsDataModel) {
        this.unitsDataModel = unitsDataModel;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public void setPluralTextParser(PluralTextParser pluralTextParser) {
        this.pluralTextParser = pluralTextParser;
    }

    public void setComponentUnitsDimensionParser(ComponentUnitsDimensionParser componentUnitsDimensionParser) {
        this.componentUnitsDimensionParser = componentUnitsDimensionParser;
    }

    public void setComponentUnitsDimensionSerializer(ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer){
        this.componentUnitsDimensionSerializer = componentUnitsDimensionSerializer;
    }

    public void setFundamentalUnitTypesDimensionSerializer(FundamentalUnitTypesDimensionSerializer fundamentalUnitTypesDimensionSerializer){
        this.fundamentalUnitTypesDimensionSerializer = fundamentalUnitTypesDimensionSerializer;
    }
}
