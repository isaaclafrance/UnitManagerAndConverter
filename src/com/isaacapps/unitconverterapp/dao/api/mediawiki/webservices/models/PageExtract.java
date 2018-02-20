package com.isaacapps.unitconverterapp.dao.api.mediawiki.webservices.models;

import org.json.JSONObject;
import org.json.JSONException;

public class PageExtract extends JSONObject {

	public PageExtract(String jsonResponse) throws JSONException{
		super(jsonResponse);
	}
	
	public String getTitle(){
		try {
			return getString("title");
		} catch (JSONException e) {
			return "";
		}
	}
	
	public String getExtract(){
		try {
			return getString("extract");
		} catch (JSONException e) {
			return "";
		}
	}
	
	public long getPageId(){
		try {
			return getLong("pageid");
		} catch (JSONException e) {
			return -1;
		}
	}
	
	public String getWikipediaReferenceUrl(){
		return "https://en.wikipedia.org/wiki/"+getTitle();
	}
	
	public boolean hasContent(){
		return length() != 0;
	}
}
