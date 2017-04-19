package com.isaacapps.unitconverterapp.models.unitmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.isaacapps.unitconverterapp.models.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager.UNIT_TYPE;

public class QueryExecutor {
	UnitManager unitManagerRef;
	
	///
	public QueryExecutor(UnitManager unitManagerRef){
		this.unitManagerRef = unitManagerRef;
	}
	
	///Retrieve Units
	public Unit getUnit(String unitName){         
		return getUnit(unitName, true);
	}
	public Unit getUnit(String unitName, boolean createMissingUnits){
		unitName = unitName.toLowerCase();
		
		//TODO:Account for abbreviations when searching
		
		Unit unit;
		unit = unitManagerRef.getDataMaps().getCoreBaseUnitsMap().get(unitName);
			if(unit != null)return unit;	
		unit = unitManagerRef.getDataMaps().getDynamicBaseUnitsMap().get(unitName);
			if(unit != null)return unit;
		unit = unitManagerRef.getDataMaps().getCoreUnitsMap().get(unitName);
			if(unit != null)return unit;	
		unit = unitManagerRef.getDataMaps().getDynamicUnitsMap().get(unitName);
			if(unit != null)return unit;
		unit = unitManagerRef.getDataMaps().getUnitsWithUnknownBaseOrUnknownFundDimensionMap().get(unitName);
			if(unit != null){
				return unit;	
			}
			else if(unitName.contains("*") || unitName.contains("/") || unitName.contains("^")
					   || unitName.contains("(") || unitName.contains(")") && createMissingUnits){
				//Fly Wheel design pattern
				//If unit manager does not already contain unit with similar complex dimension specified by unit name, 
				//then added a newly created version of such unit and store it in the unit manager for easy future access
				
				ArrayList<Unit> complexUnitMatches = getUnitsByComponentUnitsDimension(unitName, false);
				
				if(complexUnitMatches.isEmpty()){
					complexUnitMatches.add(new Unit(unitName, true));
				}
				
				unitManagerRef.getContentModifier().addUnit(complexUnitMatches.get(0));
				return complexUnitMatches.get(0);
			}
			else if(unitName.contains("-") && createMissingUnits){ //Checks if unit name has a prefix component, then extracts it.
				String[] unitNameNPrefix = unitName.split("-");
				return getUnit(unitNameNPrefix[0], unitNameNPrefix[1]);
			}
			else{
				return unitManagerRef.getDataMaps().getUnitsWithUnknownBaseOrUnknownFundDimensionMap().get(Unit.UNKNOWN_UNIT_NAME);
			}	
	}
	private Unit getUnit(String prefixName, String unitName){
		//First gets or creates a copy of the unit. Next adds prefix to unit name and changes base conversion to reflect prefix value. Then adds new unit to appropriate map and returns unit. 		
		prefixName = prefixName.toLowerCase();		
		Unit unit = getUnit(unitName);
		Unit prefixedUnit;
		double prefixValue = getPrefixValue(prefixName);
		
		if(!unit.getName().equalsIgnoreCase(Unit.UNKNOWN_UNIT_NAME) && prefixValue != 0.0){
			double[] bConPolyCoeffs = new double[]{unit.getBaseConversionPolyCoeffs()[0]*getPrefixValue(prefixName), 
					 											 unit.getBaseConversionPolyCoeffs()[1]};		
			Map<String, Double> componentUnitMap = new HashMap<String, Double>(); componentUnitMap.put(prefixName+"-"+unitName, 1.0);
			prefixedUnit = new Unit(componentUnitMap, false);
			prefixedUnit.setBaseUnit(unit, bConPolyCoeffs);
			prefixedUnit.setAbbreviation(getPrefixAbbreviation(prefixName)+unit.getAbbreviation());
			
			unitManagerRef.getContentModifier().addUnit(prefixedUnit);
		}
		else{
			prefixedUnit = unit;
		}
		
		return prefixedUnit;
	}
		
	public ArrayList<Unit> getCoreUnits(){
		ArrayList<Unit> coreUnits = new ArrayList<Unit>();
		
		coreUnits.addAll(unitManagerRef.getDataMaps().getCoreBaseUnitsMap().values());
		coreUnits.addAll(unitManagerRef.getDataMaps().getCoreUnitsMap().values());
	
		return coreUnits;	
	}
	public ArrayList<Unit> getDynamicUnits(){
		ArrayList<Unit> dynamicUnits = new ArrayList<Unit>();
		
		dynamicUnits.addAll(unitManagerRef.getDataMaps().getDynamicBaseUnitsMap().values());
		dynamicUnits.addAll(unitManagerRef.getDataMaps().getDynamicUnitsMap().values());
	
		return dynamicUnits;			
	}
	
	///Retrieve Prefixes
	public double getPrefixValue(String prefixName){
		prefixName = prefixName.toLowerCase();
		
		Double prefixValue;
		prefixValue = unitManagerRef.getDataMaps().getCorePrefixValuesMap().get(prefixName);
			if(prefixValue != null) return prefixValue;
		prefixValue = unitManagerRef.getDataMaps().getDynamicPrefixValuesMap().get(prefixName);
			if(prefixValue != null){
				return prefixValue;
			}else{
				return 0.0;
			}
	}	
	public String getPrefixAbbreviation(String prefixName){
		prefixName = prefixName.toLowerCase();
		
		String prefixAbbrev;
		prefixAbbrev = unitManagerRef.getDataMaps().getCorePrefixAbbreviationsMap().get(prefixName);
			if(prefixAbbrev != null) return prefixAbbrev;
		prefixAbbrev = unitManagerRef.getDataMaps().getDynamicPrefixAbbreviationsMap().get(prefixName);
			if(prefixAbbrev != null){
				return prefixAbbrev;
			}else{
				return "";
			}
	}
	
	public Map<String, Double> getAllPrefixValues(){
		Map<String, Double> allPrefixValuesMap = new HashMap<String, Double>();
		
		allPrefixValuesMap.putAll(unitManagerRef.getDataMaps().getCorePrefixValuesMap());
		allPrefixValuesMap.putAll(unitManagerRef.getDataMaps().getDynamicPrefixValuesMap());
		
		return allPrefixValuesMap;
	}	
	public Map<String, String> getAllPrefixAbbreviations(){
		Map<String, String> allPrefixAbbreviationsMap = new HashMap<String, String>();
		
		allPrefixAbbreviationsMap.putAll(unitManagerRef.getDataMaps().getCorePrefixAbbreviationsMap());
		allPrefixAbbreviationsMap.putAll(unitManagerRef.getDataMaps().getDynamicPrefixAbbreviationsMap());
		
		return allPrefixAbbreviationsMap;
	}		

	///Query for Existence of Units
	public boolean containsUnit(String unitName){
		unitName = unitName.toLowerCase();
		
		//Does not account for abbreviations
		if(unitManagerRef.getDataMaps().getCoreBaseUnitsMap().containsKey(unitName)){
			return true;
		}
		else if(unitManagerRef.getDataMaps().getCoreUnitsMap().containsKey(unitName)){
			return true;
		}
		else if(unitManagerRef.getDataMaps().getDynamicBaseUnitsMap().containsKey(unitName)){
			return true;
		}
		else if(unitManagerRef.getDataMaps().getDynamicUnitsMap().containsKey(unitName)){
			return true;
		}
		else if(unitManagerRef.getDataMaps().getUnitsWithUnknownBaseOrUnknownFundDimensionMap().containsKey(unitName)){
			return true;
		}
		else{
			return false;
		}
	}
	public boolean containsUnit(Unit unit){
		if(unitManagerRef.getDataMaps().getCoreBaseUnitsMap().containsValue(unit)){
			return true;
		}
		else if(unitManagerRef.getDataMaps().getCoreUnitsMap().containsValue(unit)){
			return true;
		}
		else if(unitManagerRef.getDataMaps().getDynamicBaseUnitsMap().containsValue(unit)){
			return true;
		}
		else if(unitManagerRef.getDataMaps().getDynamicUnitsMap().containsValue(unit)){
			return true;
		}
		else if(unitManagerRef.getDataMaps().getUnitsWithUnknownBaseOrUnknownFundDimensionMap().containsValue(unit)){
			return true;
		}
		else{
			return false;
		}
	}
	public boolean containsUnitByFundamentalDimension(Unit unit){
		if(containsUnit(unit)){
			return true;
		}
		if(getUnitsWithMatchingFundamentalUnitDimension(unit).size() != 0){
			return true;
		}
		else{
			return false;
		}
	}
	
	///Query for Units That Match Particular Conditions
	public ArrayList<Unit> getUnitsByComponentUnitsDimension(Map<String, Double> componentUnitsDimension, boolean overrideToFundamentalUnitsMap){		
		ArrayList<Unit> unitsMatched = new ArrayList<Unit>();
		
		for(Unit unit:unitManagerRef.getDataMaps().getCoreBaseUnitsMap().values()){
			if(unit.equalsComponentUnitsDimension(componentUnitsDimension, overrideToFundamentalUnitsMap)){
				unitsMatched.add(unit);
			}
		}
		for(Unit unit:unitManagerRef.getDataMaps().getDynamicBaseUnitsMap().values()){
			if(unit.equalsComponentUnitsDimension(componentUnitsDimension, overrideToFundamentalUnitsMap)){
				unitsMatched.add(unit);
			}
		}
		for(Unit unit:unitManagerRef.getDataMaps().getCoreUnitsMap().values()){
			if(unit.equalsComponentUnitsDimension(componentUnitsDimension, overrideToFundamentalUnitsMap)){
				unitsMatched.add(unit);
			}
		}
		for(Unit unit:unitManagerRef.getDataMaps().getDynamicUnitsMap().values()){
			if(unit.equalsComponentUnitsDimension(componentUnitsDimension, overrideToFundamentalUnitsMap)){
				unitsMatched.add(unit);
			}
		}
		for(Unit unit:unitManagerRef.getDataMaps().getUnitsWithUnknownBaseOrUnknownFundDimensionMap().values()){
			if(unit.equalsComponentUnitsDimension(componentUnitsDimension, overrideToFundamentalUnitsMap)){
				unitsMatched.add(unit);
			}
		}
				
		return unitsMatched;
	}	
	public ArrayList<Unit> getUnitsByComponentUnitsDimension(String componentUnitsDimensionString, boolean overrideToFundamentalUnitsMap){
		return getUnitsByComponentUnitsDimension(unitManagerRef.getUtility().getComponentUnitsDimensionFromString(componentUnitsDimensionString), overrideToFundamentalUnitsMap);
	}
	
 	public ArrayList<Unit> getUnitsByFundamentalUnitTypeDimension(Map<UNIT_TYPE, Double> fundamentalUnitsDimension){
		ArrayList<Unit> units = new ArrayList<Unit>();
		
		for(Unit unit:unitManagerRef.getDataMaps().getCoreBaseUnitsMap().values()){
			if(unit.equalsFundamentalUnitsDimension(fundamentalUnitsDimension)){
				units.add(unit);
			}
		}
		for(Unit unit:unitManagerRef.getDataMaps().getDynamicBaseUnitsMap().values()){
			if(unit.equalsFundamentalUnitsDimension(fundamentalUnitsDimension)){
				units.add(unit);
			}
		}
		for(Unit unit:unitManagerRef.getDataMaps().getCoreUnitsMap().values()){
			if(unit.equalsFundamentalUnitsDimension(fundamentalUnitsDimension)){
				units.add(unit);
			}
		}
		for(Unit unit:unitManagerRef.getDataMaps().getDynamicUnitsMap().values()){
			if(unit.equalsFundamentalUnitsDimension(fundamentalUnitsDimension)){
				units.add(unit);
			}
		}
		
		return units;
	}
	public ArrayList<Unit> getUnitsByFundamentalUnitsDimension(String fundamentalUnitsDimensionString){
		return getUnitsByFundamentalUnitTypeDimension(unitManagerRef.getUtility().getFundamentalUnitTypesDimensionFromString(fundamentalUnitsDimensionString));
	}
	
	public ArrayList<Unit> getUnitsByUnitSystem(String unitSystemName){
		ArrayList<Unit> unitMatches = new ArrayList<Unit>();
		
		for(Entry<String, ArrayList<String>> categoryEntry:unitManagerRef.getUnitsClassifier().getHierarchy().get(unitSystemName).entrySet()){
			for(String unitName:categoryEntry.getValue()){
				getUnit(unitName, false);
			}
		}
		
		return unitMatches;
	}
	public ArrayList<Unit> getUnitsByCategory(String unitCategory){
		ArrayList<Unit> unitMatches = new ArrayList<Unit>();
		
		for(Entry<String, Map<String, ArrayList<String>>> unitSystemsEntry:unitManagerRef.getUnitsClassifier().getHierarchy().entrySet()){
			for(String unitName:unitSystemsEntry.getValue().get(unitCategory)){
				unitMatches.add(getUnit(unitName, false));
			}
		}
		
		return unitMatches;
	}
	
	public ArrayList<Unit> getCorrespondingUnitsWithUnitSystem(Unit sourceUnit, String targetUnitSystemString){
		ArrayList<Unit> correspondingUnits = new ArrayList<Unit>();		
		
		if(sourceUnit.getUnitManagerRef() == this.unitManagerRef){	
			//Find a way to convert every component unit to one unit system. Then return resulting unit.
			if(!sourceUnit.getUnitSystem().contains(targetUnitSystemString) && !sourceUnit.getUnitSystem().contains(" and ") && sourceUnit.getComponentUnitsDimension().size() == 1){
				ArrayList<Unit> matchCandidates = getUnitsByComponentUnitsDimension(sourceUnit.getComponentUnitsDimension(), true);
				if(matchCandidates.size() != 0){
					for(Unit candidate:matchCandidates){
					    if(sourceUnit.getUnitSystem().equalsIgnoreCase(candidate.getUnitSystem())){
					    	correspondingUnits.add(candidate);
					    }
					}
				}
			}else{
				if(!sourceUnit.getFundamentalUnitTypesDimension().containsKey(UNIT_TYPE.UNKNOWN)){
					//Find replacement componentUnits in order to determine proper unit dimension associated with proper unitSystem.
					Unit replacementUnit = null;
					Map<String, Double> properComponentUnitDimension = new HashMap<String, Double>();
					
					for(Entry<String, Double> componentUnitEntry:sourceUnit.getComponentUnitsDimension().entrySet()){				
						replacementUnit = getCorrespondingUnitsWithUnitSystem(getUnit(componentUnitEntry.getKey(), false), targetUnitSystemString).get(0);
						properComponentUnitDimension.put(replacementUnit.getName(), componentUnitEntry.getValue());
					}
					
					//See if unitManager already contains unit with proper dimension and return that unit. Otherwise return a new unit with proper dimensions and add this unit manager.
					ArrayList<Unit> matchCandidates = getUnitsByComponentUnitsDimension(properComponentUnitDimension, true);
					if(matchCandidates.size() != 0){
						correspondingUnits.addAll(matchCandidates);
					}
					else{
						replacementUnit = new Unit(properComponentUnitDimension, false);
						unitManagerRef.getContentModifier().addUnit(replacementUnit);
						correspondingUnits.add(replacementUnit);
					}					
				}
			}
		}
		
		return correspondingUnits;
	}
	
	public ArrayList<Unit> getUnitsWithMatchingFundamentalUnitDimension(Unit unit){
		ArrayList<Unit> matchList = new ArrayList<Unit>(); 		
		
		if(unit.getUnitManagerRef() == this.unitManagerRef){
			if(unit.getBaseUnit().isBaseUnit() && !unit.getBaseUnit().getName().equalsIgnoreCase(Unit.UNKNOWN_UNIT_NAME)){
				matchList.addAll(unit.getBaseUnit().getConversionPolyCoeffs().keySet());
			}else{
				matchList.addAll(getUnitsByFundamentalUnitTypeDimension(unit.getFundamentalUnitTypesDimension()));			
			}
		}

		return matchList;
	}
}
