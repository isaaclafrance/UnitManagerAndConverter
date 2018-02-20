package com.isaacapps.unitconverterapp.dao.api.mediawiki.webservices;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONException;

import com.isaacapps.unitconverterapp.dao.api.mediawiki.webservices.models.PageExtractSearchResult;
import com.isaacapps.unitconverterapp.dao.api.ApiRequester;

public class MediaWikiWebServicesApiRequester{
	
	private static String BASE_URL = "https://en.wikipedia.org/w/api.php?format=json";
		
	public static PageExtractSearchResult searchPageExtracts(String searchTerm){
		try {
			String actionParameter = "&action=query";
			String pageExtractParameters = "&prop=extracts&exintro&explaintext&exsentences=2";
			String searchGeneratorParameters = "&generator=search&gsrlimit=10&gsrsearch="+searchTerm;
			String requestUrl = BASE_URL+actionParameter+pageExtractParameters+searchGeneratorParameters;
			
			HttpResponse httpResponse = ApiRequester.getHttpResponse(requestUrl);
			if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				PageExtractSearchResult searchResult = new PageExtractSearchResult(ApiRequester.getReponseString(httpResponse));
				return searchResult;
			}
			else{
				return new PageExtractSearchResult();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return new PageExtractSearchResult();
	}
}
