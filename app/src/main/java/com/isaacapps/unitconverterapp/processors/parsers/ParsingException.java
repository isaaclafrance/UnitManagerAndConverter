package com.isaacapps.unitconverterapp.processors.parsers;

import java.util.Arrays;
import java.util.Collection;

public class ParsingException extends Exception {
    private final String scenario, howToFix;

    public ParsingException(String scenario, String howToFix) {
        super(String.format("**Parsing scenario that caused problem: %s \n**The following can resolve the problem: %s", scenario, howToFix));
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
     * Throws a descriptive exception when there is aa non empty collection of require components need for proper parsing
     * within a particular context
     * @throws ParsingException
     */
    public static void validateRequiredComponentsCollection(Collection<String> requireComponentsCollection) throws ParsingException {
        if (!requireComponentsCollection.isEmpty())
            throw new ParsingException("Some components needed for parsing have not been appropriately set."
                    , "Please appropriately set the following components: " + Arrays.deepToString(requireComponentsCollection.toArray()));
    }
}
