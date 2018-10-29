package com.isaacapps.unitconverterapp.models.measurables.quantity;

public class QuantityException extends Exception {
    private final String scenario, howToFix;

    public QuantityException(String scenario, String howToFix) {
        super(String.format("**Quantity scenario that caused problem: %s \n**The following can resolve the problem: %s", scenario, howToFix));
        this.scenario = scenario;
        this.howToFix = howToFix;
    }

    public String getScenario() {
        return scenario;
    }

    public String getHowToFix() {
        return howToFix;
    }
}
