package com.isaacapps.unitconverterapp.dao.api.mediawiki.webservices;

import com.isaacapps.unitconverterapp.dao.api.ApiRequester;
import com.isaacapps.unitconverterapp.dao.api.mediawiki.webservices.models.PageExtractsSearchResult;

import org.json.JSONException;

import java.io.IOException;

import javax.net.ssl.HttpsURLConnection;

public class MediaWikiWebServicesApiRequester extends ApiRequester {
    public static final String MEDIA_WIKI_BASE_URL = "https://en.wikipedia.org/w/api.php?format=json";
    private int numberOfSentences;
    private int numberOfSearchResultsLimit;

    public MediaWikiWebServicesApiRequester(int numberOfSentences, int numberOfSearchResultsLimit){
        this.numberOfSentences = numberOfSentences;
        this.numberOfSearchResultsLimit = numberOfSearchResultsLimit;
    }

    public PageExtractsSearchResult searchPageExtracts(String searchTerm, int numOfSentences) {
        try {
            String actionParameter = "&action=query";
            String pageExtractParameters = "&prop=extracts&exintro&explaintext&exsentences=" + numOfSentences;
            String searchGeneratorParameters = "&generator=search&gsrlimit=10&gsrsearch=" + searchTerm;
            String requestUrl = MEDIA_WIKI_BASE_URL + actionParameter + pageExtractParameters + searchGeneratorParameters;

            HttpsURLConnection httpsURLConnection = createHttpURLConnection(requestUrl);
            if (httpsURLConnection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                return new PageExtractsSearchResult(retrieveResponseString(httpsURLConnection));
            } else {
                return new PageExtractsSearchResult();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new PageExtractsSearchResult();
    }

    public int getNumberOfSentences() {
        return numberOfSentences;
    }
    public void setNumberOfSentences(int numberOfSentences) {
        this.numberOfSentences = numberOfSentences;
    }

    public int getNumberOfSearchResultsLimit() {
        return numberOfSearchResultsLimit;
    }
    public void setNumberOfSearchResultsLimit(int numberOfSearchResultsLimit) {
        this.numberOfSearchResultsLimit = numberOfSearchResultsLimit;
    }
}
