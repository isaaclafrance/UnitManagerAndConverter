package com.isaacapps.unitconverterapp.utilities;

import java.util.*;
import java.util.regex.Pattern;

//Grammar Reference: https://www.grammarly.com/blog/plural-nouns/

public final class PluralTextProcessingUtility {
	private static final String VOWELS_REGEX = "[aeiou]";
	private static final String WORD_SEPERATOR_REGEX = "([ _]|\\Z)"; 
	private static final String ONLY_LETTER_WORDS_REGEX = "([a-zA-Z]+"+WORD_SEPERATOR_REGEX+"?)+";
	private static final int MINIMUM_LENGTH = 4;
	
	 //A rule consists of a regular expression to identify the plural ending and what to replace it with
	 //The rules are very simple and do not account for language specific exceptions.
	private static final String[][] FIRST_PASS_RULES = {{"(?<=ch|sh|x|s|z|ss|o)es(\\Z|\\b)", ""}
	                                   ,{"(?<=z)zes(\\Z|\\b)", ""}
	                                   ,{"ves(\\Z|\\b)", "f"}
	                                   ,{"ves(\\Z|\\b)", "fe"}
	                                   ,{"(?<!"+VOWELS_REGEX+")ies(\\Z|\\b)", "y"}
	                                   ,{"(?<="+VOWELS_REGEX+")ys(\\Z|\\b)", "y"}
	                                   ,{"i(\\Z|\\b)", "us"}
	                                   ,{"ices(\\Z|\\b)", "ex"}
	                                   ,{"ae(\\Z|\\b)", "a"}
	                                   ,{"(?<=[^zs])ses(\\Z|\\b)", "sis"}
	                                   ,{"ia(\\Z|\\b)", "ion"}
	                                   ,{"ia(\\Z|\\b)", "ium"}
	                                   ,{"ina(\\Z|\\b)", "en"}};
	private static final String[] SECOND_PASS_RULE = {"(?<=[a-zA-Z]{2,})s(\\z|\\b)", ""}; //Less restrictive. Any word greater three letters that ends in 's'.
	
	/**
	 * Determines whether a text contains possible plurals
	 * @param text Noun composed of capitalized or lowercase letters. If a compound noun, then words must be separated by underscore or space. Must be greater than {@value #MINIMUM_LENGTH}
	 * @param useLessRestrictiveSecondPass if true, then will check less restrictive case of if the text ends in s. Has more false positives.
	 */
	public static boolean hasPossiblePlural(String text, boolean useLessRestrictiveSecondPass){
		if(!text.matches(ONLY_LETTER_WORDS_REGEX) || text.length() < MINIMUM_LENGTH)
			return false;
		
		//Since the less restrictive second pass rule catches more cases, it should be tested first if parameter 
		if(useLessRestrictiveSecondPass && Pattern.compile(SECOND_PASS_RULE[0]).matcher(text).find())
			return true;

		//Transform multidimenional array of rules into one regular expression to be used for matching
		return Pattern.compile(Arrays.deepToString(FIRST_PASS_RULES)
				.replace("]]",")")
				.replaceAll(", \\[",")|(")
				.replaceAll(",[^\\[)]+","")
				.replace("[[","("))
				.matcher(text)
				.find();
	}
	
	/**
	 * Tries to apply the rules to each word in the text. Depending on structure of the words, one or more rules may apply. Rules are simple.
	 * @param text  Noun composed of capitalized or lowercase letters. If a compound noun, then words must be separated by underscore or space.
	 * @return Collection of combinations where the plural words in the provided text are alternatively replaced with their potential singular forms.  
	 */
	public static Collection<String> getPossibleSingularCombinations(String text){
		if(!hasPossiblePlural(text, true))
			return Arrays.asList(text);

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
				singularWord = word.replaceFirst(SECOND_PASS_RULE[0], SECOND_PASS_RULE[1]);
				if(!singularWord.equalsIgnoreCase(word)){
					pluralWordToSingularMap.get(word).add(singularWord);	
				}
				else{
					pluralWordToSingularMap.remove(word);
				}
			}
		}
		
		//Since this implementation is simple and does not regard which word is significant, if more than one word was plural, then a list is returned
		return createAlternativeSingularCombinations(text, pluralWordToSingularMap);
	}
	
	/**
	 * Creates combinations where the plural words in the provided text are alternatively replaced with their potential singular forms 
	 * There is no concern for which noun is significant. 
	 * @param providedText Noun text that is plural. Can be a compound noun.
	 * @param pluralWordToSinglularMap Map of plural words from provided text and singular forms.
	 * @return Set of text with plurals replaced with singulars since there is no concern for which noun is significant.
	 */
	private static Set<String> createAlternativeSingularCombinations(String providedText, SortedMap<String, ArrayList<String>> pluralWordToSinglularMap)
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