package com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel;

import com.isaacapps.unitconverterapp.models.measurables.unit.PrefixedUnit;
import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.measurables.unit.UnitException;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.PrefixesDataModel;
import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.componentunits.ComponentUnitsDimensionParser;
import com.isaacapps.unitconverterapp.processors.parsers.generaltext.PluralTextParser;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.componentnunit.ComponentUnitsDimensionSerializer;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.fundamentalunit.FundamentalUnitTypesDimensionSerializer;
import com.isaacapps.unitconverterapp.utilities.RegExUtility;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.isaacapps.unitconverterapp.utilities.RegExUtility.SIGNED_DOUBLE_VALUE_REGEX_PATTERN;

public class UnitsContentMainRetriever {
    private UnitsDataModel unitsDataModel;

    private Locale locale;
    private PluralTextParser pluralTextParser;
    private ComponentUnitsDimensionParser componentUnitsDimensionParser;
    private ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer;
    private FundamentalUnitTypesDimensionSerializer fundamentalUnitTypesDimensionSerializer;
    private IFormatter abbreviationFormatter;

    public UnitsContentMainRetriever() {
    }

    ///
    public Unit getUnit(String unitName) {
        return getUnit(unitName, true);
    }

    /**
     * Retrieves a unit instance having provided unit name / unit definition.
     * @param unformattedUnitName The name can be a a abbreviation or a full name, either of which can have prefixes.
     *        The name can also be a complex dimension consisting of a combination of units joined by the multiplication and/or division operator.
     * @param createMissingComplexValidUnits Indicates whether to created a new unit from the unit combination definition, but only if none of the units in the combination are unknown.
     */
    public Unit getUnit(String unformattedUnitName, boolean createMissingComplexValidUnits) {
        Unit unit;

        //First attempt to search for unit by unformatted full name or abbreviation
        if ((unit = unitsDataModel.getRepositoryWithDualKeyNCategory().getFirstItemByAnyKey(unformattedUnitName)) != null
                || (unit = unitsDataModel.getRepositoryWithDualKeyNCategory().getFirstItemByAnyKey(unformattedUnitName.toLowerCase().trim())) != null)
        {
            return unit;
        }

        //
        String wellFormattedUnitName;
        Map<String, Double> unitNameAsDimensionMap;
        try {
            unitNameAsDimensionMap = componentUnitsDimensionParser.parse(unformattedUnitName.toLowerCase());

            if(unitNameAsDimensionMap.containsKey(Unit.UNKNOWN_UNIT_NAME))
                return getUnknownUnit();

            //Cleans up the unit definition into a standardized dimension format for easier recognition.
            //Could have used the DimensionFormatter abstraction, but needed to explicitly separate the parser part from the serializer part.
            wellFormattedUnitName = componentUnitsDimensionSerializer.serialize(unitNameAsDimensionMap);
        }
        catch(Exception e){
            unitNameAsDimensionMap = new HashMap<>();
            wellFormattedUnitName = unformattedUnitName.toLowerCase().trim();
        }

        if((unit = unitsDataModel.getRepositoryWithDualKeyNCategory().getFirstItemByAnyKey(wellFormattedUnitName)) != null)
            return unit;

        //If the name has any plural components, then perform search again with those components singularized.
        if (pluralTextParser.hasPossiblePlural(wellFormattedUnitName, true)) {
            String singularizedUnitDefinition = unitsDataModel.getUnitsContentDeterminer().determineSingularOfUnitName(wellFormattedUnitName);
            if(!singularizedUnitDefinition.equalsIgnoreCase(wellFormattedUnitName))
                return getUnit(singularizedUnitDefinition);
        }

        if(!createMissingComplexValidUnits)
            return getUnknownUnit();

        boolean hasComplexDimension = componentUnitsDimensionParser.getDimensionParserBuilder().getDimensionComponentDefiner().hasComplexDimensions(wellFormattedUnitName);

        //Determine if the name has any prefix and a valid unit name part.
        if (!hasComplexDimension) {
            String[] prefixMatches = unitsDataModel.getUnitManagerContext().getPrefixesDataModel().findFirstPrefixPairMatches(unformattedUnitName, true);
            if(prefixMatches != PrefixesDataModel.NO_PREFIX_MATCH_ARRAY)
                return createNewPrefixedUnit(prefixMatches);
            else
                return getUnknownUnit();
        }
        else{
            //Finally, determine if the name has some complex dimension components try matching against that.
            return getUnitBasedOnComplexDimension(unitNameAsDimensionMap);
        }
    }
    /**
     * If unit manager does not already contain unit with exact complex component dimension,
     * then a version of such unit newly created and stored in the unit manager for easy future access but only if all the component units map to existing units..
     */
    private Unit getUnitBasedOnComplexDimension(Map<String, Double> unitNameAsDimensionMap){
        if(unitNameAsDimensionMap.isEmpty())
            return getUnknownUnit();

        Collection<Unit> complexUnitMatches = unitsDataModel.getUnitsContentQuerier().queryUnitsByComponentUnitsDimension(unitNameAsDimensionMap, false);

        if (complexUnitMatches.isEmpty()) {
            try {
                if(allComponentUnitsAreKnown(unitNameAsDimensionMap)) {
                    return unitsDataModel.getUnitsContentModifier().addUnit(new Unit(unitNameAsDimensionMap));
                }
                else{
                    return getUnknownUnit();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return getUnknownUnit();
            }

        } else {
            return complexUnitMatches.iterator().next();
        }
    }
    private boolean allComponentUnitsAreKnown(Map<String, Double> componentUnitsDimension){
        for(String componentUnitName:componentUnitsDimension.keySet()){
            if(!unitsDataModel.getUnitsContentQuerier().containsUnit(componentUnitName)
                    && !SIGNED_DOUBLE_VALUE_REGEX_PATTERN.matcher(componentUnitName).matches())
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Constructs a new prefixed unit to be added to a repository
     * so that it will be immediately recognizable and retrieved the next time such a unit is required.
     */
    private Unit createNewPrefixedUnit(String[] prefixMatch){
        String prefixFullName = prefixMatch[0];
        String prefixAbbreviation = prefixMatch[1];
        String prefixlessUnitName = prefixMatch[2];

        double prefixValue = unitsDataModel.getUnitManagerContext().getPrefixesDataModel().getPrefixValue(prefixAbbreviation);

        try {
            return unitsDataModel.getUnitsContentModifier().addUnit(new PrefixedUnit(prefixFullName, prefixAbbreviation
                    , prefixValue, getUnit(prefixlessUnitName), false
                    , locale, componentUnitsDimensionSerializer, fundamentalUnitTypesDimensionSerializer
                    , componentUnitsDimensionParser.getDimensionParserBuilder().getDimensionComponentDefiner()));
        } catch (UnitException e) {
            return getUnknownUnit();
        }
    }

    /**
     * Attempts to find a stored abbreviation for the specified name.
     * If not found then create an abbreviation by taking only first letter of every word following a white space or an underscore.
     */
    public String getAbbreviation(String unitName) {
        String abbreviation = unitsDataModel.getRepositoryWithDualKeyNCategory().getKey2FromKey1(unitName);

        if (abbreviation != null)
            return abbreviation;

        return abbreviationFormatter.format(unitName);
    }

    ///
    public Unit getUnknownUnit() {
        return unitsDataModel.getRepositoryWithDualKeyNCategory().getFirstItemByAnyKey(Unit.UNKNOWN_UNIT_NAME);
    }

    ///Bulk Retrieval
    public Collection<Unit> getCoreUnits() {
        return unitsDataModel.getRepositoryWithDualKeyNCategory().getItemsByCategory(UnitsContentDeterminer.DATA_MODEL_CATEGORY.CORE);
    }
    public Collection<Unit> getDynamicUnits() {
        return unitsDataModel.getRepositoryWithDualKeyNCategory().getItemsByCategory(UnitsContentDeterminer.DATA_MODEL_CATEGORY.DYNAMIC);
    }
    public Collection<Unit> getBaseUnits() {
        return unitsDataModel.getRepositoryWithDualKeyNCategory().getItemsByCategory(UnitsContentDeterminer.DATA_MODEL_CATEGORY.BASE);
    }
    public Collection<Unit> getUnknownUnits(){
        return unitsDataModel.getRepositoryWithDualKeyNCategory().getItemsByCategory(UnitsContentDeterminer.DATA_MODEL_CATEGORY.UNKNOWN);
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

    public void setAbbreviationFormatter(IFormatter abbreviationFormatter){
        this.abbreviationFormatter = abbreviationFormatter;
    }
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}
