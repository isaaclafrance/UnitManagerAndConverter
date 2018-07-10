package com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.hashdriven;

import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.ContentDeterminer;

public class PrefixesDualKeyNCategoryHashedRepository extends DualKeyNCategoryHashedRepository<String, Double, ContentDeterminer.DATA_MODEL_CATEGORY> {
    public PrefixesDualKeyNCategoryHashedRepository(){
        super(true, false, true);
    }
}
