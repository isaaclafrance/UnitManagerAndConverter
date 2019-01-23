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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static com.isaacapps.unitconverterapp.activities.MainActivity.SOURCE_NAME;
import static com.isaacapps.unitconverterapp.activities.MainActivity.TARGET_NAME;

public class UnitBrowserActivity extends Activity {
    //Unfortunately android does not recommend enums. The number determines the order in the spinner dropdown.
    private static final int ALL = 0, OPPOSITE = 1, CURRENCY = 2, LENGTH = 3, VOLUME = 4, AREA = 5, DYNAMIC = 6,  METRIC = 7, SPECIAL = 8, SI = 9, CGS = 10, US = 11;

    private static final String PREFIX_DELIMITER = " :: ";
    private static final String UNIT_ABBREVIATION_DELIMITER = " - ";
    private static final String PREFIX_IDENTITY_SYMBOL = "- - - - - - -";
    private static final String ANY_UNIT_SYSTEM = "*Any Unit System";

    private PersistentSharablesApplication pSharablesApplication;
    private ComponentUnitsDimensionParser componentUnitsDimensionParser;
    private IFormatter spinnerSelectionFormatter;
    private Locale locale;

    private Spinner quickDimFilterSpinner, unitSystemSpinner, categorySpinner;
    private Spinner unitSpinner, prefixSpinner;

    private Button selectUnitButton, cancelUnitButton, deleteUnitButton;

    private String oppositeUnitType, callerButtonUnitType;

    private Map<Integer,String> quickDimensionFilters;

    private ArrayList<String> unitSystemFilteringTokens;
    private ArrayList<String> unitCategoryFilteringTokens;

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
        populateUnitSystemFilteringTokens();
        populateUnitCategoryFilteringTokens();
        populateQuickDimFilters();
    }
    private void populateUnitSystemFilteringTokens(){
        unitSystemFilteringTokens = new ArrayList<>();
        unitSystemFilteringTokens.add(Unit.UNKNOWN_UNIT_SYSTEM);
        unitSystemFilteringTokens.add("dimless");
        unitSystemFilteringTokens.add("const");
        unitSystemFilteringTokens.add("clinical");
        unitSystemFilteringTokens.add("chemical");
    }
    private void populateUnitCategoryFilteringTokens(){
        unitCategoryFilteringTokens = new ArrayList<>();
        unitCategoryFilteringTokens.add(Unit.UNKNOWN_UNIT_CATEGORY);
    }
    private void populateQuickDimFilters(){
        quickDimensionFilters = new TreeMap<>();
        quickDimensionFilters.put(ALL,"[ All ] Units");
        quickDimensionFilters.put(OPPOSITE, "[ " + oppositeUnitType + " ] " + "Units");
        quickDimensionFilters.put(DYNAMIC, "[ Dynamic ] Units");
        quickDimensionFilters.put(CURRENCY, "[ Currency ] Units");
        quickDimensionFilters.put(LENGTH, "[ Length ] Units");
        quickDimensionFilters.put(VOLUME, "[ Volume ] Units");
        quickDimensionFilters.put(AREA, "[ Area ] Units");
        quickDimensionFilters.put(METRIC, "[ Metric ] Units");
        quickDimensionFilters.put(SI, "[ SI ] Units");
        quickDimensionFilters.put(CGS, "[ CGS ] Units");
        quickDimensionFilters.put(US, "[ US ] Units");
        //quickDimensionFilters.put(SPECIAL, "[ Special ] Units");

        //
        ArrayList<String> filters = new ArrayList(quickDimensionFilters.values());
        Collections.sort(filters);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.unit_browse_spinner_item, filters);
        quickDimFilterSpinner.setAdapter(arrayAdapter);

        //
        quickDimFilterSpinner.startAnimation(AnimationUtils
                .loadAnimation(UnitBrowserActivity.this, android.R.anim.slide_in_left));

        if (oppositeUnitType.equalsIgnoreCase(SOURCE_NAME) &&
                UnitsContentDeterminer.determineHighestPriorityDataModelCategory(pSharablesApplication
                        .getSourceQuantity().getLargestUnit()) != UnitsContentDeterminer.DATA_MODEL_CATEGORY.UNKNOWN)
        {
            quickDimFilterSpinner.setSelection(arrayAdapter.getPosition(quickDimensionFilters.get(OPPOSITE)));
        }
        else{
            quickDimFilterSpinner.setSelection(arrayAdapter.getPosition(quickDimensionFilters.get(ALL)));
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

                populateUnitSystemSpinner();
                unitSystemSpinner.setSelection(0);
                unitSystemSpinner.setSelected(true);
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
        if(!selectedUnitName.toLowerCase().matches("no.*unit.*"))
            return;

        Unit unitToBePotentiallyDeleted = !selectedUnitName.toLowerCase().matches("no.*unit.*")? pSharablesApplication.getUnitManager()
                .getUnitsDataModel().getUnitsContentMainRetriever().getUnit(selectedUnitName) : null;
		
		/*Only delete units that meet the following criteria:
		  1.Not currently being used as a 'from' or 'to' unit.
		  2.Not a core unit (ie. meter, second, currency, etc)
	    */
        if ( unitToBePotentiallyDeleted != null && !unitToBePotentiallyDeleted.isCoreUnit()
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

    private void populateUnitSystemSpinner() {
        ArrayList<String> formattedUnitSystems = getFormattedUnitSystemsBasedOnQuickDimFilterSelection();

        if (formattedUnitSystems.isEmpty())
            formattedUnitSystems.add("No Compatible Unit Systems");
        else if(formattedUnitSystems.size() > 1){
            //Any compatible unit system
            formattedUnitSystems.add(0, ANY_UNIT_SYSTEM);
        }

        unitSystemSpinner.setAdapter(new ArrayAdapter<>(this, R.layout.unit_browse_spinner_item, formattedUnitSystems));
    }
    private void populateCategorySpinner() {
        Set<String> unformattedFinalUnitCategories = new TreeSet<>(); //Naturally ordered and no duplicates
        String quickDimSpinnerSelection = (String) quickDimFilterSpinner.getSelectedItem();
        String unitSystemSpinnerSelection = (String) unitSystemSpinner.getSelectedItem();

        if (quickDimSpinnerSelection.equals(quickDimensionFilters.get(DYNAMIC))) {
            for(Unit unit:pSharablesApplication.getUnitManager().getUnitsDataModel().getUnitsContentQuerier().queryUnitsByUnitSystem(unitSystemSpinnerSelection)){
                if(UnitsContentDeterminer.determineHighestPriorityDataModelCategory(unit) == UnitsContentDeterminer.DATA_MODEL_CATEGORY.DYNAMIC)
                    unformattedFinalUnitCategories.add(unit.getCategory());
            }
        } else {
            String dimFilterUnitCategoryToken = getSingularUnitCategoryTokenBasedOnQuickDimFilterSelection();

            Collection<String> unformattedInitialUnitCategories;
            if(unitSystemSpinnerSelection.equalsIgnoreCase(ANY_UNIT_SYSTEM)) {
                unformattedInitialUnitCategories = pSharablesApplication.getUnitManager().getUnitsClassifierDataModel().getAllUnitCategories();
            } else {
                unformattedInitialUnitCategories = pSharablesApplication.getUnitManager().getUnitsClassifierDataModel().getUnitCategoriesInUnitSystem(unitSystemSpinnerSelection);
            }

            for (String unitCategory : unformattedInitialUnitCategories) {
                if (unitCategory.contains(dimFilterUnitCategoryToken))
                    unformattedFinalUnitCategories.add(unitCategory);
            }
        }

        filterInvalidUnitCategories(unformattedFinalUnitCategories);

        if(unformattedFinalUnitCategories.isEmpty()) {
            unformattedFinalUnitCategories.add("No Compatible Unit Categories.");
        }

        ArrayList<String> formattedUnitCategories = new ArrayList<>();
        for(String unitCategory:unformattedFinalUnitCategories){
            formattedUnitCategories.add(spinnerSelectionFormatter.format(unitCategory));
        }

        categorySpinner.setAdapter(new ArrayAdapter<>(this
                , R.layout.unit_browse_spinner_item
                , formattedUnitCategories));
    }
    private void populateUnitSpinner() {
        Set<String> formattedFullNameNAbbreviations = new TreeSet<>(); //Sort by default alphabetical

        String quickDimFilterSelection = (String) quickDimFilterSpinner.getSelectedItem();
        String unitSystemSelection = (String) unitSystemSpinner.getSelectedItem();
        String categorySelection = (String) categorySpinner.getSelectedItem();

        boolean oppositeTargetUnitUnknown = pSharablesApplication.getTargetQuantity().getLargestUnit().getName()
                .equalsIgnoreCase(Unit.UNKNOWN_UNIT_NAME) && oppositeUnitType.equalsIgnoreCase(TARGET_NAME);
        boolean oppositeSourceUnitUnknown = pSharablesApplication.getSourceQuantity().getLargestUnit().getName().equalsIgnoreCase(Unit.UNKNOWN_UNIT_NAME)
                && oppositeUnitType.equalsIgnoreCase(SOURCE_NAME);

        if ((oppositeTargetUnitUnknown || oppositeSourceUnitUnknown) && quickDimFilterSelection.equals(quickDimensionFilters.get(OPPOSITE)))
        {
            formattedFullNameNAbbreviations.add("No Units Have A Dimension Compatible to the " + oppositeUnitType.toUpperCase() + " Unit.");

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
                boolean unitNameIsComplex = !componentUnitsDimensionParser.getDimensionParserBuilder().getDimensionComponentDefiner().hasComplexDimensions(unitName);

                String abbreviation = unit.getAbbreviation();
                String fullNameNAbbreviation = (!unitName.equalsIgnoreCase(abbreviation) && unitNameIsComplex) ? unitName + UNIT_ABBREVIATION_DELIMITER + unit.getAbbreviation() : unitName;
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

        boolean unitSelectionHasPrefix = pSharablesApplication.getUnitManager().getPrefixesDataModel().unitNameHasPrefix(unitSelection, true);
        boolean unitSelectionIsComplex = componentUnitsDimensionParser.getDimensionParserBuilder().getDimensionComponentDefiner().hasComplexDimensions(unitSelection);
        boolean unitCategoryIsCurrency = (unitCategorySelection).equalsIgnoreCase(MainActivity.CURRENCY_CATEGORY);

        if (!unitSelection.contains("no units") && !unitSelectionHasPrefix && !unitSelectionIsComplex && !unitCategoryIsCurrency)
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
                    double lhsPrefixValue = pSharablesApplication.getUnitManager().getPrefixesDataModel()
                            .getPrefixValue(lhsPrefixFullNameWithValue.split(PREFIX_DELIMITER)[0]);

                    double rhsPrefixValue = pSharablesApplication.getUnitManager().getPrefixesDataModel()
                            .getPrefixValue(rhsPrefixFullNameWithValue.split(PREFIX_DELIMITER)[0]);

                    return Double.compare( lhsPrefixValue, rhsPrefixValue);
                }
            });
        }

        prefixSpinner.setAdapter(new ArrayAdapter<>(this, R.layout.unit_browse_spinner_item, prefixesFullNamesWithValues));
    }

    ///
    private ArrayList<String> getFormattedUnitSystemsBasedOnQuickDimFilterSelection() {
        Collection<String> unformattedUnitSystems = new TreeSet<>(); //Naturally ordered and no duplicates
        String dimFilterSelection = (String) quickDimFilterSpinner.getSelectedItem();

        if (dimFilterSelection.equals(quickDimensionFilters.get(ALL)))
        {
            for(String unitSystem:pSharablesApplication.getUnitManager().getUnitsClassifierDataModel().getAllUnitSystems())
                unformattedUnitSystems.add(unitSystem);

        } else if (dimFilterSelection.equals(quickDimensionFilters.get(DYNAMIC))) {
            for (Unit dynamicUnit : pSharablesApplication.getUnitManager()
                    .getUnitsDataModel().getUnitsContentMainRetriever().getDynamicUnits())
            {
                unformattedUnitSystems.add(dynamicUnit.getUnitSystem());
            }
        } else {
            //Only add unit system that contains a relevant token or is associated with a relevant category.
            String relevantUnitSystemToken = getSingularUnitSystemTokenBasedOnQuickDimFilterSelection();
            String relevantUnitCategory = getSingularUnitCategoryTokenBasedOnQuickDimFilterSelection();

            for (String unitSystem : pSharablesApplication.getUnitManager().getUnitsClassifierDataModel().getAllUnitSystems()) {
                boolean unitSystemIsRelevantBasedOnUnitSystemToken = !relevantUnitSystemToken.isEmpty() && unitSystem.contains(relevantUnitSystemToken);
                boolean unitSystemIsRelevantBasedOnCategory = !unitSystem.equalsIgnoreCase(Unit.UNKNOWN_UNIT_SYSTEM)
                        && pSharablesApplication.getUnitManager().getUnitsClassifierDataModel().containsUnitCategoryInUnitSystem(unitSystem, relevantUnitCategory, true);

                if (unitSystemIsRelevantBasedOnUnitSystemToken || unitSystemIsRelevantBasedOnCategory)
                    unformattedUnitSystems.add(unitSystem);
            }
        }

        filterInvalidUnitSystems(unformattedUnitSystems);

        ArrayList<String> formattedUnitsSystems = new ArrayList<>();
        for(String unitSystem:unformattedUnitSystems){
            formattedUnitsSystems.add(spinnerSelectionFormatter.format(unitSystem));
        }

        return formattedUnitsSystems;
    }
    private String getSingularUnitCategoryTokenBasedOnQuickDimFilterSelection() {
        String dimFilterSelection = (String) quickDimFilterSpinner.getSelectedItem();

        if (dimFilterSelection.equalsIgnoreCase(quickDimensionFilters.get(CURRENCY))) {
            return "currency";
        } else if (dimFilterSelection.equalsIgnoreCase(quickDimensionFilters.get(LENGTH))) {
            return "length";
        } else if (dimFilterSelection.equalsIgnoreCase(quickDimensionFilters.get(VOLUME))) {
            return "volume";
        } else if (dimFilterSelection.equalsIgnoreCase(quickDimensionFilters.get(AREA))) {
            return "area";
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
        } else if (dimFilterSelection.equalsIgnoreCase(quickDimensionFilters.get(SI))) {
            return "si";
        } else if (dimFilterSelection.equalsIgnoreCase(quickDimensionFilters.get(CGS))) {
            return "cgs";
        } else if (dimFilterSelection.equalsIgnoreCase(quickDimensionFilters.get(US))) {
            return "us";
        } else if (dimFilterSelection.equalsIgnoreCase(quickDimensionFilters.get(SPECIAL))) {
            return "special";
        }else {
            return "";
        }
    }

    private void filterInvalidUnitSystems(Collection<String> filterableUnitSystems){
        filterInvalidItems(filterableUnitSystems, unitSystemFilteringTokens);
    }
    private void filterInvalidUnitCategories(Collection<String> filterableUnitCategories){
        filterInvalidItems(filterableUnitCategories, unitCategoryFilteringTokens);
    }
    private void filterInvalidItems(Collection<String> filterableItems, ArrayList<String> filterTokens){
        Iterator<String> filterableItemsIterator = filterableItems.iterator();
        while(filterableItemsIterator.hasNext()){
            String filterCandidate = filterableItemsIterator.next();

            for(String filterToken:filterTokens) {
                if (filterCandidate.contains(filterToken))
                    filterableItemsIterator.remove();
            }
        }
    }
}
