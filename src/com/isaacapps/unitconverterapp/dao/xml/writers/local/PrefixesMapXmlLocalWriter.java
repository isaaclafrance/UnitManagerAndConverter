package com.isaacapps.unitconverterapp.dao.xml.writers.local;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.xmlpull.v1.XmlSerializer;

import com.isaacapps.unitconverterapp.dao.xml.writers.XmlWriter;

import android.content.Context;

public class PrefixesMapXmlLocalWriter extends XmlWriter<Map<String, Double>>{

	///
	public PrefixesMapXmlLocalWriter(Context context){	
		super(context);
		destination = "DynamicPrefixes.xml";
	}
	
	///
	@Override
	protected void writeEntity(XmlSerializer serializer, String namespace, Map<String, Double>  prefixes) throws IllegalArgumentException, IllegalStateException, IOException{
		for(Entry<String, Double> entry:prefixes.entrySet()){
			serializer.startTag(namespace, "prefix");
				serializer.startTag(namespace, "name");
					serializer.text(entry.getKey());
				serializer.endTag(namespace, "name");
				serializer.startTag(namespace, "value");
					serializer.text(entry.getValue().toString());
				serializer.endTag(namespace, "value");
			serializer.endTag(namespace, "prefix");
		}
	}
	
}
