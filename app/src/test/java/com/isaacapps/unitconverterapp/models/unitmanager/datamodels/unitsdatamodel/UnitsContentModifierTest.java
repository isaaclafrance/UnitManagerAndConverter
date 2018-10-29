package com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel;

import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.measurables.unit.UnitException;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.UnitsClassifierDataModel;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.IDualKeyNCategoryRepository;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.databasedriven.UnitsDualKeyNCategoryDatabaseRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.AdditionalAnswers;

import java.util.Arrays;
import java.util.HashSet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UnitsContentModifierTest {
    private UnitManager mockUnitManager;
    private UnitsDataModel mockUnitDataModel;
    private IDualKeyNCategoryRepository<String, Unit, UnitsContentDeterminer.DATA_MODEL_CATEGORY> mockUnitsDualKeyNCategoryRepository;
    private Unit mockUnit;

    @Before
    public void setUp() {
        String unitName = "meter", alias = "metre", abbreviation = "m";

        mockUnitManager = mock(UnitManager.class);

        mockUnitsDualKeyNCategoryRepository = mock(UnitsDualKeyNCategoryDatabaseRepository.class);

        mockUnitDataModel = mock(UnitsDataModel.class);
        when(mockUnitDataModel.getUnitManagerContext()).thenReturn(mockUnitManager);
        when(mockUnitDataModel.getRepositoryWithDualKeyNCategory()).thenReturn(mockUnitsDualKeyNCategoryRepository);

        mockUnit = mock(Unit.class);
        when(mockUnit.getName()).thenReturn(unitName);
        when(mockUnit.getAbbreviation()).thenReturn(abbreviation);
        when(mockUnit.getAliases()).thenReturn(new HashSet<>(Arrays.asList(alias)));
        when(mockUnit.getUnitManagerContext()).thenReturn(mockUnitManager);
    }

    @Test
    public void addUnit_Should_Associate_BaseUnit_And_Its_Names_And_Aliases_And_Type_In_Data_Structure() throws UnitException {

        ///
        when(mockUnit.getBaseUnit()).thenReturn(mockUnit);
        when(mockUnit.isBaseUnit()).thenReturn(true);
        when(mockUnit.isCoreUnit()).thenReturn(true);

        when(mockUnitsDualKeyNCategoryRepository.containsItem(mockUnit)).thenReturn(false);
        when(mockUnitsDualKeyNCategoryRepository.addItem(any(UnitsContentDeterminer.DATA_MODEL_CATEGORY.class), anyString(), anyString(), any(Unit.class))).thenAnswer(AdditionalAnswers.returnsLastArg());

        FundamentalUnitsDataModel mockFundamentalUnitsDataModel = mock(FundamentalUnitsDataModel.class);
        when(mockFundamentalUnitsDataModel.containsUnitName(anyString())).thenReturn(true);

        UnitsContentMainRetriever mockUnitsContentMainRetriever = mock(UnitsContentMainRetriever.class);
        when(mockUnitsContentMainRetriever.getBaseUnits()).thenReturn(anyCollection());

        UnitsClassifierDataModel mockUnitsClassifierDataModel = mock(UnitsClassifierDataModel.class);

        when(mockUnitDataModel.getUnitsContentMainRetriever()).thenReturn(mockUnitsContentMainRetriever);

        when(mockUnitManager.getFundamentalUnitsDataModel()).thenReturn(mockFundamentalUnitsDataModel);

        ///
        UnitsContentModifier unitsContentModifier = new UnitsContentModifier();
        unitsContentModifier.setUnitsDataModel(mockUnitDataModel);
        unitsContentModifier.addUnit(mockUnit);

        ///

    }
}