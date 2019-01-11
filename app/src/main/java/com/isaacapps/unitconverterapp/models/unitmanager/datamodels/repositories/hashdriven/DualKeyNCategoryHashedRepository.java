package com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.hashdriven;

import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.IDualKeyNCategoryRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 *Generic data structure that bidirectionally and/or invertibly associates two keys (key1 and key2) of identical types with each other
 *, unidirectionally associates key2 to an item of another type, and optionally unidirectionally associates alias key to key1 of same type.
 *The result is the following Category Theory Diagram of objects and morphism: (AliasKey) ----> (Key1) <---- ----> (Key2) ----> (Item)
 * @param <T> Type of the keys
 * @param <U> Type of the object
 * @param <V> Type of the category that stores a set of key-object pairings
 */
public class DualKeyNCategoryHashedRepository<T, U, V> implements IDualKeyNCategoryRepository<T, U, V> {
    private final Map<V, Map<T, U>> key2ToItemMapsByCategory;
    private final Map<T, T> key1ToKey2Map;
    private final Map<T, T> key2ToKey1Map;
    private final boolean keysMustHaveBijectiveRelation;
    private final boolean duplicateItemsAreRemoved;
    private final boolean emptyCategoriesAreRemoved;
    private boolean hasAliases;

    ///
    public DualKeyNCategoryHashedRepository(boolean keysMustHaveBijectiveRelation, boolean duplicateItemsAreRemoved, boolean removeEmptyCategories) {
        key2ToItemMapsByCategory = new HashMap<>();
        key1ToKey2Map = new HashMap<>();
        key2ToKey1Map = new HashMap<>();
        this.keysMustHaveBijectiveRelation = keysMustHaveBijectiveRelation;
        this.duplicateItemsAreRemoved = duplicateItemsAreRemoved;
        this.emptyCategoriesAreRemoved = removeEmptyCategories;
        hasAliases = false;
    }

    ///

    /**
     * Makes item searchable under provided category and keys only if the the conditions (bijectivity, etc.)
     * of the data structure are not violated. Create a invertible bidriection relation between the two keys.
     *
     * @return The provided item if it was successfully added or null otherwise.
     */
    public U addItem(V category, T key1, T key2, U item) {
        if (areKeysValid(key1, key2))
        {
            if (duplicateItemsAreRemoved && containsItem(item)) // Make item not associated any other keys or categories.
                removeItem(item);

            addBidirectionalKeyRelations(key1, key2);
            return addItemToCategory(category, key2, item);
        }

        return null;
    }

    /**
     * Determine specified keys can maintain bijective relationship with regard to other existing keys in data structure.
     * Keys can not already be invertibly associated with any other keys except each other.
     */
    private boolean doKeysMaintainBijection(T key1, T key2) {
        return (key1ToKey2Map.get(key1) == null && key2ToKey1Map.get(key2) == null)
                || (key1ToKey2Map.get(key1) != null && key1ToKey2Map.get(key1).equals(key2)
                && key2ToKey1Map.get(key2) != null && key2ToKey1Map.get(key2).equals(key1));
    }
    /**
     * Determine specified keys can maintain bijectiveness of data structure if that is requirement
     */
    public boolean areKeysValid(T key1, T key2){
        return !keysMustHaveBijectiveRelation || doKeysMaintainBijection(key1, key2);
    }

    private void addBidirectionalKeyRelations(T key1, T key2) {
        key1ToKey2Map.put(key1, key2);
        key2ToKey1Map.put(key2, key1);
    }

    /**
     * Updates existing key1 or key2 with a new key so long as existing provided key was not an alias pointing to another key, but it can be a key that another alias is pointed to.
     */
    public boolean updateBidirectionalKeyRelations(T existingProvidedKey, T replacementKey) {
        if (containsKey(existingProvidedKey) && !isAliasKey(existingProvidedKey) && !containsKey(replacementKey)) {
            boolean providedKeyWasAKey1 = isKey1(existingProvidedKey);
            Collection<T> previousAliases = getAllAliasesForKey(existingProvidedKey);

            //More convenient to just use an existing method to initially remove all relevant prexisting relations associated with provided key
            removeAllKeyRelationsForKey(existingProvidedKey);

            addBidirectionalKeyRelations(providedKeyWasAKey1 ? existingProvidedKey : replacementKey
                    , providedKeyWasAKey1 ? replacementKey : existingProvidedKey);

            //Readd unidirectional aliases that were removed
            for (T alias : previousAliases)
                addUniDirectionalKeyRelation(alias, existingProvidedKey);

            return true;
        }
        return false;
    }

    /**
     * Associates an alias with a provided key.
     *
     * @param aliasKey Alias to associated with provided key 1. Must not already be an existing key 1 or key 2 in data structure.
     * @param key1     Key to associated with alias. Must already exists a a key1 in data structure.
     * @return Whether or not alias was successfully added without violating restrictions.
     */
    public boolean addUniDirectionalKeyRelation(T aliasKey, T key1) {
        //The alias must be unique and must be established with an existing key1
        if (!isKey1(aliasKey) && !isKey2(aliasKey) && isKey1(key1)) {
            /*Use key2--key1 map for the alias-key1 relation so that another separate map object
             *does not need to be initialized and independently maintained. Also allows for
             *easy integration and the least modification to existing implementations. */
            key2ToKey1Map.put(aliasKey, key1);

            hasAliases = true;
        }
        return hasAliases;
    }

    private U addItemToCategory(V category, T key2, U item) {
        if (!key2ToItemMapsByCategory.containsKey(category)) {
            key2ToItemMapsByCategory.put(category, new HashMap<>());
        }
        key2ToItemMapsByCategory.get(category).put(key2, item);
        return item;
    }

    ///
    public boolean removeItemFromCategoryByKey(V category, T anyKey) {
        if (containsCategory(category)
                && key2ToItemMapsByCategory.get(category)
                .remove(isKey1(anyKey) //Convert key1's and aliases to their proper key2 equivalent before deleting from map
                        ? getKey2FromKey1(anyKey)
                        : isAliasKey(anyKey)
                        ? getKey2FromKey1(getKey1TargetOfAlias(anyKey)) : anyKey) != null)

        {
            removeAllKeyRelationsForKey(anyKey);
            if (emptyCategoriesAreRemoved && key2ToItemMapsByCategory.get(category).isEmpty())
                key2ToItemMapsByCategory.remove(category);
            return true;
        } else {
            return false;
        }
    }

    public boolean removeItemByKey(T anyKey) {
        return removeItem(null, anyKey, null);
    }

    public boolean removeItem(U item) {
        return removeItem(null, null, item);
    }

    public boolean removeCategory(V category) {
        return removeItem(category, null, null);
    }

    public boolean removeAllItems() {
        return removeItem(null, null, null);
    }

    /**
     * In most calls either category, key, or item parameters will not be null and the remaining parameters be null or
     * all three will be null. The non null parameter is the parameter upon which removal is based;
     * however, if all are null then no restriction is met and consequently everything is removed.
     * The explicit use of iterator in the loop construct prevents concurrent modifications exceptions although it's a bit bulky.
     */
    private boolean removeItem(V category, T key, U item) {
        boolean someItemRemoved = false;
        T alternateKey = getKey2FromKey1(isAliasKey(key) ? getKey1TargetOfAlias(key) : key);
        boolean removeEverything = category == null && key == null && item == null;

        for (Iterator<Entry<V, Map<T, U>>> key2ToItemByCategoryIterator = key2ToItemMapsByCategory.entrySet().iterator()
             ; key2ToItemByCategoryIterator.hasNext(); ) {
            Entry<V, Map<T, U>> key2ToItemByCategoryEntry = key2ToItemByCategoryIterator.next();

            //
            if (category != null && key2ToItemByCategoryEntry.getKey().equals(category) || removeEverything) {
                removeAllKeyRelationsForKeys(key2ToItemByCategoryEntry.getValue().keySet());
                key2ToItemByCategoryIterator.remove();
                if (!removeEverything) {
                    return true;
                } else { //Continue removing other stuff if necessary
                    someItemRemoved = true;
                    continue;
                }
            }

            //
            for (Iterator<Entry<T, U>> key2ToItemIterator = key2ToItemByCategoryEntry.getValue().entrySet().iterator()
                 ; key2ToItemIterator.hasNext(); ) {
                Entry<T, U> key2ToItemEntry = key2ToItemIterator.next();

                if (item != null && item.equals(key2ToItemEntry.getValue())) {
                    removeAllKeyRelationsForKey(key2ToItemEntry.getKey());
                    key2ToItemIterator.remove();
                    if (duplicateItemsAreRemoved) { //No duplicated items had been added to data structure, therefore no need to search any further
                        return true;
                    } else {
                        someItemRemoved = true;
                        continue;
                    }
                }

                if (key != null && (key2ToItemEntry.getKey().equals(key)
                        || key2ToItemEntry.getKey().equals(alternateKey))) {
                    removeAllKeyRelationsForKey(key);
                    key2ToItemIterator.remove();
                    if (keysMustHaveBijectiveRelation && duplicateItemsAreRemoved)
                        return true;
                    someItemRemoved = true;
                }
            }
            //
            if (emptyCategoriesAreRemoved && key2ToItemByCategoryEntry.getValue().isEmpty())
                key2ToItemByCategoryIterator.remove();
        }
        return someItemRemoved;
    }

    /**
     * Removes a unidirectional alias relations if any
     *
     * @return True only if the the key was an alias and removed.
     */
    public boolean removeUnitDirectionalRelation(T aliasKey) {
        return (isAliasKey(aliasKey)) && key2ToKey1Map.remove(aliasKey) != null;
    }

    /**
     * Removes key1 to key2 relations as well as relevant aliases. If an alias is te provided key
     * , then the alias relation to key 1 is removed as well as the bidirectional relation of key1 and key 2.
     * Should only be sparingly used to prevent data integrity inconsistencies.
     */
    public boolean removeAllKeyRelationsForKey(T providedKey) {
        boolean providedKeyWasAnAlias = isAliasKey(providedKey);

        //If the provided key is a key 1 in the data structrue then correpsonding key 2 is returned
        // , if it's associated with a key 2 then the corresponding key1 is returned, otherwise null.
        T oppositeKeyAssociatedWithProvidedKey = isKey2(providedKey) ? getKey1FromKey2(providedKey) : (isKey1(providedKey) ? providedKey : null);

        boolean providedKeyWasBothKey1AndKey2 = isKey1(providedKey) && isKey2(providedKey);

        //But if null, then there is no need to proceed any further since there exists no key associations
        //(alias or otherwise) to delete
        if (oppositeKeyAssociatedWithProvidedKey == null)
            return false;

        T removedKey2 = key1ToKey2Map.remove(key2ToKey1Map.remove(isKey1(providedKey) && !isKey2(providedKey)
                ? getKey2FromKey1(providedKey) : providedKey));
        if (providedKeyWasBothKey1AndKey2)
            key2ToKey1Map.remove(key1ToKey2Map.remove(providedKey));

        //Remove key2 --> key1 that was not captured by nested map removal for this case.
        if (providedKeyWasAnAlias)
            return key2ToKey1Map.remove(removedKey2) != null;

        //Delete remaining relations based on value of maps due to potential aliasing and nonbijectivity
        if (hasAliases || !keysMustHaveBijectiveRelation) {
            if (key1ToKey2Map.values().contains(providedKey) || key1ToKey2Map.values().contains(oppositeKeyAssociatedWithProvidedKey))
                removeFromValuesInKeyRelations(key1ToKey2Map, providedKey, oppositeKeyAssociatedWithProvidedKey);

            if (key2ToKey1Map.values().contains(providedKey) || key2ToKey1Map.values().contains(oppositeKeyAssociatedWithProvidedKey))
                removeFromValuesInKeyRelations(key2ToKey1Map, providedKey, oppositeKeyAssociatedWithProvidedKey);
        }

        return true;
    }

    private void removeFromValuesInKeyRelations(Map<T, T> keyRelationMap, T providedKey, T oppositeKey) {
        Iterator<Entry<T, T>> entryIterator = keyRelationMap.entrySet().iterator();
        while (entryIterator.hasNext()) {
            T value = entryIterator.next().getValue();
            if (value.equals(providedKey) || value.equals(oppositeKey))
                entryIterator.remove();
        }
    }

    public boolean removeAllKeyRelationsForKeys(Iterable<T> keys) {
        boolean someRelationRemoved = false;
        for (T key : keys)
            someRelationRemoved = removeAllKeyRelationsForKey(key);
        return someRelationRemoved;
    }

    ///
    public U getFirstItemByAnyKey(T key) {
        //Find item based on key1 or key2 for first category in enumeration.
        U item = null;
        for (V category : getAllAssignedCategories()) {
            item = getItem(category, key);
            if (item != null)
                break;
        }
        return item;
    }

    public U getItem(V category, T key) {
        if (containsCategory(category)) {
            U item = key2ToItemMapsByCategory.get(category).get(key);
            if (item == null) {
                T key2 = getKey2FromKey1(key);
                if (key2 == null && isAliasKey(key))
                    key2 = getKey2FromKey1(getKey1FromKey2(key));
                if (key2 != null)
                    item = key2ToItemMapsByCategory.get(category).get(key2);
            }
            return item;
        } else {
            return null;
        }
    }

    public T getKey2(V category, U item) {
        if (containsItem(item)) {
            for (T key : getKey2sByCategory(category)) {
                if (key2ToItemMapsByCategory.get(category).get(key) == item)
                    return key;
            }
        }
        return null;
    }

    public T getKey1(V category, U item) {
        return getKey1FromKey2(getKey2(category, item));
    }

    public V getCategoryOfKey(T key) {
        if (containsKey(key)) {
            for (V category : getAllAssignedCategories()) {
                if (getItem(category, key) != null)
                    return category;
            }
        }
        return null;
    }

    public Collection<U> getItemsByCategory(V category) {
        return key2ToItemMapsByCategory.containsKey(category)
                ? Collections.unmodifiableCollection(key2ToItemMapsByCategory.get(category).values())
                : Collections.emptyList();
    }

    public Collection<U> getItemsByAnyKey(T anyKey) {
        //All items based on key1 or key2 for any category
        Collection<U> items = new ArrayList<>();
        for (V category : getAllAssignedCategories()) {
            U item = getItem(category, anyKey);
            if (item != null) {
                items.add(item);
                //No need to search any further since key will not be found in any other category.
                if (keysMustHaveBijectiveRelation)
                    break;
            }
        }
        return items;
    }

    public Collection<U> getAllItems() {
        Collection<U> items = new ArrayList<>();
        for (V category : key2ToItemMapsByCategory.keySet()) {
            items.addAll(getItemsByCategory(category));
        }
        return items;
    }

    public Collection<T> getKey2sByCategory(V category) {
        if (!containsCategory(category))
            return Collections.emptyList();

        return key2ToItemMapsByCategory.get(category).keySet();
    }

    public Collection<T> getKey1sByCategory(V category) {
        Collection<T> keys = new HashSet<>();
        for (T key2 : getKey2sByCategory(category)) {
            keys.add(getKey1FromKey2(key2));
        }
        return keys;
    }

    public Collection<T> getAllKey1s() {
        return key1ToKey2Map.keySet();
    }

    public Collection<T> getAllKey2s() {
        return key2ToKey1Map.keySet();
    }

    public Collection<T> getAllKeys() {
        Collection<T> allKeys = new ArrayList(getAllKey1s());
        allKeys.addAll(getAllKey2s());
        return allKeys;
    }

    public Collection<T> getAllAliasesForKey(T key) {
        ArrayList<T> aliases = new ArrayList<>();
        if (hasAliases) {
            for (Entry<T, T> key2toKey1Entry : key2ToKey1Map.entrySet()) {
                if (key2toKey1Entry.getValue().equals(key)
                        && getKey2FromKey1(key2toKey1Entry.getValue()) != key2toKey1Entry.getKey()) //Unidirectional
                {
                    aliases.add(key2toKey1Entry.getKey());
                }
            }
        }
        return aliases;
    }

    public Collection<V> getAllAssignedCategories() {
        return key2ToItemMapsByCategory.keySet();
    }

    ///
    public T getKey2FromKey1(T key1) {
        //Only gets last added association if non bijection.
        return key1 != null ? key1ToKey2Map.get(key1) : null;
    }

    public T getKey1FromKey2(T key2) {
        //Only gets last added association if non bijection.
        return key2 != null ? key2ToKey1Map.get(key2) : null;
    }

    private T getKey1TargetOfAlias(T alias) {
        return alias != null ? key2ToKey1Map.get(alias) : null;
    }

    ///
    public boolean containsItem(U item) {
        for (V category : getAllAssignedCategories()) {
            if (key2ToItemMapsByCategory.get(category).values().contains(item))
                return true;
        }
        return false;
    }

    public boolean containsKey(T key) {
        return isKey1(key) || isKey2(key);
    }

    public boolean containsCategory(V category) {
        return key2ToItemMapsByCategory.containsKey(category);
    }

    public boolean containsKeyInCategory(V category, T key) {
        return containsCategory(category)
                && getKey2sByCategory(category).contains(isKey1(key) ? getKey2FromKey1(key) : key);

    }

    public boolean containsItemInCategory(V category, U item) {
        return containsCategory(category) && getItemsByCategory(category).contains(item);
    }

    public boolean isKey2(T key) {
        return key2ToKey1Map.containsKey(key);
    }

    public boolean isKey1(T key) {
        return key1ToKey2Map.containsKey(key);
    }

    public boolean isAliasKey(T key) {
        return hasAliases && isKey2(key) && !key1ToKey2Map.containsValue(key); //Must be unidirectional
    }

    ///
    public boolean combineWith(IDualKeyNCategoryRepository<T, U, V> otherDataModel) {
        if(otherDataModel == null)
            return true;

        if (otherDataModel.getClass() != this.getClass())
            return false;

        DualKeyNCategoryHashedRepository otherHashedDataModel = (DualKeyNCategoryHashedRepository) otherDataModel;

        //Data models must have compatible configurations.
        if (this.keysMustHaveBijectiveRelation != otherHashedDataModel.keysMustHaveBijectiveRelation
                || this.duplicateItemsAreRemoved != otherHashedDataModel.duplicateItemsAreRemoved
                || this.emptyCategoriesAreRemoved != otherHashedDataModel.emptyCategoriesAreRemoved)
            return false;

        this.key2ToItemMapsByCategory.putAll(otherHashedDataModel.key2ToItemMapsByCategory);
        this.key1ToKey2Map.putAll(otherHashedDataModel.key1ToKey2Map);
        this.key2ToKey1Map.putAll(otherHashedDataModel.key2ToKey1Map);

        return true;
    }

    ///
    public boolean keysMustHaveBijectiveRelation() {
        return keysMustHaveBijectiveRelation;
    }

    public boolean duplicateItemsAreRemoved() {
        return duplicateItemsAreRemoved;
    }

    public boolean emptyCategoriesAreRemoved() {
        return emptyCategoriesAreRemoved;
    }

    public boolean hasAliases() {
        return hasAliases;
    }
}
