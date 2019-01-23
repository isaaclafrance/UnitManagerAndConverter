package com.isaacapps.unitconverterapp.dao.xml.writers.local.units;

import android.content.Context;

import static com.isaacapps.unitconverterapp.dao.xml.readers.local.units.NonStandardUnitsMapsXmlLocalReader.NONSTANDARD_CORE_UNITS_FILE;

public class NonStandardCoreUnitsMapsXmlLocalWriter extends UnitsMapXmlLocalWriter {

    public NonStandardCoreUnitsMapsXmlLocalWriter(Context context) {
        super(context, NONSTANDARD_CORE_UNITS_FILE);
    }

}
