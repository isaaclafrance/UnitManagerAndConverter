package com.isaacapps.unitconverterapp.dao.xml.readers.online;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.isaacapps.unitconverterapp.dao.xml.readers.AsyncXmlReader;
import com.isaacapps.unitconverterapp.models.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManagerBuilder;
import com.isaacapps.unitconverterapp.models.unitmanager.Utility;

import android.content.Context;

public class PrefixesNUnitsMapXmlOnlineReader extends AsyncXmlReader<UnitManagerBuilder,UnitManagerBuilder>{

	private ArrayList<Unit> partiallyConstructedUnits;
	private Map<String, String> abbreviationCodeToFullNameMap;
	private Map<String, Unit> abbreviatedNameBaseUnitsMap;
	private Map<String, Unit> abbreviatedNameNonBaseUnitsMap;
	private Map<String, Double> prefixesMap;
	
	public PrefixesNUnitsMapXmlOnlineReader(Context context){
		super(context);
		partiallyConstructedUnits = new ArrayList<Unit>();
		abbreviationCodeToFullNameMap = new HashMap<String, String>();
		abbreviatedNameBaseUnitsMap = new HashMap<String, Unit>();
		abbreviatedNameNonBaseUnitsMap = new HashMap<String, Unit>();
		prefixesMap = new HashMap<String, Double>();
	}
		
	///
	@Override
	protected UnitManagerBuilder readEntity(XmlPullParser parser) throws XmlPullParserException, IOException{
		UnitManagerBuilder unitManagerBuilder = new UnitManagerBuilder();
		Unit partiallyConstructedUnit;
		
		//
		String tagName = parser.getName();
		if(tagName.equalsIgnoreCase("root")){
			parser.require(XmlPullParser.START_TAG, null, "root");
			String rootDescription = "Source: "+readAttribute(parser, "xmlns")+". \nUpdate Date: "+readAttribute(parser, "revision-date");
			while(parser.next() != XmlPullParser.END_TAG){
				if(parser.getEventType() != XmlPullParser.START_TAG){
					continue;
				}
				tagName = parser.getName();
				if(tagName.equalsIgnoreCase("prefix")){
					partiallyConstructedUnit = null;
					prefixesMap.putAll(readPrefix(parser));
				}
				else if(tagName.equalsIgnoreCase("base-unit")){
					partiallyConstructedUnit = readUnit(parser, rootDescription, true);
				}
				else if(tagName.equalsIgnoreCase("unit")){
					partiallyConstructedUnit = readUnit(parser, rootDescription, false);
				}
				else{
					partiallyConstructedUnit = null;
					skip(parser);
				}
				
				if(partiallyConstructedUnit != null){
					//The raw information parsed from the xml connects the units using their code abbreviation name for conversion purposes; therefore, the abbreviation has greater 
					//preference over the full name name storing and retrieving units, at least initially.
					if(partiallyConstructedUnit.isBaseUnit()){
						abbreviatedNameBaseUnitsMap.put(partiallyConstructedUnit.getAbbreviation(), partiallyConstructedUnit);
					}
					else{
						abbreviatedNameNonBaseUnitsMap.put(partiallyConstructedUnit.getAbbreviation(), partiallyConstructedUnit);
					}
					//Also adds constructed unit to a separate array for unit construction finalization later on.
					partiallyConstructedUnits.add(partiallyConstructedUnit);
				}
			}
		}
		
		//					
		completeUnitConstruction();
		
		//
		unitManagerBuilder.setBaseUnitsComponent(new ArrayList<Unit>(abbreviatedNameBaseUnitsMap.values()));
		unitManagerBuilder.setNonBaseUnitsComponent(new ArrayList<Unit>(abbreviatedNameNonBaseUnitsMap.values()));
		unitManagerBuilder.setCorePrefixesNAbbreviationsMapComponent(prefixesMap);
		
		return unitManagerBuilder; 
	}
	
	private void completeUnitConstruction(){
		//**Completes the construction of the previous constructed unit by adding missing elements i.e. coreUnitState, conversion polynomial coefficients, and base unit.
		//**Accomplishes this task by finding actual unit references for the base unit names now that all units have been created.
		for(Unit partiallyConstructedUnit:partiallyConstructedUnits){
			assignBaseUnitNConversionPolyCoeffs(partiallyConstructedUnit);
			assignFullNameComponentUnitDimension(partiallyConstructedUnit);	
			partiallyConstructedUnit.setCoreUnitState(true);
		}
	}
	private void assignBaseUnitNConversionPolyCoeffs(Unit partiallyConstructedUnit){
		//Since the actually base unit may not have already been parsed during this unit's partial construction, only a dummy unit with the name of the base unit and the conversion was associated.
		//Therefore the actual base unit candidate needs to be retrieved.
			
		partiallyConstructedUnit.setBaseUnit(getUnitFromAbbreviatedName(partiallyConstructedUnit.getBaseUnit().getAbbreviation())
				                             , partiallyConstructedUnit.getBaseConversionPolyCoeffs());	
		
		if(partiallyConstructedUnit.getBaseUnit().getCategory().equalsIgnoreCase(Unit.UNKNOWN_UNIT_CATEGORY)
		   && !partiallyConstructedUnit.getCategory().equalsIgnoreCase(Unit.UNKNOWN_UNIT_CATEGORY)){
			//Sometimes depending on how the units are constructed the base unit may need to get the category, unit system, description from its descendent.
			partiallyConstructedUnit.getBaseUnit().setCategory(partiallyConstructedUnit.getCategory());
			partiallyConstructedUnit.getBaseUnit().setUnitSystem(partiallyConstructedUnit.getUnitSystem());
			partiallyConstructedUnit.getBaseUnit().setDescription(partiallyConstructedUnit.getDescription());
		}
	}
	private void assignFullNameComponentUnitDimension(Unit partiallyConstructedUnit){
		//Copies the raw abbreviation encoded component unit dimension map that was initially assigned directly from parsing, before deleting it.
		//Then transforms that copy into an equivalent component dimension map with full names of the component units.	
		Map<String, Double> abbreviatedComponentUnitsDimension = new HashMap<String,Double>(partiallyConstructedUnit.getComponentUnitsDimension());
		partiallyConstructedUnit.clearComponentUnitsDimension();

		for(Entry<String, Double> componentEntry:abbreviatedComponentUnitsDimension.entrySet()){
			partiallyConstructedUnit.addComponentUnit(abbreviationCodeToFullNameMap.get(componentEntry.getKey()), componentEntry.getValue());
		}
	}	
	
	private Unit getUnitFromAbbreviatedName(String abbreviatedName){
		Unit unit = abbreviatedNameBaseUnitsMap.get(abbreviatedName); 
		if(unit != null){
			return unit;
		}
		
		unit = abbreviatedNameNonBaseUnitsMap.get(abbreviatedName);
		if(unit != null){
			return unit;
		}
		else{//Very likely unit name is composed a complex combination of prefix abbreviations and unit abbreviations. After decomposition, use this name to create the missing unit.		
			Map<String, Double> abbreviatedComponentUnitsDimension = convertAbbreviatedStringToComponentUnitsDimension(abbreviatedName);
			Map<String, Double> fullNameComponentUnitsDimension = new HashMap<String, Double>();
			
			String fullName;
			for(Entry<String, Double> abbreviatedCompUnitEntry:abbreviatedComponentUnitsDimension.entrySet()){
				fullName = abbreviationCodeToFullNameMap.get(abbreviatedCompUnitEntry.getKey());
				if(fullName == null){ //Very likely no match was found because the name contains a prefix.  
					unit = createMissingPrefixedUnit(abbreviatedCompUnitEntry.getKey());
					abbreviatedNameNonBaseUnitsMap.put(abbreviatedCompUnitEntry.getKey(), unit);					
					fullName = unit.getName();		
				}
				fullNameComponentUnitsDimension.put(fullName, abbreviatedCompUnitEntry.getValue());
			}
			
			//
			unit = new Unit(fullNameComponentUnitsDimension, true);
			unit.setAbbreviation(abbreviatedName);
			abbreviatedNameBaseUnitsMap.put(unit.getAbbreviation(), unit);
			
			return unit;
		}
	}
	private Unit createMissingPrefixedUnit(String prefixedAbbreviatedName){
		//Identify the prefix  and construct the prefixed unit with the appropriate base conversion based on the prefix factor.
		
		Map<String, Double> createPrefixedUnitComponentUnitsDimension = new HashMap<String, Double>();
		Unit createdPrefixedUnit = null;
		String abbreviatedPrefix, fullPrefix;
		String prefixlessFullName, prefixlessAbbreviatedName;
		//Matcher exponentMatcher = Pattern.compile("[-]?\\d+").matcher(prefixedOrExponentiatedAbbreviatedName);
		//Double exponent = exponentMatcher.find()?Double.parseDouble(exponentMatcher.group()):1.0;
		
		for(Entry<String, Double> prefixEntry:prefixesMap.entrySet()){
			abbreviatedPrefix = prefixEntry.getKey().split("::")[1];
			if(prefixedAbbreviatedName.indexOf(abbreviatedPrefix) != 0){ //make sure only matchable prefixes characters in front of the string will be replaced
				continue;
			}
			prefixlessAbbreviatedName = prefixedAbbreviatedName.replaceFirst(abbreviatedPrefix, "");
			prefixlessFullName = abbreviationCodeToFullNameMap.get(prefixlessAbbreviatedName);			
			
			if(prefixlessFullName != null){	
				fullPrefix = prefixEntry.getKey().split("::")[0];
				createPrefixedUnitComponentUnitsDimension.put(fullPrefix+"-"+prefixlessFullName, 1.0);
				
				createdPrefixedUnit = new Unit(createPrefixedUnitComponentUnitsDimension, false);
				createdPrefixedUnit.setBaseUnit(getUnitFromAbbreviatedName(prefixlessAbbreviatedName), new double[]{prefixEntry.getValue(), 0.0});
				createdPrefixedUnit.setAbbreviation(prefixedAbbreviatedName);
				
				abbreviationCodeToFullNameMap.put(prefixedAbbreviatedName, createdPrefixedUnit.getName());
				break;
			}
		}	
		
		//The forced assumption is that the xml was parsed properly, therefore the prefixedAbbreviatedName will contain a prefix from prefixesMap if it has any
		//and the prefixlessFullName will be retrievable from abbreviationCodeToFullNameMap. Hence, in all cases the returned unit will not be null.
		return createdPrefixedUnit;
	}
	
	///
	private Map<String, Double> readPrefix(XmlPullParser parser) throws XmlPullParserException, IOException{
		String tagName;
		String fullPrefixName = "";
		String abbreviatedPrefixName = "";
		double prefixValue = 0.0;
		Map<String, Double> prefixMap = new HashMap<String, Double>();
		
		parser.require(XmlPullParser.START_TAG, null, "prefix");
		
		abbreviatedPrefixName = readAttribute(parser, "CODE");
		while(parser.next() != XmlPullParser.END_TAG){
			if(parser.getEventType() != XmlPullParser.START_TAG){
				continue;
			}
			tagName = parser.getName();
			if(tagName.equalsIgnoreCase("name")){
				fullPrefixName= readText(parser);
			}else if(tagName.equalsIgnoreCase("value")){
				prefixValue = Double.parseDouble(readAttribute(parser, "value"));
				skip(parser); //only attributes were being read which does not automatically advance the parser, therefore the advancement has to be forced.
			}else{
				skip(parser);
			}
		}
		prefixMap.put(fullPrefixName +"::"+ abbreviatedPrefixName, prefixValue);
		
		return prefixMap;
	}
	private Unit readUnit(XmlPullParser parser, String rootDescription, boolean isBaseUnit) throws XmlPullParserException, IOException{
		Map<String, double[]> baseUnitNConversionPolyCoeffs = new HashMap<String, double[]>();
		Map<String, Double> abbreviatedComponentUnitsDimension = new HashMap<String,Double>(); 
		String unitName = "", unitSystem = "", unitAbbreviation = "", unitCategory = "", tagName = "";
		
		parser.require(XmlPullParser.START_TAG, null, (isBaseUnit)?"base-unit":"unit");
		
		unitAbbreviation = readUnitAbbreviation(parser, isBaseUnit);
		unitSystem = (isBaseUnit)?"si":readUnitSystem(parser);
		while(parser.next() != XmlPullParser.END_TAG){
			if(parser.getEventType() != XmlPullParser.START_TAG){
				continue;
			}
			tagName = parser.getName();
			if(tagName.equalsIgnoreCase("name")){								
				unitName = readUnitName(parser);
			}else if(tagName.equalsIgnoreCase("property")){
				unitCategory = readUnitCategory(parser);
			}
			else if(tagName.equalsIgnoreCase("value") && !isBaseUnit){	
				try{
					baseUnitNConversionPolyCoeffs = readBaseUnitNConversionPolyCoeffs(parser); //Issues may arise when the the conversion is not a simple conversion factor but is instead a complex function with some specific argument. ex. sqrt, log
					abbreviatedComponentUnitsDimension = readAbbreviatedComponentUnitsDimension(parser); //Issues can arise when trying to parse constants represented as units in the xml since there are component units with only numbers as unit names instead of alphabets. 
				}
				catch(Exception e){ 
					skip(parser);
					continue;
				}
				skip(parser); //only attributes were being read which does not automatically advance parser, there it has to be forced.
			}
			else{
				skip(parser);
			}
		}
		
		abbreviationCodeToFullNameMap.put(unitAbbreviation, unitName);
		
		//Only return a created unit:
		//1. if it is not of the dimensionaless unit category. 
		//This is because these units can have some weird properties. For example, some may have a base unit as number rather than a name, which does not fit how units are being understood and modeled. 
		//2. base unit name is not only numerical
		//3. Issue had not arose when constructing the baseUnitNConversionPolyCoeffs and abbreviatedComponentUnitsDimension
		if(unitSystem.equalsIgnoreCase("dimless") || (abbreviatedComponentUnitsDimension.size() == 0 
		   || baseUnitNConversionPolyCoeffs.size() == 0 || !baseUnitNConversionPolyCoeffs.keySet().iterator().next().matches(".*[a-zA-Z]+.*")) && !isBaseUnit ){
			return null;
		}
		else{
			Unit createdUnit = new Unit(unitName, unitCategory, rootDescription, unitSystem, unitAbbreviation, abbreviatedComponentUnitsDimension
	                , isBaseUnit?new Unit():new Unit(baseUnitNConversionPolyCoeffs.keySet().iterator().next(), new HashMap(), true)
	                , isBaseUnit? new double[]{0.0, 0.0}:baseUnitNConversionPolyCoeffs.values().iterator().next());
			
			if(isBaseUnit){
				createdUnit.setBaseUnit(createdUnit);
				createdUnit.addComponentUnit(createdUnit.getAbbreviation(), 1.0);
			}
			
			return createdUnit;
		}
	}
		
	/// Methods for retrieving entity components from XML	
	private String readUnitName(XmlPullParser parser) throws XmlPullParserException, IOException{
		parser.require(XmlPullParser.START_TAG, null, "name");	
		String unitNameString = readText(parser).trim().replaceAll(" ", "_").replace("-", "_");
		parser.require(XmlPullParser.END_TAG, null, "name");
		return unitNameString;
	}
	private String readUnitSystem(XmlPullParser parser) throws XmlPullParserException, IOException{
		parser.require(XmlPullParser.START_TAG, null, "unit");
		String unitSystemString = readAttribute(parser, "class").split("-")[0]
				                  +(readAttribute(parser, "isMetric").equalsIgnoreCase("yes")?"(metric)":"(non-metric)")
				                  +(readAttribute(parser, "isAbitrary").equalsIgnoreCase("yes")?"(abitrary)":"")
				                  +(readAttribute(parser, "isSpecial").equalsIgnoreCase("yes")?"(special)":"");
		return unitSystemString;
	}
	private String readUnitAbbreviation(XmlPullParser parser, boolean isBaseUnit) throws XmlPullParserException, IOException{
		parser.require(XmlPullParser.START_TAG, null, isBaseUnit?"base-unit":"unit");
		String abbreviation = readAttribute(parser, "CODE");
		return abbreviation;
	}
	private String readUnitCategory(XmlPullParser parser) throws XmlPullParserException, IOException{
		parser.require(XmlPullParser.START_TAG, null, "property");
		String unitCategory = readText(parser);
		parser.require(XmlPullParser.END_TAG, null, "property");
		return unitCategory;
	}
	private Map<String, Double> readAbbreviatedComponentUnitsDimension(XmlPullParser parser) throws XmlPullParserException, IOException{
		parser.require(XmlPullParser.START_TAG, null, "value");
		return convertAbbreviatedStringToComponentUnitsDimension(readAttribute(parser, "UNIT"));	
	}
	private Map<String, double[]> readBaseUnitNConversionPolyCoeffs(XmlPullParser parser) throws XmlPullParserException, Exception{
		Map<String, double[]> conversionPolyCoeffsMap = new HashMap<String, double[]>();
		parser.require(XmlPullParser.START_TAG, null, "value");
		conversionPolyCoeffsMap.put(readAttribute(parser, "UNIT")
				                    , new double[]{Double.parseDouble(readAttribute(parser, "value")), 0.0});
		return conversionPolyCoeffsMap;
	}
	
	private Map<String, Double> convertAbbreviatedStringToComponentUnitsDimension(String abbreviatedComponentUnitsDimensionString){
		//type match requirement: 1. letters and underscore 2. number can only be included if bounded by bracket. 3. words can optionally be to the left of the left bracket.
		//exponent match requirement: numeric can only be found to the right of right bracket there are any in matching group.
		return Utility.getDimensionFromString(abbreviatedComponentUnitsDimensionString, Pattern.compile("\\w*\\[\\w+\\]|[a-zA-Z_]+"), Pattern.compile("[-]?\\d+(?!.*\\])"), Pattern.compile("[\\/.]?[\\[\\]a-zA-Z_0-9-]+")
				                             , new Utility.ComponentUnitsDimensionUpdater(), new HashMap<String, Double>());
		
	}
		
	/// Loader Methods
	@Override
	public UnitManagerBuilder loadInBackground() {
		UnitManagerBuilder unitManagerBuilder = new UnitManagerBuilder();
		try {
			unitManagerBuilder = parseXML(openXmlFile("http://unitsofmeasure.org/ucum-essence.xml", true));
		} catch (Exception e){
			e.printStackTrace();
		}
		return unitManagerBuilder;
	}
}
