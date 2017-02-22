package com.example.unitconverter.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Xml;

public class ConversionFavoritesListXMLReader extends AsyncTaskLoader<ArrayList<String>>{
	//Fields
	private boolean isSourceOnline;
	private String favoritesXmlSource;
		
	//Constructor 
	public ConversionFavoritesListXMLReader(Context context){
		super(context);
		this.isSourceOnline = false;
		favoritesXmlSource = context.getFilesDir().getPath().toString() + "ConversionFavorites.xml";
	}
	public ConversionFavoritesListXMLReader(Context context, boolean isSourceOnline, String favoritesXmlSource){
		this(context);
		this.favoritesXmlSource = favoritesXmlSource;
	}
	
	//Load loads conversion unit pairs from XML file
	public ArrayList<String> loadFavoritesFromXML(InputStream in) throws XmlPullParserException, IOException{
		try{
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			
			ArrayList<String> conversionFavorites = readConversionFavorites(parser); 
			Collections.sort( conversionFavorites );
			
			return conversionFavorites;
		}
		finally{
			in.close();
		}
	}
	private ArrayList<String> readConversionFavorites(XmlPullParser parser) throws XmlPullParserException, IOException{
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
						if(tagName.equalsIgnoreCase("fromUnit")){
							fromUnit = readText(parser);
						}
						else if(tagName.equalsIgnoreCase("toUnit")){							
							conversionFavorites.add(unitCategory.toUpperCase() + ": " + fromUnit + " --> "+ readText(parser));
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
	
 	private void skip(XmlPullParser parser) throws XmlPullParserException, IOException{
		if(parser.getEventType() != XmlPullParser.START_TAG){
			throw new IllegalStateException();
		}
		int depth = 1;
		while(depth != 0){
			switch(parser.next()){
				case XmlPullParser.END_TAG:
					depth--;
					break;
				case XmlPullParser.START_TAG:
					depth++;
					break;				
			}
		}
	}
	private String readText(XmlPullParser parser)throws IOException, XmlPullParserException{
		String text = "";
		if(parser.next() == XmlPullParser.TEXT){
			text = parser.getText().toLowerCase();
			parser.nextTag();
		}
		return text;
	}

	@Override
	public ArrayList<String> loadInBackground() {
		ArrayList<String> conversionFavorites = new ArrayList<String>();
		
		try {
			if(isSourceOnline){
				URL onlineFavoriteUnits = new URL(favoritesXmlSource);	
				conversionFavorites = loadFavoritesFromXML(onlineFavoriteUnits.openStream());
			}else{
				File faveUnitsFile = new File(favoritesXmlSource);		
				if(!faveUnitsFile.exists()){
					faveUnitsFile.createNewFile();
				}
				
				conversionFavorites = loadFavoritesFromXML(new FileInputStream(favoritesXmlSource));
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return conversionFavorites;
	}
	
}
