package com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.hashdriven;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.ContentDeterminer;

public class UnitsDualKeyNCategoryHashedRepository extends DualKeyNCategoryHashedRepository<String, Unit, ContentDeterminer.DATA_MODEL_CATEGORY> {
    public UnitsDualKeyNCategoryHashedRepository() {
        super(true, true, true);
    }
}
