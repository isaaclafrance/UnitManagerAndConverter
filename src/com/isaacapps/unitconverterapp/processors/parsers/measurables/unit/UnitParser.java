package com.isaacapps.unitconverterapp.processors.parsers.measurables.unit;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager;
import com.isaacapps.unitconverterapp.processors.parsers.IParser;
import com.isaacapps.unitconverterapp.processors.parsers.ParsingException;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.componentunits.ComponentUnitsDimensionParser;
import com.isaacapps.unitconverterapp.processors.serializers.ISerializer;
import com.isaacapps.unitconverterapp.processors.serializers.SerializingException;

import java.util.Map;

public class UnitParser implements IParser<Unit> {
    public static final String UNIT_NAME_REGEX = ComponentUnitsDimensionParser.COMPONENT_NAME_REGEX;

    private UnitManager unitManager;
    private IParser<Map<String, Double>> componentUnitDimensionParser;
    private ISerializer componentUnitDimensionSerializer;
    private boolean createMissingUnits;

    public UnitParser(IParser<Map<String, Double>> componentUnitDimensionParser, ISerializer<Map<String, Double>> componentUnitDimensionSerializer) {
        this.componentUnitDimensionParser = componentUnitDimensionParser;
        this.componentUnitDimensionSerializer = componentUnitDimensionSerializer;
        this.createMissingUnits = true;
    }
    public UnitParser(IParser<Map<String, Double>> componentUnitDimensionParser, ISerializer<Map<String, Double>> componentUnitDimensionSerializer, boolean createMissingUnits) {
        this(componentUnitDimensionParser, componentUnitDimensionSerializer);
        this.createMissingUnits = createMissingUnits;
    }

    public Unit parse(String unitDefinition) throws ParsingException {
        //Cleans up the unit definition into a standardized dimension format that include parentheses where appropriate.
        String wellFormattedUnitDefinition;
        try {
            wellFormattedUnitDefinition = componentUnitDimensionSerializer.serialize(
                    componentUnitDimensionParser.parse(unitDefinition));
        } catch (SerializingException e) {
            wellFormattedUnitDefinition = unitDefinition;
        }

        if(unitManager != null){
            return unitManager.getUnitsDataModel().getContentMainRetriever().getUnit(wellFormattedUnitDefinition, createMissingUnits);
        }
        else {
            if(createMissingUnits) {
                return new Unit(componentUnitDimensionParser.parse(wellFormattedUnitDefinition), false);
            }
            else{
                throw new ParsingException("No existing unit could be found for parsed unit definition. No new unit could be created due to unit parser setting"
                        ,"Change the parser setting to allow creation of new unit from parsed unit definition.");
            }
        }
    }

    public UnitManager getUnitManager() {
        return unitManager;
    }

    public void setUnitManager(UnitManager unitManager) {
        this.unitManager = unitManager;
    }

    public void setCreateMissingUnits(boolean createMissingUnits){
        this.createMissingUnits = createMissingUnits;
    }
}
