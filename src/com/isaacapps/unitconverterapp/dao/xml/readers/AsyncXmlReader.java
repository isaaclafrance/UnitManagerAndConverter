package com.isaacapps.unitconverterapp.dao.xml.readers;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Xml;

import com.isaacapps.unitconverterapp.processors.parsers.ParsingException;
import com.isaacapps.unitconverterapp.processors.serializers.SerializingException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public abstract class AsyncXmlReader<S, T> extends AsyncTaskLoader<T> {

    ///
    public AsyncXmlReader(Context context) {
        super(context);
    }

    /**
     * According to official Google Android documentation, the XmlPullParser that reads one tag
     * at a time is the most efficient way of parsing especially in situations where there are a large number of tags.
     */
    public S parseXML(InputStream in) {
        try {
            if (in != null) {
                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(in, null);
                parser.nextTag();
                return readEntity(parser);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    ///
    public InputStream openAssetFile(String fileName) throws IOException {
        return getContext().getAssets().open(fileName);
    }

    public InputStream openXmlFile(String fileLocation, boolean isOnlineSource) throws IOException {
        if (isOnlineSource) {
            return new URL(fileLocation).openStream();
        } else {
            File xmlFile = new File(fileLocation);
            if (xmlFile.exists()) {
                return new FileInputStream(xmlFile);
            } else {
                return null;
            }
        }
    }

    ///
    abstract protected S readEntity(XmlPullParser parser) throws IOException, XmlPullParserException, ParsingException, SerializingException;

    ///
    protected String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String text = "";
        if (parser.next() == XmlPullParser.TEXT) {
            text = parser.getText().trim();
            parser.nextTag();
        }
        return text;
    }

    protected int readInt(XmlPullParser parser) throws IOException, XmlPullParserException {
        return Integer.parseInt(readText(parser));
    }

    protected double readDouble(XmlPullParser parser) throws IOException, XmlPullParserException {
        return Double.parseDouble(readText(parser));
    }

    protected double[] readDoubleArray(XmlPullParser parser) throws IOException, XmlPullParserException {
        double[] doubles = new double[2];
        String[] doublesTexts = readText(parser).split(" ", 0);

        doubles[0] = Double.valueOf(doublesTexts[0]);
        if (doublesTexts.length > 1) {
            doubles[1] = Double.valueOf(doublesTexts[1]);
        }
        return doubles;
    }

    protected String readAttribute(XmlPullParser parser, String attributeName) {
        String attributeValue = parser.getAttributeValue(null, attributeName);
        return (attributeValue == null) ? "" : attributeValue.toLowerCase();
    }

    protected void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
