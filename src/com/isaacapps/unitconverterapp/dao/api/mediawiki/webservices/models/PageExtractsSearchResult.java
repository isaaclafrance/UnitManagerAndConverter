package com.isaacapps.unitconverterapp.dao.api.mediawiki.webservices.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class PageExtractsSearchResult extends JSONObject {

    public PageExtractsSearchResult(String jsonResponse) throws JSONException {
        super(jsonResponse);
    }

    public PageExtractsSearchResult() {
        super();
    }

    public List<PageExtract> getPageExtracts() {
        try {
            if (hasContent()) {
                List<PageExtract> pageExtracts = new ArrayList<>();

                JSONObject pagesJObj = getJSONObject("query").getJSONObject("pages");
                Iterator<String> pageIds = pagesJObj.keys();
                while (pageIds.hasNext()) {
                    pageExtracts.add(new PageExtract(pagesJObj.getString(pageIds.next())));
                }

                return pageExtracts;
            } else {
                return Collections.EMPTY_LIST;
            }

        } catch (JSONException e) {
            return Collections.EMPTY_LIST;
        }
    }

    public boolean hasContent() {
        return length() != 0;
    }
}
