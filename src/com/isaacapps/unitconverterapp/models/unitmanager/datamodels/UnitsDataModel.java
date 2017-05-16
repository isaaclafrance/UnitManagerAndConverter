package com.isaacapps.unitconverterapp.models.unitmanager.datamodels;

import java.util.*;
import java.util.Map.*;

import com.isaacapps.unitconverterapp.models.*;
import com.isaacapps.unitconverterapp.models.unitmanager.*;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager.*;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel.*;

//Provides a centralized and encapsulated hub for storing, retrieving, and updating units
public class UnitsDataModel extends AbstractDataModelWithDualKeyNCategory<String, Unit, DATA_MODEL_CATEGORY> {	
	private UnitManager unitManagerContext; 
	
	///
	public UnitsDataModel(){
		super(true, true, true);
	}
	
	///Modify Data Model Content
	public void addUnits(List<Unit> units){
		for(Unit unit:units)
			addUnit(unit);
	}
	public Unit addUnit(Unit unit){	
		/*Unit needs to be put in data structure first (if it does not violate the data structure restrictions) even if its unknown because referential execution paths
		 *in the setUnitManagerContext injector will require the unit to be present in an unknown classification, even if later on it may be reclassified to something else
		 *Fortunately adding it before hand in the data structure is not resource intensive. */
		if(!containsItem(unit) 
				&& addUnitToDataModels(unit)){

			unit.setUnitManagerContext(this.unitManagerContext);
			
			/*Now assess the characteristics(type, dimension, etc) of the unit after attempting to associate it with unit manager. 
			 * If not unknown, fully incorporate it (if it does not violate data structure restrictions) with data models again. */	
			if(getDataModelCategory(unit) != DATA_MODEL_CATEGORY.UNKNOWN
					&& addUnitToDataModels(unit)){
				
				Collection<Unit> allUnitMatches = getUnitsByFundamentalUnitTypeDimension(unit.getFundamentalUnitTypesDimension());
					
				boolean unitIsOneOfAKind = allUnitMatches.isEmpty();  
				if(!unit.isBaseUnit() 
				   && !unit.isCoreUnit()
				   && (unitIsOneOfAKind || unitManagerContext.getFundamentalUnitsDataModel().containsUnitName(unit.getName())))
				{
					//Make the unit be a base unit if its the only one of its kind in the unit manager or if it is fundamental unit and not already a core base unit 
					unit.setBaseUnit(unit); 
				}
									
				//Attempt to see if this unit can be used to make other unknown units become known only if it has not been incorporated as a base unit already.
				//This is because base unit incorporation already checks for that.
				if(!incorporateBaseUnit(unit, allUnitMatches))
					updateAssociationsOfUnknownUnits();	
			}
		}
		
		return unit;
	}
	private boolean addUnitToDataModels(Unit unit){
		Unit addedUnit = addItem( getDataModelCategory(unit), unit.getName(), unit.getAbbreviation(), unit);
		if(addedUnit != null)
			unitManagerContext.getUnitsClassifierDataModel().addToHierarchy(addedUnit, false);
		
		return addedUnit != null;
	}
	
	public Unit removeDynamicUnit(String unitName){
		/*Removes unit from the classification hierarchy. Removes unit from units data model. 
		 *Updates the base units of dependent units to unknown. Also removes a reference to this unit manager from the unit. */
		Unit removedUnit = getFirstItemByAnyKey(unitName.toLowerCase());
		removeItemByKey(unitName);
		
		unitManagerContext.getUnitsClassifierDataModel().removeFromHierarchy(removedUnit);
		
		if(removedUnit.isBaseUnit()){
			for(Unit dependentUnit: removedUnit.getConversionsOfDescendents().keySet()){
				dependentUnit.setBaseUnit(getUnknownUnit());
			}
			removedUnit.clearConversions();
		}
		else{
			removedUnit.getBaseUnit().getConversionsOfDescendents().remove(removedUnit);
		}
		
		removedUnit.setUnitManagerContext(null);
		updateFundamentalUnitsDimensionOfKnownUnits();	
		
		//Remove any conversion favorites that may be associated with this unit as well update the significance rankings
		unitManagerContext.getConversionFavoritesDataModel().removeConversionByUnit(removedUnit);

		return removedUnit;
	}
	public Unit removeDynamicUnit(Unit unit){
		return removeDynamicUnit(unit.getName());
	}
	public void removeAllDynamicUnits(){
		//Also removes all UNKNOWN units since they would have been classified as DYNAMIC if some of their properties were known.
		
		for(Unit unitToBeRemoved:getItemsByCategory(DATA_MODEL_CATEGORY.DYNAMIC)){
			unitManagerContext.getUnitsClassifierDataModel().removeFromHierarchy(unitToBeRemoved);
			unitManagerContext.getConversionFavoritesDataModel().removeConversionByUnit(unitToBeRemoved);
		}
			
		for(Unit unitToBeRemoved:getItemsByCategory(DATA_MODEL_CATEGORY.UNKNOWN)){ 
			unitManagerContext.getUnitsClassifierDataModel().removeFromHierarchy(unitToBeRemoved);
			//Unknown units do not have conversions therefore there is no need to remove them from the conversion favorites
		}
	
		removeCategory(DATA_MODEL_CATEGORY.DYNAMIC);
		removeCategory(DATA_MODEL_CATEGORY.UNKNOWN);
	}
	
	///Retrieve Data Model Content
	public Unit getUnit(String unitName){         
		return getUnit(unitName, true);
	}
	public Unit getUnit(String unitName, boolean createMissingUnits){
		unitName = unitName.toLowerCase();

		Unit unit = getFirstItemByAnyKey(unitName); //Search by full unit name and abbreviations
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
		
		List<String[]> prefixMatches = unitManagerContext.getPrefixesDataModel().getPrefixMatches(unitName, true);
		if(!prefixMatches.isEmpty()){
			return addUnit(new PrefixedUnit(prefixMatches.get(0)[0], prefixMatches.get(0)[1]
					 				, unitManagerContext.getPrefixesDataModel().getPrefixValue(prefixMatches.get(0)[1])
					 				, getUnit(unitName.contains(prefixMatches.get(0)[0])? 
					 						   unitName.replace(prefixMatches.get(0)[0], "")
					 						  :unitName.replace(prefixMatches.get(0)[1], "")),false));
		}
		else{
			return getUnknownUnit();
		}	
	}	
	public Unit getUnknownUnit(){
		return getFirstItemByAnyKey(Unit.UNKNOWN_UNIT_NAME);
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
		
		if(unit.getUnitManagerContext() == this.unitManagerContext){
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
	
		for(String unitName:unitManagerContext.getUnitsClassifierDataModel().getUnitNamesByUnitSystem(unitSystem))
			unitMatches.add(getUnit(unitName, false));
		
		return unitMatches;
	}
	public Collection<Unit> getCorrespondingUnitsWithUnitSystem(Unit sourceUnit, String targetUnitSystemString){
		List<Unit> correspondingUnits = new ArrayList<Unit>();		
		
		if(sourceUnit.getUnitManagerContext() == this.unitManagerContext){	
			//If unit is under  only one unit first use the unit classifier data model to find corresponding unit target unit system, otherwise perform direct dimension comparison.
			if(!sourceUnit.getUnitSystem().contains(targetUnitSystemString) && !sourceUnit.getUnitSystem().contains(Unit.UNIT_SYSTEM_DELIMITER) 
				&& sourceUnit.getComponentUnitsDimension().size() == 1)
			{
				Collection<String> matchCandidateNames = unitManagerContext.getUnitsClassifierDataModel()
						                                             .getUnitNamesByUnitSystemNCategory(targetUnitSystemString, sourceUnit.getCategory());
				for(String unitName:matchCandidateNames){
					correspondingUnits.add(getFirstItemByAnyKey(unitName));
				}
				
				Collection<Unit> matchCandidateUnits = new ArrayList<Unit>();
				if(correspondingUnits.isEmpty())
					matchCandidateUnits = getUnitsByComponentUnitsDimension(sourceUnit.getComponentUnitsDimension(), true);
				
				for(Unit candidate:matchCandidateUnits){
					if(sourceUnit.getUnitSystem().equalsIgnoreCase(candidate.getUnitSystem()))
						correspondingUnits.add(candidate);
				}
			}else{
				if(UnitsDataModel.getDataModelCategory(sourceUnit) != DATA_MODEL_CATEGORY.UNKNOWN){
					//Find replacement component units with corresponding unit dimension that are under the target unit system.
					Unit replacementUnit;
					Map<String, Double> properComponentUnitDimension = new HashMap<String, Double>();
					
					for(Entry<String, Double> componentUnitEntry:sourceUnit.getComponentUnitsDimension().entrySet()){				
						replacementUnit = getCorrespondingUnitsWithUnitSystem(getUnit(componentUnitEntry.getKey(), false), targetUnitSystemString).iterator().next();
						properComponentUnitDimension.put(replacementUnit.getName(), componentUnitEntry.getValue());
					}
					
					//See if unit manager context already contains unit with proper dimension and return that unit. Otherwise return a new unit with proper dimensions and add this unit manager.
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
			
			//Sort the candidates list by the significance of the provided name with respect to their full name and abbreviated name
			@Override
			public int compare(Unit lhsUnit, Unit rhsUnit) {
				Double lhsUnitFullNameSignificance = (double)providedName.length()/lhsUnit.getName().length();
				Double lhsUnitAbbreviationSignificance = (double)providedName.length()/lhsUnit.getAbbreviation().length();
				
				Double rhsUnitFullNameSignificance = (double)providedName.length()/lhsUnit.getName().length();
				Double rhsUnitAbbreviationSignificance = (double)providedName.length()/lhsUnit.getAbbreviation().length();
				
				//Only select the abbreviation significance if the length of provided name is less than the length of the abbreviation, otherwise the 
				//Full Name significance is utilized. This is to ensure that abbreviations take precedence when the length of the provided name is similar to the length of the available abbreviations
				Double preferedLhsSignificance = (lhsUnitAbbreviationSignificance<1)?lhsUnitAbbreviationSignificance:lhsUnitFullNameSignificance;
				Double preferedRhsSignificance = (rhsUnitAbbreviationSignificance<1)?rhsUnitAbbreviationSignificance:rhsUnitFullNameSignificance;
				
				return -preferedLhsSignificance.compareTo(preferedRhsSignificance); //need to take negative to order from greatest to least.
			}});		

		for(String unitName:getAllKeys()){
			if(providedName.contains(unitName))
				unitCandidates.add(getFirstItemByAnyKey(unitName));
		}
	
		return unitCandidates;
	}
		
	///Query for Existence of Content
	public boolean containsUnit(String unitName){
		//Name can match to a unit full name or an abbreviation
		return containsKey(unitName.toLowerCase());
	}
	public boolean containsUnit(Unit unit){
		return containsItem(unit);
	}
	
	///Update Data Model Content
	public void updateAssociationsOfUnknownUnits(){		
		updateAssociationsOfUnknownUnits(new ArrayList<Unit>(), getItemsByCategory(DATA_MODEL_CATEGORY.UNKNOWN));
	}
	private void updateAssociationsOfUnknownUnits(Collection<Unit> unitsNoLongerUnknown, Collection<Unit> currentUnknownUnits){
		for(Unit unit:currentUnknownUnits){	
			if(unit != getUnknownUnit() && unit.setAutomaticBaseUnit()){
				unitsNoLongerUnknown.add(unit);
				//Reclassify this unit in the data structure with its newly acquired category
				addUnitToDataModels(unit);
			}
		}
		
		//Recursively accounts for the edge case where identifiability of one unit is dependent on the identifiability of another unit in the same unknown set.
		Collection<Unit> unknownUnitsToBeReAnalyzed = unitsNoLongerUnknown.isEmpty() || !unitsNoLongerUnknown.containsAll(currentUnknownUnits)
													  ? Collections.EMPTY_LIST : unknownUnitsToBeReAnalyzed(currentUnknownUnits, unitsNoLongerUnknown);
		if(!unknownUnitsToBeReAnalyzed.isEmpty())
			updateAssociationsOfUnknownUnits(unitsNoLongerUnknown, unknownUnitsToBeReAnalyzed);
	}
	private Collection<Unit> unknownUnitsToBeReAnalyzed(Collection<Unit> currentUnknownUnits, Collection<Unit> unitsNoLongerUnknown){
		Set<String> namesOfUnitsNoLongerUnknown = new HashSet<String>();
		for(Unit unit:unitsNoLongerUnknown){
			namesOfUnitsNoLongerUnknown.add(unit.getName());
		}
		
		//
		for(Iterator<Unit> currentUnknownUnitsIterator = currentUnknownUnits.iterator(); currentUnknownUnitsIterator.hasNext();){
			if(!unknownUnitShouldBeReAnalyzed(currentUnknownUnitsIterator.next(), namesOfUnitsNoLongerUnknown))
				currentUnknownUnitsIterator.remove();
		}
		
		return currentUnknownUnits;
	}
	private boolean unknownUnitShouldBeReAnalyzed(Unit currentUnknownUnit, Set<String> namesOfUnitsNoLongerUnknown){
		if(namesOfUnitsNoLongerUnknown.contains(currentUnknownUnit.getBaseUnit().getName()))	
			return true;
		
		for(String componentUnitName:currentUnknownUnit.getComponentUnitsDimension().keySet()){
			if(namesOfUnitsNoLongerUnknown.contains(componentUnitName))
				return true;
		}
		return false;
	}

	public void updateFundamentalUnitsDimensionOfKnownUnits(){
		for(Unit unit:getItemsByCategory(DATA_MODEL_CATEGORY.DYNAMIC)){
			unit.setAutomaticUnitSystem();
			unit.setAutomaticUnitTypeNFundmtTypesDim();
			 
			if(getDataModelCategory(unit) == DATA_MODEL_CATEGORY.UNKNOWN)	
				addUnitToDataModels(unit);//Readd unit so that it is recategorized as unknown in the data model.
		}
	}
	
	private boolean incorporateBaseUnit(Unit unit, Collection<Unit> allUnitMatches){
		//Assumption is that when the allUnitsMatches is empty then it simply
		if(unit.isBaseUnit()){ 			
			//Assumption is that when the 'allUnitsMatches' collection is empty then it simply was not provided by the invoker rather than there not being any matches, therefore try to retrieve those matches
			for(Unit unitMatch:(allUnitMatches.isEmpty()
					                 ?getUnitsByFundamentalUnitTypeDimension(unit.getFundamentalUnitTypesDimension())
					                		 :allUnitMatches)){ 
				/*If the core units states are compatible (Dynamic units can not replace core units)
				 * , then set the existing base units and its dependent units to be instead be associated with this base unit.
				 * Otherwise, assign the existing base unit to this unit. */
				boolean existingBaseUnitAbleToBeSetAsNonBase = unitMatch.isBaseUnit() 
						                                  && unitMatch.isCoreUnit() == unit.isCoreUnit()
												          && !unitMatch.getName().equalsIgnoreCase(unit.getName());
				if(existingBaseUnitAbleToBeSetAsNonBase){		
					/*For identity purposes, the base units include themselves as their dependents. As consequence, inorder to prevent concurrent modification exceptions
					 *the 'conversionOfDescendents' need to be reinitialized in a new list since it can cleared in setBaseUnit when the base unit switches to no longer being a base unit.*/
					Collection<Unit> dependentUnits = new ArrayList(unitMatch.getConversionsOfDescendents().keySet()); 
					
					for(Unit dependentUnit:dependentUnits)
						dependentUnit.setBaseUnit(unit);		 					
				}
				else{
					unit.setBaseUnit(unitMatch);
				}
				break; //Ideally if the data model is consistent, there should have only been one valid existing base unit present.
			}	
			
			//Determines if this unit can be associated with existing units that currently do not have base units.
			updateAssociationsOfUnknownUnits();
			
			return true;
		}
		return false;
	}
	public boolean incorporateBaseUnit(Unit unit){
		return incorporateBaseUnit(unit, Collections.EMPTY_LIST);
	}
	
	///
	public Unit getBaseUnitMatch(Unit unit){
	    /*If the unit is not unknown and the fundamental dimension of this unit and its existing base unit match, then that base unit will be returned. Otherwise, 
	     *tries to match the fundamental unit with one unit in the data model. Only one unit match will ever be needed,
	     *since if the data model is logically consistent all similar units will have the same base unit */ 
		
		if(unit.equalsFundamentalUnitsDimension(unit.getBaseUnit().getFundamentalUnitTypesDimension())){
			return unit.getBaseUnit();
		}
		else{
			Collection<Unit> unitMatches = getUnitsWithMatchingFundamentalUnitDimension(unit);			
			if(unitMatches.size() > 0){
				for(Unit unitMatch:unitMatches){
					if(unitMatch.isBaseUnit() && !unitMatch.getName().equalsIgnoreCase(unit.getName()))
						return unitMatch;
				}
			}	
			return getUnknownUnit();
		}
	}
	public Unit getReducedUnitMatch(Unit unit){
		//If result set is large enough sort from least to greatest by component units dimension size, then get the unit with the smallest dimension
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
			return unit;
		}
	}
	
	///
	public static DATA_MODEL_CATEGORY getDataModelCategory(Unit unit){
		if(unit.getBaseUnit().getType() == UNIT_TYPE.UNKNOWN 
				|| unit.getFundamentalUnitTypesDimension().containsKey(UNIT_TYPE.UNKNOWN)
				|| unit.getType() == UNIT_TYPE.UNKNOWN){
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
		if(unit.getComponentUnitsDimension().size()>1){
			return UNIT_TYPE.DERIVED_MULTI_UNIT;
		}
		else if(unit.getComponentUnitsDimension().size() == 1 
				&&(Math.abs(unit.getComponentUnitsDimension().entrySet().iterator().next().getValue()) > 1 
					       || unit.getComponentUnitsDimension().containsValue(-1)
					       || unit.getComponentUnitsDimension().containsValue(0))){
			return UNIT_TYPE.DERIVED_SINGLE_UNIT;
		}
		else if(unit.getBaseUnit() != null){
			UNIT_TYPE type = unitManagerContext.getFundamentalUnitsDataModel()
					                           .getUnitTypeByUnitSystemNUnitName(unit.getBaseUnit().getUnitSystem()
					                        		                             , unit.getBaseUnit().getName());
			
			if(type == null){
				type = unit.getBaseUnit().getType();
				if(type != UNIT_TYPE.UNKNOWN){
					return type;
				}
				else{ //If still unknown, then try using the singular component unit.
					return getUnit(unit.getComponentUnitsDimension().keySet().iterator().next()).getType();
				}
			}
			return type;
		}
		return UNIT_TYPE.UNKNOWN;
	}
	
	///
	public void setUnitManagerContext(UnitManager unitManager){
		this.unitManagerContext = unitManager;
	}
}
