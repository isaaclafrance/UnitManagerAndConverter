package com.isaacapps.unitconverterapp.models;

import java.util.*;
import java.util.Map.*;

import com.isaacapps.unitconverterapp.models.unitmanager.*;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager.DATA_MODEL_CATEGORY;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.UnitsDataModel;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel.*;

public class Unit{
	///
	protected UnitManager unitManagerRef;
	
	private boolean isCoreUnit; //Ensures the unit can not be deleted, or grossly modified due to its importance 
	private boolean isBaseUnit;
	private UNIT_TYPE type;
	private Unit baseUnit;
	private String name, abbreviation, category, description, unitSystem;
	private double[] baseConversionPolyCoeffs; //Coefficients used to convert current unit to base unit. Only linear functions can be modeled.
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
		
		this.name = name;
		setUnitSystem(UNKNOWN_UNIT_SYSTEM);
		setAbbreviation(abbreviation);	
		setCategory(UNKNOWN_UNIT_CATEGORY);	
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
		this(name.toLowerCase(), name.toLowerCase());
		this.componentUnitsDimension = componentUnitsExponentMap;
		this.isBaseUnit = isBaseUnit;
		baseUnit = isBaseUnit?this:new Unit();
		baseConversionPolyCoeffs = isBaseUnit?new double[]{1.0, 0.0}:new double[]{0.0, 0.0};
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
		this(name.toLowerCase(), abbreviation);
		this.componentUnitsDimension = componentUnitsDimension;		
		setCategory(category.toLowerCase());
		setDescription(description);
		setUnitSystem(unitSystem.toLowerCase());
		setBaseUnit(baseUnit, baseConversionPolyCoeffs);
	}
	
	///	
	private void setAutomaticCategory(){
		if(!getFundamentalUnitTypesDimension().containsKey(UNIT_TYPE.UNKNOWN) && !isCoreUnit){
			if(!baseUnit.category.equalsIgnoreCase(UNKNOWN_UNIT_CATEGORY) ){
				setCategory(baseUnit.getCategory());
			}
			else{
				setCategory(Utility.getFundamentalUnitTypesDimensionAsString(fundamentalUnitTypesDimension));
			}
		}
	}
	public void setAutomaticUnitSystem(){
		if(unitManagerRef != null){
			//If the component units are both SI and US for example, then the unit system is "SI and US".
			Set<String> unitSystemsOfComponents = new HashSet<String>();
			Unit componentUnit;
			
			for(String componentUnitName:componentUnitsDimension.keySet()){
				componentUnit = unitManagerRef.getUnitsDataModel().getUnit(componentUnitName, false);
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
			
			//Use baseunit unit system as an alternative if necessary
			if(unitSystemCandidate.equalsIgnoreCase(unitManagerRef.getUnitsDataModel().getUnit(UNKNOWN_UNIT_NAME).getUnitSystem())){
				if(baseUnit != null){
					unitSystemCandidate = baseUnit.getUnitSystem();
				}
			}
			
			setUnitSystem(unitSystemCandidate);
		}
	}
	public void setAutomaticUnitTypeNFundmtTypesDim(){
		if(unitManagerRef != null){
			type = unitManagerRef.getUnitsDataModel().determineUnitType(this);		
			fundamentalUnitTypesDimension = unitManagerRef.getFundamentalUnitsDataModel().calculateFundmtUnitTypesFromCompUnitsDimension(this.componentUnitsDimension);
		}
		else{
			type = UNIT_TYPE.UNKNOWN;
			fundamentalUnitTypesDimension = new HashMap<UNIT_TYPE, Double>();
			fundamentalUnitTypesDimension.put(UNIT_TYPE.UNKNOWN, 1.0);
		}
	}
	
	///
	public void setBaseUnit(Unit baseUnit){
		/*The method only accounts for one level where base unit is not an actual base unit. 
		 Therefore, loop as many times as necessary while cascading base units and conversions until a base until is found. */
		if(!isCoreUnit){
			do{
				setBaseUnit(baseUnit, true); 
			}while(!this.baseUnit.isBaseUnit);
		}
	}	
	public void setBaseUnit(Unit baseUnit, double[] baseConversionPolyCoeffs){
		if(!isCoreUnit){
			this.baseConversionPolyCoeffs = baseConversionPolyCoeffs;
			do{
				setBaseUnit(baseUnit, false);
			}while(!this.baseUnit.isBaseUnit);
		}
	}
	public void setBaseConversionPolyCoeffs(double[] baseConversionPolyCoeffs){
		if(!isCoreUnit){
			this.baseConversionPolyCoeffs = baseConversionPolyCoeffs;
			if(this.equals(baseUnit) && baseConversionPolyCoeffs[0]==1.0 && baseConversionPolyCoeffs[1]==0.0){
				isBaseUnit = true;
			}
		}
	}
	private void setBaseUnit(Unit baseUnit, boolean attemptSetAutomaticBaseConversionPolyCoeffs){
		boolean specifiedBaseUnitIsNotActuallyABaseUnit = !baseUnit.isBaseUnit;
		boolean baseUnitCanBeChanged = this.unitManagerRef == baseUnit.unitManagerRef 
				                       && (this.equalsDimension(baseUnit) || this.unitManagerRef == null)
				                       && !isCoreUnit;
		boolean baseUnitStatusHasChanged  = false;
		
		//
		if(baseUnitCanBeChanged){
			//Cascade to base unit of base unit except in cases where we explicitly want to set the base unit as the unit itself.
			this.baseUnit = (specifiedBaseUnitIsNotActuallyABaseUnit && !this.equals(baseUnit)) ? baseUnit.baseUnit:baseUnit; 
			
			if(this.equals(this.baseUnit)){
				baseUnitStatusHasChanged = this.isBaseUnit == false;
				this.isBaseUnit = true;
			}
			else{
				baseUnitStatusHasChanged = this.isBaseUnit == true;
				this.isBaseUnit = false;
				upPropogateBaseUnitModification();	
			}
			setAutomaticUnitTypeNFundmtTypesDim();
			setAutomaticCategory();		
			setAutomaticUnitSystem();
		}
		
		//
		if( baseUnitCanBeChanged && (attemptSetAutomaticBaseConversionPolyCoeffs || specifiedBaseUnitIsNotActuallyABaseUnit)){
			if(this.isBaseUnit){
				baseConversionPolyCoeffs = new double[]{1.0, 0.0};
			}
			else{
				setAutomaticBaseConversionPolyCoeffs(baseUnit, specifiedBaseUnitIsNotActuallyABaseUnit, attemptSetAutomaticBaseConversionPolyCoeffs);
				this.baseUnit.addDescendentConversion(this, new double[]{1.0/baseConversionPolyCoeffs[0], -baseConversionPolyCoeffs[1]/baseConversionPolyCoeffs[0]});	
			}		
		}	
				
		//
		if(baseUnitStatusHasChanged && this.isBaseUnit && unitManagerRef != null){
			//Re-add this unit to the unit manager in order to notify other units without base units of this unit's base unit status.
			unitManagerRef.getUnitsDataModel().addUnit(this);	
		}
	}
	
	public void setAutomaticBaseUnit(){ //Automatically finds and sets base unit from Unit Manager directory	
		if(unitManagerRef != null && !isCoreUnit){
			Unit retrievedBaseUnit = unitManagerRef.getUnitsDataModel().getBaseUnitMatch(this);

			if(!this.getBaseUnit().equals(retrievedBaseUnit)
				&& UnitsDataModel.getDataModelCategory(retrievedBaseUnit) != DATA_MODEL_CATEGORY.UNKNOWN){
				
				setBaseUnit(retrievedBaseUnit, true);
			}
		}
	}
	
	//The primary unit and its specified base are decomposed to their component units.
	//Then depending on primary unit's isBasUnit status and whether the specified base unit is actually a base unit, base conversion of the decomposed component units 
	//are optionally calculated and cascaded.
	private void setAutomaticBaseConversionPolyCoeffs(Unit baseUnit, boolean specifiedBaseUnitIsNotActuallyABaseUnit, boolean calculateComponentFactor){ 
		boolean shouldCalculateComponentFactorForThisUnit = !(this.componentUnitsDimension.size() == 1 && this.componentUnitsDimension.containsKey(this.name));
		boolean shouldCalculateComponentFactorForBaseUnit = !(baseUnit.componentUnitsDimension.size() == 1 && baseUnit.componentUnitsDimension.containsKey(baseUnit.name));
		
		double baseUnitOverallFactor = (shouldCalculateComponentFactorForThisUnit && shouldCalculateComponentFactorForBaseUnit || specifiedBaseUnitIsNotActuallyABaseUnit)? 
										 baseUnit.calculateOverallFactor(!specifiedBaseUnitIsNotActuallyABaseUnit, !this.isBaseUnit && shouldCalculateComponentFactorForBaseUnit)
										 :1.0;
		
		
		this.baseConversionPolyCoeffs = new double[]{this.calculateOverallFactor(!specifiedBaseUnitIsNotActuallyABaseUnit, !this.isBaseUnit && (shouldCalculateComponentFactorForThisUnit || specifiedBaseUnitIsNotActuallyABaseUnit))
				*(specifiedBaseUnitIsNotActuallyABaseUnit?baseUnitOverallFactor:1/baseUnitOverallFactor), 0.0};
	}
	private double calculateOverallFactor( boolean specifiedBaseUnitIsActuallyABaseUnit, boolean shouldCalculateComponentFactor){
		double overallFactor = 1.0;

		if(!this.fundamentalUnitTypesDimension.containsKey(UNIT_TYPE.UNKNOWN) && unitManagerRef != null && shouldCalculateComponentFactor){		
			Unit componentUnit;
			double componentFactor = 1.0;
			for(Entry<String, Double> entry:componentUnitsDimension.entrySet()){
				componentUnit = unitManagerRef.getUnitsDataModel().getUnit(entry.getKey(), false);
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
		
		if(unitManagerRef != null){
			Collection<Unit> matchedUnits = unitManagerRef.getUnitsDataModel().getUnitsByComponentUnitsDimension(newComponentUnitsExponentMap, true);
			
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
		
		if(this.unitManagerRef == unit.unitManagerRef 
		  && !unit.getFundamentalUnitTypesDimension().containsKey(UNIT_TYPE.UNKNOWN)
		  && !fundamentalUnitTypesDimension.containsKey(UNIT_TYPE.UNKNOWN)){
			if(this.baseUnit == unit.baseUnit){
				state = true;
			}
			else{
				state = equalsComponentUnitsDimension(unit.getComponentUnitsDimension(), true);
			}
		}
		
		return state;
	} 	
	public boolean equalsFundamentalUnitsDimension(Map<UNIT_TYPE, Double> otherFundamentalTypesDimension){
		//Determines if the exponents of the fundamental unit types match
		boolean state = true;
		Map<UNIT_TYPE,Double> otherFundamentalTypesDimensionCopy = new HashMap<UNIT_TYPE, Double>(otherFundamentalTypesDimension); //Copy necessary since it will be modified.
		
		if(fundamentalUnitTypesDimension.size() == otherFundamentalTypesDimensionCopy.size() && !otherFundamentalTypesDimensionCopy.containsKey(UNIT_TYPE.UNKNOWN)
		   && !fundamentalUnitTypesDimension.containsKey(UNIT_TYPE.UNKNOWN)){
			for(Map.Entry<UNIT_TYPE, Double> entry:fundamentalUnitTypesDimension.entrySet()){ //Use THIS units's map as the basis of comparison
				if(!otherFundamentalTypesDimensionCopy.containsKey(entry.getKey())){
					if(entry.getValue()>0.0){
						state = false;
						break; 
					}
				}
				else if(!otherFundamentalTypesDimensionCopy.get(entry.getKey()).equals(entry.getValue())){
					state = false;
					break; 
				}
				otherFundamentalTypesDimensionCopy.remove(entry.getKey());
			}
			if(otherFundamentalTypesDimensionCopy.size()>0){
				//Although the number of components are the same, there some fundamental types that are raised to zero and
				//there are some types that were not compared in the OTHER map since THIS unit's map was the initial basis of comparsion.
				state = false;
			}
		}else{
			state = false;
		}
		return state;
	}
	public boolean equalsComponentUnitsDimension(Map<String, Double> otherComponentUnitsDimension, boolean overrideToFundamentalUnitMap){
		boolean state = true;	
		Map<String,Double> otherComponentUnitsDimensionCopy = new HashMap<String, Double>(otherComponentUnitsDimension); //Copy necessary since it will be modified.
		
		//First determines if exponent for each component units match
		if(this.componentUnitsDimension.size() == otherComponentUnitsDimensionCopy.size()){
			for(Map.Entry<String, Double> entry:componentUnitsDimension.entrySet()){ //Use THIS units's map as the basis of comparison
				if(!otherComponentUnitsDimensionCopy.containsKey(entry.getKey())){
					if(Math.abs(entry.getValue())>0.0){
						state = false;
						break; 
					}
				}
				else if(!otherComponentUnitsDimensionCopy.get(entry.getKey()).equals(entry.getValue())){
					state = false;
					break; 
				}
				otherComponentUnitsDimensionCopy.remove(entry.getKey());
			}
			if(otherComponentUnitsDimensionCopy.size()>0){
				//Although the number of components may be the same, there maybe some components that are raised to zero and
				//or some components that were not compared in the OTHER map since THIS unit's map was the initial basis of comparison.
				for(Map.Entry<String, Double> entry:otherComponentUnitsDimensionCopy.entrySet()){
					if(Math.abs(entry.getValue()) > 0.0){
						state = false;
						break;
					}
				}
			}
		}
		else{
			state = false;
		}

		if(!state && overrideToFundamentalUnitMap){
			//If the direct comparison of component units map fails, then performs the comparison again after converting to fundamental units map using unit manager(if present). 
			if(unitManagerRef != null){
				state = this.equalsFundamentalUnitsDimension(unitManagerRef.getFundamentalUnitsDataModel()
						                                                   .calculateFundmtUnitTypesFromCompUnitsDimension(otherComponentUnitsDimension));
			}
		}
		
		return state;		
	}
	
	///
	public double[] getConversionCoeffsToTargetUnit(Unit targetUnit){
		//Linear polynomial relationship is assumed.
		if(targetUnit.getUnitManagerRef() == this.unitManagerRef && equalsDimension(targetUnit)){
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
		if(unitManagerRef != null){
			//Convert every component unit to one unit system. Then return conversion factor associated with this conversion. 
			if(getUnitSystem().contains(targetUnitSystemString) && !getUnitSystem().contains(UNIT_SYSTEM_DELIMITER)){
				return new double[]{1.0, 0.0}; 
			}
			else if(!getUnitSystem().contains(targetUnitSystemString) && !getUnitSystem().contains(UNIT_SYSTEM_DELIMITER)){
				Collection<Unit> candidateUnitsWithProperUnitSystem = unitManagerRef.getUnitsDataModel().getUnitsByUnitSystem(targetUnitSystemString);
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
					componentUnit =  unitManagerRef.getUnitsDataModel().getUnit(entry.getKey(), false);
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
	public void setUnitSystem(String unitSystemString){
		if(unitManagerRef != null)
			unitManagerRef.getUnitsClassifierDataModel().removeFromHierarchy(this);
		
		this.unitSystem = unitSystemString.toLowerCase();
		
		//Re-add this unit to the unit classifier with its new unit system classification.
		if(unitManagerRef != null)
			unitManagerRef.getUnitsClassifierDataModel().addToHierarchy(this);
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
	public void setCategory(String unitCategory){
		if(unitManagerRef != null)
			unitManagerRef.getUnitsClassifierDataModel().removeFromHierarchy(this);
		
		this.category = unitCategory.toLowerCase();
		
		//Re-add this unit to the unit classifier with its new category classification.
		if(unitManagerRef != null)
			unitManagerRef.getUnitsClassifierDataModel().addToHierarchy(this);	
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
	public void setUnitManagerRef(UnitManager unitManager){ //Although this is public, it should ideally this should only be called when the unit is added or removed from a unit data model that is associated with a particular unit manager.
		this.unitManagerRef = unitManager;
		
		//Automatically update this unit's unimplemented properties based on unit manager if present		
		setAutomaticUnitTypeNFundmtTypesDim();			
		setAutomaticBaseUnit();
		setAutomaticUnitSystem();
		setAutomaticCategory();			
	}
	public UnitManager getUnitManagerRef(){
		return unitManagerRef;
	}

	///
	@Override
	public String toString(){
		return name;
	}
}
