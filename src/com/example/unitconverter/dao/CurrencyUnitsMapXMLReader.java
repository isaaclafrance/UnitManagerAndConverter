package com.example.unitconverter.dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.example.unitconverter.Unit;
import com.example.unitconverter.UnitManagerFactory;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Xml;

public class CurrencyUnitsMapXMLReader extends AsyncTaskLoader<UnitManagerFactory>{
	//Fields
	private boolean isSourceOnline; //Determines if XML loaded from an online server or from local source.
	private String currencyUnitsXmlSource;
	private String baseUnitName = "euro";
	
	//Constructor
	public CurrencyUnitsMapXMLReader(Context context){
		super(context);
		this.isSourceOnline = true;
		currencyUnitsXmlSource = "http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";
	}
	public CurrencyUnitsMapXMLReader(Context context,String coreUnitsXmlSource){
		super(context);
		this.isSourceOnline = false;
		this.currencyUnitsXmlSource = coreUnitsXmlSource;
	}
	
	////TODO: Process Units XML's elements. Include a way to cache for later processing units that refer to component units that have not been loaded yet.
	public ArrayList<ArrayList<Unit>> loadUnitsFromXML(InputStream in) throws XmlPullParserException, IOException{
		try{
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);			
			parser.setInput(in, null);
			parser.nextTag();
			return readUnitsXML(parser);
		}
		finally{
			in.close();
		}
	}
	private ArrayList<ArrayList<Unit>> readUnitsXML(XmlPullParser parser) throws XmlPullParserException, IOException{
		Map<String, double[]> baseConversionPolyCoeffs = new HashMap<String, double[]>();
	
		Map<String, Unit> unitsMap = new HashMap<String, Unit>();
		
		Map<String, Double> componentUnitsExponentMap = new HashMap<String, Double>();
		
		String unitName = "", unitSystem = "si", abbreviation = "", unitCategory = "currency_unit", tagName = "", updateTime = "";

		//Create Euro unit that is the basis of all the calculations
		Unit baseUnit = new Unit(baseUnitName, unitCategory, "", unitSystem, "eur", new HashMap<String, Double>(), new Unit(), new double[]{1.0f, 0.0f});
		baseUnit.addComponentUnit(baseUnitName, 1.0f);
		baseUnit.setBaseUnit(baseUnit);
		
		//Partially constructs currency units units info for xml file.
		tagName = parser.getName();
		if(tagName.equalsIgnoreCase("gesmes:Envelope")){
			parser.require(XmlPullParser.START_TAG, null, "gesmes:Envelope");
			while(parser.next() != XmlPullParser.END_TAG){
				if(parser.getEventType() != XmlPullParser.START_TAG){
					continue;
				}
				tagName = parser.getName();
				if(tagName.equalsIgnoreCase("Cube")){
					//Iterate through all elements of the XML file within the 'unit' element while storing properties pertaining to the unit.	
					parser.require(XmlPullParser.START_TAG, null, "Cube");
					while(parser.next() != XmlPullParser.END_TAG){
						if(parser.getEventType() != XmlPullParser.START_TAG){
							continue;
						}
						tagName = parser.getName();
						if(tagName.equalsIgnoreCase("Cube")){
							parser.require(XmlPullParser.START_TAG, null, "Cube");
							
							updateTime = readUpdateTime(parser);
							baseUnit.setDescription(updateTime);
							while(parser.next() != XmlPullParser.END_TAG){
								if(parser.getEventType() != XmlPullParser.START_TAG){
									continue;
								}
								tagName = parser.getName();
								if(tagName.equalsIgnoreCase("Cube")){
									unitName = readUnitName(parser);
									abbreviation = readAbbreviation(parser);
									
									//Set component units
									componentUnitsExponentMap = new HashMap<String, Double>();
									componentUnitsExponentMap.put(unitName, 1.0);	
									
									//Read base unit name and associated conversion polynomials
									baseConversionPolyCoeffs = readBaseConversionPolyCoeffs(parser);
									
									//Use stored information from XML file to partially construct a new unit. The created units are missing base unit info, component unit info, and conversion polynomial info. Then places the unit in the unit map.
									Unit constructedUnit = new Unit(unitName, unitCategory, updateTime, unitSystem, abbreviation, componentUnitsExponentMap, baseUnit, baseConversionPolyCoeffs.get(baseUnitName));
									
									//Adds newly constructed unit to base units map if base unit otherwise adds unit to dynamic units map.
									unitsMap.put(unitName, constructedUnit);
									
									parser.nextTag();
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
			}
		}
				
		//**Completes the construction of the previous constructed unit by adding missing elements i.e. conversion polynomial coefficients, and base unit.
		//**Accomplishes this task by finding actual unit references for the unit names associated with the componentUnitsExponentMap and the conversionPolynomialCoeffsMap.
				
		//Adds base unit and associated conversion polynomial info to each partially constructed unit
		for(Unit partialUnit:unitsMap.values()){
			partialUnit.setBaseUnit(baseUnit, partialUnit.getBaseConversionPolyCoeffs());
		}
		
		//
		ArrayList<ArrayList<Unit>> mapArray = new ArrayList<ArrayList<Unit>>(2);
		mapArray.add(new ArrayList<Unit>()); mapArray.get(0).add(baseUnit);
		mapArray.add(new ArrayList<Unit>(unitsMap.values()));
		
		return mapArray;
	}
	
	////  Methods for retrieving unit specific elements from XML	
	private String readAbbreviation(XmlPullParser parser) throws XmlPullParserException, IOException{
		return readAttribute(parser, "currency");
	}
	private String readUnitName(XmlPullParser parser) throws XmlPullParserException, IOException{
		return readAbbreviation(parser);
	}
	private String readUpdateTime(XmlPullParser parser) throws XmlPullParserException, IOException{
		return readAttribute(parser, "time");
	}
	private Map<String, double[]> readBaseConversionPolyCoeffs(XmlPullParser parser) throws XmlPullParserException, IOException{
		Map<String, double[]> conversionPolyCoeffsMap = new HashMap<String, double[]>();
		double[] polynomialCoeffs = new double[]{Double.parseDouble(readAttribute(parser, "rate")), 0.0};
		conversionPolyCoeffsMap.put(baseUnitName, polynomialCoeffs);
		
		return conversionPolyCoeffsMap;
	}
	private String readAttribute(XmlPullParser parser, String attributeName) throws XmlPullParserException, IOException{
		return parser.getAttributeValue(null, attributeName).toLowerCase();
	}
	
	////  Methods for retrieving basic elements from XML
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
	
	//// Loader Methods
	@Override
	public UnitManagerFactory loadInBackground() {
		ArrayList<ArrayList<Unit>> currencyUnitsGroup = new ArrayList<ArrayList<Unit>>();
		
		try{
			if(isSourceOnline){
				URL onlineFileCoreUnits = new URL(currencyUnitsXmlSource);	
				currencyUnitsGroup = loadUnitsFromXML(onlineFileCoreUnits.openStream());
			}else{					
				currencyUnitsGroup = loadUnitsFromXML(getContext().getAssets().open(currencyUnitsXmlSource));				
			} 
			
			/*for(int i=0; i<currencyUnitsGroup.size(); i++){
				for(Unit unit:currencyUnitsGroup.get(i)){
					unit.setCoreUnitState(true);
				}
			}*/
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		UnitManagerFactory umcBundle = new UnitManagerFactory();
		umcBundle.setNonBaseUnitsComponent(currencyUnitsGroup.get(0));
		umcBundle.setBaseUnitsComponent(currencyUnitsGroup.get(1));
		
		return umcBundle;
	}
}
