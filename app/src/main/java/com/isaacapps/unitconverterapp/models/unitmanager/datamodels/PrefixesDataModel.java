package com.isaacapps.unitconverterapp.models.unitmanager.datamodels;

import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.IDualKeyNCategoryRepository;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsContentDeterminer.DATA_MODEL_CATEGORY;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsDataModel;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class PrefixesDataModel extends BaseDataModel<String, Double, DATA_MODEL_CATEGORY> {
    private final String[] NO_PREFIX_MATCH_ARRAY = new String[0];

    private UnitsDataModel unitsDataModel;
    protected int maxPrefixCharacterLength; //Used to limits prefix search in provided string to character range where prefixes are expected to be found.
    private final Comparator<String> characterLengthComparator;

    ///
    public PrefixesDataModel() {
        characterLengthComparator = new Comparator<String>() {
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
        adjustMaxPrefixCharacterLengthAfterAddition(prefixName);
        adjustMaxPrefixCharacterLengthAfterAddition(abbreviation);
    }

    public void addDynamicPrefix(String prefixName, String abbreviation, double prefixValue) {
        repositoryWithDualKeyNCategory.addItem(DATA_MODEL_CATEGORY.DYNAMIC, prefixName.toLowerCase(), abbreviation, prefixValue);
        adjustMaxPrefixCharacterLengthAfterAddition(prefixName);
        adjustMaxPrefixCharacterLengthAfterAddition(abbreviation);
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
    private void adjustMaxPrefixCharacterLengthAfterAddition(String prefix){
        if(prefix.length() > maxPrefixCharacterLength)
            maxPrefixCharacterLength = prefix.length();
    }

    private void adjustMaxPrefixCharacterLenghtAfterRemoval(){
        //Unlike addition of prefixes, removal requires full recalculation of prefix max character length since
        int prefixFullNameMaxLength = Collections.max(getAllPrefixFullNames(), characterLengthComparator).length();
        int prefixAbbreviationMaxLength = Collections.max(getAllPrefixAbbreviations(), characterLengthComparator).length();

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
    public boolean containsPrefix(String name) {
        return repositoryWithDualKeyNCategory.containsKey(name);
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
     * Optionally constrains based on if the unit name component is associated with a valid unit.
     *
     * @param unitNameWithPrefix             Unit name prepended with a prefix. ie. 1000g, kg, kilogram
     * @param constrainBasedOnValidUnitNames Indicates whether to use the units data model from the context
     *                                       to determine whether the prefix is prepended on valid unit name.
     * @return Array with prefix matches. The full name is the first item in the array and the abbreviation is the second.
     * If there are no matches, then the result is an empty array.
     */
    public String[] findPrefixMatch(String unitNameWithPrefix, boolean constrainBasedOnValidUnitNames) {
        /*If the constrainBasedOnValidUnitNames constraint is set to true and the unit manager is not null,
         *then select prefixes such that when they are removed the remaining text is a valid unit name.
         *A prefix and unit match is valid only if prefix and unit are BOTH abbreviations or BOTH full names or the prefix part was specified as value to begin with.
         *Otherwise simply select prefixes that can be found at the beginning of the specified name
         */
        if(unitNameWithPrefix.contains("_"))
            return NO_PREFIX_MATCH_ARRAY;

        for(int prefixCharLenBoundary = maxPrefixCharacterLength; prefixCharLenBoundary > 0 ; prefixCharLenBoundary--){
            String potentialPrefix = unitNameWithPrefix.substring(0, prefixCharLenBoundary);
            Double prefixValue = 0.0;

            if(!containsPrefix(potentialPrefix) &&
                    Character.isDigit(potentialPrefix.charAt(0)) && Character.isDigit(potentialPrefix.charAt(potentialPrefix.length()-1))){ // At minimum, the first and last characters need to be digits inorder for the whole prefix to be a number
                try {
                    prefixValue = Double.valueOf(potentialPrefix);
                    if(!containsPrefixValue(prefixValue)) {
                        continue;
                    }else {
                        potentialPrefix = getPrefixAbbreviation(prefixValue);
                    }
                }catch(NumberFormatException e){
                    continue;
                }
            }

            String unitNameWithoutPrefix = unitNameWithPrefix.substring(potentialPrefix.length());

            boolean unitNameIsValid = unitsDataModel != null && unitsDataModel.getUnitsContentQuerier().containsUnit(unitNameWithoutPrefix);

            boolean prefixAndUnitAreSameKindOfName = prefixValue != 0.0
                    && (repositoryWithDualKeyNCategory.isKey1(potentialPrefix) && unitsDataModel.getUnitsContentMainRetriever().getAllUnitFullNames().contains(unitNameWithoutPrefix)
                    || repositoryWithDualKeyNCategory.isKey2(potentialPrefix) && unitsDataModel.getUnitsContentMainRetriever().getAllUnitAbbreviatedNames().contains(unitNameWithoutPrefix));

            if ( !constrainBasedOnValidUnitNames || unitNameIsValid && prefixAndUnitAreSameKindOfName) {
                //Make sure prefix full name is first item in the array and the abbreviation is the second item.
                return new String[]{repositoryWithDualKeyNCategory.isKey2(potentialPrefix) ? repositoryWithDualKeyNCategory.getKey1FromKey2(potentialPrefix) : potentialPrefix
                        , repositoryWithDualKeyNCategory.isKey2(potentialPrefix) ? potentialPrefix : repositoryWithDualKeyNCategory.getKey2FromKey1(potentialPrefix)};
            }
            else{
                return NO_PREFIX_MATCH_ARRAY;
            }
        }

        return NO_PREFIX_MATCH_ARRAY;
    }

    /**
     * Determines if prefix full name, abbreviation, or value combination can be found in the name.
     * Optionally constrains based on if the unit name component is associated with a valid unit.
     *
     * @param unitNameWithPrefix             Unit name prepended with a prefix. ie. 1000g, kg, kilogram
     * @param constrainBasedOnValidUnitNames Indicates whether to use the units data model from the context
     *                                       to determine whether the prefix is prepended on valid unit name.
     */
    public boolean hasPrefix(String unitNameWithPrefix, boolean constrainBasedOnValidUnitNames){
        return findPrefixMatch(unitNameWithPrefix, constrainBasedOnValidUnitNames).length != 0;
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

