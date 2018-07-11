package com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContentModifierTest {
    private UnitManager mockUnitManger;
    private Unit mockUnit;

    @Before
    public void setUp() {
        String unitName = "meter", alias = "metre", abbreviation = "m";

        mockUnitManger = mock(UnitManager.class);

        mockUnit = mock(Unit.class);
        when(mockUnit.getName()).thenReturn(unitName);
        when(mockUnit.getAbbreviation()).thenReturn(abbreviation);
        when(mockUnit.getAliases()).thenReturn(new HashSet<>(Arrays.asList(alias)));
        when(mockUnit.getBaseUnit()).thenReturn(mockUnit);
        when(mockUnit.isBaseUnit()).thenReturn(true);
        when(mockUnit.isCoreUnit()).thenReturn(true);
        when(mockUnit.getUnitManagerContext()).thenReturn(mockUnitManger);

        //TODO: ....
    }

    @Test
    public void addUnit_Should_Associate_Unit_And_Its_Names_And_Aliases_And_Type_In_Data_Structure(){

        //TODO: ......
    }
}