package com.isaacapps.unitconverterapp.dao.xml.readers.online;

import java.io.IOException;
import java.util.*;

import org.xmlpull.v1.*;

import com.isaacapps.unitconverterapp.dao.xml.readers.AsyncXmlReader;
import com.isaacapps.unitconverterapp.models.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManagerBuilder;

import android.content.Context;

//Completely ignores XML namespaces.
///According to official Google Android documentation, the XmlPullParser that reads one tag at a time is the most efficient way of parsing especially in situations where there are a large number of tags.
public class CurrencyUnitsMapXmlOnlineReader extends AsyncXmlReader<List<List<Unit>>,UnitManagerBuilder>{
	Map<String, String> currencyAbbreviationNameMap;
	
	///
	public CurrencyUnitsMapXmlOnlineReader(Context context){
		super(context);	
		setCurrencyAbbreviationNameMap();
	}
		
	///
	@Override
	protected List<List<Unit>> readEntity(XmlPullParser parser) throws XmlPullParserException, IOException{
		Map<String, Unit> unitsMap = new HashMap<String, Unit>();
		
		String unitName = "", unitSystem = "si", abbreviation = "", unitCategory = "currency_unit", tagName = "";	
		String baseUnitName = "euro";

		//Create Euro unit that is the basis of all the calculations
		Unit baseUnit = new Unit(baseUnitName, unitCategory, "", unitSystem, "eur", new HashMap<String, Double>(), new Unit(), new double[]{1.0, 0.0f});
		baseUnit.addComponentUnit(baseUnitName, 1.0, false);
		baseUnit.setBaseUnit(baseUnit);
		baseUnit.setCoreUnitState(true);
		
		//Sift through the too many nested cube tags.
		tagName = parser.getName();
		if(tagName.equalsIgnoreCase("gesmes:Envelope")){
			parser.require(XmlPullParser.START_TAG, null, "gesmes:Envelope");
			while(parser.next() != XmlPullParser.END_TAG){
				if(parser.getEventType() != XmlPullParser.START_TAG){
					continue;
				}
				tagName = parser.getName();
				if(tagName.equalsIgnoreCase("Cube")){
					parser.require(XmlPullParser.START_TAG, null, "Cube");
					while(parser.next() != XmlPullParser.END_TAG){
						if(parser.getEventType() != XmlPullParser.START_TAG){
							continue;
						}
						tagName = parser.getName();
						if(tagName.equalsIgnoreCase("Cube")){
							parser.require(XmlPullParser.START_TAG, null, "Cube");
							
							baseUnit.setDescription("Currency updated from European Central Bank at "+readUpdateTime(parser));
							while(parser.next() != XmlPullParser.END_TAG){
								if(parser.getEventType() != XmlPullParser.START_TAG){
									continue;
								}
								tagName = parser.getName();
								if(tagName.equalsIgnoreCase("Cube")){
									Unit constructedUnit = readUnit(parser, baseUnit, unitName,unitSystem,abbreviation, unitCategory);
									constructedUnit.setCoreUnitState(true);
									
									unitsMap.put(constructedUnit.getName(), constructedUnit);
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
		List<List<Unit>> unitLists = new ArrayList<List<Unit>>(2);
		unitLists.add(new ArrayList<Unit>()); unitLists.get(0).add(baseUnit);
		unitLists.add(new ArrayList<Unit>(unitsMap.values()));
		
		return unitLists;
	}
	
	///
	private Unit readUnit(XmlPullParser parser, Unit baseUnit, String unitName, String unitSystem, String abbreviation, String unitCategory) throws XmlPullParserException, IOException{
		parser.require(XmlPullParser.START_TAG, null, "Cube");
		
		Map<String, Double> componentUnitsDimension = new HashMap<String, Double>();
				
		unitName = readUnitName(parser);
		abbreviation = readAbbreviation(parser);
		
		componentUnitsDimension = new HashMap<String, Double>();
		componentUnitsDimension.put(unitName, 1.0);	
				
		parser.nextTag();
		
		return new Unit(unitName, unitCategory, baseUnit.getDescription(), unitSystem, abbreviation, componentUnitsDimension, baseUnit, readBaseConversionPolyCoeffs(parser));
	}
	
	///	
	private String readAbbreviation(XmlPullParser parser) throws XmlPullParserException, IOException{
		return readAttribute(parser, "currency");
	}
	private String readUnitName(XmlPullParser parser) throws XmlPullParserException, IOException{
		String abrv = readAbbreviation(parser);
		String name = currencyAbbreviationNameMap.get(abrv);
		
		return (name==null)?abrv:name;
	}
	private String readUpdateTime(XmlPullParser parser) throws XmlPullParserException, IOException{
		return readAttribute(parser, "time");
	}
	private double[] readBaseConversionPolyCoeffs(XmlPullParser parser) throws XmlPullParserException, IOException{
		return new double[]{1.0/Double.parseDouble(readAttribute(parser, "rate")), 0.0};
	}
	
	///
	private void setCurrencyAbbreviationNameMap(){
		currencyAbbreviationNameMap = new HashMap<String, String>();
		
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
	}
	
	//// Loader Methods
	@Override
	public UnitManagerBuilder loadInBackground() {
		List<List<Unit>> currencyUnitsGroup = new ArrayList<List<Unit>>();
		try {
			currencyUnitsGroup = parseXML(openXmlFile("http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml", true));
		} catch (Exception e){
			e.printStackTrace();
		}
				
		return new UnitManagerBuilder().addNonBaseUnits(currencyUnitsGroup.get(1))
									   .addBaseUnits(currencyUnitsGroup.get(0));
	}
}
