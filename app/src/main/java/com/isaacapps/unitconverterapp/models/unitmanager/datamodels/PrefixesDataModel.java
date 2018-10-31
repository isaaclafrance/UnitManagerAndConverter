package com.isaacapps.unitconverterapp.models.unitmanager.datamodels;

import com.florianingerl.util.regex.Matcher;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.IDualKeyNCategoryRepository;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsContentDeterminer.DATA_MODEL_CATEGORY;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsDataModel;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import static com.isaacapps.unitconverterapp.utilities.RegExUtility.SIGNED_DOUBLE_VALUE_REGEX_PATTERN;

public class PrefixesDataModel extends BaseDataModel<String, Double, DATA_MODEL_CATEGORY> {
    private static final String[] NO_PREFIX_MATCH_ARRAY = new String[0];

    private UnitsDataModel unitsDataModel;
    //Used to limit prefix search in provided string to character range where prefixes are expected to be found. Usually a small value since most prefix have small character count.
    protected int maxPrefixCharacterLength;
    private final Comparator<String> stringCharacterLengthComparator;

    ///
    public PrefixesDataModel() {
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
    public void addCorePrefix(String prefixName, String abbreviation, double prefixValue) {
        repositoryWithDualKeyNCategory.addItem(DATA_MODEL_CATEGORY.CORE, prefixName.toLowerCase(), abbreviation, prefixValue);
        adjustMaxPrefixNameCharacterLengthAfterAddition(prefixName);
        adjustMaxPrefixNameCharacterLengthAfterAddition(abbreviation);
    }

    public void addDynamicPrefix(String prefixName, String abbreviation, double prefixValue) {
        repositoryWithDualKeyNCategory.addItem(DATA_MODEL_CATEGORY.DYNAMIC, prefixName.toLowerCase(), abbreviation, prefixValue);
        adjustMaxPrefixNameCharacterLengthAfterAddition(prefixName);
        adjustMaxPrefixNameCharacterLengthAfterAddition(abbreviation);
    }


    public void removePrefix(String prefixName) {
        repositoryWithDualKeyNCategory.removeItemByKey(prefixName);
        adjustMaxPrefixCharacterLenghtAfterRemoval();
    }

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
        //Unlike addition of prefixes, removal requires full recalculation of prefix max character length since
        int prefixFullNameMaxLength = Collections.max(getAllPrefixFullNames(), stringCharacterLengthComparator).length();
        int prefixAbbreviationMaxLength = Collections.max(getAllPrefixAbbreviations(), stringCharacterLengthComparator).length();

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

    private <T, S> T tryGet(T value, T nullCase){
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
    public boolean containsPrefixName(String name) {
        return repositoryWithDualKeyNCategory.containsKey(name.toLowerCase());
    }

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
     * Optionally constrains based on if the remaining unit name component is associated with a valid unit found in content
     * and if the both the prefix(if not a value) and unit name are validly of the same kind. i.e. both are abbreviations or full names.
     *
     * @param unitNameWithPrefix             Unit name prepended with a prefix. ie. 1000g, kg, kilogram
     * @param constrainBasedOnValidUnitNames Indicates whether to constrain based on valid unit name and whether  unit and prefix are of same kind.
     * @return Array with prefix matches. The full name is the first item in the array and the abbreviation is the second.
     * If there are no matches, then the result is an empty array.
     */
    public String[] findPrefixPairMatch(String unitNameWithPrefix, boolean constrainBasedOnValidUnitNames) {
        if(unitNameWithPrefix.contains("_")) //In most case, delimited multi word units usually do not have prefixes.
            return NO_PREFIX_MATCH_ARRAY;

        String prefixMatch = findPrefixMatchByValue(unitNameWithPrefix, constrainBasedOnValidUnitNames);
        if(prefixMatch.isEmpty())
            prefixMatch = findPrefixMatchByName(unitNameWithPrefix, constrainBasedOnValidUnitNames);

        if(!prefixMatch.isEmpty()) {
            return retrievePrefixPairs(prefixMatch);
        }
        else{
            return NO_PREFIX_MATCH_ARRAY;
        }
    }

    public String findPrefixMatchByName(String unitNameWithPrefix, boolean constrainBasedOnValidUnitNames){
        //Prevent out of bound exception when extracting substring;
        int adjustedMaxPrefixCharacterLength = (maxPrefixCharacterLength > unitNameWithPrefix.length())?unitNameWithPrefix.length():maxPrefixCharacterLength;

        for(int prefixCharLenBoundary = 1; prefixCharLenBoundary <= adjustedMaxPrefixCharacterLength ; prefixCharLenBoundary++){
            String potentialPrefixName = unitNameWithPrefix.substring(0, prefixCharLenBoundary);

            if(containsPrefixName(potentialPrefixName)
                    && (!constrainBasedOnValidUnitNames || (validatePrefixConstrainedOnValidUnitName(potentialPrefixName, unitNameWithPrefix) && validatePrefixNUnitNameAreSameKind(potentialPrefixName, unitNameWithPrefix))))
            {
                return potentialPrefixName;
            }
        }

        return "";
    }

    private String findPrefixMatchByValue(String unitNameWithPrefix, boolean constrainBasedOnValidUnitNames){
        if(!Character.isDigit(unitNameWithPrefix.charAt(0)))
            return "";

        String potentialPrefixValue = "";
        Matcher prefixValueMatcher = SIGNED_DOUBLE_VALUE_REGEX_PATTERN.matcher(unitNameWithPrefix);

        if(prefixValueMatcher.find()) {
            potentialPrefixValue = prefixValueMatcher.group();
        }

        if(containsPrefixValue(Double.parseDouble(potentialPrefixValue))
                && (!constrainBasedOnValidUnitNames || validatePrefixConstrainedOnValidUnitName(potentialPrefixValue, unitNameWithPrefix)))
        {
            return getPrefixFullName(Double.parseDouble(potentialPrefixValue));
        }

        return "";
    }

    private boolean validatePrefixConstrainedOnValidUnitName(String prefix, String unitNameWithPrefix){
        //In such a case, the remaining unit name would have been a empty string which would not have matched any unit.
        if(prefix.length() == unitNameWithPrefix.length())
            return false;

        return unitsDataModel != null && unitsDataModel.getUnitsContentQuerier().containsUnit(unitNameWithPrefix.substring(prefix.length()));
    }

    private boolean validatePrefixNUnitNameAreSameKind(String prefix, String unitNameWithoutPrefix){
        return (repositoryWithDualKeyNCategory.isKey1(prefix) && unitsDataModel.getUnitsContentMainRetriever().getAllUnitFullNames().contains(unitNameWithoutPrefix)
                || repositoryWithDualKeyNCategory.isKey2(prefix) && unitsDataModel.getUnitsContentMainRetriever().getAllUnitAbbreviatedNames().contains(unitNameWithoutPrefix));
    }

    /**
     * Given any single full or abbreviated prefix name , returns a two element array always with the full name first and the abbreviation second.
     * @param prefixName A prefix full name or abbreviation name
     */
    private String[] retrievePrefixPairs(String prefixName){
        return new String[]{repositoryWithDualKeyNCategory.isKey2(prefixName) ? repositoryWithDualKeyNCategory.getKey1FromKey2(prefixName) : prefixName
                , repositoryWithDualKeyNCategory.isKey2(prefixName) ? prefixName : repositoryWithDualKeyNCategory.getKey2FromKey1(prefixName)};
    }

    /**
     * Determines if prefix full name, abbreviation, or value combination can be found in the name.
     * Optionally constrains based on if the unit name component is associated with a valid unit.
     *
     * @param unitNameWithPrefix             Unit name prepended with a prefix. ie. 1000g, kg, kilogram
     * @param constrainBasedOnValidUnitNames Indicates whether to constrain based on valid unit name and whether  unit and prefix are of same kind.
     */
    public boolean hasPrefix(String unitNameWithPrefix, boolean constrainBasedOnValidUnitNames){
        return findPrefixPairMatch(unitNameWithPrefix, constrainBasedOnValidUnitNames).length != 0;
    }

    ///
    public DATA_MODEL_CATEGORY getDataModelCategory(String prefixName) {
        return repositoryWithDualKeyNCategory.getCategoryOfKey(prefixName);
    }

    ///
    public void setUnitsDataModel(UnitsDataModel unitsDataModel) {
        this.unitsDataModel = unitsDataModel;
    }
}

