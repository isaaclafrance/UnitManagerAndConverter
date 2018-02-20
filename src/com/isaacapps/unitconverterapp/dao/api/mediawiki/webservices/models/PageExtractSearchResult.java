package com.isaacapps.unitconverterapp.dao.api.mediawiki.webservices.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

public class PageExtractSearchResult extends JSONObject{
	
	public PageExtractSearchResult(String jsonReponse) throws JSONException{
		super(jsonReponse);
	}
	public PageExtractSearchResult(){
		super();
	}

	public List<PageExtract> getPageExtracts(){
		try {
			if(hasContent()){
				JSONObject pagesJObj = getJSONObject("query").getJSONObject("pages");
				List<PageExtract> pageExtracts = new ArrayList<PageExtract>();
				
				Iterator<String>  pageIds = pagesJObj.keys();
				while(pageIds.hasNext()){
					pageExtracts.add(new PageExtract(getString(pageIds.next())));
				}
				
				return pageExtracts;
			}
			else{
				return Collections.EMPTY_LIST;
			}
			
		} catch (JSONException e) {
			return Collections.EMPTY_LIST;
		}
	}
	
	public boolean hasContent(){
		return length() != 0;
	}
}
