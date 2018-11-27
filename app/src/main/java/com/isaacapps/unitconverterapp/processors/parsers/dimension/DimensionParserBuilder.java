package com.isaacapps.unitconverterapp.processors.parsers.dimension;

import com.florianingerl.util.regex.Matcher;
import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;
import com.isaacapps.unitconverterapp.processors.parsers.IParser;
import com.isaacapps.unitconverterapp.processors.parsers.ParsingException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DimensionParserBuilder<T> implements IParser<Map<T, Double>> {
    private Map<T, Double> templateDimensionMap;

    private DimensionComponentDefiner dimensionComponentDefiner;

    private IFormatter exponentValueFormatter;
    private IFormatter atomicTypeFormatter;

    private IParsedDimensionUpdater<T> parsedDimensionUpdater;

    private boolean strictParsing;
    private boolean isOptimized;

    private Matcher multiGroupRegExMatcher;
    private Matcher singleGroupRegExMatcher;
    private Matcher exponentialGroupRegExMatcher;
    private Matcher exponentValueRegExMatcher;
    private Matcher atomicTypeGroupRegExMatcher;
    private Matcher divisionSymbolsRegExMatcher;

    ///
    public DimensionParserBuilder(){

    }
    public DimensionParserBuilder(DimensionComponentDefiner dimensionComponentDefiner) {
        this.dimensionComponentDefiner = dimensionComponentDefiner;
    }

    ///
    /**
     * If the provided dimension string satisfies the balanced parenthesis criteria on the surface level
     * , then proceeds to extracting and processing multigroups and single groups.
     *
     * @throws ParsingException
     */
    @Override
    public Map<T, Double> parse(String dimensionString) throws ParsingException {

        ParsingException.validateRequiredComponentsCollection(determineInvalidOrMissingComponentsNeededForParsing());

        ///
        if (dimensionString.isEmpty()) {
            if (strictParsing) {
                throw new ParsingException("Nothing was available to be parsed."
                        , "Make dimension string non-empty by including well formatted content.");
            } else {
                return parsedDimensionUpdater.updateWithUnknownDimension(getNewTemplateDimensionMap());
            }
        }

        ///
        if (DimensionComponentDefiner.hasBalancedParentheses(dimensionString)) {
            return parseNestedMultiGroups(dimensionString, 1.0, getNewTemplateDimensionMap());
        }else {
            //Having balanced parenthesis is critical to proper parsing and must always throw an exception.
            throw new ParsingException(dimensionString
                    , "Make sure the number of open parenthesis braces equals the number of closing parenthesis braces in proper order.");
        }
    }

    /**
     * Processes multi-groups using a recursive structure that greedily parses nested unit groups bounded by parentheses. ie. ( ((a)^2 * (a)^3)^8 /(b)^4 )^5.
     * Uses single group construct as base case.
     * Updates the passed in dimension map with extracted results.
     *
     * @return Dimension map representing string that satisfies type requirement of passed in dimension updater.
     * @throws ParsingException
     */
    private Map<T, Double> parseNestedMultiGroups(String dimensionString, double recursedExponent
            ,Map<T, Double> dimensionMap) throws ParsingException {

        String truncatedDimensionString = dimensionString; // During the recursion process matching multigroups will be greedily matched and successively excised from the dimension string.

        Matcher multiGroupDimensionRegExMatcher = getMultiGroupRegExMatcher(dimensionString);

        while (multiGroupDimensionRegExMatcher.find()) {
            String multiGroupDimension = multiGroupDimensionRegExMatcher.group();

            //
            String[] parsedExponentGroup = parseExponentialGroup(multiGroupDimension);
            double currentExponent = Double.valueOf(parsedExponentGroup[1]);

            //Force a new nested multigroup search to occur in preparation for recursion.
            String multiGroupDimensionToBeRecursed = multiGroupDimension.substring(multiGroupDimension.indexOf('(') + 1, multiGroupDimension.lastIndexOf(')'));

            //Passes on multigroup into next level of recursion making sure exponents are properly accumulated and successively passed on.
            parseNestedMultiGroups(multiGroupDimensionToBeRecursed, recursedExponent * currentExponent, dimensionMap);

            truncatedDimensionString = truncatedDimensionString.replace(multiGroupDimension, "");
        }

        /*If for some reason the remaining truncated dimension string still appears to have multi groups,
         *then multigroup parsing had failed due to some incorrectly formatted token somewhere.
         */
        if (DimensionComponentDefiner.hasNestedParentheses(truncatedDimensionString)
                || dimensionComponentDefiner.hasNestedExponents(truncatedDimensionString)) {
            if (strictParsing) {
                throw new ParsingException("This text appears to be an incorrectly formatted multigroup:"
                        + truncatedDimensionString
                        , "Change text format to fit regular expression requirement for multigroups:"
                        + multiGroupDimensionRegExMatcher.pattern());
            } else {
                parsedDimensionUpdater.updateWithUnknownDimension(dimensionMap);
                return dimensionMap;
            }
        }

        if (!truncatedDimensionString.trim().isEmpty()) {
            return parseSingleGroups(truncatedDimensionString, recursedExponent, dimensionMap);
        } else {
            return dimensionMap;
        }
    }

    /**
     * Processes dimension string consisting of nonnested single groups. ie. a, a^2, (b), (b)^2,  a*b*c^2.
     * Updates the passed in dimension map with extracted results.
     *
     * @throws ParsingException
     */
    private Map<T, Double> parseSingleGroups(String dimensionString, double outerExponent
            , Map<T, Double> dimensionMap)
            throws ParsingException {

        Matcher singleGroupDimensionRegExMatcher = getSingleGroupRegExMatcher(dimensionString);

        while (singleGroupDimensionRegExMatcher.find()) {
            String singleGroupDimension = singleGroupDimensionRegExMatcher.group();

            //
            String[] parsedExponentGroup = parseExponentialGroup(singleGroupDimension);
            double currentExponent = Double.valueOf(exponentValueFormatter.format(parsedExponentGroup[1]));

            //
            String atomicType = parseAtomicTypeGroup(singleGroupDimension
                            .replaceAll(dimensionComponentDefiner.getDivisionSymbolsRegExPattern().pattern(), "")
                            .replaceAll(dimensionComponentDefiner.getMultiplicationSymbolsRegExPattern().pattern(), ""));

            if (atomicType.isEmpty())
                throw new ParsingException(String.format("No atomic type can be extracted using atomicTypeRegExPattern '%s'" +
                                " from the group '%s' that was originally produced by the singleGroupDimensionRegExPattern '%s'."
                        , dimensionComponentDefiner.getAtomicTypeRegExPattern().pattern(), singleGroupDimension, dimensionComponentDefiner.getSingleGroupRegExPattern().pattern())
                        , "The single group regular expression must correspond with atomic type regular expressions.");

            //
            parsedDimensionUpdater.updateDimension(atomicType, outerExponent * currentExponent, dimensionMap);
        }

        //All single groups will be successively excised until hopefully there is nothing left.
        String truncatedDimensionString = singleGroupDimensionRegExMatcher.replaceAll("");
        if (!truncatedDimensionString.trim().isEmpty()) {
            if (strictParsing) {
                throw new ParsingException("This remaining text could not be properly parsed: "+truncatedDimensionString
                        , "Change this remaining text to fit regular expression requirement for single groups: "
                        + singleGroupDimensionRegExMatcher.pattern());
            } else {
                parsedDimensionUpdater.updateWithUnknownDimension(dimensionMap);
            }
        }

        return dimensionMap;
    }

    /**
     * Extracts extract an exponent group and the contained exponent value
     *
     * @return Array of strings. First element is extracted full exponent group.
     * The second element is exponent value. If both can not be extracted then an empty array is returned.
     *
     * @throws ParsingException
     */
    private String[] parseExponentialGroup(String dimension) throws ParsingException {
        Matcher exponentialRegExMatcher =  getExponentialGroupRegExMatcher(dimension);
        Matcher divisionSymbolRegExMatcher = getDivisionSymbolsRegExMatcher(dimension);

        //Division inverts the exponent
        String exponentValueSignBasedOnOperation = (divisionSymbolRegExMatcher.find()
                && dimension.trim().startsWith(divisionSymbolRegExMatcher.group())) ? "-" : "";

        if (exponentialRegExMatcher.find()) {
            String exponentialGroup = exponentialRegExMatcher.group();
            Matcher exponentValueRegExMatcher = getExponentValueRegExMatcher(exponentialGroup);

            if (!exponentValueRegExMatcher.find())
                throw new ParsingException(String.format("No value can be extracted using exponentValueRegExPattern '%s' " +
                                "from the group '%s' that was produced by the exponentRegExPattern '%s'."
                        , dimensionComponentDefiner.getExponentValueRegexPattern().pattern(), exponentialGroup, dimensionComponentDefiner.getExponentialRegExPattern().pattern())
                        , "The group regular expression must correspond with value regular expression.");

            //By this point the parsing tree any present operation token was already validated as being suitable for division or multiplication
            //Therefore any operation token present other than the division is a multiplication
            String exponentValue = (exponentValueSignBasedOnOperation + exponentValueRegExMatcher.group())
                    .replace("--", ""); //negatives cancel

            return new String[]{exponentialGroup, exponentValue};

        } else {
            //If not explicitly raised to anything then assume 1 (+/- depending on presence of division)
            return new String[]{"", exponentValueSignBasedOnOperation + "1"};
        }
    }

    /**
     * Identifies and extract one atomic type. ex. 'meter' if a unit name, LENGTH if a fundamental dimension
     * @return String of extracted atomic type or an empty string.exponentValueFormatter.format(
     */
    private String parseAtomicTypeGroup(String dimension) {
        Matcher atomicTypeRegExMatcher = getAtomicTypeGroupRegExMatcher(dimension);

        if (atomicTypeRegExMatcher.find())
            return atomicTypeFormatter.format(atomicTypeRegExMatcher.group());

        return "";
    }

    ///
    private List<String> determineInvalidOrMissingComponentsNeededForParsing() {
        List<String> invalidParsingComponents = new ArrayList<>();

        if (parsedDimensionUpdater == null)
            invalidParsingComponents.add("{ An instance satisfying IParsedDimensionUpdater }");

        if (dimensionComponentDefiner == null)
            invalidParsingComponents.add("{ An instance satisfying DimensionComponentDefiner }");

        if(exponentValueFormatter == null)
            invalidParsingComponents.add("{ A formatter for the parsed exponent value }");

        if(atomicTypeFormatter == null)
            invalidParsingComponents.add("{ A formatter for the parsed atomic type }");

        return invalidParsingComponents;
    }

    ///
    public DimensionParserBuilder<T> setParsedDimensionUpdater(IParsedDimensionUpdater<T> parsedDimensionUpdater) {
        this.parsedDimensionUpdater = parsedDimensionUpdater;
        return this;
    }
    public IParsedDimensionUpdater<T> getParsedDimensionUpdater() {
        return parsedDimensionUpdater;
    }

    public DimensionParserBuilder<T> setDimensionComponentDefiner(DimensionComponentDefiner dimensionComponentDefiner) throws ParsingException {
        this.dimensionComponentDefiner = dimensionComponentDefiner;
        return this;
    }
    public DimensionComponentDefiner getDimensionComponentDefiner() {
        return dimensionComponentDefiner;
    }

    public IFormatter getExponentValueFormatter() {
        return exponentValueFormatter;
    }
    public DimensionParserBuilder<T> setExponentValueFormatter(IFormatter exponentValueFormatter) {
        this.exponentValueFormatter = exponentValueFormatter;
        return this;
    }

    public IFormatter getAtomicTypeFormatter() {
        return atomicTypeFormatter;
    }
    public DimensionParserBuilder<T> setAtomicTypeFormatter(IFormatter atomicTypeFormatter) {
        this.atomicTypeFormatter = atomicTypeFormatter;
        return this;
    }

    ///

    /**
     * Sets the template map implementation upon which parsed dimensions will be added.
     * This is useful if one wants certain algorithmic benefit of other Map implementations than the default HashMap implementation.
     */
    public DimensionParserBuilder<T> setTemplateDimensionMap(Map<T, Double> templateDimensionMap) {
        this.templateDimensionMap = templateDimensionMap;
        return this;
    }
    private Map<T, Double> getNewTemplateDimensionMap(){
        if(templateDimensionMap == null)
            return (templateDimensionMap = new HashMap<>());
        else {
            try {
                return (templateDimensionMap = templateDimensionMap.getClass().newInstance());
            } catch (Exception e) {
                return (templateDimensionMap = new HashMap<>());
            }
        }
    }

    //
    public DimensionParserBuilder<T> setStrictParsing(boolean strictParsing) {
        this.strictParsing = strictParsing;
        return this;
    }
    public boolean isStrictParsing() {
        return strictParsing;
    }

    //
    public DimensionParserBuilder<T> setIsOptimized(boolean isOptimized) {
        this.isOptimized = isOptimized;
        return this;
    }
    /**
     * Determines if certain shortcuts are taken for the sake of performance, which may have other unwelcome side effects.
     * For example, regular expression matcher may reuse is not necessarily threadsafe.
     */
    public boolean isOptimized() {
        return isOptimized;
    }

    private Matcher getMultiGroupRegExMatcher(String stringToBeMatched){
        if(!isOptimized || multiGroupRegExMatcher == null || multiGroupRegExMatcher.pattern().pattern()
                .equalsIgnoreCase(dimensionComponentDefiner.getMultiGroupRegExPattern().pattern()))
        {
            return (multiGroupRegExMatcher = dimensionComponentDefiner.getMultiGroupRegExPattern().matcher(stringToBeMatched));
        }
        else{
            return multiGroupRegExMatcher.reset(stringToBeMatched);
        }
    }

    private Matcher getSingleGroupRegExMatcher(String stringToBeMatched){
        if(!isOptimized || singleGroupRegExMatcher == null || singleGroupRegExMatcher.pattern().pattern()
                .equalsIgnoreCase(dimensionComponentDefiner.getSingleGroupRegExPattern().pattern()))
        {
            return (singleGroupRegExMatcher = dimensionComponentDefiner.getSingleGroupRegExPattern().matcher(stringToBeMatched));
        }
        else{
            return singleGroupRegExMatcher.reset(stringToBeMatched);
        }
    }

    private Matcher getExponentialGroupRegExMatcher(String stringToBeMatched){
        if(!isOptimized || exponentialGroupRegExMatcher == null || exponentialGroupRegExMatcher.pattern().pattern()
                .equalsIgnoreCase(dimensionComponentDefiner.getExponentialRegExPattern().pattern()))
        {
            return (exponentialGroupRegExMatcher = dimensionComponentDefiner.getExponentialRegExPattern().matcher(stringToBeMatched));
        }
        else{
            return exponentialGroupRegExMatcher.reset(stringToBeMatched);
        }
    }

    private Matcher getExponentValueRegExMatcher(String stringToBeMatched){
        if(!isOptimized || exponentValueRegExMatcher == null || exponentValueRegExMatcher.pattern().pattern()
                .equalsIgnoreCase(dimensionComponentDefiner.getExponentValueRegexPattern().pattern()))
        {
            return (exponentValueRegExMatcher = dimensionComponentDefiner.getExponentValueRegexPattern().matcher(stringToBeMatched));
        }
        else{
            return exponentValueRegExMatcher.reset(stringToBeMatched);
        }
    }

    private Matcher getAtomicTypeGroupRegExMatcher(String stringToBeMatched){
        if(!isOptimized || atomicTypeGroupRegExMatcher == null || atomicTypeGroupRegExMatcher.pattern().pattern()
                .equalsIgnoreCase(dimensionComponentDefiner.getAtomicTypeRegExPattern().pattern()))
        {
            return (atomicTypeGroupRegExMatcher = dimensionComponentDefiner.getAtomicTypeRegExPattern().matcher(stringToBeMatched));
        }
        else{
            return atomicTypeGroupRegExMatcher.reset(stringToBeMatched);
        }
    }

    private Matcher getDivisionSymbolsRegExMatcher(String stringToBeMatched){
        if(!isOptimized || divisionSymbolsRegExMatcher == null || divisionSymbolsRegExMatcher.pattern().pattern()
                .equalsIgnoreCase(dimensionComponentDefiner.getDivisionSymbolsRegExPattern().pattern()))
        {
            return (divisionSymbolsRegExMatcher = dimensionComponentDefiner.getDivisionSymbolsRegExPattern().matcher(stringToBeMatched));
        }
        else{
            return divisionSymbolsRegExMatcher.reset(stringToBeMatched);
        }
    }
}
