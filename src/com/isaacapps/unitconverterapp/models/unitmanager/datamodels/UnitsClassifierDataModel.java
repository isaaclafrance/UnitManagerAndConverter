package com.isaacapps.unitconverterapp.models.unitmanager.datamodels;

import java.util.*;

import com.isaacapps.unitconverterapp.models.Unit;

//Classification Map of Unit Groups Based on Their Unit Systems and Categories
//In this modified and transposed implementation, Unit System is category, Units category is both key1 and key2.
public class UnitsClassifierDataModel extends AbstractDataModelWithDualKeyNCategory<String, Collection<String>, String>{

	///
	public UnitsClassifierDataModel(){
		super(true);
	}
		
	///
	public Collection<String> addToHierarchy(Unit unit){
		Collection<String> unitsGroup = getItem(unit.getUnitSystem(), unit.getCategory());
		if(unitsGroup!=null){
			unitsGroup.add(unit.getName()); 
		}
		else{
			//Save as a Set in order to ignore duplicate names
			return addItem(unit.getUnitSystem(), unit.getCategory(), unit.getCategory(), new HashSet<String>(Arrays.asList(unit.getName())), false);
		}
		return unitsGroup;
	}	
	
	///
	public boolean removeFromHierarchy(Unit unit){
		Collection<String> unitsGroup = getItem(unit.getUnitSystem(), unit.getCategory());
		if(unitsGroup != null && unitsGroup.remove(unit.getName())){
			if(unitsGroup.isEmpty())
				removeCategory(unit )
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
		return getKey1sbyCategory(unitSystem);
	}
	
	///
	public boolean containUnitSystem(String unitSystem, boolean fuzzyMatch){
		if(fuzzyMatch){
			for(String unitSystemCandidate:getAllAssignedCategories()){
				if(unitSystemCandidate.contains(unitSystem))
					return true;
			}
		}
		return containsCategory(unitSystem);
	}
	public boolean containsUnitCategoryInUnitSystem(String unitSystem, String unitCategory, boolean fuzzyMatch){
		if(fuzzyMatch){
			for( String unitCategoryCandidate:getKey1sbyCategory(unitSystem)){
				if(unitCategoryCandidate.contains(unitCategory))
					return true;
			}
		}
		return containsKeyInCategory(unitSystem, unitCategory);
	}
	public boolean containsUnitInUnitCategoryNUnitSystem(String unitSystem, String unitCategory, String unitName, boolean fuzzyMatch){
		if(fuzzyMatch){
			for( String unitNameCandidate:getItem(unitSystem, unitCategory)){
				if(unitNameCandidate.contains(unitName))
					return true;
			}
		}
		return getUnitNamesByUnitSystemNCategory(unitSystem, unitCategory).contains(unitName);
	}
}
