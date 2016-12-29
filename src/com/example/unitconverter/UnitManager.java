package com.example.unitconverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


//A class that manages and converts between different units. 
public final class UnitManager{
	//Fields
	private Map<String, Unit> coreBaseUnitsDictionary;
	private Map<String, Unit> coreUnitsDictionary; //Store permanent frequently used units
	private Map<String, Double> corePrefixValuesDictionary;
	private Map<String, String> corePrefixAbbreviationsDictionary;
	
	private Map<String, Unit> dynamicBaseUnitsDictionary;
	private Map<String, Unit> dynamicUnitsDictionary;
	private Map<String, Double> dynamicPrefixValuesDictionary;
	private Map<String, String> dynamicPrefixAbbreviationsDictionary;
	
	
	private Map<String, Unit> unitsWithUnknownBaseOrUnknownFundDimension;	
	
	private Map<String, Map<String, UNIT_TYPE>> fundamentalUnitsMap; //Associates a fundamental unit type with particular unitClass stored in the core or dynamic units dictionaries. The first String key is the unit system name and second internal String key is the unit name.
			      
	private Map<String, Map<String, ArrayList<String>>> unitsClassificationMap; //Unit System --> Category --> Group of Units
	
	public static enum UNIT_TYPE{MASS, LENGTH, TIME, AMT_OF_SUBSTANCE, TEMPERATURE, CURRENT, LUMINOUS_INTENSITY, DERIVED_SINGLE_UNIT, DERIVED_MULTI_UNIT, UNKNOWN, CURRENCY};
	
	//Constructors
	public UnitManager(){
		coreBaseUnitsDictionary = new HashMap<String, Unit>();
		coreUnitsDictionary = new HashMap<String, Unit>(); 
		corePrefixValuesDictionary = new HashMap<String, Double>();
		corePrefixAbbreviationsDictionary = new HashMap<String, String>();
		
		dynamicBaseUnitsDictionary = new HashMap<String, Unit>();
		dynamicUnitsDictionary = new HashMap<String, Unit>();
		dynamicPrefixValuesDictionary = new HashMap<String, Double>();
		dynamicPrefixAbbreviationsDictionary = new HashMap<String, String>();
		
		unitsWithUnknownBaseOrUnknownFundDimension = new HashMap<String,Unit>();	
		
		fundamentalUnitsMap = new HashMap<String, Map<String,UNIT_TYPE>>(); 

		unitsClassificationMap = new HashMap<String, Map<String,ArrayList<String>>>();
		
		//Set an unknown base unit to be return when no other unit in manager matches a query.
		Unit unit = new Unit();
		unit.setCoreUnitState(true);
		addUnit(unit);
	}
		
	//Content Modification Methods
	public void addUnits(ArrayList<Unit> units){
		for(Unit unit:units){
			addUnit(unit);
		}
	}
	public void addUnit(Unit unit){		
		if(unit.getBaseUnit().getUnitType() == UNIT_TYPE.UNKNOWN || unit.getFundamentalUnitsExponentMap().containsKey(UNIT_TYPE.UNKNOWN)){			
			unitsWithUnknownBaseOrUnknownFundDimension.put(unit.getUnitName(), unit);
			unit.setUnitManagerRef(this);
			if(unit.getBaseUnit().getUnitType() != UNIT_TYPE.UNKNOWN && !unit.getFundamentalUnitsExponentMap().containsKey(UNIT_TYPE.UNKNOWN)){
				addUnit(unit);
			}
		}else{
			if(unit.isBaseUnit()){ 
				//Move any other instances of base units with same dimension to non base unit dictionary	
				for(Unit unitMatch:getUnitsByFundamentalUnitsDimension(unit.getFundamentalUnitsExponentMap())){
					if(unitMatch.isBaseUnit()){
						if(unit.getCoreUnitState()){
							dynamicBaseUnitsDictionary.remove(unitMatch.getUnitName());
							dynamicUnitsDictionary.put(unitMatch.getUnitName(), unitMatch);
						}
						else{
							coreBaseUnitsDictionary.remove(unitMatch.getUnitName());
							coreUnitsDictionary.put(unitMatch.getUnitName(), unitMatch);
						}
										
						//Makes sure that all units have proper base unit associations after replacement		
						//unit.conversionPolyCoeffs = unitMatch.conversionPolyCoeffs;
						ArrayList<Unit> conversionUnits = new ArrayList<Unit>(unitMatch.conversionPolyCoeffs.keySet());
						for(Unit dependentUnit:conversionUnits){
							dependentUnit.setBaseUnit(unit);
						}		
						//break;
					}
				}
				//If unit already exists in nonbase dictionary then remove it since it will instead be added to base dictionary.				
				if(unit.getCoreUnitState()){
					if(coreUnitsDictionary.containsValue(unit)){
						coreUnitsDictionary.remove(unit.getUnitName());
					}					
				}
				else{
					if(dynamicUnitsDictionary.containsValue(unit)){
						dynamicUnitsDictionary.remove(unit.getUnitName());
					}
				}
	
				//Ensures association with only this unit manager
				if(unit.getBaseUnit().getUnitManagerRef()!= this){
					unit.setBaseUnit(getUnit(Unit.UNKNOWN_UNIT_NAME));
					unit.setUnitManagerRef(this);
				}				
				
				//If unit still unknown after being associated with unit manager, then it is isolated. Or else at to dictionary.
				if(unit.getBaseUnit().getUnitType() == UNIT_TYPE.UNKNOWN || unit.getFundamentalUnitsExponentMap().containsKey(UNIT_TYPE.UNKNOWN)){
					unitsWithUnknownBaseOrUnknownFundDimension.put(unit.getUnitName(), unit);
				}else{
					if(unit.getCoreUnitState()){
						coreBaseUnitsDictionary.put(unit.getUnitName(), unit);	
					}
					else{
						dynamicBaseUnitsDictionary.put(unit.getUnitName(), unit);	
					}
						
					updateAssociationsOfUnknownUnits();//Determines if this base unit can be associated with units that currently do not have base units.						
				}	
			}
			else{			
				unit.setUnitManagerRef(this);
				
				//If unit still unknown after incorporation into the unit manager, then isolate it into a group of units with unknown components.
				if(unit.getBaseUnit().getUnitType() == UNIT_TYPE.UNKNOWN || unit.getFundamentalUnitsExponentMap().containsKey(UNIT_TYPE.UNKNOWN)){
					unitsWithUnknownBaseOrUnknownFundDimension.put(unit.getUnitName(), unit);
				}else{
					if(unit.getCoreUnitState()){
						coreUnitsDictionary.put(unit.getUnitName(), unit);	
					}
					else{
						dynamicUnitsDictionary.put(unit.getUnitName(), unit);	
					}	
					unitsWithUnknownBaseOrUnknownFundDimension.remove(unit.getUnitName());
					
					updateAssociationsOfUnknownUnits();
				}
				
			}
		}
		//TODO: Use for efficient data structure.
		removeUnitFromHierarchy(unit);
		addUnitToHierarchy(unit);
	}
	public void removeDynamicUnit(String unitName){
		if(dynamicBaseUnitsDictionary.containsKey(unitName)){
			//Removed unit from base dictionary. Updates the base units of dependent units to unknown. Also removed a reference to this unit manager.
			Unit removedBaseUnit =	dynamicBaseUnitsDictionary.remove(unitName);	
			for(Unit dependentUnit: removedBaseUnit.conversionPolyCoeffs.keySet()){
				dependentUnit.setBaseUnit(getUnit(Unit.UNKNOWN_UNIT_NAME));
			}
			removedBaseUnit.clearConversions();
			removedBaseUnit.setUnitManagerRef(null);
		}
		else if(dynamicUnitsDictionary.containsKey(unitName)){
			Unit removedUnit =	dynamicUnitsDictionary.remove(unitName);
			removedUnit.setUnitManagerRef(null);
			removedUnit.getBaseUnit().conversionPolyCoeffs.remove(removedUnit);
			dynamicUnitsDictionary.remove(unitName);
			updateFundamentalUnitsDimensionOfKnownUnits();	
		}
	}
	public void removeDynamicUnit(Unit unit){
		removeDynamicUnit(unit.getUnitName());
	}
	public void removeAllDynamicUnits(){
		for(Unit unit:dynamicBaseUnitsDictionary.values()){
			removeUnitFromHierarchy(unit);
		}
		for(Unit unit:dynamicUnitsDictionary.values()){
			removeUnitFromHierarchy(unit);
		}
		
		dynamicBaseUnitsDictionary.clear();
		dynamicUnitsDictionary.clear();
	}
	public void updateAssociationsOfUnknownUnits(){		
		Unit unit;
		for(final Iterator<Entry<String, Unit>> entryInterator = unitsWithUnknownBaseOrUnknownFundDimension.entrySet().iterator(); entryInterator.hasNext();){
			unit = entryInterator.next().getValue();

			//Remove unit for where it was previous in hierarchy. Add to it to new location based on classification.
			//TODO: Use for efficient data structure.
			removeUnitFromHierarchy(unit);
			
			unit.setAutomaticUnitTypeNFundmtUnitsExpMap();
			unit.setAutomaticBaseUnit(true);	
			unit.setAutomaticUnitSystem();			
			
			if(!(unit.getBaseUnit() == getUnit(Unit.UNKNOWN_UNIT_NAME) || unit.getFundamentalUnitsExponentMap().containsKey(UNIT_TYPE.UNKNOWN))){
				entryInterator.remove();
				if(unit.isBaseUnit()){
					if(unit.getCoreUnitState()){
						coreBaseUnitsDictionary.put(unit.getUnitName(), unit);
					}
					else{
						dynamicBaseUnitsDictionary.put(unit.getUnitName(), unit);
					}
				}
				else{
					if(unit.getCoreUnitState()){
						coreUnitsDictionary.put(unit.getUnitName(), unit);
					}
					else{
						dynamicUnitsDictionary.put(unit.getUnitName(), unit);
					}
				}
			}
			
			//Remove unit for where it was previous in hierarchy. Add to it to new location based on classification.
			//TODO: Use for efficient data structure. 
			addUnitToHierarchy(unit);
		}
	}
	private void updateFundamentalUnitsDimensionOfKnownUnits(){
		updateFundamentalUnitsDimensionOfKnownUnits(dynamicBaseUnitsDictionary);
		updateFundamentalUnitsDimensionOfKnownUnits(dynamicUnitsDictionary);
	}
	private void updateFundamentalUnitsDimensionOfKnownUnits(Map<String, Unit> unitsDictionary){
		for(Unit unit:unitsDictionary.values()){
			//Remove unit for where it was previous in hierarchy. Add to it to new location based on classification.
			//TODO: Use for efficient data structure.
			removeUnitFromHierarchy(unit);
			
			unit.setAutomaticUnitSystem();
			unit.setAutomaticUnitTypeNFundmtUnitsExpMap();
			
			if(unit.getBaseUnit() == getUnit(Unit.UNKNOWN_UNIT_NAME) || unit.getFundamentalUnitsExponentMap().containsKey(UNIT_TYPE.UNKNOWN)){
				unitsDictionary.remove(unit.getUnitName());
				unitsWithUnknownBaseOrUnknownFundDimension.put(unit.getUnitName(), unit);
			}
			
			//Remove unit for where it was previous in hierarchy. Add to it to new location based on classification.
			//TODO: Use for efficient data structure.
			addUnitToHierarchy(unit);
		}
	}
	
	void addCorePrefix(String prefixName, String abbreviation, double prefixValue){
		corePrefixValuesDictionary.put(prefixName, prefixValue);
		corePrefixAbbreviationsDictionary.put(prefixName, abbreviation);
	}
	public void addDynamicPrefix(String prefixName, String abbreviation, double prefixValue){
		dynamicPrefixValuesDictionary.put(prefixName, prefixValue);
		dynamicPrefixAbbreviationsDictionary.put(prefixName, abbreviation);
	}
	public void removeDynamicPrefix(String prefixName){
		dynamicPrefixValuesDictionary.remove(prefixName);
	}	
	public void removeAllDynamicPrefixes(){
		dynamicPrefixValuesDictionary.clear();
	}	
	
	void setFundamentalUnitsMap(Map<String, Map<String, UNIT_TYPE>> fundamentalUnitsMap){
		this.fundamentalUnitsMap = fundamentalUnitsMap;
	}
	
	///Retrieve Units
	public Unit getUnit(String unitName){
		return getUnit(unitName, true);
	}
	Unit getUnit(String unitName, boolean createMissingPrefixedUnits){
		unitName = unitName.toLowerCase();
		
		if(coreBaseUnitsDictionary.containsKey(unitName)){
			return coreBaseUnitsDictionary.get(unitName);
		}
		else if(dynamicBaseUnitsDictionary.containsKey(unitName)){
			return dynamicBaseUnitsDictionary.get(unitName);
		}
		else if(coreUnitsDictionary.containsKey(unitName)){
			return coreUnitsDictionary.get(unitName);
		}
		else if(dynamicUnitsDictionary.containsKey(unitName)){
			return dynamicUnitsDictionary.get(unitName);
		}
		else if(unitsWithUnknownBaseOrUnknownFundDimension.containsKey(unitName)){
			return unitsWithUnknownBaseOrUnknownFundDimension.get(unitName);
		}
		else if(unitName.contains("-") && createMissingPrefixedUnits){ //Checks if unit name has a prefix component, then extracts it.
			String[] unitNameNPrefix = unitName.split("-");
			return getUnit(unitNameNPrefix[0], unitNameNPrefix[1]);
		}
		else{
			return unitsWithUnknownBaseOrUnknownFundDimension.get(Unit.UNKNOWN_UNIT_NAME);
		}
	}
	private Unit getUnit(String prefixName, String unitName){
		//First creates a copy of the unit. Next adds prefix to unit name and change base conversion to reflex prefix value. Then add new unit to appropriate map and return unit. 
		
		prefixName = prefixName.toLowerCase();		
		Unit unit = getUnit(unitName);
		Unit prefixedUnit;
		double prefixValue = getPrefixValue(prefixName);
		
		if(!unit.getUnitName().equalsIgnoreCase(Unit.UNKNOWN_UNIT_NAME) && prefixValue != 0.0f){
			double[] bConPolyCoeffs = new double[]{unit.getBaseConversionPolyCoeffs()[0]*getPrefixValue(prefixName), 
					 											 unit.getBaseConversionPolyCoeffs()[1]*getPrefixValue(prefixName)};		
			Map<String, Double> componentUnitMap = new HashMap<String, Double>(); componentUnitMap.put(prefixName+"-"+unitName, 1.0);
			prefixedUnit = new Unit(componentUnitMap, false);
			prefixedUnit.setBaseUnit(unit, bConPolyCoeffs);
			prefixedUnit.setAbbreviation(getPrefixAbbreviation(prefixName)+unit.getAbbreviation());
			
			addUnit(prefixedUnit);
		}
		else{
			prefixedUnit = unit;
		}
		
		return prefixedUnit;
	}
		
	public ArrayList<Unit> getCoreUnits(){
		ArrayList<Unit> coreUnits = new ArrayList<Unit>();
		
		coreUnits.addAll(coreBaseUnitsDictionary.values());
		coreUnits.addAll(coreUnitsDictionary.values());
	
		return coreUnits;	
	}
	public ArrayList<Unit> getDynamicUnits(){
		ArrayList<Unit> dynamicUnits = new ArrayList<Unit>();
		
		dynamicUnits.addAll(dynamicBaseUnitsDictionary.values());
		dynamicUnits.addAll(dynamicUnitsDictionary.values());
	
		return dynamicUnits;			
	}
	
	///Retrieve Prefixes
	public double getPrefixValue(String prefixName){
		prefixName = prefixName.toLowerCase();
		
		if(corePrefixValuesDictionary.containsKey(prefixName)){
			return corePrefixValuesDictionary.get(prefixName);
		}
		else if(dynamicPrefixValuesDictionary.containsKey(prefixName)){
			return dynamicPrefixValuesDictionary.get(prefixName);
		}
		else{
			return 0.0f;
		}
	}	
	public String getPrefixAbbreviation(String prefixName){
		prefixName = prefixName.toLowerCase();
		
		if(corePrefixAbbreviationsDictionary.containsKey(prefixName)){
			return corePrefixAbbreviationsDictionary.get(prefixName);
		}
		else if(dynamicPrefixAbbreviationsDictionary.containsKey(prefixName)){
			return dynamicPrefixAbbreviationsDictionary.get(prefixName);
		}
		else{
			return "";
		}
	}
	
	public Map<String, Double> getAllPrefixValues(){
		Map<String, Double> allPrefixValuesMap = new HashMap<String, Double>();
		
		allPrefixValuesMap.putAll(corePrefixValuesDictionary);
		allPrefixValuesMap.putAll(dynamicPrefixValuesDictionary);
		
		return allPrefixValuesMap;
	}	
	public Map<String, String> getAllPrefixAbbreviations(){
		Map<String, String> allPrefixAbbreviationsMap = new HashMap<String, String>();
		
		allPrefixAbbreviationsMap.putAll(corePrefixAbbreviationsDictionary);
		allPrefixAbbreviationsMap.putAll(dynamicPrefixAbbreviationsDictionary);
		
		return allPrefixAbbreviationsMap;
	}		

	///Query Existence of Units
	public boolean containsUnit(String unitName){
		unitName = unitName.toLowerCase();
		
		if(coreBaseUnitsDictionary.containsKey(unitName)){
			return true;
		}
		else if(coreUnitsDictionary.containsKey(unitName)){
			return true;
		}
		else if(dynamicBaseUnitsDictionary.containsKey(unitName)){
			return true;
		}
		else if(dynamicUnitsDictionary.containsKey(unitName)){
			return true;
		}
		else if(unitsWithUnknownBaseOrUnknownFundDimension.containsKey(unitName)){
			return true;
		}
		else{
			return false;
		}
	}
	public boolean containsUnit(Unit unit){
		if(coreBaseUnitsDictionary.containsValue(unit)){
			return true;
		}
		else if(coreUnitsDictionary.containsValue(unit)){
			return true;
		}
		else if(dynamicBaseUnitsDictionary.containsValue(unit)){
			return true;
		}
		else if(dynamicUnitsDictionary.containsValue(unit)){
			return true;
		}
		else if(unitsWithUnknownBaseOrUnknownFundDimension.containsValue(unit)){
			return true;
		}
		else{
			return false;
		}
	}
	public boolean containsUnitByFundamentalDimension(Unit unknownUnit){
		if(containsUnit(unknownUnit)){
			return true;
		}
		if(getUnitsWithMatchingFundamentalUnitDimension(unknownUnit).size() != 0){
			return true;
		}
		else{
			return false;
		}
	}
	
	///Query for Units That Match Particular Conditions
	public ArrayList<Unit> getUnitsByComponentUnitsDimension(Map<String, Double> componentUnitsDimension){		
		ArrayList<Unit> unitsMatched = new ArrayList<Unit>();
		
		for(Unit unit:coreBaseUnitsDictionary.values()){
			if(unit.equalsComponentUnitsDimension(componentUnitsDimension)){
				unitsMatched.add(unit);
			}
		}
		for(Unit unit:dynamicBaseUnitsDictionary.values()){
			if(unit.equalsComponentUnitsDimension(componentUnitsDimension)){
				unitsMatched.add(unit);
			}
		}
		for(Unit unit:coreUnitsDictionary.values()){
			if(unit.equalsComponentUnitsDimension(componentUnitsDimension)){
				unitsMatched.add(unit);
			}
		}
		for(Unit unit:dynamicUnitsDictionary.values()){
			if(unit.equalsComponentUnitsDimension(componentUnitsDimension)){
				unitsMatched.add(unit);
			}
		}
		for(Unit unit:unitsWithUnknownBaseOrUnknownFundDimension.values()){
			if(unit.equalsComponentUnitsDimension(componentUnitsDimension)){
				unitsMatched.add(unit);
			}
		}
				
		return unitsMatched;
	}	
	public ArrayList<Unit> getUnitsByComponentUnitsDimension(String componentUnitsDimensionString){
		return getUnitsByComponentUnitsDimension(getComponentUnitsDimensionFromString(componentUnitsDimensionString));
	}
	
 	public ArrayList<Unit> getUnitsByFundamentalUnitsDimension(Map<UNIT_TYPE, Double> fundamentalUnitsDimension){
		ArrayList<Unit> units = new ArrayList<Unit>();
		
		for(Unit unit:coreBaseUnitsDictionary.values()){
			if(unit.equalsFundamentalUnitsDimension(fundamentalUnitsDimension)){
				units.add(unit);
			}
		}
		for(Unit unit:dynamicBaseUnitsDictionary.values()){
			if(unit.equalsFundamentalUnitsDimension(fundamentalUnitsDimension)){
				units.add(unit);
			}
		}
		for(Unit unit:coreUnitsDictionary.values()){
			if(unit.equalsFundamentalUnitsDimension(fundamentalUnitsDimension)){
				units.add(unit);
			}
		}
		for(Unit unit:dynamicUnitsDictionary.values()){
			if(unit.equalsFundamentalUnitsDimension(fundamentalUnitsDimension)){
				units.add(unit);
			}
		}
		
		return units;
	}
	public ArrayList<Unit> getUnitsByFundamentalUnitsDimension(String fundamentalUnitsDimensionString){
		return getUnitsByFundamentalUnitsDimension(getFundamentalUnitsDimensionFromString(fundamentalUnitsDimensionString));
	}
	
	public ArrayList<Unit> getUnitsByUnitSystem(String unitSystemName){
		ArrayList<Unit> unitMatches = new ArrayList<Unit>();
		
		for(Entry<String, ArrayList<String>> categoryEntry:getUnitsClassificationMap().get(unitSystemName).entrySet()){
			for(String unitName:categoryEntry.getValue()){
				getUnit(unitName, false);
			}
		}
		
		return unitMatches;
	}
	public ArrayList<Unit> getUnitsByCategory(String unitCategory){
		ArrayList<Unit> unitMatches = new ArrayList<Unit>();
		
		for(Entry<String, Map<String, ArrayList<String>>> unitSystemsEntry:getUnitsClassificationMap().entrySet()){
			for(String unitName:unitSystemsEntry.getValue().get(unitCategory)){
				getUnit(unitName, false);
			}
		}
		
		return unitMatches;
	}
	
	public ArrayList<Unit> getCorrespondingUnitsWithUnitSystem(Unit sourceUnit, String targetUnitSystemString){
		ArrayList<Unit> correspondingUnits = new ArrayList<Unit>();		
		
		if(sourceUnit.getUnitManagerRef() == this){	
			//Find a way to convert every component unit to one unit system. Then return resulting unit or return self.
			if(!sourceUnit.getUnitSystem().contains(targetUnitSystemString) && !sourceUnit.getUnitSystem().contains(" and ") && sourceUnit.getComponentUnitsExponentMap().size() == 1){
				ArrayList<Unit> matchCandidates = getUnitsByComponentUnitsDimension(sourceUnit.getComponentUnitsExponentMap());
				if(matchCandidates.size() != 0){
					for(Unit candidate:matchCandidates){
					    if(sourceUnit.getUnitSystem().equalsIgnoreCase(candidate.getUnitSystem())){
					    	correspondingUnits.add(candidate);
					    }
					}
				}
			}else{
				if(!sourceUnit.getFundamentalUnitsExponentMap().containsKey(UNIT_TYPE.UNKNOWN)){
					//Find replacement componentUnits in order to determine proper unit dimension associated with proper unitSystem.
					Unit replacementUnit = null;
					Map<String, Double> properComponentUnitDimension = new HashMap<String, Double>();
					
					for(Entry<String, Double> componentUnitEntry:sourceUnit.getComponentUnitsExponentMap().entrySet()){
						//TODO: CHECK RECURSION ASAP!!!!!!!!!
						
						replacementUnit = getCorrespondingUnitsWithUnitSystem(getUnit(componentUnitEntry.getKey(), false), targetUnitSystemString).get(0);
						properComponentUnitDimension.put(replacementUnit.getUnitName(), componentUnitEntry.getValue());
					}
					
					//See if unitManager already contains unit with proper dimension and return that unit. Otherwise return a new unit with proper dimensions and add this unit manager.
					ArrayList<Unit> matchCandidates = getUnitsByComponentUnitsDimension(properComponentUnitDimension);
					if(matchCandidates.size() != 0){
						correspondingUnits.addAll(matchCandidates);
					}
					else{
						replacementUnit = new Unit(properComponentUnitDimension, false);
						addUnit(replacementUnit);
						correspondingUnits.add(replacementUnit);
					}					
				}
			}
		}
		
		return correspondingUnits;
	}
	
	private ArrayList<Unit> getUnitsWithMatchingFundamentalUnitDimension(Unit unit){
		ArrayList<Unit> matchList = new ArrayList<Unit>(); 		
		
		if(unit.getUnitManagerRef() == this){
			if(!unit.getBaseUnit().getUnitName().equalsIgnoreCase(Unit.UNKNOWN_UNIT_NAME)){
				matchList.addAll(unit.getBaseUnit().getConversionPolyCoeffs().keySet());
			}else{
				matchList.addAll(getUnitsByFundamentalUnitsDimension(unit.getFundamentalUnitsExponentMap()));			
			}
		}

		return matchList;
	}
	
	//Conversion Methods.
	public double[] getConversionFactorToTargetUnit(Unit sourceUnit, Unit targetUnit){
		double[] bCPC = new double[]{0.0f,0.0f};
		
		if(sourceUnit.getUnitManagerRef() == this && targetUnit.getUnitManagerRef() == this){
			if(sourceUnit.equalsDimension(targetUnit) && targetUnit.getBaseUnit().getUnitType() != UNIT_TYPE.UNKNOWN){
				if(sourceUnit.getBaseConversionPolyCoeffs()[1]==0.0f && targetUnit.getBaseConversionPolyCoeffs()[1]==0.0f){
					bCPC = new double[]{sourceUnit.getBaseConversionPolyCoeffs()[0] / targetUnit.getBaseConversionPolyCoeffs()[0], 0.0f};				
				}
				else{
					bCPC = new double[]{sourceUnit.getBaseConversionPolyCoeffs()[0] / targetUnit.getBaseConversionPolyCoeffs()[0],
									  (sourceUnit.getBaseConversionPolyCoeffs()[1] - targetUnit.getBaseConversionPolyCoeffs()[1])/targetUnit.getBaseConversionPolyCoeffs()[0]};
				} 
			}else{
				bCPC = new double[]{0.0f, 0.0f};
			}
		}

		return bCPC; 
	}
	public double[] getConversionFactorToUnitSystem(Unit sourceUnit, String  targetUnitSystemString){		
		double[] conversionFactor = new double[]{0.0f, 0.0f};
		
		if(sourceUnit.getUnitManagerRef() == this){
			//Convert every component unit to one unit system. Then return conversion factor associated with this conversion. 
			if(sourceUnit.getUnitSystem().contains(targetUnitSystemString) && !sourceUnit.getUnitSystem().contains(" and ")){
				conversionFactor = new double[]{1.0f, 0.0f}; 
			}
			else if(!sourceUnit.getUnitSystem().contains(targetUnitSystemString) && !sourceUnit.getUnitSystem().contains(" and ") && sourceUnit.getComponentUnitsExponentMap().size() == 1){
				ArrayList<Unit> candidateUnitsWithProperUnitSystem = getUnitsByUnitSystem(targetUnitSystemString);
				
				if(candidateUnitsWithProperUnitSystem.size() != 0){
					Unit matchingUnit = null;;
					for(Unit candidateUnit:candidateUnitsWithProperUnitSystem){
						if(sourceUnit.equalsFundamentalUnitsDimension(candidateUnit.getFundamentalUnitsExponentMap())){
							matchingUnit = candidateUnit;
						}
					}
					if(matchingUnit != null){
						conversionFactor = getConversionFactorToTargetUnit(sourceUnit, matchingUnit);
					}else{
						conversionFactor = new double[]{0.0f, 0.0f};
					}
				}else{
					conversionFactor = new double[]{0.0f, 0.0f};
				}
			}else{	
				if(!sourceUnit.getFundamentalUnitsExponentMap().containsKey(UNIT_TYPE.UNKNOWN)){
					Unit componentUnit;
					conversionFactor = new double[]{1.0f, 0.0f};					
					for(Entry<String, Double> entry:sourceUnit.getComponentUnitsExponentMap().entrySet()){
						componentUnit =  getUnit(entry.getKey(), false);
						conversionFactor = new double[]{ conversionFactor[0] * (double)Math.pow(getConversionFactorToUnitSystem(componentUnit, targetUnitSystemString)[0], entry.getValue()), 0.0f};
					}
				}
			}	
		}
		
		return conversionFactor;
	}
	
	///Other Utility Methods
	public Unit getBaseUnit(Unit unit){
		Unit baseUnit = coreBaseUnitsDictionary.get(Unit.UNKNOWN_UNIT_NAME);
		
		if(containsUnit(unit.getUnitName())){ //If the unit is already contained in the unit dictionaries, then return the base unit of that located unit.
			baseUnit = getUnit(unit.getUnitName());
		}
		else{ //However, if the unit can not be found anywhere by name, then tries to match the fundamental unit with one unit in the dictionary. 
			ArrayList<Unit> unitMatches = getUnitsWithMatchingFundamentalUnitDimension(unit);			
			if(unitMatches.size() > 0){
				for(Unit unitMatch:unitMatches){
					if(unitMatch.isBaseUnit()){
						baseUnit = unitMatch;
						break;
					}
				}
			}
		}
		
		return baseUnit;
	}	
	public Unit getReducedUnitMatch(Unit unit){
		//TODO: Implement a way to reduce reduce the number of component units a system is composed of.
		//TODO: Possible implementation 1: Use the unit's fundamental unit map in order to find single units in the dictionary that are "best" matches. 
		//TODO: Possible implementation 2: Use an efficient combinatorics algorithm that permutates grouping of the fundamental units until a match is found. If time and effort permit use a genetic algorithm search. Make sure that there is a way for that the search to terminate.
		return null;
	}	
	
	public Map<UNIT_TYPE, Double> calculateFundmtUnitsFromCompUnitsExpMap(Map<String, Double> compUnitsExpMap){
		Map<UNIT_TYPE, Double> map = new HashMap<UnitManager.UNIT_TYPE, Double>();
				
		//Goes through each component unit whether derived or and sums up the recursively obtained total occurances of the fundamental units. Makes sure to multiply those totals by the exponent of the component unit.
		Unit componentUnit;
		for(String componentUnitName:compUnitsExpMap.keySet()){
			componentUnit = getUnit(componentUnitName, false);
			if(componentUnit.getUnitType() == UNIT_TYPE.DERIVED_MULTI_UNIT){
				Map<UNIT_TYPE, Double> recursedMap = calculateFundmtUnitsFromCompUnitsExpMap(componentUnit.getComponentUnitsExponentMap());	
				for(UNIT_TYPE unitType:UNIT_TYPE.values()){	
					
					if(recursedMap.containsKey(unitType)){ 
						map.put(unitType, 0.0);					
						map.put(unitType,  
								map.get(unitType)+ compUnitsExpMap.get(componentUnitName)*recursedMap.get(unitType));					
					}				
				}
			}
			else{
				map.put(componentUnit.getUnitType(), compUnitsExpMap.get(componentUnitName));
			}
		}
		
		return map;
	}
	public UNIT_TYPE determineUnitType(Unit unit){
		UNIT_TYPE type;
		
		if(unit.getComponentUnitsExponentMap().size()>1){
			type = UNIT_TYPE.DERIVED_MULTI_UNIT;
		}
		else if(unit.getComponentUnitsExponentMap().size() == 1 
				&&(Math.abs(unit.getComponentUnitsExponentMap().entrySet().iterator().next().getValue()) > 1 
					       || unit.getComponentUnitsExponentMap().entrySet().iterator().next().getValue() == -1
					       || unit.getComponentUnitsExponentMap().entrySet().iterator().next().getValue() == 0)){
			type = UNIT_TYPE.DERIVED_SINGLE_UNIT;
		}
		else if(unit.getBaseUnit() != null){
			if(fundamentalUnitsMap.keySet().contains(unit.getBaseUnit().getUnitSystem()) && fundamentalUnitsMap.get(unit.getBaseUnit().getUnitSystem()).containsKey(unit.getBaseUnit().getUnitName())){
				type = fundamentalUnitsMap.get(unit.getBaseUnit().getUnitSystem()).get(unit.getBaseUnit().getUnitName());
			}
			else{
				type = unit.getBaseUnit().getUnitType();
			}
		}
		else{
			type = UNIT_TYPE.UNKNOWN;
		}
		
		return type;
	}
	
	public static Map<String, Double> getComponentUnitsDimensionFromString(String componentUnitsDimensionString){
		Map<String, Double> componentUnitsDimensionMap = new HashMap<String, Double>(); 		

		//RegEx pattern to parse string into groups of unit names and exponents succeeded or preceded by '/', '*', or ' '
		//Valid group example: a^(#), (prefix-a)^#, a^#, (a)^(#)  
		Pattern groupRegExPattern = Pattern.compile("([\\*|\\/]?[\\s]*)((([\\(]?[\\s]*[a-zA-Z]+([\\-][a-zA-Z]+)?[\\s]*[\\)]?)([\\s]*\\^[\\s]*([\\(]?[\\s]*[-+]?(\\d*[.])?\\d+[\\s]*[\\)]?)))|[a-zA-Z]+)");
		Matcher groupRegExMatcher = groupRegExPattern.matcher(componentUnitsDimensionString);
		
		//RegEx pattern to extract prefixes and unit name
		Pattern prefixNunitNameRegExPattern = Pattern.compile("[a-zA-Z]+([\\-][a-zA-Z]+)?");
		
		//RegEx pattern to extract exponent
		Pattern exponentRegExPattern = Pattern.compile("[\\-]?[\\d]+");
		
		//Perform extraction using RegEx patterns
		if(groupRegExMatcher.find()){
			do{
				String groupString = groupRegExMatcher.group();
				
				Matcher prefixNunitNameRegExMatcher = prefixNunitNameRegExPattern.matcher(groupString);
				prefixNunitNameRegExMatcher.find();
				String prefixNunitName = prefixNunitNameRegExMatcher.group();
				
				Matcher exponentRegExMatcher = exponentRegExPattern.matcher(groupString);
				String exponent;
				if(exponentRegExMatcher.find()){
					exponent = exponentRegExMatcher.group();
				}else{
					exponent = "1.0f"; //If the unit is not raised by any number then it assumed to be raised by 1.
				}
							
				if(componentUnitsDimensionMap.containsKey(prefixNunitName)){
					componentUnitsDimensionMap.put(prefixNunitName, 
												   Double.valueOf(exponent) + ((groupString.charAt(0) == '/')?-componentUnitsDimensionMap.get(prefixNunitName):componentUnitsDimensionMap.get(prefixNunitName)));	
				}else{
					componentUnitsDimensionMap.put(prefixNunitName, Double.valueOf((groupString.charAt(0) == '/')?("-")+exponent:("")+exponent));						
				}
				
			}while(groupRegExMatcher.find());
		}
		else{
			//throw new Exception("Format not recognized at all. Try: (prefix-unitName1)^(#) (* or /) (unitName2)^(#)");
			componentUnitsDimensionMap.put(Unit.UNKNOWN_UNIT_NAME, 1.0);
		}
		return componentUnitsDimensionMap;
	}
	public static Map<UNIT_TYPE, Double> getFundamentalUnitsDimensionFromString(String fundamentalUnitsDimensionString){
		Map<UNIT_TYPE, Double> fundamentalUnitsDimensionMap = new HashMap<UNIT_TYPE, Double>(); 		

		//RegEx pattern to parse string into groups of unit type and exponents succeeded or preceded by '/', '*', or ' '
		//Valid group example: a^(#), (a)^#, a^#, (a)^(#)  
		Pattern groupRegExPattern = Pattern.compile("([\\*|\\/]?[\\s]*)((([\\(]?[\\s]*[a-zA-Z]+[\\s]*[\\)]?)([\\s]*\\^[\\s]*([\\(]?[\\s]*[-+]?(\\d*[.])?\\d+[\\s]*[\\)]?)))|[a-zA-Z]+)");
		Matcher groupRegExMatcher = groupRegExPattern.matcher(fundamentalUnitsDimensionString);
		
		//RegEx pattern to extract unit type
		Pattern unitTypeRegExPattern = Pattern.compile("[a-zA-Z]+");
		
		//RegEx pattern to extract exponent
		Pattern exponentRegExPattern = Pattern.compile("[\\-]?[\\d]+");
		
		//Perform extraction using RegEx patterns
		if(groupRegExMatcher.find()){
			do{
				String groupString = groupRegExMatcher.group();
				
				//TODO:Remove hyphen recognition
				Matcher unitTypeRegExMatcher = unitTypeRegExPattern.matcher(groupString);
				unitTypeRegExMatcher.find();
				String unitTypeString = unitTypeRegExMatcher.group();
				unitTypeString.toLowerCase();
				
				Matcher exponentRegExMatcher = exponentRegExPattern.matcher(groupString);
				String exponent;
				if(exponentRegExMatcher.matches()){
					exponentRegExMatcher.find();
					exponent = exponentRegExMatcher.group();
				}else{
					exponent = "1.0f"; //If the unit type is not raised by any number then it assumed to be raised by 1.
				}

				UNIT_TYPE unitType = UNIT_TYPE.valueOf(unitTypeString);					
				if(fundamentalUnitsDimensionMap.containsKey(unitType)){
					fundamentalUnitsDimensionMap.put(unitType, 
													   Double.valueOf(exponent) + ((groupString.charAt(0) == '/')?-fundamentalUnitsDimensionMap.get(unitType):fundamentalUnitsDimensionMap.get(unitType)));	
				}else{
					fundamentalUnitsDimensionMap.put(unitType, Double.valueOf((groupString.charAt(0) == '/')?("-"):("")+exponent));						
				}
			}while(groupRegExMatcher.find());
		}
		else{
			//throw new Exception("Format not recognized at all. Try: (unitType1)^(#) (* or /) (unitName2)^(#)");
			fundamentalUnitsDimensionMap.put(UNIT_TYPE.UNKNOWN, 1.0);
		}
		return fundamentalUnitsDimensionMap;
	}
	
	///Create Classification Map of Unit Groups Based on Their Unit Systems and Categories
	public Map<String, Map<String, ArrayList<String>>> getUnitsClassificationMap(){			
		return unitsClassificationMap;
	}
	private void addUnitToHierarchy(Unit unit){
		boolean doesUnitSystemExist;
		
		if(unitsClassificationMap.containsKey(unit.getUnitSystem())){
			doesUnitSystemExist = true;
		}
		else{
			doesUnitSystemExist = false;
		}
		
		addUnitToUnitSystem(unit, unit.getUnitSystem(), doesUnitSystemExist);
	}	
	private void addUnitToUnitSystem(Unit unit, String unitSystem, boolean doesUnitSystemExist){
		boolean doesUnitCategoryExist;
		
		if(!doesUnitSystemExist){
			unitsClassificationMap.put(unitSystem, new HashMap<String, ArrayList<String>>());
		}
		
		if(unitsClassificationMap.get(unitSystem).containsKey(unit.getUnitCategory())){
			doesUnitCategoryExist = true;
		}
		else{
			doesUnitCategoryExist = false;
		}
		
		addUnitToUnitCategory(unit, unit.getUnitSystem(), unit.getUnitCategory(), doesUnitCategoryExist);		
	}
	private void addUnitToUnitCategory(Unit unit, String unitSystem, String category, boolean doesUnitCategoryExist){
		
		if(!doesUnitCategoryExist){
			unitsClassificationMap.get(unitSystem).put(category, new ArrayList<String>());
		}
		
		unitsClassificationMap.get(unitSystem).get(category).add(unit.getUnitName());
	}
	private void removeUnitFromHierarchy(Unit unit){
		if(unitsClassificationMap.containsKey(unit.getUnitSystem())){
			if(unitsClassificationMap.get(unit.getUnitSystem()).containsKey(unit.getUnitCategory())){
				unitsClassificationMap.get(unit.getUnitSystem()).get(unit.getUnitCategory()).remove(unit.getUnitName());
				if(unitsClassificationMap.get(unit.getUnitSystem()).get(unit.getUnitCategory()).size() == 0){
					unitsClassificationMap.get(unit.getUnitSystem()).remove(unit.getUnitCategory());
				}
			}
			if(unitsClassificationMap.get(unit.getUnitSystem()).size() == 0){
				unitsClassificationMap.remove(unit.getUnitSystem());
			}
		}
	}
	
}

