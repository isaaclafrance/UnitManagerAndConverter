package com.example.unitconverter;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.unitconverter.UnitManager.UNIT_TYPE;

public class Quantity {
	//Fields
	private UnitManager unitManagerRef;
	private double value;
	private Unit unit;
	
	//Constructors
	public Quantity(){
		this.value = 0.0f;
		this.unit = new Unit();
		this.unitManagerRef = unit.getUnitManagerRef();
	}
	public Quantity(double value, Unit unit){
		this.value = value;
		this.unit = unit;
		this.unitManagerRef = unit.getUnitManagerRef();
	}
	public Quantity(double value){
		this.value = value;
		this.unit = new Unit();
		this.unitManagerRef = unit.getUnitManagerRef();
	}
	public Quantity(Unit unit){
		this.value = 0.0f;
		this.unit = unit;
		this.unitManagerRef = unit.getUnitManagerRef();
	}
	public Quantity(double value, String unitDimensionString, UnitManager unitManager){
		this.unitManagerRef = unitManager;
		this.value = value;
		ArrayList<Unit> matchingUnits =  unitManager.getUnitsByComponentUnitsDimension(unitDimensionString);
		this.unit = matchingUnits.get(0);	
	}	
	public Quantity(String valueNUnitString, UnitManager unitManager){
		this.unitManagerRef = unitManager;
		Pattern valueRegExPattern = Pattern.compile("^([\\s]*((\\d*[.])?\\d+)[\\s]+)");
		Matcher valueRegExMatcher = valueRegExPattern.matcher(valueNUnitString);
		String unitString;
		
		if(valueRegExMatcher.find()){
			unitString = valueNUnitString.substring(valueRegExMatcher.end());
			String valueString = valueNUnitString.substring(0, valueRegExMatcher.end());
			value = Double.valueOf(valueString);
			
		}
		else{
			unitString = Unit.UNKNOWN_UNIT_NAME;
			value = 0.0f;
		}

		ArrayList<Unit> matchingUnits =  unitManager.getUnitsByComponentUnitsDimension(unitString);
		this.unit = matchingUnits.get(0);		
	}
	
	///
	public Quantity add(Quantity secondQuantity){
		return quantityOperation(1, secondQuantity);
	}
	public Quantity subtract(Quantity secondQuantity){
		return quantityOperation(-1, secondQuantity);
	}
	private Quantity quantityOperation(int sign, Quantity secondQuantity){
		Quantity newQuantity;
	 	
		if(this.unit.equalsDimension(secondQuantity.unit)){
			newQuantity = new Quantity(this.value+ sign*secondQuantity.value, this.unit);
		}
		else{
			if(unitManagerRef != null){
				newQuantity = new Quantity(unitManagerRef.getUnit(Unit.UNKNOWN_UNIT_NAME));
			}
			else{
				newQuantity = new Quantity();
			}
		}
		return newQuantity;		
	}
	
	public Quantity multiply(Quantity secondQuantity){
		return new Quantity(this.value*secondQuantity.value, this.unit.multiply(secondQuantity.unit));
	}
	public Quantity divide(Quantity secondQuantity){
		return new Quantity(this.value/secondQuantity.value, this.unit.divide(secondQuantity.unit));
	}
	
	///
	public void convertToUnit_Self(Unit targetUnit){
		if(this.unit.equalsDimension(targetUnit)){
			this.value = this.value * unitManagerRef.getConversionFactorToTargetUnit(unit,targetUnit)[0];
			this.unit = targetUnit;
		}
	}
	public Quantity convertToUnit(Unit targetUnit){
		double newValue;
		
		if(unit.equalsDimension(targetUnit)){
			if(unit.getComponentUnitsExponentMap().keySet().size() == 1 && this.unit.getComponentUnitsExponentMap().entrySet().iterator().next().getValue() == 1){
				newValue = value * unitManagerRef.getConversionFactorToTargetUnit(unit, targetUnit)[0];
			}
			else{
				newValue = (value * unit.getBaseConversionPolyCoeffs()[0] + unit.getBaseConversionPolyCoeffs()[1] - targetUnit.getBaseConversionPolyCoeffs()[1])/targetUnit.getBaseConversionPolyCoeffs()[0];
			}
			return new Quantity(newValue, targetUnit);
		}
		else{
			return new Quantity(0.0f, targetUnit);
		}
	}
	public void toUnitSystem_Self(String targetUnitSystemString){
		//Convert every component unit to one unit system. Makes sure to appropriately scale the value when returning new quantity.	Only uses current unit manager to perform conversion.	
		if(unitManagerRef != null){
			if(unit.getUnitType() != UNIT_TYPE.UNKNOWN){
				ArrayList<Unit> correspondingUnits = unitManagerRef.getCorrespondingUnitsWithUnitSystem(unit, targetUnitSystemString);
				
				if(correspondingUnits.size()>0){
					unit = correspondingUnits.get(0);
				}
				
				double conversionFactor = unitManagerRef.getConversionFactorToUnitSystem(unit, targetUnitSystemString)[0];
				
				if(conversionFactor > 0){
					value = this.value * conversionFactor; 
				}	
			}
		}
	}
	
	///
	public boolean equalsValue(Quantity otherQuantity){
		return (value == otherQuantity.getValue());
	}
	public boolean equalsUnit(Quantity otherQuantity){
		return (unit.equalsDimension(otherQuantity.getUnit()) );
	}
	public boolean equalsValueNUnit(Quantity otherQuantity){
		return equalsValue(otherQuantity)&&equalsUnit(otherQuantity);
	}
	
	///
	public double getValue(){
		return value;
	}
	public Unit getUnit(){
		return unit;
	}
	
	public void setUnit(Unit unit){
		this.unit = unit;
		this.unitManagerRef = unit.getUnitManagerRef();
	}
	public void setValue(double value){
		this.value = value;
	}
	
	///
	public UnitManager getUnitManagerRef(){
		return unitManagerRef;
	}
	
	///
	@Override
	public String toString(){
		return String.format("%f %s", String.valueOf(value), unit.getUnitName());
	}
}
