package com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.databasedriven;

import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.IDualKeyNCategoryRepository;

import java.util.Collection;

public class UnitsDualKeyNCategoryDatabaseRepository<T, U, V> implements IDualKeyNCategoryRepository<T, U, V> {
    private UnitsRepository unitsRepository;

    public UnitsDualKeyNCategoryDatabaseRepository(UnitsRepository unitsRepository){
        this.unitsRepository = unitsRepository;
    }

    /**
     * Makes item searchable under provided category and keys only if the the conditions (bijectivity, etc.)
     * of the data structure are not violated. Create a invertible bidriection relation between the two keys.
     *
     * @param category
     * @param key1
     * @param key2
     * @param item
     * @return The provided item if it was successfully added or null otherwise.
     */
    @Override
    public U addItem(V category, T key1, T key2, U item) {
        return null;
    }

    /**
     * Updates existing key1 or key2 with a new key so long as existing provided key was not a unidirectional alias
     *
     * @param existingProvidedKey
     * @param replacementKey
     */
    @Override
    public boolean updateBidirectionalKeyRelations(T existingProvidedKey, T replacementKey) {
        return false;
    }

    @Override
    public boolean addUniDirectionalKeyRelation(T aliasKey, T key1) {
        return false;
    }

    @Override
    public boolean removeItemFromCategoryByKey(V category, T anyKey) {
        return false;
    }

    @Override
    public boolean removeItemByKey(T anyKey) {
        return false;
    }

    @Override
    public boolean removeItem(U item) {
        return false;
    }

    @Override
    public boolean removeCategory(V category) {
        return false;
    }

    @Override
    public boolean removeAllItems() {
        return false;
    }

    /**
     * Removes a unidirectional alias relations if any
     *
     * @param aliasKey
     * @return True only if the the key was an alias and removed.
     */
    @Override
    public boolean removeUnitDirectionalRelation(T aliasKey) {
        return false;
    }

    /**
     * Removes key1 to key2 relations as well as relevant aliases. If an alias is te provided key
     * , then the alias relation to key 1 is removed as well as the bidirectional relation of key1 and key 2.
     * Should only be sparingly used by descendent classes to prevent data integrity inconsistencies.
     *
     * @param providedKey
     */
    @Override
    public boolean removeAllKeyRelationsForKey(T providedKey) {
        return false;
    }

    @Override
    public boolean removeAllKeyRelationsForKeys(Iterable<T> keys) {
        return false;
    }

    @Override
    public U getFirstItemByAnyKey(T key) {
        return null;
    }

    @Override
    public U getItem(V category, T key) {
        return null;
    }

    @Override
    public T getKey2(V category, U item) {
        return null;
    }

    @Override
    public T getKey1(V category, U item) {
        return null;
    }

    @Override
    public V getCategoryOfKey(T key) {
        return null;
    }

    @Override
    public Collection<U> getItemsByCategory(V category) {
        return null;
    }

    @Override
    public Collection<U> getItemsByAnyKey(T anyKey) {
        return null;
    }

    @Override
    public Collection<U> getAllItems() {
        return null;
    }

    @Override
    public Collection<T> getKey2sByCategory(V category) {
        return null;
    }

    @Override
    public Collection<T> getKey1sByCategory(V category) {
        return null;
    }

    @Override
    public Collection<T> getAllKey1s() {
        return null;
    }

    @Override
    public Collection<T> getAllKey2s() {
        return null;
    }

    @Override
    public Collection<T> getAllKeys() {
        return null;
    }

    @Override
    public Collection<T> getAllAliasesForKey(T key) {
        return null;
    }

    @Override
    public Collection<V> getAllAssignedCategories() {
        return null;
    }

    @Override
    public T getKey2FromKey1(T key1) {
        return null;
    }

    @Override
    public T getKey1FromKey2(T key2) {
        return null;
    }

    @Override
    public boolean containsItem(U item) {
        return false;
    }

    @Override
    public boolean containsKey(T key) {
        return false;
    }

    @Override
    public boolean containsCategory(V category) {
        return false;
    }

    @Override
    public boolean containsKeyInCategory(V category, T key) {
        return false;
    }

    @Override
    public boolean containsItemInCategory(V category, U item) {
        return false;
    }

    @Override
    public boolean isKey2(T key) {
        return false;
    }

    @Override
    public boolean isKey1(T key) {
        return false;
    }

    @Override
    public boolean isAliasKey(T key) {
        return false;
    }

    @Override
    public boolean areKeysValid(T key1, T key2) {
        return false;
    }

    @Override
    public boolean combineWith(IDualKeyNCategoryRepository<T, U, V> otherDataModel) {
        return false;
    }

    @Override
    public boolean keysMustHaveBijectiveRelation() {
        return false;
    }

    @Override
    public boolean duplicateItemsAreRemoved() {
        return false;
    }

    @Override
    public boolean emptyCategoriesAreRemoved() {
        return false;
    }

    @Override
    public boolean hasAliases() {
        return false;
    }
}
