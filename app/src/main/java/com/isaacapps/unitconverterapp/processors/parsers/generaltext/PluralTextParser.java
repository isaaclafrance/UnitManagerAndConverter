package com.isaacapps.unitconverterapp.processors.parsers.generaltext;

import com.isaacapps.unitconverterapp.processors.parsers.IParser;

import java.util.Collection;

public abstract class PluralTextParser implements IParser<Collection<String>>{

    /**
     * Tries to apply plural rules to each word in the text. Depending on structure of the words, one or more rules may apply.
     *
     * @param pluralText Noun composed of capitalized or lowercase letters. If a compound noun, then words must be separated by underscore or space.
     * @return Collection of combinations where the plural words in the provided text are alternatively replaced with their potential singular forms.
     * It is up to the what is consuming the results array to determine which one is valid.
     */
    @Override
    public Collection<String> parse(String pluralText){
        return generatePossibleSingularCombinations(pluralText);
    }

    abstract protected Collection<String> generatePossibleSingularCombinations(String text);

    /**
     * Determines whether a text contains possible plurals.
     *
     * @param text                         Noun composed of capitalized or lowercase letters. If a compound noun, then words must be separated by underscore or space.
     * @param useLessRestrictiveSecondPass if true, then will check less restrictive cases. Has more false positives.
     */
    abstract public boolean hasPossiblePlural(String text, boolean useLessRestrictiveSecondPass);
}
