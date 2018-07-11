package com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.hashdriven;

import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel;

public class FundamentalUnitsHashDualKeyNCategoryHashedRepository extends DualKeyNCategoryHashedRepository<String, FundamentalUnitsDataModel.UNIT_TYPE, String> {
    public FundamentalUnitsHashDualKeyNCategoryHashedRepository(){
        super(true, false, true);
    }
}
