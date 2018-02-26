package com.isaacapps.unitconverterapp.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

//Grammar Reference: https://www.grammarly.com/blog/plural-nouns/

public final class PluralTextProcessingUtility {
	private static final String VOWELS_REGEX = "[aeiou]";
	private static final String WORD_SEPERATOR_REGEX = "([ _]|\\Z)"; 
	private static final String ONLY_LETTER_WORDS_REGEX = "([a-zA-Z]+"+WORD_SEPERATOR_REGEX+")+";
	private static final int MINIMUM_LENGTH = 4;
	
	 //A rule consists of a regular expression to identify the plural ending and what to replace it with
	 //The rules are very simple and do not account for language specific exceptions.
	private static final String[][] FIRST_PASS_RULES = {{"(?<=ch|sh|x|s|z|ss|o)es\\Z", ""}
	                                   ,{"(?<=z)zes", ""}
	                                   ,{"ves\\Z", "f"}
	                                   ,{"ves\\Z", "fe"}
	                                   ,{"(?<!"+VOWELS_REGEX+")ies\\Z", "y"}
	                                   ,{"(?<="+VOWELS_REGEX+")ys\\Z", "y"}
	                                   ,{"i\\Z", "us"}
	                                   ,{"ices\\Z", "ex"}
	                                   ,{"ae\\Z", "a"}
	                                   ,{"(?<=[^zs])ses\\Z", "sis"}
	                                   ,{"ia\\Z", "ion"}
	                                   ,{"ia\\Z", "ium"}
	                                   ,{"ina\\Z", "en"}};
	private static final String[] SECOND_PASS_RULES = {"(?<=[a-zA-z]{2,})s\\Z", ""}; //Less restrictive. Any word greater three letters that ends in 's'.
	
	/**
	 * Determines whether a text contains possible plurals
	 * @param text Noun composed of capitalized or lowercase letters. If a compound noun, then words must be separated by underscore or space. Must be greater than {@value #MINIMUM_LENGTH}
	 * @param useLessRestrictiveSecondPass if true, then will check less restrictive case of if the text ends in s. Has more false positives.
	 */
	public static boolean hasPossiblePlural(String text, boolean useLessRestrictiveSecondPass){
		if(!text.matches(ONLY_LETTER_WORDS_REGEX) || text.length() < MINIMUM_LENGTH)
			return false;
		
		//Since the less restrictive second pass rule catches more cases, it should be tested first if parameter 
		if(useLessRestrictiveSecondPass && text.matches(SECOND_PASS_RULES[0])) 
			return true;
		
		for(String[] rule:FIRST_PASS_RULES){
			if(text.matches(rule[0]))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Tries to apply the rules to each word in the text. Depending on structure of the words, one or more rules may apply. Rules are simple.
	 * @param text  Noun composed of capitalized or lowercase letters. If a compound noun, then words must be separated by underscore or space.
	 * @return Collection of combinations where the plural words in the provided text are alternatively replaced with their potential singular forms.  
	 */
	public static Collection<String> getPossibleSingularMatches(String text){
		SortedMap<String, ArrayList<String>> pluralWordToSingularMap = new TreeMap<String,ArrayList<String>>();
		
		if(!text.matches(ONLY_LETTER_WORDS_REGEX) || text.length() < MINIMUM_LENGTH)
			return Collections.EMPTY_LIST;
			
		for(String word:text.split(WORD_SEPERATOR_REGEX)){
			String singularWord;
			
			pluralWordToSingularMap.put(word, new ArrayList<String>());
			
			for(String[] rule:FIRST_PASS_RULES){
				singularWord = word.replaceFirst(rule[0], rule[1]);
				if(!singularWord.equalsIgnoreCase(word))	
					pluralWordToSingularMap.get(word).add(singularWord);
			}
			
			//Only applies second pass rule if the first pass rules did not apply at all.
			if(pluralWordToSingularMap.get(word).isEmpty()){
				singularWord = word.replaceFirst(SECOND_PASS_RULES[0], SECOND_PASS_RULES[1]);
				if(!singularWord.equalsIgnoreCase(word)){
					pluralWordToSingularMap.get(word).add(singularWord);	
				}
				else{
					pluralWordToSingularMap.remove(word);
				}
			}
		}
		
		//Since this implementation is simple and does not regard which word is significant, if more than one word was plural, then a list is returned
		return getAlternativeSingularCombinations(text, pluralWordToSingularMap);
	}
	
	/**
	 * Creates combinations where the plural words in the provided text are alternatively replaced with their potential singular forms 
	 * There is no concern for which noun is significant. 
	 * @param providedText Noun text that is plural. Can be a compound noun.
	 * @param pluralWordToSinglularMap Map of plural words from provided text and singular forms.
	 * @return Set of text with plurals replaced with singulars since there is no concern for which noun is significant.
	 */
	private static Set<String> getAlternativeSingularCombinations(String providedText, SortedMap<String, ArrayList<String>> pluralWordToSinglularMap)
	{
		Set<String> textCombinations = new HashSet<String>();
		
		List<String> pluralWordKeys = new ArrayList<String>(pluralWordToSinglularMap.keySet());
		for(int i=0; i<pluralWordKeys.size(); i++){
			String currentPluralWordKey = pluralWordKeys.get(i);
			for(String singularWord:pluralWordToSinglularMap.get(currentPluralWordKey)){
				String newText = providedText.replaceAll(currentPluralWordKey, singularWord);
				textCombinations.add(newText);
			}		
		}
		
		return textCombinations;
	}
}