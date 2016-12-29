package com.example.unitconverter.dao;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Xml;

import com.example.unitconverter.UnitManager;
import com.example.unitconverter.UnitManagerFactory;
import com.example.unitconverter.UnitManager.UNIT_TYPE;

//Loads and creates a map of unit systems and unit namee that are associates with fundamental units
public class FundUnitsMapXmlReader extends AsyncTaskLoader<UnitManagerFactory>{
	//Fields
	private XmlPullParser parser;
	private boolean isSourceOnline; //Determines if XML loaded from an online server or from local source.
	private String fundUnitsXmlSource;
	
	//Constructors
	public FundUnitsMapXmlReader(Context context){
		super(context);
		this.isSourceOnline = false;
		fundUnitsXmlSource = "FundamentalUnits.xml";
		parser = Xml.newPullParser();
	}
	public FundUnitsMapXmlReader(Context context, boolean isSourceOnline, String fundUnitsXmlSource){
		this(context);
		this.isSourceOnline = isSourceOnline;
		this.fundUnitsXmlSource = fundUnitsXmlSource;
	}
		
	////TODO:Process Fundamental Unit XML's elements. Make sure that the fundamental units XML file is read and processed before the all the units have been loaded.
	public Map<String, Map<String, UNIT_TYPE>> loadFundUnitsFromXML(InputStream in) throws XmlPullParserException, IOException{
		try{
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			return readFundUnitsXML(parser);
		}
		finally{
			in.close();
		}
	}
	private Map<String, Map<String, UNIT_TYPE>> readFundUnitsXML(XmlPullParser parser) throws XmlPullParserException, IOException{
		String tagName = "";
		String unitSystemName = "";
		Map<String, UNIT_TYPE> unitSystemDimensionAssoc = new HashMap<String, UnitManager.UNIT_TYPE>();
		Map<String, Map<String, UNIT_TYPE>> fundamentalUnitsMap = new HashMap<String, Map<String,UNIT_TYPE>>();
		
		parser.require(XmlPullParser.START_TAG, null, "main");
		while(parser.next() != XmlPullParser.END_TAG){
			if(parser.getEventType() != XmlPullParser.START_TAG){
				continue;
			}
			tagName = parser.getName();
			if(tagName.equalsIgnoreCase("unitSystem")){
				parser.require(XmlPullParser.START_TAG, null, "unitSystem");
				while(parser.next() != XmlPullParser.END_TAG){
					if(parser.getEventType() != XmlPullParser.START_TAG){
						continue;
					}
					tagName = parser.getName();
					if(tagName.equalsIgnoreCase("name")){
						unitSystemName = readUnitSystemName(parser);
					}
					else if(tagName.equalsIgnoreCase("dimensions")){
						unitSystemDimensionAssoc = readDimensionAssoc(parser);
					}else{
						skip(parser);
					}
				}
			}
			else{
				skip(parser);
			}
			fundamentalUnitsMap.put(unitSystemName, unitSystemDimensionAssoc);
		}
		
		return fundamentalUnitsMap;
	}
	private String readUnitSystemName(XmlPullParser parser) throws XmlPullParserException, IOException{
		parser.require(XmlPullParser.START_TAG, null, "name");
		String name = readText(parser).toLowerCase();
		parser.require(XmlPullParser.END_TAG, null, "name");
		return name;
	}
	private Map<String, UNIT_TYPE> readDimensionAssoc(XmlPullParser parser) throws XmlPullParserException, IOException{
		String tagName = "";
		Map<String, UNIT_TYPE> dimensionAssociations = new HashMap<String, UnitManager.UNIT_TYPE>();
		String unitName = "";
		UNIT_TYPE dimensionType = UNIT_TYPE.UNKNOWN;
		
		parser.require(XmlPullParser.START_TAG, null, "dimensions");
		while(parser.next() != XmlPullParser.END_TAG){
			if(parser.getEventType() != XmlPullParser.START_TAG){
				continue;
			}
			tagName = parser.getName();
			if(tagName.equalsIgnoreCase("dimension")){
				parser.require(XmlPullParser.START_TAG, null, "dimension");
				while(parser.next() != XmlPullParser.END_TAG){
					if(parser.getEventType() != XmlPullParser.START_TAG){
						continue;
					}
					tagName = parser.getName();
					if(tagName.equalsIgnoreCase("type")){
						dimensionType = readDimensionType(parser);
					}else if(tagName.equalsIgnoreCase("unit")){
						unitName = readUnitName(parser);
					}
					else{
						skip(parser);
					}
				}
			}else{
				skip(parser);
			}
			dimensionAssociations.put(unitName, dimensionType);
		}
		return dimensionAssociations;
	}
	private String readUnitName(XmlPullParser parser) throws XmlPullParserException, IOException{
		parser.require(XmlPullParser.START_TAG, null, "unit");
		String unitName = readText(parser).toLowerCase();
		parser.require(XmlPullParser.END_TAG, null, "unit");
		return unitName;
		
	}
	private UNIT_TYPE readDimensionType(XmlPullParser parser) throws XmlPullParserException, IOException{
		parser.require(XmlPullParser.START_TAG, null, "type");
		
		String dimensionName = readText(parser);
		UNIT_TYPE dimensionType = UNIT_TYPE.valueOf(dimensionName);
				
		parser.require(XmlPullParser.END_TAG, null, "type");		
		return dimensionType;
	}
	
	////  Methods for Units Processing
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
			text = parser.getText();
			parser.nextTag();
		}
		return text;
	}
	
	//// Loader Methods
	@Override
	public UnitManagerFactory loadInBackground() {
		Map<String, Map<String, UNIT_TYPE>> map_FundUnits = new HashMap<String, Map<String,UNIT_TYPE>>();
		
		try {
			if(isSourceOnline){
				URL onlineFileFundUnits = new URL(fundUnitsXmlSource);	
				map_FundUnits = loadFundUnitsFromXML(onlineFileFundUnits.openStream());
			}else{
				map_FundUnits = loadFundUnitsFromXML(getContext().getAssets().open(fundUnitsXmlSource));
			}
			
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		UnitManagerFactory umFac = new UnitManagerFactory();
		umFac.setFundUnitsMapComponent(map_FundUnits);
		
		return umFac;
	}
}
