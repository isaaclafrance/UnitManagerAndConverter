package com.isaacapps.unitconverterapp.dao.xml.readers.local.units;

import android.content.Context;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManagerBuilder;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.DimensionComponentDefiner;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.componentnunit.ComponentUnitsDimensionSerializer;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.fundamentalunit.FundamentalUnitTypesDimensionSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class StandardCoreUnitsMapXmlLocalReader extends UnitsMapXmlLocalReader {
    public static String STANDARD_CORE_UNITS_FILE = "StandardCoreUnits.xml";

    public StandardCoreUnitsMapXmlLocalReader(Context context, Locale locale, ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer, FundamentalUnitTypesDimensionSerializer fundamentalUnitTypesDimensionSerializer, DimensionComponentDefiner dimensionComponentDefiner) {
        super(context, locale, componentUnitsDimensionSerializer, fundamentalUnitTypesDimensionSerializer, dimensionComponentDefiner);
    }

    ///
    @Override
    public UnitManagerBuilder loadInBackground() {
        ArrayList<ArrayList<Unit>> coreUnitsGroup = new ArrayList<>();

        try {
            coreUnitsGroup = parseXML(openAssetFile(STANDARD_CORE_UNITS_FILE));
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 2; i++) {
            for (Unit unit : coreUnitsGroup.get(i)) {
                unit.setCoreUnitState(true);
            }
        }

        ArrayList<Unit> combinedUnits = coreUnitsGroup.get(0);
        combinedUnits.addAll(coreUnitsGroup.get(1));

        return new UnitManagerBuilder().addBaseUnits(combinedUnits)
                .addNonBaseUnits(combinedUnits);
    }
}
