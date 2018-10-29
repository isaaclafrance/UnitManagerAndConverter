package com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories;

import java.util.Collection;

/**Generic data structure that bidirectionally and/or invertibly associates two keys (key1 and key2) of identical types with each other
 *, unidirectionally associates key2 to an item of another type, and optionally unidirectionally associates alias key to key1 of same type.
 *The result is the following Category Theory Diagram of objects and morphism: (AliasKey) ----> (Key1) <---- ----> (Key2) ----> (Item) */
public interface IDualKeyNCategoryRepository<T, U, V> {

    /**
     * Makes item searchable under provided category and keys only if the the conditions (bijectivity, etc.)
     * of the data structure are not violated. Create a invertible bidriection relation between the two keys.
     *
     * @return The provided item if it was successfully added or null otherwise.
     */
    U addItem(V category, T key1, T key2, U item);


    /**
     * Updates existing key1 or key2 with a new key so long as existing provided key was not a unidirectional alias
     */
    boolean updateBidirectionalKeyRelations(T existingProvidedKey, T replacementKey);

    boolean addUniDirectionalKeyRelation(T aliasKey, T key1);

    ///
    boolean removeItemFromCategoryByKey(V category, T anyKey);

    boolean removeItemByKey(T anyKey);

    boolean removeItem(U item);

    boolean removeCategory(V category);

    boolean removeAllItems();


    /**
     * Removes a unidirectional alias relations if any
     *
     * @return True only if the the key was an alias and removed.
     */
    boolean removeUnitDirectionalRelation(T aliasKey);

    /**
     * Removes key1 to key2 relations as well as relevant aliases. If an alias is te provided key
     * , then the alias relation to key 1 is removed as well as the bidirectional relation of key1 and key 2.
     * Should only be sparingly used by descendent classes to prevent data integrity inconsistencies.
     */
    boolean removeAllKeyRelationsForKey(T providedKey);

    boolean removeAllKeyRelationsForKeys(Iterable<T> keys);

    ///
    U getFirstItemByAnyKey(T key);

    U getItem(V category, T key);

    T getKey2(V category, U item);

    T getKey1(V category, U item);

    V getCategoryOfKey(T key);

    Collection<U> getItemsByCategory(V category);

    Collection<U> getItemsByAnyKey(T anyKey);

    Collection<U> getAllItems();

    Collection<T> getKey2sByCategory(V category);

    Collection<T> getKey1sByCategory(V category);

    Collection<T> getAllKey1s();

    Collection<T> getAllKey2s();

    Collection<T> getAllKeys();

    Collection<T> getAllAliasesForKey(T key);

    Collection<V> getAllAssignedCategories();

    ///
    T getKey2FromKey1(T key1);

    T getKey1FromKey2(T key2);

    ///
    boolean containsItem(U item);

    boolean containsKey(T key);

    boolean containsCategory(V category);

    boolean containsKeyInCategory(V category, T key);

    boolean containsItemInCategory(V category, U item);

    boolean isKey2(T key);

    boolean isKey1(T key);

    boolean isAliasKey(T key);

    ///
    boolean combineWith(IDualKeyNCategoryRepository<T, U, V> otherDataModel);

    ///
    boolean keysMustHaveBijectiveRelation();

    boolean duplicateItemsAreRemoved();

    boolean emptyCategoriesAreRemoved();

    boolean hasAliases();
}
