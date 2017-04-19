package com.isaacapps.unitconverterapp.dao.api.rest;

import java.io.IOException;

import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.http.AndroidHttpClient;

public class RestApiRequester {

	public RestApiRequester(){
	}
	
	public JSONObject getJsonObject(String requestURI) throws ParseException, JSONException, IOException{
		return new JSONObject(EntityUtils.toString(AndroidHttpClient.newInstance("").execute(new HttpGet(requestURI)).getEntity()));
	}
}
