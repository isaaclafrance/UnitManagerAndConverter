package com.isaacapps.unitconverterapp.dao.api.mediawiki.webservices.models;

import org.json.JSONException;
import org.json.JSONObject;

public class PageExtract extends JSONObject {

    public PageExtract(String jsonResponse) throws JSONException {
        super(jsonResponse);
    }

    public String getTitle() {
        try {
            return getString("title");
        } catch (JSONException e) {
            return "";
        }
    }

    public String getExtractText() {
        try {
            return getString("extract");
        } catch (JSONException e) {
            return "";
        }
    }

    public long getPageId() {
        try {
            return getLong("pageid");
        } catch (JSONException e) {
            return -1;
        }
    }

    public long getIndex(){
        try {
            return getLong("index");
        } catch (JSONException e) {
            return -1;
        }
    }

    public String getWikipediaReferenceUrl() {
        String wikipediaUrl = "https://en.wikipedia.org/wiki/" + getTitle();
        return String.format("<a href=\"%s\">Extract Source: Wikipedia</a>", wikipediaUrl);
    }

    public boolean hasContent() {
        return length() != 0;
    }
}
