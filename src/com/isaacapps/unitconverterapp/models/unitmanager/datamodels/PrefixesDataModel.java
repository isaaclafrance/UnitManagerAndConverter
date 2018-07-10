package com.isaacapps.unitconverterapp.models.unitmanager.datamodels;

import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.IDualKeyNCategoryRepository;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.ContentDeterminer.DATA_MODEL_CATEGORY;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsDataModel;

import java.util.ArrayList;
import java.util.Collection;

public class PrefixesDataModel extends BaseDataModel<String, Double, DATA_MODEL_CATEGORY> {
    private UnitsDataModel unitsDataModel;

    ///
    public PrefixesDataModel() {
    }
    public PrefixesDataModel(IDualKeyNCategoryRepository<String, Double, DATA_MODEL_CATEGORY> repositoryWithDualKeyNCategory){
        this.repositoryWithDualKeyNCategory = repositoryWithDualKeyNCategory;
    }

    ///Modify Content
    public void addCorePrefix(String prefixName, String abbreviation, double prefixValue) {
        repositoryWithDualKeyNCategory.addItem(DATA_MODEL_CATEGORY.CORE, prefixName.toLowerCase(), abbreviation, prefixValue);
    }

    public void addDynamicPrefix(String prefixName, String abbreviation, double prefixValue) {
        repositoryWithDualKeyNCategory.addItem(DATA_MODEL_CATEGORY.DYNAMIC, prefixName.toLowerCase(), abbreviation, prefixValue);
    }


    public void removePrefix(String prefixName) {
        repositoryWithDualKeyNCategory.removeItemByKey(prefixName);
    }

    public void removeAllCorePrefixes() {
        repositoryWithDualKeyNCategory.removeCategory(DATA_MODEL_CATEGORY.CORE);
    }

    public void removeAllDynamicPrefixes() {
        repositoryWithDualKeyNCategory.removeCategory(DATA_MODEL_CATEGORY.DYNAMIC);
    }

    ///Retrieve Prefixes
    public double getPrefixValue(String prefixName) {
        Double prefixValue = repositoryWithDualKeyNCategory.getFirstItemByAnyKey(prefixName.toLowerCase());
        if (prefixValue != null) {
            return prefixValue;
        } else {
            return 0.0;
        }
    }

    public String getPrefixAbbreviation(String prefixFullName) {
        String prefixAbbreviation = repositoryWithDualKeyNCategory.getKey2FromKey1(prefixFullName.toLowerCase());
        if (prefixAbbreviation != null) {
            return prefixAbbreviation;
        } else {
            return "";
        }
    }

    public String getPrefixFullName(String prefixAbbreviationOrRegionalAlias) {
        String prefixFullName = repositoryWithDualKeyNCategory.getKey1FromKey2(prefixAbbreviationOrRegionalAlias.toLowerCase());
        if (prefixFullName != null) {
            return prefixFullName;
        } else {
            return "";
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
     * Picks out the prefix full name and abbreviation combination that can be found in the name.
     * Optionally constrains based on if the unit name component is associated with a valid unit.
     *
     * @param name                           Unit name prepended with a prefix
     * @param constrainBasedOnValidUnitNames Indicates whether to use the units data model from the context
     *                                       to determine whether the prefix is prepended on valid unit name.
     * @return Prefix matches. For each match in the arraylist, the full name is the first item in the array and the abbreviation is the second.
     */
    public ArrayList<String[]> getPrefixMatches(String name, boolean constrainBasedOnValidUnitNames) {
        ArrayList<String[]> prefixMatches = new ArrayList<>();
        ArrayList<String> allPrefixFullNameAndAbbreviations = new ArrayList<>(getAllPrefixFullNames());
        allPrefixFullNameAndAbbreviations.addAll(getAllPrefixAbbreviations());

        for (String prefix : allPrefixFullNameAndAbbreviations) {
            /*If the constrainBasedOnValidUnitNames constraint is set to true and the unit manager is not null,
             *then select prefixes such that when they are removed the remaining text is a valid unit name.
             *A prefix and unit match is valid only if prefix and unit are BOTH abbreviations or BOTH full names
             *-- must account for edge cases where full name is the same as the abbreviation.
             *Otherwise simply select prefixes that can be found at the beginning of the specified name
             */
            String unitName = name.replaceFirst(prefix, "").replaceAll("//A_|_//Z", "");
            boolean prefixFoundAtBeginning = name.indexOf(prefix) == 0,
                    unitNameIsValid = unitsDataModel != null && unitsDataModel.getContentQuerier().containsUnit(unitName),
                    prefixAndUnitAreSameKindOfName = repositoryWithDualKeyNCategory.isKey1(prefix) && unitsDataModel.getContentMainRetriever().getAllUnitFullNames().contains(unitName)
                            || repositoryWithDualKeyNCategory.isKey2(prefix) && unitsDataModel.getContentMainRetriever().getAllUnitAbbreviatedNames().contains(unitName);

            if (prefixFoundAtBeginning
                    && (constrainBasedOnValidUnitNames && unitNameIsValid && prefixAndUnitAreSameKindOfName
                    || !constrainBasedOnValidUnitNames)) {
                //Make sure prefix full name is first item in the array and the abbreviation is the second item.
                prefixMatches.add(new String[]{repositoryWithDualKeyNCategory.isKey2(prefix) ? repositoryWithDualKeyNCategory.getKey1FromKey2(prefix) : prefix
                        , repositoryWithDualKeyNCategory.isKey2(prefix) ? prefix : repositoryWithDualKeyNCategory.getKey2FromKey1(prefix)});
            }
        }

        return prefixMatches;
    }

    ///
    public DATA_MODEL_CATEGORY getModelDataType(String prefixName) {
        return repositoryWithDualKeyNCategory.getCategoryOfKey(prefixName);
    }

    ///
    public void setUnitsDataModel(UnitsDataModel unitsDataModel) {
        this.unitsDataModel = unitsDataModel;
    }
}

