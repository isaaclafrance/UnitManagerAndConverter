package com.isaacapps.unitconverterapp.models.unitmanager.datamodels;

import java.util.*;

import com.isaacapps.unitconverterapp.models.Unit;

//TODO: Use a more efficient data structure
//Classification Map of Unit Groups Based on Their Unit Systems and Categories
public class UnitsClassifierDataModel {
	private Map<String, Map<String,ArrayList<String>>> unitsClassificationHierarchy;	

	///
	public UnitsClassifierDataModel(){
		unitsClassificationHierarchy = new HashMap<String, Map<String,ArrayList<String>>>();
	}
		
	///
	public void addToHierarchy(Unit unit){
		boolean doesUnitSystemExist;
		
		if(unitsClassificationHierarchy.containsKey(unit.getUnitSystem())){
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
			unitsClassificationHierarchy.put(unitSystem, new HashMap<String, ArrayList<String>>());
		}
		
		if(unitsClassificationHierarchy.get(unitSystem).containsKey(unit.getCategory())){
			doesUnitCategoryExist = true;
		}
		else{
			doesUnitCategoryExist = false;
		}
		
		addUnitToUnitCategory(unit, unitSystem, unit.getCategory(), doesUnitCategoryExist);		
	}
	private void addUnitToUnitCategory(Unit unit, String unitSystem, String category, boolean doesUnitCategoryExist){
		
		if(!doesUnitCategoryExist){
			unitsClassificationHierarchy.get(unitSystem).put(category, new ArrayList<String>());
		}
		
		unitsClassificationHierarchy.get(unitSystem).get(category).add(unit.getName());
	}
	
	public void removeFromHierarchy(Unit unit){
		if(unitsClassificationHierarchy.containsKey(unit.getUnitSystem())){
			if(unitsClassificationHierarchy.get(unit.getUnitSystem()).containsKey(unit.getCategory())){
				unitsClassificationHierarchy.get(unit.getUnitSystem()).get(unit.getCategory()).remove(unit.getName());
				if(unitsClassificationHierarchy.get(unit.getUnitSystem()).get(unit.getCategory()).size() == 0){
					unitsClassificationHierarchy.get(unit.getUnitSystem()).remove(unit.getCategory());
				}
			}
			if(unitsClassificationHierarchy.get(unit.getUnitSystem()).size() == 0){
				unitsClassificationHierarchy.remove(unit.getUnitSystem());
			}
		}
	}
	
	///
	public boolean containsCategoryInUnitSystem(String unitSystem, String category){
		if(!unitsClassificationHierarchy.containsKey(unitSystem))
			return false;
		
		return unitsClassificationHierarchy.get(unitSystem).containsKey(category);
	}
	
	///
	public ArrayList<String> getUnitNamesByUnitSystem(String unitSystem){
		if(!unitsClassificationHierarchy.containsKey(unitSystem))
			return new ArrayList<String>();
		
		ArrayList<String> allUnitNames = new ArrayList<String>();
		for(String category:unitsClassificationHierarchy.get(unitSystem).keySet())
			allUnitNames.addAll(getUnitNamesByUnitSystemNCategory(unitSystem, category));
		
		return allUnitNames;
	}	
	public ArrayList<String> getUnitNamesByUnitSystemNCategory(String unitSystem, String category){
		if(!unitsClassificationHierarchy.containsKey(unitSystem))
			return new ArrayList<String>();
		
		if(!unitsClassificationHierarchy.get(unitSystem).containsKey(category))
			return new ArrayList<String>();
		
		return unitsClassificationHierarchy.get(unitSystem).get(category);
	}
	
	public ArrayList<String> getAllUnitSystems(){		
		return new ArrayList<String>(unitsClassificationHierarchy.keySet());
	}	
	public ArrayList<String> getCategoriesInUnitSystem(String unitSystem){	
		if(!unitsClassificationHierarchy.containsKey(unitSystem))
			return new ArrayList<String>();
		
		return new ArrayList<String>(unitsClassificationHierarchy.get(unitSystem).keySet());
	}
}
