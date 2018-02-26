package com.isaacapps.unitconverterapp.utilities;

public class ParsingException extends Exception {
	private final String scenario, howToFix;
	
	public ParsingException(String scenario, String howToFix){
		this.scenario = scenario;
		this.howToFix = howToFix;
	}
	
	public String getScenario(){
		return scenario;
	}
	
	public String getHowToFix(){
		return howToFix;
	}
	
	@Override
	public String toString(){
		return String.format("**Parsing scenario that caused problem: %s \n **The follwing can resolve the problem: %s", scenario, howToFix);
	}
}
