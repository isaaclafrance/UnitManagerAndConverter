package com.isaacapps.unitconverterapp.dao.xml.readers.online;

import android.content.Context;

import com.isaacapps.unitconverterapp.dao.api.mediawiki.webservices.MediaWikiWebServicesApiRequester;
import com.isaacapps.unitconverterapp.dao.api.mediawiki.webservices.models.PageExtract;
import com.isaacapps.unitconverterapp.dao.api.mediawiki.webservices.models.PageExtractsSearchResult;
import com.isaacapps.unitconverterapp.dao.xml.readers.AsyncXmlReader;
import com.isaacapps.unitconverterapp.models.measurables.unit.PrefixedUnit;
import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.measurables.unit.UnitException;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManagerBuilder;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.PrefixesDataModel;
import com.isaacapps.unitconverterapp.processors.formatters.ChainedFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.dimensions.DimensionFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.numbers.RoundingFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.text.GeneralTextFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.text.LowercaseFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.text.SnakeCaseFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.text.StartCaseFormatter;
import com.isaacapps.unitconverterapp.processors.parsers.IParser;
import com.isaacapps.unitconverterapp.processors.parsers.ParsingException;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.DimensionComponentDefiner;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.DimensionParserBuilder;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.componentunits.ComponentUnitsParsedDimensionUpdater;
import com.isaacapps.unitconverterapp.processors.serializers.SerializingException;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.componentnunit.ComponentUnitsDimensionSerializer;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.fundamentalunit.FundamentalUnitTypesDimensionSerializer;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//Completely ignores XML namespaces.
///According to official Google Android documentation, the XmlPullParser that reads one tag at a time is the most efficient way of parsing especially in situations where there are a large number of tags.
public class PrefixesNUnitsMapXmlOnlineReader extends AsyncXmlReader<UnitManagerBuilder, UnitManagerBuilder> {

    private final static Matcher numericalComponentUnitNameMatcher = Pattern.compile("[./]\\d+").matcher("");
    private final static Matcher nameWithOperatorMatcher = Pattern.compile("(\\w+[/.])+\\w+").matcher("");

    private final Locale locale;
    private final PrefixesDataModel prefixesDataModel;
    private final List<Unit> partiallyConstructedUnits;
    private final Map<String, Unit> rawAbbreviatedNameBaseUnitsMap;
    private final Map<String, Unit> rawAbbreviatedNameNonBaseUnitsMap;

    private final IParser<Map<String, Double>> customComponentDimensionParser;
    private final DimensionComponentDefiner dimensionComponentDefiner;
    private final ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer;
    private final FundamentalUnitTypesDimensionSerializer fundamentalUnitTypesDimensionSerializer;
    private final DimensionFormatter componentDimensionFormatter;
    private final IFormatter unitSystemFormatter;
    private final IFormatter unitNameFormatter;

    private MediaWikiWebServicesApiRequester mediaWikiWebServicesApiRequester;

    private Unit defaultBaseUnit;

    public PrefixesNUnitsMapXmlOnlineReader(Context context, Locale locale, PrefixesDataModel prefixesDataModel
            , ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer, FundamentalUnitTypesDimensionSerializer fundamentalUnitTypesDimensionSerializer
            , DimensionComponentDefiner dimensionComponentDefiner) throws ParsingException {
        super(context);
        this.locale = locale;

        this.dimensionComponentDefiner = dimensionComponentDefiner;
        this.componentUnitsDimensionSerializer = componentUnitsDimensionSerializer;
        this.fundamentalUnitTypesDimensionSerializer = fundamentalUnitTypesDimensionSerializer;

        this.prefixesDataModel = prefixesDataModel;

        partiallyConstructedUnits = new ArrayList<>();
        rawAbbreviatedNameBaseUnitsMap = new LinkedHashMap<>();
        rawAbbreviatedNameNonBaseUnitsMap = new LinkedHashMap<>();

        /*Name match requirements: 1. letters and underscore
                                   2. words can optionally be to the left of the left bracket.
                                   3. Can be a digits optionally followed by a star or carrot symbol with no letter present.
		 *Exponent match requirement: 1. There is no exponent symbol.
		                             2. Exponent number must immediately follow atomic component.
		 *Multiplication: [period] symbol
		 *Division: Back slash */
        DimensionComponentDefiner customDimensionComponentDefiner = new DimensionComponentDefiner("(?:(?:\\w*\\[\\w+\\])|[a-zA-Z_']+|(?:\\d+[\\^*]?))");
        customDimensionComponentDefiner
                .setDivisionSymbols(new String[]{"/"})
                .setMultiplicationSymbols(new String[]{"."})
                .setExponentSymbols("");

        customComponentDimensionParser = new DimensionParserBuilder<String>(customDimensionComponentDefiner)
                .setExponentValueFormatter(new RoundingFormatter(locale, 1))
                .setParsedDimensionUpdater(new ComponentUnitsParsedDimensionUpdater())
                .setTemplateDimensionMap(new HashMap<>())
                .setAtomicTypeFormatter(new GeneralTextFormatter(locale));

        componentDimensionFormatter = new DimensionFormatter(locale, customComponentDimensionParser, componentUnitsDimensionSerializer);
        unitSystemFormatter = new ChainedFormatter(locale).AddFormatter(new SnakeCaseFormatter(locale)).AddFormatter(new LowercaseFormatter(locale));
        unitNameFormatter = new ChainedFormatter(locale).AddFormatter(new SnakeCaseFormatter(locale)).AddFormatter(new LowercaseFormatter(locale));

        try {
            defaultBaseUnit = new Unit("base", Collections.emptySet(), "", "", "", "", Collections.emptyMap()
                    , new Unit()
                    , new double[]{1.0, 0.0}, locale, componentUnitsDimensionSerializer, fundamentalUnitTypesDimensionSerializer, dimensionComponentDefiner);
            defaultBaseUnit.setAsBaseUnit();
        } catch (UnitException e) {
            e.printStackTrace();
        }

        int numberOfDescriptionSentencesForEachSearchResult = 2;
        int maxNumberOfResultItemsForEachSearch = 3;
        mediaWikiWebServicesApiRequester = new MediaWikiWebServicesApiRequester(numberOfDescriptionSentencesForEachSearchResult, maxNumberOfResultItemsForEachSearch);
    }

    ///
    @Override
    protected UnitManagerBuilder readEntity(XmlPullParser parser) throws XmlPullParserException, IOException, ParsingException, SerializingException {
        initialPopulationOfUnitAndPrefixDataStructures(parser);

        completeUnitConstruction();

        UnitManagerBuilder unitManagerBuilder = new UnitManagerBuilder();

        try{
            unitManagerBuilder.addPrefixDataModel(prefixesDataModel);
        }catch(Exception e){}

        return unitManagerBuilder.addBaseUnits(new ArrayList<>(rawAbbreviatedNameBaseUnitsMap.values()))
                .addNonBaseUnits(new ArrayList<>(rawAbbreviatedNameNonBaseUnitsMap.values()));
    }

    ///
    private void initialPopulationOfUnitAndPrefixDataStructures(XmlPullParser parser) throws IOException, XmlPullParserException {
        Unit partiallyConstructedUnit;

        //
        String tagName = parser.getName();
        if (tagName.equalsIgnoreCase("root")) {
            parser.require(XmlPullParser.START_TAG, null, "root");
            String rootDescription = String.format("Source of Conversion Factors: %s.xml", readAttribute(parser, "xmlns"));
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
                        skip(parser);
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
                if(!partiallyConstructedUnit.isBaseUnit())
                    assignBaseUnit(partiallyConstructedUnit);

                assignCorrectedAbbreviatedComponentUnitsDimension(partiallyConstructedUnit);
                assignFullNameComponentUnitDimension(partiallyConstructedUnit, false);
            } catch (UnitException e) {
                e.printStackTrace();
            }
            partiallyConstructedUnit.setCoreUnitState(true);
            fetchWikiUnitDescription(partiallyConstructedUnit);
        }
        /*A separate iteration is necessary to clean the raw abbreviation since unit since processing in the previous iteration
         * requires all units have the raw abbreviation in order for them to be referenced correctly.
         * Example cleaning transformation: [ft] --> ft
         */
        for (Unit partiallyConstructedUnit : partiallyConstructedUnits) {
            String cleanedAbbreviation = partiallyConstructedUnit.getAbbreviation()
                    .replace("[","").replace("]", "");

            //Make sure there are no collisions with any prexisting abbreviations.
            if(!rawAbbreviatedNameBaseUnitsMap.containsKey(cleanedAbbreviation) && !rawAbbreviatedNameNonBaseUnitsMap.containsKey(cleanedAbbreviation)) {
                partiallyConstructedUnit.setAbbreviation(cleanedAbbreviation);
            }
        }
    }

    private void assignBaseUnit(Unit partiallyConstructedUnit) throws ParsingException, SerializingException, UnitException {
        /*Since the actually base unit may not have already been parsed during this unit's partial construction, only a dummy unit with the name of the base unit and the conversion was associated.
		 Therefore the actual base unit candidate needs to be retrieved. */
        String baseUnitAbbreviation = partiallyConstructedUnit.getBaseUnit().getAbbreviation();
        double[] baseConversionPolyCoeffs = partiallyConstructedUnit.getBaseConversionPolyCoeffs();

        partiallyConstructedUnit.setBaseUnit(getUnitFromAbbreviatedName(baseUnitAbbreviation, true)
                    , baseConversionPolyCoeffs);

        if (partiallyConstructedUnit.getBaseUnit().getCategory().equalsIgnoreCase(Unit.UNKNOWN_UNIT_CATEGORY)
                && !partiallyConstructedUnit.getCategory().isEmpty()) {
            //Sometimes depending on how the units are constructed the base unit may need to get the category, unit system, description from its descendent.
            partiallyConstructedUnit.getBaseUnit().setCategory(partiallyConstructedUnit.getCategory());
            partiallyConstructedUnit.getBaseUnit().setUnitSystem(partiallyConstructedUnit.getUnitSystem());
            partiallyConstructedUnit.getBaseUnit().setDescription(partiallyConstructedUnit.getDescription());
        }
    }
    private void assignCorrectedAbbreviatedComponentUnitsDimension(Unit partiallyConstructedUnit) throws UnitException {
        boolean hasManyComponents = partiallyConstructedUnit.getComponentUnitsDimension().size() > 1;
        boolean hasAnExponentialIdentity = partiallyConstructedUnit.getComponentUnitsDimension().containsValue(1.0);

        if (!hasManyComponents && hasAnExponentialIdentity) {
            partiallyConstructedUnit.clearComponentUnitsDimension(false);
            partiallyConstructedUnit.addComponentUnit(partiallyConstructedUnit.getAbbreviation(), 1.0, false);
        }
    }
    private void assignFullNameComponentUnitDimension(Unit partiallyConstructedUnit, boolean recreateUnitName) throws ParsingException, SerializingException, UnitException {
        //Transforms abbreviated component units dimension into its equivalent full name dimension.
        Map<String, Double> abbreviatedComponentUnitsDimension = new HashMap<>(partiallyConstructedUnit.getComponentUnitsDimension());
        partiallyConstructedUnit.clearComponentUnitsDimension(false);

        for (Entry<String, Double> componentEntry : abbreviatedComponentUnitsDimension.entrySet()) {
            partiallyConstructedUnit.addComponentUnit(getUnitFromAbbreviatedName(componentEntry.getKey(), true).getName()
                    , componentEntry.getValue()
                    , recreateUnitName
                            //recreate using component unit name dimension if name already consistent of dimensions
                            || nameWithOperatorMatcher.reset(partiallyConstructedUnit.getName()).matches());
        }
    }

    private Unit getUnitFromAbbreviatedName(String abbreviatedName, boolean createMissingUnits) throws ParsingException, SerializingException, UnitException {
        String abbreviatedNameWithNoConversionFactors = numericalComponentUnitNameMatcher.reset(abbreviatedName).replaceAll(""); //Strip any conversion factors if any exists, which may hinder map search.

        Unit unit = rawAbbreviatedNameBaseUnitsMap.get(abbreviatedNameWithNoConversionFactors );
        if (unit != null)
            return unit;

        unit = rawAbbreviatedNameNonBaseUnitsMap.get(abbreviatedNameWithNoConversionFactors );
        if (unit != null)
            return unit;

        if (createMissingUnits) { //Very likely unit name is composed of a complex component unit dimension combination of regular and prefixed unit abbreviations.
            //During decomposition, create any missing prefixed units where necessary.
            return createMissingUnitsFromComponentUnitsDimensionString(abbreviatedNameWithNoConversionFactors );
        } else {
            return null;
        }
    }

    private Unit createMissingUnitsFromComponentUnitsDimensionString(String abbreviatedComponentUnitsDimensionString) throws ParsingException, SerializingException, UnitException {
        Map<String, Double> componentUnitsDimension = customComponentDimensionParser.parse(abbreviatedComponentUnitsDimensionString);
        Unit createdPrefixedUnit = null;

        for (Entry<String, Double> abbreviatedCompUnitEntry : componentUnitsDimension.entrySet()) {
            if (getUnitFromAbbreviatedName(abbreviatedCompUnitEntry.getKey(), false) == null)
                createdPrefixedUnit = createMissingPrefixedUnitFromCandidateName(abbreviatedCompUnitEntry.getKey());
        }

        //If there is only one component unit and the component unit is a prefixed unit, then the unit was already added
        //to the non-base unit map by createMissingPrefixedUnitFromCandidateName method. Therefore, there is no need to add it again as base unit.
        String formattedName = componentDimensionFormatter.format(abbreviatedComponentUnitsDimensionString);
        if (createdPrefixedUnit == null || !( componentUnitsDimension.size() == 1 && componentUnitsDimension.values().iterator().next() == 1.0)) {

            Unit createdUnit = new Unit(formattedName, Collections.emptySet(), "", "", ""
                    , formattedName, componentUnitsDimension, defaultBaseUnit, defaultBaseUnit.getBaseConversionPolyCoeffs(), locale
                    , componentUnitsDimensionSerializer, fundamentalUnitTypesDimensionSerializer, dimensionComponentDefiner);

            convertComponentUnitsDimensionToConversionFactorsList(createdUnit.getComponentUnitsDimension());
            assignCorrectedAbbreviatedComponentUnitsDimension(createdUnit);
            rawAbbreviatedNameBaseUnitsMap.put(abbreviatedComponentUnitsDimensionString, createdUnit);
            assignFullNameComponentUnitDimension(createdUnit, true);

            createdUnit.setAsBaseUnit();
            createdUnit.setCoreUnitState(true);

            return createdUnit;
        } else {
            return createdPrefixedUnit;
        }
    }
    /**
     * Identifies an abbreviated prefixed unit and construct the prefixed unit with the appropriate base conversion based on the prefix factor.
     */
    private Unit createMissingPrefixedUnitFromCandidateName(String candidatePrefixedAbbreviatedName) throws ParsingException, SerializingException, UnitException {
        Unit createdPrefixedUnit = null;

        Collection<String[]> prefixMatches = prefixesDataModel.findPrefixPairMatches(candidatePrefixedAbbreviatedName, false);
        for(String[] prefixMatch:prefixMatches){
            Unit prefixlessUnit = getUnitFromAbbreviatedName(prefixMatch[2], false);

            if (prefixlessUnit != null) {
                createdPrefixedUnit = new PrefixedUnit(prefixMatch[0]
                        , prefixMatch[1], prefixesDataModel.getPrefixValue(prefixMatch[0])
                        , prefixlessUnit, true
                        , locale, componentUnitsDimensionSerializer, fundamentalUnitTypesDimensionSerializer, dimensionComponentDefiner);

                rawAbbreviatedNameNonBaseUnitsMap.put(candidatePrefixedAbbreviatedName, createdPrefixedUnit);

                break;
            }
        }

        return createdPrefixedUnit;
    }

    private void recalculatedBaseConversionPolyCoeffsFromComponentUnitsDimension(Unit partiallyConstructedUnit) {
		/*Due to how the original xml is setup and consequently how it parsed, conversion factors can inadvertently be parsed as a component.
		Therefore, they have to be removed after being used to updateContent the base conversion coefficient. */
        ArrayList<Double> conversionFactors = convertComponentUnitsDimensionToConversionFactorsList(partiallyConstructedUnit.getComponentUnitsDimension());
        for (Double conversionFactor : conversionFactors) {
            partiallyConstructedUnit.getBaseConversionPolyCoeffs()[0] *= Math.abs((conversionFactor < 1.0 ? 1 / conversionFactor : conversionFactor));
        }
    }
    private ArrayList<Double> convertComponentUnitsDimensionToConversionFactorsList(Map<String, Double> componentUnitsDimension) {
        ArrayList<Double> conversionFactors = new ArrayList<>();

        Iterator<Entry<String, Double>> dimensionEntryIterator = componentUnitsDimension.entrySet().iterator();
        while (dimensionEntryIterator.hasNext()) {
            Entry<String, Double> dimensionEntry = dimensionEntryIterator.next();
            String key = dimensionEntry.getKey();
            Double value = dimensionEntry.getValue();
            if (Character.isDigit(key.charAt(0)) && Character.isDigit(key.charAt(key.length()-1)))// Quickly, slight unreliable of determining if string is numeric. More performant than regex.
            {
                if(key.equals("1")) //1 is a special case and should not be removed.
                    continue;

                //Due to how the parsing was designed, when a conversion factor is picked up as a component unit as consequence of how the original xml was designed, the key and values pairs have the same absolute number.
                conversionFactors.add(Math.abs((value < 0.0 ? 1 / value : value)));
                dimensionEntryIterator.remove();
            }
        }

        return conversionFactors;
    }

    private void fetchWikiUnitDescription(Unit partiallyConstructedUnit){
        //No absolute guarantee the one will get the right correct description extract. But more specific the search terms lead to high probability fo getting the right result.
        //TODO: Some extract descriptions contain foreign characters and markup characters that are not rendered properly.
        String searchTerms = String.format("%s %s %s", partiallyConstructedUnit.getName().replace("_"," "), partiallyConstructedUnit.getCategory(), "unit");
        PageExtractsSearchResult pageExtractsSearchResult = mediaWikiWebServicesApiRequester.searchPageExtracts(searchTerms);

        try {
            PageExtract bestPageExtract = pageExtractsSearchResult.getBestMatchPageExtract();
            if (bestPageExtract.hasContent()){
                String extractText = bestPageExtract.getExtractText();
                String reference = bestPageExtract.getWikipediaReferenceUrl();
                String unitDescription = String.format("%s \n[[Source of Extract: %s]]", extractText, reference);

                partiallyConstructedUnit.setDescription(String.format("%s \n(%s)", unitDescription, partiallyConstructedUnit.getDescription()));
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    ///
    private void readPrefix(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "prefix");

        String tagName, fullPrefixName = "", abbreviatedPrefixName = "";
        Double prefixValue = 0.0;

        abbreviatedPrefixName = readAttribute(parser, "CODE").toLowerCase();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            tagName = parser.getName();
            if (tagName.equalsIgnoreCase("name")) {
                fullPrefixName = readText(parser);
            } else if (tagName.equalsIgnoreCase("value")) {
                prefixValue = Double.valueOf(readAttribute(parser, "value"));
                skip(parser); //only attributes where being read which does not automatically advance the parser, therefore the advancement has to be forced.
            } else {
                skip(parser);
            }
        }

        prefixesDataModel.addCorePrefix(fullPrefixName, abbreviatedPrefixName, prefixValue);
    }

    private Unit readUnit(XmlPullParser parser, String rootDescription, boolean isBaseUnit) throws XmlPullParserException, IOException, UnitException {
        parser.require(XmlPullParser.START_TAG, null, (isBaseUnit) ? "base-unit" : "unit");

        Map<String, double[]> baseUnitNameNConversionPolyCoeffs = new HashMap<>();
        Map<String, Double> abbreviatedComponentUnitsDimension = new HashMap<>();
        Set<String> unitNames = new HashSet<>();
        String unitCategory = "", tagName = "";

        String rawUnitAbbreviation = readRawUnitAbbreviation(parser, isBaseUnit);
        String unitSystem = (isBaseUnit) ? "si" : readUnitSystem(parser);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            tagName = parser.getName();
            if (tagName.equalsIgnoreCase("name")) {
                unitNames.add(readUnitName(parser));
            } else if (tagName.equalsIgnoreCase("property")) {
                unitCategory = readUnitCategory(parser);
            } else if (tagName.equalsIgnoreCase("value") && !isBaseUnit) {
                try {
                    baseUnitNameNConversionPolyCoeffs = readBaseUnitNConversionPolyCoeffs(parser); //Issues may arise when the the conversion is not a simple conversion factor but is instead a complex function with some specific argument. ex. sqrt, log
                    abbreviatedComponentUnitsDimension = readAbbreviatedComponentUnitsDimension(parser); //Issues can arise when trying to parse constants represented as units in the xml since there are component units with only numbers as unit names instead of alphabets.
                }
                catch(Exception e){
                    e.printStackTrace();
                }

                skip(parser); //only attributes where being read which does not automatically advance parser, there it has to be forced.
            } else {
                skip(parser);
            }
        }

        //Only return a created unit: Issue had not arisen when constructing the baseUnitNConversionPolyCoeffs and abbreviatedComponentUnitsDimension for non base units.
        if ((abbreviatedComponentUnitsDimension.size() == 0
                || baseUnitNameNConversionPolyCoeffs.size() == 0) && !isBaseUnit) {
            return null;
        } else {
            String mainUnitName = unitNames.iterator().next();

            String baseUnitName = isBaseUnit? mainUnitName:baseUnitNameNConversionPolyCoeffs.keySet().iterator().next();
            Unit standInNamedBaseUnit = new Unit(baseUnitName, Collections.emptySet(), "", "", "", baseUnitName, Collections.emptyMap()
                    , defaultBaseUnit
                    , defaultBaseUnit.getBaseConversionPolyCoeffs(), locale, componentUnitsDimensionSerializer, fundamentalUnitTypesDimensionSerializer, dimensionComponentDefiner);
            standInNamedBaseUnit.setAsBaseUnit();

            unitNames.remove(mainUnitName);

            Unit createdUnit = new Unit(mainUnitName, unitNames, unitCategory, rootDescription, unitSystem, rawUnitAbbreviation, abbreviatedComponentUnitsDimension
                    , standInNamedBaseUnit
                    , isBaseUnit ? new double[]{1.0, 0.0} : baseUnitNameNConversionPolyCoeffs.values().iterator().next(), locale, componentUnitsDimensionSerializer, fundamentalUnitTypesDimensionSerializer, dimensionComponentDefiner);

            if (isBaseUnit) {
                createdUnit.setAsBaseUnit();
                createdUnit.addComponentUnit(createdUnit.getAbbreviation(), 1.0, false);
            }

            return createdUnit;
        }
    }

    /// Methods for retrieving entity components from XML
    private String readUnitName(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "name");
        String unitNameString = unitNameFormatter.format(readText(parser));
        parser.require(XmlPullParser.END_TAG, null, "name");
        return unitNameString;
    }
    private String readUnitSystem(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "unit");
        return unitSystemFormatter.format(readAttribute(parser, "class").split("-")[0])
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
        return customComponentDimensionParser.parse(readAttribute(parser, "Unit"));
    }
    private Map<String, double[]> readBaseUnitNConversionPolyCoeffs(XmlPullParser parser) throws XmlPullParserException, IOException {
        Map<String, double[]> conversionPolyCoeffsMap = new HashMap<>();
            parser.require(XmlPullParser.START_TAG, null, "value");
            conversionPolyCoeffsMap.put(readAttribute(parser, "Unit")
                    , new double[]{1.0 / Double.parseDouble(readAttribute(parser, "value")), 0.0});
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
