package com.isaacapps.unitconverterapp.models.unitmanager.datamodels;

import java.util.*;

import com.isaacapps.unitconverterapp.models.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager.DATA_MODEL_CATEGORY;

public class ConversionFavoritesDataModel extends AbstractDataModelWithDualKeyNCategory<String, String, String>{
	private static  String[] DELIMITERS = new String[]{": ", " --> "};
	private Map<String, Integer> significanceRanks;
	
	///
	public ConversionFavoritesDataModel(){
		// Allowing for a non bijective relations between keys.
		// Key1 will be the source unit name and key2 will be the formatted conversion favorite and the item will be the target unit name
		// Therefore, according to existing abstract datamodel data structure,  many keys2 can point to the same key1 or item.
		super(false);
		significanceRanks = new HashMap<String, Integer>();
	}
	
	///
	public boolean addConversion(Unit sourceUnit, Unit targetUnit){
		if(sourceUnit.getUnitManagerRef() == targetUnit.getUnitManagerRef() && sourceUnit.equalsDimension(targetUnit) 
		   && UnitsDataModel.getDataModelCategory(sourceUnit) != DATA_MODEL_CATEGORY.UNKNOWN){
		   
			addItem( sourceUnit.getCategory(), sourceUnit.getName(), convertToFormattedConversion(sourceUnit, targetUnit), targetUnit.getName(), false);
			return true;
		}
		return false;
	}
	public boolean addConversion(String category, String sourceUnitName, String targetUnitName){
		addItem(category, sourceUnitName, convertToFormattedConversion(category, sourceUnitName, targetUnitName), targetUnitName, false);
		return true;
	}

	///
	public boolean removeConversionByUnitPair(Unit sourceUnit, Unit targetUnit){
		modifySignificanceRankOfMultipleConversions(sourceUnit, false);
		modifySignificanceRankOfMultipleConversions(targetUnit, false);
		return removeItemByAnyKey(convertToFormattedConversion(sourceUnit, targetUnit)) != null;
	}
	public boolean removeFormattedConversion(String formattedConversion){
		String sourceUnitName = getSourceUnitNameFromConversion(formattedConversion)
			   , targetUnitName = getTargetUnitNameFromConversion(formattedConversion)
			   , unitCategory = getUnitCategoryFromFormattedConversion(formattedConversion);
		
		if(removeItemByAnyKey(formattedConversion) != null){
			modifySignificanceRankOfMultipleConversions(unitCategory, false);
			//Add extra significance decrement to conversion that contains source and target unit.
			for(String favoriteConversion:getFormattedConversionsAssociatedWithCategory(unitCategory)){
				if(favoriteConversion.contains(sourceUnitName) || favoriteConversion.contains(targetUnitName))
					modifySignificanceRankOfConversion(favoriteConversion, false);
			}
			return true;
		}

		return false;
	}
	public boolean removeConversionByUnit(Unit unit){	
		boolean removed = removeItemByAnyKey(unit.getName()) != null //Removes by source unit name 
				          || removeItem(unit.getName()); // Remove by target unit name	
		if(removed){
			modifySignificanceRankOfMultipleConversions(unit.getCategory(), false);
			return true;
		}
		return false;
	}
	public boolean removedConversionByCategory(String unitCategory){
		return removeCategory(unitCategory);
	}
	
	///
	public boolean containsUnit(Unit unit){
		return containsKey(unit.getName()) || containsItem(unit.getName());
	}
	
	///
	public void modifySignificanceRankOfMultipleConversions(Unit unit, boolean increase){
		modifySignificanceRankOfMultipleConversions(getFormattedConversionsAssociatedWithUnit(unit), increase);
		modifySignificanceRankOfMultipleConversions(unit.getCategory(), increase);
	}
	public void modifySignificanceRankOfMultipleConversions(String category, boolean increase){
		modifySignificanceRankOfMultipleConversions(getFormattedConversionsAssociatedWithCategory(category), increase);
	}
	private void modifySignificanceRankOfMultipleConversions(List<String> formattedConversions, boolean increase){
		for(String formattedConversion:formattedConversions)
			modifySignificanceRankOfConversion(formattedConversion, increase);
	}
	public void modifySignificanceRankOfConversion(String formattedConversion, boolean increase){
		if(!significanceRanks.containsKey(formattedConversion))
			significanceRanks.put(formattedConversion, 0);
		if(significanceRanks.get(formattedConversion) < 0)
			significanceRanks.put(formattedConversion, significanceRanks.get(formattedConversion)+(increase?1:-1));
	}
	
	///
	public int getSignificanceRankOfConversion(String formattedConversion){
		return significanceRanks.get(formattedConversion);
	}
	
	///
	public List<String> getFormattedConversionsAssociatedWithUnit(Unit unit){
		ArrayList<String> matchingFormattedConversionFavorites = new ArrayList<String>();
		if(containsKey(unit.getName())){
			/*Since bijection is not ensured, there is no guarantee that all related conversions would be retrieved simply by constant time mapped key references.
			 * Therefore, a non constant time is necessary.*/
			matchingFormattedConversionFavorites.add(getKey2FromKey1(unit.getName())); 
			
			for(String candidateConversionFavorites:getKey2sByCategory(getCategoryOfKey(unit.getName()))){
				if(candidateConversionFavorites.contains(unit.getName()))
					matchingFormattedConversionFavorites.add(candidateConversionFavorites);
			}
		}
		else if(containsItem(unit.getName())){
			for(String formattedConversionFavorite:getAllKey2s()){
				if(formattedConversionFavorite.contains(unit.getName()))
					matchingFormattedConversionFavorites.add(formattedConversionFavorite);
			}
		}
		return matchingFormattedConversionFavorites;
	}
	public List<String> getFormattedConversionsAssociatedWithCategory(String category){
		return new ArrayList<String>(getKey2sByCategory(category));
	}
	
	///
	public static String getSourceUnitNameFromConversion(String formattedConversion){
		String[] categoryNConversionUnitNames = formattedConversion.split(DELIMITERS[0]);
		return categoryNConversionUnitNames[1].split(DELIMITERS[1])[0];
	}
	public static String getTargetUnitNameFromConversion(String formattedConversion){
		String[] categoryNConversionUnitNames = formattedConversion.split(DELIMITERS[0]);
		return categoryNConversionUnitNames[1].split(DELIMITERS[1])[1];
	}
	public static String getUnitCategoryFromFormattedConversion(String formattedConversion){
		return formattedConversion.split(DELIMITERS[0])[0];
	}
	
	///
	private String convertToFormattedConversion(Unit sourceUnit, Unit targetUnit){
		return convertToFormattedConversion(sourceUnit.getCategory(), sourceUnit.getName(), targetUnit.getName());
	}
	public static  String convertToFormattedConversion(String category, String sourceUnitName, String targetUnitName){
		return category.toUpperCase()+DELIMITERS[0]+sourceUnitName +DELIMITERS[1]+ targetUnitName;
	}
	
	///
	public Collection<String> getAllFormattedConversions(){
		return getAllKey2s();
	}
}
