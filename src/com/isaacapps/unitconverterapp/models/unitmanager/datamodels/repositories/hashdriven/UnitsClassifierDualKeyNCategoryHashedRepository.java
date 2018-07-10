package com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.hashdriven;

import java.util.Collection;

/**
 *Does not attempt to hard set removedDuplicateItems parameter to true since in principle two different units systems
 *can have the same units names group for the same unit category. However, the add to hierarchy method does have the
 *option to selectively make sure that a certain unit name does not exist in other units groups.
 *Within one unit system, a unit name can not be associated with more than one unit category, which is ensured by setting the
 *keysMustHaveBijectiveRelation to true.
 */
public class UnitsClassifierDualKeyNCategoryHashedRepository extends DualKeyNCategoryHashedRepository<String, Collection<String>, String> {
    public UnitsClassifierDualKeyNCategoryHashedRepository(){
        super(true, false, true);
    }
}
