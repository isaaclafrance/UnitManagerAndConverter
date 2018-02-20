package com.isaacapps.unitconverterapp.utilities;

import java.util.Collection;
import java.util.HashSet;

//Grammar Reference: https://www.grammarly.com/blog/plural-nouns/

public final class PluralTextProcessingUtility {
	private static final String vowlesRegEx = "[aeiou]";
	private static final String wordSeperatorRegEx = " _";
	private static final String onlyLetterWordsRegEx = "[a-zA-Z "+wordSeperatorRegEx+"]+";
	
	 //A rule consists of a regular expression to identify the plural ending and what to replace it with
	 //The rules are very simple and do not account for language specific exceptions.
	private static final String[][] rules = {{"(?<=ch|sh|x|s|z|ss|o)es\\Z", ""}
	                                   ,{"(?<=zz)es\\Z", "z"}
	                                   ,{"(?<=ss)es\\Z", "s"}
	                                   ,{"ves\\Z", "f"}
	                                   ,{"ves\\Z", "fe"}
	                                   ,{"(?<!"+vowlesRegEx+")ies\\Z", "y"}
	                                   ,{"(?<="+vowlesRegEx+")ys\\Z", "y"}
	                                   ,{"i\\Z", "us"}
	                                   ,{"ices\\Z", "ex"}
	                                   ,{"ae\\Z", "a"}};
	
	public static boolean isPossiblePlural(String text){
		if(!text.matches(onlyLetterWordsRegEx))
			return false;
		
		for(String word:text.split(wordSeperatorRegEx)){
			for(String[] rule:rules){
				if(word.matches(rule[0]))
					return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Tries to apply the rules to each word in the text. Depending on structure of words one or more rules may apply.
	 * @param text must be words separated by space or underscore that are composed of only capitalized or lowercase letters.
	 * @return
	 */
	public static Collection<String> getPossibleSingularMatches(String text){
		HashSet<String> possibleSingularMatches = new HashSet<String>();
 		
		if(text.matches(onlyLetterWordsRegEx)){
			for(String[] rule:rules){
				//Tries to apply the rules to each word in the text. Depending on structure of words one or more rules may apply.
				String singularizedText = "";
				for(String word:text.split(wordSeperatorRegEx)){
					singularizedText = text.replaceAll(word, word.replaceFirst(rule[0], rule[1]));
				}
				if(singularizedText.equalsIgnoreCase(text))
					possibleSingularMatches.add(singularizedText);
			}
		}
		
		return possibleSingularMatches;
	}
}
