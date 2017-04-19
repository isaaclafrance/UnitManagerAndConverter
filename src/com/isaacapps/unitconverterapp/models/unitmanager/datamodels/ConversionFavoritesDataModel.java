package com.isaacapps.unitconverterapp.models.unitmanager.datamodels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.isaacapps.unitconverterapp.models.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager.UNIT_TYPE;

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
		   && !sourceUnit.getFundamentalUnitTypesDimension().containsKey(UNIT_TYPE.UNKNOWN)){
		   
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
	public boolean removeConversion(Unit sourceUnit, Unit targetUnit){
		modifySignificanceRankOfConversions(sourceUnit, false);
		modifySignificanceRankOfConversions(targetUnit, false);
		return removeItemByAnyKey(convertToFormattedConversion(sourceUnit, targetUnit)) != null;
	}
	public boolean removeFormattedConversion(String formattedConversion){
		modifySignificanceRankOfConversion(formattedConversion, false);
		return removeItemByAnyKey(formattedConversion) != null;
	}
	
	///
	public boolean containsUnits(Unit sourceUnit, Unit targetUnit){
		return containsKey(convertToFormattedConversion(sourceUnit, targetUnit));
	}
	
	///
	public void modifySignificanceRankOfConversions(Unit unit, boolean increase){
		modifySignificanceRankOfConversions(getFormattedConversionsAssociatedWithUnit(unit), increase);
	}
	public void modifySignificanceRankOfConversions(String category, boolean increase){
		modifySignificanceRankOfConversions(getFormattedConversionsAssociatedWithCategory(category), increase);
	}
	private void modifySignificanceRankOfConversions(ArrayList<String> formattedConversions, boolean increase){
		for(String formattedConversion:formattedConversions){
			modifySignificanceRankOfConversion(formattedConversion, increase);
		}
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
	public ArrayList<String> getFormattedConversionsAssociatedWithUnit(Unit unit){
		ArrayList<String> matchingFormattedConversionFavorites = new ArrayList<String>();
		if(containsKey(unit.getName())){
			matchingFormattedConversionFavorites.add(getItemByAnyKey(unit.getName()));
		}
		else if(containsItem(unit.getName())){
			for(String formattedConversionFavorite:getAllKey2s()){
				if(formattedConversionFavorite.contains(unit.getName()))
					matchingFormattedConversionFavorites.add(formattedConversionFavorite);
			}
		}
		return matchingFormattedConversionFavorites;
	}
	public ArrayList<String> getFormattedConversionsAssociatedWithCategory(String category){
		return getKey2sByCategory(category);
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
	public static String getUnitCategoryFromFormattedConversion(String formattedConversionFavorite){
		return formattedConversionFavorite.split(DELIMITERS[0])[0];
	}
	
	///
	private String convertToFormattedConversion(Unit sourceUnit, Unit targetUnit){
		return convertToFormattedConversion(sourceUnit.getCategory(), sourceUnit.getName(), targetUnit.getName());
	}
	public static  String convertToFormattedConversion(String category, String sourceUnitName, String targetUnitName){
		return category.toUpperCase()+DELIMITERS[0]+sourceUnitName +DELIMITERS[1]+ targetUnitName;
	}
	
	///
	public ArrayList<String> getAllFormattedConversions(){
		return getAllKey2s();
	}
}
