package com.isaacapps.unitconverterapp.dao.xml.readers.local;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.isaacapps.unitconverterapp.dao.xml.readers.AsyncXmlReader;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManagerBuilder;

import android.content.Context;

public class PrefixesMapXmlLocalReader extends AsyncXmlReader<Map<String, Double>, UnitManagerBuilder>{

	///
	public PrefixesMapXmlLocalReader(Context context){
		super(context);
	}
		
	///
	@Override
	protected Map<String, Double> readEntity(XmlPullParser parser) throws XmlPullParserException, IOException{
		String tagName = "";
		String prefixName = "";
		String abbreviation = "";
		double prefixValue = 0.0;
		Map<String, Double> prefixMap = new HashMap<String, Double>();
		
		//
		tagName = parser.getName();
		if(tagName.equalsIgnoreCase("main")){
			parser.require(XmlPullParser.START_TAG, null, "main");
			while(parser.next() != XmlPullParser.END_TAG){
				if(parser.getEventType() != XmlPullParser.START_TAG){
					continue;
				}
				tagName = parser.getName();
				if(tagName.equalsIgnoreCase("prefix")){
					while(parser.next() != XmlPullParser.END_TAG){
						if(parser.getEventType() != XmlPullParser.START_TAG){
							continue;
						}
						tagName = parser.getName();
						if(tagName.equalsIgnoreCase("name")){
							prefixName = readText(parser).toLowerCase();
						}else if(tagName.equalsIgnoreCase("abbreviation")){
							abbreviation = readText(parser);
						}else if(tagName.equalsIgnoreCase("value")){
							prefixValue = readDouble(parser);
						}else{
							skip(parser);
						}	
					}
					prefixMap.put(prefixName +"::"+ abbreviation, prefixValue);
				}
				else{
					skip(parser);
				}
			}
		}

		return prefixMap;
	}
	
	///
	@Override
	public UnitManagerBuilder loadInBackground() {
		UnitManagerBuilder unitManagerBuilderBundle = new UnitManagerBuilder();
		try {
			unitManagerBuilderBundle.setCorePrefixesNAbbreviationsMapComponent(parseXML(openAssetFile("StandardCorePrefixes.xml")))
									.setDynamicPrefixesNAbbreviationsMapComponent(parseXML(openXmlFile(getContext().getFilesDir().getPath().toString() + "DynamicPrefixes.xml", false)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return unitManagerBuilderBundle;
	}
	    
}
