package com.isaacapps.unitconverterapp.dao.xml.readers.local;

import java.io.IOException;
import java.util.*;

import org.xmlpull.v1.*;

import com.isaacapps.unitconverterapp.dao.xml.readers.AsyncXmlReader;
import com.isaacapps.unitconverterapp.models.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManagerBuilder;

import android.content.Context;

///According to official Google Android documentation, the XmlPullParser that reads one tag at a time is the most efficient way of parsing especially in situations where there are a large number of tags.
public class UnitsMapXmlLocalReader extends AsyncXmlReader<ArrayList<ArrayList<Unit>>,UnitManagerBuilder> {
	Map<String, Unit> baseUnitsMap;
	Map<String, Unit> nonBaseUnitsMap;
	ArrayList<Unit> partiallyConstructedUnits;
	
	///
	public UnitsMapXmlLocalReader(Context context) {
		super(context);
		baseUnitsMap = new HashMap<String, Unit>();
		nonBaseUnitsMap = new HashMap<String, Unit>();
		partiallyConstructedUnits = new ArrayList<Unit>();
	}

	///
	@Override
	protected ArrayList<ArrayList<Unit>> readEntity(XmlPullParser parser) throws IOException, XmlPullParserException {
		Unit partiallyConstructedUnit;
		
		String tagName = parser.getName();
		if(tagName.equalsIgnoreCase("main")){
			parser.require(XmlPullParser.START_TAG, null, "main");
			while(parser.next() != XmlPullParser.END_TAG){
				if(parser.getEventType() != XmlPullParser.START_TAG){
					continue;
				}
				tagName = parser.getName();
				if(tagName.equalsIgnoreCase("unit")){
					partiallyConstructedUnit = readUnit(parser);	
					partiallyConstructedUnits.add(partiallyConstructedUnit);
					if(partiallyConstructedUnit.isBaseUnit()){
						baseUnitsMap.put(partiallyConstructedUnit.getName(), partiallyConstructedUnit);
					}else{
						nonBaseUnitsMap.put(partiallyConstructedUnit.getName(), partiallyConstructedUnit);
					}
				}
				else{
					skip(parser);
				}
			}
		}
		
		//
		completeUnitConstruction();
		
		//
		ArrayList<ArrayList<Unit>> unitsLists = new ArrayList<ArrayList<Unit>>(2);
		unitsLists.add(new ArrayList<Unit>(baseUnitsMap.values()));
		unitsLists.add(new ArrayList<Unit>(nonBaseUnitsMap.values()));
		
		return unitsLists;	
	}
	/**
	 *Completes the construction of the previous constructed unit by adding missing elements i.e. base unit.
	 *Finds actual unit references for the base unit names now that all units have been created.
	 */
	private void completeUnitConstruction(){
		for(Unit unit:partiallyConstructedUnits){
			String baseUnitName = unit.getBaseUnit().getName();
			unit.setBaseUnit(baseUnitsMap.containsKey(baseUnitName)?baseUnitsMap.get(baseUnitName):nonBaseUnitsMap.get(baseUnitName)
					         ,unit.getBaseConversionPolyCoeffs());	
		}
	}

	///
	private Unit readUnit(XmlPullParser parser) throws XmlPullParserException, IOException{
		Map<String, double[]> baseConversionPolyCoeffs = new HashMap<String, double[]>();
		Map<String, Double> componentUnitsExponentsMap = new HashMap<String,Double>(); 
		String unitName = "", unitSystem = "", abbreviation = "", unitCategory = "", unitDescription = "", tagName = "";
		
		parser.require(XmlPullParser.START_TAG, null, "unit");
		while(parser.next() != XmlPullParser.END_TAG){
			if(parser.getEventType() != XmlPullParser.START_TAG){
				continue;
			}
			tagName = parser.getName();
			if(tagName.equalsIgnoreCase("unitName")){
				unitName = readUnitName(parser);
			}else if(tagName.equalsIgnoreCase("unitSystem")){
				unitSystem = readUnitSystem(parser);
			}else if(tagName.equalsIgnoreCase("abbreviation")){
				abbreviation = readAbbreviation(parser);
			}else if(tagName.equalsIgnoreCase("unitCategory")){
				unitCategory = readUnitCategory(parser);
			}else if(tagName.equalsIgnoreCase("unitDescription")){
				unitDescription = readUnitDescription(parser);	
			}else if(tagName.equalsIgnoreCase("componentUnits")){
				componentUnitsExponentsMap = readComponentUnits(parser);
			}else if(tagName.equalsIgnoreCase("baseConversionPolyCoeffs")){
				baseConversionPolyCoeffs = readBaseUnitNConversionPolyCoeffs(parser);						
			}
			else{
				skip(parser);
			}
		}
		
		return new Unit(unitName, unitCategory, unitDescription, unitSystem, abbreviation, componentUnitsExponentsMap, new Unit(baseConversionPolyCoeffs.keySet().iterator().next(), new HashMap<String, Double>(), true ), baseConversionPolyCoeffs.values().iterator().next());
	}
	
	///
	private String readUnitName(XmlPullParser parser) throws XmlPullParserException, IOException{
		parser.require(XmlPullParser.START_TAG, null, "unitName");	
		String unitNameString = readText(parser).toLowerCase();
		parser.require(XmlPullParser.END_TAG, null, "unitName");
		return unitNameString;
	}
	private String readUnitSystem(XmlPullParser parser) throws XmlPullParserException, IOException{
		parser.require(XmlPullParser.START_TAG, null, "unitSystem");
		String unitSystemString = readText(parser).toLowerCase();
		parser.require(XmlPullParser.END_TAG, null, "unitSystem");
		return unitSystemString;
	}
	private String readAbbreviation(XmlPullParser parser) throws XmlPullParserException, IOException{
		parser.require(XmlPullParser.START_TAG, null, "abbreviation");
		String abbreviation = readText(parser);
		parser.require(XmlPullParser.END_TAG, null, "abbreviation");
		return abbreviation;
	}
	private String readUnitCategory(XmlPullParser parser) throws XmlPullParserException, IOException{
		parser.require(XmlPullParser.START_TAG, null, "unitCategory");
		String abbreviation = readText(parser).toLowerCase();
		parser.require(XmlPullParser.END_TAG, null, "unitCategory");
		return abbreviation;
	}
	private String readUnitDescription(XmlPullParser parser) throws XmlPullParserException, IOException{
		parser.require(XmlPullParser.START_TAG, null, "unitDescription");
		String abbreviation = readText(parser);
		parser.require(XmlPullParser.END_TAG, null, "unitDescription");
		return abbreviation;
	}
	private Map<String, Double> readComponentUnits(XmlPullParser parser) throws XmlPullParserException, IOException{
		Map<String, Double> componentUnitsExponentsMap = new HashMap<String, Double>();
		String componentUnitName = "";
		double componentExponentValue = 0.0;
		String tagName = "";
		
		parser.require(XmlPullParser.START_TAG, null, "componentUnits");
		while(parser.next() != XmlPullParser.END_TAG){
			if(parser.getEventType() != XmlPullParser.START_TAG){
				continue;
			}
			tagName = parser.getName();
			if(tagName.equalsIgnoreCase("component")){
				parser.require(XmlPullParser.START_TAG, null, "component");
				while(parser.next() != XmlPullParser.END_TAG){				
					if(parser.getEventType() != XmlPullParser.START_TAG){
						continue;
					}
					tagName = parser.getName();
					if(tagName.equalsIgnoreCase("unitName")){
						componentUnitName = readText(parser);
					}else if(tagName.equalsIgnoreCase("exponent")){
						componentExponentValue = readDouble(parser);
					}
					else{
						skip(parser);
					}
				}
				componentUnitsExponentsMap.put(componentUnitName, componentExponentValue);
			}
			else{
				skip(parser);
			}	
		}
		return componentUnitsExponentsMap;
	}
	private Map<String, double[]> readBaseUnitNConversionPolyCoeffs(XmlPullParser parser) throws XmlPullParserException, IOException{
		Map<String, double[]> conversionPolyCoeffsMap = new HashMap<String, double[]>();
		String baseUnitName = "";
		double[] polynomialCoeffs = new double[]{0.0, 0.0};
		String tagName = "";
		
		parser.require(XmlPullParser.START_TAG, null, "baseConversionPolyCoeffs");
		while(parser.next() != XmlPullParser.END_TAG){
			if(parser.getEventType() != XmlPullParser.START_TAG){
				continue;
			}
			tagName = parser.getName();
			if(tagName.equalsIgnoreCase("baseUnit")){
				baseUnitName = readText(parser);
			}else if(tagName.equals("polynomialCoeffs")){ //If no conversion polynomial coeffs were provided, then the default is {0.0f, 0.0f}
				polynomialCoeffs = readDoubleArray(parser);
			}
			else{
				skip(parser);
			}
		}
		
		conversionPolyCoeffsMap.put(baseUnitName, polynomialCoeffs);
		
		return conversionPolyCoeffsMap;
	}
	
	///
	@Override
	public UnitManagerBuilder loadInBackground() {

		ArrayList<ArrayList<Unit>> coreUnitsGroup = new ArrayList<ArrayList<Unit>>();
		ArrayList<ArrayList<Unit>> dynamicUnitsGroup = new ArrayList<ArrayList<Unit>>();
		try {
			coreUnitsGroup = parseXML(openAssetFile("StandardCoreUnits.xml"));
			dynamicUnitsGroup = parseXML(openXmlFile(getContext().getFilesDir().getPath().toString() + "DynamicUnits.xml", false));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for(int i=0; i<2; i++){
			for(Unit unit:coreUnitsGroup.get(i)){
				unit.setCoreUnitState(true);
			}
		}
		
		ArrayList<Unit> combinedUnits = coreUnitsGroup.get(0);
		combinedUnits.addAll(coreUnitsGroup.get(1));
		
		if(dynamicUnitsGroup != null){
			combinedUnits.addAll(dynamicUnitsGroup.get(0));
			combinedUnits.addAll(dynamicUnitsGroup.get(1));
		}
			
		return new UnitManagerBuilder().addBaseUnits(combinedUnits)
									   .addNonBaseUnits(combinedUnits);
	}
	

}
