package com.isaacapps.unitconverterapp.models.unitmanager.datamodels;

import java.util.*;
import java.util.Map.Entry;

//Generic template for a custom categorized data structure that associates two keys of identical types to each other and individually to an item.  
public abstract class AbstractDataModelWithDualKeyNCategory<T, U, V> {
	private Map<V,Map<T, U>> key2ToItemMapsByCategory;
	private Map<T, T> key1ToKey2Map;
	private Map<T, T> key2ToKey1Map;
	private boolean keysMustHaveBijectiveRelation;
	
	///
	AbstractDataModelWithDualKeyNCategory(boolean keysMustHaveBijectiveRelation){
		key2ToItemMapsByCategory = new HashMap<V,Map<T, U>>();
		key1ToKey2Map = new HashMap<T, T>();
		key2ToKey1Map = new HashMap<T, T>();
		this.keysMustHaveBijectiveRelation = keysMustHaveBijectiveRelation;
	}
	
	///
	protected U addItem(V category, T key1, T key2, U item, boolean removeDuplicateItems){
		U addedItem = null;
		
		//Ensure bijective relationship of keys if required
		boolean isBijective = !key1ToKey2Map.containsValue(key1) && !key2ToKey1Map.containsValue(key2),
				hasIdenticalKeys = key1ToKey2Map.get(key1) != null &&  key1ToKey2Map.get(key1).equals(key2ToKey1Map.get(key2)); //Allows an identity key relation to be replaced despite bijection restriction 
		
		if(keysMustHaveBijectiveRelation && (isBijective || hasIdenticalKeys) || !keysMustHaveBijectiveRelation)
		{			
			//If state is selected, then there can not be duplicates of item anywhere in this data structure.
			if(removeDuplicateItems)
				removeItem(item);
			
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
	protected U removeItemByAnyKey(T key){ //Remove item by key from any category and as well the key relations
		U removedItem = null;	
		for (V category:key2ToItemMapsByCategory.keySet()) {
			if (key2ToKey1Map.containsKey(key)) {
				removedItem = removeItemFromCategory(category, key);

			} else if (key1ToKey2Map.containsKey(key)) {
				removedItem = removeItemFromCategory(category, key1ToKey2Map.get(key));
			} 
			
			removeKeyRelations(key);
		}
		return removedItem;
	}
	private void removeKeyRelations(T key){
		if (key2ToKey1Map.containsKey(key)) {
			key1ToKey2Map.remove(key2ToKey1Map.remove(key));

		} else if (key1ToKey2Map.containsKey(key)) {
			key1ToKey2Map.remove(key2ToKey1Map.remove(key));
		} 
		//Since bijections relations of keys is not met, the big O of removal may be O(n) due to removal of duplicate values in opposite map
		if(!keysMustHaveBijectiveRelation){ 
			for(Iterator<Entry<T, T>> entryIterator =  key2ToKey1Map.entrySet().iterator(); entryIterator.hasNext();){
				if(entryIterator.next().getValue().equals(key))
					entryIterator.remove();	
			}
		}
	}
	private U removeItemFromCategory(V category, T key2){
		U removedItem = null;
		if(containsCategory(category)){
			removedItem = key2ToItemMapsByCategory.get(category).remove(key2);
			if(key2ToItemMapsByCategory.get(category).isEmpty())
				key2ToItemMapsByCategory.remove(category); //remove category if nothing associated with it anymore.
		}
		return removedItem;
	}
	
	protected boolean removeItem(U item){ //Remove item by its object reference from any category. Since searching done by value rather by keys, the Big O runtime is O(n).
		boolean somethingRemoved = false;
		for (V category:getAllAssignedCategories()) {
			for(Iterator<Entry<T, U>> key2ToItemEntryIterator = key2ToItemMapsByCategory.get(category).entrySet().iterator()
				;key2ToItemEntryIterator.hasNext();){
				
				Entry<T, U> key2ToItemEntry = key2ToItemEntryIterator.next();
				if(key2ToItemEntry.getValue().equals(item)){
					removeKeyRelations(key2ToItemEntry.getKey());
					key2ToItemEntryIterator.remove();
					somethingRemoved = true;
				}		
			}	
		}
		return somethingRemoved;
	}
	protected boolean removeCategory(V category){
		boolean somethingRemoved = false;
		for(T key2:getKey2sByCategory(category)){
			somethingRemoved = removeItemByAnyKey(key2) != null;
		}
		return somethingRemoved;
	}
	
	protected void removeAllItems(){
		for(T key1:key1ToKey2Map.keySet()){
			removeItemByAnyKey(key1);
		}
	}
	
	///
	protected U getItemByAnyKey(T key){
		//Find item using based on key 1 or key 2 for any category
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
	protected Collection<T> getKey1sbyCategory(V category){
		Collection<T> keys = new HashSet<T>();
		for(T key2:getKey2sByCategory(category)){
			keys.add(getKey1FromKey2(key2));
		}
		return keys;
	}	
	
	protected Collection<T> getAllKey1s(){
		Collection<T> keys = new ArrayList<T>();
		for(V category:getAllAssignedCategories()){
			keys.addAll(getKey1sbyCategory(category));
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
		return key1ToKey2Map.containsKey(key)||key2ToKey1Map.containsKey(key);
	}
	protected boolean containsCategory(V category){
		return key2ToItemMapsByCategory.containsKey(category);
	}
	protected boolean containsKeyInCategory(V category, T key){
		if(!containsCategory(category))
			return false;
		
		if(containsKey(key) && isKey1(key))
			key = getKey2FromKey1(key);
		
		return getKey2sByCategory(category).contains(key);		
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
