package com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.hashdriven;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class DualKeyNCategoryHashedRepositoryTest {

    @Test
    public void addItem_Should_Successfully_Associate_Item_With_Keys_And_Category_And_Create_Key_Relationships(){
        DualKeyNCategoryHashedRepository<String, Object, String> baseDataModel = new DualKeyNCategoryHashedRepository(false, false, false);

        String category = "category";
        String key1 = "key1";
        String key2 = "key2";
        Object item = new Object();

        //
        Object returnedItem = baseDataModel.addItem(category, key1, key2, item);

        //
        assertThat("Added item not returned", returnedItem, is(notNullValue()));
        assertThat("Data structure does not contain item", baseDataModel.containsItem(item), is(true));

        assertThat("Key 1 is not tied to item.", baseDataModel.getItemsByAnyKey(key1), hasItem(item));
        assertThat("Category is not tied to item.", baseDataModel.getItemsByCategory(category), hasItem(item));
        assertThat("Category is not tied to item.", baseDataModel.containsItemInCategory(category, item), is(true));

        assertThat("Item not tied to category AND key1", baseDataModel.getItem(category, key1), is(item));
        assertThat("Item not tied to category AND key1", baseDataModel.getKey1(category, item), is(key1));

        assertThat("Item not tied to category AND key2", baseDataModel.getItem(category, key2), is(item));
        assertThat("Item not tied to category AND key2", baseDataModel.getKey2(category, item), is(key2));

        assertThat("Key 1 is not tied to category", baseDataModel.getCategoryOfKey(key1), is(category));
        assertThat("Key 2 is not tied to category", baseDataModel.containsKeyInCategory(category, key2), is(true));
        assertThat("Key 1 is not tied to category", baseDataModel.getKey1sByCategory(category), hasItem(key1));
        assertThat("Key 2 is not tied to category", baseDataModel.getKey2sByCategory(category), hasItem(key2));

    }

    @Test
    public void addItem_Should_Not_Associate_Item_When_Keys_And_Items_Violate_Bijection_Requirement(){
        DualKeyNCategoryHashedRepository<String, Object, String> baseDataModel = new DualKeyNCategoryHashedRepository(true, false, false);

        String category = "category";
        String key1 = "key1";
        String key2 = "key2";

        String otherCategory = "otherCategory";
        String otherKey1 = "otherKey1";
        String otherKey2 = "otherKey2";

        Object item = new Object();
        Object otherItem = new Object();

        //
        baseDataModel.addItem(category, key1, key2, item);

        //
        assertThat("Item added even though key2 already associated",baseDataModel.addItem( category, otherKey1, key2, otherItem), not(otherItem));
        assertThat("Item added even though key1 already associated",baseDataModel.addItem(category, key1, otherKey2, otherItem), not(otherItem));

        assertThat("Item added with one key different even though item already associated in data structure", baseDataModel.addItem(category, key1, otherKey2, item), not(item));
        //assertThat("Item added with all keys different even though item already associated in data structure", baseDataModel.addItem(category, otherKey1, otherKey2, item), not(item));
        //assertThat("Item added with different category and keys even though item already associated in data structure", baseDataModel.addItem(otherCategory, otherKey1, otherKey2, item), not(item));
    }

    @Test
    public void addItem_Should_Allow_Replacement_Of_Item_With_Same_Category_And_Keys_When_Bijection_Requirement(){
        DualKeyNCategoryHashedRepository<String, Object, String> baseDataModel = new DualKeyNCategoryHashedRepository(true, false, false);

        String category = "category";
        String key1 = "key1";
        String key2 = "key2";

        Object item = new Object();
        Object otherItem = new Object();

        //
        baseDataModel.addItem(category, key1, key2, item);
        baseDataModel.addItem(category, key1, key2, otherItem);

        //
        assertThat("Old item still tied to key1", baseDataModel.getItem(category, key1), not(item));
        assertThat("Old item still tied to key2", baseDataModel.getItem(category, key2), not(item));

        assertThat("New item not tied to key1", baseDataModel.getItem(category, key1), is(otherItem));
        assertThat("New item not tied to key2", baseDataModel.getItem(category, key2), is(otherItem));
    }

    @Test
    public void addItem_Should_Remove_Duplicate_Item(){
        DualKeyNCategoryHashedRepository<String, Object, String> baseDataModel = new DualKeyNCategoryHashedRepository(false, true, false);

        String category = "category";
        String key1 = "key1";
        String key2 = "key2";

        String otherCategory = "otherCategory";
        String otherKey1 = "otherKey1";
        String otherKey2 = "otherKey2";

        Object item = new Object();

        //
        baseDataModel.addItem(category, key1, key2, item);
        baseDataModel.addItem(otherCategory, otherKey1, otherKey2, item);

        //
        assertThat("Old item still tied to key1", baseDataModel.getItem(category, key1), not(item));
        assertThat("Old item still tied to key2", baseDataModel.getItem(category, key2), not(item));

        assertThat("New item not tied to new key1", baseDataModel.getItem(otherCategory, otherKey1), is(item));
        assertThat("New item not tied to new key2", baseDataModel.getItem(otherCategory, otherKey2), is(item));
    }

    @Test
    public void addUnidirectionalKeyRelation_Should_Associate_Alias_To_Key1_And_Item(){
        DualKeyNCategoryHashedRepository<String, Object, String> baseDataModel = new DualKeyNCategoryHashedRepository(false, true, false);

        String category = "category";
        String key1 = "key1";
        String key2 = "key2";

        String aliasKey = "aliasKey";

        Object item = new Object();

        //
        baseDataModel.addItem(category, key1, key2, item);
        boolean aliasAdded = baseDataModel.addUniDirectionalKeyRelation(aliasKey, key1);

        //
        assertThat("Alias not added", aliasAdded, is(true));

        assertThat("Alias key is not identified as an alias key", baseDataModel.isAliasKey(aliasKey), is(true));
        assertThat("Alias key is not associated with key 1", baseDataModel.getKey1FromKey2(aliasKey), is(key1));
        assertThat("Alias key association with key 1 is not unidirectional", baseDataModel.getKey2FromKey1(key1), is(not(aliasKey)));

        assertThat("Item not associated to alias", baseDataModel.getFirstItemByAnyKey(aliasKey), is(item));
    }

    @Test
    public void addUnidirectionalKeyRelation_Should_Not_Associate_Alias_If_Already_Key1_Or_Key2(){
        DualKeyNCategoryHashedRepository<String, Object, String> baseDataModel = new DualKeyNCategoryHashedRepository(false, true, false);

        String category = "category";
        String key1 = "key1";
        String key2 = "key2";

        Object item = new Object();

        baseDataModel.addItem(category, key1, key2, item);

        //
        String aliasKey = key1;

        boolean aliasAdded = baseDataModel.addUniDirectionalKeyRelation(aliasKey, key1);

        assertThat("Alias is added when already exists as key1", aliasAdded, is(false));
        assertThat("Alias key is identified as an alias key", baseDataModel.isAliasKey(aliasKey), is(false));

        //
        aliasKey = key2;

        aliasAdded = baseDataModel.addUniDirectionalKeyRelation(aliasKey, key1);

        assertThat("Alias is added when already exists as key2", aliasAdded, is(false));
        assertThat("Alias key is identified as an alias key", baseDataModel.isAliasKey(aliasKey), is(false));
    }

    @Test
    public void addUnidirectionalKeyRelation_Should_Not_Associate_Alias_If_Target_Is_Not_Key1(){
        DualKeyNCategoryHashedRepository<String, Object, String> baseDataModel = new DualKeyNCategoryHashedRepository(false, true, false);

        String category = "category";
        String key1 = "key1";
        String key2 = "key2";

        Object item = new Object();

        baseDataModel.addItem(category, key1, key2, item);

        //
        String aliasKey = "aliasKey";

        boolean aliasAdded = baseDataModel.addUniDirectionalKeyRelation(aliasKey, key2);

        //
        assertThat("Alias is added when target is not key 1", aliasAdded, is(false));
        assertThat("Alias key is identified as an alias key", baseDataModel.isAliasKey(aliasKey), is(false));
    }

    @Test
    public void updateBidirectionalKeyRelation_Should_Update_Key_Relations(){
        DualKeyNCategoryHashedRepository<String, Object, String> baseDataModel = new DualKeyNCategoryHashedRepository(false, true, false);

        String category = "category";
        String key1 = "key1";
        String key2 = "key2";

        Object item = new Object();

        baseDataModel.addItem(category, key1, key2, item);

        String otherKey = "otherKey";

        //
        boolean relationUpdated = baseDataModel.updateBidirectionalKeyRelations(key1, otherKey);

        //
        assertThat("Bidirectional relation not updated when relation for key 1 updated.", relationUpdated, is(true));
        assertThat("Key 1 not associated with key 2. Key 1 relation updateContent.", baseDataModel.getKey2FromKey1(key1), is(otherKey));
        assertThat("Key 2 not associated with key 1. Key 1 relation updateContent.", baseDataModel.getKey1FromKey2(otherKey), is(key1));
    }

    @Test
    public void updateBidirectionalKeyRelation_Should_Not_Update_Bidirectional_Key_Relations(){
        DualKeyNCategoryHashedRepository<String, Object, String> baseDataModel = new DualKeyNCategoryHashedRepository(false, true, false);

        String category = "category";
        String key1 = "key1";
        String key2 = "key2";

        Object item = new Object();

        baseDataModel.addItem(category, key1, key2, item);

        String otherKey1 = "otherKey1";
        String otherKey2 = "otherKey2";

        //
        boolean relationUpdated = baseDataModel.updateBidirectionalKeyRelations(otherKey1, otherKey2);
        assertThat("Bidirectional relation is updated when no existing key used as source.", relationUpdated, is(false));

        //
        relationUpdated = baseDataModel.updateBidirectionalKeyRelations(key1, key2);
        assertThat("Bidirectional relation is updated when target key already existing.", relationUpdated, is(false));
    }

    @Test
    public void updateBidirectionalKeyRelation_Should_Not_Affect_Unidirectional_Key_Relations(){
        DualKeyNCategoryHashedRepository<String, Object, String> baseDataModel = new DualKeyNCategoryHashedRepository(false, true, false);

        String category = "category";
        String key1 = "key1";
        String key2 = "key2";

        String aliasKey = "aliasKey";
        String otherKey2 = "otherKey2";

        Object item = new Object();

        baseDataModel.addItem(category, key1, key2, item);
        baseDataModel.addUniDirectionalKeyRelation(aliasKey, key1);

        //
        boolean relationUpdated = baseDataModel.updateBidirectionalKeyRelations(key1, otherKey2);
        assertThat("Bidirectional relation was not updated.", relationUpdated, is(true));

        assertThat("Unidirectional alias for key1 is no longer present."
                , baseDataModel.getAllAliasesForKey(key1), hasItem(aliasKey));
    }

    @Test
    public void removeKeyRelations_Should_Remove_Bidirectional_And_Unidirectional_Relations_Without_Bijection_Restriction(){
        DualKeyNCategoryHashedRepository<String, Object, String> baseDataModel = new DualKeyNCategoryHashedRepository(false, true, false);

        String category1 = "category1";
        String category2 = "category2";
        String key1 = "key1";
        String key2 = "key2";
        String key3 = "key3";
        String key4 = "key4";
        String key5 = "key5";
        String aliasKey = "aliasKey";

        Object item1 = new Object();
        Object item2 = new Object();
        Object item3 = new Object();

        //Non bijective case where a key(key 2 in this case) is present in multiple relation pairing
        baseDataModel.addItem(category1, key1, key2, item1);
        baseDataModel.addItem(category2, key3, key1, item2);

        baseDataModel.addItem(category2, key4, key5, item3);

        baseDataModel.addUniDirectionalKeyRelation(aliasKey, key4);

        //
        boolean biDirectionalRelationRemoved = baseDataModel.removeAllKeyRelationsForKeys(Arrays.asList(key2, aliasKey));

        //
        assertThat("Some bidirectional relations not removed", biDirectionalRelationRemoved, is(true));

        assertThat("Key 1 still tied to Key 2", baseDataModel.getKey1FromKey2(key2), is(nullValue()));
        assertThat("Key 2 still tied to Key 1", baseDataModel.getKey2FromKey1(key1), is(nullValue()));

        assertThat("Key 3 still tied to Key 2", baseDataModel.getKey1FromKey2(key2), is(nullValue()));
        assertThat("Key 2 still tied to Key 3", baseDataModel.getKey2FromKey1(key3), is(nullValue()));

        assertThat("Key 4 still tied to Key 5", baseDataModel.getKey1FromKey2(key5), is(nullValue()));
        assertThat("Key 5 still tied to Key 4", baseDataModel.getKey2FromKey1(key4), is(nullValue()));

        assertThat("Alias key still tied to key 4", baseDataModel.isAliasKey(aliasKey), is(false));
    }

    @Test
    public void removeUnitDirectionalRelation_Should_Remove_Unidirectional_Relation(){
        DualKeyNCategoryHashedRepository<String, Object, String> baseDataModel = new DualKeyNCategoryHashedRepository(false, true, false);

        String category = "category";
        String key1 = "key1";
        String key2 = "key2";
        String aliasKey = "aliasKey";

        Object item = new Object();

        baseDataModel.addItem(category, key1, key2, item);

        baseDataModel.addUniDirectionalKeyRelation(aliasKey, key1);

        //
        boolean uniDirectionalRelationRemoved = baseDataModel.removeUnitDirectionalRelation(aliasKey);

        //
        assertThat("Unidirectional alias relation not removed", uniDirectionalRelationRemoved, is(true));
        assertThat("Alias is stil tied to key 1", baseDataModel.isAliasKey(aliasKey), is(false));
    }

    @Test
    public void removeItemFromCategoryByAnyKey_Should_Remove_Item_And_Key_Relations(){
        DualKeyNCategoryHashedRepository<String, Object, String> baseDataModel = new DualKeyNCategoryHashedRepository(false, true, false);

        String category1 = "category1";
        String category2 = "category2";
        String key1 = "key1";
        String key2 = "key2";
        String key3 = "key3";
        String alias = "alias";

        Object item1 = new Object();
        Object item2 = new Object();

        baseDataModel.addItem(category1, key1, key2, item1);
        baseDataModel.addItem(category2, key3, key3, item2);

        baseDataModel.addUniDirectionalKeyRelation(alias, key3);

        //
        boolean removalSucceeded = baseDataModel.removeItemFromCategoryByKey(category1, key1);

        assertThat("Removal not successful", removalSucceeded, is(true));
        assertThat("Item 1 not removed", baseDataModel.containsItem(item1), is(false));
        assertThat("All of key1 and key2 relations not removed"
                , baseDataModel.containsKey(key1) && baseDataModel.containsKey(key2)
                , is(false));

        //
        removalSucceeded = baseDataModel.removeItemFromCategoryByKey(category2, alias);

        assertThat("Removal not successful", removalSucceeded, is(true));
        assertThat("Item 2 not removed", baseDataModel.containsItem(item2), is(false));
        assertThat("Key 3 is still considered a key", baseDataModel.containsKey(key3), is(false));
        assertThat("Alias relationship to key 3 not removed", baseDataModel.isAliasKey(alias)
                , is(false));
    }

    @Test
    public void removeItemByKey_Should_Remove_Item_And_Key_Relations(){
        DualKeyNCategoryHashedRepository<String, Object, String> baseDataModel = new DualKeyNCategoryHashedRepository(false, false, false);

        String category = "category";
        String key1 = "key1";
        String key2 = "key2";
        String key3 = "key3";
        String alias = "alias";

        Object item1 = new Object();
        Object item2 = new Object();

        baseDataModel.addItem(category, key1, key2, item1);
        baseDataModel.addItem(category, key3, key3, item2);

        baseDataModel.addUniDirectionalKeyRelation(alias, key3);

        //
        boolean removalSucceeded = baseDataModel.removeItemByKey(key1);

        assertThat("Removal not successful", removalSucceeded, is(true));
        assertThat("Item 1 not removed", baseDataModel.containsItem(item1), is(false));
        assertThat("All of key1 and key2 relations not removed"
                , baseDataModel.containsKey(key1) && baseDataModel.containsKey(key2)
                , is(false));

        //
        removalSucceeded = baseDataModel.removeItemByKey(alias);

        assertThat("Removal not successful", removalSucceeded, is(true));
        assertThat("Item 2 not removed", baseDataModel.containsItem(item2), is(false));
        assertThat("Key 3 is still considered a key", baseDataModel.containsKey(key3), is(false));
        assertThat("Alias relationship to key 3 not removed", baseDataModel.isAliasKey(alias)
                , is(false));
    }

    @Test
    public void removeItem_Should_Remove_Items_And_Key_Relations_And_Category_When_Empty(){
        DualKeyNCategoryHashedRepository<String, Object, String> baseDataModel = new DualKeyNCategoryHashedRepository(false, false, true);

        String category1 = "category1";
        String category2 = "category2";
        String key1 = "key1";
        String key2 = "key2";
        String key3 = "key3";
        String alias = "alias";

        Object item1 = new Object();

        baseDataModel.addItem(category1, key1, key2, item1);
        baseDataModel.addItem(category2, key3, key3, item1);

        baseDataModel.addUniDirectionalKeyRelation(alias, key3);

        //
        boolean removalSucceeded = baseDataModel.removeItem(item1);

        assertThat("Removal not successful", removalSucceeded, is(true));
        assertThat("Item 1 not removed", baseDataModel.containsItem(item1), is(false));
        assertThat("All of key1 and key2 relations not removed"
                , baseDataModel.containsKey(key1) && baseDataModel.containsKey(key2)
                , is(false));
        assertThat("Key 3 is still considered a key", baseDataModel.containsKey(key3), is(false));
        assertThat("Alias relationship to key 3 not removed", baseDataModel.isAliasKey(alias)
                , is(false));
        assertThat("Category 1 was not removed", baseDataModel.containsCategory(category1), is(false));
    }

    @Test
    public void removeAllItems_Should_Remove_All_Items_And_Their_Associated_Categories_And_Keys(){
        DualKeyNCategoryHashedRepository<String, Object, String> baseDataModel = new DualKeyNCategoryHashedRepository(false, true, false);

        String category = "category";
        String key1 = "key1";
        String key2 = "key2";
        String key3 = "key3";
        String alias = "alias";

        Object item1 = new Object();
        Object item2 = new Object();

        baseDataModel.addItem(category, key1, key2, item1);
        baseDataModel.addItem(category, key3, key3, item2);

        baseDataModel.addUniDirectionalKeyRelation(alias, key3);

        //
        baseDataModel.removeAllItems();

        //
        assertThat("All categories not successfully removed", baseDataModel.getAllAssignedCategories(), not(hasItem(anything())));
        assertThat("All keys not successfully combined.", baseDataModel.getAllKeys(), not(hasItem(anything())));
        assertThat("All items not successfully removed.", baseDataModel.getAllItems(), not(hasItem(anything())));
    }

    @Test
    public void removeCategory_Should_Remove_Item_And_Key_Relations_From_Specified_Category(){
        DualKeyNCategoryHashedRepository<String, Object, String> baseDataModel = new DualKeyNCategoryHashedRepository(false, false, false);

        String category1 = "category1";
        String category2 = "category2";
        String key1 = "key1";
        String key2 = "key2";
        String key3 = "key3";
        String alias = "alias";

        Object item1 = new Object();
        Object item2 = new Object();

        baseDataModel.addItem(category1, key1, key2, item1);
        baseDataModel.addItem(category2, key3, key3, item2);

        baseDataModel.addUniDirectionalKeyRelation(alias, key1);

        //
        boolean removalSucceeded = baseDataModel.removeCategory(category1);

        assertThat("Removal not successful", removalSucceeded, is(true));

        assertThat("Category 1 was not removed", baseDataModel.containsCategory(category1), is(false));
        assertThat("Item 1 in category 1 is not removed", baseDataModel.containsItem(item1), is(false));
        assertThat("All of key1 and key2 relations not removed"
                , baseDataModel.containsKey(key1) && baseDataModel.containsKey(key2)
                , is(false));
        assertThat("Alias relationship to key 1 not removed", baseDataModel.isAliasKey(alias)
                , is(false));

        assertThat("Category 2 was removed", baseDataModel.containsCategory(category2), is(true));
        assertThat("Item 2 in category 2 is removed", baseDataModel.containsItem(item2), is(true));
        assertThat("Key 3 is not still considered a key", baseDataModel.containsKey(key3), is(true));
    }

    @Test
    public void combineWith_Should_Join_All_Categories_Respective_Categories_And_Item_When_Configurations_Compatible(){
        DualKeyNCategoryHashedRepository<String, Object, String> baseDataModel1 = new DualKeyNCategoryHashedRepository(false, true, false);
        DualKeyNCategoryHashedRepository<String, Object, String> baseDataModel2 = new DualKeyNCategoryHashedRepository(false, true, false);

        String category1 = "category1";
        String category2 = "category2";
        String key1 = "key1";
        String key2 = "key2";
        String key3 = "key3";
        String key4 = "key4";
        String alias1 = "alias1";
        String alias2 = "alias2";

        Object item1 = new Object();
        Object item2 = new Object();

        baseDataModel1.addItem(category1, key1, key2, item1);
        baseDataModel2.addItem(category2, key3, key4, item2);

        baseDataModel1.addUniDirectionalKeyRelation(alias1, key1);
        baseDataModel2.addUniDirectionalKeyRelation(alias2, key3);

        //
        boolean combinationSuccessful = baseDataModel1.combineWith(baseDataModel2);

        //
        assertThat("Data model combination was not successful", combinationSuccessful, is(true));

        assertThat("All categories not successfully combined.", baseDataModel1.getAllAssignedCategories(), hasItems(category1, category2));
        assertThat("All keys not successfully combined.", baseDataModel1.getAllKeys(), hasItems(key1, key2, key3, key4));
        assertThat("Alias 1 not in combined data model", baseDataModel1.getAllAliasesForKey(key1), hasItems(alias1));
        assertThat("Alias 2 not in combined data model", baseDataModel1.getAllAliasesForKey(key3), hasItems(alias2));
        assertThat("All items not successfully combined.", baseDataModel1.getAllItems(), hasItems(item1, item2));
    }

    @Test
    public void combineWith_Should_Be_Unsucccessful_When_DataModel_Configurations_Incompatible(){
        DualKeyNCategoryHashedRepository<String, Object, String> baseDataModel1 = new DualKeyNCategoryHashedRepository(true, true, false);
        DualKeyNCategoryHashedRepository<String, Object, String> baseDataModel2 = new DualKeyNCategoryHashedRepository(false, false, true);

        String category1 = "category1";
        String category2 = "category2";
        String key1 = "key1";
        String key2 = "key2";
        String key3 = "key3";
        String key4 = "key4";

        Object item1 = new Object();
        Object item2 = new Object();

        baseDataModel1.addItem(category1, key1, key2, item1);
        baseDataModel2.addItem(category2, key3, key4, item2);

        //
        boolean combinationSuccessful = baseDataModel1.combineWith(baseDataModel2);

        //
        assertThat("Data model combination was successful despite configuration incompatibilty", combinationSuccessful, is(false));
    }

}