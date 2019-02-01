package com.isaacapps.unitconverterapp.tokenizers;

import android.widget.MultiAutoCompleteTextView;

import static com.isaacapps.unitconverterapp.adapters.MultiAutoCompleteUnitsDefinitionArrayAdapter.DEFAULT_MULTI_AUTO_COMPLETE_UNIT_DISPLAY_DELIMITER;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.DimensionComponentDefiner;

import com.florianingerl.util.regex.Matcher ;

public class MultiAutoCompleteUnitDefinitionTokenizer implements MultiAutoCompleteTextView.Tokenizer {
    private DimensionComponentDefiner dimensionComponentDefiner;
    private final String tokenTerminator;
    private final String quantityGroupingStartSymbol;

    public MultiAutoCompleteUnitDefinitionTokenizer(DimensionComponentDefiner dimensionComponentDefiner, String quantityGroupingStartSymbol){
        this(dimensionComponentDefiner, " ", quantityGroupingStartSymbol);
    }
    public MultiAutoCompleteUnitDefinitionTokenizer(DimensionComponentDefiner dimensionComponentDefiner, String tokenTerminator, String quantityGroupingStartSymbol){
        this.dimensionComponentDefiner = dimensionComponentDefiner;
        this.tokenTerminator = tokenTerminator;
        this.quantityGroupingStartSymbol = quantityGroupingStartSymbol;
    }


    @Override
    public int findTokenStart(CharSequence unitDefinitionText, int cursorPosition) {
        //TODO: Improve token recognition to account for when multiple unit groups are involved

        //Try finding tokens consisting of larger and more complex dimension constructs first, then if that fails attempt to use smaller dimension sub components.
        //The tokenization domain is bounded by the last detected terminator symbol that comes after the grouping start symbol and cursor position

        if(unitDefinitionText.length() == 0)
            return 0;

        String unitDefinitionSubSequence = unitDefinitionText.toString().substring(0, cursorPosition);
        int posOfOpeningGroupingSymbol = unitDefinitionSubSequence.lastIndexOf(quantityGroupingStartSymbol);
        posOfOpeningGroupingSymbol = posOfOpeningGroupingSymbol == -1 ? 0 : posOfOpeningGroupingSymbol;

        int posAfterLastTerminatorSymbol = unitDefinitionSubSequence.substring(posOfOpeningGroupingSymbol).lastIndexOf(tokenTerminator);
        posAfterLastTerminatorSymbol = (posAfterLastTerminatorSymbol == -1 ? posOfOpeningGroupingSymbol : posAfterLastTerminatorSymbol + 1);
        unitDefinitionSubSequence = unitDefinitionSubSequence.substring(posAfterLastTerminatorSymbol);

        int tokenPosition = posAfterLastTerminatorSymbol;

        Matcher unitDefinitionMultiGroupMatcher = dimensionComponentDefiner.getMultiGroupRegExPattern().matcher(unitDefinitionSubSequence);
        if(unitDefinitionMultiGroupMatcher.find()){
            tokenPosition += unitDefinitionMultiGroupMatcher.start();
        }

        if(tokenPosition == posAfterLastTerminatorSymbol){
            Matcher unitDefinitionSingleGroupMatcher = dimensionComponentDefiner.getSingleGroupRegExPattern().matcher(unitDefinitionSubSequence);
            if(unitDefinitionSingleGroupMatcher.find()){
                tokenPosition += unitDefinitionSingleGroupMatcher.start();
            }
        }

        return tokenPosition;
    }
    @Override
    public int findTokenEnd(CharSequence unitDefinitionText, int cursorPosition) {
        //TODO: Improve token recognition to account for when multiple unit groups are involved

        String unitDefinitionSubSequence = unitDefinitionText.subSequence(cursorPosition, unitDefinitionText.length() - 1).toString();

        int posOfOpeningGroupingSymbol = unitDefinitionSubSequence.indexOf(quantityGroupingStartSymbol);
        unitDefinitionSubSequence = unitDefinitionSubSequence.substring(0, posOfOpeningGroupingSymbol);

        int posOfTerminatorSymbol = unitDefinitionSubSequence.lastIndexOf(tokenTerminator);
        unitDefinitionSubSequence = unitDefinitionSubSequence.substring(0, posOfTerminatorSymbol);

        int tokenPosition = -1;

        //Try finding tokens consisting of larger and more complex dimension constructs first, then if that fails attempt to use smaller dimension sub components.
        //Attempt this until the last possible token is obtained and then take the ending position of that token plus cursor position, otherwise only cursor position is selected.

        Matcher unitDefinitionMultiGroupMatcher = dimensionComponentDefiner.getMultiGroupRegExPattern().matcher(unitDefinitionSubSequence);
        while(unitDefinitionMultiGroupMatcher.find()){
            tokenPosition = cursorPosition + unitDefinitionMultiGroupMatcher.end();
        }

        if(tokenPosition == posOfOpeningGroupingSymbol){
            Matcher unitDefinitionSingleGroupMatcher = dimensionComponentDefiner.getSingleGroupRegExPattern().matcher(unitDefinitionSubSequence);
            while(unitDefinitionSingleGroupMatcher.find()){
                tokenPosition = cursorPosition + unitDefinitionSingleGroupMatcher.end();
            }
        }

        return tokenPosition != -1 ? tokenPosition : cursorPosition;
    }

    @Override
    public CharSequence terminateToken(CharSequence unitDefinitionText) {
        int delimiterPosition = unitDefinitionText.toString().indexOf(DEFAULT_MULTI_AUTO_COMPLETE_UNIT_DISPLAY_DELIMITER);
        CharSequence fullNameToken = delimiterPosition == -1 ? unitDefinitionText : unitDefinitionText.toString().substring(0, delimiterPosition);
        return String.format("%s%s", fullNameToken, tokenTerminator);
    }
}
