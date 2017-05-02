package com.isaacapps.unitconverterapp.models.unitmanager.datamodels;

import java.util.*;
import java.util.Map.*;

import com.isaacapps.unitconverterapp.models.*;
import com.isaacapps.unitconverterapp.models.unitmanager.*;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager.*;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel.*;

//Provides a centralized and encapsulated hub for storing and retrieving units
public class UnitsDataModel extends AbstractDataModelWithDualKeyNCategory<String, Unit, DATA_MODEL_CATEGORY> {	
	private UnitManager unitManagerRef; 
	
	///
	public UnitsDataModel(){
		super(true);
		
		//Set an unknown base unit to be return when no other unit in data model matches a query.
		Unit unit = new Unit();
		unit.setCoreUnitState(true);
		addUnit(unit);
	}
	
	///Modify Content
	public void addUnits(List<Unit> units){
		for(Unit unit:units)
			addUnit(unit);
	}
	public Unit addUnit(Unit unit){	
		/////////////////////////
		System.out.println(unit.getName());
		//////////////
		
		///////////////////////
		if(unit.getName().equals("radian")){
			int a = 1;
			a++;
		}
		///////////////////////////
		
		
		//Needs to be put in data structure first even if its unknown because referential execution paths in the setUnitManagerRef injector will require the unit to be present in a unknown classification.
		addUnitToDataStructure(unit);
		
		unit.setUnitManagerRef(this.unitManagerRef);

		//Assess the characteristics(type, dimension, etc) of the unit again after associating it with this unit manager.
		if(UnitsDataModel.getDataModelCategory(unit) != DATA_MODEL_CATEGORY.UNKNOWN){
			Collection<Unit> allUnitMatches = getUnitsByFundamentalUnitTypeDimension(unit.getFundamentalUnitTypesDimension());
			boolean unitIsOneOfAKind = allUnitMatches.isEmpty() ||  allUnitMatches.size() == 1 && allUnitMatches.iterator().next() == unit; //Make the unit to be added a base unit if its the only one of its kind in the unit manager.
			boolean fundamentalBaseUnitNotYetAdded = !unitManagerRef.getFundamentalUnitsDataModel().containsUnitName(unit.getBaseUnit().getName()); //Account for edge case where fundamental based unit had been added yet
			
			if(unitIsOneOfAKind && !unit.isBaseUnit() && fundamentalBaseUnitNotYetAdded)
				unit.setBaseUnit(unit);
			
			if(unit.isBaseUnit()){ 
				//Move any other instances of base units with same dimension to non base unit dictionary, if core units states are compatible
				boolean existingBaseUnitAbleTobeRemoved;	
				for(Unit unitMatch:allUnitMatches){
					existingBaseUnitAbleTobeRemoved = unitMatch.isBaseUnit() && unitMatch.isCoreUnit() == unit.isCoreUnit() // Dynamic units can not replace core units
													  && !unitMatch.getName().equalsIgnoreCase(unit.getName());
					if(existingBaseUnitAbleTobeRemoved){	
						//Makes sure that all units have proper base unit associations after replacement	
						List<Unit> dependentUnits = new ArrayList<Unit>(unitMatch.getConversionsOfDescendents().keySet()); //Prevent concurrent modification exceptions since 'conversionOfDescendents' can be cleared in setBaseUnit.
						for(Unit dependentUnit:dependentUnits)
							dependentUnit.setBaseUnit(unit);

						unitMatch.setBaseUnit(unit);		
						addUnitToDataStructure(unitMatch); //Readd unit match to that is it reorganized within data model as a non base unit now. 
							
						break; //Ideally if the data model is consistent, there should have only been one valid existing base unit.
					}
				}
			}
			addUnitToDataStructure(unit); //Since the unit has been clearly identified by this point, re-add it to the data structure so that it can associated with a more specific category, ie. CORE or DYNAMIC
			
			updateAssociationsOfUnknownUnits();//Determines if this base unit can be associated with units that currently do not have base units.			
		}

		return unit;
	}
	private Unit addUnitToDataStructure(Unit unit){
		return addItem( getDataModelCategory(unit), unit.getName(), unit.getAbbreviation(), unit, true);
	}
	
	public Unit removeDynamicUnit(String unitName){
		//Removes unit from hierarchy. Removes unit from base and dynamic dictionaries. Updates the base units of dependent units to unknown. Also removed a reference to this unit manager.
		Unit removedUnit = removeItemByAnyKey(unitName);
		unitManagerRef.getUnitsClassifierDataModel().removeFromHierarchy(removedUnit);
		if(removedUnit.isBaseUnit()){
			for(Unit dependentUnit: removedUnit.getConversionsOfDescendents().keySet()){
				dependentUnit.setBaseUnit(getUnknownUnit());
			}
			removedUnit.clearConversions();
			removedUnit.setUnitManagerRef(null);
		}
		else{
			removedUnit.getBaseUnit().getConversionsOfDescendents().remove(removedUnit);
			removedUnit.setUnitManagerRef(null);
		}
		
		updateFundamentalUnitsDimensionOfKnownUnits();	
		
		//Remove any conversion favorites that may be associated with this unit as well update the significance rankings
		unitManagerRef.getConversionFavoritesDataModel().removeConversionByUnit(removedUnit);
		
		return removedUnit;
	}
	public Unit removeDynamicUnit(Unit unit){
		return removeDynamicUnit(unit.getName());
	}
	public void removeAllDynamicUnits(){
		//Also removes all UNKNOWN units since they would have been classified as DYNAMIC is some of their properties were known
		
		for(Unit unitToBeRemoved:getItemsByCategory(DATA_MODEL_CATEGORY.DYNAMIC)){
			unitManagerRef.getUnitsClassifierDataModel().removeFromHierarchy(unitToBeRemoved);
			unitManagerRef.getConversionFavoritesDataModel().removeConversionByUnit(unitToBeRemoved);
		}
			
		for(Unit unitToBeRemoved:getItemsByCategory(DATA_MODEL_CATEGORY.UNKNOWN)){ 
			unitManagerRef.getUnitsClassifierDataModel().removeFromHierarchy(unitToBeRemoved);
			//Unknown units do not have conversions therefore there is no need to remove them from the conversion favorites
		}
	
		removeCategory(DATA_MODEL_CATEGORY.DYNAMIC);
		removeCategory(DATA_MODEL_CATEGORY.UNKNOWN);
	}
	
	///Retrieve Content
	public Unit getUnit(String unitName){         
		return getUnit(unitName, true);
	}
	public Unit getUnit(String unitName, boolean createMissingUnits){
		unitName = unitName.toLowerCase();

		
		///////////////////////
		if(unitName.equals("drachm")){
			int a = 1;
			a++;
		}
		///////////////////////////
		
		Unit unit = getItemByAnyKey(unitName); //Search by full unit name and abbreviations
		if(unit != null)
			return unit;	
		if(Utility.unitNameHasComplexDimensions(unitName)&& createMissingUnits){
			//Fly Weight design pattern.
			//If unit manager does not already contain unit with exact complex component dimension specified by unit name, 
			//then added a newly created version of such unit and store it in the unit manager for easy future access
			
			Collection<Unit> complexUnitMatches = getUnitsByComponentUnitsDimension(unitName, false);
			
			if(complexUnitMatches.isEmpty())
				complexUnitMatches.add(new Unit(unitName, false));
			
			return addUnit(complexUnitMatches.iterator().next());
		}
		
		List<String[]> prefixMatches = unitManagerRef.getPrefixesDataModel().getPrefixMatches(unitName, true);
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
	public Unit getUnknownUnit(){
		return getUnit(Unit.UNKNOWN_UNIT_NAME);
	}
	
	public Collection<Unit> getCoreUnits(){
		return getItemsByCategory(DATA_MODEL_CATEGORY.CORE);	
	}
	public Collection<Unit> getDynamicUnits(){
		return getItemsByCategory(DATA_MODEL_CATEGORY.DYNAMIC);				
	}
		
	///Query for Content That Match Particular Conditions
	public Collection<Unit> getUnitsByComponentUnitsDimension(Map<String, Double> componentUnitsDimension, boolean overrideToFundamentalUnitsMap){		
		List<Unit> unitsMatched = new ArrayList<Unit>();
		
		for(Unit unit:getAllItems()){
			if(unit.equalsComponentUnitsDimension(componentUnitsDimension, overrideToFundamentalUnitsMap))
				unitsMatched.add(unit);
		}
				
		return unitsMatched;
	}	
	public Collection<Unit> getUnitsByComponentUnitsDimension(String componentUnitsDimensionString, boolean overrideToFundamentalUnitsMap){
		return getUnitsByComponentUnitsDimension(Utility.getComponentUnitsDimensionFromString(componentUnitsDimensionString), overrideToFundamentalUnitsMap);
	}
	
 	public Collection<Unit> getUnitsByFundamentalUnitTypeDimension(Map<UNIT_TYPE, Double> fundamentalUnitsDimension){
		List<Unit> units = new ArrayList<Unit>();
		
		for(Unit unit:getAllItems()){
			if(unit.equalsFundamentalUnitsDimension(fundamentalUnitsDimension))
				units.add(unit);
		}
		
		return units;
	}
	public Collection<Unit> getUnitsByFundamentalUnitsDimension(String fundamentalUnitsDimensionString){
		return getUnitsByFundamentalUnitTypeDimension(Utility.getFundamentalUnitTypesDimensionFromString(fundamentalUnitsDimensionString));
	}
	public Collection<Unit> getUnitsWithMatchingFundamentalUnitDimension(Unit unit){
		List<Unit> matchUnits = new ArrayList<Unit>(); 		
		
		if(unit.getUnitManagerRef() == this.unitManagerRef){
			if(unit.getBaseUnit().isBaseUnit() && UnitsDataModel.getDataModelCategory(unit.getBaseUnit()) != DATA_MODEL_CATEGORY.UNKNOWN){
				matchUnits.addAll(unit.getBaseUnit().getConversionsOfDescendents().keySet());
			}else{
				matchUnits.addAll(getUnitsByFundamentalUnitTypeDimension(unit.getFundamentalUnitTypesDimension()));			
			}
		}

		return matchUnits;
	}
	
	public Collection<Unit> getUnitsByUnitSystem(String unitSystem){
		List<Unit> unitMatches = new ArrayList<Unit>();
	
		for(String unitName:unitManagerRef.getUnitsClassifierDataModel().getUnitNamesByUnitSystem(unitSystem))
			unitMatches.add(getUnit(unitName, false));
		
		return unitMatches;
	}
	public Collection<Unit> getCorrespondingUnitsWithUnitSystem(Unit sourceUnit, String targetUnitSystemString){
		List<Unit> correspondingUnits = new ArrayList<Unit>();		
		
		if(sourceUnit.getUnitManagerRef() == this.unitManagerRef){	
			//Find a way to convert every component unit to one unit system. Then return resulting unit.
			if(!sourceUnit.getUnitSystem().contains(targetUnitSystemString) && !sourceUnit.getUnitSystem().contains(Unit.UNIT_SYSTEM_DELIMITER) && sourceUnit.getComponentUnitsDimension().size() == 1){
				Collection<Unit> matchCandidates = getUnitsByComponentUnitsDimension(sourceUnit.getComponentUnitsDimension(), true);
				if(!matchCandidates.isEmpty()){
					for(Unit candidate:matchCandidates){
					    if(sourceUnit.getUnitSystem().equalsIgnoreCase(candidate.getUnitSystem()))
					    	correspondingUnits.add(candidate);
					}
				}
			}else{
				if(UnitsDataModel.getDataModelCategory(sourceUnit) != DATA_MODEL_CATEGORY.UNKNOWN){
					//Find replacement componentUnits in order to determine proper unit dimension associated with proper unitSystem.
					Unit replacementUnit = null;
					Map<String, Double> properComponentUnitDimension = new HashMap<String, Double>();
					
					for(Entry<String, Double> componentUnitEntry:sourceUnit.getComponentUnitsDimension().entrySet()){				
						replacementUnit = getCorrespondingUnitsWithUnitSystem(getUnit(componentUnitEntry.getKey(), false), targetUnitSystemString).iterator().next();
						properComponentUnitDimension.put(replacementUnit.getName(), componentUnitEntry.getValue());
					}
					
					//See if unitManager already contains unit with proper dimension and return that unit. Otherwise return a new unit with proper dimensions and add this unit manager.
					 Collection<Unit> matchCandidates = getUnitsByComponentUnitsDimension(properComponentUnitDimension, true);
					if(matchCandidates.size() != 0){
						correspondingUnits.addAll(matchCandidates);
					}else{
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
				
				//Onyl se
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
	    /*Tries to match the fundamental unit with one unit in the dictionary. Only one unit match will ever be needed,
	     *since if the data model is logically consistent all similar units will have the same base unit */ 
		Collection<Unit> unitMatches = getUnitsWithMatchingFundamentalUnitDimension(unit);			
		if(unitMatches.size() > 0){
			for(Unit unitMatch:unitMatches){
				if(unitMatch.isBaseUnit() && !unitMatch.getName().equalsIgnoreCase(unit.getName()))
					return unitMatch;
			}
		}	
		return getUnknownUnit();
	}
	public Unit getReducedUnitMatch(Unit unit){
		//If result set is large enough sort from least to greatest by component unit size, then get the unit with the smallest dimension
		TreeSet<Unit> matchingUnits = new TreeSet<Unit>(new Comparator<Unit>() {
			@Override
			public int compare(Unit lhsUnit, Unit rhsUnit) {
				return Double.compare(lhsUnit.getComponentUnitsDimension().size(), rhsUnit.getComponentUnitsDimension().size());
			}
		});
		matchingUnits.addAll(getUnitsByFundamentalUnitTypeDimension(unit.getFundamentalUnitTypesDimension()));
		if(!matchingUnits.isEmpty()){
			return matchingUnits.first();
		}
		else{
			return getUnknownUnit();
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
		//TODO: Need to create a new list instance to prevent java.util.ConcurrentModificationException. Even using an instance of the iterator does not work.
		for(Unit unit:new ArrayList<Unit>(getItemsByCategory(DATA_MODEL_CATEGORY.UNKNOWN))){
			
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
			 
			if(unit.getBaseUnit() == getUnknownUnit() || unit.getFundamentalUnitTypesDimension().containsKey(UNIT_TYPE.UNKNOWN))	
				addUnit(unit);//Readd unit so that it is recategorized as unknown in the data model.
		}
	}
	
	///
	public static DATA_MODEL_CATEGORY getDataModelCategory(Unit unit){
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
			
			if(type == UNIT_TYPE.UNKNOWN) //If still unknown, then use component unit.
				type = getUnit(unit.getComponentUnitsDimension().keySet().iterator().next()).getType();
		}
		
		return type;
	}
	
	///
	public void setUnitManagerRef(UnitManager unitManager){
		this.unitManagerRef = unitManager;
	}
}
