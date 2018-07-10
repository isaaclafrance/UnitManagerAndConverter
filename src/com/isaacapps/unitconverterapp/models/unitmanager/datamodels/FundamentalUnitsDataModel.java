package com.isaacapps.unitconverterapp.models.unitmanager.datamodels;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.IDualKeyNCategoryRepository;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsDataModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Associates a unit full name with a particular unit type within a unit system
 *
 * @author Isaac Lafrance
 */
public class FundamentalUnitsDataModel extends BaseDataModel<String, FundamentalUnitsDataModel.UNIT_TYPE, String> {
    public enum UNIT_TYPE {
        MASS, LENGTH, TIME, AMT_OF_SUBSTANCE, ANGLE, TEMPERATURE, CHARGE, LUMINOUS_INTENSITY, DERIVED_SINGLE_UNIT, DERIVED_MULTI_UNIT, UNKNOWN, CURRENCY
    }

    public static final String DEFAULT_UNIT_SYSTEM = "si";

    private UnitsDataModel unitsDataModel;

    ///
    public FundamentalUnitsDataModel() {
    }
    public FundamentalUnitsDataModel(IDualKeyNCategoryRepository<String, UNIT_TYPE, String> repositoryWithDualKeyNCategory){
        super(repositoryWithDualKeyNCategory);
    }

    ///
    public void addFundamentalUnit(String unitSystem, String unitName, UNIT_TYPE unitType) {
        repositoryWithDualKeyNCategory.addItem(unitSystem.toLowerCase(), unitName.toLowerCase(), unitName.toLowerCase(), unitType);
        //Ideally, since the fundamental units data model is so integral as basis to the other data models, everything should reinitalize event for a minor change.
        unitsDataModel.getContentModifier().updateFundamentalUnitsDimensionOfKnownUnits();
    }

    public void removeAllFundamentalUnits() {
        repositoryWithDualKeyNCategory.removeAllItems();
    }

    ///
    public UNIT_TYPE getUnitTypeByUnitSystemNUnitName(String unitSystem, String unitName) {
        return repositoryWithDualKeyNCategory.getItem(unitSystem.toLowerCase(), unitName.toLowerCase());
    }

    public String getUnitNameByUnitType(String unitSystem, UNIT_TYPE unitType) {
        return repositoryWithDualKeyNCategory.getKey1(unitSystem, unitType);
    }

    ///
    public boolean containsUnitSystem(String unitSystem) {
        return repositoryWithDualKeyNCategory.containsCategory(unitSystem.toLowerCase());
    }

    public boolean containsUnitNameInUnitSystem(String unitSystem, String unitName) {
        return repositoryWithDualKeyNCategory.containsKeyInCategory(unitSystem.toLowerCase(), unitName.toLowerCase());
    }

    public boolean containsUnitName(String unitName) {
        return repositoryWithDualKeyNCategory.containsKey(unitName.toLowerCase());
    }

    /**
     * Analyzes each component unit whether derived or not with reference to the unitManager context
     * and sums up the recursively obtained total occurrences of the fundamental units.
     * Makes sure to multiply those totals by the exponent of the component unit.
     */
    public Map<UNIT_TYPE, Double> transformComponentUnitsDimensionToFundamentalUnitsDimension(Map<String, Double> componentUnitsDimension) {
        Map<UNIT_TYPE, Double> fundamentalUnitMap = new HashMap<>();

        Unit componentUnit;
        for (String componentUnitName : componentUnitsDimension.keySet()) {
            componentUnit = unitsDataModel.getContentMainRetriever().getUnit(componentUnitName, true);

            if (componentUnit.getType() == UNIT_TYPE.DERIVED_MULTI_UNIT) {
                Map<UNIT_TYPE, Double> recursedMap = transformComponentUnitsDimensionToFundamentalUnitsDimension(((componentUnit.getComponentUnitsDimension().size() == 1) ? componentUnit.getBaseUnit() : componentUnit)
                        .getComponentUnitsDimension());
                for (UNIT_TYPE unitType : UNIT_TYPE.values()) {
                    if (recursedMap.containsKey(unitType)) {
                        if (!fundamentalUnitMap.containsKey(unitType)) {
                            fundamentalUnitMap.put(unitType, 0.0);
                        }

                        fundamentalUnitMap.put(unitType,
                                fundamentalUnitMap.get(unitType) + componentUnitsDimension.get(componentUnitName) * recursedMap.get(unitType));
                    }
                }
            } else {
                if (componentUnit.getType() != UNIT_TYPE.DERIVED_SINGLE_UNIT) {
                    if (!fundamentalUnitMap.containsKey(componentUnit.getBaseUnit().getType())) {
                        fundamentalUnitMap.put(componentUnit.getBaseUnit().getType(), 0.0);
                    }
                    fundamentalUnitMap.put(componentUnit.getBaseUnit().getType(), fundamentalUnitMap.get(componentUnit.getBaseUnit().getType()) + componentUnitsDimension.get(componentUnitName));
                } else {
                    for (Entry<UNIT_TYPE, Double> fundEntry : componentUnit.getBaseUnit().getFundamentalUnitTypesDimension().entrySet()) {
                        if (!fundamentalUnitMap.containsKey(fundEntry.getKey())) {
                            fundamentalUnitMap.put(fundEntry.getKey(), 0.0);
                        }

                        fundamentalUnitMap.put(fundEntry.getKey(), fundamentalUnitMap.get(fundEntry.getKey()) + fundEntry.getValue() * componentUnitsDimension.get(componentUnitName));
                    }
                }
            }
        }

        return fundamentalUnitMap;
    }

    ///
    public Map<String, Double> transformFundamentalUnitsDimensionToComponentUnitDimension(Map<UNIT_TYPE, Double> fundamentalUnitTypesDimension
            , String unitSystem) {
        Map<String, Double> componentUnitsDimension = new HashMap<>();

        for (Entry<UNIT_TYPE, Double> entry : fundamentalUnitTypesDimension.entrySet())
            componentUnitsDimension.put(getUnitNameByUnitType(unitSystem, entry.getKey()), entry.getValue());

        return componentUnitsDimension;
    }

    ///
    public void setUnitsDataModel(UnitsDataModel unitsDataModel) {
        this.unitsDataModel = unitsDataModel;
    }

}
