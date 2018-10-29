package com.isaacapps.unitconverterapp.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.isaacapps.unitconverterapp.dao.xml.readers.local.FundamentalUnitsMapXmlLocalReader;
import com.isaacapps.unitconverterapp.dao.xml.readers.local.PrefixesMapXmlLocalReader;
import com.isaacapps.unitconverterapp.dao.xml.readers.local.UnitsMapXmlLocalReader;
import com.isaacapps.unitconverterapp.dao.xml.readers.online.EuropeanCentralBankCurrencyUnitsMapXmlOnlineReader;
import com.isaacapps.unitconverterapp.dao.xml.readers.online.PrefixesNUnitsMapXmlOnlineReader;
import com.isaacapps.unitconverterapp.models.measurables.quantity.Quantity;
import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManagerBuilder;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel.UNIT_TYPE;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.PrefixesDataModel;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.hashdriven.FundamentalUnitsHashDualKeyNCategoryHashedRepository;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.hashdriven.PrefixesDualKeyNCategoryHashedRepository;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsContentDeterminer;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsContentDeterminer.DATA_MODEL_CATEGORY;
import com.isaacapps.unitconverterapp.processors.converters.QuantityConverter;
import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.grouping.GroupingFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.grouping.UnitNamesGroupingFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.grouping.ValuesGroupingFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.numbers.RoundingFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.text.GeneralTextFormatter;
import com.isaacapps.unitconverterapp.processors.operators.measurables.quantity.QuantityOperators;
import com.isaacapps.unitconverterapp.processors.parsers.ParsingException;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.componentunits.ComponentUnitsDimensionParser;
import com.isaacapps.unitconverterapp.processors.parsers.measurables.quantity.QuantityGroupingDefiner;
import com.isaacapps.unitconverterapp.processors.parsers.measurables.quantity.tokeniser.SerialGroupingQuantityTokenizer;
import com.isaacapps.unitconverterapp.processors.parsers.measurables.unit.UnitParser;
import com.isaacapps.unitconverterapp.processors.serializers.SerializingException;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.componentnunit.ComponentUnitsDimensionItemSerializer;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.componentnunit.ComponentUnitsDimensionSerializer;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.fundamentalunit.FundamentalUnitTypesDimensionItemSerializer;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.fundamentalunit.FundamentalUnitTypesDimensionSerializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<UnitManagerBuilder> {
    private static final int LOCAL_UNITS_LOADER = 0, FUND_UNITS_LOADER = 1, ONLINE_CURRENCY_UNITS_LOADER = 2, LOCAL_PREFIXES_LOADER = 3, ONLINE_PREFIXES_N_UNITS_LOADER = 4, POST_LOADER = 5;

    static public final String SOURCE_NAME = "Source";
    static public final String TARGET_NAME = "Target";

    private PersistentSharablesApplication pSharablesApplication;
    private UnitManager unitManager;
    private Quantity sourceQuantity, targetQuantity;

    private Locale locale;
    private UnitParser unitParser;
    private QuantityGroupingDefiner quantityGroupingDefiner;
    private GroupingFormatter generalGroupingFormatter;
    private UnitNamesGroupingFormatter unitNamesGroupingFormatter;
    private ValuesGroupingFormatter valuesGroupingFormatter;
    private IFormatter individualValueFormatter;
    private ComponentUnitsDimensionParser componentUnitsDimensionParser;
    private SerialGroupingQuantityTokenizer serialGroupingQuantityTokenizer;
    private ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer;
    private FundamentalUnitTypesDimensionSerializer fundamentalUnitTypesDimensionSerializer;

    private Button sourceUnitBrowseButton;
    private Button sourceUnitViewInfoButton;
    private ToggleButton sourceValueExpressionToggleButton;
    private MultiAutoCompleteTextView sourceUnitTextView;
    private TextView sourceValueTextView;

    private Button targetUnitViewInfoButton;
    private Button targetUnitBrowseButton;
    private MultiAutoCompleteTextView targetUnitTextView;

    private TextView conversionValueTextView;
    private Animation convertButtonAnimation;
    private Button convertButton;

    private AlertDialog unitInfoDialog;
    private MenuItem multiModeMenuItem;
    private ProgressBar unitManagerLoaderProgressBar;
    private TextView progressBarTextView;

    ///
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pSharablesApplication = (PersistentSharablesApplication) this.getApplication();

        sourceQuantity = pSharablesApplication.getSourceQuantity();
        targetQuantity = pSharablesApplication.getTargetQuantity();

        locale = pSharablesApplication.getResources().getConfiguration().locale;
        quantityGroupingDefiner = new QuantityGroupingDefiner();
        generalGroupingFormatter = new GroupingFormatter(locale, quantityGroupingDefiner);
        unitNamesGroupingFormatter = new UnitNamesGroupingFormatter(locale, quantityGroupingDefiner);
        valuesGroupingFormatter = new ValuesGroupingFormatter(locale, quantityGroupingDefiner, 1.0);
        individualValueFormatter = new RoundingFormatter(locale, 4);

        fundamentalUnitTypesDimensionSerializer = new FundamentalUnitTypesDimensionSerializer(locale, new FundamentalUnitTypesDimensionItemSerializer(locale, new GeneralTextFormatter(locale)));
        componentUnitsDimensionSerializer  = new ComponentUnitsDimensionSerializer(locale, new ComponentUnitsDimensionItemSerializer(locale, new GeneralTextFormatter(locale)));

        try {
            componentUnitsDimensionParser = new ComponentUnitsDimensionParser();
        } catch (ParsingException e1) {
            e1.printStackTrace();
        }
        unitParser = new UnitParser(componentUnitsDimensionParser);
        serialGroupingQuantityTokenizer = new SerialGroupingQuantityTokenizer(locale, unitParser, quantityGroupingDefiner, valuesGroupingFormatter, unitNamesGroupingFormatter);

        //
        setupUIComponents();

        setupAnimations();

        setListenersOnMainButtons();
        setListenersOnTextViews();

        loadUnitManager();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            try {
                String sourceQuantityUnitNames = sourceQuantity.getUnitNames();
                if(!multiModeMenuItem.isChecked() && sourceQuantity.getUnits().size() == 1){ //No need to have groups if there only one item and multiunit mode is off.
                    sourceQuantityUnitNames = sourceQuantityUnitNames.replace(quantityGroupingDefiner.getGroupOpeningSymbol(), "")
                            .replace(quantityGroupingDefiner.getGroupClosingSymbol(), "");
                }
                sourceUnitTextView.setText(sourceQuantityUnitNames);

                String sourceQuantityValues = sourceQuantity.getValuesString();
                if(!multiModeMenuItem.isChecked() && sourceQuantity.getValues().size() == 1){ //No need to have groups if there only one item and multiunit mode is off.
                    sourceQuantityValues = sourceQuantityValues .replace(quantityGroupingDefiner.getGroupOpeningSymbol(), "")
                            .replace(quantityGroupingDefiner.getGroupClosingSymbol(), "");
                }
                sourceValueTextView.setText(sourceQuantityValues);

                String targetQuantityUnitNames = targetQuantity.getUnitNames();
                if(!multiModeMenuItem.isChecked() && targetQuantity.getUnits().size() == 1){ //No need to have groups if there only one item and multiunit mode is off.
                    targetQuantityUnitNames = sourceQuantityUnitNames.replace(quantityGroupingDefiner.getGroupOpeningSymbol(), "")
                            .replace(quantityGroupingDefiner.getGroupClosingSymbol(), "");
                }
                targetUnitTextView.setText(targetQuantityUnitNames);

                checkUnits();
            }
            catch(Exception e){}
        } else {
            pSharablesApplication.saveUnits();
            pSharablesApplication.saveConversionFavorites();
        }
    }

    ///Menu Methods
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        multiModeMenuItem = menu.findItem(R.id.multiUnitModeItem);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addToFavoritesItem:
                addToFavorites(item);
                return true;
            case R.id.viewFavoritesItem:
                viewFavorites(item);
                return true;
            case R.id.flipItem:
                flipConversion(item);
                return true;
            case R.id.multiUnitModeItem:
                setMultiUnitMode(item);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addToFavorites(MenuItem item) {
        boolean favoritesAdditionPossible;
        try {
            favoritesAdditionPossible = unitManager.getConversionFavoritesDataModel().addConversion(sourceQuantity, targetQuantity).length() > 0;
        }
        catch(Exception e){
            favoritesAdditionPossible = false;
        }

        if (favoritesAdditionPossible) {
            sourceUnitTextView.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.slide_out_right));
            targetUnitTextView.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.slide_out_right));
        }
    }

    private void viewFavorites(MenuItem item) {
        Intent i = new Intent(MainActivity.this, ConversionFavoritesActivity.class);
        startActivity(i);
    }

    private void flipConversion(MenuItem item) {
        String tempText = sourceUnitTextView.getText().toString();

        sourceUnitTextView.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in));
        sourceUnitTextView.setText(targetUnitTextView.getText().toString());

        targetUnitTextView.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in));
        targetUnitTextView.setText(tempText);

        setSourceUnitsIntoQuantity();
        setTargetUnitIntoQuantity();

        if (checkUnits() && checkNSetSourceValuesIntoQuantity())
            processConversion();
    }

    private void setMultiUnitMode(MenuItem item) {
        boolean isMultiUnitMode = !item.isChecked();
        item.setChecked(isMultiUnitMode);
        item.setTitle(item.getTitle().toString().replaceFirst(":.+", isMultiUnitMode ? ":ON" : ":OFF"));
        item.setTitleCondensed(item.getTitleCondensed().toString().replaceFirst(":.+", isMultiUnitMode ? ":ON" : ":OFF"));

        if (isMultiUnitMode) {
            //No grouping exist, then add some dummy ones for demonstration purposes
            if (generalGroupingFormatter.calculateGroupingCount(sourceUnitTextView.getText().toString()) == 0)
                sourceUnitTextView.setText("{a}{b}{c}");
            if (generalGroupingFormatter.calculateGroupingCount(targetUnitTextView.getText().toString()) == 0)
                targetUnitTextView.setText("{a}{b}{c}");

            sourceValueTextView.setText(valuesGroupingFormatter.format(sourceValueTextView.getText().toString()));

            adjustSourceValueTextViewGroupingsCountBaseOnSourceUnits();

            sourceValueExpressionToggleButton.setChecked(false); //Bracketed groupings are not recognized by the expression evaluator
        }
    }

    ///Setup UI Components Methods
    private void setupUIComponents() {
        unitManagerLoaderProgressBar = (ProgressBar) findViewById(R.id.unitManagerLoaderProgressBar);
        progressBarTextView = (TextView) findViewById(R.id.progressBarTextView);

        //
        sourceUnitBrowseButton = (Button) findViewById(R.id.sourceUnitBrowseButton);
        targetUnitBrowseButton = (Button) findViewById(R.id.targetUnitBrowseButton);
        convertButton = (Button) findViewById(R.id.convertButton);
        sourceUnitViewInfoButton = (Button) findViewById(R.id.sourceUnitViewDescButton);
        targetUnitViewInfoButton = (Button) findViewById(R.id.targetUnitViewDescButton);

        //
        sourceValueExpressionToggleButton = (ToggleButton) findViewById(R.id.sourceValueExpressionToggleButton);

        //
        sourceUnitTextView = (MultiAutoCompleteTextView) findViewById(R.id.sourceUnitTextView);
        sourceValueTextView = (TextView) findViewById(R.id.sourceValueTextView);
        sourceValueTextView.setText("1");
        targetUnitTextView = (MultiAutoCompleteTextView) findViewById(R.id.targetUnitTextView);
        conversionValueTextView = (TextView) findViewById(R.id.conversionOutputTextView);

        //
        unitInfoDialog = new AlertDialog.Builder(MainActivity.this).create();
        unitInfoDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    ///Setup Animation Methods
    private void setupAnimations() {
        convertButtonAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.conversion_button_animation);
    }

    ///Unit Manager Loading Methods
    private void loadUnitManager() {
        if (!pSharablesApplication.isUnitManagerPreReqLoadingComplete()) {
            //getSupportLoaderManager().initLoader(ONLINE_PREFIXES_N_UNITS_LOADER, null, this).forceLoad();
            getSupportLoaderManager().initLoader(LOCAL_UNITS_LOADER, null, this).forceLoad();
            getSupportLoaderManager().initLoader(LOCAL_PREFIXES_LOADER, null, this).forceLoad();
            getSupportLoaderManager().initLoader(FUND_UNITS_LOADER, null, this).forceLoad();
            //getSupportLoaderManager().initLoader(ONLINE_CURRENCY_UNITS_LOADER, null, this).forceLoad();

            //
            findViewById(R.id.progressBarLinearLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.sourceLinearLayout).setVisibility(View.INVISIBLE);
            findViewById(R.id.convertLinearLayout).setVisibility(View.INVISIBLE);
            findViewById(R.id.targetLinearLayout).setVisibility(View.INVISIBLE);

            //
            sourceValueExpressionToggleButton.setVisibility(View.INVISIBLE);
        }
    }

    private void postLoadSetup() {
        findViewById(R.id.progressBarLinearLayout).setVisibility(View.GONE);
        findViewById(R.id.sourceLinearLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.convertLinearLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.targetLinearLayout).setVisibility(View.VISIBLE);

        unitManager = pSharablesApplication.getUnitManager();
        unitParser.setUnitManager(unitManager);
    }

    ///Listeners Methods
    private void setListenersOnMainButtons() {

        sourceUnitBrowseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, UnitBrowserActivity.class);
                i.putExtra("buttonName", SOURCE_NAME);
                startActivity(i);
            }
        });

        targetUnitBrowseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, UnitBrowserActivity.class);
                i.putExtra("buttonName", TARGET_NAME);
                startActivity(i);
            }
        });

        sourceUnitViewInfoButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                unitInfoDialog.setTitle(SOURCE_NAME + " Unit(s) Details");
                unitInfoDialog.setMessage(getUnitsDetailsMessage(sourceQuantity.getUnits()));
                unitInfoDialog.show();
            }
        });

        targetUnitViewInfoButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                unitInfoDialog.setTitle(TARGET_NAME + " Unit(s) Details");
                unitInfoDialog.setMessage(getUnitsDetailsMessage(targetQuantity.getUnits()));
                unitInfoDialog.show();
            }
        });

        sourceValueExpressionToggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //TODO: Make necessary preparation to use an expression evaluator some third party library. Possibly, the JEL third party library

                if (multiModeMenuItem.isChecked()) {//Disable multi-unit group to prevent interference with expression evaluation
                    onOptionsItemSelected(multiModeMenuItem);
                    sourceValueTextView.setInputType(InputType.TYPE_CLASS_TEXT);
                } else {
                    sourceValueTextView.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL
                            | InputType.TYPE_NUMBER_FLAG_SIGNED);
                }

                //
            }
        });

        convertButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                //setSourceUnitsIntoQuantity();
                //setTargetUnitIntoQuantity();

                if (checkUnits() && checkNSetSourceValuesIntoQuantity())
                    processConversion();
            }
        });
    }

    private String getUnitsDetailsMessage(Collection<Unit> unitsGroup) {
        StringBuilder detailsBuilder = new StringBuilder();

        appendPerUnitInfo(detailsBuilder, unitsGroup);

        //Assumption is that all units in collection are of the same kind, therefore certain "type" info of the first unit also applies to the others.
        appendInvariantInfo(detailsBuilder, unitsGroup.iterator().next());

        return detailsBuilder.toString();
    }
    private void appendPerUnitInfo(StringBuilder detailsBuilder, Collection<Unit> unitsGroup){
        for(Unit unit:unitsGroup){
            detailsBuilder.append("Name: ").append(unit.getName()).append("(").append(unit.getAbbreviation()).append(")").append("\n");
            if(!unit.getAliases().isEmpty())
                detailsBuilder.append("Aliases: ").append(Arrays.toString(unit.getAliases().toArray())).append("\n");
            if(!unit.getDescription().isEmpty())
                detailsBuilder.append("Description: ").append(unit.getDescription()).append("\n");
            detailsBuilder.append("Unit System: ").append(unit.getUnitSystem()).append("\n");

            String componentUnitsDimimension = "";
            try {
                if(UnitsContentDeterminer.determineGeneralDataModelCategory(unit) != DATA_MODEL_CATEGORY.UNKNOWN
                        && (unit.getType() == UNIT_TYPE.DERIVED_MULTI_UNIT || unit.getType() == UNIT_TYPE.DERIVED_SINGLE_UNIT))
                {
                    componentUnitsDimimension = componentUnitsDimensionSerializer.serialize(unit.getComponentUnitsDimension());
                }
            } catch (SerializingException e) { }

            if(!componentUnitsDimimension.isEmpty())
                detailsBuilder.append("Component Units Dimension: ").append(componentUnitsDimimension).append("\n\n");
        }
    }
    private void appendInvariantInfo(StringBuilder detailsBuilder, Unit groupRepresentativeUnit){
        String fundamentalTypesDimension = "";
        try {
            if(UnitsContentDeterminer.determineGeneralDataModelCategory(groupRepresentativeUnit) != DATA_MODEL_CATEGORY.UNKNOWN)
                fundamentalTypesDimension =fundamentalUnitTypesDimensionSerializer.serialize(groupRepresentativeUnit.getFundamentalUnitTypesDimension());
        } catch (SerializingException e) { }

        detailsBuilder.append("-------------").append("\n");
        if(!fundamentalTypesDimension.isEmpty())
            detailsBuilder.append("Fundamental Types Dimension: ").append(fundamentalTypesDimension).append("\n");
        if(!groupRepresentativeUnit.getCategory().equalsIgnoreCase(fundamentalTypesDimension)) //Prevent display of duplicate data in case of complex units
            detailsBuilder.append("Category: ").append(groupRepresentativeUnit.getCategory()).append("\n");
    }

    private void setListenersOnTextViews() {
        sourceUnitTextView.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {

                    if (multiModeMenuItem.isChecked()) {
                        ((TextView) view).setText(unitNamesGroupingFormatter.format(((TextView) view).getText().toString()));
                        adjustTextViewGroupingsCount(sourceValueTextView, sourceQuantity.getUnits().size(), "1.0");
                    }

                    try {
                        setSourceUnitsIntoQuantity();
                        checkUnits();
                        checkNSetSourceValuesIntoQuantity();
                    }
                    catch(Exception e){
                        sourceUnitTextView.setTextColor(Color.rgb(180, 0, 0));
                    }
                }
            }
        });

        targetUnitTextView.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {

                    if (multiModeMenuItem.isChecked())
                        ((TextView) view).setText(unitNamesGroupingFormatter.format(((TextView) view).getText().toString()));

                    try {
                        setTargetUnitIntoQuantity();
                        checkUnits();
                        checkNSetSourceValuesIntoQuantity();
                    }
                    catch(Exception e){
                        targetUnitTextView.setTextColor(Color.rgb(180, 0, 0));
                    }
                }
            }
        });

        sourceValueTextView.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    if (multiModeMenuItem.isChecked()) {
                        String sourceValueText = valuesGroupingFormatter.format(((TextView) view).getText().toString());
                        ((TextView) view).setText(sourceValueText);

                        adjustSourceUnitTextViewGroupingsCountBasedOnSourceValue();
                    }

                    checkNSetSourceValuesIntoQuantity();
                }
            }
        });
    }

    ///Multi-Unit Grouping Formatting Methods
    private boolean adjustTextViewGroupingsCount(TextView textViewToBeAdjusted, int referenceGroupCount, String dummyTextForEmptyGroupings) {
        if (referenceGroupCount > 0) {
            String initialText = textViewToBeAdjusted.getText().toString();
            String textWithAdjustedGroupingCount = generalGroupingFormatter.adjustGroupingsCount(initialText, referenceGroupCount);
            String adjustedTextWithReplacedEmptyGroupings = generalGroupingFormatter.replaceEmptyGroupingsWithDefaultGrouping(textWithAdjustedGroupingCount, dummyTextForEmptyGroupings);

            textViewToBeAdjusted.setText(adjustedTextWithReplacedEmptyGroupings);

            return true;
        }

        return false;
    }
    private boolean adjustSourceValueTextViewGroupingsCountBaseOnSourceUnits() {
        return adjustTextViewGroupingsCount(sourceValueTextView, unitNamesGroupingFormatter.calculateGroupingCount(sourceUnitTextView.getText().toString()), "1.0");
    }
    private boolean adjustSourceUnitTextViewGroupingsCountBasedOnSourceValue() {
        return adjustTextViewGroupingsCount(sourceUnitTextView, valuesGroupingFormatter.calculateGroupingCount(sourceValueTextView.getText().toString()), "a");
    }

    ///Quantity Setting and Validation Methods
    private void setSourceUnitsIntoQuantity() {
        setUnitsIntoQuantity(false);
    }

    private void setTargetUnitIntoQuantity(){
        setUnitsIntoQuantity(true);
    }

    private void setUnitsIntoQuantity(boolean isTargetQuantity) {
        String unitNamesOrDimension = (isTargetQuantity ? targetUnitTextView : sourceUnitTextView).getText().toString().trim();

        Quantity selectedQuantity = (isTargetQuantity) ? targetQuantity : sourceQuantity;

        try {
            //Only replace anything only if there is difference
            if (!(quantityGroupingDefiner.removeGroupingSymbol(selectedQuantity.getUnitNames())
                    .equalsIgnoreCase(quantityGroupingDefiner.removeGroupingSymbol(unitNamesOrDimension))) )
            {
                if (unitNamesOrDimension.isEmpty()) {
                    selectedQuantity.setUnits(Collections.singletonList(unitManager.getUnitsDataModel().getUnitsContentMainRetriever().getUnknownUnit()));
                } else {
                    selectedQuantity.setUnits(serialGroupingQuantityTokenizer.parseSerialGroupingToUnitsList(unitNamesOrDimension));
                }
            }

            //Update conversion favorites rank
            for (Unit unit : (isTargetQuantity ? targetQuantity.getUnits() : sourceQuantity.getUnits())) {
                unitManager.getConversionFavoritesDataModel().modifySignificanceRankOfMultipleConversions(unit, true);
            }
        }
        catch(Exception e){
            try {
                if (isTargetQuantity) {
                    pSharablesApplication.getTargetQuantity().setUnit(pSharablesApplication.getUnitManager().getUnitsDataModel().getUnitsContentMainRetriever().getUnknownUnit(), true);
                } else {
                    pSharablesApplication.getSourceQuantity().setUnit(pSharablesApplication.getUnitManager().getUnitsDataModel().getUnitsContentMainRetriever().getUnknownUnit(), true);
                }
            } catch (Exception e1) { }
        }
    }

    private boolean checkNSetSourceValuesIntoQuantity() {
        try {
            String selectedSourceValuesText = sourceValueTextView.getText().toString();

            sourceQuantity.setValues(serialGroupingQuantityTokenizer.parseSerialGroupingToValuesList(selectedSourceValuesText));
            sourceValueTextView.setTextColor(Color.BLACK);

            return true;

        }catch(Exception e){
            sourceValueTextView.setTextColor(Color.rgb(180, 0, 0));
            setConversionButton(false);
            return false;
        }
    }

    private boolean checkUnits() {
        boolean sourceUnitIsUnknown = UnitsContentDeterminer.determineGeneralDataModelCategory(sourceQuantity.getLargestUnit()) == DATA_MODEL_CATEGORY.UNKNOWN;
        boolean targetUnitIsUnknown = UnitsContentDeterminer.determineGeneralDataModelCategory(targetQuantity.getLargestUnit()) == DATA_MODEL_CATEGORY.UNKNOWN;
        boolean UnitsMatch = QuantityOperators.equalsUnitDimensionOf(sourceQuantity, targetQuantity);
        boolean UnitsAreOK = !(sourceUnitIsUnknown || targetUnitIsUnknown) && UnitsMatch;

        sourceUnitTextView.setTextColor(Color.rgb(sourceUnitIsUnknown ? 180 : 0, 0, 0));
        targetUnitTextView.setTextColor(Color.rgb(targetUnitIsUnknown ? 180 : 0, 0, 0));
        conversionValueTextView.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in));
        conversionValueTextView.setTextColor(Color.rgb(UnitsAreOK ? 0 : 200, 0, 0));

        setConversionButton(UnitsAreOK);

        //
        if (UnitsAreOK) {
            conversionValueTextView.setText("##:Units Match");
        } else if (!(sourceUnitIsUnknown || targetUnitIsUnknown) && !UnitsMatch) {
            conversionValueTextView.setText("XX:Units Don't Match");
        } else {
            String unknownName = (sourceUnitIsUnknown ? "'" + SOURCE_NAME + "', " : "") + (targetUnitIsUnknown ? "'" + TARGET_NAME + "', " : "");
            String unknownSymbol = (sourceUnitIsUnknown ? "?" : "#") + (targetUnitIsUnknown ? "?" : "#");

            conversionValueTextView.setText(String.format("%s: Unit In %s Group(s) is Unkn. or Has Wrong Dim.", unknownSymbol, unknownName));
        }

        return UnitsAreOK;
    }

    ///Conversion Methods
    private void processConversion() {
        try {
            List<Unit> targetUnits = Collections.list(Collections.enumeration(targetQuantity.getUnits()));
            List<Double> values = new ArrayList<>(QuantityConverter.determineConversionQuantityToTargetUnitsGroup(sourceQuantity, targetUnits, false).getValues());

            targetQuantity.setValues(values);

            conversionValueTextView.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.slide_in_left));
            conversionValueTextView.setText(individualValueFormatter.format(targetQuantity.getValuesString()));

            //
            setConversionButton(false);
        }
        catch (Exception e) {
        }
    }

    private void setConversionButton(boolean triggerFocus) {
        if (triggerFocus) {
            convertButton.setTextColor(Color.BLUE);
            convertButton.startAnimation(convertButtonAnimation);
        } else {
            convertButton.setTextColor(Color.BLACK);
            convertButton.clearAnimation();
        }
    }

    ///Loader Manager Methods
    @Override
    public Loader<UnitManagerBuilder> onCreateLoader(int id, Bundle arg1) {
        switch (id) {
            case LOCAL_UNITS_LOADER:
                return new UnitsMapXmlLocalReader(this, this.locale, this.componentUnitsDimensionSerializer, this.fundamentalUnitTypesDimensionSerializer, this.componentUnitsDimensionParser.getDimensionParserBuilder().getDimensionComponentDefiner());
            case LOCAL_PREFIXES_LOADER:
                return new PrefixesMapXmlLocalReader(this, new PrefixesDataModel(new PrefixesDualKeyNCategoryHashedRepository()));
            case FUND_UNITS_LOADER:
                return new FundamentalUnitsMapXmlLocalReader(this, new FundamentalUnitsDataModel(new FundamentalUnitsHashDualKeyNCategoryHashedRepository()));
            case ONLINE_CURRENCY_UNITS_LOADER:
                return new EuropeanCentralBankCurrencyUnitsMapXmlOnlineReader(this, this.locale, this.componentUnitsDimensionSerializer, this.fundamentalUnitTypesDimensionSerializer, this.componentUnitsDimensionParser.getDimensionParserBuilder().getDimensionComponentDefiner());
            case ONLINE_PREFIXES_N_UNITS_LOADER:
                try {
                    return new PrefixesNUnitsMapXmlOnlineReader(this, this.locale, new PrefixesDataModel(new PrefixesDualKeyNCategoryHashedRepository()), this.componentUnitsDimensionSerializer, this.fundamentalUnitTypesDimensionSerializer, this.componentUnitsDimensionParser.getDimensionParserBuilder().getDimensionComponentDefiner());
                } catch (ParsingException e) {
                    e.printStackTrace();
                }
            case POST_LOADER:
                return new AsyncTaskLoader<UnitManagerBuilder>(this) {
                    @Override
                    public UnitManagerBuilder loadInBackground() {
                        ((PersistentSharablesApplication) getContext().getApplicationContext()).recreateUnitManager();
                        return null;
                    }
                };
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<UnitManagerBuilder> loader, UnitManagerBuilder loadedUnitManagerBuilder) {
        if (loadedUnitManagerBuilder != null) {
            try {
                pSharablesApplication.getUnitManagerBuilder().combineWith(loadedUnitManagerBuilder);
                pSharablesApplication.setNumberOfLoadersCompleted(pSharablesApplication.getNumberOfLoadersCompleted() + 1);
            }
            catch (Exception e){
                postLoadSetup();
            }
        }

        if (pSharablesApplication.isUnitManagerPreReqLoadingComplete()) {
            if (loadedUnitManagerBuilder != null)
                getSupportLoaderManager().initLoader(POST_LOADER, null, this).forceLoad();
            else {
                postLoadSetup(); //Executed when POST_LOADER returns after its completion
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<UnitManagerBuilder> arg0) { }
}
