package com.heatIntegration.internals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.heatIntegration.internals.UNIT_SYSTEMS.UNKNOWN_SYSTEM.UNKNOWN_UNITS;

//TODO: Implement a class that manages and converts between different units. Specifically it will act as a unit conversion library that contains different classes for "base unit class", "derived unit class", and a unit management class. Modeled after the unit package in Mathematica
//TODO: Finish inputting core units into StandardCoreUnits.xml file
//TODO: Investigate the design implications of implementing this class a singleton
//TODO: Investigate the design implications of implementing this class with all methods being static

public final class UnitManager{
	//Singleton Instance
	private static UnitManager unitManagerSingleton;
	
	//Fields
	private static Map<String, UnitClass> coreBaseUnitDictionary = new HashMap<String, UnitClass>();
	private static Map<String, UnitClass> coreUnitDictionary = new HashMap<String, UnitClass>(); //Store permanent frequently used units
	private static Map<String, Float> corePrefixDictionary = new HashMap<String, Float>();
	
	private static Map<String, UnitClass> dynamicBaseUnitDictionary = new HashMap<String, UnitClass>();
	private static Map<String, UnitClass> dynamicUnitDictionary = new HashMap<String, UnitClass>();
	private static Map<String, Float> dynamicPrefixDictionary = new HashMap<String, Float>();
	
	private static Map<String, Map<String, UNIT_TYPE>> fundamentalUnitsMap = new HashMap<String, Map<String,UNIT_TYPE>>(); //Associates a fundamental unit type with particular unitClass stored in the core or dynamic units dictionaries. The String is the unit system name.
	//
	//TODO: Implement a way to determine dimension based on fundamental quantity types, i.e. mass, temperature, length etc 	
	public static enum UNIT_TYPE{MASS, LENGTH, TIME, AMT_OF_SUBSTANCE, TEMPERATURE, CURRENT, LUMINOUS_INTENSITY, DERIVED, UNKNOWN};
	
	//Private Constructors and Singleton Reference
	private UnitManager(){
		setupPrefixesAndUnits();
		setupFundamentalUnitsMap();
	}
	static public UnitManager getInstance(){
		if(unitManagerSingleton == null){
			unitManagerSingleton = new UnitManager();
		}
		return unitManagerSingleton;
	}
	
	//Map Initialization and Loading Methods
	private void setupPrefixesAndUnits(){		
		loadCoreUnitsFromXML();
		loadDynamicUnitsFromXML();
		loadCorePrefixesFromXML();
		loadDynamicPrefixesFromXML();
	}
	private void setupFundamentalUnitsMap(){
		loadFundamentalUnitsFromXML();
	}
	
	//TODO: Implement the ability to load unit information from an XML file
	private void loadCoreUnitsFromXML(){

	}	
	private void loadDynamicUnitsFromXML(){
		
	}
	private void loadCorePrefixesFromXML(){
		
	}
	private void loadDynamicPrefixesFromXML(){
		
	}
	private void loadFundamentalUnitsFromXML(){
		//TODO:Finish implementation for loading fundamental unit associations from XML
		String fundamentalUnitNameString
		
		if(fundamentalUnitNameString.equalsIgnoreCase("length")){
			fundamentalUnitsMap.put(getUnit(unitNameLoadedFromXML), UNIT_TYPE.LENGTH);
		}
	}
	
	//TODO: Implement Map Saving Methods
	public static void saveDynamicPrefixesToXML(){
		Enum;
	}
	public static void saveDynamicUnitsToXML(){
		
	}	
	
	//Map Modification Methods
	public void addDynamicUnit(String name, UnitClass unitClass){
		//unitClass.setUnitManagerRef(this); //Not necessary if the unit manager is a singleton.
		if(unitClass.isBaseUnit()){ 
			//Removes any other instances of base units with same dimension			
			for(UnitClass unitMatches:getFundamentalUnitDimensionMatches(unitClass)){
				if(unitMatches.isBaseUnit()){
					dynamicBaseUnitDictionary.remove(unitMatches.getUnitNameString());
					updateBaseUnitAssociations(false); //Makes sure that current units have proper base unit associations					
				}
			}
			dynamicBaseUnitDictionary.put(name, unitClass);		
			updateBaseUnitAssociations(true);
		}
		else{
			dynamicUnitDictionary.put(name, unitClass);
		}
	}
	public void removeDynamicUnit(String name){
		if(dynamicBaseUnitDictionary.containsKey(name)){
			dynamicBaseUnitDictionary.remove(name);
			updateBaseUnitAssociations(false); //Makes sure that current units have proper base unit associations
		}
		else if(dynamicUnitDictionary.containsKey(name)){
			dynamicUnitDictionary.remove(name);
		}
	}	
	public void removeAllDynamicUnits(){
		dynamicBaseUnitDictionary.clear();
		dynamicUnitDictionary.clear();
	}
	private void updateBaseUnitAssociations(boolean onlyUnitsWithUnknownBaseUnits){
		if(onlyUnitsWithUnknownBaseUnits){
			for(UnitClass unit:dynamicUnitDictionary.values()){
				if(unit.getBaseUnit() == null){
					unit.setAutomaticBaseUnit(true);					
				}
			}
		}
		else{
			for(UnitClass unit:dynamicUnitDictionary.values()){
				unit.setAutomaticBaseUnit(true);
			}				
		}
	}
	
	public void addDynamicPrefix(String prefixName, Float prefixValue){
		dynamicPrefixDictionary.put(prefixName, prefixValue);
	}
	public void removeDynamicPrefix(String prefixName){
		dynamicPrefixDictionary.remove(prefixName);
	}	
	public void removeAllDynamicPrefixes(){
		dynamicPrefixDictionary.clear();
	}	

	///
	public boolean containsUnit(String unknownUnitName){
		if(coreBaseUnitDictionary.containsKey(unknownUnitName.toLowerCase())){
			return true;
		}
		else if(coreUnitDictionary.containsKey(unknownUnitName.toLowerCase())){
			return true;
		}
		else if(dynamicBaseUnitDictionary.containsKey(unknownUnitName.toLowerCase())){
			return true;
		}
		else if(dynamicUnitDictionary.containsKey(unknownUnitName.toLowerCase())){
			return true;
		}
		else{
			return false;
		}
	}
	public boolean containsUnit(UnitClass unknownUnit){
		if(coreBaseUnitDictionary.containsValue(unknownUnit)){
			return true;
		}
		else if(coreUnitDictionary.containsValue(unknownUnit)){
			return true;
		}
		else if(dynamicBaseUnitDictionary.containsValue(unknownUnit)){
			return true;
		}
		else if(dynamicUnitDictionary.containsValue(unknownUnit)){
			return true;
		}
		else{
			return false;
		}
	}
	public boolean containsUnitByFundamentalDimension(UnitClass unknownUnit){
		if(getFundamentalUnitDimensionMatches(unknownUnit).size() == 0){
			return false;
		}
		else{
			return true;
		}
	}
	
	///
	public UNIT_TYPE determineUnitType(UnitClass unknownUnit){
		if(unknownUnit.getComponentUnitsExponentMap().size()>1){
			return UNIT_TYPE.DERIVED;
		}
		else if(fundamentalUnitsMap.keySet().contains(unknownUnit.getUnitSystemString())){
			if(fundamentalUnitsMap.get(unknownUnit.getUnitSystemString()).containsKey(unknownUnit.getBaseUnit())){
				return fundamentalUnitsMap.get(unknownUnit.getUnitSystemString()).get(unknownUnit.getBaseUnit());
			}
			else{
				return UNIT_TYPE.UNKNOWN;
			}
		}
		else{
			return UNIT_TYPE.UNKNOWN;
		}
	}
	
	///
	public UnitClass getBaseUnit(UnitClass unknownUnit){
		//TODO:FIX THIS ASAP!!!!!!!
		//Goes through a series of check in order to determine the base unit.Find base unit based on fundamental quantity type i.e mass, temperature.
		if(unknownUnit.isBaseUnit()){ //if the unit is already a fundamental unit, then returns self 
			return unknownUnit;
		}		
		else if(containsUnit(unknownUnit.getUnitNameString())){ //If the unit is already contained in the unit dictionaries, then return the base unit of that located unit.
			return getUnit(unknownUnit.getUnitNameString()).getBaseUnit();
		}
		else if(containsUnitByFundamentalDimension(unknownUnit)){ //However, if the unit can not be found anywhere by name, then tries to match the fundamental unit with one unit in the dictionary. 
			UnitClass unitMatch = getFundamentalUnitDimensionMatches(unknownUnit).get(0);
			if(unitMatch.isBaseUnit()){
				return unitMatch;
			}
		}
		return coreBaseUnitDictionary.get("Unknown_Unit");//If all else fails, then return an unknown unit
	}	
	public Float[] calculateBaseConverPolyCoeffs(UnitClass unknownUnit){
		//TODO:FIX THIS ASAP!!!!!!!
		//TODO:Determine the base conversion polynomials by recursively going through all the component units. For now it only uses factors and ignores the the constant part in the calculations. 
		Float[] polyCoeffs = new Float[]{1.0f, 0.0f};
		
		for(UnitClass componentUnit:unknownUnit.getComponentUnitsExponentMap().keySet()){
			if(componentUnit.getUnitType() == UNIT_TYPE.DERIVED){
				polyCoeffs[0] = (float) ( polyCoeffs[0] * Math.pow(calculateBaseConverPolyCoeffs(unknownUnit)[0], unknownUnit.getComponentUnitsExponentMap().get(componentUnit)) );
			}
			else if(componentUnit.getUnitType() != UNIT_TYPE.UNKNOWN){
				//TODO: Recheck this formulation in order to make sure that conversion is done properly
				//TODO: Make sure that the base units are of the same system 
				polyCoeffs[0] = (float) ( polyCoeffs[0] * Math.pow((componentUnit.getBaseConversionPolyCoeffs()[0]), unknownUnit.getComponentUnitsExponentMap().get(componentUnit)) );
			}
		}
		return polyCoeffs;
	}
	
	//
	public ArrayList<UnitClass> getUnitSystemUnits(String unitSystemName){ //Gets the collection of units that belong to a unit system 
		ArrayList<UnitClass> unitArayList = new ArrayList<UnitClass>();
		
		for(UnitClass unit:coreBaseUnitDictionary.values()){
			if(!unit.getUnitNameString().equalsIgnoreCase(unitSystemName)){
				unitArayList.add(unit);
			}
		}
		for(UnitClass unit:coreUnitDictionary.values()){
			if(!unit.getUnitNameString().equalsIgnoreCase(unitSystemName)){
				unitArayList.add(unit);
			}
		}
		for(UnitClass unit:dynamicBaseUnitDictionary.values()){
			if(!unit.getUnitNameString().equalsIgnoreCase(unitSystemName)){
				unitArayList.add(unit);
			}
		}
		for(UnitClass unit:dynamicUnitDictionary.values()){
			if(!unit.getUnitNameString().equalsIgnoreCase(unitSystemName)){
				unitArayList.add(unit);
			}
		}
		
		return unitArayList;
	}
	public ArrayList<String> getAllUnitSystemNames(){ //Gets a collection of strings with the names of the available unit systems.
		ArrayList<String> usNamesArayList = new ArrayList<String>();
		
		for(UnitClass unit:coreBaseUnitDictionary.values()){
			if(!usNamesArayList.contains(unit.getUnitNameString())){
				usNamesArayList.add(unit.getUnitSystemString());
			}
		}
		for(UnitClass unit:coreUnitDictionary.values()){
			if(!usNamesArayList.contains(unit.getUnitNameString())){
				usNamesArayList.add(unit.getUnitSystemString());
			}
		}
		for(UnitClass unit:dynamicBaseUnitDictionary.values()){
			if(!usNamesArayList.contains(unit.getUnitNameString())){
				usNamesArayList.add(unit.getUnitSystemString());
			}
		}
		for(UnitClass unit:dynamicUnitDictionary.values()){
			if(!usNamesArayList.contains(unit.getUnitNameString())){
				usNamesArayList.add(unit.getUnitSystemString());
			}
		}
		
		return usNamesArayList;
	}
	
	///
	public UnitClass getReducedUnitMatch(UnitClass unit){
		//TODO: Implement a way to reduce reduce the number of component units a system is composed of.
		//TODO: Possible implementation 1: Use the unit's fundamental unit map in order to find single units in the dictionary that is the "best" match. 
		//TODO: Possible implementation 2: Use an efficient combinatorics algorithm that permutates grouping of the fundamental unit until a match is found. If time and effort permit use a genetic algorithm search. Make sure that there is a way for that the search to terminate.
		return null;
	}
	public ArrayList<UnitClass> getFundamentalUnitDimensionMatches(UnitClass unknownUnit){ //Gets units that matches the fundamental unit dimension of the unknown unit
		return getUnitsFromFundamentalUnitsDimension(unknownUnit.getFundamentalUnitsExponentMap());
	}
	
	///
	public UnitClass getUnit(String unitName){
		if(coreBaseUnitDictionary.containsKey(unitName.toLowerCase())){
			return coreBaseUnitDictionary.get(unitName.toLowerCase());
		}
		else if(dynamicBaseUnitDictionary.containsKey(unitName.toLowerCase())){
			return dynamicBaseUnitDictionary.get(unitName.toLowerCase());
		}
		else if(coreUnitDictionary.containsKey(unitName.toLowerCase())){
			return getBaseUnit(coreUnitDictionary.get(unitName.toLowerCase()));
		}
		else if(dynamicUnitDictionary.containsKey(unitName.toLowerCase())){
			return getBaseUnit(dynamicUnitDictionary.get(unitName.toLowerCase()));
		}
		else{
			return coreBaseUnitDictionary.get("Unknown_Unit");
		}		
	}
	public UnitClass getUnit(String prefixName, String unitName){
		//First creates a copy of the unit. Next adds prefix to unit name and change base conversion to reflex prefix value. Then add new unit to appropriate map and return unit. 
		
		if(coreBaseUnitDictionary.containsKey(unitName.toLowerCase())){
			UnitClass prefixedUnit = coreBaseUnitDictionary.get(unitName.toLowerCase()).getCopy();
			prefixedUnit.setUnitNameString(prefixName.toLowerCase()+unitName.toLowerCase());
			prefixedUnit.setBaseConversionPolyCoeffs(new Float[]{prefixedUnit.getBaseConversionPolyCoeffs()[0]/getPrefixValue(prefixName.toLowerCase()), 
																 prefixedUnit.getBaseConversionPolyCoeffs()[1]/getPrefixValue(prefixName.toLowerCase())});
				
			coreBaseUnitDictionary.put(prefixName.toLowerCase()+unitName.toLowerCase(), prefixedUnit);
			return prefixedUnit;
		}
		else if(dynamicBaseUnitDictionary.containsKey(unitName.toLowerCase())){
			UnitClass prefixedUnit = dynamicBaseUnitDictionary.get(unitName.toLowerCase()).getCopy();
			prefixedUnit.setUnitNameString(prefixName.toLowerCase()+unitName.toLowerCase());
			prefixedUnit.setBaseConversionPolyCoeffs(new Float[]{prefixedUnit.getBaseConversionPolyCoeffs()[0]/getPrefixValue(prefixName.toLowerCase()), 
																 prefixedUnit.getBaseConversionPolyCoeffs()[1]/getPrefixValue(prefixName.toLowerCase())});
				
			dynamicBaseUnitDictionary.put(prefixName.toLowerCase()+unitName.toLowerCase(), prefixedUnit);
			return prefixedUnit;
		}
		else if(coreUnitDictionary.containsKey(unitName.toLowerCase())){
			UnitClass prefixedUnit = coreUnitDictionary.get(unitName.toLowerCase()).getCopy();
			prefixedUnit.setUnitNameString(prefixName.toLowerCase()+unitName.toLowerCase());
			prefixedUnit.setBaseConversionPolyCoeffs(new Float[]{prefixedUnit.getBaseConversionPolyCoeffs()[0]/getPrefixValue(prefixName.toLowerCase()), 
																 prefixedUnit.getBaseConversionPolyCoeffs()[1]/getPrefixValue(prefixName.toLowerCase())});
				
			coreUnitDictionary.put(prefixName.toLowerCase()+unitName.toLowerCase(), prefixedUnit);
			return prefixedUnit;
		}
		else if(dynamicUnitDictionary.containsKey(unitName.toLowerCase())){
			UnitClass prefixedUnit = dynamicUnitDictionary.get(unitName.toLowerCase()).getCopy();
			prefixedUnit.setUnitNameString(prefixName.toLowerCase()+unitName.toLowerCase());
			prefixedUnit.setBaseConversionPolyCoeffs(new Float[]{prefixedUnit.getBaseConversionPolyCoeffs()[0]/getPrefixValue(prefixName.toLowerCase()), 
																 prefixedUnit.getBaseConversionPolyCoeffs()[1]/getPrefixValue(prefixName.toLowerCase())});
				
			dynamicUnitDictionary.put(prefixName.toLowerCase()+unitName.toLowerCase(), prefixedUnit);
			return prefixedUnit;
		}
		else{
			return coreBaseUnitDictionary.get("Unknown_Unit");
		}	
	}
	public ArrayList<UnitClass> getUnitsFromComponentUnitsDimension(Map<UnitClass, Float> componentUnitsDimension){
		ArrayList<UnitClass> units = new ArrayList<UnitClass>();
		
		for(UnitClass unit:coreBaseUnitDictionary.values()){
			if(unit.equalsComponentUnitsDimension(componentUnitsDimension)){
				units.add(unit);
			}
		}
		for(UnitClass unit:dynamicBaseUnitDictionary.values()){
			if(unit.equalsComponentUnitsDimension(componentUnitsDimension)){
				units.add(unit);
			}
		}
		for(UnitClass unit:coreUnitDictionary.values()){
			if(unit.equalsComponentUnitsDimension(componentUnitsDimension)){
				units.add(unit);
			}
		}
		for(UnitClass unit:dynamicUnitDictionary.values()){
			if(unit.equalsComponentUnitsDimension(componentUnitsDimension)){
				units.add(unit);
			}
		}
		
		return units;
	}	
	public ArrayList<UnitClass> getUnitsFromComponentUnitsDimension(String componentUnitsDimensionString){
		return getUnitsFromComponentUnitsDimension(getComponentUnitsDimensionFromString(componentUnitsDimensionString));
	}
	public Map<UnitClass, Float> getComponentUnitsDimensionFromString(String componentUnitsDimensionString){
		//TODO: Implement a way to 
		String[] unitName = new String[2]; //The zeroth index is the the actual unit name. If there is any prefix it is located in the first index.
		int unitNameBeginIndex = 0; 
	    int unitNameEndIndex = 0; //Indices used to keep track of where a unit name is located in input string
		int exponentIndex = 0;
		float exponent = 0.0f;
		char[] inputStringArray = componentUnitsDimensionString.toCharArray(); // Needs to have format (unitName)^#
		boolean unitNameFound = false;
		boolean exponentFound = false;
		Map<UnitClass, Float> componentUnitDimension = new HashMap<UnitClass, Float>();
		
		//Parse input string
		for(int i=0; i<inputStringArray.length; i++){
			if(inputStringArray[i] == '('){
				unitNameBeginIndex = i;
			}
			else if(inputStringArray[i] == ')'){
				unitNameEndIndex = i;
				unitNameFound = true;
			}
			else if(inputStringArray[i] == '^'){
				exponentIndex = i+1;
				exponentFound = true;
			}
			if(unitNameFound && exponentFound){
				unitName[0] = componentUnitsDimensionString.substring(unitNameBeginIndex, unitNameEndIndex).trim().toLowerCase(); //Extracts trimmed and lower cased unitName
				if(unitName[0].contains("-")){ // Determines if the name has a prefix based on the presence of '-'. If it is present then splits string into two substrings based around '-'.
					unitName = unitName[0].split("-");
					
					exponent  = Float.parseFloat(componentUnitsDimensionString.substring(exponentIndex, exponentIndex));
					componentUnitDimension.put(getUnit(unitName[0],  unitName[1]), exponent);
					unitNameFound = false;
					exponentFound = false;
				}
				else{
					exponent = Float.parseFloat(componentUnitsDimensionString.substring(exponentIndex, exponentIndex));
					componentUnitDimension.put(getUnit(unitName[0]), exponent);
					unitNameFound = false;
					exponentFound = false;	
				}
			}
		}
		
		return componentUnitDimension;
		
	}
	
 	public ArrayList<UnitClass> getUnitsFromFundamentalUnitsDimension(Map<UNIT_TYPE, Float> fundamentalUnitsDimension){
		ArrayList<UnitClass> units = new ArrayList<UnitClass>();
		
		for(UnitClass unit:coreBaseUnitDictionary.values()){
			if(unit.equalsFundamentalUnitsDimension(fundamentalUnitsDimension)){
				units.add(unit);
			}
		}
		for(UnitClass unit:dynamicBaseUnitDictionary.values()){
			if(unit.equalsFundamentalUnitsDimension(fundamentalUnitsDimension)){
				units.add(unit);
			}
		}
		for(UnitClass unit:coreUnitDictionary.values()){
			if(unit.equalsFundamentalUnitsDimension(fundamentalUnitsDimension)){
				units.add(unit);
			}
		}
		for(UnitClass unit:dynamicUnitDictionary.values()){
			if(unit.equalsFundamentalUnitsDimension(fundamentalUnitsDimension)){
				units.add(unit);
			}
		}
		
		return units;
	}
	public ArrayList<UnitClass> getUnitsFromFundamentalUnitsDimension(String fundamentalUnitsDimensionString){
		return getUnitsFromFundamentalUnitsDimension(getFundamentalUnitsDimensionFromString(fundamentalUnitsDimensionString));
	}
	public Map<UNIT_TYPE, Float> getFundamentalUnitsDimensionFromString(String fundamentalUnitsDimensionString){
		int unitTypeNameBeginIndex = 0; 
	    int unitTypeNameEndIndex = 0; //Indices used to keep track of where a unit type name is located in input string
		int exponentIndex = 0;
		char[] inputStringArray = fundamentalUnitsDimensionString.toCharArray(); // Needs to have format: (unitTypeName)^# or the format: (prefixName-unitTypeName)^#.
		boolean unitTypeNameFound = false;
		boolean exponentFound = false;
		Map<UNIT_TYPE, Float> fundamentalUnitDimension = new HashMap<UNIT_TYPE, Float>();
		
		//Parse input string
		for(int i=0; i<inputStringArray.length; i++){
			if(inputStringArray[i] == '('){
				unitTypeNameBeginIndex = i;
			}
			else if(inputStringArray[i] == ')'){
				unitTypeNameEndIndex = i;
				unitTypeNameFound = true;
			}
			else if(inputStringArray[i] == '^'){
				exponentIndex = i+1;
				exponentFound = true;
			}
			if(unitTypeNameFound && exponentFound){
				String unitTypeName = fundamentalUnitsDimensionString.substring(unitTypeNameBeginIndex, unitTypeNameEndIndex).trim().toLowerCase(); //Extracts trimmed and lower cased unitName
				float exponent = Float.parseFloat(fundamentalUnitsDimensionString.substring(exponentIndex, exponentIndex));
				fundamentalUnitDimension.put(UNIT_TYPE.valueOf(unitTypeName), exponent);
				unitTypeNameFound = false;
				exponentFound = false;
			}
		}
		
		return fundamentalUnitDimension;
	}
	
	public float getPrefixValue(String prefixName){
		if(corePrefixDictionary.containsKey(prefixName.toLowerCase())){
			return corePrefixDictionary.get(prefixName.toLowerCase());
		}
		else if(corePrefixDictionary.containsKey(prefixName.toLowerCase())){
			return corePrefixDictionary.get(prefixName.toLowerCase());
		}
		else{
			return 1.0f;
		}
	}
	
	///
	public ArrayList<UnitClass> getUnitSystemFundamentalUnits(String unitSystemName){ //Gets the collection of fundamental units associated with a particular unit system
		ArrayList<UnitClass> fundUnitsArray = new ArrayList<UnitClass>();
		for(String fundUnitName:fundamentalUnitsMap.get(unitSystemName).keySet()){
			fundUnitsArray.add(getUnit(fundUnitName));
		}
		
		return fundUnitsArray;
	}
}

