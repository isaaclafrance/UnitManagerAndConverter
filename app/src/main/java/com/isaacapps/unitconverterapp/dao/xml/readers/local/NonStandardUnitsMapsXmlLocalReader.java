package com.isaacapps.unitconverterapp.dao.xml.readers.local;

import android.content.Context;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManagerBuilder;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.DimensionComponentDefiner;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.componentnunit.ComponentUnitsDimensionSerializer;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.fundamentalunit.FundamentalUnitTypesDimensionSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class NonStandardUnitsMapsXmlLocalReader extends UnitsMapXmlLocalReader {
    public static String NONSTANDARD_CORE_UNITS_FILE = "CoreUnits.xml";
    public static String NONSTANDARD_DYNAMIC_UNITS_FILE = "DynamicUnits.xml";

    public NonStandardUnitsMapsXmlLocalReader(Context context, Locale locale, ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer, FundamentalUnitTypesDimensionSerializer fundamentalUnitTypesDimensionSerializer, DimensionComponentDefiner dimensionComponentDefiner) {
        super(context, locale, componentUnitsDimensionSerializer, fundamentalUnitTypesDimensionSerializer, dimensionComponentDefiner);
    }

    ///
    @Override
    public UnitManagerBuilder loadInBackground() {
        UnitManagerBuilder unitManagerBuilder = new UnitManagerBuilder();

        try {
            ArrayList<ArrayList<Unit>> coreUnitsGroup  = parseXML(openXmlFile(getContext().getFilesDir().getPath() + NONSTANDARD_CORE_UNITS_FILE, false));
            ArrayList<ArrayList<Unit>> dynamicUnitsGroup = parseXML(openXmlFile(getContext().getFilesDir().getPath() + NONSTANDARD_DYNAMIC_UNITS_FILE, false));

            for (int i = 0; i < 2; i++) {
                for (Unit unit : coreUnitsGroup.get(i)) {
                    unit.setCoreUnitState(true);
                }
            }

            ArrayList<Unit> combinedBaseUnits = coreUnitsGroup.get(0);
            combinedBaseUnits.addAll(dynamicUnitsGroup.get(0));

            ArrayList<Unit> combinedNonBaseUnits = coreUnitsGroup.get(1);
            combinedBaseUnits.addAll(dynamicUnitsGroup.get(1));

            unitManagerBuilder.addBaseUnits(combinedBaseUnits).addNonBaseUnits(combinedNonBaseUnits);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return unitManagerBuilder;

    }
}
