package com.isaacapps.unitconverterapp.processors.parsers.measurables.unit;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.measurables.unit.UnitException;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager;
import com.isaacapps.unitconverterapp.processors.parsers.IParser;
import com.isaacapps.unitconverterapp.processors.parsers.ParsingException;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.componentunits.ComponentUnitsDimensionParser;

import java.util.Map;

public class UnitParser implements IParser<Unit> {
    public static final String UNIT_NAME_REGEX = ComponentUnitsDimensionParser.COMPONENT_NAME_REGEX;

    private UnitManager unitManager;
    private IParser<Map<String, Double>> componentUnitDimensionParser;
    private boolean newUnitsAreSaved;

    ///
    public UnitParser(IParser<Map<String, Double>> componentUnitDimensionParser) {
        this.componentUnitDimensionParser = componentUnitDimensionParser;
    }

    ///
    @Override
    public Unit parse(String unitDefinition) throws ParsingException {

        Unit unit = null;

        if(unitManager != null)
            unit = unitManager.getUnitsDataModel().getUnitsContentMainRetriever().getUnit(unitDefinition, true);

        if(unit != null && (unitManager != null && unitManager.getUnitsDataModel().getUnitsContentMainRetriever().getUnknownUnit() != unit))
            return unit;

        unit = new Unit(componentUnitDimensionParser.parse(unitDefinition));

        if(unitManager != null && newUnitsAreSaved) {
            try {
                return unitManager.getUnitsDataModel().getUnitsContentModifier().addUnit(unit);
            } catch (UnitException e) {
                e.printStackTrace();
            }
        }

        return unit;
    }

    ///
    public UnitManager getUnitManager() {
        return unitManager;
    }
    public void setUnitManager(UnitManager unitManager) {
        this.unitManager = unitManager;
    }

    public boolean areUnknownUnitsSaved(){
        return newUnitsAreSaved;
    }
    public void setNewUnitsAreSaved(boolean newUnitsAreSaved){
        this.newUnitsAreSaved = newUnitsAreSaved;
    }
}
