package com.isaacapps.unitconverterapp.models.measurables.unit;

import java.util.Arrays;
import java.util.Collection;

public class UnitException extends Exception {
    private final String scenario, howToFix;

    public UnitException(String scenario, String howToFix) {
        super(String.format("**Unit scenario that caused problem: %s \n**The following can resolve the problem: %s", scenario, howToFix));
        this.scenario = scenario;
        this.howToFix = howToFix;
    }

    /**
     * Throws a descriptive exception when there is a non empty collection of issues and requirements involving units.
     * @throws UnitException
     */
    public static void validateRequiredComponentsCollection(Collection<String> requireComponentsCollection) throws UnitException {
        if (!requireComponentsCollection.isEmpty())
            throw new UnitException("Some requirements involving units have not been met."
                    , "Please resolve the following: " + Arrays.deepToString(requireComponentsCollection.toArray()));
    }

    public String getScenario() {
        return scenario;
    }

    public String getHowToFix() {
        return howToFix;
    }
}
