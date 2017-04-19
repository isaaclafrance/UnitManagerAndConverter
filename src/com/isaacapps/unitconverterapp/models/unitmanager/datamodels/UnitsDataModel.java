package com.isaacapps.unitconverterapp.models.unitmanager.datamodels;

import java.util.*;
import java.util.Map.*;

import com.isaacapps.unitconverterapp.models.*;
import com.isaacapps.unitconverterapp.models.unitmanager.*;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager.*;

//Provides a centralized and encapsulated hub from retrieving unit 
public class UnitsDataModel extends AbstractDataModelWithDualKeyNCategory<String, Unit, DATA_MODEL_CATEGORY> {	
	private UnitManager unitManagerRef; 
	
	///
	public UnitsDataModel(){
		super(true);
	}
	
	///Modify Content
	public void addUnits(ArrayList<Unit> units){
		for(Unit unit:units){
			addUnit(unit);
		}
	}
	public Unit addUnit(Unit unit){	
		/////////////////////////
		System.out.println(unit.getName());
		//////////////
		
		if(unit.getUnitManagerRef() != this.unitManagerRef || unit.getBaseUnit().getType() == UNIT_TYPE.UNKNOWN || unit.getFundamentalUnitTypesDimension().containsKey(UNIT_TYPE.UNKNOWN)){			
			//Needs to be put in data structure first even if its unknown because referential execution paths in the setUnitManagerRef injector will require the unit to be present in a unknown classification.
			addUnitToDataStructure(unit);
		}
		unit.setUnitManagerRef(this.unitManagerRef);

		//Assess the characteristics(type, dimension, etc) of the unit again after associating it with this unit manager.
		if(!unit.getFundamentalUnitTypesDimension().containsKey(UNIT_TYPE.UNKNOWN) && unit.getBaseUnit().getType() != UNIT_TYPE.UNKNOWN ){
			ArrayList<Unit> allUnitMatches = getUnitsByFundamentalUnitTypeDimension(unit.getFundamentalUnitTypesDimension());
			if((allUnitMatches.isEmpty() ||  allUnitMatches.size() == 1 && allUnitMatches.get(0) == unit)&& !unit.isBaseUnit() //Make the unit to be added a base unit if its the only one of its kind in the unit manager.
			   && !unitManagerRef.getFundamentalUnitsDataModel().containsUnitName(unit.getBaseUnit().getName())){ //Account for edge case where fundamental based unit had been added yet
				unit.setBaseUnit(unit);
			}
			
			if(unit.isBaseUnit()){ 
				//Move any other instances of base units with same dimension to non base unit dictionary, if core units states are compatible
				boolean existingBaseUnitAbleTobeRemoved;	
				for(Unit unitMatch:allUnitMatches){
					existingBaseUnitAbleTobeRemoved = unitMatch.isBaseUnit() && unitMatch.isCoreUnit() == unit.isCoreUnit() // Dynamic units can not replace core units
													  && !unitMatch.getName().equalsIgnoreCase(unit.getName());
					if(existingBaseUnitAbleTobeRemoved){	
						//Makes sure that all units have proper base unit associations after replacement	
						ArrayList<Unit> dependentUnits = new ArrayList<Unit>(unitMatch.getConversionsOfDescendents().keySet()); //Prevent concurrent modification exceptions since 'conversionOfDescendents' can be cleared in setBaseUnit.
						for(Unit dependentUnit:dependentUnits){
							dependentUnit.setBaseUnit(unit);
						}	
						unitMatch.setBaseUnit(unit);		
						addUnitToDataStructure(unitMatch); //Readd unit match to that is it reorganized within data model as a non base unit now. 
							
						break; //Ideally if the unit manager is consistent, there should have only been one valid existing base unit.
					}
				}
			}
			addUnitToDataStructure(unit); //Since the unit has been clearly identified by this, re-add it to the data structure so that it can associated with a more specific category, ie. CORE or DYNAMIC
			
			updateAssociationsOfUnknownUnits();//Determines if this base unit can be associated with units that currently do not have base units.			
		}

		return unit;
	}
	private Unit addUnitToDataStructure(Unit unit){
		return addItem( getModelDataType(unit), unit.getName(), unit.getAbbreviation(), unit, true);
	}
	
	public Unit removeDynamicUnit(String unitName){
		//Removes unit from hierarchy. Removes unit from base and dynamic dictionaries. Updates the base units of dependent units to unknown. Also removed a reference to this unit manager.
		Unit removedUnit = removeItemByAnyKey(unitName);
		unitManagerRef.getUnitsClassifierDataModel().removeFromHierarchy(removedUnit);
		if(removedUnit.isBaseUnit()){
			for(Unit dependentUnit: removedUnit.getConversionsOfDescendents().keySet()){
				dependentUnit.setBaseUnit(getUnit(Unit.UNKNOWN_UNIT_NAME));
			}
			removedUnit.clearConversions();
			removedUnit.setUnitManagerRef(null);
		}
		else{
			removedUnit.getBaseUnit().getConversionsOfDescendents().remove(removedUnit);
			removedUnit.setUnitManagerRef(null);
		}
		
		updateFundamentalUnitsDimensionOfKnownUnits();	
		
		return removedUnit;
	}
	public Unit removeDynamicUnit(Unit unit){
		return removeDynamicUnit(unit.getName());
	}
	public void removeAllDynamicUnits(){
		//Also removes all UNKNOWN units since they would have been classified as DYNAMIC is some of their properties were known
		
		for(Unit unit:getItemsByCategory(DATA_MODEL_CATEGORY.DYNAMIC))
			unitManagerRef.getUnitsClassifierDataModel().removeFromHierarchy(unit);
		
		for(Unit unit:getItemsByCategory(DATA_MODEL_CATEGORY.UNKNOWN))
			unitManagerRef.getUnitsClassifierDataModel().removeFromHierarchy(unit);
	
		removeCategory(DATA_MODEL_CATEGORY.DYNAMIC);
		removeCategory(DATA_MODEL_CATEGORY.DYNAMIC);
	}
	
	///Retrieve Content
	public Unit getUnit(String unitName){         
		return getUnit(unitName, true);
	}
	public Unit getUnit(String unitName, boolean createMissingUnits){
		unitName = unitName.toLowerCase();

		Unit unit = getItemByAnyKey(unitName); //Search by full unit name and abbreviations
		if(unit != null)
			return unit;	
		if(Utility.unitNameHasComplexDimensions(unitName)&& createMissingUnits){
			//Fly Weight design pattern.
			//If unit manager does not already contain unit with similar complex dimension specified by unit name, 
			//then added a newly created version of such unit and store it in the unit manager for easy future access
			
			ArrayList<Unit> complexUnitMatches = getUnitsByComponentUnitsDimension(unitName, false);
			
			if(complexUnitMatches.isEmpty())
				complexUnitMatches.add(new Unit(unitName, false));
			
			return addUnit(complexUnitMatches.get(0));
		}
		
		ArrayList<String[]> prefixMatches = unitManagerRef.getPrefixesDataModel().getPrefixMatches(unitName, true);
		if(!prefixMatches.isEmpty()){
			return addUnit(new PrefixedUnit(prefixMatches.get(0)[0], prefixMatches.get(0)[1]
					 				, unitManagerRef.getPrefixesDataModel().getPrefixValue(prefixMatches.get(0)[1])
					 				, getUnit(unitName.indexOf(prefixMatches.get(0)[0])==0? 
					 						  unitName.replace(prefixMatches.get(0)[0], "")
					 						  :unitName.replace(prefixMatches.get(0)[1], "")),false));
		}
		else{
			return getItemByAnyKey(Unit.UNKNOWN_UNIT_NAME);
		}	
	}	
		
	public ArrayList<Unit> getCoreUnits(){
		return getItemsByCategory(DATA_MODEL_CATEGORY.CORE);	
	}
	public ArrayList<Unit> getDynamicUnits(){
		return getItemsByCategory(DATA_MODEL_CATEGORY.DYNAMIC);				
	}
		
	///Query for Content That Match Particular Conditions
	public ArrayList<Unit> getUnitsByComponentUnitsDimension(Map<String, Double> componentUnitsDimension, boolean overrideToFundamentalUnitsMap){		
		ArrayList<Unit> unitsMatched = new ArrayList<Unit>();
		
		for(Unit unit:getAllItems()){
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
		
		for(Unit unit:getAllItems()){
			if(unit.equalsFundamentalUnitsDimension(fundamentalUnitsDimension)){
				units.add(unit);
			}
		}
		
		return units;
	}
	public ArrayList<Unit> getUnitsByFundamentalUnitsDimension(String fundamentalUnitsDimensionString){
		return getUnitsByFundamentalUnitTypeDimension(unitManagerRef.getUtility().getFundamentalUnitTypesDimensionFromString(fundamentalUnitsDimensionString));
	}
	public ArrayList<Unit> getUnitsWithMatchingFundamentalUnitDimension(Unit unit){
		ArrayList<Unit> matchUnits = new ArrayList<Unit>(); 		
		
		if(unit.getUnitManagerRef() == this.unitManagerRef){
			if(unit.getBaseUnit().isBaseUnit() && !unit.getBaseUnit().getName().equalsIgnoreCase(Unit.UNKNOWN_UNIT_NAME)){
				matchUnits.addAll(unit.getBaseUnit().getConversionsOfDescendents().keySet());
			}else{
				matchUnits.addAll(getUnitsByFundamentalUnitTypeDimension(unit.getFundamentalUnitTypesDimension()));			
			}
		}

		return matchUnits;
	}
	
	public ArrayList<Unit> getUnitsByUnitSystem(String unitSystem){
		ArrayList<Unit> unitMatches = new ArrayList<Unit>();
	
		for(String unitName:unitManagerRef.getUnitsClassifierDataModel().getUnitNamesByUnitSystem(unitSystem))
			unitMatches.add(getUnit(unitName, false));
		
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
						addUnit(replacementUnit);
						correspondingUnits.add(replacementUnit);
					}					
				}
			}
		}
		
		return correspondingUnits;
	}
	
	public Set<Unit> getUnitsWithSimilarNames(final String providedName){
		//Non duplicate data structure since there are instances where full name may be the same as abbreviations.
		SortedSet<Unit> unitCandidates = new TreeSet<Unit>(new Comparator<Unit>(){
			
			//Sort the candidates list by the significance of the provided name to their full name and abbreviated name
			@Override
			public int compare(Unit lhsUnit, Unit rhsUnit) {
				Double lhsUnitFullNameSignificance = (double)providedName.length()/lhsUnit.getName().length();
				Double lhsUnitAbbreviationSignificance = (double)providedName.length()/lhsUnit.getAbbreviation().length();
				
				Double rhsUnitFullNameSignificance = (double)providedName.length()/lhsUnit.getName().length();
				Double rhsUnitAbbreviationSignificance = (double)providedName.length()/lhsUnit.getAbbreviation().length();
				
				Double preferedLhsSignificance = (lhsUnitAbbreviationSignificance<1)?lhsUnitAbbreviationSignificance:lhsUnitFullNameSignificance;
				Double preferedRhsSignificance = (rhsUnitAbbreviationSignificance<1)?rhsUnitAbbreviationSignificance:rhsUnitFullNameSignificance;
				
				return -preferedLhsSignificance.compareTo(preferedRhsSignificance); //need to take negative to order from greatest to least.
			}});		

		for(String unitName:getAllKeys()){
			if(providedName.contains(unitName))
				unitCandidates.add(getItemByAnyKey(unitName));
		}
	
		return unitCandidates;
	}
	
	///
	public Unit getBaseUnitMatch(Unit unit){
	    //Tries to match the fundamental unit with one unit in the dictionary. Only one unit match will ever be needed.
		ArrayList<Unit> unitMatches = getUnitsWithMatchingFundamentalUnitDimension(unit);			
		if(unitMatches.size() > 0){
			for(Unit unitMatch:unitMatches){
				if(unitMatch.isBaseUnit() && !unitMatch.getName().equalsIgnoreCase(unit.getName())){
					return unitMatch;
				}
			}
		}	
		return getUnit(Unit.UNKNOWN_UNIT_NAME);
	}
	public Unit getReducedUnitMatch(Unit unit){
		ArrayList<Unit> matchingUnits = getUnitsByFundamentalUnitTypeDimension(unit.getFundamentalUnitTypesDimension());
		
		//If result set is large enough sort from least to greatest by component unit size, then get the unit with the smallest dimension
		if(matchingUnits.size() > 0){
			Collections.sort(matchingUnits, new Comparator<Unit>() {
				@Override
				public int compare(Unit lhsUnit, Unit rhsUnit) {
					return Double.compare(lhsUnit.getComponentUnitsDimension().size(), rhsUnit.getComponentUnitsDimension().size());
				}
			});
			
			return matchingUnits.get(0);
		}
		else{
			return getUnit(Unit.UNKNOWN_UNIT_NAME);
		}
	}
	
	///Query for Existence of Content
	public boolean containsUnit(String unitName){
		//Name can match to a unit full name or an abbreviation
		return containsKey(unitName.toLowerCase());
	}
	public boolean containsUnit(Unit unit){
		return containsItem(unit);
	}

	
	///Update Content
	public void updateAssociationsOfUnknownUnits(){		
		Unit unit;
		//Need to do this to prevent java.util.ConcurrentModificationException. Even using an instance of the iterator does not work.
		ArrayList<Unit>  unknList = new ArrayList<Unit>(getItemsByCategory(DATA_MODEL_CATEGORY.UNKNOWN));

		for(int i=0;i<unknList.size();i++){
			unit = unknList.get(i); 
			
			unit.setAutomaticUnitTypeNFundmtTypesDim();
			unit.setAutomaticBaseUnit();
			
			if(!(unit.getFundamentalUnitTypesDimension().containsKey(UNIT_TYPE.UNKNOWN)) && unit.getBaseUnit().getType() != UNIT_TYPE.UNKNOWN){
				
				//////////////
				System.out.println( unit.getBaseUnit().getName() +" "+ unit.getBaseUnit().hashCode() + "------" + unit.getName()+" "+unit.hashCode());
				//////////////
				
				addUnit(unit);	
			}
			
			unit.setAutomaticUnitSystem();
		}
	
	}
	public void updateFundamentalUnitsDimensionOfKnownUnits(){
		for(Unit unit:getItemsByCategory(DATA_MODEL_CATEGORY.DYNAMIC)){
			unit.setAutomaticUnitSystem();
			unit.setAutomaticUnitTypeNFundmtTypesDim();
			 
			if(unit.getBaseUnit() == getUnit(Unit.UNKNOWN_UNIT_NAME) || unit.getFundamentalUnitTypesDimension().containsKey(UNIT_TYPE.UNKNOWN)){
				//Readd unit so that it is recategorized as unknown in the data model.
				addUnit(unit);
			}
		}
	}
	
	///
	public static DATA_MODEL_CATEGORY getModelDataType(Unit unit){
		if(unit.getBaseUnit().getType() == UNIT_TYPE.UNKNOWN || unit.getFundamentalUnitTypesDimension().containsKey(UNIT_TYPE.UNKNOWN)){
			return DATA_MODEL_CATEGORY.UNKNOWN;
		}
		else if(unit.isCoreUnit()){
			return DATA_MODEL_CATEGORY.CORE;
		}
		else{
			return DATA_MODEL_CATEGORY.DYNAMIC;
		}
	}
	public UNIT_TYPE determineUnitType(Unit unit){
		UNIT_TYPE type = UNIT_TYPE.UNKNOWN;
		
		if(unit.getComponentUnitsDimension().size()>1){
			type = UNIT_TYPE.DERIVED_MULTI_UNIT;
		}
		else if(unit.getComponentUnitsDimension().size() == 1 
				&&(Math.abs(unit.getComponentUnitsDimension().entrySet().iterator().next().getValue()) > 1 
					       || unit.getComponentUnitsDimension().containsValue(-1)
					       || unit.getComponentUnitsDimension().containsValue(0))){
			type = UNIT_TYPE.DERIVED_SINGLE_UNIT;
		}
		else if(unit.getBaseUnit() != null){
			if(unitManagerRef.getFundamentalUnitsDataModel().containsUnitNameInUnitSystem(unit.getBaseUnit().getUnitSystem(), unit.getBaseUnit().getName())){
				type = unitManagerRef.getFundamentalUnitsDataModel().getUnitTypeByUnitSystemNUnitName(unit.getBaseUnit().getUnitSystem(), unit.getBaseUnit().getName());
			}
			else{
				type = unit.getBaseUnit().getType();
			}
			
			if(type == UNIT_TYPE.UNKNOWN) //If still unknown, then component unit which is assumed to be singular at this point in the logic 
				type = getUnit(unit.getComponentUnitsDimension().keySet().iterator().next()).getType();
		}
		
		return type;
	}
	
	///
	public void setUnitManagerRef(UnitManager unitManager){
		this.unitManagerRef = unitManager;
	}
}
