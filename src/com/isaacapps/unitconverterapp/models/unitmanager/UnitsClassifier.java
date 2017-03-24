package com.isaacapps.unitconverterapp.models.unitmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.isaacapps.unitconverterapp.models.Unit;

//TODO: Use a more efficient data structure
//Classification Map of Unit Groups Based on Their Unit Systems and Categories
public class UnitsClassifier {
	private DataMaps unitManagerDataMaps;	

	///
	UnitsClassifier(){
	}
		
	///
	public void addToHierarchy(Unit unit){
		boolean doesUnitSystemExist;
		
		if(unitManagerDataMaps.getUnitsClassificationHierarchy().containsKey(unit.getUnitSystem())){
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
			unitManagerDataMaps.getUnitsClassificationHierarchy().put(unitSystem, new HashMap<String, ArrayList<String>>());
		}
		
		if(unitManagerDataMaps.getUnitsClassificationHierarchy().get(unitSystem).containsKey(unit.getCategory())){
			doesUnitCategoryExist = true;
		}
		else{
			doesUnitCategoryExist = false;
		}
		
		addUnitToUnitCategory(unit, unitSystem, unit.getCategory(), doesUnitCategoryExist);		
	}
	private void addUnitToUnitCategory(Unit unit, String unitSystem, String category, boolean doesUnitCategoryExist){
		
		if(!doesUnitCategoryExist){
			unitManagerDataMaps.getUnitsClassificationHierarchy().get(unitSystem).put(category, new ArrayList<String>());
		}
		
		unitManagerDataMaps.getUnitsClassificationHierarchy().get(unitSystem).get(category).add(unit.getName());
	}
	public void removeFromHierarchy(Unit unit){
		if(unitManagerDataMaps.getUnitsClassificationHierarchy().containsKey(unit.getUnitSystem())){
			if(unitManagerDataMaps.getUnitsClassificationHierarchy().get(unit.getUnitSystem()).containsKey(unit.getCategory())){
				unitManagerDataMaps.getUnitsClassificationHierarchy().get(unit.getUnitSystem()).get(unit.getCategory()).remove(unit.getName());
				if(unitManagerDataMaps.getUnitsClassificationHierarchy().get(unit.getUnitSystem()).get(unit.getCategory()).size() == 0){
					unitManagerDataMaps.getUnitsClassificationHierarchy().get(unit.getUnitSystem()).remove(unit.getCategory());
				}
			}
			if(unitManagerDataMaps.getUnitsClassificationHierarchy().get(unit.getUnitSystem()).size() == 0){
				unitManagerDataMaps.getUnitsClassificationHierarchy().remove(unit.getUnitSystem());
			}
		}
	}
	
	///
	public Map<String, Map<String, ArrayList<String>>> getHierarchy(){			
		return unitManagerDataMaps.getUnitsClassificationHierarchy();
	}
	
	///
	void setDataMaps(DataMaps unitManagerDataMaps){
		this.unitManagerDataMaps = unitManagerDataMaps;
	}
}
