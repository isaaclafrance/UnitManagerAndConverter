package com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.hashdriven;

import java.util.HashMap;
import java.util.Map;

public class SignificanceRankHashedRepository {
    private Map<String, Integer> significanceRanks = new HashMap<>();

    ///
    public SignificanceRankHashedRepository(){}

    ///
    public void modifySignificanceRankOfMultipleConversions(Iterable<String> formattedConversions, int delta) {
        for (String formattedConversion : formattedConversions)
            modifySignificanceRankOfConversion(formattedConversion, delta);
    }

    public void modifySignificanceRankOfConversion(String formattedConversion, int delta) {
        if (!significanceRanks.containsKey(formattedConversion))
            significanceRanks.put(formattedConversion, 0);

        int currentValue = significanceRanks.get(formattedConversion);
        significanceRanks.put(formattedConversion, currentValue + delta < 0 ? 0 : currentValue + delta); //Zero is lowest ranks value
    }

    ///
    public int getSignificanceRankOfConversion(String formattedConversion) {
        return significanceRanks.get(formattedConversion);
    }
}
