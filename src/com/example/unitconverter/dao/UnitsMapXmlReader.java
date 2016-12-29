package com.example.unitconverter.dao;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.example.unitconverter.Unit;
import com.example.unitconverter.UnitManagerFactory;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Xml;

//TODO: Implement a separate loader for loading core units
public class UnitsMapXmlReader extends AsyncTaskLoader<UnitManagerFactory>{
	//Fields
	private XmlPullParser parser;
	private boolean isSourceOnline; //Determines if XML loaded from an online server or from local source.
	private String coreUnitsXmlSource;
	private String dynamicUnitsXmlSource = "DynamicUnits.xml";
	
	//Constructor
	public UnitsMapXmlReader(Context context){
		super(context);
		this.isSourceOnline = false;
		coreUnitsXmlSource = "StandardCoreUnits.xml";
		parser = Xml.newPullParser();
	}
	public UnitsMapXmlReader(Context context, boolean isSourceOnline, String coreUnitsXmlSource){
		this(context);
		this.isSourceOnline = isSourceOnline;
		this.coreUnitsXmlSource = coreUnitsXmlSource;
	}
	
	////TODO: Process Units XML's elements. Include a way to cache for later processing units that refer to component units that have not been loaded yet.
	public ArrayList<ArrayList<Unit>> loadUnitsFromXML(InputStream in) throws XmlPullParserException, IOException{
		try{
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
	
		ArrayList<Unit> partiallyConstructedUnitsArray = new ArrayList<Unit>();
		ArrayList<Map<String, Double>> componentUnitsExponentsMap_Array = new ArrayList<Map<String,Double>>(); 
		ArrayList<String> baseUnitNameArray = new ArrayList<String>();
		
		Map<String, Unit> baseUnitsMap = new HashMap<String, Unit>();
		Map<String, Unit> nonBaseUnitsMap = new HashMap<String, Unit>();
		
		String unitNameString = "", unitSystemString = "", abbreviationString = "", unitCategoryString = "", tagName = "";

		tagName = parser.getName();
		if(tagName.equalsIgnoreCase("main")){
			//Iterate through all elements of the XML file within the 'unit' element while storing properties pertaining to the unit.
			parser.require(XmlPullParser.START_TAG, null, "main");
			while(parser.next() != XmlPullParser.END_TAG){
				if(parser.getEventType() != XmlPullParser.START_TAG){
					continue;
				}
				tagName = parser.getName();
				if(tagName.equalsIgnoreCase("unit")){
					parser.require(XmlPullParser.START_TAG, null, "unit");
					while(parser.next() != XmlPullParser.END_TAG){
						if(parser.getEventType() != XmlPullParser.START_TAG){
							continue;
						}
						tagName = parser.getName();
						if(tagName.equalsIgnoreCase("unitName")){
							unitNameString = readUnitNameString(parser);
						}else if(tagName.equalsIgnoreCase("unitSystem")){
							unitSystemString = readUnitSystemString(parser);
						}else if(tagName.equalsIgnoreCase("abbreviation")){
							abbreviationString = readAbbreviationString(parser);
						}else if(tagName.equalsIgnoreCase("unitCategory")){
							unitCategoryString = readunitCategoryString(parser);
						}else if(tagName.equalsIgnoreCase("componentUnits")){
							componentUnitsExponentsMap_Array.add(readComponentUnits(parser));
						}else if(tagName.equalsIgnoreCase("baseConversionPolyCoeffs")){
							//Read base unit name and associated conversion polynomials
							baseConversionPolyCoeffs = readBaseUnitNConversionPolyCoeffs(parser);
							for(Entry<String, double[]> entry:baseConversionPolyCoeffs.entrySet()){
								baseUnitNameArray.add(entry.getKey());
							}							
						}
					}
				}
				else{
					skip(parser);
				}
						
				//Use stored information from XML file to partially construct a new unit. The created units are missing base unit info, component unit info, and conversion polynomial info. Then places the unit in the unit map.
				Unit constructedUnit = new Unit(unitNameString, unitCategoryString, unitCategoryString, unitSystemString, abbreviationString, new HashMap<String, Double>(), new Unit(baseUnitNameArray.get(baseUnitNameArray.size()-1), new HashMap<String, Double>(), true ), baseConversionPolyCoeffs.get(baseUnitNameArray.get(baseUnitNameArray.size()-1)));
				
				//Adds newly constructed unit to base units map if base unit otherwise adds unit to dynamic units map.
				if(constructedUnit.isBaseUnit()){
					baseUnitsMap.put(unitNameString, constructedUnit);
				}else{
					nonBaseUnitsMap.put(unitNameString, constructedUnit);
				}
				
				//Also adds constructed unit to a separate array for unit construction finalization later on.
				partiallyConstructedUnitsArray.add(constructedUnit);
			}
		}
		
		//**Completes the construction of the previous constructed unit by adding missing elements i.e. component units info, conversion polynomial coefficients, and base unit.
		//**Accomplishes this task by finding actual unit references for the unit names associated with the componentUnitsExponentMap and the conversionPolynomialCoeffsMap.
		
		//Adds component units info to each partially constructed unit. 		
		int index = 0;	
		for(Map<String, Double> compUnitsMap:componentUnitsExponentsMap_Array){ 
			for(Entry<String, Double> compMapEntry:compUnitsMap.entrySet()){
				partiallyConstructedUnitsArray.get(index).addComponentUnit(compMapEntry.getKey(), compMapEntry.getValue());
			}
			index++;
		}
				
		//Adds base unit and associated conversion polynomial info to each partially constructed unit
		index = 0;
		for(String buName:baseUnitNameArray){
			partiallyConstructedUnitsArray.get(index).setBaseUnit(baseUnitsMap.get(buName), partiallyConstructedUnitsArray.get(index).getBaseConversionPolyCoeffs());
			
			index++;
		}
		
		//
		ArrayList<ArrayList<Unit>> unitsArray = new ArrayList<ArrayList<Unit>>(2);
		unitsArray.add(new ArrayList<Unit>(baseUnitsMap.values()));
		unitsArray.add(new ArrayList<Unit>(nonBaseUnitsMap.values()));
		
		return unitsArray;
	}
	
	////  Methods for retrieving unit specific elements from XML	
	private String readUnitNameString(XmlPullParser parser) throws XmlPullParserException, IOException{
		parser.require(XmlPullParser.START_TAG, null, "unitName");	
		String unitNameString = readText(parser).trim();
		parser.require(XmlPullParser.END_TAG, null, "unitName");
		return unitNameString;
	}
	private String readUnitSystemString(XmlPullParser parser) throws XmlPullParserException, IOException{
		parser.require(XmlPullParser.START_TAG, null, "unitSystem");
		String unitSystemString = readText(parser);
		parser.require(XmlPullParser.END_TAG, null, "unitSystem");
		return unitSystemString;
	}
	private String readAbbreviationString(XmlPullParser parser) throws XmlPullParserException, IOException{
		parser.require(XmlPullParser.START_TAG, null, "abbreviation");
		String abbreviation = readText(parser);
		parser.require(XmlPullParser.END_TAG, null, "abbreviation");
		return abbreviation;
	}
	private String readunitCategoryString(XmlPullParser parser) throws XmlPullParserException, IOException{
		parser.require(XmlPullParser.START_TAG, null, "unitCategory");
		String abbreviation = readText(parser);
		parser.require(XmlPullParser.END_TAG, null, "unitCategory");
		return abbreviation;
	}	
	private Map<String, Double> readComponentUnits(XmlPullParser parser) throws XmlPullParserException, IOException{
		Map<String, Double> componentUnitsExponentsMap = new HashMap<String, Double>();
		String componentUnitName = "";
		double componentExponentValue = 0.0f;
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
						componentExponentValue = readdouble(parser);
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
		double[] polynomialCoeffs = new double[]{0.0f, 0.0f};
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
				polynomialCoeffs = readdoubles(parser);
			}
			else{
				skip(parser);
			}
		}
		
		conversionPolyCoeffsMap.put(baseUnitName, polynomialCoeffs);
		
		return conversionPolyCoeffsMap;
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
	private String readText(XmlPullParser parser)throws IOException, XmlPullParserException{
		String text = "";
		if(parser.next() == XmlPullParser.TEXT){
			text = parser.getText().toLowerCase();
			parser.nextTag();
		}
		return text;
	}
	private double[] readdoubles(XmlPullParser parser) throws IOException, XmlPullParserException{
		double[] doubles = new double[2];
		String[] doubleTexts = readText(parser).split(" ");
		
		if(doubleTexts.length >= 2){
			doubles[0] = Double.valueOf(doubleTexts[0]);
			doubles[1] = Double.valueOf(doubleTexts[1]);
		}
		return doubles;
	}
	private double readdouble(XmlPullParser parser) throws IOException, XmlPullParserException{
		return Double.valueOf(readText(parser));
	}
	
	//// Loader Methods
	@Override
	public UnitManagerFactory loadInBackground() {
		ArrayList<ArrayList<Unit>> coreUnitsGroup = new ArrayList<ArrayList<Unit>>(); //Has both base and non-base core units
		ArrayList<ArrayList<Unit>> dynamicUnitsGroup = new ArrayList<ArrayList<Unit>>(); // Has both base and non-base dynamic units

		coreUnitsGroup.add(new ArrayList<Unit>()); coreUnitsGroup.add(new ArrayList<Unit>());
		dynamicUnitsGroup.add(new ArrayList<Unit>()); dynamicUnitsGroup.add(new ArrayList<Unit>());
		
		try{
			if(isSourceOnline){
				URL onlineFileCoreUnits = new URL(coreUnitsXmlSource);	
				coreUnitsGroup = loadUnitsFromXML(onlineFileCoreUnits.openStream());
			}else{					
				coreUnitsGroup = loadUnitsFromXML(getContext().getAssets().open(coreUnitsXmlSource));				
			} 
			
			for(int i=0; i<coreUnitsGroup.size(); i++){
				for(Unit unit:coreUnitsGroup.get(i)){
					unit.setCoreUnitState(true);
				}
			}
			
			File fileDUnitsFile = new File(dynamicUnitsXmlSource);		
			if(!fileDUnitsFile.exists()){
				fileDUnitsFile.createNewFile();
			}
			dynamicUnitsGroup = loadUnitsFromXML(getContext().openFileInput(dynamicUnitsXmlSource));	
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ArrayList<Unit> combinedUnits;
		UnitManagerFactory unitManagerFactoryBundle = new UnitManagerFactory();
		
		combinedUnits = coreUnitsGroup.get(0);
		combinedUnits.addAll(dynamicUnitsGroup.get(0));
		unitManagerFactoryBundle.setBaseUnitsComponent(combinedUnits);
		
		
		combinedUnits = coreUnitsGroup.get(1);
		combinedUnits.addAll(dynamicUnitsGroup.get(1));
		unitManagerFactoryBundle.setNonBaseUnitsComponent(combinedUnits);
		
		return unitManagerFactoryBundle;
	}
}
