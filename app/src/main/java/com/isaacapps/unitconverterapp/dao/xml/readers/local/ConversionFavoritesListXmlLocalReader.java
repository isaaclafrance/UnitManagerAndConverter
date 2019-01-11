package com.isaacapps.unitconverterapp.dao.xml.readers.local;

import android.app.Application;
import android.content.Context;

import com.isaacapps.unitconverterapp.dao.xml.readers.AsyncXmlReader;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.ConversionFavoritesDataModel;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

///According to official Google Android documentation, the XmlPullParser that reads one tag at a time is the most efficient way of parsing especially in situations where there are a large number of tags.
public class ConversionFavoritesListXmlLocalReader extends AsyncXmlReader<ConversionFavoritesDataModel, ConversionFavoritesDataModel> {
    public static String CONVERSION_FAVORITES_FILE = "ConversionFavorites.xml";

    private final ConversionFavoritesDataModel conversionFavoritesDataModel;

    ///
    public ConversionFavoritesListXmlLocalReader(Context context, ConversionFavoritesDataModel conversionFavoritesDataModel) {
        super(context);
        this.conversionFavoritesDataModel = conversionFavoritesDataModel;
    }

    ///
    @Override
    protected ConversionFavoritesDataModel readEntity(XmlPullParser parser) throws XmlPullParserException, IOException {
        int significanceRank = 0;
        String unitCategory, sourceUnit, targetUnit, tagName;
        unitCategory = sourceUnit = targetUnit = "";
        tagName = parser.getName();

        if (tagName.equalsIgnoreCase("main")) {
            parser.require(XmlPullParser.START_TAG, null, "main");
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                tagName = parser.getName();
                if (tagName.equalsIgnoreCase("favorite")) {
                    parser.require(XmlPullParser.START_TAG, null, "favorite");
                    while (parser.next() != XmlPullParser.END_TAG) {
                        if (parser.getEventType() != XmlPullParser.START_TAG) {
                            continue;
                        }
                        tagName = parser.getName();
                        if (tagName.equalsIgnoreCase("significanceRank")) {
                            significanceRank = readInt(parser);
                        } else if (tagName.equalsIgnoreCase("sourceUnit")) {
                            sourceUnit = readText(parser).toLowerCase();
                        } else if (tagName.equalsIgnoreCase("targetUnit")) {
                            targetUnit = readText(parser).toLowerCase();
                        } else if (tagName.equalsIgnoreCase("unitCategory")) {
                            unitCategory = readText(parser).toLowerCase();
                        } else {
                            skip(parser);
                        }
                    }
                    conversionFavoritesDataModel.modifySignificanceRankOfConversion(conversionFavoritesDataModel
                            .addConversion(unitCategory, sourceUnit, targetUnit), significanceRank);
                } else {
                    skip(parser);
                }
            }
        } else {
            skip(parser);
        }
        return conversionFavoritesDataModel;
    }

    ///
    @Override
    public ConversionFavoritesDataModel loadInBackground() {
        ConversionFavoritesDataModel conversionFavoritesDataModel = new ConversionFavoritesDataModel();
        try {
            conversionFavoritesDataModel = parseXML(openXmlFile(getContext().getFilesDir().getPath() + CONVERSION_FAVORITES_FILE, false));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return conversionFavoritesDataModel;
    }

}
