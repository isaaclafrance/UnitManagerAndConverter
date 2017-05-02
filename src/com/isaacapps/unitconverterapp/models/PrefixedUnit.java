package com.isaacapps.unitconverterapp.models;

import java.util.HashMap;

//Encapsulates the prefixed unit creation process
public class PrefixedUnit extends Unit {
	private String prefix;
	
	///
	public PrefixedUnit(String prefixFullName, String prefixAbbreviation, Double prefixValue, Unit unit , boolean useAbbreviation){
		super(prefixFullName+unit.getName(), unit.getCategory(), unit.getDescription(), unit.getUnitSystem(), prefixAbbreviation+unit.getAbbreviation()
			 , new HashMap<String, Double>(), unit, new double[]{prefixValue, unit.getBaseConversionPolyCoeffs()[1]});
		addComponentUnit(useAbbreviation ? unit.getAbbreviation() : unit.getName(), 1.0, false);
		prefix = prefixFullName;
	}
	
	///
	public String getPrefix(){
		return prefix;
	}
	public String getPrefixlessFullName(){
		return getName().replace(prefix, "");
	}
}
