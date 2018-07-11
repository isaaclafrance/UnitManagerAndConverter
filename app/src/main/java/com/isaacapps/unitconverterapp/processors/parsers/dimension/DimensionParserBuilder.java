package com.isaacapps.unitconverterapp.processors.parsers.dimension;

import com.florianingerl.util.regex.Matcher;
import com.florianingerl.util.regex.Pattern;
import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;
import com.isaacapps.unitconverterapp.processors.parsers.IParser;
import com.isaacapps.unitconverterapp.processors.parsers.ParsingException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DimensionParserBuilder<T> implements IParser<Map<T, Double>> {
    private DimensionComponentDefinerBuilder dimensionComponentDefinerBuilder;
    private IFormatter exponentValueFormatter;
    private IFormatter atomicTypeFormatter;

    private IParsedDimensionUpdater<T> parsedDimensionUpdater;
    private boolean strictParsing;

    ///
    public DimensionParserBuilder(){
        this(new DimensionComponentDefinerBuilder());
    }
    public DimensionParserBuilder(DimensionComponentDefinerBuilder dimensionComponentDefinerBuilder){
        this.dimensionComponentDefinerBuilder = dimensionComponentDefinerBuilder;
    }

    ///
    /**
     * If the provided dimension string satisfies the balanced parenthesis criteria on the surface level
     * , then proceeds to extracting and processing multigroups and single groups.
     *
     * @throws ParsingException
     */
    public Map<T, Double> parse(String dimensionString) throws ParsingException {

        ParsingException.validateRequiredComponentsCollection(determineInvalidOrMissingComponentsNeededForParsing());

        ///
        if (dimensionString.isEmpty()) {
            if (strictParsing) {
                throw new ParsingException("Nothing was available to be parsed."
                        , "Make dimension string non-empty by including well formatted content.");
            } else {
                return parsedDimensionUpdater.updateWithUnknownDimension(new HashMap<>());
            }
        }

        ///
        if (DimensionComponentDefinerBuilder.hasBalancedParentheses(dimensionString)) {
            return parseNestedMultiGroups(dimensionString, 1.0, new HashMap<>());
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
     * @return Dimension map representing string that satifies type requirement of passed in dimension updater.
     * @throws ParsingException
     */
    private Map<T, Double> parseNestedMultiGroups(String dimensionString, double recursedExponent
            ,Map<T, Double> dimensionMap) throws ParsingException {

        String truncatedDimensionString = dimensionString; // During the recursion process matching multigroups will be greedily matched and successively excised from the dimension string.

        Matcher multiGroupDimensionRegExMatcher = dimensionComponentDefinerBuilder.createMultiGroupRegExPattern().matcher(dimensionString);

        /*There is exists an edgecase where a unit definition classified as a single groups is also classified as
         *  a mutltigroup by the respective regexes. This edgecase is explicitly when an atomic type
         *  is bounded by parentheses and raised to an exponent, ie '(meter)^2'.
         *  Rather than modify and over complicate the mutligroup regex to not match this special,
         *  there will just be a second to make sure that single group regex does not also match the multigroup.
         */
        while (multiGroupDimensionRegExMatcher.find()
                && !dimensionComponentDefinerBuilder.createSingleGroupRegExPattern().matcher(multiGroupDimensionRegExMatcher.group()).matches()) {
            String multiGroupDimension = multiGroupDimensionRegExMatcher.group();

            //
            String[] parsedExponentGroup = parseExponentGroup(multiGroupDimension);
            double currentExponent = Double.valueOf(parsedExponentGroup[1]);

            //Force a new nested multigroup search to occur in preparation for recursion.
            String multiGroupDimensionToBeRecursed = multiGroupDimension.substring(0, multiGroupDimension
                    .lastIndexOf(parsedExponentGroup[0]))
                    .replaceFirst("^[^(]*\\(", "").replaceFirst("\\)$", "")
                    .trim();

            //Passes on multigroup into next level of recursion making sure exponents are properly accumulated and successively passed on.
            parseNestedMultiGroups(multiGroupDimensionToBeRecursed
                    , recursedExponent * currentExponent
                    , dimensionMap);

            truncatedDimensionString = truncatedDimensionString.replace(multiGroupDimension, "");
        }

        /*If for some reason the remaining truncated dimension string still appears to have multi groups,
         *then multigroup parsing had failed due to some incorrectly formatted token somewhere.
         */
        if (DimensionComponentDefinerBuilder.hasNestedParentheses(truncatedDimensionString)
                || dimensionComponentDefinerBuilder.hasNestedExponents(truncatedDimensionString)) {
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

        if (!truncatedDimensionString.isEmpty()) {
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

        Matcher singleGroupDimensionRegExMatcher = dimensionComponentDefinerBuilder.createSingleGroupRegExPattern().matcher(dimensionString);

        while (singleGroupDimensionRegExMatcher.find()) {
            String singleGroupDimension = singleGroupDimensionRegExMatcher.group();

            //
            String[] parsedExponentGroup = parseExponentGroup(singleGroupDimension);
            double currentExponent = Double.valueOf(parsedExponentGroup[1]);

            //
            String atomicType = parseAtomicTypeGroup(singleGroupDimension
                            .replaceAll(dimensionComponentDefinerBuilder.createDivisionSymbolsRegEx(), "")
                            .replaceAll(dimensionComponentDefinerBuilder.createMultiplicationSymbolsRegEx(), ""));

            if (atomicType.isEmpty())
                throw new ParsingException(String.format("No atomic type can be extracted using atomicTypeRegExPattern '%s'" +
                                " from the group '%s' that was produced by the singleGroupDimensionRegExPattern '%s'."
                        , dimensionComponentDefinerBuilder.getAtomicTypeRegEx(), singleGroupDimension, dimensionComponentDefinerBuilder.createSingleGroupRegExPattern().pattern())
                        , "The group regular expression must correspond with atomic type regular expressions.");

            //
            parsedDimensionUpdater.updateDimension(atomicType, outerExponent * currentExponent, dimensionMap);
        }

        //All single groups will be successively excised until hopefully there is nothing left.
        String truncatedDimensionString = singleGroupDimensionRegExMatcher.replaceAll("");
        if (!truncatedDimensionString.isEmpty()) {
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
    private String[] parseExponentGroup(String dimension) throws ParsingException {
        Matcher exponentRegExMatcher =  Pattern.compile(dimensionComponentDefinerBuilder.createExponentGroupRegex()+"$").matcher(dimension);
        Matcher divisionSymbolRegExMatcher = Pattern.compile(dimensionComponentDefinerBuilder.createDivisionSymbolsRegEx()).matcher(dimension);

        //Division inverts the exponent
        String exponentValueSignBasedOnOperation = (divisionSymbolRegExMatcher.find()
                && dimension.trim().startsWith(divisionSymbolRegExMatcher.group())) ? "-" : "";

        if (exponentRegExMatcher.find()) {
            String exponentGroup = exponentRegExMatcher.group();
            Matcher exponentValueRegExMatcher = Pattern.compile(dimensionComponentDefinerBuilder.getExponentValueRegEx()+"$").matcher(exponentGroup);

            if (!exponentValueRegExMatcher.find())
                throw new ParsingException(String.format("No value can be extracted using exponentValueRegExPattern '%s' " +
                                "from the group '%s' that was produced by the exponentRegExPattern '%s'."
                        , dimensionComponentDefinerBuilder.getExponentValueRegEx(), exponentGroup, dimensionComponentDefinerBuilder.createExponentGroupRegex())
                        , "The group regular expression must correspond with value regular expression.");

            //By this point the parsing tree any present operation token was already validated as being suitable for division or multiplication
            //Therefore any operation token present other than the division is a multiplication
            String exponentValue = (exponentValueSignBasedOnOperation + exponentValueRegExMatcher.group())
                    .replace("--", ""); //negatives cancel
            return new String[]{exponentGroup, exponentValueFormatter.format(exponentValue)};

        } else {
            //If not explicitly raised to anything then assume 1 (+/- depending on presence of division)
            return new String[]{"", exponentValueFormatter.format(exponentValueSignBasedOnOperation + "1")};
        }
    }

    /**
     * Identifies and extract one atomic type. ex. 'meter' if a unit name, LENGTH if a fundamental dimension
     * @return String of extracted atomic type or an empty string.
     */
    private String parseAtomicTypeGroup(String dimension) {
        Matcher atomicTypeRegExMatcher = Pattern.compile(dimensionComponentDefinerBuilder.getAtomicTypeRegEx()).matcher(dimension);

        if (atomicTypeRegExMatcher.find())
            return exponentValueFormatter.format(atomicTypeRegExMatcher.group());

        return "";
    }

    ///
    private List<String> determineInvalidOrMissingComponentsNeededForParsing() {
        List<String> invalidParsingComponents = new ArrayList<>();

        if (parsedDimensionUpdater == null)
            invalidParsingComponents.add("{ An instance satisfying IParsedDimensionUpdater }");

        if (dimensionComponentDefinerBuilder == null)
            invalidParsingComponents.add("{ An instance satisfying DimensionComponentDefinerBuilder }");

        if(exponentValueFormatter == null)
            invalidParsingComponents.add("{ A formatter for the parsed exponent value }");

        if(atomicTypeFormatter == null)
            invalidParsingComponents.add("{ A formatter for the parsed atomic type }");

        return invalidParsingComponents;
    }
    
    
    ///
    public DimensionParserBuilder<T> setAtomicTypeRegEx(String atomicTypeRegEx) {
        dimensionComponentDefinerBuilder.setAtomicTypeRegEx(atomicTypeRegEx);
        return this;
    }


    public DimensionParserBuilder<T> setExponentSymbols(String[] exponentSymbols) {
        dimensionComponentDefinerBuilder.setExponentSymbols(exponentSymbols);
        return this;
    }

    public DimensionParserBuilder<T> setExponentValueRegEx(String exponentValueRegEx) {
        dimensionComponentDefinerBuilder.setExponentValueRegEx(exponentValueRegEx);
        return this;
    }

    public DimensionParserBuilder<T> setDivisionSymbols(String[] divisionSymbols) {
        dimensionComponentDefinerBuilder.setDivisionSymbols(divisionSymbols);
        return this;
    }

    public DimensionParserBuilder<T> setMultiplicationSymbols(String[] multiplicationSymbols) {
        dimensionComponentDefinerBuilder.setMultiplicationSymbols(multiplicationSymbols);
        return this;
    }

    ///
    public DimensionParserBuilder<T> setParsedDimensionUpdater(IParsedDimensionUpdater<T> parsedDimensionUpdater) {
        this.parsedDimensionUpdater = parsedDimensionUpdater;
        return this;
    }
    public IParsedDimensionUpdater<T> getParsedDimensionUpdater() {
        return parsedDimensionUpdater;
    }

    public DimensionParserBuilder<T> setDimensionComponentDefinerBuilder(DimensionComponentDefinerBuilder dimensionComponentDefinerBuilder) {
        this.dimensionComponentDefinerBuilder = dimensionComponentDefinerBuilder;
        return this;
    }
    public DimensionComponentDefinerBuilder getDimensionComponentDefinerBuilder() {
        return dimensionComponentDefinerBuilder;
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

    public DimensionParserBuilder<T> setStrictParsing(boolean strictParsing) {
        this.strictParsing = strictParsing;
        return this;
    }
    public boolean isStrictParsing() {
        return strictParsing;
    }
}
