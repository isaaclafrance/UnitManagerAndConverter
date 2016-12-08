package com.example.unitconverter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;

public class ConversionFavoritesListXMLWriter {
	//Fields
	private String favoritesXmlDestination;
		
	//Constructor 
	public ConversionFavoritesListXMLWriter(){
		favoritesXmlDestination = "ConversionFavorites.xml";
	}
	
	//Saves unit pairs involved in conversion into a XML file
	public void saveToXML(Context context, ArrayList<String> favoritesConversions) throws XmlPullParserException, IllegalArgumentException, IllegalStateException, IOException{
		OutputStream xmlFileOutputStream = context.openFileOutput(favoritesXmlDestination, 0);
		Writer xmlStreamWriter = new OutputStreamWriter(xmlFileOutputStream); 
		
		try{
			String[] categoryNconversionUnitsNames;
			String[] conversionUnitNames;
			
			String namespace = "";
			XmlPullParserFactory xpFactory = XmlPullParserFactory.newInstance();
			XmlSerializer serializer = xpFactory.newSerializer();
			
			serializer.setOutput(xmlStreamWriter);
			serializer.startDocument("UTF-8", true);
			serializer.startTag(namespace, "main");
			
			for(String conversionGroup:favoritesConversions){
				categoryNconversionUnitsNames = conversionGroup.split(": ");
				conversionUnitNames = categoryNconversionUnitsNames[1].split(" --> ");
				
				serializer.startTag(namespace, "conversion");
					serializer.startTag(namespace, "unitCategory");
						serializer.text(categoryNconversionUnitsNames[0]);
					serializer.endTag(namespace, "unitCategory");
					serializer.startTag(namespace, "fromUnit");
						serializer.text(conversionUnitNames[0]);
					serializer.endTag(namespace, "fromUnit");
					serializer.startTag(namespace, "toUnit");
						serializer.text(conversionUnitNames[1]);
					serializer.endTag(namespace, "toUnit");
				serializer.endTag(namespace, "conversion");	
			}
			
			serializer.endTag(namespace, "main");	
			serializer.endDocument();
			serializer.flush();
		}finally{
			xmlStreamWriter.flush();
			xmlStreamWriter.close();
			xmlFileOutputStream.flush();
			xmlFileOutputStream.close();			
		}		
	}
}
