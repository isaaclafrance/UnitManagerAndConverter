package com.isaacapps.unitconverterapp.processors.serializers;

import java.util.Arrays;
import java.util.Collection;

public class SerializingException extends Exception {
    private final String scenario, howToFix;

    public SerializingException(String scenario, String howToFix) {
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
     * Throws a descriptive exception when there is aa non empty collection of require components need for proper serializtion
     * within a particular context.
     * @throws SerializingException
     */
    public static void validateRequiredComponentsCollection(Collection<String> requireComponentsCollection) throws SerializingException {
        if (!requireComponentsCollection.isEmpty())
            throw new SerializingException("Some components needed for string generation have not been set yet."
                    , "Please set the following components: " + Arrays.deepToString(requireComponentsCollection.toArray()));
    }

    ///
    @Override
    public String toString() {
        return String.format("**Serialization scenario that caused problem: %s \n **The following can resolve the problem: %s", scenario, howToFix);
    }
}
