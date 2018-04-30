package com.isaacapps.unitconverterapp.utilities;

import org.junit.*;
import java.util.*;

import static com.isaacapps.unitconverterapp.utilities.PluralTextProcessingUtility.*;
import static junit.framework.Assert.*;

public class PluralTextProcessingUtilityTest {
    private Map<String, String> firstPassPluralToSingular;
    private Map<String, String> secondPassPluralToSingular;

    @Before
    public void setUp(){
        firstPassPluralToSingular = new HashMap<>();
        firstPassPluralToSingular.put("inches", "inch");
        firstPassPluralToSingular.put("henries", "henry");
        firstPassPluralToSingular.put("atlases", "atlas");
        firstPassPluralToSingular.put("crashes", "crash");
        firstPassPluralToSingular.put("botches", "botch");
        firstPassPluralToSingular.put("waltzes", "waltz");
        firstPassPluralToSingular.put("alleys", "alley");
        firstPassPluralToSingular.put("skies", "sky");
        firstPassPluralToSingular.put("wolves", "wolf");
        firstPassPluralToSingular.put("potatoes", "potato");
        firstPassPluralToSingular.put("hypotheses", "hypothesis");
        firstPassPluralToSingular.put("nuclei", "nucleus");
        firstPassPluralToSingular.put("bacteria", "bacterium");
        firstPassPluralToSingular.put("vertebrae", "vertebra");
        firstPassPluralToSingular.put("vertices", "vertex");

        secondPassPluralToSingular = new HashMap<>();
        secondPassPluralToSingular.put("yards", "yard");
        secondPassPluralToSingular.put("meters", "meter");
        secondPassPluralToSingular.put("lumens", "lumen");
    }

    @Test
    public void hasPossiblePlural_Should_Be_True_When_Single_Word_Fits_First_Pass_Rules(){
        for(String firstPassPluralInput:firstPassPluralToSingular.keySet()){
            assertTrue(String.format("Plural word does not match any first pass plural rules: %s", firstPassPluralInput)
                    , hasPossiblePlural(firstPassPluralInput, false));
        }
    }

    @Test
    public void hasPossiblePlural_Should_Be_True_When_Single_Word_Fits_Second_Pass_Rules(){
        for(String secondPassPluralInput:secondPassPluralToSingular.keySet()){
            assertTrue(String.format("Plural word does not match any second pass plural rules: %s", secondPassPluralInput)
                    , hasPossiblePlural(secondPassPluralInput, true));
        }
    }

    @Test
    public void hasPossiblePlural_Should_Be_False_When_Single_Word_Does_Not_Fit_First_Or_Second_Pass_Rules(){
        for(String singularInput:secondPassPluralToSingular.values()){
            assertFalse(String.format("Singlar word identified as plural: %s", singularInput)
                    , hasPossiblePlural(singularInput, true));
        }
    }

    @Test
    public void hasPossiblePlural_Should_Be_False_When_Single_Word_Is_Not_Word(){
        String nonWordInput = "(*&$$(*";

        assertFalse(String.format("Nonword identified as plural: %s", nonWordInput)
                , hasPossiblePlural(nonWordInput, true));
    }

    @Test
    public void hasPossiblePlural_Should_Be_True_When_Compound_Noun_Fits_First_Pass_Rules(){
        for(Map.Entry<String, String> pluralSingularEntry:firstPassPluralToSingular.entrySet()){
            String compoundNoun = String.format("%s %s %1$s %2$s", pluralSingularEntry.getKey(), pluralSingularEntry.getValue());

            assertTrue(String.format("Plural compound not identified as plural: %s", compoundNoun)
                    , hasPossiblePlural(compoundNoun, false));
        }
    }

    @Test
        for(Map.Entry<String, String> pluralSingularEntry:firstPassPluralToSingular.entrySet()){

            String compoundNoun = String.format("%s %s_%1$s", pluralSingularEntry.getKey(), pluralSingularEntry.getValue());
            String expectedSingularizedCompoundNoun = String.format("%s %<s_%<s", pluralSingularEntry.getValue());

            assertTrue(String.format("Singularization of plural compound word is incorrect" +
                            ". Input plural compound noun %s" +
                            ". Expected singular conversion: %s"
                        , compoundNoun, expectedSingularizedCompoundNoun)
                    , getPossibleSingularCombinations(compoundNoun)
                            .contains(expectedSingularizedCompoundNoun));
        }
    }
}
