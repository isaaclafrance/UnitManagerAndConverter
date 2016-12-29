package com.example.unitconverter.dao;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.example.unitconverter.UnitManagerFactory;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;

import android.util.Xml;

public class PrefixesMapXmlReader extends AsyncTaskLoader<UnitManagerFactory>{
	//Fields
	private XmlPullParser parser;
	private boolean isSourceOnline; //Determines if XML loaded from an online server or from local source.
	private String corePrefixesXmlSource;
	private String dynamicPrefixesXmlSource = "DynamicPrefixes.xml";
	
	//Constructor
	public PrefixesMapXmlReader(Context context){
		super(context);
		this.isSourceOnline = false;
		corePrefixesXmlSource = "StandardCorePrefixes.xml";
		parser = Xml.newPullParser();
	}
	public PrefixesMapXmlReader(Context context, boolean isSourceOnline, String corePrefixesXmlSource){
		this(context);
		this.isSourceOnline = isSourceOnline;
		this.corePrefixesXmlSource = corePrefixesXmlSource;
	}
		
	//// Process Prefix XML's elements
	public Map<String, Double> loadPrefixesNAbbreviationsFromXML(InputStream in) throws XmlPullParserException, IOException{
		try{
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			return readPrefixXML(parser);
		}
		finally{
			in.close();
		}
	}
	
	private Map<String, Double> readPrefixXML(XmlPullParser parser) throws XmlPullParserException, IOException{
		String tagName = "";
		////
		String prefixName = "";
		String abbrev = "";
		double prefixValue = 0.0f;
		////
		Map<String, Double> prefixMap = new HashMap<String, Double>();
		////
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
							prefixName = readText(parser);
						}else if(tagName.equalsIgnoreCase("abbreviation")){
							abbrev = readText(parser);
						}else if(tagName.equalsIgnoreCase("value")){
							prefixValue = readdouble(parser);
						}else{
							skip(parser);
						}	
					}
					prefixMap.put(prefixName +"::"+ abbrev, prefixValue);
				}
				else{
					skip(parser);
				}
			}
		}
		return prefixMap;
	}
	
	//// Methods for Prefix Processing
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
	private double readdouble(XmlPullParser parser) throws IOException, XmlPullParserException{
		return Double.valueOf(readText(parser));
	}
	
	//// Loader Methods
	@Override
	public UnitManagerFactory loadInBackground() {
		Map<String, Double> map_CorePrefixesNAbbreviations = new HashMap<String, Double>();
		Map<String, Double> map_DynamicPrefixesNAbbreviations = new HashMap<String, Double>();
		
		try{
			if(isSourceOnline){
				URL onlineFileCorePrefixes = new URL(corePrefixesXmlSource);	
				map_CorePrefixesNAbbreviations = loadPrefixesNAbbreviationsFromXML(onlineFileCorePrefixes.openStream());
			}else{					
				map_CorePrefixesNAbbreviations = loadPrefixesNAbbreviationsFromXML(getContext().getAssets().open(corePrefixesXmlSource));				
			}
			
			File fileDPrefixes = new File("DynamicPrefixes.xml");		
					
			if(!fileDPrefixes.exists()){
				fileDPrefixes.createNewFile();
			}
		
			map_DynamicPrefixesNAbbreviations = loadPrefixesNAbbreviationsFromXML(getContext().openFileInput(dynamicPrefixesXmlSource));
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		UnitManagerFactory umFac = new UnitManagerFactory();
		umFac.setCorePrefixesNAbbreviationsMapComponent(map_CorePrefixesNAbbreviations);
		umFac.setDynamicPrefixesNAbbreviationsMapComponent(map_DynamicPrefixesNAbbreviations);
		
		return umFac;
	}
	    
}
