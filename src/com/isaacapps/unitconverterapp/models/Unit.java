package com.isaacapps.unitconverterapp.models;

import java.util.*;
import java.util.Map.*;

import com.isaacapps.unitconverterapp.models.unitmanager.*;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager.DATA_MODEL_CATEGORY;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.UnitsDataModel;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel.*;

public class Unit{
	///
	protected UnitManager unitManagerContext;
	
	private boolean isCoreUnit; //Ensures the unit can not be deleted, or grossly modified due to its importance 
	private boolean isBaseUnit;
	private UNIT_TYPE type;
	private Unit baseUnit;
	private String name, abbreviation, category, description, unitSystem;
	private double[] baseConversionPolyCoeffs; //Coefficients used to convert current unit to base unit. Currently, only linear polynomial conversion functions can be modeled.
	private Map<String, Double> componentUnitsDimension; //Map that associates derivative component unit names with the value of their exponential power	
	Map<UNIT_TYPE, Double> fundamentalUnitTypesDimension; //Map that associates derivative component fundamental units with the value of their exponential power	
	Map<Unit, double[]> conversionOfDescendents; //Contains functions that enable base unit to convert to other units of same dimension.	
	
	public static final String UNKNOWN_UNIT_NAME = "unknown_unit", UNKNOWN_UNIT_SYSTEM = "unknown_system"
			                   ,UNKNOWN_UNIT_CATEGORY = "unknown_category", UNKNOWN_UNIT_ABBREVIATION = "unit"
			                   ,UNIT_SYSTEM_DELIMITER = " and ";
	
	///
	public Unit(){
		this(UNKNOWN_UNIT_NAME, UNKNOWN_UNIT_ABBREVIATION);
	}
	public Unit(String name, String abbreviation){
		isCoreUnit = false;
		
		this.name = name.toLowerCase();
		setUnitSystem(UNKNOWN_UNIT_SYSTEM, false);
		setAbbreviation(abbreviation);	
		setCategory(UNKNOWN_UNIT_CATEGORY, false);	
		setDescription("");
		
		conversionOfDescendents = new HashMap<Unit, double[]>();
		componentUnitsDimension = new HashMap<String, Double>(); componentUnitsDimension.put(name, 1.0);
		fundamentalUnitTypesDimension = new HashMap<UNIT_TYPE, Double>(); fundamentalUnitTypesDimension.put(UNIT_TYPE.UNKNOWN, 1.0);
		type = UNIT_TYPE.UNKNOWN;
		
		isBaseUnit = true;
		baseUnit = this;
		
		baseConversionPolyCoeffs = new double[]{1.0, 0.0};
		conversionOfDescendents = new HashMap<Unit, double[]>();
	}
	public Unit(String name, Map<String, Double> componentUnitsExponentMap, boolean isBaseUnit){
		this(name, name.toLowerCase());
		this.componentUnitsDimension = componentUnitsExponentMap;
		this.isBaseUnit = isBaseUnit;
		baseUnit = isBaseUnit?this:new Unit();
		baseConversionPolyCoeffs = new double[]{isBaseUnit?1.0:0.0, 0.0};
	}
	public Unit(String name, String componentUnitsExponentMapString, boolean isBaseUnit){
		this(name, Utility.getComponentUnitsDimensionFromString(componentUnitsExponentMapString), isBaseUnit);
	}	
	public Unit(String componentUnitsExponentMapString, boolean isBaseUnit){
		this(Utility.getComponentUnitsDimensionFromString(componentUnitsExponentMapString), isBaseUnit);
	}	
	public Unit(Map<String, Double> componentUnitsExponentMap, boolean isBaseUnit){
		this(Utility.getComponentUnitsDimensionAsString(componentUnitsExponentMap), componentUnitsExponentMap, isBaseUnit);
	}
	public Unit(String name, String category, String description, String unitSystem, String abbreviation, Map<String, Double> componentUnitsDimension, Unit baseUnit, double[] baseConversionPolyCoeffs){
		this(name, abbreviation);
		this.componentUnitsDimension = componentUnitsDimension;		
		setCategory(category, false);
		setDescription(description);
		setUnitSystem(unitSystem, false);
		setBaseUnit(baseUnit, baseConversionPolyCoeffs);
	}
	
	///	
	private void setAutomaticCategory(){
		if(!getFundamentalUnitTypesDimension().containsKey(UNIT_TYPE.UNKNOWN) && !isCoreUnit){
			if(!baseUnit.category.equalsIgnoreCase(UNKNOWN_UNIT_CATEGORY) ){
				setCategory(baseUnit.getCategory(), false);
			}
			else{
				setCategory(Utility.getFundamentalUnitTypesDimensionAsString(fundamentalUnitTypesDimension), false);
			}
		}
	}
	public void setAutomaticUnitSystem(){
		if(unitManagerContext != null && !isCoreUnit){
			//If the component units are both SI and US for example, then the unit system is "SI and US".
			Set<String> unitSystemsOfComponents = new HashSet<String>();
			Unit componentUnit;
			
			for(String componentUnitName:componentUnitsDimension.keySet()){
				componentUnit = unitManagerContext.getUnitsDataModel().getUnit(componentUnitName, false);
				if(componentUnit != null){
					String[] unitSystems = componentUnit.getUnitSystem().split(UNIT_SYSTEM_DELIMITER);		
					for(String unitSystem:unitSystems){
						unitSystemsOfComponents.add(unitSystem);			
					}
				}
			}
		
			String unitSystemCandidate = "";
			for(String unitSystem:unitSystemsOfComponents){
				if(unitSystemCandidate.equals("")){
					unitSystemCandidate = unitSystem;
				}
				else{
					unitSystemCandidate += UNIT_SYSTEM_DELIMITER+unitSystem;
				}							
			}
			
			//Use the baseunit's unit system as an alternative if necessary
			if(unitSystemCandidate.equalsIgnoreCase(unitManagerContext.getUnitsDataModel().getUnknownUnit().getUnitSystem())){
				if(baseUnit != null){
					unitSystemCandidate = baseUnit.getUnitSystem();
				}
			}
			
			setUnitSystem(unitSystemCandidate, false);
		}
	}
	public boolean setAutomaticUnitTypeNFundmtTypesDim(){
		if(unitManagerContext != null){
			type = unitManagerContext.getUnitsDataModel().determineUnitType(this);		
			fundamentalUnitTypesDimension = unitManagerContext.getFundamentalUnitsDataModel().calculateFundmtUnitTypesFromCompUnitsDimension(this.componentUnitsDimension);
		}
		else{
			type = UNIT_TYPE.UNKNOWN;
			fundamentalUnitTypesDimension = new HashMap<UNIT_TYPE, Double>();
			fundamentalUnitTypesDimension.put(UNIT_TYPE.UNKNOWN, 1.0);
		}
		return type != UNIT_TYPE.UNKNOWN && !fundamentalUnitTypesDimension.containsKey(UNIT_TYPE.UNKNOWN);
	}
	
	///
	public boolean setAutomaticBaseUnit(){ //Automatically finds and sets base unit from Unit Manager context
		if(unitManagerContext != null){
			//Update the unit type and fundamental type dimension so that they are both current since, they will help determine which base unit is selected
			if(setAutomaticUnitTypeNFundmtTypesDim()){		
				Unit retrievedBaseUnit = unitManagerContext.getUnitsDataModel().getBaseUnitMatch(this);
	
				boolean alreadyHasRetrievedBaseUnit = this.getBaseUnit().equals(retrievedBaseUnit);
				
				if(!alreadyHasRetrievedBaseUnit
					&& UnitsDataModel.getDataModelCategory(retrievedBaseUnit) != DATA_MODEL_CATEGORY.UNKNOWN){
					
					setBaseUnit(retrievedBaseUnit, true);
					return true;
				}
				return alreadyHasRetrievedBaseUnit;
			}
		}
		return false;
	}
	
	public void setBaseUnit(Unit baseUnit){
		if(!isCoreUnit)
			setBaseUnit(baseUnit, true); 
	}	
	public void setBaseUnit(Unit baseUnit, double[] baseConversionPolyCoeffs){
		if(!isCoreUnit){
			this.baseConversionPolyCoeffs = baseConversionPolyCoeffs;
			setBaseUnit(baseUnit, false);
		}
	}
	private void setBaseUnit(Unit specifiedBaseUnit, boolean attemptToSetAutomaticBaseConversionPolyCoeffs){
		boolean specifiedBaseUnitIsNotActuallyABaseUnit = !specifiedBaseUnit.isBaseUnit;
		boolean baseUnitCanBeChanged = this.unitManagerContext == specifiedBaseUnit.unitManagerContext 
				                       && (this.equalsDimension(specifiedBaseUnit) || this.unitManagerContext == null)
				                       && !isCoreUnit;
		boolean baseUnitStatusHasChanged  = false;
		
		//Assign base unit and cascade assignment by one level if necessary
		if(baseUnitCanBeChanged){
			//Cascade by one level to base unit of base unit except in cases where we explicitly want to set the base unit as the unit itself.
			this.baseUnit = (specifiedBaseUnitIsNotActuallyABaseUnit && !this.equals(specifiedBaseUnit)) ? specifiedBaseUnit.baseUnit:specifiedBaseUnit; 
			
			if(this.equals(this.baseUnit)){
				baseUnitStatusHasChanged = this.isBaseUnit == false;
				this.isBaseUnit = true;
			}
			else{
				baseUnitStatusHasChanged = this.isBaseUnit == true;
				this.isBaseUnit = false;
				upPropogateBaseUnitModification(); 	
			}
		}
		
		//Assign base conversion and cascade conversion when appropriate
		if( baseUnitCanBeChanged && (attemptToSetAutomaticBaseConversionPolyCoeffs || specifiedBaseUnitIsNotActuallyABaseUnit)){
			if(this.isBaseUnit){
				baseConversionPolyCoeffs = new double[]{1.0, 0.0};
			}
			else{
				setAutomaticBaseConversionPolyCoeffs(specifiedBaseUnit, specifiedBaseUnitIsNotActuallyABaseUnit, attemptToSetAutomaticBaseConversionPolyCoeffs);	
			}		
		}	
			
		/* This 'setBaseUnit' methods can only deal with one level of base unit cascading. Consequently, if newly assigned base unit is still not a base unit after one level of cascading,
		 * then recursive call this method with newly assigned base unit as the specified base unit until all subsequent levels lead to an actual base unit. */
		if(!this.baseUnit.isBaseUnit)
			setBaseUnit(this.baseUnit, attemptToSetAutomaticBaseConversionPolyCoeffs);
		
		//Now that an actual base unit has been obtained by this point, update this base unit with this unit as its descendant
		this.baseUnit.addDescendentConversion(this, new double[]{1.0/baseConversionPolyCoeffs[0], -baseConversionPolyCoeffs[1]/baseConversionPolyCoeffs[0]});
		
		//Modify this unit's properties that may indirectly depend on the on the base unit
		setAutomaticUnitSystem();	
		setAutomaticUnitTypeNFundmtTypesDim();
		setAutomaticCategory();	
		
		//Get unit manager to tell the appropriate data model to notify other units without base units of this unit's base unit status.
		if(baseUnitStatusHasChanged && this.isBaseUnit && unitManagerContext != null)
			unitManagerContext.getUnitsDataModel().incorporateBaseUnit(this);	
		
	}
		
	//The primary unit and its specified base are decomposed to their component units.
	//Then depending on primary unit's isBasUnit status and whether the specified base unit is actually a base unit, base conversion of the decomposed component units 
	//are optionally calculated and cascaded.
	private void setAutomaticBaseConversionPolyCoeffs(Unit specifiedBaseUnit, boolean specifiedBaseUnitIsNotActuallyABaseUnit, boolean calculateComponentFactor){ 
		boolean shouldCalculateComponentFactorForThisUnit = !(this.componentUnitsDimension.size() == 1 && this.componentUnitsDimension.containsKey(this.name));
		boolean shouldCalculateComponentFactorForBaseUnit = !(specifiedBaseUnit.componentUnitsDimension.size() == 1 && specifiedBaseUnit.componentUnitsDimension.containsKey(specifiedBaseUnit.name));
		
		double baseUnitOverallFactor = (shouldCalculateComponentFactorForThisUnit && shouldCalculateComponentFactorForBaseUnit || specifiedBaseUnitIsNotActuallyABaseUnit)
										 ?specifiedBaseUnit.calculateOverallFactor(!specifiedBaseUnitIsNotActuallyABaseUnit, !this.isBaseUnit && shouldCalculateComponentFactorForBaseUnit)
										 :1.0;
		
		
		this.baseConversionPolyCoeffs = new double[]{this.calculateOverallFactor(!specifiedBaseUnitIsNotActuallyABaseUnit, !this.isBaseUnit && (shouldCalculateComponentFactorForThisUnit || specifiedBaseUnitIsNotActuallyABaseUnit))
				*(specifiedBaseUnitIsNotActuallyABaseUnit?baseUnitOverallFactor:1/baseUnitOverallFactor), 0.0};
	}
	private double calculateOverallFactor( boolean specifiedBaseUnitIsActuallyABaseUnit, boolean shouldCalculateComponentFactor){
		double overallFactor = 1.0;

		if(!this.fundamentalUnitTypesDimension.containsKey(UNIT_TYPE.UNKNOWN) && unitManagerContext != null && shouldCalculateComponentFactor){		
			Unit componentUnit;
			double componentFactor = 1.0;
			for(Entry<String, Double> entry:componentUnitsDimension.entrySet()){
				componentUnit = unitManagerContext.getUnitsDataModel().getUnit(entry.getKey(), false);
				if(componentUnit.getType() == UNIT_TYPE.DERIVED_MULTI_UNIT && componentUnit.getComponentUnitsDimension().keySet().size() > 1 ){
					componentFactor = componentUnit.calculateOverallFactor(specifiedBaseUnitIsActuallyABaseUnit, true);
				}else if (!componentUnit.isBaseUnit){
					componentFactor = this!=componentUnit ? componentUnit.getBaseUnit().calculateOverallFactor(specifiedBaseUnitIsActuallyABaseUnit, true)
							*componentUnit.getBaseConversionPolyCoeffs()[0]:1.0; //recursively calculate base conversion until absolute base is reached. 
				}
				overallFactor *= (double) Math.pow(componentFactor, entry.getValue());
			}		
		}	
				
 		return overallFactor*(specifiedBaseUnitIsActuallyABaseUnit?1.0:baseConversionPolyCoeffs[0]);
	}
	
	private void upPropogateBaseUnitModification(){
		//Called when this unit switches from being a base unit to not. Changes base unit of the previously dependent units to the new base unit.
		for(Unit depUnit:conversionOfDescendents.keySet()){
			depUnit.setBaseUnit(this.baseUnit);
		}
		conversionOfDescendents.clear();
	}
	
	///
	public Unit multiply(Unit secondUnit){
		return dimensionOperation(+1, secondUnit);
	}	
	public Unit divide(Unit secondUnit){
		return dimensionOperation(-1, secondUnit);
	}
	private Unit dimensionOperation(int sign, Unit secondUnit){
		Map<String, Double> newComponentUnitsExponentMap = new HashMap<String, Double>();
		
		//Subtracts or adds the exponents of same component units based on division or multiplication operation respectively.
		for(String cUnitName:componentUnitsDimension.keySet()){
			if(secondUnit.getComponentUnitsDimension().containsKey(cUnitName)){ 
				newComponentUnitsExponentMap.put(cUnitName, componentUnitsDimension.get(cUnitName)+sign*secondUnit.getComponentUnitsDimension().get(cUnitName));
			}
			else{
				newComponentUnitsExponentMap.put(cUnitName, componentUnitsDimension.get(cUnitName));				
			}
		}
		for(String cUnitName:secondUnit.getComponentUnitsDimension().keySet()){
			if(!newComponentUnitsExponentMap.containsKey(cUnitName)){ 
				newComponentUnitsExponentMap.put(cUnitName, sign*secondUnit.getComponentUnitsDimension().get(cUnitName));			
			}
		}
		
		//Return base unit that matches new unit. Otherwise return a new unit with same dimension .
		Unit resultUnit = new Unit(newComponentUnitsExponentMap, false);		
		
		if(unitManagerContext != null){
			Collection<Unit> matchedUnits = unitManagerContext.getUnitsDataModel().getUnitsByComponentUnitsDimension(newComponentUnitsExponentMap, true);
			
			if(!matchedUnits.isEmpty()){
				if(!matchedUnits.iterator().next().getBaseUnit().getName().equalsIgnoreCase(Unit.UNKNOWN_UNIT_NAME) ){
					resultUnit = matchedUnits.iterator().next().getBaseUnit();
				}
				else{
					resultUnit = matchedUnits.iterator().next();
				}
			}
		}
		
		return resultUnit;		
	}
	
	///
	private void addDescendentConversion(Unit targetUnit, double[] polynomialCoeffs){ 
		//If the object is a base unit, then the conversion polynomial coefficients are simply stored as is. Otherwise adds appropriately modified conversion to base unit 
		if(isBaseUnit){
			conversionOfDescendents.put(targetUnit, polynomialCoeffs);
			//Sometimes depending on how the base unit unit was added, it may need category info from its conversion descendents
			if(this.category.contains("^") && !targetUnit.category.contains("^")){
				setCategory(targetUnit.category);
			}
		}
		else{
			baseUnit.addDescendentConversion(targetUnit, new double[]{1.0/baseUnit.getBaseConversionPolyCoeffs()[0]*polynomialCoeffs[0],
															 1.0/getBaseConversionPolyCoeffs()[1]*polynomialCoeffs[0]+polynomialCoeffs[1]});
		}
	}
	public void clearConversions(){
		conversionOfDescendents.clear();
	}
		
	///
	public void addComponentUnit(String componentUnitName, double exponent, boolean updateUnitName){
		if(!isCoreUnit){
			componentUnitsDimension.put(componentUnitName, exponent);
			setAutomaticBaseUnit();
			if(updateUnitName){
				name = Utility.getComponentUnitsDimensionAsString(componentUnitsDimension);
				setAbbreviation(name);
			}
		}
	}
	public void clearComponentUnitsDimension(boolean updateName){
		if(!isCoreUnit){
			componentUnitsDimension.clear();
			if(updateName){
				 name = UNKNOWN_UNIT_NAME;
				 setAbbreviation(UNKNOWN_UNIT_ABBREVIATION);
			}
		}
	}
	
	///
	public boolean equalsDimension(Unit unit){
		boolean state = false;
		
		if(this.unitManagerContext == unit.unitManagerContext 
				&& !unit.getFundamentalUnitTypesDimension().containsKey(UNIT_TYPE.UNKNOWN)
				&& !fundamentalUnitTypesDimension.containsKey(UNIT_TYPE.UNKNOWN)){
			 //Since the units belong to same unit manager context and the unit manager is assumed to be internally consistent,
			 //units with the same dimension are given the same base units
			if(this.baseUnit == unit.baseUnit) 
				state = true;

		}
		else{
			state = equalsComponentUnitsDimension(unit.getComponentUnitsDimension(), true);
		}
		
		return state;
	} 	
	private <T> boolean equalsGenericDimension(Map<T,Double> firstGenericDimension, Map<T,Double> secondGenericDimension){
		if(firstGenericDimension.size() == secondGenericDimension.size()){
			for(Map.Entry<T, Double> entry:firstGenericDimension.entrySet()){ //Use THIS units's map as the basis of comparison
				if(!secondGenericDimension.containsKey(entry.getKey())){
					if(Math.abs(entry.getValue())>0.0){
						return false;
					}
				}
				else if(!secondGenericDimension.get(entry.getKey()).equals(entry.getValue())){
					return false;
				}
				secondGenericDimension.remove(entry.getKey());
			}
			if(secondGenericDimension.size()>0){
				//Although the number of components may be the same, there maybe some components that are raised to zero and
				//or some components that were not compared in the OTHER map since THIS unit's map was the initial basis of comparison.
				for(Map.Entry<T, Double> entry:secondGenericDimension.entrySet()){
					if(Math.abs(entry.getValue()) > 0.0){
						return false;
					}
				}
			}
			return true;
		}
		else{
			return false;
		}
	}
	public boolean equalsFundamentalUnitsDimension(Map<UNIT_TYPE, Double> otherFundamentalTypesDimension){
		return  !otherFundamentalTypesDimension.containsKey(UNIT_TYPE.UNKNOWN)
				&& !fundamentalUnitTypesDimension.containsKey(UNIT_TYPE.UNKNOWN)
				//Copy of otherFundamentalTypesDimension necessary since it will be modified.
				&& equalsGenericDimension(this.fundamentalUnitTypesDimension, new HashMap<UNIT_TYPE, Double>(otherFundamentalTypesDimension));  
	}
	public boolean equalsComponentUnitsDimension(Map<String, Double> otherComponentUnitsDimension, boolean overrideToFundamentalUnitMap){
		if(!equalsGenericDimension(this.componentUnitsDimension, new HashMap<String, Double>(otherComponentUnitsDimension)))
		{
			if(unitManagerContext != null && overrideToFundamentalUnitMap){
				return this.equalsFundamentalUnitsDimension(unitManagerContext.getFundamentalUnitsDataModel()
						                                                   .calculateFundmtUnitTypesFromCompUnitsDimension(otherComponentUnitsDimension));
			}
			return false;	
		}
		return true;
	}
	
	///
	public double[] getConversionCoeffsToTargetUnit(Unit targetUnit){
		//Linear polynomial relationship is assumed.
		if(targetUnit.getUnitManagerContext() == this.unitManagerContext && equalsDimension(targetUnit)){
			if(getBaseConversionPolyCoeffs()[1]==0.0 && targetUnit.getBaseConversionPolyCoeffs()[1]==0.0){
				return new double[]{getBaseConversionPolyCoeffs()[0] / targetUnit.getBaseConversionPolyCoeffs()[0], 0.0};				
			}
			else{
				return new double[]{getBaseConversionPolyCoeffs()[0] / targetUnit.getBaseConversionPolyCoeffs()[0],
								  (getBaseConversionPolyCoeffs()[1] - targetUnit.getBaseConversionPolyCoeffs()[1])/targetUnit.getBaseConversionPolyCoeffs()[0]};
			} 
		}else{
			return new double[]{0.0, 0.0};
		}
	}
	public double[] getConversionCoeffsToUnitSystem(String targetUnitSystemString){		
		//Linear polynomial relationship is assumed
		if(unitManagerContext != null){
			//Convert every component unit to one unit system. Then return conversion factor associated with this conversion. 
			if(getUnitSystem().contains(targetUnitSystemString) && !getUnitSystem().contains(UNIT_SYSTEM_DELIMITER)){
				return new double[]{1.0, 0.0}; 
			}
			else if(!getUnitSystem().contains(targetUnitSystemString) && !getUnitSystem().contains(UNIT_SYSTEM_DELIMITER)){
				Collection<Unit> candidateUnitsWithProperUnitSystem = unitManagerContext.getUnitsDataModel().getUnitsByUnitSystem(targetUnitSystemString);
				if(!candidateUnitsWithProperUnitSystem.isEmpty()){
					for(Unit candidateUnit:candidateUnitsWithProperUnitSystem){
						if(equalsFundamentalUnitsDimension(candidateUnit.getFundamentalUnitTypesDimension()))
							return this.getConversionCoeffsToTargetUnit(candidateUnit);		
					}
				}
			}else if(!getFundamentalUnitTypesDimension().containsKey(UNIT_TYPE.UNKNOWN)){
				Unit componentUnit;
				double[] conversionCoeffs = new double[]{1.0, 0.0};					
				for(Entry<String, Double> entry:getComponentUnitsDimension().entrySet()){
					componentUnit =  unitManagerContext.getUnitsDataModel().getUnit(entry.getKey(), false);
					conversionCoeffs = new double[]{ conversionCoeffs[0] * (double)Math.pow(componentUnit.getConversionCoeffsToUnitSystem(targetUnitSystemString)[0], entry.getValue()), 0.0};
				}
				return conversionCoeffs;
			}	
		}
		
		return new double[]{0.0, 0.0};
	}
	
	///
	public String getName(){ //Ideally one should only be able to get name. It should only be set during unit initialization
		return name;
	}
	
	public String getUnitSystem(){
		return unitSystem;
	}
	private void setUnitSystem(String unitSystem, boolean notifyUnitsClassifier){
		this.unitSystem = unitSystem.toLowerCase();
		
		//Re-add this unit to the unit classifier with its new unit system classification such that any other
		//pre-exisiting classifications for the same units name are removed. 
		if(unitManagerContext != null && notifyUnitsClassifier)
			unitManagerContext.getUnitsClassifierDataModel().addToHierarchy(this, true);
	}
	public void setUnitSystem(String unitSystem){
		setUnitSystem(unitSystem, true);
	}
	
	public String getAbbreviation(){
		return abbreviation;
	}
	public void setAbbreviation(String abbreviation){
		this.abbreviation = abbreviation;
	}
	
	public String getCategory(){
		return category;
	}
	private void setCategory(String unitCategory, boolean notifyUnitsClassfier){
		this.category = unitCategory.toLowerCase();
		
		//Re-add this unit to the unit classifier with its new category classification such that any other
		//pre-exisiting classifications for the same units name are removed. 
		if(unitManagerContext != null && notifyUnitsClassfier)
			unitManagerContext.getUnitsClassifierDataModel().addToHierarchy(this, true);
	}
	public void setCategory(String unitCategory){
		setCategory(unitCategory, true);
	}
	
	public String getDescription(){
		return description;
	}
	public void setDescription(String description){
		this.description = description;
	}
	
	public boolean isCoreUnit(){
		return isCoreUnit;
	}
	public void setCoreUnitState(boolean state){
		isCoreUnit = state;
	} 
	
	///
	public UNIT_TYPE getType(){
		return type;
	}	
		
	public boolean isBaseUnit(){
		return isBaseUnit;
	}
	public Unit getBaseUnit(){
		return baseUnit;
	}
	public double[] getBaseConversionPolyCoeffs(){
		return this.baseConversionPolyCoeffs;
	}
	
	public Map<Unit, double[]> getConversionsOfDescendents(){
		return conversionOfDescendents;
	}	

	public Map<String, Double> getComponentUnitsDimension(){
		return componentUnitsDimension;
	}
	public Map<UNIT_TYPE, Double> getFundamentalUnitTypesDimension(){
		return fundamentalUnitTypesDimension;
	}
		
	///
	public void setUnitManagerContext(UnitManager unitManager){ //Although this is public, it should ideally only be called when the unit is added or removed from a unit data model that is associated with a particular unit manager.
		this.unitManagerContext = unitManager;
		
		//Automatically update this unit's unimplemented properties based on unit manager if present				
		setAutomaticBaseUnit();
		setAutomaticUnitSystem();
		setAutomaticCategory();			
	}
	public UnitManager getUnitManagerContext(){
		return unitManagerContext;
	}

	///
	@Override
	public String toString(){
		return name;
	}
}
