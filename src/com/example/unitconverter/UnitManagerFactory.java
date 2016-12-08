package com.example.unitconverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.example.unitconverter.UnitManager.UNIT_TYPE;

public class UnitManagerFactory {
	boolean[] componentStates; // Indicates which component has been added. [0] -> base units maps, [1] -> non-base units map, [2] -> core prefixes map, [3] -> dynamic prefixes map, [4] -> fundamental units map
	private ArrayList<Unit> baseUnits;
	private ArrayList<Unit> nonBaseUnits;
	private Map<String, Float> corePrefixesNAbbreviationsMap;
	private Map<String, Float> dynamicPrefixesNAbbreviationsMap;
	private Map<String, Map<String, UNIT_TYPE>> fundUnitsMap;
	
	public UnitManagerFactory(){
		componentStates = new boolean[]{false, false, false, false, false};
		baseUnits = new ArrayList<Unit>();
		nonBaseUnits = new ArrayList<Unit>();
		fundUnitsMap = new HashMap<String, Map<String,UNIT_TYPE>>();
		corePrefixesNAbbreviationsMap = new HashMap<String, Float>();
		dynamicPrefixesNAbbreviationsMap = new HashMap<String, Float>();
	}
	
	//
	public void setBaseUnitsComponent(ArrayList<Unit> unitMaps){	
		if(!unitMaps.isEmpty()){
			this.baseUnits = unitMaps;
			componentStates[0] = true;
		}
	}
	public void setNonBaseUnitsComponent(ArrayList<Unit> unitMaps){	
		if(!unitMaps.isEmpty()){
			this.nonBaseUnits = unitMaps;
			componentStates[1] = true;
		}
	}
	public void setCorePrefixesNAbbreviationsMapComponent(Map<String, Float> prefixesMap){
		if(!prefixesMap.isEmpty()){
			this.corePrefixesNAbbreviationsMap = prefixesMap;
			componentStates[2] = true;
		}
	}
	public void setDynamicPrefixesNAbbreviationsMapComponent(Map<String, Float> prefixesMap){
		if(!prefixesMap.isEmpty()){
			this.dynamicPrefixesNAbbreviationsMap = prefixesMap;
			componentStates[3] = true;
		}
	}
	public void setFundUnitsMapComponent(Map<String, Map<String, UNIT_TYPE>> fundUnitsMap){
		if(!fundUnitsMap.isEmpty()){
			this.fundUnitsMap = fundUnitsMap;
			componentStates[4] = true;	
		}
	}
	
	public boolean areMinComponentsForCreationAvailable(){
		//Determines if the minimum needed components are available to create an adequately functional unit manager. 
		return (componentStates[0] && componentStates[2] && componentStates[4]);
	}
	
	//
	public static UnitManagerFactory combineUnitManagerFactories(UnitManagerFactory factory1, UnitManagerFactory factory2){
		UnitManagerFactory combinedFactory = new UnitManagerFactory();
		
		ArrayList<Unit> combinedBaseUnits = factory1.baseUnits; combinedBaseUnits.addAll(factory2.baseUnits);
		combinedFactory.setBaseUnitsComponent(combinedBaseUnits);

		ArrayList<Unit> combinedNonBaseUnits = factory1.nonBaseUnits; combinedNonBaseUnits.addAll(factory2.nonBaseUnits);
		combinedFactory.setNonBaseUnitsComponent(combinedNonBaseUnits);
		
		Map<String, Float> combinedCorePrefixMap = factory1.corePrefixesNAbbreviationsMap; combinedCorePrefixMap.putAll(factory2.corePrefixesNAbbreviationsMap);
		combinedFactory.setCorePrefixesNAbbreviationsMapComponent(combinedCorePrefixMap);
		 
		Map<String, Float> combinedDynamicPrefixMap = factory1.dynamicPrefixesNAbbreviationsMap; combinedDynamicPrefixMap.putAll(factory2.dynamicPrefixesNAbbreviationsMap);
		combinedFactory.setDynamicPrefixesNAbbreviationsMapComponent(combinedDynamicPrefixMap); 
		
		Map<String, Map<String, UNIT_TYPE>> combinedFundUnitsMap = factory1.fundUnitsMap; combinedFundUnitsMap.putAll(factory2.fundUnitsMap);
		combinedFactory.setFundUnitsMapComponent(combinedFundUnitsMap);

		return combinedFactory;
	}
	
	//
	public void clearBaseUnitComponent(){
		baseUnits.clear();
		componentStates[0] = false;
	}
	public void clearNonBaseUnitsComponent(){
		nonBaseUnits.clear();
		componentStates[1] = false;
	}
	public void clearCorePrefixesMapComponent(){
		corePrefixesNAbbreviationsMap.clear();
		componentStates[2] = false;
	}
	public void clearDynamicPrefixesMapComponent(){
		dynamicPrefixesNAbbreviationsMap.clear();
		componentStates[3] = false;
	}
	public void clearFundUnitsMapComponent(){
		fundUnitsMap.clear();
		componentStates[4] = false;
	}
	public void clearAll(){
		clearBaseUnitComponent();
		clearCorePrefixesMapComponent();
		clearDynamicPrefixesMapComponent();
		clearFundUnitsMapComponent();
		clearNonBaseUnitsComponent();
	}
	//
	public UnitManager createUnitManager(){
		UnitManager unitManager = new UnitManager();
		updateManager(unitManager);
		return unitManager;
	}
	public void updateManager(UnitManager unitManager){
		if(areMinComponentsForCreationAvailable()){
			for(Entry<String, Float> prefixEntry:corePrefixesNAbbreviationsMap.entrySet()){
				unitManager.addCorePrefix(prefixEntry.getKey().split("::")[0], 
						                  prefixEntry.getKey().split("::")[1], prefixEntry.getValue());
			}
				
			if(componentStates[3]){
				for(Entry<String, Float> prefixEntry:dynamicPrefixesNAbbreviationsMap.entrySet()){
					unitManager.addDynamicPrefix(prefixEntry.getKey().split("::")[0], 
												 prefixEntry.getKey().split("::")[1], prefixEntry.getValue());
				}
			}	
			
			unitManager.setFundamentalUnitsMap(fundUnitsMap);
			
			unitManager.addUnits(baseUnits);
			
			if(componentStates[1]){
				unitManager.addUnits(nonBaseUnits);
			}
		}
	}
}
