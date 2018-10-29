package com.isaacapps.unitconverterapp.models.unitmanager;

import java.util.Arrays;
import java.util.Collection;

public class UnitManagerBuilderException extends Exception {
    private final String scenario, howToFix;

    public UnitManagerBuilderException(String scenario, String howToFix) {
        super(String.format("**The following prevented a unit manager for being built successfully: %s " +
                "\n**The following can resolve the problem: %s", scenario, howToFix));
        this.scenario = scenario;
        this.howToFix = howToFix;
    }

    public String getScenario() {
        return scenario;
    }

    public String getHowToFix() {
        return howToFix;
    }

    ///
    /**
     * Throws a descriptive exception when there is aa non empty collection of require components need for proper unit manager building
     * within a particular context
     * @throws UnitManagerBuilderException
     */
    public static void validateRequiredComponentsCollection(Collection<String> requireComponentsCollection) throws UnitManagerBuilderException {
        if (!requireComponentsCollection.isEmpty())
            throw new UnitManagerBuilderException("Some components needed for unit manager building have not been appropriately set."
                    , "Please appropriately set the following components: " + Arrays.deepToString(requireComponentsCollection.toArray()));
    }
}
