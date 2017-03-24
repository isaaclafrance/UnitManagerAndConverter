package com.isaacapps.unitconverterapp.dao.xml.readers.local;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;

import com.isaacapps.unitconverterapp.dao.xml.readers.AsyncXmlReader;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManagerBuilder;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager.UNIT_TYPE;

public class FundUnitsMapXmlLocalReader extends AsyncXmlReader<Map<String, Map<String, UNIT_TYPE>>, UnitManagerBuilder>{

	///
	public FundUnitsMapXmlLocalReader(Context context){
		super(context);
	}
		
	///
	protected Map<String, Map<String, UNIT_TYPE>> readEntity(XmlPullParser parser) throws XmlPullParserException, IOException{
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
						unitSystemName = readUnitSystem(parser);
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
	
	///
	private String readUnitSystem(XmlPullParser parser) throws XmlPullParserException, IOException{
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
		
		String dimensionName = readText(parser).toUpperCase();
		UNIT_TYPE dimensionType = UNIT_TYPE.valueOf(dimensionName);
				
		parser.require(XmlPullParser.END_TAG, null, "type");		
		return dimensionType;
	}
	
	
	//// Loader Methods
	@Override
	public UnitManagerBuilder loadInBackground() {
		UnitManagerBuilder unitManagerBuilderBundle = new UnitManagerBuilder();
		try {
			unitManagerBuilderBundle.setFundUnitsMapComponent(parseXML(openAssetFile("FundamentalUnits.xml")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return unitManagerBuilderBundle;
	}
}
