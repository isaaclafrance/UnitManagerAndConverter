package com.isaacapps.unitconverterapp.models.unitmanager.datamodels;

import java.util.ArrayList;
import java.util.Collection;

import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager.DATA_MODEL_CATEGORY;

public class PrefixesDataModel extends AbstractDataModelWithDualKeyNCategory<String, Double, DATA_MODEL_CATEGORY> {
	private UnitManager unitManagerContext;
		
	///
	public PrefixesDataModel(){
		super(true, true, true);
	}

	///Modify Content
	public void addCorePrefix(String prefixName, String abbreviation, double prefixValue){
		addItem(DATA_MODEL_CATEGORY.CORE, prefixName.toLowerCase(), abbreviation, prefixValue);
	}	
	public void addDynamicPrefix(String prefixName, String abbreviation, double prefixValue){
		addItem(DATA_MODEL_CATEGORY.DYNAMIC, prefixName.toLowerCase(), abbreviation, prefixValue);
	}
	
	
	public void removePrefix(String prefixName){
		removeItemByKey(prefixName);
	}
	public void removeAllCorePrefixes(){
		removeCategory(DATA_MODEL_CATEGORY.CORE);
	}
	public void removeAllDynamicPrefixes(){
		removeCategory(DATA_MODEL_CATEGORY.DYNAMIC);
	}
	
	///Retrieve Prefixes
	public double getPrefixValue(String prefixName){
		Double prefixValue = getFirstItemByAnyKey(prefixName.toLowerCase());
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
		if(prefixFullName != null){
			return prefixFullName;
		}else{
			return "";
		}
	}
	
	public Collection<String> getAllPrefixFullNames(){
		return getAllKey1s();
	}	
	public Collection<String> getAllPrefixAbbreviations(){
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
			/*If the constrainBasedOnValidUnitNames constraint is set to true and the unit manager is not null, then select prefixes such that when they are removed the remaining text is a valid unit name.
			 *A prefix and unit match is valid only if prefix and unit are BOTH abbreviations or BOTH full names -- must account for edge cases where full name is the same as the abbreviation.   
			 *Otherwise simply select prefixes that can found at the beginning of the specified name */
			String unitName = name.replaceFirst(prefix, "");
			boolean prefixFoundAtBeginning = name.indexOf(prefix) == 0,
					unitNameIsValid = unitManagerContext != null && unitManagerContext.getUnitsDataModel().containsUnit(unitName),
					prefixAndUnitAreSameKindOfName = isKey1(prefix) && unitManagerContext.getUnitsDataModel().isKey1(unitName) 
		             				|| isKey2(prefix) && unitManagerContext.getUnitsDataModel().isKey2(unitName)
		             				|| unitManagerContext.getUnitsDataModel().isKey1(unitName) && unitManagerContext.getUnitsDataModel().isKey2(unitName);
			
			if( prefixFoundAtBeginning
			    && (constrainBasedOnValidUnitNames && unitNameIsValid && prefixAndUnitAreSameKindOfName
			         || !constrainBasedOnValidUnitNames) )
			{	
				//Make sure prefix full name is first item in the array and the abbreviation is the second item.
				prefixMatches.add(new String[]{unitManagerContext.getPrefixesDataModel().isKey2(prefix)?unitManagerContext.getPrefixesDataModel().getKey1FromKey2(prefix):prefix,
									           unitManagerContext.getPrefixesDataModel().isKey2(prefix)?prefix:unitManagerContext.getPrefixesDataModel().getKey2FromKey1(prefix)});
			}
		}
		
		return prefixMatches;
	}
	
	///
	public DATA_MODEL_CATEGORY getModelDataType(String prefixName){
		return getCategoryOfKey(prefixName);
	}
	
	///
	public void setUnitManagerContext(UnitManager unitManager){
		this.unitManagerContext = unitManager;
	}
}

