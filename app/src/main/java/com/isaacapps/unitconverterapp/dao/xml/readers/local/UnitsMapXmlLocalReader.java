package com.isaacapps.unitconverterapp.dao.xml.readers.local;

import android.content.Context;

import com.isaacapps.unitconverterapp.dao.xml.readers.AsyncXmlReader;
import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.measurables.unit.UnitException;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManagerBuilder;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.DimensionComponentDefiner;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.componentnunit.ComponentUnitsDimensionSerializer;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.fundamentalunit.FundamentalUnitTypesDimensionSerializer;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

///According to official Google Android documentation, the XmlPullParser that reads one tag at a time is the most efficient way of parsing especially in situations where there are a large number of tags.
public class UnitsMapXmlLocalReader extends AsyncXmlReader<ArrayList<ArrayList<Unit>>, UnitManagerBuilder> {
    private final Locale locale;
    private final DimensionComponentDefiner dimensionComponentDefiner;
    private final ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer;
    private final FundamentalUnitTypesDimensionSerializer fundamentalUnitTypesDimensionSerializer;

    private final Map<String, Unit> baseUnitsMap;
    private final Map<String, Unit> nonBaseUnitsMap;
    private final ArrayList<Unit> partiallyConstructedUnits;
    private final ArrayList<Unit> defectiveUnits;

    private Unit defaultUnit;

    ///
    public UnitsMapXmlLocalReader(Context context, Locale locale, ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer, FundamentalUnitTypesDimensionSerializer fundamentalUnitTypesDimensionSerializer, DimensionComponentDefiner dimensionComponentDefiner) {
        super(context);
        this.locale = locale;
        this.dimensionComponentDefiner = dimensionComponentDefiner;
        this.componentUnitsDimensionSerializer = componentUnitsDimensionSerializer;
        this.fundamentalUnitTypesDimensionSerializer = fundamentalUnitTypesDimensionSerializer;

        baseUnitsMap = new HashMap<>();
        nonBaseUnitsMap = new HashMap<>();
        partiallyConstructedUnits = new ArrayList<>();
        defectiveUnits = new ArrayList<>();

        try {
            defaultUnit = new Unit("base", Collections.emptySet(), "", "", "", "", Collections.emptyMap()
                    , new Unit()
                    , new double[]{1.0, 0.0}, locale, componentUnitsDimensionSerializer, fundamentalUnitTypesDimensionSerializer, dimensionComponentDefiner);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ///
    @Override
    protected ArrayList<ArrayList<Unit>> readEntity(XmlPullParser parser) throws IOException, XmlPullParserException {
        Unit partiallyConstructedUnit;

        String tagName = parser.getName();
        if (tagName.equalsIgnoreCase("main")) {
            parser.require(XmlPullParser.START_TAG, null, "main");
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                tagName = parser.getName();
                if (tagName.equalsIgnoreCase("unit")) {
                    try {
                        partiallyConstructedUnit = readUnit(parser);
                        partiallyConstructedUnits.add(partiallyConstructedUnit);
                        if (partiallyConstructedUnit.isBaseUnit()) {
                            baseUnitsMap.put(partiallyConstructedUnit.getName(), partiallyConstructedUnit);
                        } else {
                            nonBaseUnitsMap.put(partiallyConstructedUnit.getName(), partiallyConstructedUnit);
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                } else {
                    skip(parser);
                }
            }
        }

        //
        completeUnitConstruction();

        //
        ArrayList<ArrayList<Unit>> unitsLists = new ArrayList<>(2);
        unitsLists.add(new ArrayList<>(baseUnitsMap.values()));
        unitsLists.add(new ArrayList<>(nonBaseUnitsMap.values()));

        unitsLists.removeAll(defectiveUnits);

        return unitsLists;
    }

    /**
     * Completes the construction of the previous constructed unit by adding missing elements i.e. base unit.
     * Finds actual unit references for the base unit names now that all units have been created.
     */
    private void completeUnitConstruction() {
        for (Unit unit : partiallyConstructedUnits) {
           try {
               //The implicit assumption, dependent on the organization of the XML, is that the partiallyConstructedUnits list is ordered by sequential base unit dependence.
               //In other words if unit 'A' needs to set 'B' as a base unit, then 'B' had already had its base units properly set.
               String baseUnitName = unit.getBaseUnit().getName();
               unit.setBaseUnit(baseUnitsMap.containsKey(baseUnitName) ? baseUnitsMap.get(baseUnitName) : nonBaseUnitsMap.get(baseUnitName)
                       , unit.getBaseConversionPolyCoeffs());
           }
           catch (UnitException e){
               defectiveUnits.add(unit);
           }
        }
    }

    ///
    private Unit readUnit(XmlPullParser parser) throws XmlPullParserException, IOException, UnitException{
        Map<String, double[]> baseUnitNameNConversionPolyCoeffs = new HashMap<>();
        Map<String, Double> componentUnitsExponentsMap = new HashMap<>();
        Set<String> unitNameAliases = new HashSet<>();
        String unitName = "", unitSystem = "", abbreviation = "", unitCategory = "", unitDescription = "", tagName = "";

        parser.require(XmlPullParser.START_TAG, null, "unit");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            tagName = parser.getName();
            if (tagName.equalsIgnoreCase("unitName")) {
                unitName = readUnitName(parser);
            } else if (tagName.equalsIgnoreCase("unitNameAliases")) {
                unitNameAliases = readUnitNameAliases(parser);
            } else if (tagName.equalsIgnoreCase("unitSystem")) {
                unitSystem = readUnitSystem(parser);
            } else if (tagName.equalsIgnoreCase("abbreviation")) {
                abbreviation = readAbbreviation(parser);
            } else if (tagName.equalsIgnoreCase("unitCategory")) {
                unitCategory = readUnitCategory(parser);
            } else if (tagName.equalsIgnoreCase("unitDescription")) {
                unitDescription = readUnitDescription(parser);
            } else if (tagName.equalsIgnoreCase("componentUnits")) {
                componentUnitsExponentsMap = readComponentUnits(parser);
            } else if (tagName.equalsIgnoreCase("baseConversionPolyCoeffs")) {
                baseUnitNameNConversionPolyCoeffs = readBaseUnitNConversionPolyCoeffs(parser);
            } else {
                skip(parser);
            }
        }

        Unit standInNamedBaseUnit = new Unit(baseUnitNameNConversionPolyCoeffs.keySet().iterator().next(), Collections.emptySet(), "", "", "", "", Collections.emptyMap()
                , defaultUnit
                , defaultUnit.getBaseConversionPolyCoeffs(), locale, componentUnitsDimensionSerializer, fundamentalUnitTypesDimensionSerializer, dimensionComponentDefiner);
        standInNamedBaseUnit.setAsBaseUnit();

        Unit createdUnit = new Unit(unitName, unitNameAliases, unitCategory, unitDescription, unitSystem, abbreviation, componentUnitsExponentsMap
                , standInNamedBaseUnit, baseUnitNameNConversionPolyCoeffs.values().iterator().next()
                , locale, componentUnitsDimensionSerializer, fundamentalUnitTypesDimensionSerializer, dimensionComponentDefiner);

        if (createdUnit.getName().equalsIgnoreCase(standInNamedBaseUnit.getName()))
            createdUnit.setAsBaseUnit();

        return createdUnit;
    }

    ///
    private String readUnitName(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "unitName");
        String unitNameString = readText(parser).toLowerCase();
        parser.require(XmlPullParser.END_TAG, null, "unitName");
        return unitNameString;
    }

    private Set<String> readUnitNameAliases(XmlPullParser parser) throws XmlPullParserException, IOException {
        Set<String> unitNameAliases = new HashSet<>();
        String tagName = "";

        parser.require(XmlPullParser.START_TAG, null, "unitNameAliases");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            tagName = parser.getName();
            if (tagName.equalsIgnoreCase("alias")) {
                unitNameAliases.add(readText(parser));
            } else {
                skip(parser);
            }
        }
        return unitNameAliases;
    }

    private String readUnitSystem(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "unitSystem");
        String unitSystemString = readText(parser).toLowerCase();
        parser.require(XmlPullParser.END_TAG, null, "unitSystem");
        return unitSystemString;
    }

    private String readAbbreviation(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "abbreviation");
        String abbreviation = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "abbreviation");
        return abbreviation;
    }

    private String readUnitCategory(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "unitCategory");
        String abbreviation = readText(parser).toLowerCase();
        parser.require(XmlPullParser.END_TAG, null, "unitCategory");
        return abbreviation;
    }

    private String readUnitDescription(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "unitDescription");
        String abbreviation = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "unitDescription");
        return abbreviation;
    }

    private Map<String, Double> readComponentUnits(XmlPullParser parser) throws XmlPullParserException, IOException {
        Map<String, Double> componentUnitsExponentsMap = new HashMap<>();
        String componentUnitName = "";
        double componentExponentValue = 0.0;
        String tagName = "";

        parser.require(XmlPullParser.START_TAG, null, "componentUnits");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            tagName = parser.getName();
            if (tagName.equalsIgnoreCase("component")) {
                parser.require(XmlPullParser.START_TAG, null, "component");
                while (parser.next() != XmlPullParser.END_TAG) {
                    if (parser.getEventType() != XmlPullParser.START_TAG) {
                        continue;
                    }
                    tagName = parser.getName();
                    if (tagName.equalsIgnoreCase("unitName")) {
                        componentUnitName = readText(parser);
                    } else if (tagName.equalsIgnoreCase("exponent")) {
                        componentExponentValue = readDouble(parser);
                    } else {
                        skip(parser);
                    }
                }
                componentUnitsExponentsMap.put(componentUnitName, componentExponentValue);
            } else {
                skip(parser);
            }
        }
        return componentUnitsExponentsMap;
    }

    private Map<String, double[]> readBaseUnitNConversionPolyCoeffs(XmlPullParser parser) throws XmlPullParserException, IOException {
        Map<String, double[]> conversionPolyCoeffsMap = new HashMap<>();
        String baseUnitName = "";
        double[] polynomialCoeffs = new double[]{0.0, 0.0};
        String tagName = "";

        parser.require(XmlPullParser.START_TAG, null, "baseConversionPolyCoeffs");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            tagName = parser.getName();
            if (tagName.equalsIgnoreCase("baseUnit")) {
                baseUnitName = readText(parser);
            } else if (tagName.equals("polynomialCoeffs")) { //If no conversion polynomial coeffs were provided, then the default is {0.0f, 0.0f}
                polynomialCoeffs = readDoubleArray(parser);
            } else {
                skip(parser);
            }
        }

        conversionPolyCoeffsMap.put(baseUnitName, polynomialCoeffs);

        return conversionPolyCoeffsMap;
    }

    ///
    @Override
    public UnitManagerBuilder loadInBackground() {

        ArrayList<ArrayList<Unit>> coreUnitsGroup = new ArrayList<>();
        ArrayList<ArrayList<Unit>> dynamicUnitsGroup = new ArrayList<>();
        try {
            coreUnitsGroup = parseXML(openAssetFile("StandardCoreUnits.xml"));
            //dynamicUnitsGroup = parseXML(openXmlFile(getContext().getFilesDir().getPath().toString() + "DynamicUnits.xml", false));
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 2; i++) {
            for (Unit unit : coreUnitsGroup.get(i)) {
                unit.setCoreUnitState(true);
            }
        }

        ArrayList<Unit> combinedUnits = coreUnitsGroup.get(0);
        combinedUnits.addAll(coreUnitsGroup.get(1));

        //if(dynamicUnitsGroup != null){
        //combinedUnits.addAll(dynamicUnitsGroup.get(0));
        //combinedUnits.addAll(dynamicUnitsGroup.get(1));
        //}

        return new UnitManagerBuilder().addBaseUnits(combinedUnits)
                .addNonBaseUnits(combinedUnits);
    }


}
