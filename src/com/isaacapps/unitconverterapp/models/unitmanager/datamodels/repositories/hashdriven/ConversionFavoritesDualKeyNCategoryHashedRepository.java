package com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.hashdriven;

/**
 * Allowing for a non bijective relations between keys.
 * Key1 will be the source unit name and key2 will be the formatted conversion favorite and the item will be the target unit name.
 * Therefore, according to existing abstract datamodel data structure,  many keys2 can point to the same key1 or item.
 * "UnitName" can refer to a singular unit (eg. 'second', 'meter', etc.) or a unit group (eg. '{hour,minute,second}', '{feet,inch}, etc.')
 */
public class ConversionFavoritesDualKeyNCategoryHashedRepository extends DualKeyNCategoryHashedRepository<String, String, String> {
    public ConversionFavoritesDualKeyNCategoryHashedRepository(){
        /* */
        super(false, false, true);
    }
}
