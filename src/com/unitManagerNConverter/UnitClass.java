package com.heatIntegration.internals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.heatIntegration.internals.UnitManager.UNIT_TYPE;

public class UnitClass{
	//Fields
	private UnitManager unitManagerRef;
	
	private String unitSystemString;
	private UnitManager.UNIT_TYPE unitType; //TODO: Implement methods that set update/or assign unit type properly
	private boolean isBaseUnit; //Is set to true if baseFactorConversion is set to values 1 or 0.
	private UnitClass baseUnit;
	private String unitNameString;
	private String abbreviationString;
	private Map<UnitClass, Float[]> conversionPolyCoeffs; //Contains functions that enable base unit to convert to other units of same dimension that are not necessarily proportionally related.
	private Float[] baseConversionPolyCoeffs; //Function used to convert current unit to base unit
	private Map<UnitClass, Float> componentUnitsExponentMap; //Map that associates derivative component units with the value of their exponential power	
	private Map<UnitManager.UNIT_TYPE, Float> fundamentalUnitsExponentMap; //Map that associates derivative component fundamental units with the value of their exponential power	
	
	///Constructors
	public UnitClass(){
		unitManagerRef = UnitManager.getInstance(); 
		conversionPolyCoeffs = new HashMap<UnitClass, Float[]>();
		componentUnitsExponentMap = new HashMap<UnitClass, Float>();
		fundamentalUnitsExponentMap = new HashMap<UnitManager.UNIT_TYPE, Float>();
		unitNameString = "Unknown_Unit";
		baseUnit = this;
		setAutomaticBaseUnit(false);
		setAutomaticUnitTypeNFundmtUnitsExpMap();
		setAutomaticUnitSystemString();
	}
	public UnitClass(Map<UnitClass, Float> componentUnitsExponentMap){
		this();	
		this.abbreviationString = unitNameString.substring(0, 3);
		this.componentUnitsExponentMap = componentUnitsExponentMap;
		this.unitNameString = getDimensionAsString();		
		this.isBaseUnit = true;
		setAutomaticBaseUnit(true);
		setAutomaticUnitTypeNFundmtUnitsExpMap();
		setAutomaticUnitSystemString();		
	}
	public UnitClass(Map<UnitClass, Float> componentUnitsExponentMap, float baseConversionFactor){
		this();
		this.abbreviationString = unitNameString.substring(0, 3);
		this.componentUnitsExponentMap = componentUnitsExponentMap;
		this.unitNameString = getDimensionAsString();			
		this.isBaseUnit = true;
		setAutomaticBaseUnit(false);
		setAutomaticUnitTypeNFundmtUnitsExpMap();
		setAutomaticUnitSystemString();
	}
	public UnitClass(String unitNameString, Map<UnitClass, Float> componentUnitsExponentMap){
		this();
		this.unitNameString = unitNameString;	
		this.abbreviationString = unitNameString.substring(0, 3);
		this.componentUnitsExponentMap = componentUnitsExponentMap;
		this.isBaseUnit = true;
		setAutomaticBaseUnit(true);
		setAutomaticUnitTypeNFundmtUnitsExpMap();
		setAutomaticUnitSystemString();
	}
	public UnitClass(String unitNameString, Map<UnitClass, Float> componentUnitsExponentMap, float baseConversionFactor){
		this();
		this.unitNameString = unitNameString;
		this.abbreviationString = unitNameString.substring(0, 3);
		this.componentUnitsExponentMap = componentUnitsExponentMap;		
		if(baseConversionFactor == 1){
			isBaseUnit = true;	
		}
		else{
			isBaseUnit = false;
		}
		this.baseConversionPolyCoeffs = new Float[]{baseConversionFactor, 0.0f};		
		setAutomaticBaseUnit(false);
		setAutomaticUnitTypeNFundmtUnitsExpMap();
		setAutomaticUnitSystemString();
	}
	public UnitClass(String unitNameString, String unitSystemString, String abbreviationString, Map<UnitClass, Float> componentUnitsExponentMap, Map<UnitClass, Float[]> conversionPolyCoeffs, UnitClass baseUnit, Float[] baseConversionPolyCoeffs){
		this();
		this.unitNameString = unitNameString;
		this.unitSystemString = unitSystemString;
		this.abbreviationString = abbreviationString;
		this.componentUnitsExponentMap = componentUnitsExponentMap;
		this.conversionPolyCoeffs = conversionPolyCoeffs;
		if(baseConversionPolyCoeffs[0] == 1 && baseConversionPolyCoeffs[1] != 0){ 
			isBaseUnit = true;	
		}
		else{
			isBaseUnit = false;
		}
		this.baseConversionPolyCoeffs = baseConversionPolyCoeffs;		
		this.baseUnit = baseUnit;
		setAutomaticUnitTypeNFundmtUnitsExpMap();
		setAutomaticUnitSystemString();
	}
	
	///	
	private void setAutomaticUnitSystemString(){
		//If contains component are both SI and US, then string is "SI and US". However, if the component are not recognizable, then set string to "Unknown Unit System(s)."
		ArrayList<String> unitSystemsOfComponentsArrayList = new ArrayList<String>();
		
		for(UnitClass componentUnit:componentUnitsExponentMap.keySet()){
			if(!unitSystemsOfComponentsArrayList.contains(componentUnit.getUnitNameString())){
				unitSystemsOfComponentsArrayList.add(componentUnit.getUnitSystemString());
			}
		}
		
		if(unitSystemsOfComponentsArrayList.size() == 0){
			unitSystemString = "Unknown Unit System(s)";
		}
		else{
			for(String str:unitSystemsOfComponentsArrayList){
				unitSystemString += (" and "+str);				
			}
		}
	}
	private void setAutomaticUnitTypeNFundmtUnitsExpMap(){
		fundamentalUnitsExponentMap = calculateFundmtUnitsExpMap(this);
		unitType = unitManagerRef.determineUnitType(this);
	}
	private Map<UNIT_TYPE, Float> calculateFundmtUnitsExpMap(UnitClass unknownUnit){
		Map<UNIT_TYPE, Float> map = new HashMap<UnitManager.UNIT_TYPE, Float>();
				
		//Goes through each component unit whether derived or and sums up the recursively obtained total occurances of the fundamental units. Makes sure to multiply those totals by the exponent of the component unit.
		for(UnitClass componentUnit:unknownUnit.getComponentUnitsExponentMap().keySet()){
			if(componentUnit.getUnitType() == UNIT_TYPE.DERIVED){
				for(UNIT_TYPE unitType:UNIT_TYPE.values()){
					Map<UNIT_TYPE, Float> recursedMap = calculateFundmtUnitsExpMap(componentUnit);					
					
					//Initializes exponent for a fundamental unit only when it is need
					if(recursedMap.containsKey(unitType)){ 
						map.put(unitType, 0.0f);
					}
					
					map.put(unitType,  
							map.get(unitType)+ unknownUnit.getComponentUnitsExponentMap().get(componentUnit)*recursedMap.get(unitType));
				}
			}
			else{
				map.put(componentUnit.getUnitType(), map.get(componentUnit.getUnitType()) + unknownUnit.getComponentUnitsExponentMap().get(componentUnit));
			}
		}
		
		return map;
	}
	
	public void setAutomaticBaseUnit(boolean calculateBaseCoversionPolyCoeffs){ //Automatically finds and sets base unit from Unit Manager directory
		this.baseUnit = unitManagerRef.getBaseUnit(this);
		
		if(calculateBaseCoversionPolyCoeffs){
			this.baseConversionPolyCoeffs = unitManagerRef.calculateBaseConverPolyCoeffs(this);//.calculateBaseConverPolyCoeffs(this);
		}
	}
	public void setBaseUnit(UnitClass baseUnit){
		//Makes sure that a non-base unit is not assigned as a base unit
		if(baseUnit.isBaseUnit){
			this.baseUnit = baseUnit;
		}
		else{
			this.baseUnit = baseUnit.getBaseUnit();
		}
	}
	public void setBaseUnit(UnitClass baseUnit, Float[] baseConversionPolyCoeffs){
		setBaseUnit(baseUnit);
		this.baseConversionPolyCoeffs = baseConversionPolyCoeffs;
	}
	public void setBaseConversionPolyCoeffs(Float[] baseConversionPolyCoeffs){
		this.baseConversionPolyCoeffs = baseConversionPolyCoeffs;
	}

	///
	public UnitClass multiply(UnitClass secondArgUnitType){
		Map<UnitClass, Float> newComponentUnitsExponentMap = new HashMap<UnitClass, Float>();
		
		//Adds the exponents of component units that are the same.
		for(UnitClass cUnit:componentUnitsExponentMap.keySet()){
			if(secondArgUnitType.getComponentUnitsExponentMap().containsKey(cUnit)){ 
				newComponentUnitsExponentMap.put(cUnit, componentUnitsExponentMap.get(cUnit)+secondArgUnitType.getComponentUnitsExponentMap().get(cUnit));
			}
			else{
				newComponentUnitsExponentMap.put(cUnit, componentUnitsExponentMap.get(cUnit));				
			}
		}
		for(UnitClass cUnit:secondArgUnitType.getComponentUnitsExponentMap().keySet()){
			if(!newComponentUnitsExponentMap.containsKey(cUnit)){ 
				newComponentUnitsExponentMap.put(cUnit, secondArgUnitType.getComponentUnitsExponentMap().get(cUnit));			
			}
		}
		
		//Return base unit that matches new unit. Otherwise return the unknown new unit  .
		UnitClass newUnit = new UnitClass("Unknown_Unit", newComponentUnitsExponentMap);		
		
		if(unitManagerRef.containsUnit(newUnit)){
			return unitManagerRef.getBaseUnit(newUnit);
		}
		else{
			return newUnit;
		}
	}	
	public UnitClass divide(UnitClass secondArgUnitType){
		Map<UnitClass, Float> newComponentUnitsExponentMap = new HashMap<UnitClass, Float>();
		
		//Subtracts the exponents of component units that are the same.
		for(UnitClass cUnit:componentUnitsExponentMap.keySet()){
			if(secondArgUnitType.getComponentUnitsExponentMap().containsKey(cUnit)){ 
				newComponentUnitsExponentMap.put(cUnit, componentUnitsExponentMap.get(cUnit)-secondArgUnitType.getComponentUnitsExponentMap().get(cUnit));
			}
			else{
				newComponentUnitsExponentMap.put(cUnit, componentUnitsExponentMap.get(cUnit));				
			}
		}
		for(UnitClass cUnit:secondArgUnitType.getComponentUnitsExponentMap().keySet()){
			if(!newComponentUnitsExponentMap.containsKey(cUnit)){ 
				newComponentUnitsExponentMap.put(cUnit, -secondArgUnitType.getComponentUnitsExponentMap().get(cUnit));			
			}
		}
		
		//Return base unit that matches new unit. Otherwise return the unknown new unit  .
		UnitClass newUnit = new UnitClass("Unknown_Unit", newComponentUnitsExponentMap);		
		
		if(unitManagerRef.containsUnit(newUnit)){
			return unitManagerRef.getBaseUnit(newUnit);
		}
		else{
			return newUnit;
		}
	}
		
	///
	public void addConversions(Map<UnitClass, Float[]> conversionPolynomialMap){
		for(Entry<UnitClass, Float[]> entry:conversionPolynomialMap.entrySet()){
			addConversion(entry.getKey(), entry.getValue());
		}
	}
	public void addConversion(UnitClass targetUnit, Float[] polynomialCoeffs){ 
		//If the object is a base unit, then the conversion polynomial coefficients are simply stored as it. Otherwise adds appropriately modified conversion to base unit 
		if(isBaseUnit){
			conversionPolyCoeffs.put(targetUnit, polynomialCoeffs);
		}
		else{
			conversionPolyCoeffs.put(targetUnit, new Float[]{baseUnit.getBaseConversionPolyCoeffs()[0]*polynomialCoeffs[0],
															 baseUnit.getBaseConversionPolyCoeffs()[1]*polynomialCoeffs[0]+polynomialCoeffs[1]});
		}
	}
	public void removeConversion(UnitClass targetUnit){
		if(isBaseUnit){
			conversionPolyCoeffs.remove(targetUnit);
		}
		else{
			baseUnit.removeConversion(targetUnit);
		}
	}
	
	///
	public void addComponentUnits(Map<UnitClass, Float> componentUnitsMap){
		for(Entry<UnitClass, Float> entry:componentUnitsMap.entrySet()){
			addComponentUnit(entry.getKey(), entry.getValue());
		}
	}
	public void addComponentUnit(UnitClass componentUnit, float exponent){
		componentUnitsExponentMap.put(componentUnit, exponent);
		setAutomaticUnitTypeNFundmtUnitsExpMap();
		setAutomaticUnitSystemString();
	}
	public void removeComponentUnit(UnitClass componentUnit){
		componentUnitsExponentMap.remove(componentUnit);
		setAutomaticUnitTypeNFundmtUnitsExpMap();
		setAutomaticUnitSystemString();
	}
	
	////
	public boolean equalsDimension(UnitClass unknownUnit){
		return equalsFundamentalUnitsDimension(unknownUnit.getFundamentalUnitsExponentMap());
	} 	
	public boolean equalsFundamentalUnitsDimension(Map<UNIT_TYPE, Float> fundamentalUnitsDimension){
		//Determines if the exponents of the fundamental derivative units match
		boolean state = true;
		
		for(Map.Entry<UNIT_TYPE, Float> entry:fundamentalUnitsDimension.entrySet()){
			if(!fundamentalUnitsExponentMap.containsKey(entry.getKey())){
				return false;
			}
			else{
				state = state && (fundamentalUnitsExponentMap.get(entry.getKey()) == entry.getValue());
			}
		}
		
		return state;
	}
	public boolean equalsComponentUnitsDimension(Map<UnitClass, Float> componentUnitsDimension){
		//Uses component units dimension to a unit. Then compares fundamental dimensions of that unit with this unit.
		boolean state = true;
		UnitClass associatedUnit = new UnitClass(componentUnitsDimension, 0.0f);
		
		for(Map.Entry<UNIT_TYPE, Float> entry:associatedUnit.fundamentalUnitsExponentMap.entrySet()){
			if(!fundamentalUnitsExponentMap.containsKey(entry.getKey())){
				return false;
			}
			else{
				state = state && (fundamentalUnitsExponentMap.get(entry.getKey()) == entry.getValue());
			}
		}
		
		return state;
	}
	public boolean equalsUnitTypeNDimension(UnitClass unknownUnit){
		boolean state = (unknownUnit.unitNameString == unitNameString) && equalsDimension(unknownUnit);
		
		return state;
	}
	
	///
	public void setUnitSystemString(String unitSystemString){
		this.unitSystemString = unitSystemString;
	}
	//public void setUnitManagerRef(UnitManager unitManager){ //Unnecessary if unit manager is a singleton
		//this.unitManagerRef = unitManager;
	//}
	public void setAbbreviationString(String abbreviation){
		this.abbreviationString = abbreviation;
	}
	public void setUnitNameString(String unitName){
		this.unitNameString = unitName;
	}
	
	public UnitClass getCopy(){
		UnitClass newUnit = new UnitClass(unitNameString, unitSystemString, abbreviationString, componentUnitsExponentMap, conversionPolyCoeffs, baseUnit, baseConversionPolyCoeffs);
		newUnit.unitManagerRef = unitManagerRef;
		return newUnit;
	}
	public String getAbbreviationString(){
		return abbreviationString;
	}
	public UnitClass getBaseUnit(){
		return baseUnit;
	}
	public Float[] getBaseConversionPolyCoeffs(){
		return this.baseConversionPolyCoeffs;
	}
	public String getUnitSystemString(){
		return unitSystemString;
	}
	public String getUnitNameString(){
		return unitNameString;
	}
	public UnitManager.UNIT_TYPE getUnitType(){
		return unitType;
	}
	public String getDimensionAsString(){ //Gets dimension based on component units
		String dimString = "";
		
		for(UnitClass componentUnit:componentUnitsExponentMap.keySet()){
			dimString += "("+componentUnit.getUnitNameString()+")^"+( (componentUnitsExponentMap.get(componentUnit)==1)?"":componentUnitsExponentMap.get(componentUnit));
		}
		
		return dimString;
	}
	public String getFundamentalDimension(){ //Gets dimension based on fundamental units
		String fDimString = "";
		
		for(UNIT_TYPE fundamentalUnit:fundamentalUnitsExponentMap.keySet()){
			fDimString += "("+fundamentalUnit.name()+")^"+( (componentUnitsExponentMap.get(fundamentalUnit.name())==1)?"":componentUnitsExponentMap.get(fundamentalUnit));
		}
		
		return fDimString;
	}
	public Map<UnitClass, Float> getComponentUnitsExponentMap(){
		return componentUnitsExponentMap;
	}
	public Map<UnitManager.UNIT_TYPE, Float> getFundamentalUnitsExponentMap(){
		return fundamentalUnitsExponentMap;
	}
	
	public boolean isBaseUnit(){
		return isBaseUnit;
	}	
	
	//
	public float[] getConversionFactorToTargetUnit(UnitClass targetUnit){
		if(this.equalsDimension(targetUnit) && targetUnit.baseUnit.getUnitType() != UNIT_TYPE.UNKNOWN){
			if(this.baseConversionPolyCoeffs[1]==0.0f){
				return new float[]{targetUnit.getBaseConversionPolyCoeffs()[0] / this.baseConversionPolyCoeffs[0], 0.0f};				
			}
			else{
				return new float[]{targetUnit.getBaseConversionPolyCoeffs()[0] / this.baseConversionPolyCoeffs[0],
								  (targetUnit.getBaseConversionPolyCoeffs()[1]-this.baseConversionPolyCoeffs[1])/this.baseConversionPolyCoeffs[0]};
			}
		}
		else{
			return null;
		}
	}
	public float getConversionFactorFromCompUnitReplace(UnitClass replacedUnit, UnitClass replacementUnit){
		//Determine the conversion factor resulting from replacing a component unit with another one. As of the moment does not account for complex conversions involving constants.
		if(this.getComponentUnitsExponentMap().keySet().contains(replacedUnit)&&
		   this.equalsDimension(replacementUnit)){
		 	return (float)Math.pow(replacedUnit.getConversionFactorToTargetUnit(replacementUnit)[0], this.getComponentUnitsExponentMap().get(replacedUnit));
		}
		else{
			return 1.0f;
		}
	}
	public UnitClass getUnitFromCompUnitReplace(UnitClass replacedUnit, UnitClass replacementUnit){
		UnitClass newUnit = this.getCopy();
		newUnit.getComponentUnitsExponentMap().put(replacementUnit, getComponentUnitsExponentMap().get(replacedUnit));
		newUnit.getComponentUnitsExponentMap().remove(replacedUnit);
		
		return newUnit;
	}
	
	//
	public UnitClass toUnitSystem_Unit(String unitSystemString){
		//Find a way to convert every component unit to one unit system. Then return resulting unit or return self.
		
		if(this.unitSystemString.contains(unitSystemString) && !this.unitSystemString.contains(" and ")){
			//If this unit already has all component units in proper unit system, then return this unit.
			return this; 
		}
		else{
			//Find replacement componentUnits in order to determine proper unit dimension associated with proper unitSystem.
			UnitClass replacementUnit = null;
			Map<UnitClass, Float> properComponentUnitDimension = new HashMap<UnitClass, Float>();
			
			for(Entry<UnitClass, Float> componentUnitEntry:componentUnitsExponentMap.entrySet()){
				replacementUnit = componentUnitEntry.getKey().toUnitSystem_Unit(unitSystemString);
				
				for(UnitClass unitWithSameDimension:unitManagerRef.getUnitsFromComponentUnitsDimension(componentUnitEntry.getKey().getComponentUnitsExponentMap()) ){
					if(unitWithSameDimension.unitSystemString.equalsIgnoreCase(unitSystemString)){
						replacementUnit = unitWithSameDimension;
						break;
					}
				}
				properComponentUnitDimension.put(replacementUnit, componentUnitEntry.getValue());
			}
			
			//See if unitManager already contains unit with proper dimension and return that unit. Otherwise return a new unit with proper dimensions.
			ArrayList<UnitClass> correspondingUnitsFromManager = unitManagerRef.getUnitsFromComponentUnitsDimension(properComponentUnitDimension);
			if(correspondingUnitsFromManager.size() != 0){
				return correspondingUnitsFromManager.get(0);
			}
			else{
				return new UnitClass(properComponentUnitDimension);
			}
		}
	}
	public float toUnitSystem_Factor(String unitSystemString){
		//Return the factor by which the unit changes when every component unit is converted to unit system
		
		if(this.unitSystemString.contains(unitSystemString) && !this.unitSystemString.contains(" and ")){
			//If this unit already has all component units in proper unit system, then return factor of one.
			return 1.0f; 
		}
		else{
			//Find product of conversion factors associated with the conversion of the component units to a similar of unit of proper unit system.
			float conversionFactor = 1.0f;
			UnitClass replacementUnit = null;

			for(Entry<UnitClass, Float> componentUnitEntry:componentUnitsExponentMap.entrySet()){
				replacementUnit = componentUnitEntry.getKey().toUnitSystem_Unit(unitSystemString);				
				conversionFactor = (float) Math.pow(componentUnitEntry.getKey().getConversionFactorToTargetUnit(replacementUnit)[0],
											componentUnitEntry.getValue());
			}
			
			return conversionFactor;
		}
	}
}
