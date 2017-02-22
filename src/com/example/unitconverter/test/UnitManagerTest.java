package com.example.unitconverter.test;

import java.util.HashMap;
import java.util.Map;

import com.example.unitconverter.UnitManager;
import com.example.unitconverter.UnitManagerFactory;
import com.example.unitconverter.UnitManager.UNIT_TYPE;

import junit.framework.TestCase;

public class UnitManagerTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
		
		//Fundamental Units Map
		Map<String, Map<String, UNIT_TYPE>> fundUnitsMap = new HashMap<String, Map<String, UNIT_TYPE>> ();
		fundUnitsMap.put("si", new HashMap<String, UNIT_TYPE>());
		fundUnitsMap.put("us customary", new HashMap<String, UNIT_TYPE>());
		fundUnitsMap.get("si").put("meter", UNIT_TYPE.LENGTH);
		fundUnitsMap.get("si").put("meter", UNIT_TYPE.LENGTH);
		
		//Prefixes Map
		Map<String, Double> prefixesMap = new HashMap<String, Double>();
		prefixesMap.put("kilo", 1000.0);
		prefixesMap.put("milli", 0.001);
		
		UnitManagerFactory unitManagerFactory = new UnitManagerFactory();
	}

	public void testUpdateAssociationsOfUnknownUnits() {
		fail("Not yet implemented"); // TODO
	}

	public void testGetUnitStringBoolean() {
		fail("Not yet implemented"); // TODO
	}

	public void testContainsUnitUnit() {
		fail("Not yet implemented"); // TODO
	}

	public void testContainsUnitByFundamentalDimension() {
		fail("Not yet implemented"); // TODO
	}

	public void testGetUnitsByComponentUnitsDimensionMapOfStringDoubleBoolean() {
		fail("Not yet implemented"); // TODO
	}

	public void testGetUnitsByComponentUnitsDimensionStringBoolean() {
		fail("Not yet implemented"); // TODO
	}

	public void testGetUnitsByFundamentalUnitsDimensionMapOfUNIT_TYPEDouble() {
		fail("Not yet implemented"); // TODO
	}

	public void testGetUnitsByFundamentalUnitsDimensionString() {
		fail("Not yet implemented"); // TODO
	}

	public void testGetUnitsByUnitSystem() {
		fail("Not yet implemented"); // TODO
	}

	public void testGetUnitsByCategory() {
		fail("Not yet implemented"); // TODO
	}

	public void testGetCorrespondingUnitsWithUnitSystem() {
		fail("Not yet implemented"); // TODO
	}

	public void testGetConversionFactorToTargetUnit() {
		fail("Not yet implemented"); // TODO
	}

	public void testGetConversionFactorToUnitSystem() {
		fail("Not yet implemented"); // TODO
	}

	public void testGetBaseUnit() {
		fail("Not yet implemented"); // TODO
	}

	public void testCalculateFundmtUnitsFromCompUnitsExpMap() {
		fail("Not yet implemented"); // TODO
	}

	public void testDetermineUnitType() {
		fail("Not yet implemented"); // TODO
	}

	public void testGetComponentUnitsDimensionFromString() {
		fail("Not yet implemented"); // TODO
	}

	public void testGetFundamentalUnitsDimensionFromString() {
		fail("Not yet implemented"); // TODO
	}

	public void testAddUnitToHierarchy() {
		fail("Not yet implemented"); // TODO
	}

	public void testRemoveUnitFromHierarchy() {
		fail("Not yet implemented"); // TODO
	}

}
