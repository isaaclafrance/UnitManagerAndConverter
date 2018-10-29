package com.isaacapps.unitconverterapp.dao.xml.writers.local;

import android.content.Context;

import com.isaacapps.unitconverterapp.dao.xml.writers.XmlWriter;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

public class PrefixesMapXmlLocalWriter extends XmlWriter<Map<String, Double>> {

    ///
    public PrefixesMapXmlLocalWriter(Context context) {
        super(context);
        destination = "DynamicPrefixes.xml";
    }

    ///
    @Override
    protected void writeEntity(XmlSerializer xmlSerializer, String namespace, Map<String, Double> prefixes) throws IllegalArgumentException, IllegalStateException, IOException {
        for (Entry<String, Double> entry : prefixes.entrySet()) {
            xmlSerializer.startTag(namespace, "prefix");

            xmlSerializer.startTag(namespace, "name");
                xmlSerializer.text(entry.getKey());
            xmlSerializer.endTag(namespace, "name");

            xmlSerializer.startTag(namespace, "value");
                xmlSerializer.text(entry.getValue().toString());
            xmlSerializer.endTag(namespace, "value");

            xmlSerializer.endTag(namespace, "prefix");
        }
    }

}
