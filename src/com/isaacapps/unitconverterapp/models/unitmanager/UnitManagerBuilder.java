package com.isaacapps.unitconverterapp.models.unitmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.isaacapps.unitconverterapp.models.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager.UNIT_TYPE;

public class UnitManagerBuilder {
	boolean[] componentStates; //Keeps track component has been added or removed. [0] -> base units maps, [1] -> non-base units map, [2] -> core prefixes map, [3] -> dynamic prefixes map, [4] -> fundamental units map
	private ArrayList<Unit> baseUnits;
	private ArrayList<Unit> nonBaseUnits;
	private Map<String, Double> corePrefixesNAbbreviationsMap;
	private Map<String, Double> dynamicPrefixesNAbbreviationsMap;
	private Map<String, Map<String, UNIT_TYPE>> fundUnitsMap;
	
	///
	public UnitManagerBuilder(){
		componentStates = new boolean[]{false, false, false, false, false};
		baseUnits = new ArrayList<Unit>();
		nonBaseUnits = new ArrayList<Unit>();
		fundUnitsMap = new HashMap<String, Map<String,UNIT_TYPE>>();
		corePrefixesNAbbreviationsMap = new HashMap<String, Double>();
		dynamicPrefixesNAbbreviationsMap = new HashMap<String, Double>();
	}
	
	///
	public UnitManagerBuilder setBaseUnitsComponent(ArrayList<Unit> unitMaps){	
		if(unitMaps != null && !unitMaps.isEmpty()){
			this.baseUnits = unitMaps;
			componentStates[0] = true;
		}
		return this;
	}
	public UnitManagerBuilder setNonBaseUnitsComponent(ArrayList<Unit> unitMaps){	
		if(unitMaps != null && !unitMaps.isEmpty()){
			this.nonBaseUnits = unitMaps;
			componentStates[1] = true;
		}
		return this;
	}
	public UnitManagerBuilder setCorePrefixesNAbbreviationsMapComponent(Map<String, Double> prefixesMap){
		if(!prefixesMap.isEmpty()){
			this.corePrefixesNAbbreviationsMap = prefixesMap;
			componentStates[2] = true;
		}
		return this;
	}
	public UnitManagerBuilder setDynamicPrefixesNAbbreviationsMapComponent(Map<String, Double> prefixesMap){
		if(prefixesMap != null && !prefixesMap.isEmpty()){
			this.dynamicPrefixesNAbbreviationsMap = prefixesMap;
			componentStates[3] = true;
		}
		return this;
	}
	public UnitManagerBuilder setFundUnitsMapComponent(Map<String, Map<String, UNIT_TYPE>> fundUnitsMap){
		if(fundUnitsMap != null && !fundUnitsMap.isEmpty()){
			this.fundUnitsMap = fundUnitsMap;
			componentStates[4] = true;	
		}
		return this;
	}
	
	public boolean areMinComponentsForCreationAvailable(){
		//Determines if the minimum needed components are available to create an adequately functional unit manager. 
		return (componentStates[0] && componentStates[2] && componentStates[4]);
	}
	
	///
	public UnitManagerBuilder combineWith(UnitManagerBuilder builder2){
		UnitManagerBuilder combinedBuilder = new UnitManagerBuilder();
		
		ArrayList<Unit> combinedBaseUnits = this.baseUnits; combinedBaseUnits.addAll(builder2.baseUnits);
		combinedBuilder.setBaseUnitsComponent(combinedBaseUnits);

		ArrayList<Unit> combinedNonBaseUnits = this.nonBaseUnits; combinedNonBaseUnits.addAll(builder2.nonBaseUnits);
		combinedBuilder.setNonBaseUnitsComponent(combinedNonBaseUnits);
		
		Map<String, Double> combinedCorePrefixMap = this.corePrefixesNAbbreviationsMap; combinedCorePrefixMap.putAll(builder2.corePrefixesNAbbreviationsMap);
		combinedBuilder.setCorePrefixesNAbbreviationsMapComponent(combinedCorePrefixMap);
		 
		Map<String, Double> combinedDynamicPrefixMap = this.dynamicPrefixesNAbbreviationsMap; combinedDynamicPrefixMap.putAll(builder2.dynamicPrefixesNAbbreviationsMap);
		combinedBuilder.setDynamicPrefixesNAbbreviationsMapComponent(combinedDynamicPrefixMap); 
		
		Map<String, Map<String, UNIT_TYPE>> combinedFundUnitsMap = this.fundUnitsMap; combinedFundUnitsMap.putAll(builder2.fundUnitsMap);
		combinedBuilder.setFundUnitsMapComponent(combinedFundUnitsMap);

		return combinedBuilder;
	}
	
	///
	public UnitManagerBuilder clearBaseUnitComponent(){
		baseUnits.clear();
		componentStates[0] = false;
		return this;
	}
	public UnitManagerBuilder clearNonBaseUnitsComponent(){
		nonBaseUnits.clear();
		componentStates[1] = false;
		return this;
	}
	public UnitManagerBuilder clearCorePrefixesMapComponent(){
		corePrefixesNAbbreviationsMap.clear();
		componentStates[2] = false;
		return this;
	}
	public UnitManagerBuilder clearDynamicPrefixesMapComponent(){
		dynamicPrefixesNAbbreviationsMap.clear();
		componentStates[3] = false;
		return this;
	}
	public UnitManagerBuilder clearFundUnitsMapComponent(){
		fundUnitsMap.clear();
		componentStates[4] = false;
		return this;
	}
	public UnitManagerBuilder clearAll(){
		clearBaseUnitComponent();
		clearCorePrefixesMapComponent();
		clearDynamicPrefixesMapComponent();
		clearFundUnitsMapComponent();
		return clearNonBaseUnitsComponent();
	}
	
	///
	public UnitManager createUnitManager(){
		UnitManager unitManager = new UnitManager();

		//Inject dependencies
		unitManager.setDataMaps(new DataMaps());		
		unitManager.setUnitsClassifier(new UnitsClassifier());
		unitManager.setContentModifiers(new ContentModifier(unitManager));
		unitManager.setQueryExecutor(new QueryExecutor(unitManager));
		unitManager.setConverter(new Converter(unitManager));
		unitManager.setUtility(new Utility(unitManager.getDataMaps(), unitManager.getQueryExecutor()));
		
		//Set an unknown base unit to be return when no other unit in manager matches a query.
		Unit unit = new Unit();
		unit.setCoreUnitState(true);
		unitManager.getContentModifier().addUnit(unit);
		
		updateManager(unitManager);
	
		return unitManager;
	}
	public boolean updateManager(UnitManager unitManager){
		if(areMinComponentsForCreationAvailable()){
			for(Entry<String, Double> prefixEntry:corePrefixesNAbbreviationsMap.entrySet()){
				unitManager.getContentModifier().addCorePrefix(prefixEntry.getKey().split("::")[0], 
						                  prefixEntry.getKey().split("::")[1], prefixEntry.getValue());
			}
				
			if(componentStates[3]){
				for(Entry<String, Double> prefixEntry:dynamicPrefixesNAbbreviationsMap.entrySet()){
					unitManager.getContentModifier().addDynamicPrefix(prefixEntry.getKey().split("::")[0], 
												 prefixEntry.getKey().split("::")[1], prefixEntry.getValue());
				}
			}	
			
			unitManager.getContentModifier().setFundamentalUnitsMap(fundUnitsMap);
			
			unitManager.getContentModifier().addUnits(baseUnits);
			
			if(componentStates[1]){
				unitManager.getContentModifier().addUnits(nonBaseUnits);
			}
			
			return true;
		}
		else{
			return false;
		}
	}
}
