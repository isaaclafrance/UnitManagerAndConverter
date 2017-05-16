package com.isaacapps.unitconverterapp.models.unitmanager.datamodels;

import java.util.*;
import java.util.Map.Entry;

//Generic template for a custom categorized data structure that associates two keys of identical types to each other and individually to an item.  
public abstract class AbstractDataModelWithDualKeyNCategory<T, U, V> {
	private Map<V,Map<T, U>> key2ToItemMapsByCategory;
	private Map<T, T> key1ToKey2Map;
	private Map<T, T> key2ToKey1Map;
	private boolean keysMustHaveBijectiveRelation;
	private boolean removeDuplicateItems;
	private boolean removeEmptyCategories;
	
	///
	AbstractDataModelWithDualKeyNCategory(boolean keysMustHaveBijectiveRelation, boolean removeDuplicateItems, boolean removeEmptyCategories){
		key2ToItemMapsByCategory = new HashMap<V,Map<T, U>>();
		key1ToKey2Map = new HashMap<T, T>();
		key2ToKey1Map = new HashMap<T, T>();
		this.keysMustHaveBijectiveRelation = keysMustHaveBijectiveRelation;
		this.removeDuplicateItems = removeDuplicateItems;
		this.removeEmptyCategories = removeEmptyCategories;
	}
	
	///
	protected U addItem(V category, T key1, T key2, U item){
		U addedItem = null;
		
		//Ensure bijective relationship of keys if required. These keys can not be found any where else across all categories.
		boolean isBijective = !key1ToKey2Map.containsValue(key1) && !key2ToKey1Map.containsValue(key2)
				,hasIdenticalKeys = key1ToKey2Map.get(key1) != null &&  key1ToKey2Map.get(key1).equals(key2ToKey1Map.get(key2)) //Allows an identity key relation to be replaced despite bijection restriction 
				,keysAreAlreadyAssociatedWithExistingOtherItem = isKey1(key1) && isKey2(key2) && containsItem(item); //Allow existing keys to be replaced with a different item
		
		if(keysMustHaveBijectiveRelation && (isBijective || hasIdenticalKeys || keysAreAlreadyAssociatedWithExistingOtherItem) 
		   || !keysMustHaveBijectiveRelation)
		{			
			if(removeDuplicateItems)
				removeItem(null, null, item);
			
			addedItem = addItemToCategory(category, key2, item);
			key1ToKey2Map.put(key1, key2);
			key2ToKey1Map.put(key2, key1);
		}
		
		return addedItem;
	}
	protected U addItemToCategory(V category, T key2, U item){
		if(!key2ToItemMapsByCategory.containsKey(category)){
			key2ToItemMapsByCategory.put(category, new HashMap<T, U>());	
		}
		key2ToItemMapsByCategory.get(category).put(key2, item);
		return item;
	}
	
	///
	protected boolean removeItemFromCategoryByKey(V category, T anyKey){
		if(containsCategory(category)
			&& key2ToItemMapsByCategory.get(category)
			                .remove(isKey1(anyKey)
			                		?getKey2FromKey1(anyKey)
			                				:anyKey) != null) //Remove with respect to key1 or key 2 which ever is valid

		{
			removeKeyRelations(anyKey);
			if(removeEmptyCategories && key2ToItemMapsByCategory.get(category).isEmpty())
				key2ToItemMapsByCategory.remove(category);
			return true;
		}
		else{
			return false;
		}
	}
	protected boolean removeItemByKey(T anyKey){
		return removeItem(null, anyKey, null);
	}	
	protected boolean removeItem(U item){
		return removeItem(null, null, item);
	}
	
	protected boolean removeCategory(V category){
		return removeItem(category, null, null);
	}
	
	protected void removeAllItems(){
		removeItem(null, null, null);
	}
	
	/*In most call either category, key, or item parameters will not be null and the remaining parameters not null or
	 *all three will be null. The non null parameter is the parameter upon which removal is based; however, if all are null then
	 *no restriction is met and consequently everything is removed. The use of iterator prevent concurrent modifications exceptions although it's 
	 *a bit bulky. */
	private boolean removeItem(V category, T key, U item){ 		
		boolean someItemRemoved = false;
		for ( Iterator<Entry<V, Map<T, U>>> key2ToItemByCategoryIterator = key2ToItemMapsByCategory.entrySet().iterator()
				; key2ToItemByCategoryIterator.hasNext();) 
		{
			Entry<V,Map<T,U>> key2ToItemByCategoryEntry = key2ToItemByCategoryIterator.next();
			
			boolean removeEverything = category == null && key == null && item == null;
			
			//
			if(key2ToItemByCategoryEntry.getKey() == category || removeEverything){
				removeKeyRelations(key2ToItemByCategoryEntry.getValue().keySet());
				key2ToItemByCategoryIterator.remove();
				if(!removeEverything){
					return true;
				}
				else{ //Continue removing other stuff if necessary
					someItemRemoved = true;
					continue;
				}
			}
			
			//
			for( Iterator<Entry<T,U>> key2ToItemIterator = key2ToItemByCategoryEntry.getValue().entrySet().iterator()
					; key2ToItemIterator.hasNext();)
			{
				Entry<T,U> key2ToItemEntry = key2ToItemIterator.next();
				
				if(item != null && item == key2ToItemEntry.getValue()){
					removeKeyRelations(key2ToItemEntry.getKey());
					key2ToItemIterator.remove();
					if(!removeDuplicateItems){ //No duplicated items had been added to data structure, therefore no need to search any further
						return true;	
					}		
					else{
						someItemRemoved = true; 
						continue;
					}
				}		
				if( key != null && (key2ToItemEntry.getKey() == key || getKey2FromKey1(key) == key) ){
					removeKeyRelations(key);
					key2ToItemIterator.remove();
					if(keysMustHaveBijectiveRelation)
						return true;
				}
			}
			
			//
			if( removeEmptyCategories && key2ToItemByCategoryEntry.getValue().isEmpty())
				key2ToItemByCategoryIterator.remove(); 

		}
		return someItemRemoved;
	}
	protected void removeKeyRelations(T key){//Should only be sparingly used by descendent classes
		if (key2ToKey1Map.containsKey(key)) {
			key1ToKey2Map.remove(key2ToKey1Map.remove(key));

		} else if (key1ToKey2Map.containsKey(key)) {
			key1ToKey2Map.remove(key2ToKey1Map.remove(key));
		} 
		//Since bijections relations of keys is not met, the big O of removal may be O(n) due to removal of duplicate values in opposite map
		if(!keysMustHaveBijectiveRelation){ 
			for(Iterator<Entry<T, T>> entryIterator =  key2ToKey1Map.entrySet().iterator()
					; entryIterator.hasNext();)
			{
				if(entryIterator.next().getValue().equals(key))
					entryIterator.remove();	
			}
		}
	}
	private void removeKeyRelations(Collection<T> keys){
		for(T key:keys){
			removeKeyRelations(key);
		}
	}
	
	///
	protected U getFirstItemByAnyKey(T key){
		//Find item based on key1 or key2 for first category in enumeration.
		U item = null;
		for(V category:getAllAssignedCategories()){
			item = getItem(category, key);
			if(item != null)
				break;
		}	
		return item;
	}
	protected U getItem(V category, T key){	
		if(containsCategory(category)){
			U item = key2ToItemMapsByCategory.get(category).get(key);
			if(item == null ){
				T key2 = getKey2FromKey1(key);
				if(key2 != null)
					item = key2ToItemMapsByCategory.get(category).get(key2);
			}	
			return item;
		}
		else{
			return null;
		}	
	}
	
	protected V getCategoryOfKey(T key){
		if(containsKey(key)){
			for(V category:getAllAssignedCategories()){
				if(getItem(category, key) != null)
					return category;
			}
		}
		return null;
	}
		
	protected Collection<U> getItemsByCategory(V category){
		Collection<U> items = new ArrayList<U>();
		if(key2ToItemMapsByCategory.containsKey(category)){
			items = new ArrayList<U>(key2ToItemMapsByCategory.get(category).values());
		}
		return items;
	}
	protected Collection<U> getItemsByAnyKey(T anyKey){
		//All items based on key1 or key2 for any category
		Set<U> items = new HashSet<U>();
		for(V category:getAllAssignedCategories()){
			U item = getItem(category, anyKey);
			if(item != null){
				items.add(item);
				 //No need to search any further since key will not be found in any other category.
				if(keysMustHaveBijectiveRelation)
					break;
			}
		}	
		return items;
	}
	protected Collection<U> getAllItems(){
		Collection<U> items = new ArrayList<U>();
		for(V category:key2ToItemMapsByCategory.keySet()){
			items.addAll(getItemsByCategory(category));
		}
		return items;
	}
	
	protected Collection<T> getKey2sByCategory(V category){
		if(!containsCategory(category))
			return new HashSet<T>();
			
		return key2ToItemMapsByCategory.get(category).keySet();
	}
	protected Collection<T> getKey1sByCategory(V category){
		Collection<T> keys = new HashSet<T>();
		for(T key2:getKey2sByCategory(category)){
			keys.add(getKey1FromKey2(key2));
		}
		return keys;
	}	
	
	protected Collection<T> getAllKey1s(){
		Collection<T> keys = new ArrayList<T>();
		for(V category:getAllAssignedCategories()){
			keys.addAll(getKey1sByCategory(category));
		}
		return keys;
	}
	protected Collection<T> getAllKey2s(){
		Collection<T> keys = new ArrayList<T>();
		for(V category:getAllAssignedCategories()){
			keys.addAll(getKey2sByCategory(category));
		}
		return keys;
	}
	protected Collection<T> getAllKeys(){
		Collection<T> allKeys = getAllKey1s();
		allKeys.addAll(getAllKey2s());
		return allKeys;
	}
	
	protected Collection<V> getAllAssignedCategories(){
		return key2ToItemMapsByCategory.keySet();
	}
	
	///
	protected T getKey2FromKey1(T key1){
		//Only gets last added association if non bijection.
		return key1ToKey2Map.get(key1);
	}
	protected T getKey1FromKey2(T key2){
		//Only gets last added association if non bijection.
		return key2ToKey1Map.get(key2);
	}
	
	///
	protected boolean containsItem(U item){
		for(V category:getAllAssignedCategories()){
			if(key2ToItemMapsByCategory.get(category).values().contains(item))
				return true;
		}
		return false;
	}
	protected boolean containsKey(T key){
		return isKey1(key)||isKey2(key);
	}
	protected boolean containsCategory(V category){
		return key2ToItemMapsByCategory.containsKey(category);
	}
	protected boolean containsKeyInCategory(V category, T key){
		if(!containsCategory(category))
			return false;
				
		return getKey2sByCategory(category)
				.contains( isKey1(key)
						? getKey2FromKey1(key): key);		
	}
	
	protected boolean isKey2(T key){
		return key2ToKey1Map.containsKey(key);
	}
	protected boolean isKey1(T key){
		return key1ToKey2Map.containsKey(key);
	}
	
	///
	public void combineWith(AbstractDataModelWithDualKeyNCategory<T, U, V> otherDataModel){
		if(otherDataModel != null){
			this.key2ToItemMapsByCategory.putAll(otherDataModel.key2ToItemMapsByCategory);
			this.key1ToKey2Map.putAll(otherDataModel.key1ToKey2Map);
			this.key2ToKey1Map.putAll(otherDataModel.key2ToKey1Map);
		}
	}
}
