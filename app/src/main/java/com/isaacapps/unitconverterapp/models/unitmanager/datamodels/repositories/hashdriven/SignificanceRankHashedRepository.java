package com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.hashdriven;

import java.util.HashMap;
import java.util.Map;

public class SignificanceRankHashedRepository {
    private Map<String, Integer> significanceRanks = new HashMap<>();

    ///
    public SignificanceRankHashedRepository(){}

    ///
    /**
     * Add new or updates existing formatted conversions' rank by a specified delta value
     */
    public void modifySignificanceRankOfMultipleConversions(Iterable<String> formattedConversions, int delta) {
        for (String formattedConversion : formattedConversions)
            modifySignificanceRankOfConversion(formattedConversion, delta);
    }

    /**
     * Add a new or updates an existing formatted conversion's rank by a specified delta value
     */
    public void modifySignificanceRankOfConversion(String formattedConversion, int delta) {
        if (!significanceRanks.containsKey(formattedConversion))
            significanceRanks.put(formattedConversion, 0);

        int currentValue = significanceRanks.get(formattedConversion);
        significanceRanks.put(formattedConversion, currentValue + delta < 0 ? 0 : currentValue + delta); //Zero is lowest ranks value
    }

    ///
    public int getSignificanceRankOfConversion(String formattedConversion) {
        if(significanceRanks.containsKey(formattedConversion)) {
            return significanceRanks.get(formattedConversion);
        }else{
            return 0;
        }
    }
}
