package com.isaacapps.unitconverterapp.models.measurables.quantity;

public class QuantityException extends Exception {
    private final String scenario, howToFix;

    public QuantityException(String scenario, String howToFix) {
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
    @Override
    public String toString() {
        return String.format("**Quantity construction scenario that caused problem: %s \n **The following can resolve the problem: %s", scenario, howToFix);
    }
}
