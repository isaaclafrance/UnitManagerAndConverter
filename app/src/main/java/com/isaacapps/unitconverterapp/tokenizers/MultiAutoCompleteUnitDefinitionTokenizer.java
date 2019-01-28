package com.isaacapps.unitconverterapp.tokenizers;

import android.widget.MultiAutoCompleteTextView;

import static com.isaacapps.unitconverterapp.adapters.MultiAutoCompleteUnitsDefinitionArrayAdapter.DEFAULT_MULTI_AUTO_COMPLETE_UNIT_DISPLAY_DELIMITER;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.DimensionComponentDefiner;

import com.florianingerl.util.regex.Matcher ;

public class MultiAutoCompleteUnitDefinitionTokenizer implements MultiAutoCompleteTextView.Tokenizer {
    private DimensionComponentDefiner dimensionComponentDefiner;
    private final String tokenTerminator;

    public MultiAutoCompleteUnitDefinitionTokenizer(DimensionComponentDefiner dimensionComponentDefiner){
        this(dimensionComponentDefiner, " ");
    }
    public MultiAutoCompleteUnitDefinitionTokenizer(DimensionComponentDefiner dimensionComponentDefiner, String tokenTerminator){
        this.dimensionComponentDefiner = dimensionComponentDefiner;
        this.tokenTerminator = tokenTerminator;
    }


    @Override
    public int findTokenStart(CharSequence unitDefinitionText, int cursorPosition) {
        //TODO: Improve token recognition to account for when multiple unit groups are involved

        CharSequence unitDefinitionSubSequence = unitDefinitionText.subSequence(0, cursorPosition);
        int tokenPosition = -1;

        //Try finding tokens consisting of larger and more complex dimension constructs first, then if that fails attempt to use smaller dimension sub components.
        //Attempt this until the last possible token is obtained and then take the starting position of that token.

        Matcher unitDefinitionMultiGroupMatcher = dimensionComponentDefiner.getMultiGroupRegExPattern().matcher(unitDefinitionSubSequence);
        if(unitDefinitionMultiGroupMatcher.find()){
            tokenPosition = unitDefinitionMultiGroupMatcher.start();
        }

        if(tokenPosition == -1){
            Matcher unitDefinitionSingleGroupMatcher = dimensionComponentDefiner.getSingleGroupRegExPattern().matcher(unitDefinitionSubSequence);
            if(unitDefinitionSingleGroupMatcher.find()){
                tokenPosition = unitDefinitionSingleGroupMatcher.start();
            }
        }

        return tokenPosition != -1 ? tokenPosition : 0;
    }
    @Override
    public int findTokenEnd(CharSequence unitDefinitionText, int cursorPosition) {
        //TODO: Improve token recognition to account for when multiple unit groups are involved

        CharSequence unitDefinitionSubSequence = unitDefinitionText.subSequence(cursorPosition, unitDefinitionText.length() - 1);
        int tokenPosition = -1;

        //Try finding tokens consisting of larger and more complex dimension constructs first, then if that fails attempt to use smaller dimension sub components.
        //Attempt this until the last possible token is obtained and then take the ending position of that token.

        Matcher unitDefinitionMultiGroupMatcher = dimensionComponentDefiner.getMultiGroupRegExPattern().matcher(unitDefinitionSubSequence);
        while(unitDefinitionMultiGroupMatcher.find()){
            tokenPosition = unitDefinitionMultiGroupMatcher.end();
        }

        if(tokenPosition == -1){
            Matcher unitDefinitionSingleGroupMatcher = dimensionComponentDefiner.getSingleGroupRegExPattern().matcher(unitDefinitionSubSequence);
            while(unitDefinitionSingleGroupMatcher.find()){
                return unitDefinitionSingleGroupMatcher.end();
            }
        }

        return tokenPosition != -1 ? tokenPosition : unitDefinitionText.length();
    }

    @Override
    public CharSequence terminateToken(CharSequence unitDefinitionText) {
        int delimiterPosition = unitDefinitionText.toString().indexOf(DEFAULT_MULTI_AUTO_COMPLETE_UNIT_DISPLAY_DELIMITER);
        CharSequence fullNameToken = delimiterPosition == -1 ? unitDefinitionText : unitDefinitionText.toString().substring(0, delimiterPosition);
        return String.format("%s%s", fullNameToken, tokenTerminator);
    }
}
