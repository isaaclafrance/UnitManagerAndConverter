package com.isaacapps.unitconverterapp.models.unitmanager.datamodels;

import java.util.ArrayList;

import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager.DATA_MODEL_CATEGORY;

public class PrefixesDataModel extends AbstractDataModelWithDualKeyNCategory<String, Double, DATA_MODEL_CATEGORY> {
	private UnitManager unitManagerRef;
	
	///
	public PrefixesDataModel(){
		super(true);
	}

	///Modify Content
	public void addCorePrefix(String prefixName, String abbreviation, double prefixValue){
		addItem(DATA_MODEL_CATEGORY.CORE, prefixName.toLowerCase(), abbreviation, prefixValue, true);
	}	
	public void addDynamicPrefix(String prefixName, String abbreviation, double prefixValue){
		addItem(DATA_MODEL_CATEGORY.DYNAMIC, prefixName.toLowerCase(), abbreviation, prefixValue, true);
	}
	
	
	public void removePrefix(String prefixName){
		removeItemByAnyKey(prefixName);
	}
	public void removeAllCorePrefixes(){
		removeCategory(DATA_MODEL_CATEGORY.CORE);
	}
	public void removeAllDynamicPrefixes(){
		removeCategory(DATA_MODEL_CATEGORY.DYNAMIC);
	}
	
	///Retrieve Prefixes
	public double getPrefixValue(String prefixName){
		Double prefixValue = getItemByAnyKey(prefixName.toLowerCase());
			if(prefixValue != null){
				return prefixValue;
			}else{
				return 0.0;
			}
	}	
	public String getPrefixAbbreviation(String prefixFullName){
		String prefixAbbreviation = getKey2FromKey1(prefixFullName.toLowerCase());
		if(prefixAbbreviation != null){
			return prefixAbbreviation;
		}else{
			return "";
		}
	}
	public String getPrefixFullName(String prefixAbbreviation){
		String prefixFullName = getKey1FromKey2(prefixAbbreviation.toLowerCase());
		if(prefixAbbreviation != null){
			return prefixFullName;
		}else{
			return "";
		}
	}
	
	public ArrayList<String> getAllPrefixFullNames(){
		return getAllKey1s();
	}	
	public ArrayList<String> getAllPrefixAbbreviations(){
		return getAllKey2s();
	}
	
	///Query for Existence of Prefixes
	public boolean containsPrefix(String name){
		return containsKey(name);
	}
	public boolean containsPrefixValue(double value){
		return containsItem(value);
	}
	public boolean hasCorePrefxes(){
		return !getItemsByCategory(DATA_MODEL_CATEGORY.CORE).isEmpty();
	}
	public boolean hasDynamicPrefxes(){
		return !getItemsByCategory(DATA_MODEL_CATEGORY.DYNAMIC).isEmpty();
	}
	
	///
	public ArrayList<String[]> getPrefixMatches(String name, boolean constrainBasedOnValidUnitNames){
		//Picks out the prefix full name and abbreviation combination that can be found in the name. 
	
		ArrayList<String[]> prefixMatches = new ArrayList<String[]>();
		ArrayList<String> allPrefixFullNameAndAbbreviations = new ArrayList<String>(getAllPrefixFullNames());
		allPrefixFullNameAndAbbreviations.addAll(getAllPrefixAbbreviations());
		
		for(String prefix:allPrefixFullNameAndAbbreviations){
			 //If the constrainBasedOnValidUnitNames constraint is set to true and the unit manager is not null, then select prefixes such that when they are removed the remaining text is a valid unit name. 
			 //Otherwise simply select prefixes that can found at the begind of the specifed name
			if(  name.indexOf(prefix) == 0 
			     &&( constrainBasedOnValidUnitNames && unitManagerRef != null && unitManagerRef.getUnitsDataModel().containsUnit(name.replaceFirst(prefix, ""))
			        || !constrainBasedOnValidUnitNames )){
				
				//Make sure prefix full name is first item in the array and the abbreviation is the second item.
				prefixMatches.add(new String[]{unitManagerRef.getPrefixesDataModel().isKey2(prefix)?unitManagerRef.getPrefixesDataModel().getKey1FromKey2(prefix):prefix,
									           unitManagerRef.getPrefixesDataModel().isKey2(prefix)?prefix:unitManagerRef.getPrefixesDataModel().getKey2FromKey1(prefix)});
			}
		}
		
		return prefixMatches;
	}
	
	///
	public DATA_MODEL_CATEGORY getModelDataType(String prefixName){
		return getCategoryOfKey(prefixName);
	}
	
	///
	public void setUnitManagerRef(UnitManager unitManager){
		this.unitManagerRef = unitManager;
	}
}

