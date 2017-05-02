package com.isaacapps.unitconverterapp.models;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.*;

import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager.DATA_MODEL_CATEGORY;
import com.isaacapps.unitconverterapp.models.unitmanager.Utility;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel.*;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.UnitsDataModel;

public class Quantity {
	public static String GROUPING_REGEX = "[\\{\\}]";
	public static String UNIT_GROUPING_REGEX = "\\{"+Utility.UNIT_NAME_REGEX+"\\}";
	public static String VALUE_GROUPING_REGEX = "{[\\s]*(\\d*[.])?\\d+[\\s]*}";
	private SortedMap<Unit, Double> unitValueMap;
	
	///
	public Quantity(Map<Unit, Double> unitValueMap){
		this.unitValueMap = toUnmodifiableSortedUnitValueMap(unitValueMap);
	}
	public Quantity(){
		this(Collections.EMPTY_MAP);
	}
	public Quantity(Collection<Double> values, Collection<Unit> units){
		setUnitValues(units, values);
	}
	public Quantity(double value, Unit unit){
		this(Arrays.asList(value), Arrays.asList(unit));
	}
	public Quantity(double value){
		this(Arrays.asList(value), Arrays.asList(new Unit()));
	}
	public Quantity(Unit unit){
		this(Arrays.asList(0.0), Arrays.asList(unit));
	}
	public Quantity(String valuesGrouping, String unitsGrouping, UnitManager unitManager){
		this(toValidatedValues(valuesGrouping), toValidatedUnits(unitsGrouping, unitManager));	
	}	
	public Quantity(String valueNUnitGrouping, UnitManager unitManager){
		Matcher valueGroupRegExMatcher = Pattern.compile(VALUE_GROUPING_REGEX+"+").matcher(valueNUnitGrouping);
		String unitsGrouping = "", valuesGrouping = "";
		
		if(valueGroupRegExMatcher.find()){
			valuesGrouping = valueGroupRegExMatcher.group();
			unitsGrouping = valueNUnitGrouping.replace(valuesGrouping, "");
		}

		unitValueMap = toUnModifiableSortedUnitValueMap(toValidatedUnits(unitsGrouping, unitManager)
				                                        ,toValidatedValues(valuesGrouping));
	}
	
	///
	private static boolean unitsAreCompatible(Collection<Unit> units){
		//Units must belong to the same unit manager reference basis and as well as have the same dimension.
		if(!units.isEmpty()){
			UnitManager compatibleUnitManager = units.iterator().next().getUnitManagerRef();
			Unit firstUnit = units.iterator().next();
			for(Unit unit:units){
				if(!unit.equalsDimension(firstUnit) || unit.getUnitManagerRef() != compatibleUnitManager)
					return false;
			}
		}	
		return true;
	}
	public static Collection<Unit> toValidatedUnits(Collection<Unit> units){
		//Make sure units is nonempty unmodifiable list have same dimension, otherwise a null set with an unknown unit is returned from a unit manager reference if any exists.
		UnitManager unitManager = units.isEmpty() ? units.iterator().next().getUnitManagerRef():null;
		return  Collections.unmodifiableCollection(!units.isEmpty() && unitsAreCompatible(units)
				                             ?units:Arrays.asList( unitManager != null 
				                                                   ? unitManager.getUnitsDataModel().getUnknownUnit(): new Unit()));
	}
	public static Collection<Unit> toValidatedUnits(String unitsGrouping, UnitManager unitManager){
		Collection<Unit> unitsToBeValidated = new ArrayList<Unit>();
		Matcher unitGroupMatcher = Pattern.compile(UNIT_GROUPING_REGEX).matcher(unitsGrouping);
		while(unitGroupMatcher.find()){
			Collection<Unit> matchedUnits = unitManager.getUnitsDataModel()
					                                   .getUnitsByComponentUnitsDimension(unitGroupMatcher.group().replace(GROUPING_REGEX, ""), false);
			if(!matchedUnits.isEmpty())
				unitsToBeValidated.add(matchedUnits.iterator().next());
		}
		return toValidatedUnits(unitsToBeValidated);
	}
	public static Collection<Double> toValidatedValues(Collection<Double> values){
		return !values.isEmpty()?values:Arrays.asList(0.0);
	}
	public static Collection<Double> toValidatedValues(String valuesGrouping){
		Collection<Double> valuesToBeValidated = new ArrayList<Double>();
		
		if(!valuesGrouping.contains("{"))
			valuesGrouping = "{"+valuesGrouping;
		if(!valuesGrouping.contains("}"))
			valuesGrouping = valuesGrouping+"}";
			
		Matcher valueGroupMatcher = Pattern.compile(VALUE_GROUPING_REGEX).matcher(valuesGrouping);
		while(valueGroupMatcher.find()){
			valuesToBeValidated.add(Double.parseDouble(valueGroupMatcher.group().replace(GROUPING_REGEX, "")));
		}
		return toValidatedValues(valuesToBeValidated);
	}
	private static SortedMap<Unit, Double> toUnmodifiableSortedUnitValueMap(Map<Unit, Double> unitValueMap){
		SortedMap<Unit, Double> sortedUnitValueMap = new TreeMap<Unit, Double>( new Comparator<Unit>(){
	        //Sorts units from largest to smallest based on base conversion to the same common base unit.
			@Override
			public int compare(Unit lhsUnit, Unit rhsUnit) {
				return -Double.compare(lhsUnit.getBaseConversionPolyCoeffs()[0], rhsUnit.getBaseConversionPolyCoeffs()[0]);
			}
			
		});

		if( !unitValueMap.isEmpty() && unitsAreCompatible(new ArrayList(unitValueMap.keySet()))){	
			sortedUnitValueMap.putAll(unitValueMap);
		}
		else{
			//Get the unknown unit from the unit manager reference from one of the units in the unit group so long unit manager is not null and unit group is not empty
			UnitManager unitManager = !unitValueMap.isEmpty() ? unitValueMap.keySet().iterator().next().getUnitManagerRef():null;
			sortedUnitValueMap.put(unitManager != null ? unitManager.getUnitsDataModel().getUnknownUnit():new Unit(), 0.0);
		}

		return Collections.unmodifiableSortedMap(sortedUnitValueMap);
	}
	private static SortedMap<Unit, Double> toUnModifiableSortedUnitValueMap(Collection<Unit> units, Collection<Double> values){
		//Assumption is that the units and values are paired based on corresponding positions in the two lists.
		List<Unit> validatedUnits = new ArrayList<Unit>(toValidatedUnits(units));
		List<Double> validatedValues = new ArrayList<Double>(toValidatedValues(values));
		if(validatedUnits.size() == validatedUnits.size()){
			Map<Unit, Double> unitValueMap = new HashMap<Unit, Double>();
			for(int i=0;i<validatedUnits.size();i++)
				unitValueMap.put(validatedUnits.get(i), validatedValues.get(i));
			return toUnmodifiableSortedUnitValueMap(unitValueMap);
		}
		else{
			return toUnmodifiableSortedUnitValueMap(new HashMap()); //Return a sorted map with unknown unit;
		}
	}
	
	///
	public Quantity add(Quantity secondQuantity){
		return quantityOperation(1, secondQuantity);
	}
	public Quantity subtract(Quantity secondQuantity){
		return quantityOperation(-1, secondQuantity);
	}
	private Quantity quantityOperation(int sign, Quantity secondQuantity){
		Quantity newQuantity;
	 	
		if(this.equalsUnit(secondQuantity)){
			newQuantity = secondQuantity.toUnit(this.getLargestUnit());
			newQuantity.setValues(Arrays.asList(this.getValueRespectToLargestUnit() + sign*newQuantity.getValueRespectToLargestUnit()));
		}
		else{
			if(getUnitManagerRef() != null){
				newQuantity = new Quantity(getUnitManagerRef().getUnitsDataModel().getUnit(Unit.UNKNOWN_UNIT_NAME));
			}
			else{
				newQuantity = new Quantity();
			}
		}
		return newQuantity;		
	}
	
	public Quantity multiply(Quantity secondQuantity){
		return new Quantity(this.getValueRespectToLargestUnit()*secondQuantity.getValueRespectToLargestUnit()
				            , this.getLargestUnit().multiply(secondQuantity.getLargestUnit()));
	}
	public Quantity divide(Quantity secondQuantity){
		//To reduce complexity only accounts for the largest unit and corresponding weighted value
		return new Quantity(this.getValueRespectToLargestUnit()/secondQuantity.getValueRespectToLargestUnit()
				            , this.getLargestUnit().divide(secondQuantity.getLargestUnit()));
	}
	
	///
	public Quantity toUnit(Unit targetUnit){
		Collection<Unit> targetUnits = Arrays.asList(targetUnit);
		return new Quantity(calculateValuesForTargetUnitsGroup(targetUnits), targetUnits);
	}
	public Quantity toUnitsGroup(Collection<Unit> targetUnits){
		return new Quantity( calculateValuesForTargetUnitsGroup(targetUnits),  targetUnits);
	}
	private Collection<Double> calculateValuesForTargetUnitsGroup(Collection<Unit> targetUnits){
		/*If the target units match and is not empty, then use the weighted sum of each of the Source values 
		 *to find the distributed values associated with the target units. */	
		List<Unit> validatedTargetUnits = new ArrayList<Unit>(toValidatedUnits(targetUnits));	
		
		if(UnitsDataModel.getDataModelCategory(validatedTargetUnits.get(0)) != DATA_MODEL_CATEGORY.UNKNOWN
		   && validatedTargetUnits.get(0).equalsDimension(getLargestUnit())){

			Collections.sort(validatedTargetUnits, this.unitValueMap.comparator()); //Sort from largest to least unit.
			
			return calculateValuesGroupForTargetUnitsWithRespectToUnitBasis(this.getLargestUnit(), 
					                                                        calculateReducedValueWithRespectToLargestUnit(this.unitValueMap)
					                                                        , validatedTargetUnits);
		}
		else{
			return new ArrayList<Double>();
		}
	}
	private double calculateReducedValueWithRespectToLargestUnit(Map<Unit, Double> unitValueMap){
		//Reduces the all the unit values to one value with the largest unit as the basis.
		double[] conversionCoeffs;
		Double weightedSumOfValues = unitValueMap.get(getLargestUnit());
		for(Iterator<Entry<Unit,Double>> iterator = unitValueMap.entrySet().iterator(); iterator.hasNext();){
			Entry<Unit, Double> unitValueEntry = iterator.next();
			conversionCoeffs = unitValueEntry.getKey().getConversionCoeffsToTargetUnit(getLargestUnit());
			weightedSumOfValues += unitValueEntry.getValue()*conversionCoeffs[0] + conversionCoeffs[1];
		}
		return weightedSumOfValues;
	}
	private Collection<Double> calculateValuesGroupForTargetUnitsWithRespectToUnitBasis(Unit basisUnit, Double valueOfBasisUnit, List<Unit> targetUnits){
		double[] conversionCoeffs = targetUnits.get(0).getConversionCoeffsToTargetUnit(basisUnit);
		List<Double> targetValues = new ArrayList<Double>(targetUnits.size());
		targetValues.add(valueOfBasisUnit*conversionCoeffs[0]+conversionCoeffs[1]);
		
		for(int i=1;i<targetValues.size();i++){
			conversionCoeffs = targetUnits.get(i-1).getConversionCoeffsToTargetUnit(targetUnits.get(i));
			double cascadedRemainder = targetValues.get(i-1) - Math.floor(targetValues.get(i-1));
			targetValues.add(cascadedRemainder*conversionCoeffs[0] + conversionCoeffs[1]);
		}
		
		return targetValues;
	}
	
	public Quantity toUnitSystem(String targetUnitSystem){
		//Convert every component of the largest unit to one unit system. Makes sure to appropriately scale the weighted value for largest unit when returning new quantity.	
		double value = 0.0;
		Unit targetUnit = new Unit();
		if(getUnitManagerRef() != null && getLargestUnit().getType() != UNIT_TYPE.UNKNOWN){
			Collection<Unit> correspondingUnits = getUnitManagerRef().getUnitsDataModel().getCorrespondingUnitsWithUnitSystem(getLargestUnit(), targetUnitSystem);
			
			if(!correspondingUnits.isEmpty()){
				targetUnit = correspondingUnits.iterator().next();
			    double[] conversionCoeffs = getLargestUnit().getConversionCoeffsToUnitSystem(targetUnitSystem);
			    value = calculateReducedValueWithRespectToLargestUnit(unitValueMap)*conversionCoeffs[0] + conversionCoeffs[1];
			}
		}
		return new Quantity(value, targetUnit);
	}
	
	///
 	public boolean equalsValue(Quantity otherQuantity){
		return this.getValueRespectToLargestUnit() == otherQuantity.getValueRespectToLargestUnit();
	}
	public boolean equalsUnit(Quantity otherQuantity){
		//The use of only the first unit for comparison is sufficient since dimensional equality is enforced during Quantity initialization
		return (getLargestUnit().equalsDimension(otherQuantity.getLargestUnit()) );
	}
	public boolean equalsValueNUnit(Quantity otherQuantity){
		return equalsValue(otherQuantity)&&equalsUnit(otherQuantity);
	}
	
	///
	public Double getValueRespectToLargestUnit(){
		return calculateReducedValueWithRespectToLargestUnit(unitValueMap);
	}
	public Collection<Double> getValues(){
		return unitValueMap.values();
	}
	public String getValuesString(){
		String representation = "";
		for(Entry<Unit, Double> unitValueEntry:unitValueMap.entrySet()){
			representation += String.format("{%f} ", unitValueEntry.getValue());
		}
		return representation.trim();
	}
	public boolean setValues(Collection<Double> values){
		if(values.size() == unitValueMap.size()){
			unitValueMap = toUnModifiableSortedUnitValueMap(unitValueMap.keySet(), values);
			return true;
		}
		else{
			return false;
		}
	}
	
	public Unit getLargestUnit(){
		//Assumption is the units were already sorted from greatest to least during quantity initialization
		return unitValueMap.firstKey();
	}
	public Collection<Unit> getUnits(){
		return unitValueMap.keySet();
	}
	public String getUnitNames(){
		String representation = "";
		for(Entry<Unit, Double> unitValueEntry:unitValueMap.entrySet()){
			representation += String.format("{%s} ", unitValueEntry.getKey().getName());
		}
		return representation.trim();
	}
	public boolean setUnits(Collection<Unit> units){
		Collection<Double> values = Arrays.asList(new Double[units.size()]);  //List of zeros.
		SortedMap<Unit, Double> tempUnitValueMap = toUnModifiableSortedUnitValueMap(units, values);
		//Make sure the validation results is that the units are not invalid.
		if(!(UnitsDataModel.getDataModelCategory(tempUnitValueMap.firstKey()) == DATA_MODEL_CATEGORY.UNKNOWN
			 && tempUnitValueMap.size() == 1)){ 
			return true;
		}
		else{
			return false;
		}
	}
	
	public boolean setUnitValues(Collection<Unit> units, Collection<Double> values){
		SortedMap<Unit, Double> tempUnitValueMap = toUnModifiableSortedUnitValueMap(units, values);
		if(!tempUnitValueMap.firstKey().getName().equalsIgnoreCase(Unit.UNKNOWN_UNIT_NAME)){
			unitValueMap = tempUnitValueMap;
			return true;
		}
		else{
			return false;
		}
	}
	
	public boolean isValid(){
		return  !(unitValueMap.size() == 1
				&& UnitsDataModel.getDataModelCategory(unitValueMap.firstKey()) == DATA_MODEL_CATEGORY.UNKNOWN
				&& unitValueMap.values().iterator().next() == 0.0);
	}
	
	///
	public UnitManager getUnitManagerRef(){
		return !unitValueMap.isEmpty()?unitValueMap.firstKey().getUnitManagerRef():null;
	}
	
	///
	@Override
	public String toString(){
		String representation = "";
		for(Entry<Unit, Double> unitValueEntry:unitValueMap.entrySet()){
			representation += String.format("{%f} {%s}; ", String.valueOf(unitValueEntry.getValue()), unitValueEntry.getKey().getName());
		}
		return representation;
	}
}
