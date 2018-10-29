package com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.hashdriven;

import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsContentDeterminer;

/**
 * Data structure that bidirectionally and invertibly associates prefix full name and abbreviation strings with each other.
 *, unidirectionally associates abbreviation to a prefix numerical value, and optionally unidirectionally associates alias name string to the prefix full name.
 *The result is the following Category Theory Diagram of objects and morphism: (AliasName) ----> (FullName) <---- ----> (Abbreviation) ----> (PrefixNumericalValue)
 */
public class PrefixesDualKeyNCategoryHashedRepository extends DualKeyNCategoryHashedRepository<String, Double, UnitsContentDeterminer.DATA_MODEL_CATEGORY> {
    public PrefixesDualKeyNCategoryHashedRepository(){
        super(true, false, true);
    }
}
