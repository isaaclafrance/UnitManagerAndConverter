package com.isaacapps.unitconverterapp.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.InputType;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
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
import android.widget.Toast;
import android.widget.ToggleButton;

import com.isaacapps.unitconverterapp.adapters.MultiAutoCompleteUnitsDefinitionArrayAdapter;
import com.isaacapps.unitconverterapp.dao.xml.readers.local.ConversionFavoritesListXmlLocalReader;
import com.isaacapps.unitconverterapp.dao.xml.readers.local.FundamentalUnitsMapXmlLocalReader;
import com.isaacapps.unitconverterapp.dao.xml.readers.local.units.NonStandardUnitsMapsXmlLocalReader;
import com.isaacapps.unitconverterapp.dao.xml.readers.local.PrefixesMapXmlLocalReader;
import com.isaacapps.unitconverterapp.dao.xml.readers.local.units.StandardCoreUnitsMapXmlLocalReader;
import com.isaacapps.unitconverterapp.dao.xml.readers.online.EuropeanCentralBankCurrencyUnitsMapXmlOnlineReader;
import com.isaacapps.unitconverterapp.dao.xml.readers.online.PrefixesNUnitsMapXmlOnlineReader;
import com.isaacapps.unitconverterapp.models.measurables.quantity.Quantity;
import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManagerBuilder;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.ConversionFavoritesDataModel;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.FundamentalUnitsDataModel;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.PrefixesDataModel;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.hashdriven.ConversionFavoritesDualKeyNCategoryHashedRepository;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.hashdriven.FundamentalUnitsHashDualKeyNCategoryHashedRepository;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.hashdriven.PrefixesDualKeyNCategoryHashedRepository;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.hashdriven.SignificanceRankHashedRepository;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsContentDeterminer;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.unitsdatamodel.UnitsContentDeterminer.DATA_MODEL_CATEGORY;
import com.isaacapps.unitconverterapp.processors.converters.QuantityConverter;
import com.isaacapps.unitconverterapp.processors.formatters.ChainedFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.grouping.GroupingFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.grouping.UnitNamesGroupingFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.grouping.ValuesGroupingFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.numbers.CurrencyFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.numbers.DecimalToMixedFractionFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.numbers.MixedFractionToDecimalFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.numbers.RoundingFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.numbers.ScientificNotationFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.text.GeneralTextFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.text.SnakeCaseFormatter;
import com.isaacapps.unitconverterapp.processors.operators.dimensions.DimensionOperators.DIMENSION_TYPE;
import com.isaacapps.unitconverterapp.processors.operators.measurables.quantity.QuantityOperators;
import com.isaacapps.unitconverterapp.processors.parsers.ParsingException;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.DimensionComponentDefiner;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.componentunits.ComponentUnitsDimensionParser;
import com.isaacapps.unitconverterapp.processors.parsers.measurables.quantity.QuantityGroupingDefiner;
import com.isaacapps.unitconverterapp.processors.parsers.measurables.quantity.tokeniser.SerialGroupingQuantityTokenizer;
import com.isaacapps.unitconverterapp.processors.parsers.measurables.unit.UnitParser;
import com.isaacapps.unitconverterapp.processors.serializers.SerializingException;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.componentnunit.ComponentUnitsDimensionSerializer;
import com.isaacapps.unitconverterapp.processors.serializers.dimension.fundamentalunit.FundamentalUnitTypesDimensionSerializer;
import com.isaacapps.unitconverterapp.tokenizers.MultiAutoCompleteUnitDefinitionTokenizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<UnitManagerBuilder> {
    //It's a shame that the use of Enum's is not recommended on android
    private static final int NON_STANDARD_LOCAL_UNITS_LOADER = 0, FUND_UNITS_LOADER = 1, ONLINE_CURRENCY_UNITS_LOADER = 2, LOCAL_PREFIXES_LOADER = 3, ONLINE_PREFIXES_N_UNITS_LOADER = 4, POST_LOADER = 5, UPDATE_LOADER = 6, FAVORITES_LOADER = 7, STANDARD_CORE_LOCAL_UNITS_LOADER = 8;

    public static final String SOURCE_NAME = "Source";
    public static final String TARGET_NAME = "Target";
    public static final String CURRENCY_CATEGORY = "currency_unit";

    private static final String DUMMY_UNIT = "<unit>";
    private static final String DUMMY_VALUE = "1.0";
    private static final int DEMONSTRATION_GROUP_NUM_IN_MULTI_UNIT_MODE = 3;

    private boolean currencyUnitsLoaded;
    private boolean standardLocalCoreUnitsLoaded;
    private boolean nonStandardLocalUnitsLoaded;
    private boolean onlineUnitsPreviouslySaved;
    private boolean localPrefixesLoaded;
    private boolean favoritesLoaded;

    private SharedPreferences sharedPreferences;

    private PersistentSharablesApplication pSharablesApplication;
    private UnitManager unitManager;
    private Quantity sourceQuantity, targetQuantity;

    private Locale locale;
    private UnitParser unitParser;
    private QuantityGroupingDefiner quantityGroupingDefiner;
    private ComponentUnitsDimensionParser componentUnitsDimensionParser;
    private SerialGroupingQuantityTokenizer serialGroupingQuantityTokenizer;
    private ComponentUnitsDimensionSerializer componentUnitsDimensionSerializer;
    private FundamentalUnitTypesDimensionSerializer fundamentalUnitTypesDimensionSerializer;

    private GroupingFormatter generalGroupingFormatter;
    private UnitNamesGroupingFormatter unitNamesGroupingFormatter;
    private ValuesGroupingFormatter valuesGroupingFormatter;
    private IFormatter overallValuesGroupingFormatter;
    private RoundingFormatter conversionRoundingFormatter;
    private CurrencyFormatter conversionCurrencyFormatter;
    private DecimalToMixedFractionFormatter decimalToMixedFractionFormatter;
    private ScientificNotationFormatter scientificNotationFormatter;
    private IFormatter sourceValueFormatter;

    private Button sourceUnitBrowseButton;
    private Button sourceUnitViewInfoButton;
    private ToggleButton sourceValueExpressionToggleButton;
    private MultiAutoCompleteTextView sourceUnitsTextView;
    private MultiAutoCompleteUnitsDefinitionArrayAdapter sourceUnitsTextViewArrayAdapter;
    private TextView sourceValueTextView;

    private Button targetUnitViewInfoButton;
    private Button targetUnitBrowseButton;
    private MultiAutoCompleteTextView targetUnitsTextView;
    private MultiAutoCompleteUnitsDefinitionArrayAdapter targetUnitsTextViewArrayAdapter;

    private TextView conversionValueTextView;
    private Animation convertButtonAnimation;
    private Button convertButton;

    private AlertDialog unitInfoDialog;
    private TextView dialogTextView;
    private MenuItem multiModeMenuItem;
    private ProgressBar unitManagerLoaderProgressBar;
    private TextView progressBarTextView;

    ///
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getPreferences(MODE_PRIVATE);
        onlineUnitsPreviouslySaved = sharedPreferences.getBoolean(getString(R.string.online_units_previously_saved_pref_key), false);

        pSharablesApplication = (PersistentSharablesApplication) this.getApplication();

        //
        Bundle extras = getIntent().getExtras();
        boolean multiUnitMode = (extras != null ? extras.getBoolean(PersistentSharablesApplication.MULTI_UNIT_MODE_BUNDLE_NAME):false);
        pSharablesApplication.setMultiUnitModeOn(multiUnitMode);

        sourceQuantity = pSharablesApplication.getSourceQuantity();
        targetQuantity = pSharablesApplication.getTargetQuantity();

        locale = pSharablesApplication.getResources().getConfiguration().locale;

        try {
            quantityGroupingDefiner = new QuantityGroupingDefiner();
            generalGroupingFormatter = new GroupingFormatter(locale, quantityGroupingDefiner);
            unitNamesGroupingFormatter = new UnitNamesGroupingFormatter(locale, quantityGroupingDefiner);
            valuesGroupingFormatter = new ValuesGroupingFormatter(locale, quantityGroupingDefiner, 1.0);
            sourceValueFormatter = new MixedFractionToDecimalFormatter(locale);
            overallValuesGroupingFormatter = new ChainedFormatter(locale).AddFormatter(valuesGroupingFormatter).AddFormatter(sourceValueFormatter);

            conversionRoundingFormatter = new RoundingFormatter(locale, 4);
            conversionCurrencyFormatter = new CurrencyFormatter(locale);
            decimalToMixedFractionFormatter = new DecimalToMixedFractionFormatter(locale);
            scientificNotationFormatter = new ScientificNotationFormatter(locale, 3);

            fundamentalUnitTypesDimensionSerializer = new FundamentalUnitTypesDimensionSerializer(locale, new GeneralTextFormatter(locale), new RoundingFormatter(locale, 2));
            componentUnitsDimensionSerializer  = new ComponentUnitsDimensionSerializer(locale, new GeneralTextFormatter(locale), new RoundingFormatter(locale, 2));

            componentUnitsDimensionParser = new ComponentUnitsDimensionParser(locale);
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

        initiateLoadUnitManagerComponent();
    }
    @Override
    protected void onResume(){
        super.onResume();

        if(unitManager != null){
            populateTextViews();
            checkUnits();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();

        boolean unitsAbleToBeSaved = false;
        if(standardLocalCoreUnitsLoaded)
            unitsAbleToBeSaved = pSharablesApplication.saveUnits(!onlineUnitsPreviouslySaved);

        if(!onlineUnitsPreviouslySaved && unitsAbleToBeSaved && pSharablesApplication.isOnlineUnitsCurrentlyLoaded())
            sharedPreferences.edit().putBoolean(getString(R.string.online_units_previously_saved_pref_key), true).commit();

        if(favoritesLoaded || !unitManager.getConversionFavoritesDataModel().getAllFormattedConversions().isEmpty())
            pSharablesApplication.saveConversionFavorites();
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        if(hasFocus && unitManager != null){
            //populateTextViews();
            //checkUnits();
        }
    }

    ///Menu Methods
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        multiModeMenuItem = menu.findItem(R.id.multi_unit_mode_item);
        setMultiUnitMode(multiModeMenuItem, pSharablesApplication.isMultiUnitModeOn());
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_to_favorites_item:
                addToFavorites(item);
                return true;
            case R.id.view_favorites_item:
                viewFavorites(item);
                return true;
            case R.id.flip_item:
                flipConversion(item);
                return true;
            case R.id.multi_unit_mode_item:
                setMultiUnitMode(item);
                return true;
            case R.id.update_currency_item:
                updateCurrency(item);
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
            sourceUnitsTextView.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.slide_out_right));
            targetUnitsTextView.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.slide_out_right));
        }
        else{
            Toast.makeText(this, "Conversion not added. Likely already added before.", Toast.LENGTH_SHORT);
        }
    }
    private void viewFavorites(MenuItem item) {
        Intent i = new Intent(MainActivity.this, ConversionFavoritesActivity.class);
        startActivity(i);
    }
    private void flipConversion(MenuItem item) {
        String tempText = sourceUnitsTextView.getText().toString();

        sourceUnitsTextView.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in));
        sourceUnitsTextView.setText(targetUnitsTextView.getText().toString());

        targetUnitsTextView.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in));
        targetUnitsTextView.setText(tempText);

        setSourceUnitsIntoQuantity();
        setTargetUnitIntoQuantity();

        if (checkUnits() && checkNSetSourceValuesIntoQuantity())
            processConversion();
    }
    private void setMultiUnitMode(MenuItem item) {
        setMultiUnitMode(item, !item.isChecked());
    }
    private void setMultiUnitMode(MenuItem item, boolean isMultiUnitModeOn) {
        pSharablesApplication.setMultiUnitModeOn(isMultiUnitModeOn);

        item.setChecked(isMultiUnitModeOn);
        item.setTitle(item.getTitle().toString().replaceFirst(":.+", isMultiUnitModeOn ? ":ON" : ":OFF"));
        item.setTitleCondensed(item.getTitleCondensed().toString().replaceFirst(":.+", isMultiUnitModeOn ? ":ON" : ":OFF"));

        adjustAllTextViewUnitGroupingBasedOnMultiUnitState(true);
    }

    /**
     *  If multi-unit mode is on, then the adjust grouping count up until an initial count.
     *  Otherwise, reduce group count to 1 and remove grouping symbols.
     */
    private void adjustAllTextViewUnitGroupingBasedOnMultiUnitState(boolean multiUnitStateChanged){
        formatAllTextViewUnitGroupings();

        if (multiModeMenuItem.isChecked()) {
            if(multiUnitStateChanged) { //If not enough groupings exist, then add some extra dummy ones for demonstration purposes, but only after the multi-unit state changes.
                adjustAllTextViewsUnitGroupings(DEMONSTRATION_GROUP_NUM_IN_MULTI_UNIT_MODE, DEMONSTRATION_GROUP_NUM_IN_MULTI_UNIT_MODE);
            }

            sourceValueExpressionToggleButton.setChecked(false); //Bracketed groupings are not recognized by the expression evaluator
        }
        else{
            //Reduce to one group and then remove grouping symbols
            adjustAllTextViewsUnitGroupings(1, 1);

            setSourceUnitsIntoQuantity();
            setTargetUnitIntoQuantity();
            if(checkUnits())
                checkNSetSourceValuesIntoQuantity();

            removeAllGroupingSymbols();
            removeAllUnknownUnitTokens();
        }
    }
    private void adjustAllTextViewsUnitGroupings(int desiredSourceUnitGroupingCount, int desiredTargetUnitGroupingCount){
        adjustTextViewGroupingsCount(sourceUnitsTextView, desiredSourceUnitGroupingCount, DUMMY_UNIT);
        adjustTextViewGroupingsCount(targetUnitsTextView, desiredTargetUnitGroupingCount, DUMMY_UNIT);
        adjustSourceValueTextViewGroupingsCountBaseOnSourceUnits();
    }
    private void removeAllGroupingSymbols(){
        String sourceUnitTextWithoutGrouping = quantityGroupingDefiner.removeGroupingSymbol(sourceUnitsTextView.getText().toString());
        sourceUnitsTextView.setText(sourceUnitTextWithoutGrouping);

        String targetUnitTextWithoutGrouping = quantityGroupingDefiner.removeGroupingSymbol(targetUnitsTextView.getText().toString());
        targetUnitsTextView.setText(targetUnitTextWithoutGrouping);

        String sourceValuesTextWithoutGrouping = quantityGroupingDefiner.removeGroupingSymbol(sourceValueTextView.getText().toString());
        sourceValueTextView.setText(sourceValuesTextWithoutGrouping );
    }
    private void formatAllTextViewUnitGroupings(){
        sourceUnitsTextView.setText(unitNamesGroupingFormatter.format(sourceUnitsTextView.getText().toString()));
        sourceValueTextView.setText(overallValuesGroupingFormatter.format(sourceValueTextView.getText().toString()));
        targetUnitsTextView.setText(unitNamesGroupingFormatter.format(targetUnitsTextView.getText().toString()));
    }
    private void removeAllUnknownUnitTokens(){
        try {
            sourceUnitsTextView.setText(sourceQuantity.getUnitNames().replace(Unit.UNKNOWN_UNIT_NAME, "").replace(DUMMY_UNIT, ""));
            sourceValueTextView.setText(sourceQuantity.getValuesString());
            targetUnitsTextView.setText(targetQuantity.getUnitNames().replace(Unit.UNKNOWN_UNIT_NAME, "").replace(DUMMY_UNIT, ""));
        } catch (SerializingException e) {
            e.printStackTrace();
        }
    }

    private void updateCurrency(MenuItem item){
        getSupportLoaderManager().initLoader(ONLINE_CURRENCY_UNITS_LOADER, null, this).forceLoad();
    }

    ///Setup UI Components Methods
    private void setupUIComponents() {
        unitManagerLoaderProgressBar = (ProgressBar) findViewById(R.id.unitManagerLoaderProgressBar);
        progressBarTextView = (TextView) findViewById(R.id.progressBarTextView);
        sourceValueExpressionToggleButton = (ToggleButton) findViewById(R.id.sourceValueExpressionToggleButton);

        setupButtons();
        setupTextViews();
        setupDialogs();
    }
    private void setupButtons(){
        sourceUnitBrowseButton = (Button) findViewById(R.id.sourceUnitBrowseButton);
        targetUnitBrowseButton = (Button) findViewById(R.id.targetUnitBrowseButton);
        convertButton = (Button) findViewById(R.id.convertButton);
        sourceUnitViewInfoButton = (Button) findViewById(R.id.sourceUnitViewDescButton);
        targetUnitViewInfoButton = (Button) findViewById(R.id.targetUnitViewDescButton);
    }
    private void setupTextViews(){
        sourceUnitsTextView = (MultiAutoCompleteTextView) findViewById(R.id.sourceUnitTextView);
        targetUnitsTextView = (MultiAutoCompleteTextView) findViewById(R.id.targetUnitTextView);

        sourceValueTextView = (TextView) findViewById(R.id.sourceValueTextView);
        sourceValueTextView.setText("1");

        conversionValueTextView = (TextView) findViewById(R.id.conversionOutputTextView);
        conversionValueTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
        conversionValueTextView.setTextIsSelectable(true);
    }
    private void setupDialogs(){
        dialogTextView = new TextView(MainActivity.this);
        dialogTextView.setLinksClickable(true);
        dialogTextView.setMovementMethod(LinkMovementMethod.getInstance());

        unitInfoDialog = new AlertDialog.Builder(MainActivity.this).create();
        unitInfoDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }
    private void populateTextViews(){
        try {
            sourceUnitsTextView.setText(sourceQuantity.getUnitNames());
            targetUnitsTextView.setText(targetQuantity.getUnitNames());

            sourceValueTextView.setText(sourceQuantity.getValuesString());
        }
        catch(Exception e){
            e.printStackTrace();
        }

        adjustAllTextViewUnitGroupingBasedOnMultiUnitState(false);
    }

    ///Setup Animation Methods
    private void setupAnimations() {
        convertButtonAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.conversion_button_animation);
    }

    ///Unit Manager Loading Methods
    private void initiateLoadUnitManagerComponent() {
        if (!pSharablesApplication.isUnitManagerPreReqLoadingComplete()) {

            getSupportLoaderManager().initLoader(LOCAL_PREFIXES_LOADER, null, this).forceLoad();
            getSupportLoaderManager().initLoader(FUND_UNITS_LOADER, null, this).forceLoad();
            getSupportLoaderManager().initLoader(ONLINE_CURRENCY_UNITS_LOADER, null, this).forceLoad();

            //Always load the standard core units first since they are few to load and they are most frequently used.
            getSupportLoaderManager().initLoader(STANDARD_CORE_LOCAL_UNITS_LOADER, null, this).forceLoad();

            //
            findViewById(R.id.progressBarLinearLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.sourceLinearLayout).setVisibility(View.INVISIBLE);
            findViewById(R.id.convertLinearLayout).setVisibility(View.INVISIBLE);
            findViewById(R.id.targetLinearLayout).setVisibility(View.INVISIBLE);
        }
    }
    private void postLoadUnitManagerComponentSetup() {
        findViewById(R.id.progressBarLinearLayout).setVisibility(View.GONE);
        findViewById(R.id.sourceLinearLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.convertLinearLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.targetLinearLayout).setVisibility(View.VISIBLE);

        unitManager = pSharablesApplication.getUnitManager();
        unitParser.setUnitManager(unitManager);

        setupTextViewAutoCompleteBasedOnUnitManagerComponents();
    }
    private void setupTextViewAutoCompleteBasedOnUnitManagerComponents(){
        DimensionComponentDefiner dimensionComponentDefiner = componentUnitsDimensionParser.getDimensionParserBuilder().getDimensionComponentDefiner();

        sourceUnitsTextViewArrayAdapter = new MultiAutoCompleteUnitsDefinitionArrayAdapter(this, android.R.layout.simple_list_item_1
                , unitManager.getUnitsDataModel().getUnitsContentQuerier()
                , dimensionComponentDefiner
                , unitManager.getConversionFavoritesDataModel()
                , unitManager.getPrefixesDataModel());
        sourceUnitsTextView.setAdapter(sourceUnitsTextViewArrayAdapter);
        sourceUnitsTextView.setThreshold(1);
        sourceUnitsTextView.setTokenizer(new MultiAutoCompleteUnitDefinitionTokenizer(dimensionComponentDefiner));

        targetUnitsTextViewArrayAdapter = new MultiAutoCompleteUnitsDefinitionArrayAdapter(this, android.R.layout.simple_list_item_1
                , unitManager.getUnitsDataModel().getUnitsContentQuerier()
                , dimensionComponentDefiner
                , unitManager.getConversionFavoritesDataModel()
                , unitManager.getPrefixesDataModel());
        targetUnitsTextView.setAdapter(targetUnitsTextViewArrayAdapter);
        sourceUnitsTextView.setThreshold(1);
        targetUnitsTextView.setTokenizer(new MultiAutoCompleteUnitDefinitionTokenizer(dimensionComponentDefiner));
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
                checkUnits();
                setSourceUnitsIntoQuantity();

                unitInfoDialog.setTitle(SOURCE_NAME + " Unit(s) Details");
                dialogTextView.setText(composeUnitsDetailsMessage(sourceQuantity.getUnits()));
                unitInfoDialog.setView(dialogTextView, 50, 50, 50, 50);
                unitInfoDialog.show();
            }
        });

        targetUnitViewInfoButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                checkUnits();
                setTargetUnitIntoQuantity();

                unitInfoDialog.setTitle(TARGET_NAME + " Unit(s) Details");
                dialogTextView.setText(composeUnitsDetailsMessage(targetQuantity.getUnits()));
                unitInfoDialog.setView(dialogTextView, 50, 50, 50, 50);
                unitInfoDialog.show();
            }
        });

        //TODO: Not entirely sure about this feature yet....
        sourceValueExpressionToggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //TODO: Make necessary preparation to use an expression evaluator. Some third party library, possibly, the JEL third party library
                if (multiModeMenuItem.isChecked()) {//Disable multi-unit group to prevent interference with expression evaluation
                    onOptionsItemSelected(multiModeMenuItem);
                    sourceValueTextView.setInputType(InputType.TYPE_CLASS_TEXT);
                } else {
                    sourceValueTextView.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL
                            | InputType.TYPE_NUMBER_FLAG_SIGNED);
                }
            }
        });

        convertButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkUnits() && checkNSetSourceValuesIntoQuantity())
                    processConversion();
            }
        });
    }

    private Spanned composeUnitsDetailsMessage(Collection<Unit> unitsGroup) {
        StringBuilder detailsBuilder = new StringBuilder();

        appendPerUnitInfo(detailsBuilder, unitsGroup);

        //Assumption is that all units in collection are of the same kind, therefore certain "type" info of the first unit also applies to the others.
        appendInvariantInfo(detailsBuilder, unitsGroup.iterator().next());

        return Html.fromHtml(detailsBuilder.toString());
    }
    private void appendPerUnitInfo(StringBuilder detailsBuilder, Collection<Unit> unitsGroup){
        for(Unit unit:unitsGroup){
            detailsBuilder.append("----------------");

            detailsBuilder.append("<p>").append("<b>NAME:</b> ").append(unit.getName()).append("(").append(unit.getAbbreviation()).append(")").append("</p>");
            if(!unit.getAliases().isEmpty())
                detailsBuilder.append("<p>").append("<b>ALIASES:</b> ").append(Arrays.toString(unit.getAliases().toArray())).append("</p>");
            if(!unit.getDescription().isEmpty())
                detailsBuilder.append("<p>").append("<b>DESCRIPTION:</b> ").append(unit.getDescription()).append("</p>");
            detailsBuilder.append("<p>").append("<b>UNIT SYSTEM:</b> ").append(unit.getUnitSystem()).append("</p>");

            String componentUnitsDimension = "";
            try {
                if(UnitsContentDeterminer.determineHighestPriorityDataModelCategory(unit) != DATA_MODEL_CATEGORY.UNKNOWN
                        && unit.getDimensionType() != DIMENSION_TYPE.SIMPLE)
                {
                    componentUnitsDimension = componentUnitsDimensionSerializer.serialize(unit.getComponentUnitsDimension());
                }
            } catch (SerializingException e) { }

            if(!componentUnitsDimension.isEmpty())
                detailsBuilder.append("<p>").append("<b>COMPONENT UNITS DIMENSION:</b> ").append(componentUnitsDimension).append("</p>");
        }
    }
    private void appendInvariantInfo(StringBuilder detailsBuilder, Unit groupRepresentativeUnit){
        String fundamentalTypesDimension = "";
        try {
            if(UnitsContentDeterminer.determineHighestPriorityDataModelCategory(groupRepresentativeUnit) != DATA_MODEL_CATEGORY.UNKNOWN)
                fundamentalTypesDimension =fundamentalUnitTypesDimensionSerializer.serialize(groupRepresentativeUnit.getFundamentalUnitTypesDimension());
        } catch (SerializingException e) { }

        detailsBuilder.append("<p>-----------------------------------------</p>");
        if(!fundamentalTypesDimension.isEmpty())
            detailsBuilder.append("<p>").append("<b>FUNDAMENTAL TYPES DIMENSION:</b> ").append(fundamentalTypesDimension).append("</p>");
        if(!groupRepresentativeUnit.getCategory().equalsIgnoreCase(fundamentalTypesDimension)) //Prevent display of duplicate data in case of complex units
            detailsBuilder.append("<p>").append("<b>CATEGORY:</b> ").append(groupRepresentativeUnit.getCategory()).append("</p>");
    }

    private void setListenersOnTextViews() {
        sourceUnitsTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //removeAllUnknownUnitTokens();
                //if(!multiModeMenuItem.isChecked())
                    //removeAllGroupingSymbols();
            }
        });
        sourceUnitsTextView.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    try {
                        setSourceUnitsIntoQuantity();
                        checkUnits();

                        if (multiModeMenuItem.isChecked()) {
                            String sourceUnitsText = unitNamesGroupingFormatter.format((((TextView) view).getText().toString()));
                            ((TextView) view).setText(sourceUnitsText);

                            adjustSourceValueTextViewGroupingsCountBaseOnSourceUnits();
                        }

                        checkNSetSourceValuesIntoQuantity();
                    }
                    catch(Exception e){
                        sourceUnitsTextView.setTextColor(Color.rgb(180, 0, 0));
                    }
                }
                else{
                    //removeAllUnknownUnitTokens();
                    if(!multiModeMenuItem.isChecked())
                        removeAllGroupingSymbols();
                }
            }
        });

        targetUnitsTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //removeAllUnknownUnitTokens();
                //if(!multiModeMenuItem.isChecked())
                    //removeAllGroupingSymbols();
            }
        });
        targetUnitsTextView.setOnFocusChangeListener(new OnFocusChangeListener() {
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
                        targetUnitsTextView.setTextColor(Color.rgb(180, 0, 0));
                    }
                }
                else{
                    //removeAllUnknownUnitTokens();
                    if(!multiModeMenuItem.isChecked())
                        removeAllGroupingSymbols();
                }
            }
        });

        sourceValueTextView.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    if (multiModeMenuItem.isChecked()) {
                        String sourceValueText = overallValuesGroupingFormatter.format(((TextView) view).getText().toString());
                        ((TextView) view).setText(sourceValueText);

                        //if(valuesGroupingFormatter.calculateGroupingCount(sourceValueText) > unitNamesGroupingFormatter.calculateGroupingCount(sourceUnitsTextView.getText().toString()))
                            //adjustSourceUnitTextViewGroupingsCountBasedOnSourceValue();
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
            String textWithAdjustedGroupingCount = generalGroupingFormatter.adjustGroupingsCount(initialText, referenceGroupCount, GroupingFormatter.GROUP_ADJUSTMENT_LOCATION.END);
            String adjustedTextWithReplacedEmptyGroupings = generalGroupingFormatter.replaceEmptyGroupingsWithDefaultGrouping(textWithAdjustedGroupingCount, dummyTextForEmptyGroupings);

            textViewToBeAdjusted.setText(adjustedTextWithReplacedEmptyGroupings);

            return true;
        }
        return false;
    }
    private boolean adjustSourceValueTextViewGroupingsCountBaseOnSourceUnits() {
        //int sourceUnitGroupingCount = unitNamesGroupingFormatter.calculateGroupingCount(sourceUnitsTextView.getText().toString());
        int sourceUnitGroupingCount = sourceQuantity.getUnits().size();
        return adjustTextViewGroupingsCount(sourceValueTextView, sourceUnitGroupingCount == 0 ? 1 : sourceUnitGroupingCount, DUMMY_VALUE);
    }
    private boolean adjustSourceUnitTextViewGroupingsCountBasedOnSourceValue() {
        int valuesGroupingCount = valuesGroupingFormatter.calculateGroupingCount(sourceValueTextView.getText().toString());
        return adjustTextViewGroupingsCount(sourceUnitsTextView, valuesGroupingCount == 0 ? 1 : valuesGroupingCount, DUMMY_UNIT);
    }

    ///Quantity Setting and Validation Methods
    private void setSourceUnitsIntoQuantity() {
        setUnitsIntoQuantity(false);
    }
    private void setTargetUnitIntoQuantity(){
        setUnitsIntoQuantity(true);
    }
    private void setUnitsIntoQuantity(boolean isTargetQuantity) {
        String unitNamesOrDimension = (isTargetQuantity ? targetUnitsTextView : sourceUnitsTextView).getText().toString().trim();
        Quantity selectedQuantity = (isTargetQuantity) ? targetQuantity : sourceQuantity;
        boolean valueSettingFailed = false;

        try {
            //Only replace anything only if there is difference
            if (!(quantityGroupingDefiner.removeGroupingSymbol(selectedQuantity.getUnitNames())
                    .equalsIgnoreCase(quantityGroupingDefiner.removeGroupingSymbol(unitNamesOrDimension))) )
            {
                if (unitNamesOrDimension.isEmpty()) {
                    selectedQuantity.setUnits(Collections.singletonList(unitManager.getUnitsDataModel().getUnitsContentMainRetriever().getUnknownUnit()));
                } else {
                    selectedQuantity.setUnits(retrieveUnitsList(unitNamesOrDimension));
                }
            }

            //Update conversion favorites rank
            for (Unit unit : (isTargetQuantity ? targetQuantity.getUnits() : sourceQuantity.getUnits())) {
                unitManager.getConversionFavoritesDataModel().modifySignificanceRankOfMultipleConversions(unit, true);
            }
        } catch (Exception exception){
            exception.printStackTrace();
        }

        if(valueSettingFailed){
            try {
                Unit unknownUnit = pSharablesApplication.getUnitManager().getUnitsDataModel().getUnitsContentMainRetriever().getUnknownUnit();
                if (isTargetQuantity) {
                    pSharablesApplication.getTargetQuantity().setUnit(unknownUnit, true);
                    targetUnitsTextView.setTextColor(Color.rgb(180, 0, 0));
                    createUnknownUnitsInformationToast("Please fix following target units: ", sourceUnitsTextView.getText().toString());
                } else {
                    pSharablesApplication.getSourceQuantity().setUnit(unknownUnit, true);
                    sourceUnitsTextView.setTextColor(Color.rgb(180, 0, 0));
                    createUnknownUnitsInformationToast("Please fix following target units: ", targetUnitsTextView.getText().toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private List<Unit> retrieveUnitsList(String unitNamesOrDimension){
        String formattedUnitNamesOrDimension = unitNamesGroupingFormatter.format(unitNamesOrDimension);
        return serialGroupingQuantityTokenizer.parseSerialGroupingToUnitsList(formattedUnitNamesOrDimension);
    }

    private boolean checkNSetSourceValuesIntoQuantity() {
        try {
            String formattedSourceValuesText = overallValuesGroupingFormatter.format(sourceValueTextView.getText().toString());

            sourceQuantity.setValues(serialGroupingQuantityTokenizer.parseSerialGroupingToValuesList(formattedSourceValuesText));
            sourceValueTextView.setTextColor(Color.BLACK);

            return true;

        }catch(Exception e){
            sourceValueTextView.setTextColor(Color.rgb(180, 0, 0));
            setConversionButton(false);
            return false;
        }
    }
    private boolean checkUnits() {
        boolean sourceUnitIsUnknown = UnitsContentDeterminer.determineHighestPriorityDataModelCategory(sourceQuantity.getLargestUnit()) == DATA_MODEL_CATEGORY.UNKNOWN;
        boolean targetUnitIsUnknown = UnitsContentDeterminer.determineHighestPriorityDataModelCategory(targetQuantity.getLargestUnit()) == DATA_MODEL_CATEGORY.UNKNOWN;
        boolean UnitsMatch = QuantityOperators.equalsUnitDimensionOf(sourceQuantity, targetQuantity);
        boolean UnitsAreOK = !(sourceUnitIsUnknown || targetUnitIsUnknown) && UnitsMatch;

        sourceUnitsTextView.setTextColor(Color.rgb(sourceUnitIsUnknown ? 180 : 0, 0, 0));
        targetUnitsTextView.setTextColor(Color.rgb(targetUnitIsUnknown ? 180 : 0, 0, 0));
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

            if(sourceUnitIsUnknown)
                createUnknownUnitsInformationToast("Please fix following source units: ", sourceUnitsTextView.getText().toString());

            if(targetUnitIsUnknown)
                createUnknownUnitsInformationToast("Please fix following target units: ", targetUnitsTextView.getText().toString());
        }

        return UnitsAreOK;
    }
    private void createUnknownUnitsInformationToast(String contextualPrefixText, String unitsGroupText){
        StringBuilder unknownSourceUnitsInformationBuilder = new StringBuilder(contextualPrefixText);

        List<String> unknownSourceUnitNames = determineUnknownUnitsInGroup(unitsGroupText);
        for(String unknownUnitName:unknownSourceUnitNames){
            String formattedUnknownUnitName = String.format("%s%s%s", quantityGroupingDefiner.getGroupOpeningSymbol(), unknownUnitName, quantityGroupingDefiner.getGroupClosingSymbol());
            unknownSourceUnitsInformationBuilder.append(",").append(formattedUnknownUnitName);
        }

        Toast.makeText(this, unknownSourceUnitsInformationBuilder.toString(), Toast.LENGTH_SHORT);
    }
    private List<String> determineUnknownUnitsInGroup(String unitsGroupText){
        List<Unit> sourceUnits = retrieveUnitsList(unitsGroupText);

        List<String> unknownUnitNames = new ArrayList<>();
        for(Unit unit:sourceUnits){
            if(UnitsContentDeterminer.determineHighestPriorityDataModelCategory(unit) == DATA_MODEL_CATEGORY.UNKNOWN)
                unknownUnitNames.add(unit.getName());
        }

        return unknownUnitNames;
    }

    ///Conversion Methods
    private void processConversion() {
        try {
            setSourceUnitsIntoQuantity();
            setTargetUnitIntoQuantity();

            checkUnits();
            checkNSetSourceValuesIntoQuantity();

            populateTextViews();

            List<Unit> targetUnits = Collections.list(Collections.enumeration(targetQuantity.getUnits()));
            List<Double> values = new ArrayList<>(QuantityConverter.determineConversionQuantityToTargetUnitsGroup(sourceQuantity, targetUnits, true).getValues());

            targetQuantity.setValues(values);
            String formattedConversionValues = formatConversionValue(targetQuantity.getValuesString());

            conversionValueTextView.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.slide_in_left));
            conversionValueTextView.setText(formattedConversionValues);

            //
            setConversionButton(false);
        }
        catch (Exception e) {
            e.printStackTrace();
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

    private String formatConversionValue(String rawConversionValueGroupText){
        //TODO: Conditionally format value as a fraction or as scientific notation based on which takes less space.

        if(targetQuantity.getLargestUnit().getCategory().equalsIgnoreCase(CURRENCY_CATEGORY)) {
            return formatConversionValueAsCurrency(rawConversionValueGroupText);
        } else{
            StringBuilder formattedConversionValueTextBuilder = new StringBuilder();

            Collection<String> rawConversionValueTextCollection = serialGroupingQuantityTokenizer.parseSerialGroupingToValuesStringList(rawConversionValueGroupText);

            for(String rawIndividualConversionValueText:rawConversionValueTextCollection){
                double rawConversionValueNumber = Double.parseDouble(rawIndividualConversionValueText);
                int logarithmicSize =  rawConversionValueNumber != 0 ? (int)Math.log10(rawConversionValueNumber) : 0;
                int logarthmicSizeUpperThreshold = 6;

                if(Math.abs(logarithmicSize) < logarthmicSizeUpperThreshold) {
                    String conversionValueTextAsFraction = decimalToMixedFractionFormatter.format(rawIndividualConversionValueText);
                    String fractionPart = MixedFractionToDecimalFormatter.extractFractionPart(conversionValueTextAsFraction);
                    String numeratorPart = MixedFractionToDecimalFormatter.extractNumeratorOfFractionPart(fractionPart);
                    String denominatorPart = MixedFractionToDecimalFormatter.extractDenominatorOfFractionPart(fractionPart);

                    int denominatorUpperThreshold = 20;
                    if(Double.parseDouble(denominatorPart) < denominatorUpperThreshold && Double.parseDouble(numeratorPart) != 0){
                        formattedConversionValueTextBuilder.append(valuesGroupingFormatter.format(conversionValueTextAsFraction));
                    }else {
                        String conversionValueTextAsRoundedDecimal = formatConversionValueAsRoundedDecimal(rawIndividualConversionValueText, logarithmicSize);
                        formattedConversionValueTextBuilder.append(valuesGroupingFormatter.format(conversionValueTextAsRoundedDecimal));
                    }
                }else{
                    String formattedConversionValueAsScientificNotation = scientificNotationFormatter.format(rawIndividualConversionValueText);
                    formattedConversionValueTextBuilder.append(valuesGroupingFormatter.format(formattedConversionValueAsScientificNotation));
                }
            }

            return formattedConversionValueTextBuilder.toString();
        }
    }
    private String formatConversionValueAsCurrency(String rawIndividualConversionValueText){
        Locale guessedLocale = CurrencyFormatter.guessCurrencyLocaleBasedOnCode(targetQuantity.getLargestUnit().getAbbreviation());
        if(guessedLocale != null) {
            conversionCurrencyFormatter.setLocale(guessedLocale);
        }
        return conversionCurrencyFormatter.format(rawIndividualConversionValueText);
    }
    private String formatConversionValueAsRoundedDecimal(String rawIndividualConversionValueText, int logarithmicSize){
        int adjustedDecimalPlaces = (logarithmicSize < 0 ? Math.abs(logarithmicSize):0) + 4;
        conversionRoundingFormatter.setNumOfDecimalPlaces(adjustedDecimalPlaces);
        return conversionRoundingFormatter.format(rawIndividualConversionValueText);
    }

    ///Loader Manager Methods
    @Override
    public Loader<UnitManagerBuilder> onCreateLoader(int id, Bundle arg1) {
        switch (id) {
            case STANDARD_CORE_LOCAL_UNITS_LOADER:
                Toast.makeText(this, "Loading Fast Standard Core Local Units...", Toast.LENGTH_SHORT).show();
                return new StandardCoreUnitsMapXmlLocalReader(this, this.locale, this.componentUnitsDimensionSerializer, this.fundamentalUnitTypesDimensionSerializer, this.componentUnitsDimensionParser.getDimensionParserBuilder().getDimensionComponentDefiner());
            case NON_STANDARD_LOCAL_UNITS_LOADER:
                Toast.makeText(this, "Loading Regular Local Units...", Toast.LENGTH_LONG).show();
                return new NonStandardUnitsMapsXmlLocalReader(this, this.locale, this.componentUnitsDimensionSerializer, this.fundamentalUnitTypesDimensionSerializer, this.componentUnitsDimensionParser.getDimensionParserBuilder().getDimensionComponentDefiner());
            case LOCAL_PREFIXES_LOADER:
                Toast.makeText(this, "Loading Local Prefixes...", Toast.LENGTH_SHORT).show();
                return new PrefixesMapXmlLocalReader(this, new PrefixesDataModel(new PrefixesDualKeyNCategoryHashedRepository()));
            case FUND_UNITS_LOADER:
                Toast.makeText(this, "Loading Fundamental Unit Mappings...", Toast.LENGTH_SHORT).show();
                return new FundamentalUnitsMapXmlLocalReader(this, new FundamentalUnitsDataModel(new FundamentalUnitsHashDualKeyNCategoryHashedRepository()));
            case ONLINE_CURRENCY_UNITS_LOADER:
                Toast.makeText(this, "Loading Online Currency Units From European Central Bank...", Toast.LENGTH_SHORT).show();
                return new EuropeanCentralBankCurrencyUnitsMapXmlOnlineReader(this, this.locale, this.componentUnitsDimensionSerializer
                        , this.fundamentalUnitTypesDimensionSerializer, this.componentUnitsDimensionParser.getDimensionParserBuilder().getDimensionComponentDefiner()
                        , new ChainedFormatter(locale).AddFormatter(new GeneralTextFormatter(locale)).AddFormatter(new SnakeCaseFormatter(locale)));
            case ONLINE_PREFIXES_N_UNITS_LOADER:
                try {
                    Toast.makeText(this, "Loading Online Units From \"Unified Code for Units of Measure\"...", Toast.LENGTH_LONG).show();
                    return new PrefixesNUnitsMapXmlOnlineReader(this, this.locale, new PrefixesDataModel(new PrefixesDualKeyNCategoryHashedRepository()), this.componentUnitsDimensionSerializer, this.fundamentalUnitTypesDimensionSerializer, this.componentUnitsDimensionParser.getDimensionParserBuilder().getDimensionComponentDefiner());
                } catch (ParsingException e) {
                    e.printStackTrace();
                }
            case FAVORITES_LOADER:
                Toast.makeText(this, "Loading Conversion Favorites...", Toast.LENGTH_SHORT).show();
                return new AsyncTaskLoader<UnitManagerBuilder>(this) {
                    @Override
                    public UnitManagerBuilder loadInBackground() {
                        ConversionFavoritesDataModel conversionFavoritesDataModel = new ConversionFavoritesDataModel(new ConversionFavoritesDualKeyNCategoryHashedRepository());
                        conversionFavoritesDataModel.setSignificanceRankRepository(new SignificanceRankHashedRepository());

                        ConversionFavoritesDataModel loadedConversionFavoritesDataModel = new ConversionFavoritesListXmlLocalReader(this.getContext(), conversionFavoritesDataModel).loadInBackground();
                        if(loadedConversionFavoritesDataModel != null)
                            ((PersistentSharablesApplication) getContext().getApplicationContext()).getUnitManager().getConversionFavoritesDataModel().combineWith(loadedConversionFavoritesDataModel);
                        return null;
                    }
                };
            case POST_LOADER:
                Toast.makeText(this, "Cool. Now Putting Things Together...", Toast.LENGTH_SHORT).show();
                return new AsyncTaskLoader<UnitManagerBuilder>(this) {
                    @Override
                    public UnitManagerBuilder loadInBackground() {
                        ((PersistentSharablesApplication) getContext().getApplicationContext()).createUnitManager();
                        return null;
                    }
                };
            case UPDATE_LOADER:
                Toast.makeText(this, "Updating Units...", Toast.LENGTH_SHORT).show();
                pSharablesApplication.getUnitManagerBuilder().initializeAllStructuralDefaults();
                pSharablesApplication.getUnitManagerBuilder().clearAllUnits();
                return new AsyncTaskLoader<UnitManagerBuilder>(this) {
                    @Override
                    public UnitManagerBuilder loadInBackground() {
                        ((PersistentSharablesApplication) getContext().getApplicationContext()).updateUnitManager();
                        return null;
                    }
                };
            default:
                return null;
        }
    }
    @Override
    public void onLoadFinished(Loader<UnitManagerBuilder> loader, UnitManagerBuilder loadedUnitManagerBuilderBundle) {
        try {
            pSharablesApplication.getUnitManagerBuilder().combineWith(loadedUnitManagerBuilderBundle);
        }
        catch (Exception e){
            Toast.makeText(this, "All components needed for initialization may not have successfully been assembled.", Toast.LENGTH_SHORT).show();
        }

        //
        if(loader.getId() == ONLINE_PREFIXES_N_UNITS_LOADER &&  loadedUnitManagerBuilderBundle.areAnyComponentsAvailable()){
            pSharablesApplication.setOnlineUnitsCurrentlyLoaded(true);
        }
        if(loader.getId() == ONLINE_CURRENCY_UNITS_LOADER && loadedUnitManagerBuilderBundle.areAnyComponentsAvailable()){
            currencyUnitsLoaded = true;
        }
        if(loader.getId() == NON_STANDARD_LOCAL_UNITS_LOADER && loadedUnitManagerBuilderBundle.areAnyComponentsAvailable()){
            nonStandardLocalUnitsLoaded = true;
        }
        if(loader.getId() == STANDARD_CORE_LOCAL_UNITS_LOADER && loadedUnitManagerBuilderBundle.areAnyComponentsAvailable()){
            standardLocalCoreUnitsLoaded = true;
        }
        if(loader.getId() == LOCAL_PREFIXES_LOADER && loadedUnitManagerBuilderBundle.areAnyComponentsAvailable()){
            localPrefixesLoaded = true;
        }
        if(loader.getId() == FAVORITES_LOADER ){
            favoritesLoaded = true;
        }

        //
        String loaderUpdateName;
        switch (loader.getId()){
            case ONLINE_CURRENCY_UNITS_LOADER:
                loaderUpdateName = "Currency Units";
                break;
            case ONLINE_PREFIXES_N_UNITS_LOADER:
                loaderUpdateName = "Online Units";
                break;
            case STANDARD_CORE_LOCAL_UNITS_LOADER:
            case NON_STANDARD_LOCAL_UNITS_LOADER:
                loaderUpdateName = "Local Units";
                break;
            default:
                loaderUpdateName = "";
        }

        //
        if (pSharablesApplication.isUnitManagerPreReqLoadingComplete() && pSharablesApplication.isUnitManagerAlreadyCreated() && loader.getId() == POST_LOADER) {
            postLoadUnitManagerComponentSetup();
            getSupportLoaderManager().initLoader(FAVORITES_LOADER, null, this).forceLoad();
            if(!onlineUnitsPreviouslySaved){
                getSupportLoaderManager().initLoader(ONLINE_PREFIXES_N_UNITS_LOADER, null, this).forceLoad();
            }
            else{
                //If the online units had been previously loaded AND saved, then they will be in this local loaded units set.
                //Depending pending on how many online units and dynamic units were saved, this may take a while to fully load
                getSupportLoaderManager().initLoader(NON_STANDARD_LOCAL_UNITS_LOADER, null, this).forceLoad();
            }
        }
        else if( standardLocalCoreUnitsLoaded && currencyUnitsLoaded && localPrefixesLoaded
                && pSharablesApplication.isUnitManagerPreReqLoadingComplete() && !pSharablesApplication.isUnitManagerAlreadyCreated()){
            getSupportLoaderManager().initLoader(POST_LOADER, null, this).forceLoad();
        }
        else if(pSharablesApplication.isUnitManagerAlreadyCreated() && loader.getId() != UPDATE_LOADER){
            getSupportLoaderManager().initLoader(UPDATE_LOADER, null, this).forceLoad();
        }

        if(!loaderUpdateName.isEmpty()){
            Toast.makeText(this, String.format("%s Updated.", loaderUpdateName), Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onLoaderReset(Loader<UnitManagerBuilder> arg0) { }
}
