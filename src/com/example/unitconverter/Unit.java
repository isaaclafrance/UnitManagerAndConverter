package com.example.unitconverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.example.unitconverter.UnitManager.UNIT_TYPE;

public class Unit{
	//Fields
	private UnitManager unitManagerRef;
	
	private boolean isCoreUnit; //If true, the unit can not be deleted
	private String unitSystem;
	private UnitManager.UNIT_TYPE unitType;
	private boolean isBaseUnit; //Is set to true if base conversion factor is set to values 1 or 0.
	private Unit baseUnit;
	private String unitName;
	private String abbreviation;
	private String unitCategory;
	private String description;
	private double[] baseConversionPolyCoeffs; //Function used to convert current unit to base unit
	private Map<String, Double> componentUnitsExponentMap; //Map that associates derivative component unit names with the value of their exponential power	
	Map<UnitManager.UNIT_TYPE, Double> fundamentalUnitsExponentMap; //Map that associates derivative component fundamental units with the value of their exponential power	
	Map<Unit, double[]> conversionPolyCoeffs; //Contains functions that enable base unit to convert to other units of same dimension.	
	
	public static final String UNKNOWN_UNIT_NAME = "unknown_unit";
	
	///Constructors
	public Unit(){
		isCoreUnit = false;
		
		unitName = UNKNOWN_UNIT_NAME;
		unitSystem = "unknown_system";
		abbreviation = "unit";	
		unitCategory = "unknown_units";	
		description = "";
		
		conversionPolyCoeffs = new HashMap<Unit, double[]>();
		componentUnitsExponentMap = new HashMap<String, Double>(); componentUnitsExponentMap.put(unitName, 1.0);
		fundamentalUnitsExponentMap = new HashMap<UnitManager.UNIT_TYPE, Double>(); fundamentalUnitsExponentMap.put(UNIT_TYPE.UNKNOWN, 1.0);
		unitType = UNIT_TYPE.UNKNOWN;
		
		isBaseUnit = true;
		baseUnit = this;
		
		baseConversionPolyCoeffs = new double[]{1.0, 0.0};
		conversionPolyCoeffs = new HashMap<Unit, double[]>();
	}
	public Unit(String componentUnitsExponentMapString, boolean isBaseUnit){
		this(UnitManager.getComponentUnitsDimensionFromString(componentUnitsExponentMapString), isBaseUnit);
	}	
	public Unit(String unitName, String componentUnitsExponentMapString, boolean isBaseUnit){
		this(unitName, UnitManager.getComponentUnitsDimensionFromString(componentUnitsExponentMapString), isBaseUnit);
	}		
	public Unit(Map<String, Double> componentUnitsExponentMap, boolean isBaseUnit){
		this();	
		this.componentUnitsExponentMap = componentUnitsExponentMap;
		this.unitName = getDimension();	
		this.isBaseUnit = isBaseUnit;
		if(isBaseUnit == true){
			baseUnit = this;
			baseConversionPolyCoeffs = new double[]{1.0f, 0.0f};
		}
	}
	public Unit(String unitName, Map<String, Double> componentUnitsExponentMap, boolean isBaseUnit){
		this();
		this.unitName = unitName.toLowerCase();	
		if(this.abbreviation.length() >= 5){
			this.abbreviation = unitName.substring(0, 4);
		}
		else{
			this.abbreviation = unitName;
		}	
		this.componentUnitsExponentMap = componentUnitsExponentMap;
		this.isBaseUnit = isBaseUnit;
		if(isBaseUnit = true){
			baseUnit = this;
			baseConversionPolyCoeffs = new double[]{1.0f, 0.0f};
		}
	}
	public Unit(String unitName, String unitCategory, String description, String unitSystem, String abbreviation, Map<String, Double> componentUnitsExponentMap, Unit baseUnit, double[] baseConversionPolyCoeffs){
		this();
		this.unitName = unitName.toLowerCase();
		this.unitCategory = unitCategory.toLowerCase();
		this.description = description.toLowerCase();
		this.unitSystem = unitSystem.toLowerCase();
		this.abbreviation = abbreviation;
		this.componentUnitsExponentMap = componentUnitsExponentMap;
		setBaseUnit(baseUnit, baseConversionPolyCoeffs);
	}
	
	///	
	private void setAutomaticUnitCategory(){
		if(!baseUnit.getUnitName().equalsIgnoreCase(UNKNOWN_UNIT_NAME) && !getFundamentalUnitsExponentMap().containsKey(UnitManager.UNIT_TYPE.UNKNOWN)){
			if(!baseUnit.unitCategory.equalsIgnoreCase("unknown_units")){
				setUnitCategory(baseUnit.getUnitCategory());
			}
			else{
				setUnitCategory(getFundamentalDimension());
			}
		}		
	}
	void setAutomaticUnitSystem(){
		if(unitManagerRef != null){
			//If contains component are both SI and US, then string is "SI and US". However, if the component are not recognizable, then set string to "Unknown Unit System(s)."
			Set<String> unitSystemsOfComponentsArrayList = new HashSet<String>();
			Unit componentUnit;
			
			for(String componentUnitName:componentUnitsExponentMap.keySet()){
				componentUnit = unitManagerRef.getUnit(componentUnitName, false);
				if(componentUnit != null){
					String[] unitSystems = componentUnit.getUnitSystem().split(" and ");		
					for(String unitSystem:unitSystems){
						unitSystemsOfComponentsArrayList.add(unitSystem);			
					}
				}
			}
		
			String unitSystem = "";
			for(String str:unitSystemsOfComponentsArrayList){
				if(unitSystem == ""){
					unitSystem = str;
				}
				else{
					unitSystem += (" and "+str);				
				}		
			}
			
			//Use baseunit unit system as an alternative if necessary
			if(unitSystem.equalsIgnoreCase(unitManagerRef.getUnit(UNKNOWN_UNIT_NAME).unitSystem)){
				if(baseUnit != null){
					unitSystem = baseUnit.unitSystem;
				}
			}
			
			setUnitSystem(unitSystem);
		}
	}
	public void setAutomaticUnitTypeNFundmtUnitsExpMap(){
		if(unitManagerRef != null){
			unitType = unitManagerRef.determineUnitType(this);		
			fundamentalUnitsExponentMap = unitManagerRef.calculateFundmtUnitsFromCompUnitsExpMap(this.componentUnitsExponentMap);
		}
		else{
			unitType = UNIT_TYPE.UNKNOWN;
			fundamentalUnitsExponentMap = new HashMap<UnitManager.UNIT_TYPE, Double>();
			fundamentalUnitsExponentMap.put(UNIT_TYPE.UNKNOWN, 1.0);
		}
	}
	
	void setAutomaticBaseUnit(boolean setAutomaticBaseCoversionPolyCoeffs){ //Automatically finds and sets base unit from Unit Manager directory	
		if(unitManagerRef != null){
			Unit retrievedBaseUnit = unitManagerRef.getBaseUnit(this);

			if(!this.getBaseUnit().equals(retrievedBaseUnit)
				&&!retrievedBaseUnit.getUnitName().equalsIgnoreCase(Unit.UNKNOWN_UNIT_NAME)){
				
				if(this.baseUnit.getBaseUnit().equals(retrievedBaseUnit)){ //Ensures that core units loaded from xml that have a nonbase unit set as base unit are able to cascade their conversion coefficients
					setBaseUnit(this.baseUnit, setAutomaticBaseCoversionPolyCoeffs);
				}
				else{
					setBaseUnit(retrievedBaseUnit, setAutomaticBaseCoversionPolyCoeffs);
				}
				
			}
		}
	}
	private void upPropogateBaseUnitModification(){
		//Called when this unit switches from being a base unit to not. Changes base unit of the previously dependent units to the new base unit.
		for(Unit depUnit:conversionPolyCoeffs.keySet()){
			depUnit.setBaseUnit(this.baseUnit);
		}
		conversionPolyCoeffs.clear();
	}
	private void setBaseUnit(Unit baseUnit, boolean setAutomaticBaseConversionPolyCoeffs){
		//Makes sure that a non-base unit with improper dimensions is not assigned as a base unit
		boolean specifiedBaseUnitIsNotActuallyABaseUnit = !baseUnit.isBaseUnit;

		if(!baseUnit.getFundamentalUnitsExponentMap().containsKey(UNIT_TYPE.UNKNOWN) && !this.getFundamentalUnitsExponentMap().containsKey(UNIT_TYPE.UNKNOWN)){
			if(this.equalsDimension(baseUnit)){ 
				if(this.baseUnit != baseUnit || specifiedBaseUnitIsNotActuallyABaseUnit && this.baseUnit == baseUnit){
					if(this.unitManagerRef == baseUnit.unitManagerRef){// For consistency sake, a valid base unit can only come from the same unit manager.
						if(!specifiedBaseUnitIsNotActuallyABaseUnit){
							this.baseUnit = baseUnit;								
						}else{
							this.baseUnit = baseUnit.baseUnit;
						}				
						
						if(this.isBaseUnit && this != baseUnit && !isCoreUnit){ 
							this.isBaseUnit = false;
							upPropogateBaseUnitModification();
							setAutomaticUnitCategory();							
						}
					}else{ 
						setAutomaticBaseConversionPolyCoeffs = false; //No need to set conversion polynomial coefficients since the current base unit is not changed
					}
				}
				else if(this.equals(baseUnit)){
					//If the unit add itself as base unit then it becomes a base unit. Then the unit is added to the unit manager in order to notify it of this change in base unit status.
					this.baseUnit = baseUnit;
					this.isBaseUnit = true;
		
					if(unitManagerRef != null){
						unitManagerRef.addUnit(this);
					}					
				}
			
				if(setAutomaticBaseConversionPolyCoeffs){
					if(this.isBaseUnit){
						baseConversionPolyCoeffs = new double[]{1.0f, 0.0f};
					}
					else{
						setAutomaticBaseConversionPolyCoeffs(baseUnit, specifiedBaseUnitIsNotActuallyABaseUnit);
					}
					baseUnit.addConversion(this, new double[]{1.0f/baseConversionPolyCoeffs[0], -baseConversionPolyCoeffs[1]/baseConversionPolyCoeffs[0]});			
				}	
			}
			else if(this.getFundamentalUnitsExponentMap().containsKey(UNIT_TYPE.UNKNOWN)){
				this.baseUnit = baseUnit;
			}
		}else{
			this.baseUnit = baseUnit;
			
			//When either the units or it base unit have unknown component, the usual tightly enforced conjunction restriction becomes a disjunction.
			if(this.equals(baseUnit) || (baseConversionPolyCoeffs[0]==1.0f && baseConversionPolyCoeffs[1]==0.0f)){
				this.isBaseUnit = true;
			}
			else{
				this.isBaseUnit = false;
			}
		}
			
		//
		setAutomaticUnitTypeNFundmtUnitsExpMap();
		setAutomaticUnitSystem();
		
		//Update the unassosciated units in this unit's unit manager.
		if(unitManagerRef != null){
			unitManagerRef.updateAssociationsOfUnknownUnits();	
		}
	}
	public void setBaseUnit(Unit baseUnit){
		setBaseUnit(baseUnit, true);
	}	
	public void setBaseUnit(Unit baseUnit, double[] baseConversionPolyCoeffs){
		this.baseConversionPolyCoeffs = baseConversionPolyCoeffs;		
		setBaseUnit(baseUnit, true);//setBaseUnit(baseUnit, false);

	}
	
	public void setBaseConversionPolyCoeffs(double[] baseConversionPolyCoeffs){
		this.baseConversionPolyCoeffs = baseConversionPolyCoeffs;
		if(this.equals(baseUnit) && baseConversionPolyCoeffs[0]==1.0f && baseConversionPolyCoeffs[1]==0.0f){
			isBaseUnit = true;
		}
	}
	//The unit and its base are converted such that all their component units are in the same unit system. 
	//Then the conversion factor for the base unit's conversion is divided by the factor for the unit's conversion. The result of this division is the base conversion factor
	private void setAutomaticBaseConversionPolyCoeffs(Unit baseUnit, boolean specifiedBaseUnitIsNotActuallyABaseUnit){ 
		double baseUnitComponenetFactor = baseUnit.calculateComponentFactor(!specifiedBaseUnitIsNotActuallyABaseUnit, !isBaseUnit);
		
		baseConversionPolyCoeffs = new double[]{this.calculateComponentFactor(!specifiedBaseUnitIsNotActuallyABaseUnit, !isBaseUnit)
				*((this.baseUnit != baseUnit)?baseUnitComponenetFactor:1/baseUnitComponenetFactor), 0.0f};
	}
	private double calculateComponentFactor( boolean specifiedBaseUnitIsActuallyABaseUnit, boolean calculateComponentFactor){
		double factor = 1.0f;
		Unit componentUnit;
		
		if(unitManagerRef != null){
			if(!this.fundamentalUnitsExponentMap.containsKey(UNIT_TYPE.UNKNOWN)){		
				double fct = 1.0f;
				for(Entry<String, Double> entry:componentUnitsExponentMap.entrySet()){
					componentUnit = unitManagerRef.getUnit(entry.getKey(), false);
					if(componentUnit.getUnitType() == UNIT_TYPE.DERIVED_MULTI_UNIT && !componentUnit.isBaseUnit){
						fct = componentUnit.calculateComponentFactor(specifiedBaseUnitIsActuallyABaseUnit, true);
					}else if (!componentUnit.isBaseUnit){
						fct = this!=componentUnit && calculateComponentFactor ? componentUnit.getBaseUnit().calculateComponentFactor(specifiedBaseUnitIsActuallyABaseUnit, true)
								*componentUnit.getBaseConversionPolyCoeffs()[0]:1.0;
					}else{
						fct = componentUnit.baseConversionPolyCoeffs[0];
					}
					factor *= (double) Math.pow(fct, entry.getValue());
				}		
			}	
		}
				
 		return factor*(!specifiedBaseUnitIsActuallyABaseUnit?baseConversionPolyCoeffs[0]:1.0);
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
		for(String cUnitName:componentUnitsExponentMap.keySet()){
			if(secondUnit.getComponentUnitsExponentMap().containsKey(cUnitName)){ 
				newComponentUnitsExponentMap.put(cUnitName, componentUnitsExponentMap.get(cUnitName)+sign*secondUnit.getComponentUnitsExponentMap().get(cUnitName));
			}
			else{
				newComponentUnitsExponentMap.put(cUnitName, componentUnitsExponentMap.get(cUnitName));				
			}
		}
		for(String cUnitName:secondUnit.getComponentUnitsExponentMap().keySet()){
			if(!newComponentUnitsExponentMap.containsKey(cUnitName)){ 
				newComponentUnitsExponentMap.put(cUnitName, sign*secondUnit.getComponentUnitsExponentMap().get(cUnitName));			
			}
		}
		
		//Return base unit that matches new unit. Otherwise return a new unit with same dimension .
		Unit resultUnit = new Unit(newComponentUnitsExponentMap, false);		
		
		if(unitManagerRef != null){
			ArrayList<Unit> matchedUnits = unitManagerRef.getUnitsByComponentUnitsDimension(newComponentUnitsExponentMap, true);
			
			if(matchedUnits.size() > 0){
				if(!matchedUnits.get(0).getBaseUnit().getUnitName().equalsIgnoreCase(Unit.UNKNOWN_UNIT_NAME)){
					resultUnit = matchedUnits.get(0).getBaseUnit();
				}
				else{
					resultUnit = matchedUnits.get(0);
				}
			}
		}
		
		return resultUnit;		
	}
	
	///
	private void addConversion(Unit targetUnit, double[] polynomialCoeffs){ 
		//If the object is a base unit, then the conversion polynomial coefficients are simply stored as is. Otherwise adds appropriately modified conversion to base unit 
		if(isBaseUnit){
			conversionPolyCoeffs.put(targetUnit, polynomialCoeffs);
		}
		else{
			baseUnit.conversionPolyCoeffs.put(targetUnit, new double[]{1.0f/baseUnit.getBaseConversionPolyCoeffs()[0]*polynomialCoeffs[0],
															 1.0f/getBaseConversionPolyCoeffs()[1]*polynomialCoeffs[0]+polynomialCoeffs[1]});
		}
	}
	void clearConversions(){
		conversionPolyCoeffs.clear();
	}
		
	///
	public void addComponentUnit(String componentUnitName, double exponent){
		componentUnitsExponentMap.put(componentUnitName, exponent);
		setAutomaticUnitSystem();		
		setAutomaticUnitTypeNFundmtUnitsExpMap();
	}
	public void removeComponentUnit(Unit componentUnit){
		componentUnitsExponentMap.remove(componentUnit);
		setAutomaticUnitSystem();		
		setAutomaticUnitTypeNFundmtUnitsExpMap();
	}
	
	///
	public boolean equalsDimension(Unit unit){
		boolean state = false;
		
		if(this.unitManagerRef == unit.unitManagerRef){
			if(this.baseUnit == unit.baseUnit){
				state = true;
			}
			else{
				state = equalsComponentUnitsDimension(unit.getComponentUnitsExponentMap(), true);
			}
		}
		
		return state;
	} 	
	public boolean equalsFundamentalUnitsDimension(Map<UNIT_TYPE, Double> fundamentalUnitsDimension){
		//Determines if the exponents of the fundamental derivative units match
		boolean state = true;
		if(fundamentalUnitsExponentMap.size() == fundamentalUnitsDimension.size() && !fundamentalUnitsDimension.containsKey(UNIT_TYPE.UNKNOWN)
		   && !fundamentalUnitsExponentMap.containsKey(UNIT_TYPE.UNKNOWN)){
			for(Map.Entry<UNIT_TYPE, Double> entry:fundamentalUnitsExponentMap.entrySet()){
				if(!fundamentalUnitsDimension.containsKey(entry.getKey())){
					state = false;
					break; 
				}
				else if(!fundamentalUnitsDimension.get(entry.getKey()).equals(entry.getValue())){
					state = false;
					break; 
				}
			}
		}else{
			state = false;
		}
		return state;
	}
	public boolean equalsComponentUnitsDimension(Map<String, Double> componentUnitsDimension, boolean overrideToFundamentalUnitMap){
		boolean state = true;	
		
		//First determines if exponent for each component units match
		if(componentUnitsExponentMap.size() == componentUnitsDimension.size()){
			for(Map.Entry<String, Double> entry:componentUnitsExponentMap.entrySet()){
				if(!componentUnitsDimension.containsKey(entry.getKey())){
					state = false;
					break; 
				}
				else if(!componentUnitsDimension.get(entry.getKey()).equals(entry.getValue())){
					state = false;
					break; 
				}
			}
		}
		else{
			state = false;
		}

		if(!state && overrideToFundamentalUnitMap){
			//If the direct comparison of component units map fails, then performs the comparison again after converting to fundamental units map using unit manager(if present). 
			if(unitManagerRef != null){
				state = this.equalsFundamentalUnitsDimension(unitManagerRef.calculateFundmtUnitsFromCompUnitsExpMap(componentUnitsDimension));
			}
		}
		
		return state;		
	}
	
	///
	public void setUnitSystem(String unitSystemString){
		unitManagerRef.removeUnitFromHierarchy(this);
		this.unitSystem = unitSystemString.toLowerCase();
		unitManagerRef.addUnitToHierarchy(this);
	}
	public void setAbbreviation(String abbreviation){
		this.abbreviation = abbreviation;
	}
	public void setUnitCategory(String unitCategory){
		if(unitManagerRef != null)
			unitManagerRef.removeUnitFromHierarchy(this);
		
		this.unitCategory = unitCategory.toLowerCase();
		
		if(unitManagerRef != null)
			unitManagerRef.addUnitToHierarchy(this);	
	}
	public void setUnitName(String unitName){
		this.unitName = unitName.toLowerCase();
	}
	public void setDescription(String description){
		this.description = description;
	}
	
	public String getAbbreviation(){
		return abbreviation;
	}
	public String getUnitCategory(){
		return unitCategory;
	}
	public String getDescription(){
		return description;
	}
	public Unit getBaseUnit(){
		return baseUnit;
	}
	public double[] getBaseConversionPolyCoeffs(){
		return this.baseConversionPolyCoeffs;
	}
	public Map<Unit, double[]> getConversionPolyCoeffs(){
		return conversionPolyCoeffs;
	}
	public String getUnitSystem(){
		return unitSystem;
	}
	public String getUnitName(){
		return unitName;
	}
	public UnitManager.UNIT_TYPE getUnitType(){
		return unitType;
	}
	public String getDimension(){ //Gets dimension based on component units
		String dimString = "";
		
		if(componentUnitsExponentMap.size() == 1 && componentUnitsExponentMap.values().iterator().next() == 1.0){
			dimString = componentUnitsExponentMap.keySet().iterator().next();
		}
		else{
			
			for(String componentUnitName:componentUnitsExponentMap.keySet()){
				if(componentUnitsExponentMap.get(componentUnitName) == 1){
					dimString +=  componentUnitName + " * ";
				}
				else{
					dimString += "("+componentUnitName+")^"+"("+Double.toString(componentUnitsExponentMap.get(componentUnitName))+") * ";
				}
			}
			
			//Removes the terminal " * " sequence.
			if(dimString.length() > 3){
				dimString = dimString.substring(0, dimString.length()-3);
			}
		}
		
		return dimString;
	}
	public String getFundamentalDimension(){ 
		String fDimString = "";
		
		for(UNIT_TYPE fundUnit:fundamentalUnitsExponentMap.keySet()){
			if(fundamentalUnitsExponentMap.get(fundUnit) == 1){
				fDimString +=  fundUnit.name() + " * ";
			}
			else{
				fDimString += "("+fundUnit.name()+")^"+"("+Double.toString(fundamentalUnitsExponentMap.get(fundUnit))+") * ";
			}
		}
		
		//Removes the terminal " * " sequence.
		if(fDimString.length() > 3){
			fDimString = fDimString.substring(0, fDimString.length()-3);
		}
		
		return fDimString;
	}
	public Map<String, Double> getComponentUnitsExponentMap(){
		return componentUnitsExponentMap;
	}
	public Map<UnitManager.UNIT_TYPE, Double> getFundamentalUnitsExponentMap(){
		return fundamentalUnitsExponentMap;
	}
	
	public boolean isBaseUnit(){
		return isBaseUnit;
	}	
	
	///
	public void setCoreUnitState(boolean state){
		isCoreUnit = state;
	} 
	public boolean getCoreUnitState(){
		return isCoreUnit;
	}
	
	void setUnitManagerRef(UnitManager unitManager){
		this.unitManagerRef = unitManager;
		
		//Automatically update this unit's unimplemented properties based on unit manager if present		
		setAutomaticUnitTypeNFundmtUnitsExpMap();			
		if(isBaseUnit == false && this.baseUnit == null || !baseUnit.isBaseUnit){
			setAutomaticBaseUnit(true);
		}
		setAutomaticUnitSystem();
		setAutomaticUnitCategory();			
	}
	public UnitManager getUnitManagerRef(){
		return unitManagerRef;
	}
}
