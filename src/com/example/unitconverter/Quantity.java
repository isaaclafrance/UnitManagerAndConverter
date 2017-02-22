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
		ArrayList<Unit> matchingUnits =  unitManager.getUnitsByComponentUnitsDimension(unitDimensionString, false);
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

		ArrayList<Unit> matchingUnits =  unitManager.getUnitsByComponentUnitsDimension(unitString, false);
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
			newQuantity = secondQuantity.convertToUnit(this.unit);
			newQuantity.setValue(this.value + sign*newQuantity.value);
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
	public Quantity convertToUnit(Unit targetUnit){
		double[] conversionFactor = unitManagerRef.getConversionFactorToTargetUnit(unit,targetUnit);
		return new Quantity( this.value * conversionFactor[0] + conversionFactor[1],  targetUnit);
	}

	public Quantity toUnitSystem(String targetUnitSystemString){
		//Convert every component unit to one unit system. Makes sure to appropriately scale the value when returning new quantity.	Only uses current unit manager to perform conversion.	
		double value = 0.0;
		Unit targetUnit = new Unit();
		if(unitManagerRef != null){
			if(unit.getType() != UNIT_TYPE.UNKNOWN){
				ArrayList<Unit> correspondingUnits = unitManagerRef.getCorrespondingUnitsWithUnitSystem(unit, targetUnitSystemString);
				
				if(correspondingUnits.size()>0){
					targetUnit = correspondingUnits.get(0);
				}
				
				double conversionFactor = unitManagerRef.getConversionFactorToUnitSystem(unit, targetUnitSystemString)[0];
				
				if(conversionFactor > 0){
					value = this.value * conversionFactor; 
				}	
			}
		}
		return new Quantity(value, targetUnit);
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
		return String.format("%f %s", String.valueOf(value), unit.getName());
	}
}
