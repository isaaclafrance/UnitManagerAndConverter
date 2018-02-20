package com.isaacapps.unitconverterapp.utilities;

public class ParsingException extends Exception {
	private final String itemBeingParsed, howToFix;
	
	public ParsingException(String itemBeingParsed, String howToFix){
		this.itemBeingParsed = itemBeingParsed;
		this.howToFix = howToFix;
	}
	
	public String getItemBeingParsed(){
		return itemBeingParsed;
	}
	
	public String getHowToFix(){
		return howToFix;
	}
	
	@Override
	public String toString(){
		return String.format("**Parsed item that caused problem: %s \n **This can be resolved by: %s", itemBeingParsed, howToFix);
	}
}
