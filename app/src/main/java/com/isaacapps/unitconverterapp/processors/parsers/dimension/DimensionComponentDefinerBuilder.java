package com.isaacapps.unitconverterapp.processors.parsers.dimension;

import com.florianingerl.util.regex.Pattern;
import com.isaacapps.unitconverterapp.processors.parsers.ParsingException;

import java.util.ArrayList;
import java.util.List;

import static com.isaacapps.unitconverterapp.utilities.RegExUtility.escapeRegexReservedCharacters;

public class DimensionComponentDefinerBuilder {
    public static final String[] DEFAULT_EXPONENT_SYMBOL_GROUPS = new String[]{"^", "**", " raised to "};
    public static final String[] DEFAULT_DIVISION_SYMBOL_GROUPS = new String[]{"/", " per ", " divided by "};
    public static final String[] DEFAULT_MULTIPLICATION_SYMBOL_GROUPS = new String[]{"*", " x ", " times ", " multiplied by "};

    private String[] divisionSymbols, multiplicationSymbols, exponentSymbols;
    private String exponentValueRegEx;
    private String atomicTypeRegEx;

    /**
     * Create a regular expression from a groups of division symbols.
     */
    public String createDivisionSymbolsRegEx() throws ParsingException {
        ParsingException.validateRequiredComponentsCollection(determineInvalidOrMissingComponentsNeededToConstructDivisionSymbolComponent());
        return createMultipleSymbolsRegEx(divisionSymbols);
    }

    /**
     * Create a regular expression from a groups of multiplication symbols.
     */
    public String createMultiplicationSymbolsRegEx() throws ParsingException {
        ParsingException.validateRequiredComponentsCollection(determineInvalidOrMissingComponentsNeededToConstructMultiplicationSymbolComponent());
        return createMultipleSymbolsRegEx(multiplicationSymbols);
    }

    /**
     * Create a regular expression from groups multiplication and division symbols using alternation.
     */
    public String createOperationComponentRegEx() throws ParsingException {
        ParsingException.validateRequiredComponentsCollection(determineInvalidOrMissingComponentsNeededToConstructOperationComponent());
        return String.format("(?:[\\s]*(?:%s|%s)[\\s]*)", createMultipleSymbolsRegEx(multiplicationSymbols)
                , createMultipleSymbolsRegEx(divisionSymbols));
    }

    /**
     * Create a regular expression using the atomic type regular expression modified to ignore white spaces.
     * Serves as the smallest non exponential parsable component of a unit definition.
     */
    public String createInteriorGroupComponentRegEx() throws ParsingException {
        ParsingException.validateRequiredComponentsCollection(determineInvalidOrMissingComponentsNeededToConstructInteriorGroupComponent());
        return String.format("(?:[\\s]*%s[\\s]*)", atomicTypeRegEx);
    }

    /**
     * Creates a regular expression using an alteration of exponent symbols and an exponent value regex.
     */
    public String createExponentGroupRegex() throws ParsingException {
        ParsingException.validateRequiredComponentsCollection(determineInvalidOrMissingComponentsNeededToConstructExponentGroupComponent());
        return String.format("(?:[\\s]*%s[\\s]*([\\s]*%s))", createMultipleSymbolsRegEx(exponentSymbols)
                , exponentValueRegEx);
    }

    /**
     * Recursively and greedily searches for the existence nested multigroups. All multigroups must satisfy the complex dimension criteria.
     * Form can be satisfied when obviously nested such as '( ((a)^2 * (a)^3)^8 /(b)^4 )^5',  '(a^3/b)^3'
     * When trivially nested and consisting of multiple single groups such as '(a*b*c/d)^5' which is the same as ((a)^1*(b)^1*(c)^1/(d)^1)^5.
     * The regex also  unfortunately extends to trivial cases where a single atomic type is inclosed in parenthesis
     * .This is the only case where the single group and multigroup regex's both have matches and for practical aprsing purposes this should not be the case.
     * but its not worth it to make the nested regex ignore that case.
     */
    public Pattern createMultiGroupRegExPattern() throws ParsingException {
        ParsingException.validateRequiredComponentsCollection(determineInvalidOrMissingComponentsNeededToConstructMultigroupComponent());

        String optionalOperationComponent = String.format("(?:%s)?", createOperationComponentRegEx());
        String optionalExponentialComponent = String.format("(?:%s)?", createExponentGroupRegex());

        String singleGroupComponent = createSingleGroupRegExPattern().pattern();

        String nestedMultiGroup = String.format("(?<group>%s[(][\\s]*(?:(?'group')|%s)+[\\s]*[)]%s)"
                , optionalOperationComponent, singleGroupComponent, optionalExponentialComponent);

        return Pattern.compile(nestedMultiGroup);
    }

    /**
     * Search for a single group that has one of the three simple formats 'a', 'a^2', '(a)^3', with and without operation symbols.
     * May or may not be complex depending on if there is an exponent.
     */
    public Pattern createSingleGroupRegExPattern() throws ParsingException {
        ParsingException.validateRequiredComponentsCollection(determineInvalidOrMissingComponentsNeededToConstructSinglegroupComponent());

        String optionalExponentialComponent = String.format("(?:%s)?", createExponentGroupRegex());
        String optionalOperationalComponent = String.format("(?:(?<=\\s|[()]|\\A)(?:%s)?|%<s)"
                , createOperationComponentRegEx());

        return Pattern.compile(String.format("%s(?<p>[(][\\s]*)?%s(?(p)[\\s]*[)])%s(?=\\b|\\Z)"
                , optionalOperationalComponent, createInteriorGroupComponentRegEx()
                , optionalExponentialComponent));
    }

    /**
     * Dimension is complex if it has at least one division or multiplication symbol separating other token
     * , has nested or multiple side by side parenthesized groups, or uses exponents.
     */
    public boolean hasComplexDimensions(String unitDefinition){
        try {
            return unitDefinition.matches("(?:([(].*[)]){2,})")
                    || Pattern.compile(createOperationComponentRegEx()).matcher(unitDefinition).find()
                    || Pattern.compile(createExponentGroupRegex()).matcher(unitDefinition).find()
                    || hasNestedParentheses(unitDefinition);
        }
        catch(Exception e){
            return false;
        }
    }

    ///
    private List<String> determineInvalidOrMissingComponentsNeededToConstructMultiplicationSymbolComponent() {
        List<String> invalidComponents = new ArrayList<>();

        if (multiplicationSymbols == null || multiplicationSymbols.length == 0)
            invalidComponents.add("Multiplication symbol groups");

        return invalidComponents;
    }

    private List<String> determineInvalidOrMissingComponentsNeededToConstructDivisionSymbolComponent() {
        List<String> invalidComponents = new ArrayList<>();

        if (divisionSymbols == null || divisionSymbols.length == 0)
            invalidComponents.add("Division symbol groups");

        return invalidComponents;
    }

    private List<String> determineInvalidOrMissingComponentsNeededToConstructOperationComponent() {
        List<String> invalidComponents = determineInvalidOrMissingComponentsNeededToConstructMultiplicationSymbolComponent();
        invalidComponents.addAll(determineInvalidOrMissingComponentsNeededToConstructDivisionSymbolComponent());
        return invalidComponents;
    }

    private List<String> determineInvalidOrMissingComponentsNeededToConstructInteriorGroupComponent() {
        List<String> invalidComponents = new ArrayList<>();

        if (atomicTypeRegEx == null || atomicTypeRegEx.isEmpty())
            invalidComponents.add("Atomic type regex");

        return invalidComponents;
    }

    private List<String> determineInvalidOrMissingComponentsNeededToConstructExponentGroupComponent() {
        List<String> invalidComponents = new ArrayList<>();

        if (exponentValueRegEx == null || exponentValueRegEx.isEmpty())
            invalidComponents.add("Exponent value regex");

        if (exponentSymbols == null || exponentSymbols.length == 0)
            invalidComponents.add("Exponent symbol groups");

        return invalidComponents;
    }

    private List<String> determineInvalidOrMissingComponentsNeededToConstructSinglegroupComponent() {
        List<String> invalidComponents = determineInvalidOrMissingComponentsNeededToConstructExponentGroupComponent();
        invalidComponents.addAll(determineInvalidOrMissingComponentsNeededToConstructInteriorGroupComponent());
        invalidComponents.addAll(determineInvalidOrMissingComponentsNeededToConstructOperationComponent());
        return invalidComponents;
    }

    /**
     * Has the same basic components requirements as a single group since its built on top of the single group contruct.
     */
    private List<String> determineInvalidOrMissingComponentsNeededToConstructMultigroupComponent() {
        return determineInvalidOrMissingComponentsNeededToConstructSinglegroupComponent();
    }

    ///

    /**
     * Converts groups of symbols into a regular expression with alternation.
     *
     * @param symbolGroups Array of symbol groups. Each group in the array is a string
     *                     that can contain one or more symbol characters or text.
     *                     Ex. '*', '**', ' x ', ' some word '
     * @return Regular expression string that captures symbol groups.
     */
    public static String createMultipleSymbolsRegEx(String[] symbolGroups) {
        StringBuilder regexStringBuilder = new StringBuilder();

        for (String symbolGroup : symbolGroups) {
            String trimedSymbolGroup = symbolGroup.trim();

            if (trimedSymbolGroup.isEmpty())
                continue;

            //If there are multiple symbol groups, then they are joined with an alternation symbol.
            regexStringBuilder.append(regexStringBuilder.length() == 0 ? "" : "|");

            //Replace white spaces with regex word boundary
            if (symbolGroup.startsWith(" "))
                regexStringBuilder.append("\\b");

            //Escape reserved words
            regexStringBuilder.append(escapeRegexReservedCharacters(trimedSymbolGroup));

            //Replace white spaces with regex word boundary
            if (symbolGroup.endsWith(" "))
                regexStringBuilder.append("\\b");
        }

        return regexStringBuilder.insert(0, "(?:").append(")").toString();
    }

    /**
     * Quickly determines if for every open brace, there is a corresponding closing brace.
     * Text with no parentheses is trivially balanced...
     */
    public static boolean hasBalancedParentheses(String unitDefinition) {
        unitDefinition = Pattern.compile("(\\((?:[^()]|(?1))*\\))").matcher(unitDefinition).replaceAll(""); //remove all individual and nested balanced parentheses
        return !(unitDefinition.contains("(") || unitDefinition.contains(")")); //If the text was originally balanced, then should be no remaining parentheses
    }

    /**
     * Quickly determines if there is a nesting of parentheses, but only if the text is balanced
     * If there is at least one instance of " '(' [anything not ')'] '(' ", then there is some nesting of some kind.
     */
    public static boolean hasNestedParentheses(String unitDefinition) {
        return hasBalancedParentheses(unitDefinition)
                && Pattern.compile("[(][^)]*[(]").matcher(unitDefinition.trim()).find();
    }

    /**
     * Quickly determines if there is an obvious nesting of exponents, ie. '(a^2/(b)^4)^2'
     */
    public boolean hasNestedExponents(String unitDefinition) throws ParsingException {
        return Pattern.compile(String.format("(?:[(](?:.+%s.+)[)]%<s)", createExponentGroupRegex()))
                .matcher(unitDefinition.trim()).find();
    }

    ///
    /**
     * Array of symbol groups to be used to identify a division operation. Each group in the array is a string
     * that can contain one or more symbol characters.
     */
    public DimensionComponentDefinerBuilder setDivisionSymbols(String... divisionSymbols) {
        this.divisionSymbols = divisionSymbols;
        return this;
    }
    public String[] getDivisionSymbols() {
        return divisionSymbols;
    }

    /**
     * Array of symbol groups to be used to identify a division operation. Each group in the array is a string
     * that can contain one or more symbol characters.
     */
    public DimensionComponentDefinerBuilder setMultiplicationSymbols(String... multiplicationSymbols) {
        this.multiplicationSymbols = multiplicationSymbols;
        return this;
    }
    public String[] getMultiplicationSymbols() {
        return multiplicationSymbols;
    }

    /**
     * Array of symbol groups to be used to identify a exponential operation. Each group in the array is a string
     * that can contain one or more symbol characters.
     */
    public DimensionComponentDefinerBuilder setExponentSymbols(String... exponentSymbols) {
        this.exponentSymbols = exponentSymbols;
        return this;
    }
    public String[] getExponentSymbols() {
        return exponentSymbols;
    }

    /**
     * Sets regular expression that is able to capture the numerical exponent part.
     */
    public DimensionComponentDefinerBuilder setExponentValueRegEx(String exponentValueRegEx) {
        this.exponentValueRegEx = exponentValueRegEx;
        return this;
    }
    public String getExponentValueRegEx() {
        return exponentValueRegEx;
    }

    public DimensionComponentDefinerBuilder setAtomicTypeRegEx(String atomicTypeRegEx) {
        this.atomicTypeRegEx = atomicTypeRegEx;
        return this;
    }
    public String getAtomicTypeRegEx() {
        return atomicTypeRegEx;
    }
}
