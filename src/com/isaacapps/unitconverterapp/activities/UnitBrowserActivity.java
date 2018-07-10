package com.isaacapps.unitconverterapp.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.isaacapps.unitconverterapp.models.measurables.quantity.QuantityException;
import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.ContentDeterminer;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.componentunits.ComponentUnitsDimensionParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.isaacapps.unitconverterapp.activities.MainActivity.SOURCE_NAME;
import static com.isaacapps.unitconverterapp.activities.MainActivity.TARGET_NAME;

public class UnitBrowserActivity extends Activity {
    private static final int ALL = 0, OPPOSITE = 1, DYNAMIC = 2, CURRENCY = 3, METRIC = 4, SPECIAL = 5;

    private static final String DELIMITER = " :: ";
    private static final String PREFIX_IDENTITY_SYMBOL = "- - - - - - -";
    private static final String ANY_UNIT_SYSTEM = "_Any Unit System_"; //Shows up first in unit system spinner due to underscore prefix.

    private PersistentSharablesApplication pSharablesApplication;
    private ComponentUnitsDimensionParser componentUnitsDimensionParser;

    private Spinner quickDimFilterSpinner, unitSystemSpinner, categorySpinner;
    private Spinner unitSpinner, prefixSpinner;

    private Button selectUnitButton, cancelUnitButton, deleteUnitButton;

    private String oppositeUnitType, callerButtonUnitType;

    private Map<Integer,String> quickDimensionFilters;

    ///
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unit_browse);

        pSharablesApplication = (PersistentSharablesApplication) this.getApplication();

        componentUnitsDimensionParser = new ComponentUnitsDimensionParser();

        //Set unit category and the unit type of the opposite unit based on the current unit for which activity was invoked.
        Bundle extras = getIntent().getExtras();
        callerButtonUnitType = extras != null ? extras.getString("buttonName") : "";
        oppositeUnitType = extras != null
                ? (callerButtonUnitType.equalsIgnoreCase(TARGET_NAME) ? SOURCE_NAME : TARGET_NAME)
                : "";

        //
        quickDimensionFilters = new HashMap<>();
        quickDimensionFilters.put(ALL,"[ All ] Units");
        quickDimensionFilters.put(OPPOSITE, "[ " + oppositeUnitType + " ] " + "Units");
        quickDimensionFilters.put(DYNAMIC, "[ Dynamic ] Units");
        quickDimensionFilters.put(CURRENCY, "[ Currency ] Units");
        quickDimensionFilters.put(METRIC, "[ Metric ] Units");
        quickDimensionFilters.put(SPECIAL, "[ Special ] Units");

        //
        setTitle(String.format("Select %s Unit", callerButtonUnitType.toUpperCase()));
        setupUIComponents();

        //
        populateSpinners();

        //
        addListenerOnUnitBrowserButtons();
        addListenerOnSelectionSpinners();

        //
        if (oppositeUnitType.equalsIgnoreCase(SOURCE_NAME) &&
                ContentDeterminer.determineGeneralDataModelCategory(pSharablesApplication
                        .getSourceQuantity().getLargestUnit()) != ContentDeterminer.DATA_MODEL_CATEGORY.UNKNOWN)
        {
            quickDimFilterSpinner.setSelection(1);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.unitbrowser, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_all_dynamic_units:
                pSharablesApplication.getUnitManager().getUnitsDataModel().getContentModifier().removeAllDynamicUnits();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    ///Setup UI Components Methods
    private void setupUIComponents() {
        quickDimFilterSpinner = (Spinner) findViewById(R.id.dimFilterSpinner);
        unitSystemSpinner = (Spinner) findViewById(R.id.unitSystemSpinner);
        categorySpinner = (Spinner) findViewById(R.id.categorySpinner);
        unitSpinner = (Spinner) findViewById(R.id.unitSpinner);
        prefixSpinner = (Spinner) findViewById(R.id.prefixSpinner);

        //
        selectUnitButton = (Button) findViewById(R.id.selectUnitButton);
        cancelUnitButton = (Button) findViewById(R.id.cancelUnitButton);
        deleteUnitButton = (Button) findViewById(R.id.deleteUnitButton);
        deleteUnitButton.setVisibility(View.GONE);
    }

    private void showDeleteButtonIfSelectedUnitDeleteable(String selectedUnitName) {
        Unit unitToBePotentiallyDeleted = pSharablesApplication.getUnitManager()
                .getUnitsDataModel().getContentMainRetriever().getUnit(selectedUnitName);
		
		/*Only delete units that meet the following criteria:
		  1.Not currently being used as a 'from' or 'to' unit.
		  2.Not a core unit (ie. meter, second, currency, etc)
	    */
        if (!unitToBePotentiallyDeleted.isCoreUnit()
                && !pSharablesApplication.getSourceQuantity().getUnits().contains(unitToBePotentiallyDeleted)
                && !pSharablesApplication.getTargetQuantity().getUnits().contains(unitToBePotentiallyDeleted)) {

            deleteUnitButton.setVisibility(View.VISIBLE);
        } else {
            deleteUnitButton.setVisibility(View.GONE);
        }
    }

    private void showSelectButtonIfSelectedUnitSelectable(String selectedUnitName) {
        //Only show select button that meet the following criteria: Not a COMPLETELY unknown
        if (selectedUnitName != null && !selectedUnitName.equalsIgnoreCase("No Compatible Units")) {

            selectUnitButton.setVisibility(View.VISIBLE);
        } else {
            selectUnitButton.setVisibility(View.GONE);
        }
    }

    ///Button Listeners Methods
    private void addListenerOnUnitBrowserButtons() {
        selectUnitButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String prefix = ((String) prefixSpinner.getSelectedItem())
                            .replace(PREFIX_IDENTITY_SYMBOL, "").split(DELIMITER)[0].trim();
                    String mainUnitName = ((String) unitSpinner.getSelectedItem()).split("-")[0].trim();

                    Unit selectedUnit = pSharablesApplication.getUnitManager()
                            .getUnitsDataModel().getContentMainRetriever().getUnit(prefix + mainUnitName);

                    if (callerButtonUnitType.equalsIgnoreCase(SOURCE_NAME)) {
                        pSharablesApplication.getSourceQuantity()
                                .setUnits(Collections.singletonList(selectedUnit));
                    } else if (callerButtonUnitType.equalsIgnoreCase(TARGET_NAME)) {
                        pSharablesApplication.getTargetQuantity()
                                .setUnits(Collections.singletonList(selectedUnit));
                    }
                }catch (QuantityException e) {}
                finally {
                    finish();
                }
            }
        });

        cancelUnitButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        deleteUnitButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String mainUnitName = (String) unitSpinner.getSelectedItem();
                pSharablesApplication.getUnitManager().getUnitsDataModel().getContentModifier().removeUnit(mainUnitName);

                //
                unitSpinner.startAnimation(AnimationUtils
                        .loadAnimation(UnitBrowserActivity.this, android.R.anim.slide_out_right));
                populateUnitSpinner();
                unitSpinner.setSelection(0);
                unitSpinner.setSelected(true);
            }
        });
    }

    ///Selection Spinner Listener Methods
    private void addListenerOnSelectionSpinners() {
        quickDimFilterSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                populateUnitSystemSpinner();
                unitSystemSpinner.setSelection(0);
                unitSystemSpinner.setSelected(true);

                ///
                unitSystemSpinner.startAnimation(AnimationUtils
                        .loadAnimation(UnitBrowserActivity.this, android.R.anim.slide_in_left));
                categorySpinner.startAnimation(AnimationUtils
                        .loadAnimation(UnitBrowserActivity.this, android.R.anim.slide_in_left));
                unitSpinner.startAnimation(AnimationUtils
                        .loadAnimation(UnitBrowserActivity.this, android.R.anim.slide_in_left));
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        unitSystemSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                populateCategorySpinner();
                categorySpinner.setSelection(0);
                categorySpinner.setSelected(true);

                ///
                categorySpinner.startAnimation(AnimationUtils
                        .loadAnimation(UnitBrowserActivity.this, android.R.anim.slide_in_left));
                unitSpinner.startAnimation(AnimationUtils
                        .loadAnimation(UnitBrowserActivity.this, android.R.anim.slide_in_left));
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        categorySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                populateUnitSpinner();
                unitSpinner.setSelection(0);
                unitSpinner.setSelected(true);

                ///
                unitSpinner.startAnimation(AnimationUtils.loadAnimation(UnitBrowserActivity.this, android.R.anim.slide_in_left));
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        unitSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                populatePrefixSpinner();
                prefixSpinner.setSelection(0);
                prefixSpinner.setSelected(true);

                ///
                prefixSpinner.startAnimation(AnimationUtils
                        .loadAnimation(UnitBrowserActivity.this, android.R.anim.slide_in_left));

                ///
                showDeleteButtonIfSelectedUnitDeleteable((String) parent.getItemAtPosition(position));
                showSelectButtonIfSelectedUnitSelectable((String) parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    ///Populate Spinners. Spinners dynamically update selection of spinner higher in hierarchy and restrictions of general filter spinner.
    private void populateSpinners() {
        populateQuickDimensionFilterSpinner();
        quickDimFilterSpinner.startAnimation(AnimationUtils
                .loadAnimation(UnitBrowserActivity.this, android.R.anim.slide_in_left));
        quickDimFilterSpinner.setSelection(0);
        quickDimFilterSpinner.setSelected(true);

        populateUnitSystemSpinner();
        unitSystemSpinner.setSelection(0);
        unitSystemSpinner.setSelected(true);

        populateCategorySpinner();
        categorySpinner.setSelection(0);
        categorySpinner.setSelected(true);

        populateUnitSpinner();
        unitSpinner.setSelection(0);
        unitSpinner.setSelected(true);

        populatePrefixSpinner();
        prefixSpinner.startAnimation(AnimationUtils.loadAnimation(UnitBrowserActivity.this
                , android.R.anim.slide_in_left));
        prefixSpinner.setSelection(0);
        prefixSpinner.setSelected(true);
    }

    private void populateQuickDimensionFilterSpinner() {
        quickDimFilterSpinner.setAdapter(new ArrayAdapter<>(this
                , R.layout.unit_browse_spinner_item, quickDimensionFilters.values().toArray(new String[quickDimensionFilters.size()])));
    }

    private void populateUnitSystemSpinner() {
        Set<String> unitSystems = getUnitSystemsBasedOnQuickDimFilterSelection();

        if (unitSystems.isEmpty())
            unitSystems.add("No Compatible Unit Systems");
        else {
            //Only there are compatible unit systems should one be able to search by any unit system
            unitSystems.add(ANY_UNIT_SYSTEM);
        }

        unitSystemSpinner.setAdapter(new ArrayAdapter<>(this, R.layout.unit_browse_spinner_item, new ArrayList<>(unitSystems)));
    }

    private void populateCategorySpinner() {
        Set<String> unitCategories = new TreeSet<>(); //Naturally ordered and no duplicates
        String dimSpinnerSelection = (String) quickDimFilterSpinner.getSelectedItem();
        String unitSystemSpinnerSelection = (String) unitSystemSpinner.getSelectedItem();

        if (dimSpinnerSelection.equals(quickDimensionFilters.get(DYNAMIC))) {
            for (Unit dynamicUnit : pSharablesApplication.getUnitManager().getUnitsDataModel().getContentMainRetriever().getDynamicUnits())
                unitCategories.add(dynamicUnit.getCategory());
        } else {
            String dimFilterUnitCategory = getSingularUnitCategoryBasedOnQuickDimFilterSelection();

            if (!dimFilterUnitCategory.isEmpty()) {
                unitCategories.add(dimFilterUnitCategory);
            }
            else if(unitSystemSpinnerSelection.equalsIgnoreCase(ANY_UNIT_SYSTEM)) {
                unitCategories.addAll(pSharablesApplication.getUnitManager()
                        .getUnitsClassifierDataModel().getAllUnitCategories());
            }
            else
            {
                unitCategories.addAll(pSharablesApplication.getUnitManager()
                        .getUnitsClassifierDataModel()
                        .getUnitCategoriesInUnitSystem(unitSystemSpinnerSelection));
            }
        }

        categorySpinner.setAdapter(new ArrayAdapter<>(this
                , R.layout.unit_browse_spinner_item
                , new ArrayList<>(unitCategories)));
    }

    private void populateUnitSpinner() {
        Set<String> unitsNames = new TreeSet<>(); //Sort by default alphabetical

        String quickDimFilterSelection = (String) quickDimFilterSpinner.getSelectedItem(),
                unitSystemSelection = (String) unitSystemSpinner.getSelectedItem(),
                categorySelection = (String) categorySpinner.getSelectedItem();

        if ((pSharablesApplication.getTargetQuantity().getLargestUnit().getName()
                .equalsIgnoreCase(Unit.UNKNOWN_UNIT_NAME) && oppositeUnitType.equalsIgnoreCase(TARGET_NAME)
                || pSharablesApplication.getSourceQuantity().getLargestUnit().getName().equalsIgnoreCase(Unit.UNKNOWN_UNIT_NAME)
                && oppositeUnitType.equalsIgnoreCase(SOURCE_NAME))
                && quickDimFilterSelection.equals(quickDimensionFilters.get(OPPOSITE)))
        {
            unitsNames.add("No Units Have A Dimension Compatible to the " + oppositeUnitType.toUpperCase() + " Unit.");

        } else if (pSharablesApplication.getUnitManager().getUnitsDataModel().getContentMainRetriever().getDynamicUnits().isEmpty()
                && quickDimFilterSelection.equals(quickDimensionFilters.get(DYNAMIC)))
        {

            unitsNames.add("No Dynamic Units");

        } else {
            //Include abbreviation with unit full name
            String abbreviation;
            Unit unit;

            for (String unitName : pSharablesApplication.getUnitManager().getUnitsClassifierDataModel()
                    .getUnitNamesByUnitSystemNCategory(unitSystemSelection, categorySelection))
            {

                unit = pSharablesApplication.getUnitManager().getUnitsDataModel().getContentMainRetriever().getUnit(unitName);

                if (!quickDimFilterSelection.equals(quickDimensionFilters.get(DYNAMIC)) || !unit.isCoreUnit())
                {
                    abbreviation = unit.getAbbreviation();
                    unitsNames.add((!unitName.equalsIgnoreCase(abbreviation)) ? unitName + " - " + unit.getAbbreviation() : unitName);
                }
            }
			
			/*If there are no units, then the fault would not be with the unit system and category pairing 
			  since those spinner were specifically populated to prevent that from occuring. */

            if (unitsNames.isEmpty())
                unitsNames.add("Sorry, no units available due to current '" + quickDimFilterSelection + "' Quick Dimension Filter selection");
        }

        unitSpinner.setAdapter(new ArrayAdapter<>(this, R.layout.unit_browse_spinner_item
                , new ArrayList<>(unitsNames)));
    }

    private void populatePrefixSpinner() {
        List<String> prefixesFullNamesWithValues = new ArrayList<>();
        prefixesFullNamesWithValues.add(PREFIX_IDENTITY_SYMBOL + DELIMITER + "1.0"); //When no selected prefix is applied, the identity factor is used.

        /*Only display prefixes if the unit name itself does not contain a prefix.
         *Make sure that unit name does not contain any complex term combinations.
         *Also make sure it is not a currency.
         */
        String unitCategorySelection = (String) categorySpinner.getSelectedItem(), unitSelection = (String) unitSpinner.getSelectedItem();

        if (!(unitSelection).equalsIgnoreCase("No Compatible Units")
                && pSharablesApplication.getUnitManager().getPrefixesDataModel()
                .getPrefixMatches(unitSelection, true).isEmpty()
                && !componentUnitsDimensionParser.getDimensionParserBuilder().getDimensionComponentDefinerBuilder().hasComplexDimensions(unitSelection)
                && !(unitCategorySelection).equalsIgnoreCase("currency_unit"))
        {

            for (String prefixFullName : pSharablesApplication.getUnitManager()
                    .getPrefixesDataModel().getAllPrefixFullNames()) {
                prefixesFullNamesWithValues.add(prefixFullName
                        + DELIMITER
                        + pSharablesApplication.getUnitManager().getPrefixesDataModel()
                        .getPrefixValue(prefixFullName));
            }

            //Sort prefix list from least to greatest factor
            Collections.sort(prefixesFullNamesWithValues, new Comparator<String>() {
                @Override
                public int compare(String lhsPrefixFullNameWithValue, String rhsPrefixFullNameWithValue) {
                    return Double.compare(pSharablesApplication.getUnitManager().getPrefixesDataModel()
                                    .getPrefixValue(lhsPrefixFullNameWithValue.split(DELIMITER)[0])
                            , pSharablesApplication.getUnitManager().getPrefixesDataModel()
                                    .getPrefixValue(rhsPrefixFullNameWithValue.split(DELIMITER)[0]));
                }
            });
        }

        prefixSpinner.setAdapter(new ArrayAdapter<>(this, R.layout.unit_browse_spinner_item, prefixesFullNamesWithValues));
    }

    ///
    private Set<String> getUnitSystemsBasedOnQuickDimFilterSelection() {
        Set<String> unitSystems = new TreeSet<>(); //Naturally ordered and no duplicates
        String dimFilterSelection = (String) quickDimFilterSpinner.getSelectedItem();

        if (dimFilterSelection.equals(quickDimensionFilters.get(ALL)))
        {
            unitSystems.addAll(pSharablesApplication.getUnitManager()
                    .getUnitsClassifierDataModel().getAllUnitSystems());

            unitSystems.remove(pSharablesApplication.getUnitManager()
                    .getUnitsDataModel().getContentMainRetriever().getUnknownUnit().getUnitSystem());

        } else if (dimFilterSelection.equals(quickDimensionFilters.get(DYNAMIC))) {
            for (Unit dynamicUnit : pSharablesApplication.getUnitManager()
                    .getUnitsDataModel().getContentMainRetriever().getDynamicUnits()) {
                unitSystems.add(dynamicUnit.getUnitSystem());
            }
        } else {
            String relevantUnitCategory = getSingularUnitCategoryBasedOnQuickDimFilterSelection();

            //Only add unit system relevant to unit category.
            for (String unitSystem : pSharablesApplication.getUnitManager().getUnitsClassifierDataModel().getAllUnitSystems()) {

                boolean unitSystemIsRelevant = !dimFilterSelection.equalsIgnoreCase(quickDimensionFilters.get(OPPOSITE))
                        && pSharablesApplication.getUnitManager().getUnitsClassifierDataModel()
                        .containsUnitCategoryInUnitSystem(unitSystem, relevantUnitCategory, true);

                if (unitSystemIsRelevant)
                    unitSystems.add(unitSystem);
            }
        }

        return unitSystems;
    }

    private String getSingularUnitCategoryBasedOnQuickDimFilterSelection() {
        String dimFilterSelection = (String) quickDimFilterSpinner.getSelectedItem();
        String unitCategoryName = "";

        if (dimFilterSelection.equalsIgnoreCase(quickDimensionFilters.get(CURRENCY))) {
            unitCategoryName = "currency_unit";
        } else if (dimFilterSelection.equalsIgnoreCase(quickDimensionFilters.get(METRIC))) {
            unitCategoryName = "metric";
        } else if (dimFilterSelection.equalsIgnoreCase(quickDimensionFilters.get(SPECIAL))) {
            unitCategoryName = "special";
        } else if (dimFilterSelection.equalsIgnoreCase(quickDimensionFilters.get(OPPOSITE))) {
            if (oppositeUnitType.equalsIgnoreCase(TARGET_NAME)) {
                unitCategoryName = pSharablesApplication.getTargetQuantity().getLargestUnit().getCategory();
            } else if (oppositeUnitType.equalsIgnoreCase(SOURCE_NAME)) {
                unitCategoryName = pSharablesApplication.getSourceQuantity().getLargestUnit().getCategory();
            }
        }

        return unitCategoryName;
    }
}
