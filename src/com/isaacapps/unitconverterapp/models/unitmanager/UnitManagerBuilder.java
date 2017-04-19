package com.isaacapps.unitconverterapp.models.unitmanager;

import java.util.ArrayList;

import com.isaacapps.unitconverterapp.models.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager.*;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.*;

public class UnitManagerBuilder {
	boolean[] componentStates; //Keeps track of components that have been added or removed. [0] -> base units maps, [1] -> non-base units map, [2] -> core prefixes map, [3] -> dynamic prefixes map, [4] -> fundamental units map
	
	private ArrayList<Unit> baseUnits;
	private ArrayList<Unit> nonBaseUnits;
		
	private PrefixesDataModel prefixesDataModel;
	private UnitsDataModel unitsDataModel;
	private FundamentalUnitsDataModel fundamentalUnitsDataModel;	
	private UnitsClassifierDataModel unitsClassifierDataModel;
	private ConversionFavoritesDataModel conversionFavoritesDataModel;
	
	private Converter converter;
	private Utility utility;
	
	///
	public UnitManagerBuilder(){
		componentStates = new boolean[]{false, false, false, false, false};
		
		baseUnits = new ArrayList<Unit>();
		nonBaseUnits = new ArrayList<Unit>();
		
		prefixesDataModel = new PrefixesDataModel();
		unitsDataModel = new UnitsDataModel();
		fundamentalUnitsDataModel = new FundamentalUnitsDataModel();
		unitsClassifierDataModel = new UnitsClassifierDataModel();
		conversionFavoritesDataModel = new ConversionFavoritesDataModel();

		converter = new Converter();
		utility = new Utility();
	}
	
	///
	public UnitManagerBuilder addBaseUnits(ArrayList<Unit> baseUnits){	
		if(baseUnits != null){
			for(Unit baseUnit:baseUnits){
				addBaseUnit(baseUnit);
			}
		}
		return this;
	}
	public UnitManagerBuilder addBaseUnit(Unit baseUnit){
		if(baseUnit != null && baseUnit.isBaseUnit()){
			baseUnits.add(baseUnit);
			componentStates[0] = true; 
		}
		return this;
	}
	public UnitManagerBuilder clearBaseUnits(){
		baseUnits.clear();
		componentStates[0] = false;
		return this;
	}
	
	public UnitManagerBuilder addNonBaseUnits(ArrayList<Unit> nonBaseUnits){	
		if(nonBaseUnits != null){
			for(Unit nonBaseUnit:nonBaseUnits){
				addNonBaseUnit(nonBaseUnit);
			}
		}
		return this;
	}
	public UnitManagerBuilder addNonBaseUnit(Unit nonBaseUnit){
		if(nonBaseUnit != null && !nonBaseUnit.isBaseUnit()){
			nonBaseUnits.add(nonBaseUnit);
			componentStates[1] = true; 
		}
		return this;
	}
	public UnitManagerBuilder clearNonBaseUnits(){
		nonBaseUnits.clear();
		componentStates[1] = false;
		return this;
	}
	
	///
	public UnitManagerBuilder addPrefixDataModel(PrefixesDataModel prefixesDataModel){
		this.prefixesDataModel.combineWith(prefixesDataModel);
		componentStates[2] = this.prefixesDataModel.hasCorePrefxes();
		componentStates[3] = this.prefixesDataModel.hasDynamicPrefxes();
		return this;
	}
	
	public UnitManagerBuilder addCorePrefix(String prefixFullName, String prefixAbbreviation, Double prefixValue){
		prefixesDataModel.addCorePrefix(prefixFullName, prefixAbbreviation, prefixValue);
		componentStates[2] = true;
		return this;
	}
	public UnitManagerBuilder clearCorePrefixes(){
		prefixesDataModel.removeAllCorePrefixes();;
		componentStates[2] = false;
		return this;
	}
	
	public UnitManagerBuilder addDynamicPrefix(String prefixFullName, String prefixAbbreviation, Double prefixValue){
		prefixesDataModel.addDynamicPrefix(prefixFullName, prefixAbbreviation, prefixValue);
		componentStates[3] = true;
		return this;
	}
	public UnitManagerBuilder cleaDynamicPrefixesNAbbreviations(){
		prefixesDataModel.removeAllDynamicPrefixes();;
		componentStates[3] = false;
		return this;
	}
	
	///
	public UnitManagerBuilder addFundamentalUnitsDataModel(FundamentalUnitsDataModel fundamentalUnitsDataModel){
		this.fundamentalUnitsDataModel.combineWith(fundamentalUnitsDataModel);
		componentStates[4] = true;
		return this;
	}
	public UnitManagerBuilder addFundamentalUnit(String unitSystem, String unitName, UNIT_TYPE unitType){
		fundamentalUnitsDataModel.addFundamentalUnit(unitSystem, unitName, unitType);
		componentStates[4] = true;
		return this;	
	}
	public UnitManagerBuilder clearFundUnits(){
		fundamentalUnitsDataModel.removeAllItems();
		componentStates[4] = false;
		return this;
	}
	
	///
	public UnitManagerBuilder clearAll(){
		clearBaseUnits();
		clearNonBaseUnits();
		clearCorePrefixes();
		cleaDynamicPrefixesNAbbreviations();
		return clearFundUnits();
	}
		
	///
	public UnitManagerBuilder combineWith(UnitManagerBuilder otherBuilder){
		this.addBaseUnits(otherBuilder.baseUnits);
		this.addNonBaseUnits(otherBuilder.nonBaseUnits);

		this.addPrefixDataModel(otherBuilder.prefixesDataModel);
		
		this.addFundamentalUnitsDataModel(otherBuilder.fundamentalUnitsDataModel);

		return this;
	}
	
	///
	public boolean areMinComponentsForCreationAvailable(){
		//Determines if the minimum needed components are available to create an adequately functional unit manager. 
		return componentStates[0] && componentStates[2] && componentStates[4] && someFundamentalUnitsAreImplemented();
	}
	private boolean someFundamentalUnitsAreImplemented(){
		//Assumption is that only base units can be be defined as fundamental units.
		for(Unit baseUnit:baseUnits){
			if(fundamentalUnitsDataModel.containsUnitName(baseUnit.getName()))
				return true;
		}
		return false;
	}
	public boolean areAnyComponentsAvailable(){
		for(int i=0;i<componentStates.length;i++){
			if(componentStates[i])
				return true;
		}
		return false;
	}
	
	public UnitManager build(){
		UnitManager unitManager = new UnitManager();
		
		//Inject dependencies
		unitManager.setUnitsModelData(unitsDataModel);		
		unitManager.setPrefixesModelData(prefixesDataModel);
		unitManager.setFundamentalUnitsModelData(fundamentalUnitsDataModel);
		unitManager.setUnitsClassifierDataModel(unitsClassifierDataModel);
		unitManager.setConversionFavoritesDataModel(conversionFavoritesDataModel);
		
		unitManager.setConverter(converter);
		unitManager.setUtility(utility);
		
		//Set an unknown base unit to be return when no other unit in manager matches a query.
		Unit unit = new Unit();
		unit.setCoreUnitState(true);
		unitsDataModel.addUnit(unit);
		
		update(unitManager);
	
		return unitManager;
	}
	public boolean update(UnitManager unitManager){ //Updates any existing unit manager with the content of this unit manager builder.
		if(areAnyComponentsAvailable()){
			if(componentStates[2] || componentStates[3])
				unitManager.getPrefixesDataModel().combineWith(prefixesDataModel);
			
			if(componentStates[4])
				unitManager.getFundamentalUnitsDataModel().combineWith(fundamentalUnitsDataModel);
			
			//Base units depend on the fundamental units map being set first in order to ensure that their types can be determined.
			if(componentStates[0])
				unitManager.getUnitsDataModel().addUnits(baseUnits);
			
			//Non base units depend on the base units being set first in order to ensure dimensions and types are properly determined.
			if(componentStates[1])
				unitManager.getUnitsDataModel().addUnits(nonBaseUnits);
			
			return true;
		}
		else{
			return false;
		}
	}
}
