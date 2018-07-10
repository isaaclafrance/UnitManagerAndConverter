package com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.componentunits.ComponentUnitsDimensionParser;
import com.isaacapps.unitconverterapp.processors.parsers.generaltext.PluralTextParser;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.componentnunit.ComponentUnitsDimensionSerializer;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ContentDeterminer {
    public enum DATA_MODEL_CATEGORY {CORE, DYNAMIC, BASE, NON_BASE, UNKNOWN}

    private UnitsDataModel unitsDataModel;
    private PluralTextParser pluralTextParser;
    private ComponentUnitsDimensionParser componentUnitsDimensionParser;
    private ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer;

    public ContentDeterminer() {
    }

    /**
     * Simplifies a group into one category based on based on order of precedence. If a unit is defined unknown, no other categories matter.
     * Whether or not a unit is core or dynamic has greater functional implications than where or not it is a base unit.
     */
    public static DATA_MODEL_CATEGORY determineGeneralDataModelCategory(Unit unit) {
        Collection<DATA_MODEL_CATEGORY> dataModelCategoryGroup = determineDataModelCategoryGroup(unit);

        if (dataModelCategoryGroup.contains(DATA_MODEL_CATEGORY.UNKNOWN)) {
            return DATA_MODEL_CATEGORY.UNKNOWN;
        } else if (dataModelCategoryGroup.contains(DATA_MODEL_CATEGORY.CORE)) {
            return DATA_MODEL_CATEGORY.CORE;
        } else {
            return DATA_MODEL_CATEGORY.DYNAMIC;
        }
    }

    public static Collection<DATA_MODEL_CATEGORY> determineDataModelCategoryGroup(Unit unit) {
        Collection<DATA_MODEL_CATEGORY> dataModelCategoryGroup = new HashSet<>();

        //
        if (unit.getBaseUnit() == null || unit.getBaseUnit().getType() == FundamentalUnitsDataModel.UNIT_TYPE.UNKNOWN
                || unit.getFundamentalUnitTypesDimension().containsKey(FundamentalUnitsDataModel.UNIT_TYPE.UNKNOWN)
                || unit.getType() == FundamentalUnitsDataModel.UNIT_TYPE.UNKNOWN) {
            dataModelCategoryGroup.add(DATA_MODEL_CATEGORY.UNKNOWN);
        }

        //
        if (unit.isCoreUnit()) {
            dataModelCategoryGroup.add(DATA_MODEL_CATEGORY.CORE);
        } else {
            dataModelCategoryGroup.add(DATA_MODEL_CATEGORY.DYNAMIC);
        }

        //
        if (unit.isBaseUnit()) {
            dataModelCategoryGroup.add(DATA_MODEL_CATEGORY.BASE);
        } else {
            dataModelCategoryGroup.add(DATA_MODEL_CATEGORY.NON_BASE);
        }

        return dataModelCategoryGroup;
    }

    ///
    /**
     * Finds the corresponding singularized unit definition for a provided definition containing
     * plural component units.
     *
     * @param pluralUnitDefinition Complex (ie. a^2*b^2) or atomic unit definition with a plural component.
     * @return New formatted unit definition with all potential plural components singualrized to an existing unit name.
     * if nothing can be singualized then it is returned as is.
     */
    public String determineSingularOfUnitName(String pluralUnitDefinition) {
        try {
            if (!componentUnitsDimensionParser.getDimensionParserBuilder().getDimensionComponentDefinerBuilder().hasComplexDimensions(pluralUnitDefinition)) {
                Collection<String> unitNameCandidates = pluralTextParser
                        .getPossibleSingularCombinations(pluralUnitDefinition);

                for (String unitNameCandidate : unitNameCandidates) {
                    if (unitsDataModel.getContentQuerier().containsUnit(unitNameCandidate))
                        return unitNameCandidate;
                }

                return pluralUnitDefinition;
            } else {
                Map<String, Double> oldComplexComponentUnitDimension = componentUnitsDimensionParser.parse(pluralUnitDefinition);

                Map<String, Double> singularizedComplexComponentUnitDimension = new HashMap<>();

                for (Map.Entry<String, Double> componentUnitEntry : oldComplexComponentUnitDimension.entrySet()) {
                    singularizedComplexComponentUnitDimension.put(determineSingularOfUnitName(componentUnitEntry.getKey())
                            , componentUnitEntry.getValue());
                }

                return componentUnitsDimensionSerializer.serialize(singularizedComplexComponentUnitDimension);
            }
        }
        catch(Exception e){
            return pluralUnitDefinition;
        }
    }

    /**
     * Returns the appropriate derived type when there are multiple component units or the fundamental unit type when
     * there is only one component unit. Even if the unit is determined to be derived that does not necessarily mean that it is entirely 'known'
     * within the context of the units data model since its base unit may be unknown or any one of its component units may also be unknown.
     */
    public FundamentalUnitsDataModel.UNIT_TYPE determineUnitType(Unit unit) {
        if (unit.getComponentUnitsDimension().size() > 1) {

            return FundamentalUnitsDataModel.UNIT_TYPE.DERIVED_MULTI_UNIT;
        }
        else if (unit.getComponentUnitsDimension().size() == 1
                && (Math.abs(unit.getComponentUnitsDimension().entrySet().iterator().next().getValue()) > 1
                || unit.getComponentUnitsDimension().containsValue(-1.0)
                || unit.getComponentUnitsDimension().containsValue(0.0)))
        {
            return FundamentalUnitsDataModel.UNIT_TYPE.DERIVED_SINGLE_UNIT;
        }
        else if (unit.getBaseUnit() != null)
        {
            FundamentalUnitsDataModel.UNIT_TYPE type = unitsDataModel.getUnitManagerContext().getFundamentalUnitsDataModel().getUnitTypeByUnitSystemNUnitName(unit.getBaseUnit().getUnitSystem()
                            , unit.getBaseUnit().getName());

            if (type == null)
            {
                type = unit.getBaseUnit().getType();
                if (type != FundamentalUnitsDataModel.UNIT_TYPE.UNKNOWN) {
                    return type;
                } else { //If still unknown, then try using the singular component unit.
                    return unitsDataModel.getContentMainRetriever().getUnit(unit.getComponentUnitsDimension().keySet().iterator().next()).getType();
                }
            }
            return type;
        }
        return FundamentalUnitsDataModel.UNIT_TYPE.UNKNOWN;
    }

    ///
    public void setUnitsDataModel(UnitsDataModel unitsDataModel) {
        this.unitsDataModel = unitsDataModel;
    }

    public void setPluralTextParser(PluralTextParser pluralTextParser) {
        this.pluralTextParser = pluralTextParser;
    }

    public void setComponentUnitsDimensionParser(ComponentUnitsDimensionParser componentUnitsDimensionPatser) {
        this.componentUnitsDimensionParser = componentUnitsDimensionPatser;
    }

    public void setComponentUnitsDimensionSerializer(ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer) {
        this.componentUnitsDimensionSerializer = componentUnitsDimensionSerializer;
    }
}
