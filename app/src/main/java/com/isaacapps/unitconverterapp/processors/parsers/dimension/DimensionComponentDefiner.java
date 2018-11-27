package com.isaacapps.unitconverterapp.processors.parsers.dimension;

import com.florianingerl.util.regex.Pattern;
import com.isaacapps.unitconverterapp.processors.parsers.ParsingException;

import java.util.Collections;

import static com.isaacapps.unitconverterapp.utilities.RegExUtility.SIGNED_DOUBLE_VALUE_REGEX_PATTERN;
import static com.isaacapps.unitconverterapp.utilities.RegExUtility.escapeRegexReservedCharacters;

/**
 * Has lexer-like attributes
 */
public class DimensionComponentDefiner {
    public static final String[] DEFAULT_EXPONENT_SYMBOL_GROUPS = new String[]{"^", "**", " raised to "};
    public static final String[] DEFAULT_DIVISION_SYMBOL_GROUPS = new String[]{"/", " per ", " divided by "};
    public static final String[] DEFAULT_MULTIPLICATION_SYMBOL_GROUPS = new String[]{"*", " x ", " times ", " multiplied by "};

    private static final Pattern BALANCED_PARENTHESES_REGEX_PATTERN =  Pattern.compile("(\\((?:[^()]|(?1))*\\))");
    private static final Pattern NESTED_PARENTHESES_REGEX_PATTERN =  Pattern.compile("[(][^)]*[(]");
    private static final Pattern ADJACENT_PARENTHESES_REGEX_PATTERN = Pattern.compile("(?:([(].*[)]){2,})");

    private String[] divisionSymbols, multiplicationSymbols, exponentSymbols;

    private String exponentValueRegex;

    private Pattern exponentValueRegexPattern;
    private Pattern exponentialRegExPattern;
    private Pattern exponentSymbolsRegExPattern;

    private Pattern divisionSymbolsRegExPattern;
    private Pattern multiplicationSymbolsRegExPattern;
    private Pattern operationComponentRegExPattern;

    private Pattern atomicTypeRegExPattern;
    private Pattern singleGroupRegExPattern;
    private Pattern multiGroupRegExPattern;

    private Pattern nestedExponentsRegExPattern;

    ///
    public DimensionComponentDefiner(String atomicTypeRegEx) throws ParsingException {
        this(atomicTypeRegEx, SIGNED_DOUBLE_VALUE_REGEX_PATTERN.pattern(), DEFAULT_EXPONENT_SYMBOL_GROUPS, DEFAULT_DIVISION_SYMBOL_GROUPS, DEFAULT_MULTIPLICATION_SYMBOL_GROUPS);
    }
    public DimensionComponentDefiner(String atomicTypeRegEx, String exponentValueRegEx, String[] exponentSymbols
            , String[] divisionSymbols, String[] multiplicationSymbols) throws ParsingException {

        addDivisionSymbols(divisionSymbols);
        addMultiplicationSymbols(multiplicationSymbols);
        setOperationsRegExPatterns();

        addExponentSymbols(exponentSymbols);
        addExponentValueRegEx(exponentValueRegEx);
        setExponentialRegExPatterns();

        setAtomicTypeRegEx(atomicTypeRegEx);
        setGroupingRegExPatterns();
    }

    /**
     * Create a regular expression from a groups of division symbols.
     */
    String createDivisionSymbolsRegEx(){
        return createMultipleSymbolsRegEx(divisionSymbols);
    }

    /**
     * Create a regular expression from a groups of multiplication symbols.
     */
    String createMultiplicationSymbolsRegEx(){
        return createMultipleSymbolsRegEx(multiplicationSymbols);
    }

    /**
     * Create a regular expression from a groups of multiplication symbols.
     */
    String createExponentSymbolsRegEx(){
        return createMultipleSymbolsRegEx(exponentSymbols);
    }

    /**
     * Create a regular expression from groups multiplication and division symbols using alternation.
     */
    String createOperationComponentRegEx(){
        return String.format("(?:[\\s]*(?:%s|%s)[\\s]*)", createMultipleSymbolsRegEx(multiplicationSymbols)
                , createMultipleSymbolsRegEx(divisionSymbols));
    }

    /**
     * Create a regular expression using the atomic type regular expression modified to ignore white spaces.
     * Serves as the smallest non exponential parsable component of a unit definition.
     */
    String createInteriorGroupComponentRegEx(){
        return String.format("(?:[\\s]*%s[\\s]*)", atomicTypeRegExPattern.pattern());
    }

    /**
     * Creates a regular expression using an alteration of exponent symbols and an exponent value regex.
     */
    String createExponentGroupRegex(){
        return String.format("(?:[\\s]*%s[\\s]*([\\s]*%s))", createMultipleSymbolsRegEx(exponentSymbols)
                , exponentValueRegex);
    }

    /**
     * Recursively and greedily searches for the existence of largest nested multigroup. All multigroups must satisfy the complex dimension criteria.
     * Form can be satisfied when obviously nested such as '( ((a)^2 * (a)^3)^8 /(b)^4 )^5',  '(a^3/b)^3'
     * When trivially nested and consisting of multiple single groups such as '(a*b*c/d)^5' which is the same as ((a)^1*(b)^1*(c)^1/(d)^1)^5.
     */
    Pattern createMultiGroupRegExPattern(){
        String optionalOperationComponent = String.format("(?:%s)?", createOperationComponentRegEx());
        String optionalExponentialComponent = String.format("(?:%s)?", createExponentGroupRegex());

        String singleGroupComponent = createSingleGroupRegExPattern().pattern();

        String nestedMultiGroup = String.format("(?<group>%s[(][\\s]*(?:(?'group')|%s)+[\\s]*[)]%s)"
                , optionalOperationComponent, singleGroupComponent, optionalExponentialComponent);

        return Pattern.compile(nestedMultiGroup);
    }

    /**
     * Search for a single group that has one of the three simple formats 'a', 'a^2', with and without operation symbols.
     * May or may not be complex depending on if there is an exponent.
     */
    Pattern createSingleGroupRegExPattern() {
        String optionalExponentialComponent = String.format("(?:%s)?", createExponentGroupRegex());
        String optionalOperationalComponent = String.format("(?:(?<=\\s|\\b|[()]|\\A)(?:%s))?"
                , createOperationComponentRegEx());

        return Pattern.compile(String.format("(?:%s%s%s(?=\\b|\\Z))"
                , optionalOperationalComponent, createInteriorGroupComponentRegEx()
                , optionalExponentialComponent));
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
    String createMultipleSymbolsRegEx(String[] symbolGroups) {
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
        unitDefinition = BALANCED_PARENTHESES_REGEX_PATTERN.matcher(unitDefinition).replaceAll(""); //remove all individual and nested balanced parentheses
        return !(unitDefinition.contains("(") || unitDefinition.contains(")")); //If the text was originally balanced, then should be no remaining parentheses
    }

    /**
     * Quickly determines if there is a nesting of parentheses, but only if the text is balanced
     * If there is at least one instance of " '(' [anything not ')'] '(' ", then there is some nesting of some kind.
     */
    public static boolean hasNestedParentheses(String unitDefinition) {
        return hasBalancedParentheses(unitDefinition)
                && NESTED_PARENTHESES_REGEX_PATTERN.matcher(unitDefinition.trim()).find();
    }

    /**
     * Quickly determines if there is an obvious nesting of exponents, ie. '(a^2/(b)^4)^2'
     */
    public boolean hasNestedExponents(String unitDefinition){
        try {
            return nestedExponentsRegExPattern.matcher(unitDefinition.trim()).find();
        }
        catch(Exception e){
            return false;
        }
    }

    /**
     * Dimension is complex if it has at least one division or multiplication symbol separating other token
     * , has nested or multiple side by side parenthesized groups, or uses exponents.
     */
    public boolean hasComplexDimensions(String unitDefinition){
        try {
            return ADJACENT_PARENTHESES_REGEX_PATTERN.matcher(unitDefinition).find()
                    || operationComponentRegExPattern.matcher(unitDefinition).find()
                    || exponentialRegExPattern.matcher(unitDefinition).find()
                    || hasNestedParentheses(unitDefinition);
        }
        catch(Exception e){
            return false;
        }
    }

    ///
    /**
     * Array of symbol groups to be used to identify a division operation. Each group in the array is a string
     * that can contain one or more symbol characters.
     */
    public DimensionComponentDefiner setDivisionSymbols(String... divisionSymbols) throws ParsingException {
        addDivisionSymbols(divisionSymbols);
        setOperationsRegExPatterns();
        setGroupingRegExPatterns();
        return this;
    }
    private void addDivisionSymbols(String... divisionSymbols) throws ParsingException {
        if (divisionSymbols == null || divisionSymbols.length == 0)
            ParsingException.validateRequiredComponentsCollection(Collections.singleton("Exponent symbols"));

        this.divisionSymbols = divisionSymbols;
        divisionSymbolsRegExPattern = Pattern.compile(createDivisionSymbolsRegEx());
    }
    public String[] getDivisionSymbols() {
        return divisionSymbols;
    }

    /**
     * Array of symbol groups to be used to identify a division operation. Each group in the array is a string
     * that can contain one or more symbol characters.
     */
    public DimensionComponentDefiner setMultiplicationSymbols(String... multiplicationSymbols) throws ParsingException {
        addMultiplicationSymbols(multiplicationSymbols);
        setOperationsRegExPatterns();
        setGroupingRegExPatterns();
        return this;
    }
    private void addMultiplicationSymbols(String... multiplicationSymbols) throws ParsingException {
        if (multiplicationSymbols == null || multiplicationSymbols.length == 0)
            ParsingException.validateRequiredComponentsCollection(Collections.singleton("Multiplication symbols"));

        this.multiplicationSymbols = multiplicationSymbols;
        multiplicationSymbolsRegExPattern = Pattern.compile(createMultiplicationSymbolsRegEx());
    }
    public String[] getMultiplicationSymbols() {
        return multiplicationSymbols;
    }

    /**
     * Array of symbol groups to be used to identify a exponential operation. Each group in the array is a string
     * that can contain one or more symbol characters.
     */
    public DimensionComponentDefiner setExponentSymbols(String... exponentSymbols) throws ParsingException {
        addExponentSymbols(exponentSymbols);
        setExponentialRegExPatterns();
        setGroupingRegExPatterns();
        return this;
    }
    private void addExponentSymbols(String... exponentSymbols) throws ParsingException {
        if (exponentSymbols == null || exponentSymbols.length == 0)
            ParsingException.validateRequiredComponentsCollection(Collections.singleton("Exponent symbols"));

        this.exponentSymbols = exponentSymbols;
        exponentSymbolsRegExPattern = Pattern.compile(createExponentSymbolsRegEx());
    }
    public String[] getExponentSymbols() {
        return exponentSymbols;
    }

    /**
     * Sets regular expression that is able to capture the numerical exponent part.
     */
    public DimensionComponentDefiner setExponentValueRegEx(String exponentValueRegEx) throws ParsingException {
        addExponentValueRegEx(exponentValueRegEx);
        setExponentialRegExPatterns();
        setGroupingRegExPatterns();
        return this;
    }
    private void addExponentValueRegEx(String exponentValueRegEx) throws ParsingException {
        if (exponentValueRegEx == null || exponentValueRegEx.isEmpty())
            ParsingException.validateRequiredComponentsCollection(Collections.singleton("Exponent value regex"));

        this.exponentValueRegex = exponentValueRegEx;
        exponentValueRegexPattern = Pattern.compile(exponentValueRegEx + "$");
    }

    public DimensionComponentDefiner setAtomicTypeRegEx(String atomicTypeRegEx) throws ParsingException {
        addAtomicTypeRegEx(atomicTypeRegEx);
        setGroupingRegExPatterns();
        return this;
    }
    private void addAtomicTypeRegEx(String atomicTypeRegEx) throws ParsingException {
        if (atomicTypeRegEx == null || atomicTypeRegEx.isEmpty())
            ParsingException.validateRequiredComponentsCollection(Collections.singleton("Atomic type regex"));

        atomicTypeRegExPattern = Pattern.compile(atomicTypeRegEx);
    }

    ///
    private void setExponentialRegExPatterns() {
        String exponentialRegex = createExponentGroupRegex();
        exponentialRegExPattern = Pattern.compile(exponentialRegex + "$");
        nestedExponentsRegExPattern = Pattern.compile(String.format("(?:[(](?:.+%s.+)[)]%<s)", exponentialRegex));
    }
    private void setOperationsRegExPatterns() {
        operationComponentRegExPattern = Pattern.compile(createOperationComponentRegEx());
    }
    private void setGroupingRegExPatterns() {
        singleGroupRegExPattern = createSingleGroupRegExPattern();
        multiGroupRegExPattern = createMultiGroupRegExPattern();
    }

    ///
    /**
     * Identifies the farthest right most exponential consisting of a exponent symbol and a value.
     */
    public Pattern getExponentialRegExPattern() {
        return exponentialRegExPattern;
    }

    /**
     * Identifies the exponent value of the farthest right most exponential.
     */
    public Pattern getExponentValueRegexPattern()  {
        return exponentValueRegexPattern;
    }

    public Pattern getExponentSymbolsRegExPattern(){
        return exponentSymbolsRegExPattern;
    }

    public Pattern getAtomicTypeRegExPattern()  {
        return atomicTypeRegExPattern;
    }

    public Pattern getSingleGroupRegExPattern() {
        return singleGroupRegExPattern;
    }

    public Pattern getMultiGroupRegExPattern(){
        return multiGroupRegExPattern;
    }

    public Pattern getDivisionSymbolsRegExPattern(){
        return divisionSymbolsRegExPattern;
    }

    public Pattern getMultiplicationSymbolsRegExPattern(){
        return multiplicationSymbolsRegExPattern;
    }

}
