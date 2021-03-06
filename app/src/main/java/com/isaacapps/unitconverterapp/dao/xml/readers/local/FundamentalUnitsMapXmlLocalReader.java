package com.isaacapps.unitconverterapp.dao.xml.readers.local;

import android.content.Context;

import com.isaacapps.unitconverterapp.dao.xml.readers.AsyncXmlReader;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManagerBuilder;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel.UNIT_TYPE;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

///According to official Google Android documentation, the XmlPullParser that reads one tag at a time is the most efficient way of parsing especially in situations where there are a large number of tags.
public class FundamentalUnitsMapXmlLocalReader extends AsyncXmlReader<FundamentalUnitsDataModel, UnitManagerBuilder> {
    public static String FUNDAMENTAL_UNITS_FILE = "FundamentalUnits.xml";

    private final FundamentalUnitsDataModel fundamentalUnitsDataModel;

    ///
    public FundamentalUnitsMapXmlLocalReader(Context context, FundamentalUnitsDataModel fundamentalUnitsDataModel) {
        super(context);
        this.fundamentalUnitsDataModel = fundamentalUnitsDataModel;
    }

    ///
    protected FundamentalUnitsDataModel readEntity(XmlPullParser parser) throws XmlPullParserException, IOException {
        String tagName, unitSystem = "";

        parser.require(XmlPullParser.START_TAG, null, "main");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            tagName = parser.getName();
            if (tagName.equalsIgnoreCase("unitSystem")) {
                parser.require(XmlPullParser.START_TAG, null, "unitSystem");
                while (parser.next() != XmlPullParser.END_TAG) {
                    if (parser.getEventType() != XmlPullParser.START_TAG) {
                        continue;
                    }
                    tagName = parser.getName();
                    if (tagName.equalsIgnoreCase("name")) {
                        unitSystem = readUnitSystem(parser);
                    } else if (tagName.equalsIgnoreCase("dimensions")) {
                        readDimensionAssoc(parser, unitSystem, fundamentalUnitsDataModel);
                    } else {
                        skip(parser);
                    }
                }
            } else {
                skip(parser);
            }
        }

        return fundamentalUnitsDataModel;
    }

    ///
    private String readUnitName(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "unit");
        String unitName = readText(parser).toLowerCase();
        parser.require(XmlPullParser.END_TAG, null, "unit");
        return unitName;
    }

    private String readUnitSystem(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "name");
        String name = readText(parser).toLowerCase();
        parser.require(XmlPullParser.END_TAG, null, "name");
        return name;
    }

    private void readDimensionAssoc(XmlPullParser parser, String unitSystem, FundamentalUnitsDataModel fundamentalUnitsDataModel) throws XmlPullParserException, IOException {
        String tagName, unitName, dimensionName;
        tagName = unitName = dimensionName = "";

        parser.require(XmlPullParser.START_TAG, null, "dimensions");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            tagName = parser.getName();
            if (tagName.equalsIgnoreCase("dimension")) {
                parser.require(XmlPullParser.START_TAG, null, "dimension");
                while (parser.next() != XmlPullParser.END_TAG) {
                    if (parser.getEventType() != XmlPullParser.START_TAG) {
                        continue;
                    }
                    tagName = parser.getName();
                    if (tagName.equalsIgnoreCase("type")) {
                        dimensionName = readFundamentalDimension(parser);
                    } else if (tagName.equalsIgnoreCase("unit")) {
                        unitName = readUnitName(parser);
                    } else {
                        skip(parser);
                    }
                }
                fundamentalUnitsDataModel.addFundamentalUnit(unitSystem, unitName, UNIT_TYPE.valueOf(dimensionName));
            } else {
                skip(parser);
            }
        }
    }
    private String readFundamentalDimension(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "type");
        String fundamentalDimension = readText(parser).toUpperCase();
        parser.require(XmlPullParser.END_TAG, null, "type");
        return fundamentalDimension;
    }


    //// Loader Methods
    @Override
    public UnitManagerBuilder loadInBackground() {
        UnitManagerBuilder unitManagerBuilderBundle = new UnitManagerBuilder();
        try {
            unitManagerBuilderBundle.addFundamentalUnitsDataModel(parseXML(openAssetFile(FUNDAMENTAL_UNITS_FILE)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return unitManagerBuilderBundle;
    }
}
