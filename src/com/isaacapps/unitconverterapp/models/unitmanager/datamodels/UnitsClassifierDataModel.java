package com.isaacapps.unitconverterapp.models.unitmanager.datamodels;

import java.util.*;

import com.isaacapps.unitconverterapp.models.Unit;

//Classification Map of Unit Groups Based on Their Unit Systems and Categories
/*In this modified implementation of abstract data model, Unit System specifies the category, Unit Name specifies key1 and the Unit Category specifies key2.
 *And the item is the groups unit */
public class UnitsClassifierDataModel extends AbstractDataModelWithDualKeyNCategory<String, Collection<String>, String>{

	///
	public UnitsClassifierDataModel(){
		/*Do not attempt to hard set removedDuplicateItems parameter to true since in principle two different units systems 
		 *can have the same units names group for the same unit category. However, the add to hierarchy method does have the
		 *option to selectively make sure that a certain unit name does not exist in other units groups.
		 *Within one unit system, a unit name can not be associated with more than one unit category, which is ensured by setting the 
		 *keysMustHaveBijectiveRelation to true.
		 */
		super(true, false, true);
	}
		
	///
	public Collection<String> addToHierarchy(Unit unit, boolean removeFromOtherUnitNamesGroups){
		Collection<String> currentUnitNamesGroup = getItem(unit.getUnitSystem(), unit.getCategory());
	
		if(currentUnitNamesGroup != null){
			currentUnitNamesGroup.add(unit.getName());
			
			if(removeFromOtherUnitNamesGroups){
				Collection<Collection<String>> otherUnitNamesGroups = getItemsByAnyKey(unit.getName());
				otherUnitNamesGroups.remove(currentUnitNamesGroup);
				
				removeUnitNameFromOtherUnitGroups(unit.getName(), getItemsByAnyKey(unit.getName()));
				for(Collection<String> unitGroupWithRemovedUnitName:otherUnitNamesGroups){ 
					if(unitGroupWithRemovedUnitName.isEmpty())
						removeItem(unitGroupWithRemovedUnitName);
				}
			}
		}
		else{
			//Save as a Set collection  in order to ignore duplicate names in same unit group
			return addItem(unit.getUnitSystem(), unit.getName(), unit.getCategory(), new HashSet<String>(Arrays.asList(unit.getName())));
		}
		return currentUnitNamesGroup;
	}	
	private void removeUnitNameFromOtherUnitGroups(String unitName, Collection<Collection<String>> otherUnitNamesGroups){
		for(Collection<String> unitNamesGroup:otherUnitNamesGroups)
			unitNamesGroup.remove(unitName);
	}
	
	///
	public boolean removeFromHierarchy(Unit unit){
		Collection<String> unitsGroup = getItem(unit.getUnitSystem(), unit.getCategory());
		
		removeKeyRelations(unit.getName());
		
		if(unitsGroup != null && unitsGroup.remove(unit.getName()) && unitsGroup.isEmpty()){
			removeItemFromCategoryByKey(unit.getUnitSystem(), unit.getCategory());
			
			if(getUnitCategoriesInUnitSystem(unit.getUnitSystem()).isEmpty())
				removeCategory(unit.getUnitSystem());
			
			return true;
		}
		return false;
	}
	public void clearHierarchy(){
		removeAllItems();
	}
	
	///
	public Collection<String> getUnitNamesByUnitSystem(String unitSystem){
		Collection<String> allUnitNames = new HashSet<String>();
		
		for(String unitCategory:getUnitCategoriesInUnitSystem(unitSystem))
			allUnitNames.addAll(getUnitNamesByUnitSystemNCategory(unitSystem, unitCategory));
		
		return allUnitNames;
	}	
	public Collection<String> getUnitNamesByUnitSystemNCategory(String unitSystem, String unitCategory){
		if(!containsUnitCategoryInUnitSystem(unitSystem, unitCategory, false))
			return new HashSet<String>();
		
		return getItem(unitSystem, unitCategory);
	}
	
	public Collection<String> getAllUnitSystems(){		
		return getAllAssignedCategories();
	}	
	public Collection<String> getUnitCategoriesInUnitSystem(String unitSystem){	
		return getKey2sByCategory(unitSystem);
	}
	
	public Collection<String> getUnitGroupsOfUnit(String unitName){
		/*Since the data structure enforces a relations between unitname and category, the unit name can be used to return unit name groups that share the same category.
		 *If the unit name is found in multiple unit systems then multiple groups will be returned*/ 
		return getFirstItemByAnyKey(unitName);
	}
	
	///
	public boolean containUnitSystem(String unitSystem, boolean fuzzyMatch){
		if(fuzzyMatch){ // Fuzzy match such that specified unitSystem is at least a token in any existing unit system
			for(String unitSystemCandidate:getAllAssignedCategories()){
				if(unitSystemCandidate.contains(unitSystem))
					return true;
			}
			return false;
		}
		return containsCategory(unitSystem);
	}
	public boolean containsUnitCategoryInUnitSystem(String unitSystem, String unitCategory, boolean fuzzyMatch){
		if(fuzzyMatch){ // Fuzzy match such that the specified unitName is at least a token in any unit name in a unit group
			for( String unitCategoryCandidate:getKey2sByCategory(unitSystem)){
				if(unitCategoryCandidate.contains(unitCategory))
					return true;
			}
			return false;
		}
		return containsKeyInCategory(unitSystem, unitCategory);
	}
	public boolean containsUnitNameInUnitCategoryFromUnitSystem(String unitSystem, String unitCategory, String unitName, boolean fuzzyMatch){
		if(fuzzyMatch){ // Fuzzy match such that the specified unitName is at least a token in any unit name in a unit group
			for( String unitNameCandidate:getItem(unitSystem, unitCategory)){
				if(unitNameCandidate.contains(unitName))
					return true;
			}
			return false;
		}
		return getUnitNamesByUnitSystemNCategory(unitSystem, unitCategory).contains(unitName);
	}
}
