package com.isaacapps.unitconverterapp.dao.xml.readers.local;

import android.content.Context;

import com.isaacapps.unitconverterapp.dao.xml.readers.AsyncXmlReader;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManagerBuilder;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.PrefixesDataModel;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.ContentDeterminer;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

///According to official Google Android documentation, the XmlPullParser that reads one tag at a time is the most efficient way of parsing especially in situations where there are a large number of tags.
public class PrefixesMapXmlLocalReader extends AsyncXmlReader<PrefixesDataModel, UnitManagerBuilder> {
    private ContentDeterminer.DATA_MODEL_CATEGORY categoryCurrentlyBeingRead;
    final PrefixesDataModel prefixesDataModel;

    ///
    public PrefixesMapXmlLocalReader(Context context, PrefixesDataModel prefixesDataModel) {
        super(context);
        this.prefixesDataModel = prefixesDataModel;
    }

    ///
    @Override
    protected PrefixesDataModel readEntity(XmlPullParser parser) throws XmlPullParserException, IOException {
        String tagName = "", prefixName = "", abbreviation = "";
        double prefixValue = 0.0;

        //
        tagName = parser.getName();
        if (tagName.equalsIgnoreCase("main")) {
            parser.require(XmlPullParser.START_TAG, null, "main");
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                tagName = parser.getName();
                if (tagName.equalsIgnoreCase("prefix")) {
                    while (parser.next() != XmlPullParser.END_TAG) {
                        if (parser.getEventType() != XmlPullParser.START_TAG) {
                            continue;
                        }
                        tagName = parser.getName();
                        if (tagName.equalsIgnoreCase("name")) {
                            prefixName = readText(parser).toLowerCase();
                        } else if (tagName.equalsIgnoreCase("abbreviation")) {
                            abbreviation = readText(parser);
                        } else if (tagName.equalsIgnoreCase("value")) {
                            prefixValue = readDouble(parser);
                        } else {
                            skip(parser);
                        }
                    }
                    if (categoryCurrentlyBeingRead == ContentDeterminer.DATA_MODEL_CATEGORY.CORE) {
                        prefixesDataModel.addCorePrefix(prefixName, abbreviation, prefixValue);
                    } else {
                        prefixesDataModel.addDynamicPrefix(prefixName, abbreviation, prefixValue);
                    }
                } else {
                    skip(parser);
                }
            }
        }

        return prefixesDataModel;
    }

    ///
    @Override
    public UnitManagerBuilder loadInBackground() {
        UnitManagerBuilder unitManagerBuilderBundle = new UnitManagerBuilder();
        try {
            categoryCurrentlyBeingRead = ContentDeterminer.DATA_MODEL_CATEGORY.CORE;
            unitManagerBuilderBundle.addPrefixDataModel(parseXML(openAssetFile("StandardCorePrefixes.xml")));
            categoryCurrentlyBeingRead = ContentDeterminer.DATA_MODEL_CATEGORY.DYNAMIC;
            unitManagerBuilderBundle.addPrefixDataModel(parseXML(openXmlFile(getContext().getFilesDir().getPath() + "DynamicPrefixes.xml", false)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return unitManagerBuilderBundle;
    }

}
