package com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.hashdriven;

import java.util.Collection;

/**
 *Does not attempt to hard set removedDuplicateItems parameter to true since in principle two different units systems
 *can have the same units names group for the same unit category. However, the add to hierarchy method does have the
 *option to selectively make sure that a certain unit name does not exist in other units groups.
 */
public class UnitsClassifierDualKeyNCategoryHashedRepository extends DualKeyNCategoryHashedRepository<String, Collection<String>, String> {
    public UnitsClassifierDualKeyNCategoryHashedRepository(){
        super(false, false, true);
    }
}
