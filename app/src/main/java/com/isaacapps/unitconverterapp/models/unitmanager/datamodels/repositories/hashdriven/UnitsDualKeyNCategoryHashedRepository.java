package com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.hashdriven;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsContentDeterminer;

/**
 * Data structure that bidirectionally associates unit full name and abbreviation strings with each other.
 *, unidirectionally associates abbreviation to a Unit object, and optionally unidirectionally associates alias name string to the unit full name.
 *The result is the following Category Theory Diagram of objects and morphism: (AliasName) ----> (FullName) <---- ----> (Abbreviation) ----> (UnitObject)
 * The same unit can be part of multiple categories, ie core, base_unit, etc
 */
public class UnitsDualKeyNCategoryHashedRepository extends DualKeyNCategoryHashedRepository<String, Unit, UnitsContentDeterminer.DATA_MODEL_CATEGORY> {
    public UnitsDualKeyNCategoryHashedRepository() {
        super(true, false, true);
    }
}
