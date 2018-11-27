package com.isaacapps.unitconverterapp.dao.api.mediawiki.webservices.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class PageExtractsSearchResult extends JSONObject {
    Comparator<PageExtract> pageExtractBestMatchComparator;

    public PageExtractsSearchResult(String jsonResponse) throws JSONException {
        super(jsonResponse);
        pageExtractBestMatchComparator = new Comparator<PageExtract>() {
            @Override
            public int compare(PageExtract lhsPageExtract, PageExtract rhsPageExtract) {
                return Long.compare(lhsPageExtract.getIndex(), rhsPageExtract.getIndex());
            }
        };
    }

    public PageExtractsSearchResult() {
        super();
    }

    public Collection<PageExtract> getPageExtracts() {
        try {
            if (hasContent()) {
                Collection<PageExtract> pageExtracts = new ArrayList<>();

                JSONObject pagesJObj = getJSONObject("query").getJSONObject("pages");
                Iterator<String> pageIds = pagesJObj.keys();
                while (pageIds.hasNext()) {
                    pageExtracts.add(new PageExtract(pagesJObj.getString(pageIds.next())));
                }

                return pageExtracts;
            } else {
                return Collections.emptyList();
            }

        } catch (JSONException e) {
            return Collections.emptyList();
        }
    }

    /**
     * Uses the index property to rank
     * @return
     */
    public PageExtract getBestMatchPageExtract(){
        return Collections.min(getPageExtracts(), pageExtractBestMatchComparator);
    }

    public boolean hasContent() {
        return length() != 0;
    }
}
