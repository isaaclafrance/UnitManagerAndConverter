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
import com.isaacapps.unitconverterapp.models.measurables.unit.UnitException;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsContentDeterminer;
import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.text.StartCaseFormatter;
import com.isaacapps.unitconverterapp.processors.parsers.ParsingException;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.componentunits.ComponentUnitsDimensionParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.isaacapps.unitconverterapp.activities.MainActivity.SOURCE_NAME;
import static com.isaacapps.unitconverterapp.activities.MainActivity.TARGET_NAME;

public class UnitBrowserActivity extends Activity {
    private static final int ALL = 0, OPPOSITE = 1, DYNAMIC = 2, CURRENCY = 3, METRIC = 4, SPECIAL = 5;

    private static final String PREFIX_DELIMITER = " :: ";
    private static final String UNIT_ABBREVIATION_DELIMITER = " - ";
    private static final String PREFIX_IDENTITY_SYMBOL = "- - - - - - -";
    private static final String ANY_UNIT_SYSTEM = "*Any Unit System"; //Shows up first in unit system spinner due to underscore prefix.

    private PersistentSharablesApplication pSharablesApplication;
    private ComponentUnitsDimensionParser componentUnitsDimensionParser;
    private IFormatter spinnerSelectionFormatter;
    private Locale locale;

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

        locale = pSharablesApplication.getResources().getConfiguration().locale;
        try {
            componentUnitsDimensionParser = new ComponentUnitsDimensionParser(locale);
        } catch (ParsingException e) {
            e.printStackTrace();
        }
        spinnerSelectionFormatter = new StartCaseFormatter(locale);

        //Set unit category and the unit type of the opposite unit based on the current unit for which activity was invoked.
        Bundle extras = getIntent().getExtras();
        callerButtonUnitType = extras != null ? extras.getString("buttonName") : "";
        oppositeUnitType = extras != null ? (callerButtonUnitType.equalsIgnoreCase(TARGET_NAME) ? SOURCE_NAME : TARGET_NAME) : "";

        //
        setTitle(String.format("Select %s Unit", callerButtonUnitType.toUpperCase()));
        setupUIComponents();

        //
        addOnClickListenerForUnitBrowserButtons();
        addOnItemSelectionListenerForSpinners();

        //
        quickDimensionFilters = new HashMap<>();
        quickDimensionFilters.put(ALL,"[ All ] Units");
        quickDimensionFilters.put(OPPOSITE, "[ " + oppositeUnitType + " ] " + "Units");
        quickDimensionFilters.put(DYNAMIC, "[ Dynamic ] Units");
        quickDimensionFilters.put(CURRENCY, "[ Currency ] Units");
        quickDimensionFilters.put(METRIC, "[ Metric ] Units");
        quickDimensionFilters.put(SPECIAL, "[ Special ] Units");

        //
        populateQuickDimensionFilterSpinner();
        quickDimFilterSpinner.startAnimation(AnimationUtils
                .loadAnimation(UnitBrowserActivity.this, android.R.anim.slide_in_left));

        if (oppositeUnitType.equalsIgnoreCase(SOURCE_NAME) &&
                UnitsContentDeterminer.determineGeneralDataModelCategory(pSharablesApplication
                        .getSourceQuantity().getLargestUnit()) != UnitsContentDeterminer.DATA_MODEL_CATEGORY.UNKNOWN)
        {
            quickDimFilterSpinner.setSelection(OPPOSITE);
        }
        else{
            quickDimFilterSpinner.setSelection(ALL);
        }

        quickDimFilterSpinner.setSelected(true);
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
                pSharablesApplication.getUnitManager().getUnitsDataModel().getUnitsContentModifier().removeAllDynamicUnits();
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
        if(selectedUnitName.toLowerCase().matches("no.*unit.*"))
            return;

        Unit unitToBePotentiallyDeleted = pSharablesApplication.getUnitManager()
                .getUnitsDataModel().getUnitsContentMainRetriever().getUnit(selectedUnitName);
		
		/*Only delete units that meet the following criteria:
		  1.Not currently being used as a 'from' or 'to' unit.
		  2.Not a core unit (ie. meter, second, currency, etc)
	    */
        if (!unitToBePotentiallyDeleted.isCoreUnit()
                && !pSharablesApplication.getSourceQuantity().getUnits().contains(unitToBePotentiallyDeleted)
                && !pSharablesApplication.getTargetQuantity().getUnits().contains(unitToBePotentiallyDeleted))
        {
            deleteUnitButton.setVisibility(View.VISIBLE);
            deleteUnitButton.setAnimation(AnimationUtils.loadAnimation(UnitBrowserActivity.this, android.R.anim.fade_in));
        } else {
            deleteUnitButton.setVisibility(View.GONE);
            deleteUnitButton.setAnimation(AnimationUtils.loadAnimation(UnitBrowserActivity.this, android.R.anim.slide_out_right));
        }
    }

    private void showSelectButtonIfSelectedUnitSelectable(String selectedUnitName) {
        //Only show select button that meet the following criteria: Not a COMPLETELY unknown
        if (selectedUnitName != null && !selectedUnitName.contains("No Compatible Units")) {
            selectUnitButton.setVisibility(View.VISIBLE);
            selectUnitButton.setAnimation(AnimationUtils.loadAnimation(UnitBrowserActivity.this, android.R.anim.slide_in_left));
        } else {
            selectUnitButton.setVisibility(View.GONE);
            selectUnitButton.setAnimation(AnimationUtils.loadAnimation(UnitBrowserActivity.this, android.R.anim.fade_out));
        }
    }

    ///Button Listeners Methods
    private void addOnClickListenerForUnitBrowserButtons() {
        selectUnitButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String prefix = ((String) prefixSpinner.getSelectedItem())
                            .replace(PREFIX_IDENTITY_SYMBOL, "").split(PREFIX_DELIMITER)[0].trim();
                    String mainUnitName = ((String) unitSpinner.getSelectedItem()).split(UNIT_ABBREVIATION_DELIMITER)[0].trim();

                    Unit selectedUnit = pSharablesApplication.getUnitManager()
                            .getUnitsDataModel().getUnitsContentMainRetriever().getUnit(prefix + mainUnitName);

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
                try {
                    pSharablesApplication.getUnitManager().getUnitsDataModel().getUnitsContentModifier().removeUnit(mainUnitName);
                } catch (UnitException e) { }

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
    private void addOnItemSelectionListenerForSpinners() {
        //Populate spinner cascade. Spinners dynamically update content based on selection of spinner higher in hierarchy and on the restrictions of general filter spinner.

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
                String unitName = ((String) parent.getItemAtPosition(position)).split(UNIT_ABBREVIATION_DELIMITER)[0];
                showDeleteButtonIfSelectedUnitDeleteable(unitName);
                showSelectButtonIfSelectedUnitSelectable(unitName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    private void populateQuickDimensionFilterSpinner() {
        ArrayList<String> filters = new ArrayList(quickDimensionFilters.values());
        Collections.sort(filters);

        quickDimFilterSpinner.setAdapter( new ArrayAdapter<String>(this, R.layout.unit_browse_spinner_item, filters));
    }
    private void populateUnitSystemSpinner() {
        Set<String> unitSystems = getUnitSystemsBasedOnQuickDimFilterSelection();

        if (unitSystems.isEmpty())
            unitSystems.add("No Compatible Unit Systems");
        else if(unitSystems.size() > 1){
            //Any compatible unit system
            unitSystems.add(ANY_UNIT_SYSTEM);
        }

        unitSystemSpinner.setAdapter(new ArrayAdapter<>(this, R.layout.unit_browse_spinner_item, new ArrayList<>(unitSystems)));
    }
    private void populateCategorySpinner() {
        Set<String> unitCategories = new TreeSet<>(); //Naturally ordered and no duplicates
        String dimSpinnerSelection = (String) quickDimFilterSpinner.getSelectedItem();
        String unitSystemSpinnerSelection = (String) unitSystemSpinner.getSelectedItem();

        if (dimSpinnerSelection.equals(quickDimensionFilters.get(DYNAMIC))) {
            for (Unit dynamicUnit : pSharablesApplication.getUnitManager().getUnitsDataModel().getUnitsContentMainRetriever().getDynamicUnits())
                unitCategories.add(spinnerSelectionFormatter.format(dynamicUnit.getCategory()));
        } else {
            String dimFilterUnitCategory = getSingularUnitCategoryBasedOnQuickDimFilterSelection();

            if (!dimFilterUnitCategory.isEmpty()) {
                unitCategories.add(spinnerSelectionFormatter.format(dimFilterUnitCategory));
            }
            else if(unitSystemSpinnerSelection.equalsIgnoreCase(ANY_UNIT_SYSTEM)) {
                for(String unitCategory:pSharablesApplication.getUnitManager().getUnitsClassifierDataModel().getAllUnitCategories())
                    unitCategories.add(spinnerSelectionFormatter.format(unitCategory));

                unitCategories.remove(Unit.UNKNOWN_UNIT_CATEGORY);
            }
            else
            {
                for(String unitCategory:pSharablesApplication.getUnitManager().getUnitsClassifierDataModel().getUnitCategoriesInUnitSystem(unitSystemSpinnerSelection))
                    unitCategories.add(spinnerSelectionFormatter.format(unitCategory));
            }
        }

        if(unitCategories.isEmpty())
            unitCategories.add("No Compatible Unit Categories.");

        categorySpinner.setAdapter(new ArrayAdapter<>(this
                , R.layout.unit_browse_spinner_item
                , new ArrayList<>(unitCategories)));
    }
    private void populateUnitSpinner() {
        Set<String> formattedFullNameNAbbreviations = new TreeSet<>(); //Sort by default alphabetical

        String quickDimFilterSelection = (String) quickDimFilterSpinner.getSelectedItem();
        String unitSystemSelection = (String) unitSystemSpinner.getSelectedItem();
        String categorySelection = (String) categorySpinner.getSelectedItem();

        if ((pSharablesApplication.getTargetQuantity().getLargestUnit().getName()
                .equalsIgnoreCase(Unit.UNKNOWN_UNIT_NAME) && oppositeUnitType.equalsIgnoreCase(TARGET_NAME)
                || pSharablesApplication.getSourceQuantity().getLargestUnit().getName().equalsIgnoreCase(Unit.UNKNOWN_UNIT_NAME)
                && oppositeUnitType.equalsIgnoreCase(SOURCE_NAME))
                && quickDimFilterSelection.equals(quickDimensionFilters.get(OPPOSITE)))
        {
            formattedFullNameNAbbreviations.add("No Units Have A Dimension Compatible to the " + oppositeUnitType.toUpperCase() + " Unit.");

        } else if (quickDimFilterSelection.equals(quickDimensionFilters.get(DYNAMIC) )) {

            for(Unit dynamicUnit:pSharablesApplication.getUnitManager().getUnitsDataModel().getUnitsContentMainRetriever().getDynamicUnits())
                formattedFullNameNAbbreviations.add(spinnerSelectionFormatter.format(dynamicUnit.getName()));

            if(formattedFullNameNAbbreviations.isEmpty())
                formattedFullNameNAbbreviations.add("No Dynamic Units");

        } else {
            List<String> allRelevantUnitNames = new ArrayList<>();

            if(unitSystemSelection.equalsIgnoreCase(ANY_UNIT_SYSTEM)){
                for(Collection<String> unitNameGroup:pSharablesApplication.getUnitManager().getUnitsClassifierDataModel().getUnitGroupsWithSameCategory(categorySelection)){
                    allRelevantUnitNames.addAll(unitNameGroup);
                }
            }
            else{
                allRelevantUnitNames.addAll(pSharablesApplication.getUnitManager().getUnitsClassifierDataModel()
                        .getUnitNamesByUnitSystemNCategory(unitSystemSelection, categorySelection));
            }

            for (String unitName : allRelevantUnitNames) {
                Unit unit = pSharablesApplication.getUnitManager().getUnitsDataModel().getUnitsContentMainRetriever().getUnit(unitName);

                String abbreviation = unit.getAbbreviation();
                String fullNameNAbbreviation = (!unitName.equalsIgnoreCase(abbreviation)) ? unitName + UNIT_ABBREVIATION_DELIMITER + unit.getAbbreviation() : unitName;
                formattedFullNameNAbbreviations.add(spinnerSelectionFormatter.format(fullNameNAbbreviation));
            }
			
			/*If there are no units at this point, then the fault would not be that of the Quick Dimension Filter and with the selected unit system and category pairing
			  since the category and unit system spinners are populated based on available units. */

            if (formattedFullNameNAbbreviations.isEmpty())
                formattedFullNameNAbbreviations.add("No units available due to current Quick Dimension Filter selection");
        }

        unitSpinner.setAdapter(new ArrayAdapter<>(this, R.layout.unit_browse_spinner_item
                , new ArrayList<>(formattedFullNameNAbbreviations)));
    }
    private void populatePrefixSpinner() {
        List<String> prefixesFullNamesWithValues = new ArrayList<>();
        prefixesFullNamesWithValues.add(PREFIX_IDENTITY_SYMBOL + PREFIX_DELIMITER + "1.0"); //When no selected prefix is applied, the identity factor is used.

        /*Only display prefixes if the unit name itself does not contain a prefix.
         *Make sure that unit name does not contain any complex term combinations.
         *Also make sure it is not a currency.
         */
        String unitCategorySelection = (String) categorySpinner.getSelectedItem();
        String unitSelection = ((String) unitSpinner.getSelectedItem()).split(UNIT_ABBREVIATION_DELIMITER)[0].trim();

        if (!unitSelection.contains("no units")
                && !pSharablesApplication.getUnitManager().getPrefixesDataModel().hasPrefix(unitSelection, true)
                && !componentUnitsDimensionParser.getDimensionParserBuilder().getDimensionComponentDefiner().hasComplexDimensions(unitSelection)
                && !(unitCategorySelection).equalsIgnoreCase(MainActivity.CURRENCY_CATEGORY))
        {

            for (String prefixFullName : pSharablesApplication.getUnitManager().getPrefixesDataModel().getAllPrefixFullNames())
            {
                prefixesFullNamesWithValues.add(prefixFullName
                        + PREFIX_DELIMITER
                        + pSharablesApplication.getUnitManager().getPrefixesDataModel().getPrefixValue(prefixFullName));
            }

            //Sort prefix list from least to greatest factor
            Collections.sort(prefixesFullNamesWithValues, new Comparator<String>() {
                @Override
                public int compare(String lhsPrefixFullNameWithValue, String rhsPrefixFullNameWithValue) {
                    return Double.compare(pSharablesApplication.getUnitManager().getPrefixesDataModel()
                                    .getPrefixValue(lhsPrefixFullNameWithValue.split(PREFIX_DELIMITER)[0])
                            , pSharablesApplication.getUnitManager().getPrefixesDataModel()
                                    .getPrefixValue(rhsPrefixFullNameWithValue.split(PREFIX_DELIMITER)[0]));
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
            for(String unitSystem:pSharablesApplication.getUnitManager().getUnitsClassifierDataModel().getAllUnitSystems())
                unitSystems.add(spinnerSelectionFormatter.format(unitSystem));

            unitSystems.remove(spinnerSelectionFormatter.format(Unit.UNKNOWN_UNIT_SYSTEM));

        } else if (dimFilterSelection.equals(quickDimensionFilters.get(DYNAMIC))) {
            for (Unit dynamicUnit : pSharablesApplication.getUnitManager()
                    .getUnitsDataModel().getUnitsContentMainRetriever().getDynamicUnits())
            {
                unitSystems.add(spinnerSelectionFormatter.format(dynamicUnit.getUnitSystem()));
            }
        } else {
            //Only add unit system that contains a relevant token or is associated with a relevant category.
            String relevantUnitSystemToken = getSingularUnitSystemTokenBasedOnQuickDimFilterSelection();
            String relevantUnitCategory = getSingularUnitCategoryBasedOnQuickDimFilterSelection();

            for (String unitSystem : pSharablesApplication.getUnitManager().getUnitsClassifierDataModel().getAllUnitSystems()) {
                boolean unitSystemIsRelevantBasedOnUnitSystemToken = !relevantUnitSystemToken.isEmpty() && unitSystem.contains(relevantUnitSystemToken);
                boolean unitSystemIsRelevantBasedOnCategory = !unitSystem.equalsIgnoreCase(Unit.UNKNOWN_UNIT_SYSTEM)
                        && pSharablesApplication.getUnitManager().getUnitsClassifierDataModel().containsUnitCategoryInUnitSystem(unitSystem, relevantUnitCategory, true);

                if (unitSystemIsRelevantBasedOnUnitSystemToken || unitSystemIsRelevantBasedOnCategory)
                    unitSystems.add(spinnerSelectionFormatter.format(unitSystem));
            }
        }

        return unitSystems;
    }
    private String getSingularUnitCategoryBasedOnQuickDimFilterSelection() {
        String dimFilterSelection = (String) quickDimFilterSpinner.getSelectedItem();

        if (dimFilterSelection.equalsIgnoreCase(quickDimensionFilters.get(CURRENCY))) {
            return "currency_unit";
        } else if (dimFilterSelection.equalsIgnoreCase(quickDimensionFilters.get(OPPOSITE))) {
            if (oppositeUnitType.equalsIgnoreCase(TARGET_NAME)) {
                return pSharablesApplication.getTargetQuantity().getLargestUnit().getCategory();
            } else {
                return pSharablesApplication.getSourceQuantity().getLargestUnit().getCategory();
            }
        }
        else{
            return "";
        }
    }
    private String getSingularUnitSystemTokenBasedOnQuickDimFilterSelection() {
        String dimFilterSelection = (String) quickDimFilterSpinner.getSelectedItem();

        if (dimFilterSelection.equalsIgnoreCase(quickDimensionFilters.get(METRIC))) {
            return "metric";
        } else if (dimFilterSelection.equalsIgnoreCase(quickDimensionFilters.get(SPECIAL))) {
            return "special";
        } else {
            return "";
        }

    }
}
