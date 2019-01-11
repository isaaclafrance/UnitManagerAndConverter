package com.isaacapps.unitconverterapp.dao.xml.writers.local;

import android.content.Context;

import static com.isaacapps.unitconverterapp.dao.xml.readers.local.NonStandardUnitsMapsXmlLocalReader.NONSTANDARD_DYNAMIC_UNITS_FILE;

public class NonStandardDynamicUnitsMapXmlLocalWriter extends UnitsMapXmlLocalWriter {

    public NonStandardDynamicUnitsMapXmlLocalWriter(Context context) {
        super(context, NONSTANDARD_DYNAMIC_UNITS_FILE);
    }

}
