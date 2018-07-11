package com.isaacapps.unitconverterapp.processors.formatters.grouping;

import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;
import com.isaacapps.unitconverterapp.processors.parsers.measurables.quantity.QuantityGroupingDefiner;

import java.util.Locale;
import java.util.regex.Pattern;

public class GroupingFormatter implements IFormatter {
    QuantityGroupingDefiner quantityGroupingDefiner;

    private Pattern missingStartBracePattern;
    private Pattern missingEndBracePattern;
    private Pattern betweenGroupingPattern;
    private Pattern extraStartBracePattern;
    private Pattern extraEndBracePattern;
    private Pattern endBraceAtBeginningPattern;
    private Pattern startingBraceAtEndPattern;
    private Pattern edgecasePattern;

    private Locale locale;

    public GroupingFormatter(Locale locale, QuantityGroupingDefiner quantityGroupingDefiner) {
        this.locale = locale;
        this.quantityGroupingDefiner = quantityGroupingDefiner;
        compileGroupingEditingPatterns();
    }

    public void compileGroupingEditingPatterns(){
        missingStartBracePattern = Pattern.compile(String.format("(?!\\A)%2s(?=[^%1s]+%2s)"
                , quantityGroupingDefiner.getRegexEscapedGroupOpeningSymbol()
                , quantityGroupingDefiner.getRegexEscapedGroupClosingingSymbol()));
        missingEndBracePattern = Pattern.compile(String.format("(?<!\\A|%2s)%1s(?=[^%2s]+%2s)" //used to be "(?<!\\A|%2s)%1s(?=[^%2s]+%2s)"
                , quantityGroupingDefiner.getRegexEscapedGroupOpeningSymbol()
                , quantityGroupingDefiner.getRegexEscapedGroupClosingingSymbol()));
        betweenGroupingPattern = Pattern.compile(String.format("%2s[^%1s%2s\\s]+%1s"
                , quantityGroupingDefiner.getRegexEscapedGroupOpeningSymbol()
                , quantityGroupingDefiner.getRegexEscapedGroupClosingingSymbol()));
        extraStartBracePattern = Pattern.compile(String.format("%s[%s]+"
                , quantityGroupingDefiner.getRegexEscapedGroupOpeningSymbol()));
        extraEndBracePattern = Pattern.compile(String.format("%s[%s]+"
                , quantityGroupingDefiner.getRegexEscapedGroupClosingingSymbol()));
        endBraceAtBeginningPattern = Pattern.compile(String.format("\\A%2s\\s*%1s"
                , quantityGroupingDefiner.getRegexEscapedGroupOpeningSymbol()
                , quantityGroupingDefiner.getRegexEscapedGroupClosingingSymbol()));
        startingBraceAtEndPattern = Pattern.compile(String.format("%s\\Z"
                , quantityGroupingDefiner.getRegexEscapedGroupOpeningSymbol()));
        edgecasePattern = Pattern.compile(String.format("(?<=%2s)%1s(?=.+%1s)"
                , quantityGroupingDefiner.getRegexEscapedGroupOpeningSymbol()
                , quantityGroupingDefiner.getRegexEscapedGroupClosingingSymbol()));
    }

    /**
     * Finds the number of '{ [anything in between] }' groupings. Only accounts for kernel groupings if groups are nested.
     */
    public int calculateGroupingCount(String groupings) {
        int groupingCount = 0;

        int index;
        while ((index = groupings.indexOf(quantityGroupingDefiner.getGroupOpeningSymbol())) != -1) {
            String subText = groupings.substring(index);

            // if the braces are not arranged appropriately, then do not tally count
            if (subText.indexOf("{") < subText.indexOf(quantityGroupingDefiner.getGroupClosingSymbol()))
                groupingCount++;
        }

        return groupingCount;
    }

    public String adjustGroupingsCount(String groupingTextToBeAdjusted, int targetGroupCount) {
        StringBuilder adjustedGroupingTextBuilder = new StringBuilder(groupingTextToBeAdjusted);

        int currentGroupCount = calculateGroupingCount(groupingTextToBeAdjusted);
        if (currentGroupCount > targetGroupCount) {
            //remove extra groupings starting at the beginning
            while (calculateGroupingCount(adjustedGroupingTextBuilder.toString()) != targetGroupCount)
                adjustedGroupingTextBuilder.replace(adjustedGroupingTextBuilder.indexOf(quantityGroupingDefiner.getGroupOpeningSymbol()), adjustedGroupingTextBuilder.indexOf(quantityGroupingDefiner.getGroupClosingSymbol()), "");
        } else if (currentGroupCount < targetGroupCount) {
            //Add empty groupings to make up the difference in count
            adjustedGroupingTextBuilder.append(new String(new char[targetGroupCount - currentGroupCount]).replaceAll("\0", quantityGroupingDefiner.getGroupOpeningSymbol()+quantityGroupingDefiner.getGroupClosingSymbol()));
        }

        return adjustedGroupingTextBuilder.toString();
    }

    public String replaceEmptyGroupingsWithDefaultGrouping(String textWithEmptyGroupings, String defaultTextInReplacementGrouping){
        String groupingWithDefaultReplacementText = String.format("%s %s %s"
                ,quantityGroupingDefiner.getRegexEscapedGroupOpeningSymbol()
                , defaultTextInReplacementGrouping
                , quantityGroupingDefiner.getRegexEscapedGroupClosingingSymbol());

        return textWithEmptyGroupings.replaceAll(quantityGroupingDefiner.getEmptyGroupingPattern().pattern()
                , groupingWithDefaultReplacementText);
    }

    ///

    /**
     * Recursively tries to format grouping since fixing one kind of may disrupt an already fixed formatting.
     * There is probably a more efficient graph theory algorithmic or context sensitive grammar approach to this.
     * Strict regular expression restrictions that lookbacks (unlike lookaheads) have fixed lengths caused some obstacles and limitations in implementation
     */
    @Override
    public String format(String grouping) {
        if (calculateGroupingCount(grouping) == 0)
            grouping = "{" + grouping + "}";

        if (startingBraceAtEndPattern.matcher(grouping).find()) //In most cases when a line ends in a starting brace, the actual intent would be a closing brace
            grouping = format(grouping.replaceAll(startingBraceAtEndPattern.pattern(), "}"));

        // Add missing starting brace as long as the adjacent terminating brace is not in the beginning of the string, ie {aa}bb}... --> {aa}{bb}
        if (missingStartBracePattern.matcher(grouping).find())
            grouping = format(grouping.replaceAll(missingStartBracePattern.pattern(), "} {"));

        // Add missing terminating brace as long as the adjacent beginning brace is not in the beginning of string or immediately to the right of a terminating brace , ie. {aa{bb}... --> {aa}{bb}
        if (missingEndBracePattern.matcher(grouping).find())
            grouping = format(grouping.replaceAll(missingEndBracePattern.pattern(), "} {"));

        if (betweenGroupingPattern.matcher(grouping).find()) //Nothing should be in between groupings, ie. '} dkfj [too many space characters] {' --> '} {'
            grouping = format(grouping.replaceAll(betweenGroupingPattern.pattern(), "} {"));

        if (edgecasePattern.matcher(grouping).find()) //Account for some edge case....
            grouping = format(grouping.replaceAll(edgecasePattern.pattern(), ""));

        if (endBraceAtBeginningPattern.matcher(grouping).find()) // Remove terminating bracket at beginning, ie '}asnd}' --> '{asnd}'
            grouping = grouping.replaceAll(endBraceAtBeginningPattern.pattern(), "{");

        if (extraEndBracePattern.matcher(grouping).find()) // Remove extra ending braces ie '}}}' --> '}'
            grouping = grouping.replaceAll(extraEndBracePattern.pattern(), "}");

        if (extraStartBracePattern.matcher(grouping).find()) // Remove extra starting braces ie '{{{' --> '{'
            grouping = grouping.replaceAll(extraStartBracePattern.pattern(), "{");

        return grouping;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    ///
    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}
