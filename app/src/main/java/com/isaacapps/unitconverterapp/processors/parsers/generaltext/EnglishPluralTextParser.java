package com.isaacapps.unitconverterapp.processors.parsers.generaltext;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

//Grammar Reference: https://www.grammarly.com/blog/plural-nouns/

public class EnglishPluralTextParser extends PluralTextParser{
    private static final String VOWELS_REGEX = "[aeiou]";
    private static final String WORD_SEPERATOR_REGEX = "([ _]|\\Z)";
    private static final int MINIMUM_LENGTH = 4;

    //A rule consists of a regular expression to identify the plural ending and what to replace it with.
    //The rules are very simple and do not account for all language specific exceptions and irregularities.
    private static final String[][] FIRST_PASS_RULES = {{"(?<=ch|sh|x|s|z|ss|o)es\\b", ""}, {"(?<=z)zes\\b", ""}
            , {"ves\\b", "f"}
            , {"ves\\b", "fe"}
            , {"(?<!" + VOWELS_REGEX + ")ies\\b", "y"}
            , {"(?<=" + VOWELS_REGEX + ")ys\\b", "y"}
            , {"i\\b", "us"}
            , {"ices\\b", "ex"}
            , {"ae\\b", "a"}
            , {"(?<=[^zs])ses\\b", "sis"}
            , {"ia\\b", "ion"}
            , {"ia\\b", "ium"}
            , {"ina\\b", "en"}};
    private static final String[] SECOND_PASS_RULE = {"(?<=[a-zA-Z])s\\b", ""}; //Less restrictive.

    //Transform multidimenional array of rules into one regular expression to be used for matching
    private static final Pattern FIRST_PASS_RULE_PATTERN = Pattern.compile(Arrays.deepToString(FIRST_PASS_RULES)
                .replace("]]", ")")
                .replaceAll(", \\[", ")|(?:")
                .replaceAll(",[^\\[)]+", "")
                .replace("[[", "(?:"));
    private static final Pattern SECOND_PASS_RULE_PATTERN = Pattern.compile(SECOND_PASS_RULE[0]);

    /**
     * Determines whether a text contains possible plurals based on English grammar.
     *
     * @param text                         Noun composed of capitalized or lowercase letters. If a compound noun, then words must be separated by underscore or space. Must be greater than {@value #MINIMUM_LENGTH}
     * @param useLessRestrictiveSecondPass if true, then will check less restrictive case of if the text ends in s. Has more false positives.
     */
    @Override
    public boolean hasPossiblePlural(String text, boolean useLessRestrictiveSecondPass) {
        if (text.length() < MINIMUM_LENGTH)
            return false;

        //Since the less restrictive second pass rule catches more cases, it should be tested first if parameter
        if (useLessRestrictiveSecondPass && SECOND_PASS_RULE_PATTERN.matcher(text).find())
            return true;

        return FIRST_PASS_RULE_PATTERN.matcher(text).find();
    }

    /**
     * Tries to apply American English plural rules to each word in the text. Depending on structure of the words, one or more rules may apply. Rules are simple.
     *
     * @param text Noun composed of capitalized or lowercase letters. If a compound noun, then words must be separated by underscore or space.
     * @return Collection of combinations where the plural words in the provided text are alternatively replaced with their potential singular forms.
     * For example, input 'groups of peas' would return 'group of peas', and 'groups of pea'. It is up to what is consuming the results array to determine which one is valid.
     */
    @Override
    public Collection<String> generatePossibleSingularCombinations(String text) {
        if (!hasPossiblePlural(text, true))
            return Collections.emptyList();

        Map<String, Set<String>> pluralWordToSingularMap = new HashMap<>();

        for (String word : text.split(WORD_SEPERATOR_REGEX)) {
            String singularWord;

            pluralWordToSingularMap.put(word, new HashSet<>());

            for (String[] rule : FIRST_PASS_RULES) {
                singularWord = word.replaceFirst(rule[0], rule[1]);
                if (!singularWord.equalsIgnoreCase(word))
                    pluralWordToSingularMap.get(word).add(singularWord);
            }

            //Only applies second pass rule if the first pass rules did not apply at all.
            if (pluralWordToSingularMap.get(word).isEmpty()) {
                singularWord = word.replaceFirst(SECOND_PASS_RULE[0], SECOND_PASS_RULE[1]);
                if (!singularWord.equalsIgnoreCase(word)) {
                    pluralWordToSingularMap.get(word).add(singularWord);
                } else {
                    pluralWordToSingularMap.remove(word);
                }
            }
        }

        //Since this implementation is simple and does not regard which word is significant, if more than one word was plural, then a list is returned
        return generateAlternativeSingularCombinations(text, pluralWordToSingularMap);
    }

    /**
     * Creates combinations where the plural words in the provided text are individually and alternately replaced with their potential singular forms.
     *
     * @param providedText             Noun text that is plural. Can be a compound nouns, eg groups of peas.
     * @param pluralWordToSinglularMap Map of plural words from provided text and singular forms.
     * @return Set of modified texts with individual plurals replaced with possible singulars.
     */
    private Set<String> generateAlternativeSingularCombinations(String providedText, Map<String, Set<String>> pluralWordToSinglularMap) {
        Set<String> textCombinations = new HashSet<>();

        for (Map.Entry<String, Set<String>> currentPluralWordToSingularEntry : pluralWordToSinglularMap.entrySet()) {
            for (String singularWord : currentPluralWordToSingularEntry.getValue())
                textCombinations.add(providedText.replaceAll(currentPluralWordToSingularEntry.getKey()
                                , singularWord));
        }

        return textCombinations;
    }
}