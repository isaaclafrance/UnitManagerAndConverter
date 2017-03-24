package com.isaacapps.unitconverterapp.dao.xml.readers.local;

import java.io.IOException;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.isaacapps.unitconverterapp.dao.xml.readers.AsyncXmlReader;

import android.content.Context;

public class ConversionFavoritesListXmlLocalReader extends AsyncXmlReader<ArrayList<String>,ArrayList<String>>{

	///
	public ConversionFavoritesListXmlLocalReader(Context context){
		super(context);
	}
	
	///
	@Override
	protected ArrayList<String> readEntity(XmlPullParser parser) throws XmlPullParserException, IOException{
		ArrayList<String> conversionFavorites = new ArrayList<String>();
		String unitCategory = "";
		String fromUnit = "";
		String tagName = parser.getName();
	
		if(tagName.equalsIgnoreCase("main")){
			parser.require(XmlPullParser.START_TAG, null, "main");
			while(parser.next() != XmlPullParser.END_TAG){
				if(parser.getEventType() != XmlPullParser.START_TAG){
					continue;
				}
				tagName = parser.getName();
				if(tagName.equalsIgnoreCase("conversion")){
					parser.require(XmlPullParser.START_TAG, null, "conversion");
					while(parser.next() != XmlPullParser.END_TAG){
						if(parser.getEventType() != XmlPullParser.START_TAG){
							continue;
						}
						tagName = parser.getName();
						if(tagName.equalsIgnoreCase("unitCategory")){
							unitCategory = readText(parser);
						}
						else if(tagName.equalsIgnoreCase("fromUnit")){
							fromUnit = readText(parser).toLowerCase();
						}
						else if(tagName.equalsIgnoreCase("toUnit")){							
							conversionFavorites.add(unitCategory.toUpperCase() + ": " + fromUnit + " --> "+ readText(parser).toLowerCase());
						}
						else{
							skip(parser);
						}
					}
				}
				else{
					skip(parser);
				}
			}
		}
		else{
			skip(parser);
		}		

		return conversionFavorites;
	}
	
	///
	@Override
	public ArrayList<String> loadInBackground() {
		ArrayList<String> entities = new ArrayList<String>();
		try {
			entities = parseXML(openXmlFile(getContext().getFilesDir().getPath().toString() + "ConversionFavorites.xml", false));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return entities;
	}
	
}
