package com.isaacapps.unitconverterapp.processors.parsers.generaltext;

import com.isaacapps.unitconverterapp.processors.parsers.IParser;

import java.util.Collection;

public abstract class PluralTextParser implements IParser<Collection<String>>{

    public Collection<String> parse(String pluralText){
        return getPossibleSingularCombinations(pluralText);
    }

    abstract public Collection<String> getPossibleSingularCombinations(String text);

    abstract public boolean hasPossiblePlural(String text, boolean useLessRestrictiveSecondPass);
}
