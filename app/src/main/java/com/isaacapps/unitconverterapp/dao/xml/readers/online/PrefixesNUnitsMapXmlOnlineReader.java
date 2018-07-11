package com.isaacapps.unitconverterapp.dao.xml.readers.online;

import android.content.Context;

import com.isaacapps.unitconverterapp.dao.xml.readers.AsyncXmlReader;
import com.isaacapps.unitconverterapp.models.measurables.unit.PrefixedUnit;
import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManagerBuilder;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.PrefixesDataModel;
import com.isaacapps.unitconverterapp.processors.formatters.numbers.RoundingFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.text.GeneralTextFormatter;
import com.isaacapps.unitconverterapp.processors.parsers.ParsingException;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.DimensionParserBuilder;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.componentunits.ComponentUnitsParsedDimensionUpdater;
import com.isaacapps.unitconverterapp.processors.serializers.SerializingException;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.componentnunit.ComponentUnitsDimensionSerializer;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.fundamentalunit.FundamentalUnitTypesDimensionSerializer;
import com.isaacapps.unitconverterapp.utilities.RegExUtility;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import static com.isaacapps.unitconverterapp.processors.parsers.dimension.DimensionComponentDefinerBuilder.DEFAULT_EXPONENT_SYMBOL_GROUPS;

//Completely ignores XML namespaces.
///According to official Google Android documentation, the XmlPullParser that reads one tag at a time is the most efficient way of parsing especially in situations where there are a large number of tags.
public class PrefixesNUnitsMapXmlOnlineReader extends AsyncXmlReader<UnitManagerBuilder, UnitManagerBuilder> {

    private final Locale locale;
    private final ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer;
    private final FundamentalUnitTypesDimensionSerializer fundamentalUnitTypesDimensionSerializer;
    private final List<Unit> partiallyConstructedUnits;
    private final Map<String, Unit> rawAbbreviatedNameBaseUnitsMap;
    private final Map<String, Unit> rawAbbreviatedNameNonBaseUnitsMap;
    private final PrefixesDataModel prefixesDataModel;
    private final DimensionParserBuilder<String> customDimensionParserBuilder;

    public PrefixesNUnitsMapXmlOnlineReader(Context context, Locale locale, PrefixesDataModel prefixesDataModel
            , ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer, FundamentalUnitTypesDimensionSerializer fundamentalUnitTypesDimensionSerializer) {
        super(context);
        this.locale = locale;
        this.componentUnitsDimensionSerializer = componentUnitsDimensionSerializer;
        this.fundamentalUnitTypesDimensionSerializer = fundamentalUnitTypesDimensionSerializer;
        this.prefixesDataModel = prefixesDataModel;

        partiallyConstructedUnits = new ArrayList<>();
        rawAbbreviatedNameBaseUnitsMap = new HashMap<>();
        rawAbbreviatedNameNonBaseUnitsMap = new HashMap<>();

        /*Name match requirements: 1. letters and underscore 2. words can optionally be to the left of the left bracket. 3. Can be a digits optionally followed by a star or carrot symbol with no letter present.
		 *Exponent match requirements: numeric can only be found to the right of right bracket if there are any in matching group.
		                               Must be immediately to the right of the star symbol if any exists in order to accommodate dimensionless parsed units
		 *[period] symbol represents multiplication */
        customDimensionParserBuilder = new DimensionParserBuilder<String>()
                .setAtomicTypeRegEx("\\w*\\[\\w+\\]|[a-zA-Z_]+|^\\d+[^*]?$")
                .setDivisionSymbols(new String[]{"/"})
                .setMultiplicationSymbols(new String[]{"."})
                .setExponentSymbols(DEFAULT_EXPONENT_SYMBOL_GROUPS)
                .setExponentValueRegEx(RegExUtility.SIGNED_DOUBLE_VALUE_REGEX)
                .setExponentValueFormatter(new RoundingFormatter(Locale.ENGLISH, 1))
                .setParsedDimensionUpdater(new ComponentUnitsParsedDimensionUpdater())
                .setAtomicTypeFormatter(new GeneralTextFormatter(Locale.ENGLISH));
    }

    ///
    @Override
    protected UnitManagerBuilder readEntity(XmlPullParser parser) throws XmlPullParserException, IOException, ParsingException, SerializingException {
        //
        initialPopulationOfUnitAndPrefixDataStructures(parser);

        //
        completeUnitConstruction();

        //
        return new UnitManagerBuilder().addBaseUnits(new ArrayList<>(rawAbbreviatedNameBaseUnitsMap.values()))
                .addNonBaseUnits(new ArrayList<>(rawAbbreviatedNameNonBaseUnitsMap.values()))
                .addPrefixDataModel(prefixesDataModel);
    }

    ///
    private void initialPopulationOfUnitAndPrefixDataStructures(XmlPullParser parser) throws IOException, XmlPullParserException {
        Unit partiallyConstructedUnit;

        //
        String tagName = parser.getName();
        if (tagName.equalsIgnoreCase("root")) {
            parser.require(XmlPullParser.START_TAG, null, "root");
            String rootDescription = "Source: " + readAttribute(parser, "xmlns") + ". \nUpdate Date: " + readAttribute(parser, "revision-date");
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                tagName = parser.getName();
                if (tagName.equalsIgnoreCase("prefix")) {
                    partiallyConstructedUnit = null;
                    readPrefix(parser);
                } else if (tagName.equalsIgnoreCase("base-unit") || tagName.equalsIgnoreCase("unit")) {
                    try {
                        partiallyConstructedUnit = readUnit(parser, rootDescription, tagName.equalsIgnoreCase("base-unit"));
                    } catch (Exception e) {
                        partiallyConstructedUnit = null;
                    }
                } else {
                    partiallyConstructedUnit = null;
                    skip(parser);
                }

                if (partiallyConstructedUnit != null) {
					/*The raw information parsed from the xml connects the units using their code abbreviation name for conversion purposes; therefore, the abbreviation has greater
					 preference over the full name for storing and retrieving units, at least initially. */
                    if (partiallyConstructedUnit.isBaseUnit()) {
                        rawAbbreviatedNameBaseUnitsMap.put(partiallyConstructedUnit.getAbbreviation(), partiallyConstructedUnit);
                    } else {
                        rawAbbreviatedNameNonBaseUnitsMap.put(partiallyConstructedUnit.getAbbreviation(), partiallyConstructedUnit);
                    }
                    //Also adds constructed unit to a separate array for unit construction finalization later on.
                    partiallyConstructedUnits.add(partiallyConstructedUnit);
                }
            }
        }
    }

    ///
    private void completeUnitConstruction() throws ParsingException, SerializingException {
        /*Completes the construction of the previous constructed unit by adding missing elements
         * i.e. coreUnitState, conversion polynomial coefficients, and base unit.
         * Accomplishes this task by finding actual unit references for the base unit names now that all units have been created.
         */
        for (Unit partiallyConstructedUnit : partiallyConstructedUnits) {
            recalculatedBaseConversionPolyCoeffsFromComponentUnitsDimension(partiallyConstructedUnit);
            try {
                assignBaseUnit(partiallyConstructedUnit);
            } catch (ParsingException e) {
                continue;
            }
            assignCorrectedAbbreviatedComponentUnitsDimension(partiallyConstructedUnit);
            assignFullNameComponentUnitDimension(partiallyConstructedUnit, false);
            partiallyConstructedUnit.setCoreUnitState(true);
        }
        /*A separate iteration is necessary to clean the raw abbreviation since unit since processing in the previous iteration
         * requires all units have the raw abbreviation in order for them to be referenced correctly.
         * Example cleaning transformation: [ft] --> ft
         */
        for (Unit partiallyConstructedUnit : partiallyConstructedUnits) {
            partiallyConstructedUnit.setAbbreviation(partiallyConstructedUnit
                    .getAbbreviation().replaceAll("(?:[\\[\\]])", ""));
        }
    }

    private void assignBaseUnit(Unit partiallyConstructedUnit) throws ParsingException, SerializingException {
		/*Since the actually base unit may not have already been parsed during this unit's partial construction, only a dummy unit with the name of the base unit and the conversion was associated.
		 Therefore the actual base unit candidate needs to be retrieved. */
        partiallyConstructedUnit.setBaseUnit(getUnitFromAbbreviatedName(partiallyConstructedUnit.getBaseUnit().getAbbreviation(), true)
                , partiallyConstructedUnit.getBaseConversionPolyCoeffs());

        if (partiallyConstructedUnit.getBaseUnit().getCategory().equalsIgnoreCase(Unit.UNKNOWN_UNIT_CATEGORY)
                && !partiallyConstructedUnit.getCategory().equalsIgnoreCase(Unit.UNKNOWN_UNIT_CATEGORY)) {
            //Sometimes depending on how the units are constructed the base unit may need to get the category, unit system, description from its descendent.
            partiallyConstructedUnit.getBaseUnit().setCategory(partiallyConstructedUnit.getCategory());
            partiallyConstructedUnit.getBaseUnit().setUnitSystem(partiallyConstructedUnit.getUnitSystem());
            partiallyConstructedUnit.getBaseUnit().setDescription(partiallyConstructedUnit.getDescription());
        }
    }

    private void assignCorrectedAbbreviatedComponentUnitsDimension(Unit partiallyConstructedUnit) {
        boolean hasManyComponents = partiallyConstructedUnit.getComponentUnitsDimension().size() > 1;
        boolean baseConversionIsIdentity = partiallyConstructedUnit.getBaseConversionPolyCoeffs()[0] == 1.0;
        boolean hasAnExponentialIdentity = partiallyConstructedUnit.getComponentUnitsDimension().containsValue(1.0);

        if ((hasManyComponents || !hasAnExponentialIdentity) && !baseConversionIsIdentity || !hasManyComponents && hasAnExponentialIdentity) {
            partiallyConstructedUnit.clearComponentUnitsDimension(false);
            partiallyConstructedUnit.addComponentUnit(partiallyConstructedUnit.getAbbreviation(), 1.0, false);
        }
    }

    private void assignFullNameComponentUnitDimension(Unit partiallyConstructedUnit, boolean recreateUnitName) throws ParsingException, SerializingException {
        //Transforms abbreviated component units dimension into its equivalent full name dimension.
        Map<String, Double> abbreviatedComponentUnitsDimension = new HashMap<>(partiallyConstructedUnit.getComponentUnitsDimension());
        partiallyConstructedUnit.clearComponentUnitsDimension(false);

        for (Entry<String, Double> componentEntry : abbreviatedComponentUnitsDimension.entrySet()) {
            partiallyConstructedUnit.addComponentUnit(getUnitFromAbbreviatedName(componentEntry.getKey(), false).getName()
                    , componentEntry.getValue()
                    , recreateUnitName
                            //recreate using component unit name dimension if name already consistent of dimensions
                            || partiallyConstructedUnit.getName().matches("(\\w+[/*])+\\w+"));
        }
    }

    private Unit getUnitFromAbbreviatedName(String abbreviatedName, boolean createMissingUnits) throws ParsingException, SerializingException {
        String abbreviatedNameWithNoConversionFactors = abbreviatedName.replaceAll("[*/]\\d+", ""); //Strip any conversion factors if any exists, which may hinder map search.

        //Roundabout way of using existing methods to put it in format "[*/](a)^(#)". Some previously stored units with complex combinations will be stored as such.
        String abbreviatedNameWithNoConversionFactorsWithParen = componentUnitsDimensionSerializer.serialize(customDimensionParserBuilder.parse(abbreviatedNameWithNoConversionFactors));

        Unit unit = rawAbbreviatedNameBaseUnitsMap.get(abbreviatedNameWithNoConversionFactorsWithParen);
        if (unit != null)
            return unit;

        unit = rawAbbreviatedNameNonBaseUnitsMap.get(abbreviatedNameWithNoConversionFactorsWithParen);
        if (unit != null)
            return unit;

        if (createMissingUnits) { //Very likely unit name is composed of a complex component unit dimension combination of regular and prefixed unit abbreviations.
            //During decomposition, create any missing prefixed units where necessary.
            return createMissingUnitsFromComponentUnitsDimensionString(abbreviatedNameWithNoConversionFactors);
        } else {
            return null;
        }
    }

    private Unit createMissingUnitsFromComponentUnitsDimensionString(String abbreviatedComponentUnitsDimensionString) throws ParsingException, SerializingException {
        Map<String, Double> componentUnitsDimension = customDimensionParserBuilder.parse(abbreviatedComponentUnitsDimensionString);
        Unit createdPrefixedUnit = null;

        for (Entry<String, Double> abbreviatedCompUnitEntry : componentUnitsDimension.entrySet()) {
            if (getUnitFromAbbreviatedName(abbreviatedCompUnitEntry.getKey(), false) == null)
                createdPrefixedUnit = createMissingPrefixedUnitFromCandidateName(abbreviatedCompUnitEntry.getKey());
        }

        //If there is only one component unit and the component unit is a prefixed unit, then the unit was already added
        //to the non-base unit map by createMissingPrefixedUnitFromCandidateName method. Therefore, there is no need to add it again as base unit.
        if (!(createdPrefixedUnit != null && componentUnitsDimension.size() == 1)) {
            Unit createdUnit = new Unit(componentUnitsDimension, true);
            convertComponentUnitsDimensionToConversionFactorsList(createdUnit.getComponentUnitsDimension());
            assignCorrectedAbbreviatedComponentUnitsDimension(createdUnit);

            rawAbbreviatedNameBaseUnitsMap.put(createdUnit.getAbbreviation(), createdUnit);

            assignFullNameComponentUnitDimension(createdUnit, true);
            createdUnit.setCoreUnitState(true);

            return createdUnit;
        } else {
            return createdPrefixedUnit;
        }
    }

    private Unit createMissingPrefixedUnitFromCandidateName(String candidatePrefixedAbbreviatedName) throws ParsingException, SerializingException {
		/*Identify the prefix  and construct the prefixed unit with the appropriate base conversion based on the prefix factor.
		To reduce complexity, the forced assumption is that the xml was parsed properly, therefore the candidateprefixedAbbreviatedName will contain a prefix from prefixesMap if it has any
		and the prefixlessFullName will be retrievable from abbreviationCodeToFullNameMap. Hence, in all cases the prefixed unit will be able to be created. */

        Unit createdPrefixedUnit = null;
        String prefixlessAbbreviatedName;

        for (String abbreviatedPrefix : prefixesDataModel.getAllPrefixAbbreviations()) {
            if (candidatePrefixedAbbreviatedName.indexOf(abbreviatedPrefix) != 0) { //make sure only matchable prefixes characters in front of the string will be replaced
                continue;
            }
            prefixlessAbbreviatedName = candidatePrefixedAbbreviatedName.replaceFirst(abbreviatedPrefix, "");

            if (getUnitFromAbbreviatedName(prefixlessAbbreviatedName, false) != null) {
                createdPrefixedUnit = new PrefixedUnit(prefixesDataModel.getPrefixFullName(abbreviatedPrefix)
                        , abbreviatedPrefix, prefixesDataModel.getPrefixValue(abbreviatedPrefix)
                        , getUnitFromAbbreviatedName(prefixlessAbbreviatedName, false), true
                        , locale, componentUnitsDimensionSerializer, fundamentalUnitTypesDimensionSerializer);

                rawAbbreviatedNameNonBaseUnitsMap.put(candidatePrefixedAbbreviatedName, createdPrefixedUnit);
            }
        }

        return createdPrefixedUnit;
    }

    private void recalculatedBaseConversionPolyCoeffsFromComponentUnitsDimension(Unit partiallyConstructedUnit) {
		/*Due to how the original xml is setup and consequently how it parsed, conversion factors can inadvertently be parsed as a component.
		Therefore, they have to be removed after being used to update the base conversion coefficient. */
        ArrayList<Double> conversionFactors = convertComponentUnitsDimensionToConversionFactorsList(partiallyConstructedUnit.getComponentUnitsDimension());
        for (Double conversionFactor : conversionFactors) {
            partiallyConstructedUnit.getBaseConversionPolyCoeffs()[0] *= Math.abs((conversionFactor < 0.0 ? 1 / conversionFactor : conversionFactor));
        }
    }

    private ArrayList<Double> convertComponentUnitsDimensionToConversionFactorsList(Map<String, Double> componentUnitsDimension) {
        ArrayList<Double> conversionFactors = new ArrayList<>();

        Iterator<Entry<String, Double>> dimensionEntryIterator = componentUnitsDimension.entrySet().iterator();
        Entry<String, Double> dimensionEntry;
        while (dimensionEntryIterator.hasNext()) {
            dimensionEntry = dimensionEntryIterator.next();
            if (dimensionEntry.getKey().matches("\\d+")) {
                //Due to how the parsing was designed, when a conversion factor is picked up as a component unit as consequence of how the original xml was designed, the key and values pairs have the same absolute number.
                conversionFactors.add(Math.abs((dimensionEntry.getValue() < 0.0 ? 1 / dimensionEntry.getValue() : dimensionEntry.getValue())));
                dimensionEntryIterator.remove();
            }
        }

        return conversionFactors;
    }

    ///
    private void readPrefix(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "prefix");

        String tagName, fullPrefixName = "", abbreviatedPrefixName = "";
        Double prefixValue = 0.0;

        abbreviatedPrefixName = readAttribute(parser, "CODE");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            tagName = parser.getName();
            if (tagName.equalsIgnoreCase("name")) {
                fullPrefixName = readText(parser);
            } else if (tagName.equalsIgnoreCase("value")) {
                prefixValue = Double.valueOf(readAttribute(parser, "value"));
                skip(parser); //only attributes were being read which does not automatically advance the parser, therefore the advancement has to be forced.
            } else {
                skip(parser);
            }
        }

        prefixesDataModel.addCorePrefix(fullPrefixName, abbreviatedPrefixName, prefixValue);
    }

    private Unit readUnit(XmlPullParser parser, String rootDescription, boolean isBaseUnit) throws XmlPullParserException, IOException, ParsingException {
        parser.require(XmlPullParser.START_TAG, null, (isBaseUnit) ? "base-unit" : "unit");

        Map<String, double[]> baseUnitNConversionPolyCoeffs = new HashMap<>();
        Map<String, Double> abbreviatedComponentUnitsDimension = new HashMap<>();
        String unitName = "", unitCategory = "", tagName = "";

        String rawUnitAbbreviation = readRawUnitAbbreviation(parser, isBaseUnit);
        String unitSystem = (isBaseUnit) ? "si" : readUnitSystem(parser);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            tagName = parser.getName();
            if (tagName.equalsIgnoreCase("name") && unitName.isEmpty()) {
                unitName = readUnitName(parser);
            } else if (tagName.equalsIgnoreCase("property")) {
                unitCategory = readUnitCategory(parser);
            } else if (tagName.equalsIgnoreCase("value") && !isBaseUnit) {
                baseUnitNConversionPolyCoeffs = readBaseUnitNConversionPolyCoeffs(parser); //Issues may arise when the the conversion is not a simple conversion factor but is instead a complex function with some specific argument. ex. sqrt, log
                abbreviatedComponentUnitsDimension = readAbbreviatedComponentUnitsDimension(parser); //Issues can arise when trying to parse constants represented as units in the xml since there are component units with only numbers as unit names instead of alphabets.

                skip(parser); //only attributes were being read which does not automatically advance parser, there it has to be forced.
            } else {
                skip(parser);
            }
        }

        //Only return a created unit: Issue had not arose when constructing the baseUnitNConversionPolyCoeffs and abbreviatedComponentUnitsDimension for non base units.
        if ((abbreviatedComponentUnitsDimension.size() == 0
                || baseUnitNConversionPolyCoeffs.size() == 0) && !isBaseUnit) {
            return null;
        } else {
            Unit createdUnit = new Unit(unitName, new HashSet<>(), unitCategory, rootDescription, unitSystem, rawUnitAbbreviation, abbreviatedComponentUnitsDimension
                    , isBaseUnit ? new Unit() : new Unit(baseUnitNConversionPolyCoeffs.keySet().iterator().next(), new HashMap(), true)
                    , isBaseUnit ? new double[]{1.0, 0.0} : baseUnitNConversionPolyCoeffs.values().iterator().next(), locale, componentUnitsDimensionSerializer, fundamentalUnitTypesDimensionSerializer);

            if (isBaseUnit) {
                createdUnit.setBaseUnit(createdUnit);
                createdUnit.addComponentUnit(createdUnit.getAbbreviation(), 1.0, false);
            }

            return createdUnit;
        }
    }

    /// Methods for retrieving entity components from XML
    private String readUnitName(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "name");
        String unitNameString = readText(parser).trim().replaceAll("\\s+", "_").replace("-", "_");
        parser.require(XmlPullParser.END_TAG, null, "name");
        return unitNameString;
    }

    private String readUnitSystem(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "unit");
        return readAttribute(parser, "class").split("-")[0]
                + (readAttribute(parser, "isMetric").equalsIgnoreCase("yes") ? "(metric)" : "")
                + (readAttribute(parser, "isAbitrary").equalsIgnoreCase("yes") ? "(abitrary)" : "")
                + (readAttribute(parser, "isSpecial").equalsIgnoreCase("yes") ? "(special)" : "");
    }

    private String readRawUnitAbbreviation(XmlPullParser parser, boolean isBaseUnit) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, isBaseUnit ? "base-unit" : "unit");
        return readAttribute(parser, "Code");
    }

    private String readUnitCategory(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "property");
        String unitCategory = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "property");
        return unitCategory;
    }

    private Map<String, Double> readAbbreviatedComponentUnitsDimension(XmlPullParser parser) throws XmlPullParserException, IOException, ParsingException {
        parser.require(XmlPullParser.START_TAG, null, "value");
        return customDimensionParserBuilder.parse(readAttribute(parser, "Unit"));
    }

    private Map<String, double[]> readBaseUnitNConversionPolyCoeffs(XmlPullParser parser) throws XmlPullParserException, IOException {
        Map<String, double[]> conversionPolyCoeffsMap = new HashMap<>();
        parser.require(XmlPullParser.START_TAG, null, "value");
        conversionPolyCoeffsMap.put(readAttribute(parser, "Unit")
                , new double[]{Double.parseDouble(readAttribute(parser, "value")), 0.0});
        return conversionPolyCoeffsMap;
    }

    /// Loader Methods
    @Override
    public UnitManagerBuilder loadInBackground() {
        UnitManagerBuilder unitManagerBuilder = new UnitManagerBuilder();
        try {
            unitManagerBuilder = parseXML(openXmlFile("http://unitsofmeasure.org/ucum-essence.xml", true));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return unitManagerBuilder;
    }
}
