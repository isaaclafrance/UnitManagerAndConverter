package com.isaacapps.unitconverterapp.dao.api.rest.mediawiki.models;

import org.json.JSONException;
import org.json.JSONObject;

public class PageSummary extends JSONObject{
	
	public PageSummary(String jsonReponse) throws JSONException{
		super(jsonReponse);
	}

}
