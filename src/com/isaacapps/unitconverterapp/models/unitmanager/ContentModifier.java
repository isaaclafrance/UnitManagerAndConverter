package com.isaacapps.unitconverterapp.models.unitmanager;

import java.util.ArrayList;
import java.util.Map;

import com.isaacapps.unitconverterapp.models.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager.UNIT_TYPE;

public class ContentModifier {
	private UnitManager unitManagerRef;
	
	///
	public ContentModifier(UnitManager unitManagerRef) {
		this.unitManagerRef = unitManagerRef;
	}
	
	///
	public void addUnits(ArrayList<Unit> units){
		for(Unit unit:units){
			addUnit(unit);
		}
	}
	public void addUnit(Unit unit){			
		if(unit.getUnitManagerRef() != this.unitManagerRef || unit.getBaseUnit().getType() == UNIT_TYPE.UNKNOWN || unit.getFundamentalUnitTypesDimension().containsKey(UNIT_TYPE.UNKNOWN)){			
			//Need to be put in the unknown units maps first because referential execution paths in the setUnitManagerRef injector will require the unit to be present.
			unitManagerRef.getDataMaps().getUnitsWithUnknownBaseOrUnknownFundDimensionMap().put(unit.getName(), unit);
			unit.setUnitManagerRef(this.unitManagerRef);
			//Assess the characteristics(type, dimension, etc) of the unit again after associating it with this unit manager.
			if(unit.getBaseUnit().getType() != UNIT_TYPE.UNKNOWN && !unit.getFundamentalUnitTypesDimension().containsKey(UNIT_TYPE.UNKNOWN)){
				addUnit(unit);
			}
		}else{
			if(unit.isBaseUnit()){ 
				//Move any other instances of base units with same dimension to non base unit dictionary	
				for(Unit unitMatch:unitManagerRef.getQueryExecutor().getUnitsByFundamentalUnitTypeDimension(unit.getFundamentalUnitTypesDimension())){
					if(unitMatch.isBaseUnit() && !unitMatch.getName().equalsIgnoreCase(unit.getName())){
						if(unitMatch.isCoreUnit()){
							unitManagerRef.getDataMaps().getCoreBaseUnitsMap().remove(unitMatch.getName());
							unitManagerRef.getDataMaps().getCoreUnitsMap().put(unitMatch.getName(), unitMatch);
						}else{
							unitManagerRef.getDataMaps().getDynamicBaseUnitsMap().remove(unitMatch.getName());
							unitManagerRef.getDataMaps().getDynamicUnitsMap().put(unitMatch.getName(), unitMatch);
						}
										
						//Makes sure that all units have proper base unit associations after replacement	
						ArrayList<Unit> dependentUnits = new ArrayList<Unit>(unitMatch.getConversionPolyCoeffs().keySet()); //Prevent concurrent modification exceptions since 'conversionPolyCoeffs' can be cleared in setBaseUnit.
						for(Unit dependentUnit:dependentUnits){
							dependentUnit.setBaseUnit(unit);
						}	
						unitMatch.setBaseUnit(unit);
					}
				}
				//If unit already exists in nonbase dictionary then remove it since it will instead be added to base dictionary.				
				if(unit.isCoreUnit() && unitManagerRef.getDataMaps().getCoreUnitsMap().containsValue(unit)){
					unitManagerRef.getDataMaps().getCoreUnitsMap().remove(unit.getName());				
				}
				else if(unitManagerRef.getDataMaps().getDynamicUnitsMap().containsValue(unit)){
					unitManagerRef.getDataMaps().getDynamicUnitsMap().remove(unit.getName());
				}
					
				if(unit.isCoreUnit()){
					unitManagerRef.getDataMaps().getCoreBaseUnitsMap().put(unit.getName(), unit);	
				}
				else{
					unitManagerRef.getDataMaps().getDynamicBaseUnitsMap().put(unit.getName(), unit);	
				}					
			}
			else if(unit.isCoreUnit()){
				unitManagerRef.getDataMaps().getCoreUnitsMap().put(unit.getName(), unit);	
			}
			else{
				unitManagerRef.getDataMaps().getDynamicUnitsMap().put(unit.getName(), unit);		
			}
			updateAssociationsOfUnknownUnits();//Determines if this base unit can be associated with units that currently do not have base units.			
		}

	}
	public void removeDynamicUnit(String unitName){
		//Removes unit from hierarchy. Removes unit from base and dynamic dictionaries. Updates the base units of dependent units to unknown. Also removed a reference to this unit manager.
		if(unitManagerRef.getDataMaps().getDynamicBaseUnitsMap().containsKey(unitName)){
			Unit removedBaseUnit =	unitManagerRef.getDataMaps().getDynamicBaseUnitsMap().remove(unitName);
			if(removedBaseUnit != null){
				unitManagerRef.getUnitsClassifier().removeFromHierarchy(removedBaseUnit);
				for(Unit dependentUnit: removedBaseUnit.getConversionPolyCoeffs().keySet()){
					dependentUnit.setBaseUnit(unitManagerRef.getQueryExecutor().getUnit(Unit.UNKNOWN_UNIT_NAME));
				}
				removedBaseUnit.clearConversions();
				removedBaseUnit.setUnitManagerRef(null);
			}
		}
		else if(unitManagerRef.getDataMaps().getDynamicUnitsMap().containsKey(unitName)){
			Unit removedUnit =	unitManagerRef.getDataMaps().getDynamicUnitsMap().remove(unitName);
			if(removedUnit != null){
				unitManagerRef.getUnitsClassifier().removeFromHierarchy(removedUnit);
				removedUnit.getBaseUnit().getConversionPolyCoeffs().remove(removedUnit);
				removedUnit.setUnitManagerRef(null);
			}
		}
		updateFundamentalUnitsDimensionOfKnownUnits();	
	}
	public void removeDynamicUnit(Unit unit){
		removeDynamicUnit(unit.getName());
	}
	public void removeAllDynamicUnits(){
		for(Unit unit:unitManagerRef.getDataMaps().getDynamicBaseUnitsMap().values()){
			unitManagerRef.getUnitsClassifier().removeFromHierarchy(unit);
		}
		for(Unit unit:unitManagerRef.getDataMaps().getDynamicUnitsMap().values()){
			unitManagerRef.getUnitsClassifier().removeFromHierarchy(unit);
		}
		
		unitManagerRef.getDataMaps().getDynamicBaseUnitsMap().clear();
		unitManagerRef.getDataMaps().getDynamicUnitsMap().clear();
	}
	public void updateAssociationsOfUnknownUnits(){		
		Unit unit;
		//Need to do this to prevent java.util.ConcurrentModificationException
		ArrayList<Unit>  unknList = new ArrayList<Unit>(unitManagerRef.getDataMaps().getUnitsWithUnknownBaseOrUnknownFundDimensionMap().values());

		for(int i=0;i<unknList.size();i++){
			unit = unknList.get(i);
			
			unit.setAutomaticUnitTypeNFundmtTypesDim();

			if(!(unit.getFundamentalUnitTypesDimension().containsKey(UNIT_TYPE.UNKNOWN))){
								
				if(unit.isBaseUnit()){
					if(unit.isCoreUnit()){
						unitManagerRef.getDataMaps().getCoreBaseUnitsMap().put(unit.getName(), unit);
					}
					else{
						unitManagerRef.getDataMaps().getDynamicBaseUnitsMap().put(unit.getName(), unit);
					}
				}
				else{
					if(unit.isCoreUnit()){
						unitManagerRef.getDataMaps().getCoreUnitsMap().put(unit.getName(), unit);
					}
					else{
						unitManagerRef.getDataMaps().getDynamicUnitsMap().put(unit.getName(), unit);
					}
				}
				unitManagerRef.getDataMaps().getUnitsWithUnknownBaseOrUnknownFundDimensionMap().remove(unit.getName());
				unit.setAutomaticBaseUnit(true);				
			}
			
			unit.setAutomaticUnitSystem();
		}
	
	}
	private void updateFundamentalUnitsDimensionOfKnownUnits(){
		updateFundamentalUnitsDimensionOfKnownUnits(unitManagerRef.getDataMaps().getDynamicBaseUnitsMap());
		updateFundamentalUnitsDimensionOfKnownUnits(unitManagerRef.getDataMaps().getDynamicUnitsMap());
	}
	private void updateFundamentalUnitsDimensionOfKnownUnits(Map<String, Unit> unitsDictionary){
		for(Unit unit:unitsDictionary.values()){
			unit.setAutomaticUnitSystem();
			unit.setAutomaticUnitTypeNFundmtTypesDim();
			 
			if(unit.getBaseUnit() == unitManagerRef.getQueryExecutor().getUnit(Unit.UNKNOWN_UNIT_NAME) || unit.getFundamentalUnitTypesDimension().containsKey(UNIT_TYPE.UNKNOWN)){
				unitsDictionary.remove(unit.getName());
				unitManagerRef.getDataMaps().getUnitsWithUnknownBaseOrUnknownFundDimensionMap().put(unit.getName(), unit);
			}
		}
	}
	
	///
	void addCorePrefix(String prefixName, String abbreviation, double prefixValue){
		unitManagerRef.getDataMaps().getCorePrefixValuesMap().put(prefixName, prefixValue);
		unitManagerRef.getDataMaps().getCorePrefixAbbreviationsMap().put(prefixName, abbreviation);
	}
	public void addDynamicPrefix(String prefixName, String abbreviation, double prefixValue){
		unitManagerRef.getDataMaps().getDynamicPrefixValuesMap().put(prefixName, prefixValue);
		unitManagerRef.getDataMaps().getDynamicPrefixAbbreviationsMap().put(prefixName, abbreviation);
	}
	public void removeDynamicPrefix(String prefixName){
		unitManagerRef.getDataMaps().getDynamicPrefixValuesMap().remove(prefixName);
	}	
	public void removeAllDynamicPrefixes(){
		unitManagerRef.getDataMaps().getDynamicPrefixValuesMap().clear();
	}	
	
	void setFundamentalUnitsMap(Map<String, Map<String, UNIT_TYPE>> fundamentalUnitsMap){
		unitManagerRef.getDataMaps().getFundamentalUnitsMap().clear();
		unitManagerRef.getDataMaps().getFundamentalUnitsMap().putAll(fundamentalUnitsMap);
	}
}
