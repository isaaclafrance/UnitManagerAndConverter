package com.isaacapps.unitconverterapp.dao.xml.readers.online;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.isaacapps.unitconverterapp.dao.xml.readers.AsyncXmlReader;
import com.isaacapps.unitconverterapp.models.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManagerBuilder;

import android.content.Context;

public class CurrencyUnitsMapXmlOnlineReader extends AsyncXmlReader<ArrayList<ArrayList<Unit>>,UnitManagerBuilder>{

	///
	public CurrencyUnitsMapXmlOnlineReader(Context context){
		super(context);	
	}
		
	///
	@Override
	protected ArrayList<ArrayList<Unit>> readEntity(XmlPullParser parser) throws XmlPullParserException, IOException{
		Map<String, double[]> baseConversionPolyCoeffs = new HashMap<String, double[]>();
		Map<String, Unit> unitsMap = new HashMap<String, Unit>();
		Map<String, Double> componentUnitsDimension = new HashMap<String, Double>();
		Map<String, String> currencyAbbreviationNameMap = getCurrencyAbbreviationNameMap();
		
		String unitName = "", unitSystem = "si", abbreviation = "", unitCategory = "currency_unit", tagName = "", updateTime = "";	
		String baseUnitName = "euro";

		//Create Euro unit that is the basis of all the calculations
		Unit baseUnit = new Unit(baseUnitName, unitCategory, "", unitSystem, "eur", new HashMap<String, Double>(), new Unit(), new double[]{1.0f, 0.0f});
		baseUnit.addComponentUnit(baseUnitName, 1.0f);
		baseUnit.setBaseUnit(baseUnit);
		baseUnit.setCoreUnitState(true);
		
		//Partially constructs currency units using info from xml file.
		tagName = parser.getName();
		if(tagName.equalsIgnoreCase("gesmes:Envelope")){
			parser.require(XmlPullParser.START_TAG, null, "gesmes:Envelope");
			while(parser.next() != XmlPullParser.END_TAG){
				if(parser.getEventType() != XmlPullParser.START_TAG){
					continue;
				}
				tagName = parser.getName();
				if(tagName.equalsIgnoreCase("Cube")){
					//Iterate through all elements of the XML file within the 'unit' element while storing properties pertaining to the currency unit.	
					parser.require(XmlPullParser.START_TAG, null, "Cube");
					while(parser.next() != XmlPullParser.END_TAG){
						if(parser.getEventType() != XmlPullParser.START_TAG){
							continue;
						}
						tagName = parser.getName();
						if(tagName.equalsIgnoreCase("Cube")){
							parser.require(XmlPullParser.START_TAG, null, "Cube");
							
							updateTime = "Currency updated from European Central Bank at "+readUpdateTime(parser);
							baseUnit.setDescription(updateTime);
							while(parser.next() != XmlPullParser.END_TAG){
								if(parser.getEventType() != XmlPullParser.START_TAG){
									continue;
								}
								tagName = parser.getName();
								if(tagName.equalsIgnoreCase("Cube")){
									unitName = readUnitName(parser, currencyAbbreviationNameMap);
									abbreviation = readAbbreviation(parser);
									
									componentUnitsDimension = new HashMap<String, Double>();
									componentUnitsDimension.put(unitName, 1.0);	
									
									baseConversionPolyCoeffs = readBaseConversionPolyCoeffs(parser, baseUnitName);
									
									//Use stored information from XML file to partially construct a new unit..
									Unit constructedUnit = new Unit(unitName, unitCategory, updateTime, unitSystem, abbreviation, componentUnitsDimension, baseUnit, baseConversionPolyCoeffs.get(baseUnitName));
									constructedUnit.setCoreUnitState(true);
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
			
		//
		ArrayList<ArrayList<Unit>> unitLists = new ArrayList<ArrayList<Unit>>(2);
		unitLists.add(new ArrayList<Unit>()); unitLists.get(0).add(baseUnit);
		unitLists.add(new ArrayList<Unit>(unitsMap.values()));
		
		return unitLists;
	}
	
	///	
	private String readAbbreviation(XmlPullParser parser) throws XmlPullParserException, IOException{
		return readAttribute(parser, "currency");
	}
	private String readUnitName(XmlPullParser parser, Map<String, String> currencyAbbreviationNameMap) throws XmlPullParserException, IOException{
		String abrv = readAbbreviation(parser);
		String name = currencyAbbreviationNameMap.get(abrv);
		
		return (name==null)?abrv:name;
	}
	private String readUpdateTime(XmlPullParser parser) throws XmlPullParserException, IOException{
		return readAttribute(parser, "time");
	}
	private Map<String, double[]> readBaseConversionPolyCoeffs(XmlPullParser parser, String baseUnitName) throws XmlPullParserException, IOException{
		Map<String, double[]> conversionPolyCoeffsMap = new HashMap<String, double[]>();
		double[] polynomialCoeffs = new double[]{1.0/Double.parseDouble(readAttribute(parser, "rate")), 0.0};
		conversionPolyCoeffsMap.put(baseUnitName, polynomialCoeffs);
		
		return conversionPolyCoeffsMap;
	}
	
	///
	private Map<String, String> getCurrencyAbbreviationNameMap(){
		Map<String, String> currencyAbbreviationNameMap = new HashMap<String, String>();
		
		currencyAbbreviationNameMap.put("usd", "U.S Dollar");currencyAbbreviationNameMap.put("gbp", "U.K. Pound Sterling");
		currencyAbbreviationNameMap.put("cad", "Canadian Dollar");currencyAbbreviationNameMap.put("aud", "Australian Dollar");
		currencyAbbreviationNameMap.put("chf", "Swiss Franc");currencyAbbreviationNameMap.put("jpy", "Japanese Yen");
		currencyAbbreviationNameMap.put("bgn", "Bulgarian Lev");currencyAbbreviationNameMap.put("czk", "Czech Koruna");
		currencyAbbreviationNameMap.put("dkk", "Danish Krone");currencyAbbreviationNameMap.put("huf", "Hungarian Forint");
		currencyAbbreviationNameMap.put("pln", "Polish Zloty");currencyAbbreviationNameMap.put("ron", "Romanian New Leu");
		currencyAbbreviationNameMap.put("sek", "Swedish Krona");currencyAbbreviationNameMap.put("nok", "Norwegian Krone");
		currencyAbbreviationNameMap.put("hrk", "Croatian Kuna");currencyAbbreviationNameMap.put("rub", "Russian Rouble");
		currencyAbbreviationNameMap.put("try", "Turkish Lira");currencyAbbreviationNameMap.put("brl", "Brazilian Real");
		currencyAbbreviationNameMap.put("cny", "Chinese Yuan");currencyAbbreviationNameMap.put("hkd", "Hong Kong Dollar");
		currencyAbbreviationNameMap.put("idr", "Indonesian Rupiah");currencyAbbreviationNameMap.put("ils", "Israeli New Sheqel");
		currencyAbbreviationNameMap.put("inr", "Indian Rupee");currencyAbbreviationNameMap.put("krw", "South Korean Won");
		currencyAbbreviationNameMap.put("mxn", "Mexican Peso");currencyAbbreviationNameMap.put("myr", "Malaysian Ringgit");
		currencyAbbreviationNameMap.put("nzd", "New Zealand Dollar");currencyAbbreviationNameMap.put("php", "Philippine Peso");
		currencyAbbreviationNameMap.put("sgd", "Singapore Dollar");currencyAbbreviationNameMap.put("thb", "Thai Baht");
		currencyAbbreviationNameMap.put("zar", "South African Rand");
		
		return currencyAbbreviationNameMap;	
	}
	
	//// Loader Methods
	@Override
	public UnitManagerBuilder loadInBackground() {
		ArrayList<ArrayList<Unit>> currencyUnitsGroup = new ArrayList<ArrayList<Unit>>();
		try {
			currencyUnitsGroup = parseXML(openXmlFile("http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml", true));
		} catch (Exception e){
			e.printStackTrace();
		}
				
		return new UnitManagerBuilder().setNonBaseUnitsComponent(currencyUnitsGroup.get(1))
									   .setBaseUnitsComponent(currencyUnitsGroup.get(0));
	}
}
