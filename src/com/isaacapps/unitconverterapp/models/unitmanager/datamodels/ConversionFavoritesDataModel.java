package com.isaacapps.unitconverterapp.models.unitmanager.datamodels;

import com.isaacapps.unitconverterapp.models.measurables.quantity.Quantity;
import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.IDualKeyNCategoryRepository;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.hashdriven.SignificanceRankHashedRepository;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.ContentDeterminer;
import com.isaacapps.unitconverterapp.processors.operators.measurables.QuantityOperators;
import com.isaacapps.unitconverterapp.processors.operators.measurables.units.UnitOperators;
import com.isaacapps.unitconverterapp.processors.serializers.SerializingException;

import java.util.ArrayList;
import java.util.Collection;

public class ConversionFavoritesDataModel extends BaseDataModel<String, String, String> {
    private static final String CATEGORY_DELIMITER = ": ";
    private static final String CONVERSION_DELIMITER = " --> ";
    private SignificanceRankHashedRepository significanceRankRepository;

    ///
    public ConversionFavoritesDataModel() {
    }
    public ConversionFavoritesDataModel(IDualKeyNCategoryRepository<String, String, String> repositoryWithDualKeyNCategory){
        super(repositoryWithDualKeyNCategory);
    }

    ///
    public String addConversion(Unit sourceUnit, Unit targetUnit) {
        String formattedConversion = convertToFormattedConversion(sourceUnit, targetUnit);
        if (sourceUnit.getUnitManagerContext() == targetUnit.getUnitManagerContext()
                && UnitOperators.equalsDimension(sourceUnit,targetUnit)
                && ContentDeterminer.determineGeneralDataModelCategory(sourceUnit) != ContentDeterminer.DATA_MODEL_CATEGORY.UNKNOWN) {
            return repositoryWithDualKeyNCategory.addItem(sourceUnit.getCategory(), sourceUnit.getName()
                    , formattedConversion, targetUnit.getName()) != null ? formattedConversion : "";
        }
        return "";
    }

    public String addConversion(String unitCategory, String sourceUnitName, String targetUnitName) {
        String formattedConversion = convertToFormattedConversion(unitCategory, sourceUnitName, targetUnitName);
        return repositoryWithDualKeyNCategory.addItem(unitCategory, sourceUnitName, formattedConversion, targetUnitName) != null ? formattedConversion : "";
    }

    public String addConversion(Quantity sourceQuantity, Quantity targetQuantity) throws SerializingException {
        if (sourceQuantity.getUnitManagerContext() == targetQuantity.getUnitManagerContext()
                && QuantityOperators.equalsUnitDimensionOf(sourceQuantity, targetQuantity)) {

            //Properly update other conversions based on if they are associated by unit or category
            for (Unit sourceUnit : sourceQuantity.getUnits()) {
                modifySignificanceRankOfMultipleConversions(sourceUnit, true);
            }
            for (Unit targetUnit : targetQuantity.getUnits()) {
                modifySignificanceRankOfMultipleConversions(targetUnit, true);
            }
            modifySignificanceRankOfMultipleConversions(sourceQuantity.getLargestUnit().getCategory(), true);

            return addConversion(sourceQuantity.getLargestUnit().getCategory()
                    , sourceQuantity.getUnitNames(), targetQuantity.getUnitNames());
        }
        return "";
    }

    ///
    public boolean removeConversionByUnitPair(Unit sourceUnit, Unit targetUnit) {
        modifySignificanceRankOfMultipleConversions(sourceUnit, false);
        modifySignificanceRankOfMultipleConversions(targetUnit, false);
        return repositoryWithDualKeyNCategory.removeItemByKey(convertToFormattedConversion(sourceUnit, targetUnit));
    }

    public boolean removeFormattedConversion(String formattedConversion) {
        String sourceUnitName = getSourceUnitNameFromConversion(formattedConversion);
        String targetUnitName = getTargetUnitNameFromConversion(formattedConversion);
        String unitCategory = parseUnitCategoryFromConversion(formattedConversion);

        if (repositoryWithDualKeyNCategory.removeItemByKey(formattedConversion)) {
            modifySignificanceRankOfMultipleConversions(unitCategory, false);
            //Add extra significance decrement to conversion that contains source and target unit.
            for (String favoriteConversion : getFormattedConversionsAssociatedWithCategory(unitCategory)) {
                if (favoriteConversion.contains(sourceUnitName) || favoriteConversion.contains(targetUnitName))
                    significanceRankRepository.modifySignificanceRankOfConversion(favoriteConversion, -1);
            }
            return true;
        }

        return false;
    }

    public boolean removeAllConversionsWithUnit(Unit unit) {
        boolean removed = repositoryWithDualKeyNCategory.removeItemByKey(unit.getName()) //Remove by source unit name
                || repositoryWithDualKeyNCategory.removeItem(unit.getName()); // Remove by target unit name
        if (removed) {
            modifySignificanceRankOfMultipleConversions(unit.getCategory(), false);
            return true;
        }
        return false;
    }

    public boolean removedAllConversionWithCategory(String unitCategory) {
        return repositoryWithDualKeyNCategory.removeCategory(unitCategory);
    }

    ///
    public boolean hasAsASourceUnit(String unitName) {
        return repositoryWithDualKeyNCategory.containsKey(unitName);
    }

    public boolean hasAsATargetUnit(String unitName) {
        return repositoryWithDualKeyNCategory.containsItem(unitName);
    }

    public boolean hasUnit(String unitName) {
        return hasAsASourceUnit(unitName) || hasAsATargetUnit(unitName);
    }

    public boolean hasConversion(String conversion) {
        return repositoryWithDualKeyNCategory.isKey2(conversion);
    }

    public boolean hasUnitCategory(String unitCategory) {
        return repositoryWithDualKeyNCategory.containsCategory(unitCategory);
    }

    ///
    public void modifySignificanceRankOfMultipleConversions(Unit unit, boolean increase) {
        //Increase rank of conversions with related units. Add an extra rank rating to conversion
        //specifically containing the unit.
        modifySignificanceRankOfMultipleConversions(unit.getCategory(), increase);
        significanceRankRepository.modifySignificanceRankOfMultipleConversions(getFormattedConversionsAssociatedWithUnit(unit), increase ? 1 : -1);
    }

    public void modifySignificanceRankOfMultipleConversions(String category, boolean increase) {
        significanceRankRepository.modifySignificanceRankOfMultipleConversions(getFormattedConversionsAssociatedWithCategory(category), increase ? 1 : -1);
    }

    /**
     * Increases a formatted conversion's ranks by a specific delta amount. This should be rarely used.
     */
    public void modifySignificanceRankOfConversion(String formattedConversion, int rankDelta) {
        significanceRankRepository.modifySignificanceRankOfConversion(formattedConversion, rankDelta);
    }

    ///
    public Collection<String> getFormattedConversionsAssociatedWithUnit(Unit unit) {
        ArrayList<String> matchingFormattedConversionFavorites = new ArrayList<>();
        if (repositoryWithDualKeyNCategory.containsKey(unit.getName())) {
            /*Since bijection is not ensured, there is no guarantee that all related conversions would be retrieved simply by constant time mapped key references.
             * Therefore, a non constant time is necessary.*/
            matchingFormattedConversionFavorites.add(repositoryWithDualKeyNCategory.getKey2FromKey1(unit.getName()));

            for (String candidateConversionFavorites : repositoryWithDualKeyNCategory.getKey2sByCategory(repositoryWithDualKeyNCategory.getCategoryOfKey(unit.getName()))) {
                if (candidateConversionFavorites.contains(unit.getName()))
                    matchingFormattedConversionFavorites.add(candidateConversionFavorites);
            }
        } else if (repositoryWithDualKeyNCategory.containsItem(unit.getName())) {
            for (String formattedConversionFavorite : repositoryWithDualKeyNCategory.getAllKey2s()) {
                if (formattedConversionFavorite.contains(unit.getName()))
                    matchingFormattedConversionFavorites.add(formattedConversionFavorite);
            }
        }
        return matchingFormattedConversionFavorites;
    }

    public Collection<String> getFormattedConversionsAssociatedWithCategory(String category) {
        return new ArrayList<>(repositoryWithDualKeyNCategory.getKey2sByCategory(category));
    }

    ///
    public String getSourceUnitNameFromConversion(String formattedConversion) {
        //Use a quick constant time lookup to find corresponding source unit associated with conversion, otherwsie use string transformations
        if (hasConversion(formattedConversion)) {
            return repositoryWithDualKeyNCategory.getKey1FromKey2(formattedConversion);
        }
        return formattedConversion.split(CATEGORY_DELIMITER)[1].split(CONVERSION_DELIMITER)[0];
    }

    public String getTargetUnitNameFromConversion(String formattedConversion) {
        //Use a quick constant time lookup to find corresponding target unit associated with conversion, otherwsie use string transformations
        if (hasConversion(formattedConversion)) {
            return repositoryWithDualKeyNCategory.getFirstItemByAnyKey(formattedConversion);
        }
        return formattedConversion.split(CATEGORY_DELIMITER)[1].split(CONVERSION_DELIMITER)[1];
    }

    ///
    private String convertToFormattedConversion(Unit sourceUnit, Unit targetUnit) {
        return convertToFormattedConversion(sourceUnit.getCategory(), sourceUnit.getName(), targetUnit.getName());
    }

    private String convertToFormattedConversion(String category, String sourceUnitName, String targetUnitName) {
        return category.toUpperCase() + CATEGORY_DELIMITER + sourceUnitName + CONVERSION_DELIMITER + targetUnitName;
    }

    ///
    public static String parseUnitCategoryFromConversion(String formattedConversion) {
        return formattedConversion.split(CATEGORY_DELIMITER)[0];
    }

    ///
    public int getSignificanceRankOfConversion(String formattedConversion){
        return significanceRankRepository.getSignificanceRankOfConversion(formattedConversion);
    }

    ///
    public void setSignificanceRankRepository(SignificanceRankHashedRepository significanceRankRepository){
        this.significanceRankRepository = significanceRankRepository;
    }

    ///
    public Collection<String> getAllFormattedConversions() {
        return repositoryWithDualKeyNCategory.getAllKey2s();
    }

    public Collection<String> getAllSourceUnits() {
        return repositoryWithDualKeyNCategory.getAllKey1s();
    }

    public Collection<String> getAllTargetUnits() {
        return repositoryWithDualKeyNCategory.getAllItems();
    }
}
