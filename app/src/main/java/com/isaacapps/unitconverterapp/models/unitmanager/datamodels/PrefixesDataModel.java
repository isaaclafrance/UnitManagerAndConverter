package com.isaacapps.unitconverterapp.models.unitmanager.datamodels;

import com.florianingerl.util.regex.Matcher;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.IDualKeyNCategoryRepository;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsContentDeterminer.DATA_MODEL_CATEGORY;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsDataModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import static com.isaacapps.unitconverterapp.utilities.RegExUtility.SIGNED_DOUBLE_VALUE_REGEX_PATTERN;

public class PrefixesDataModel extends BaseDataModel<String, Double, DATA_MODEL_CATEGORY> {
    public static final String[] NO_PREFIX_MATCH_ARRAY = new String[0]; //constant empty string array with no expectation of addition so that a new does not need to be initialized each time.

    private UnitsDataModel unitsDataModel;
    //Used to limit prefix search in provided string to character range where prefixes are expected to be found. Usually a small value since most prefix have small character count.
    private int maxPrefixCharacterLength;
    private final Comparator<String> stringCharacterLengthComparator;

    private boolean initiallySearchByPrefixValue;

    ///
    public PrefixesDataModel() {
        initiallySearchByPrefixValue = false;
        stringCharacterLengthComparator = new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return Integer.compare(s1.length(), s2.length());
            }
        };
    }
    public PrefixesDataModel(IDualKeyNCategoryRepository<String, Double, DATA_MODEL_CATEGORY> repositoryWithDualKeyNCategory){
        this();
        this.repositoryWithDualKeyNCategory = repositoryWithDualKeyNCategory;
    }

    ///Modify Content
    public boolean addCorePrefix(String prefixName, String abbreviation, double prefixValue) {
        return addPrefixToDataStructures(DATA_MODEL_CATEGORY.CORE, prefixName, abbreviation, prefixValue);
    }
    public boolean addDynamicPrefix(String prefixName, String abbreviation, double prefixValue) {
        return addPrefixToDataStructures(DATA_MODEL_CATEGORY.DYNAMIC, prefixName, abbreviation, prefixValue);
    }
    private boolean addPrefixToDataStructures(DATA_MODEL_CATEGORY prefixCategory, String prefixName, String abbreviation, double prefixValue){
        if(repositoryWithDualKeyNCategory.addItem(prefixCategory, prefixName.toLowerCase(), abbreviation, prefixValue) != null)
        {
            adjustMaxPrefixNameCharacterLengthAfterAddition(prefixName);
            adjustMaxPrefixNameCharacterLengthAfterAddition(abbreviation);
            return true;
        }
        return false;
    }

    public void removePrefix(String prefixName) {
        if(getDataModelCategory(prefixName) != DATA_MODEL_CATEGORY.CORE) {
            repositoryWithDualKeyNCategory.removeItemByKey(prefixName);
            if (prefixName.length() == maxPrefixCharacterLength) {
                //Calculate what the next second largest prefix is, but could still be the same if there is still a remaining prefix of same size.
                adjustMaxPrefixCharacterLenghtAfterRemoval();

            }
        }
    }

    //Ideally this should only be called by the unit manager builder
    public void removeAllCorePrefixes() {
        repositoryWithDualKeyNCategory.removeCategory(DATA_MODEL_CATEGORY.CORE);
        adjustMaxPrefixCharacterLenghtAfterRemoval();
    }
    public void removeAllDynamicPrefixes() {
        repositoryWithDualKeyNCategory.removeCategory(DATA_MODEL_CATEGORY.DYNAMIC);
        adjustMaxPrefixCharacterLenghtAfterRemoval();
    }

    //
    private void adjustMaxPrefixNameCharacterLengthAfterAddition(String prefixName){
        if(prefixName.length() > maxPrefixCharacterLength)
            maxPrefixCharacterLength = prefixName.length();
    }
    private void adjustMaxPrefixCharacterLenghtAfterRemoval(){
        //Unlike addition of prefixes, removal requires full recalculation of prefix max character length to account for remaining prefixes.
        int prefixFullNameMaxLength = !getAllPrefixFullNames().isEmpty() ? Collections.max(getAllPrefixFullNames(), stringCharacterLengthComparator).length() : 0;
        int prefixAbbreviationMaxLength = !getAllPrefixAbbreviations().isEmpty() ? Collections.max(getAllPrefixAbbreviations(), stringCharacterLengthComparator).length() : 0;

        maxPrefixCharacterLength = Math.max(prefixAbbreviationMaxLength, prefixFullNameMaxLength);
    }

    //
    public boolean combineWith(PrefixesDataModel otherPrefixDataModel){
        if(otherPrefixDataModel != null){
            this.maxPrefixCharacterLength = Math.max(this.maxPrefixCharacterLength, otherPrefixDataModel.maxPrefixCharacterLength);
        }
        return super.combineWith(otherPrefixDataModel);
    }

    ///Retrieve Prefixes
    /**
     * Near constant time retrieval of value associated with a prefix. If provided prefix is invalid, then result is zero.
     * @param prefixName Valid prefix full name or abbreviation
     * @return
     */
    public double getPrefixValue(String prefixName) {
        return tryGet(repositoryWithDualKeyNCategory.getFirstItemByAnyKey(prefixName.toLowerCase()), 0.0);
    }

    public String getPrefixAbbreviation(String prefixFullName) {
        return tryGet(repositoryWithDualKeyNCategory.getKey2FromKey1(prefixFullName.toLowerCase()), "");
    }
    public String getPrefixAbbreviation(double prefixValue){
        if(repositoryWithDualKeyNCategory.containsItemInCategory(DATA_MODEL_CATEGORY.CORE, prefixValue)){
            return repositoryWithDualKeyNCategory.getKey2(DATA_MODEL_CATEGORY.CORE, prefixValue);
        }
        else{
            return tryGet(repositoryWithDualKeyNCategory.getKey2(DATA_MODEL_CATEGORY.DYNAMIC, prefixValue), "");
        }
    }

    public String getPrefixFullName(String prefixAbbreviationOrRegionalAlias) {
        return tryGet(repositoryWithDualKeyNCategory.getKey1FromKey2(prefixAbbreviationOrRegionalAlias.toLowerCase()), "");
    }
    public String getPrefixFullName(double prefixValue){
        if(repositoryWithDualKeyNCategory.containsItemInCategory(DATA_MODEL_CATEGORY.CORE, prefixValue)){
            return repositoryWithDualKeyNCategory.getKey1(DATA_MODEL_CATEGORY.CORE, prefixValue);
        }
        else{
            return tryGet(repositoryWithDualKeyNCategory.getKey1(DATA_MODEL_CATEGORY.DYNAMIC, prefixValue), "");
        }
    }

    private <T> T tryGet(T value, T nullCase){
        if (value != null) {
            return value;
        } else {
            return nullCase;
        }
    }

    public Collection<String> getAllPrefixFullNames() {
        return repositoryWithDualKeyNCategory.getAllKey1s();
    }
    public Collection<String> getAllPrefixAbbreviations() {
        return repositoryWithDualKeyNCategory.getAllKey2s();
    }

    ///Query for Existence of Prefixes

    /**
     * Near constant time look up for whether data structure has an instance of specified prefix full name or prefix abbreviation.
     */
    public boolean containsPrefixName(String name) {
        return repositoryWithDualKeyNCategory.containsKey(name.toLowerCase());
    }
    /**
     * Near constant time look up for whether data structure has any prefix instance associated with value.
     */
    public boolean containsPrefixValue(double value) {
        return repositoryWithDualKeyNCategory.containsItem(value);
    }

    public boolean hasCorePrefixes() {
        return !repositoryWithDualKeyNCategory.getItemsByCategory(DATA_MODEL_CATEGORY.CORE).isEmpty();
    }
    public boolean hasDynamicPrefixes() {
        return !repositoryWithDualKeyNCategory.getItemsByCategory(DATA_MODEL_CATEGORY.DYNAMIC).isEmpty();
    }

    ///

    /**
     * Picks out the prefix full name, abbreviation, or value combination that can be found in the name.
     * Optionally constrains based on if the remaining unit name component is associated with a valid unit found in units datamodel
     * and if both the prefix(if not a value) and unit name are validly of the same kind. i.e. both are abbreviations or full names.
     *
     * @param unitNameWithPrefix             Unit name prepended with a prefix. ie. 1000g (less typical) , kg, kilogram
     * @param constrainBasedOnValidUnitNames Indicates whether to constrain based on valid unit name and whether  unit and prefix are of same kind.
     * @return  Collection of String arrays. Each array is constructed such that the prefix full name is the first item, the abbreviation is the second item, the prefix less name is the third item.
     */
    public Collection<String[]> findAllPrefixPairMatches(String unitNameWithPrefix, boolean constrainBasedOnValidUnitNames) {
        return findPrefixPairMatches(unitNameWithPrefix, constrainBasedOnValidUnitNames, false);
    }
    /**
     * Picks out the prefix full name, abbreviation, or value combination that can be found in the name.
     * Optionally constrains based on if the remaining unit name component is associated with a valid unit found in units datamodel
     * and if both the prefix(if not a value) and unit name are validly of the same kind. i.e. both are abbreviations or full names.
     *
     * @param unitNameWithPrefix             Unit name prepended with a prefix. ie. 1000g (less typical) , kg, kilogram
     * @param constrainBasedOnValidUnitNames Indicates whether to constrain based on valid unit name and whether  unit and prefix are of same kind.
     * @return  An array constructed such that the prefix full name is the first item, the abbreviation is the second item, the prefix less name is the third item.
     * If there are no matches, then the result is an empty array with no elements.
     */
    public String[] findFirstPrefixPairMatches(String unitNameWithPrefix, boolean constrainBasedOnValidUnitNames) {
        Collection<String[]> prefixMatches = findPrefixPairMatches(unitNameWithPrefix, constrainBasedOnValidUnitNames, true);
        if(!prefixMatches.isEmpty()){
            return prefixMatches.iterator().next();
        }
        return NO_PREFIX_MATCH_ARRAY;
    }
    /**
     * Picks out the prefix full name, abbreviation, or value combination that can be found in the name.
     * Optionally constrains based on if the remaining unit name component is associated with a valid unit found in content
     * and if the both the prefix(if not a value) and unit name are validly of the same kind. i.e. both are abbreviations or full names.
     *
     * @param unitNameWithPrefix             Unit name prepended with a prefix. ie. 1000g (less typical) , kg, kilogram
     * @param constrainBasedOnValidUnitNames Indicates whether to constrain based on valid unit name and whether  unit and prefix are of same kind.
     * @return  Collection of String arrays. Each array is constructed such that the prefix full name is the first item, the abbreviation is the second item, the prefix less name is the third item.
     * If there are no matches, then the result is an empty array.
     */
    private Collection<String[]> findPrefixPairMatches(String unitNameWithPrefix, boolean constrainBasedOnValidUnitNames, boolean onlyFirstMatch) {
        Collection<String[]> prefixMatches = new ArrayList<>();

        String[] prefixMatchByValue = findPrefixMatchByValue(unitNameWithPrefix, constrainBasedOnValidUnitNames);
        if(prefixMatchByValue != NO_PREFIX_MATCH_ARRAY)
            prefixMatches.add(prefixMatchByValue);

        prefixMatches.addAll(findPrefixMatchesByName(unitNameWithPrefix, constrainBasedOnValidUnitNames, onlyFirstMatch));

        for(String[] prefixMatch:prefixMatches)
            setProperlyOrderedPrefixNamePairs(prefixMatch);

        return prefixMatches;
    }

    private Collection<String[]> findPrefixMatchesByName(String unitNameWithPrefix, boolean constrainBasedOnValidUnitNames, boolean onlyFirstMatch){
        Collection<String[]> prefixMatches = new ArrayList<>();

        //Prevent out of bound exception when extracting substring;
        int adjustedMaxPrefixCharacterLength = (maxPrefixCharacterLength > unitNameWithPrefix.length())?unitNameWithPrefix.length():maxPrefixCharacterLength;

        //Search is naturally faster when the provided prefixed unit name valid and is an abbreviation, in such cases usually only one or two passes is needed for identification.
        for(int prefixCharLenBoundary = 1; prefixCharLenBoundary <= adjustedMaxPrefixCharacterLength ; prefixCharLenBoundary++){
            String potentialPrefixName = unitNameWithPrefix.substring(0, prefixCharLenBoundary);
            String unitNameWithoutPrefix = unitNameWithPrefix.substring(prefixCharLenBoundary);

            if(containsPrefixName(potentialPrefixName)
                    && (!constrainBasedOnValidUnitNames || (constrainedOnValidUnitName(unitNameWithoutPrefix) && validatePrefixNUnitNameAreSameKind(potentialPrefixName, unitNameWithoutPrefix))))
            {
                prefixMatches.add(new String[]{potentialPrefixName, "", unitNameWithoutPrefix});
                if(onlyFirstMatch)
                    return prefixMatches;
            }
        }

        return prefixMatches;
    }
    private String[] findPrefixMatchByValue(String unitNameWithPrefix, boolean constrainBasedOnValidUnitNames){
        if(!initiallySearchByPrefixValue)
            return NO_PREFIX_MATCH_ARRAY;

        if(!Character.isDigit(unitNameWithPrefix.charAt(0)))
            return NO_PREFIX_MATCH_ARRAY;

        String potentialPrefixValue = "";
        Matcher prefixValueMatcher = SIGNED_DOUBLE_VALUE_REGEX_PATTERN.matcher(unitNameWithPrefix);

        if(prefixValueMatcher.find()) {
            potentialPrefixValue = prefixValueMatcher.group();
        }

        String unitNameWithoutPrefix = unitNameWithPrefix.substring(potentialPrefixValue.length());
        if(containsPrefixValue(Double.parseDouble(potentialPrefixValue))
                && (!constrainBasedOnValidUnitNames || constrainedOnValidUnitName(unitNameWithoutPrefix)))
        {
            return new String[]{getPrefixFullName(Double.parseDouble(potentialPrefixValue)), "", unitNameWithoutPrefix};
        }

        return NO_PREFIX_MATCH_ARRAY;
    }

    private boolean constrainedOnValidUnitName(String unitNameWithoutPrefix){
        //An empty string would not have matched any unit.
        if(unitNameWithoutPrefix.isEmpty())
            return false;

        return unitsDataModel != null && unitsDataModel.getUnitsContentQuerier().containsUnit(unitNameWithoutPrefix);
    }
    private boolean validatePrefixNUnitNameAreSameKind(String prefix, String unitNameWithoutPrefix){
        return (repositoryWithDualKeyNCategory.isKey1(prefix) && unitsDataModel.getUnitsContentMainRetriever().getAllUnitFullNames().contains(unitNameWithoutPrefix.toLowerCase())
                || repositoryWithDualKeyNCategory.isKey2(prefix) && unitsDataModel.getUnitsContentMainRetriever().getAllUnitAbbreviatedNames().contains(unitNameWithoutPrefix));
    }
    /**
     * Given a three element array with any single full or abbreviated prefix name as the first element, returns a modified array always with the prefix full name first and the prefix abbreviation second.
     * @param prefixMatchArray A prefix full name or abbreviation name
     */
    private void setProperlyOrderedPrefixNamePairs(String[] prefixMatchArray){
        prefixMatchArray[0] = repositoryWithDualKeyNCategory.isKey2(prefixMatchArray[0]) ? repositoryWithDualKeyNCategory.getKey1FromKey2(prefixMatchArray[0]) : prefixMatchArray[0];
        prefixMatchArray[1] = repositoryWithDualKeyNCategory.isKey2(prefixMatchArray[0]) ? prefixMatchArray[0] : repositoryWithDualKeyNCategory.getKey2FromKey1(prefixMatchArray[0]);
    }

    ///
    /**
     * Determines if prefix full name, abbreviation, or value can be found in the name.
     * Optionally constrains based on if the unit name component is associated with a valid unit.
     *
     * @param unitNameWithPrefix             Unit name prepended with a prefix. ie. 1000g, kg, kilogram
     * @param constrainBasedOnValidUnitNames Indicates whether to constrain based on valid unit name and whether  unit and prefix are of same kind.
     */
    public boolean unitNameHasPrefix(String unitNameWithPrefix, boolean constrainBasedOnValidUnitNames){
        return findFirstPrefixPairMatches(unitNameWithPrefix, constrainBasedOnValidUnitNames) != NO_PREFIX_MATCH_ARRAY;
    }

    ///
    public DATA_MODEL_CATEGORY getDataModelCategory(String prefixName) {
        return repositoryWithDualKeyNCategory.getCategoryOfKey(prefixName);
    }

    public boolean isInitiallySearchByPrefixValue() {
        return initiallySearchByPrefixValue;
    }

    /**
     * Determines if the prefix parsing search should also attempt to find instances where unit name is preceded by a prefix value instead of just by prefix name.
     * For example, 1000g rather than just kilogram or kg.
     */
    public void setInitiallySearchByPrefixValue(boolean initiallySearchByPrefixValue) {
        this.initiallySearchByPrefixValue = initiallySearchByPrefixValue;
    }

    ///
    public void setUnitsDataModel(UnitsDataModel unitsDataModel) {
        this.unitsDataModel = unitsDataModel;
    }
}

