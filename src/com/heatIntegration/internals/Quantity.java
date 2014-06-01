package com.heatIntegration.internals;

import com.heatIntegration.internals.UNIT_SYSTEMS.UNKNOWN_SYSTEM.UNKNOWN_UNITS;
import com.heatIntegration.internals.UnitManager.UNIT_TYPE;

public class Quantity {
	//Fields
	public float value;
	public UnitClass unit;

	//Constructors
	public Quantity(){
		this.value = 0.0f;
		this.unit = UNKNOWN_UNITS.UNKNOWN_UNIT.getUnit();
	}
	public Quantity(float value, UnitClass unit){
		this.value = value;
		this.unit = unit;
	}
	public Quantity(float value){
		this.value = value;
		this.unit = UNKNOWN_UNITS.UNKNOWN_UNIT.getUnit();
	}
	
	///
	public Quantity add(Quantity secondQuantity){
		if(this.unit.equalsUnitTypeNDimension(secondQuantity.unit)){
			return new Quantity(this.value+secondQuantity.value, this.unit);
		}
		else{
			return new Quantity();
		}
	}
	public Quantity subtract(Quantity secondQuantity){
		if(this.unit.equalsUnitTypeNDimension(secondQuantity.unit)){
			return new Quantity(this.value-secondQuantity.value, this.unit);
		}
		else{
			return new Quantity();
		}
	}
	
	public Quantity multiply(Quantity secondQuantity){
		return new Quantity(this.value*secondQuantity.value, this.unit.multiply(secondQuantity.unit));
	}
	public Quantity divide(Quantity secondQuantity){
		return new Quantity(this.value*secondQuantity.value, this.unit.divide(secondQuantity.unit));
	}
	
	///
	public void convertToUnitSelf(UnitClass targetUnitType){
		if(this.unit.equalsDimension(targetUnitType)){
			this.value = this.value * unit.getConversionFactorToTargetUnit(targetUnitType)[0];
			this.unit = targetUnitType;
		}
	}
	public Quantity convertToUnit(UnitClass targetUnitType){
		if(this.unit.equalsDimension(targetUnitType)){
			float value = this.value * unit.getConversionFactorToTargetUnit(targetUnitType)[0];
			return new Quantity(value, targetUnitType);
		}
		else{
			return new Quantity();
		}
	}
	public void replaceComponentUnitSelf(UnitClass componentUnitReplaced, UnitClass componentUnitReplacement){
		value = value*unit.getConversionFactorFromCompUnitReplace(componentUnitReplaced, componentUnitReplacement);
		unit = unit.getUnitFromCompUnitReplace(componentUnitReplaced, componentUnitReplacement);
	}
	public Quantity getQuantityFromReplacedComponentUnit(UnitClass componentUnitReplaced, UnitClass componentUnitReplacement){
		float value = this.value*this.unit.getConversionFactorFromCompUnitReplace(componentUnitReplaced, componentUnitReplacement);
		UnitClass unit = this.unit.getUnitFromCompUnitReplace(componentUnitReplaced, componentUnitReplacement);
		
		return new Quantity(value, unit);
	}	
	public void toUnitSystemSelf(String unitSystemString){
		//Convert every component unit to one unit system. Makes sure to appropriately scale the the value.
		if(unit.getUnitType() != UNIT_TYPE.UNKNOWN){
			value = value * unit.toUnitSystem_Factor(unitSystemString);
			unit = unit.toUnitSystem_Unit(unitSystemString);
		}
	}
	public Quantity toUnitSystem(String unitSystemString){
		//Convert every component unit to one unit system. Makes sure to appropriately scale the value when returning new quantity.
		if(unit.getUnitType() != UNIT_TYPE.UNKNOWN){
			float value = this.value * this.unit.toUnitSystem_Factor(unitSystemString);
			UnitClass unit = this.unit.toUnitSystem_Unit(unitSystemString);
			
			return new Quantity(value, unit);
		}
		else{
			return new Quantity();
		}
	}
	
	///
	public boolean equalsValue(Quantity otherQuantity){
		return (value == otherQuantity.getValue());
	}
	public boolean equalsUnit(Quantity otherQuantity){
		return (unit == otherQuantity.getUnit());
	}
	public boolean equalsValueNUnit(Quantity otherQuantity){
		return equalsValue(otherQuantity)&&equalsUnit(otherQuantity);
	}
	
	///
	public float getValue(){
		return value;
	}
	public UnitClass getUnit(){
		return unit;
	}
	
	public void setUnit(UnitClass unit){
		this.unit = unit;
	}
	public void setValue(float value){
		this.value = value;
	}
	
	///
	public String getString(){
		return String.format("%f %s", String.valueOf(value), unit.getUnitNameString());
	}
}
