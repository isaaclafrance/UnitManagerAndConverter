package com.example.unitconverter.dao;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Map.Entry;

import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;

public class PrefixesMapXmlWriter {
	//Fields
	private static String prefixesXmlDestination = "DynamicPrefixes.xml";
	
	//Constructors
	public PrefixesMapXmlWriter(){	
	}
	
	public static void savePrefixToXML(Context context, Map<String, Double> prefixDictionary) throws XmlPullParserException, IllegalArgumentException, IllegalStateException, IOException{
		OutputStream xmlFileOutputStream = new FileOutputStream(context.getFilesDir().getPath().toString() + prefixesXmlDestination, false);
		Writer xmlStreamWriter = new OutputStreamWriter(xmlFileOutputStream);			
		
		try{
			String namespace = "";
			XmlPullParserFactory xpFactory = XmlPullParserFactory.newInstance();
			XmlSerializer serializer = xpFactory.newSerializer();

			serializer.setOutput(xmlStreamWriter);
			serializer.startDocument("UTF-8", true);
			serializer.startTag(namespace, "main");
			
			for(Entry<String, Double> entry:prefixDictionary.entrySet()){
				serializer.startTag(namespace, "prefix");
					serializer.startTag(namespace, "name");
						serializer.text(entry.getKey());
					serializer.endTag(namespace, "name");
					serializer.startTag(namespace, "value");
						serializer.text(Double.toString(entry.getValue()));
					serializer.endTag(namespace, "value");
				serializer.endTag(namespace, "prefix");
			}
			
			serializer.endTag(namespace, "main");	
			serializer.endDocument();
			serializer.flush();
		}
		finally{
			xmlStreamWriter.flush();
			xmlStreamWriter.close();
			xmlFileOutputStream.flush();
			xmlFileOutputStream.close();			
		}
	}
	
}
