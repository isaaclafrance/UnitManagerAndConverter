package com.isaacapps.unitconverterapp.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import com.isaacapps.unitconverterapp.adapters.ConversionFavoritesExpandableListAdapter;
import com.isaacapps.unitconverterapp.adapters.ConversionFavoritesRegularListAdapter;
import com.isaacapps.unitconverterapp.dao.xml.readers.local.ConversionFavoritesListXmlLocalReader;
import com.isaacapps.unitconverterapp.models.measurables.quantity.QuantityException;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.ConversionFavoritesDataModel;
import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.grouping.UnitNamesGroupingFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.grouping.ValuesGroupingFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.text.AllUpperCaseFormatter;
import com.isaacapps.unitconverterapp.processors.formatters.text.StartCaseFormatter;
import com.isaacapps.unitconverterapp.processors.parsers.ParsingException;
import com.isaacapps.unitconverterapp.processors.parsers.dimension.componentunits.ComponentUnitsDimensionParser;
import com.isaacapps.unitconverterapp.processors.parsers.measurables.quantity.QuantityGroupingDefiner;
import com.isaacapps.unitconverterapp.processors.parsers.measurables.quantity.tokeniser.SerialGroupingQuantityTokenizer;
import com.isaacapps.unitconverterapp.processors.parsers.measurables.unit.UnitParser;

import java.util.Comparator;
import java.util.Locale;

public class ConversionFavoritesActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<ConversionFavoritesDataModel> {
    private PersistentSharablesApplication pSharablesApplication;

    private ExpandableListView conversionsExpandableListView;
    private ListView conversionsRegularListView;
    private Button selectButton, removeButton, cancelButton;

    private ConversionFavoritesRegularListAdapter conversionsRegularListAdapter;
    private ConversionFavoritesExpandableListAdapter conversionsExpandableListAdapter;
    
    private String selectedConversion;
    private View pastChildViewSelection;
    private int pastSelectedConversionPosition;

    private Locale locale;
    private UnitParser unitParser;
    private QuantityGroupingDefiner quantityGroupingDefiner;
    private ComponentUnitsDimensionParser componentUnitsDimensionParser;
    private SerialGroupingQuantityTokenizer serialGroupingQuantityTokenizer;

    private UnitNamesGroupingFormatter unitNamesGroupingFormatter;
    private ValuesGroupingFormatter valuesGroupingFormatter;
    private IFormatter groupFormatter;
    private IFormatter conversionFormatter;

    private Comparator<String> conversionRankComparator;

    private final int FAVORITES_LOADER = 1;
    private final int BY_RANK = 1, BY_CATGEORY = 2, BY_ALPHABETICAL = 3; //Unfortunately enums are not recommended on android

    private int listMode;

    ///
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversion_favorites);

        pSharablesApplication = (PersistentSharablesApplication) this.getApplication();

        locale = pSharablesApplication.getResources().getConfiguration().locale;

        try {
            quantityGroupingDefiner = new QuantityGroupingDefiner();
            unitNamesGroupingFormatter = new UnitNamesGroupingFormatter(locale, quantityGroupingDefiner);
            valuesGroupingFormatter = new ValuesGroupingFormatter(locale, quantityGroupingDefiner, 1.0);

            groupFormatter = new AllUpperCaseFormatter(locale);
            conversionFormatter = new StartCaseFormatter(locale);

            componentUnitsDimensionParser = new ComponentUnitsDimensionParser();
        } catch (ParsingException e1) {
            e1.printStackTrace();
        }
        unitParser = new UnitParser(componentUnitsDimensionParser);
        unitParser.setUnitManager(pSharablesApplication.getUnitManager());
        serialGroupingQuantityTokenizer = new SerialGroupingQuantityTokenizer(locale, unitParser, quantityGroupingDefiner, valuesGroupingFormatter, unitNamesGroupingFormatter);

        pastSelectedConversionPosition = -1;

        conversionRankComparator = new Comparator<String>() {
            @Override
            public int compare(String lhsConversion, String rhsConversion) {
                int lhsConversionRank = pSharablesApplication.getUnitManager().getConversionFavoritesDataModel().getSignificanceRankOfConversion(lhsConversion);
                int rhsConversionRank = pSharablesApplication.getUnitManager().getConversionFavoritesDataModel().getSignificanceRankOfConversion(rhsConversion);

                return -1 * Integer.compare(lhsConversionRank, rhsConversionRank);
            }
        };

        setupUIComponents();
        addListenerOnConversionFavoritesButtons();
        addListenerOnConversionListView();

        setListMode(BY_RANK);
        if (pSharablesApplication.getUnitManager().getConversionFavoritesDataModel().getAllFormattedConversions().isEmpty())
        {
            loadConversionFavorites();
        }
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        if(hasFocus){
            repopulateCurrentList();
        }
    }

    ///Menu Methods
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.conversionfavorites, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.by_category:
                setListMode(BY_CATGEORY);
                break;
            case R.id.by_rank_item:
                setListMode(BY_RANK);
                break;
            case R.id.by_alphabetical_item:
                setListMode(BY_ALPHABETICAL);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        repopulateCurrentList();
        return true;
    }

    ///Setup UI Components Methods
    private void setupUIComponents() {
        setupRegularListView();
        setupExpandableListView();

        selectButton = (Button) findViewById(R.id.selectConversionButton);
        removeButton = (Button) findViewById(R.id.removeConversionButton);
        cancelButton = (Button) findViewById(R.id.cancelConversionButton);

        selectButton.setVisibility(View.GONE);
        removeButton.setVisibility(View.GONE);
    }
    private void setupRegularListView() {
        conversionsRegularListView = (ListView) findViewById(R.id.conversionListView);
        conversionsRegularListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        conversionsRegularListAdapter = new ConversionFavoritesRegularListAdapter(this, android.R.layout.simple_list_item_1, pSharablesApplication.getUnitManager().getConversionFavoritesDataModel(), conversionFormatter);
        conversionsRegularListView.setAdapter(conversionsRegularListAdapter);
    }
    private void setupExpandableListView() {
        conversionsExpandableListView = (ExpandableListView) findViewById(R.id.conversionExpandableListView);
        conversionsExpandableListView.setChoiceMode(ExpandableListView.CHOICE_MODE_SINGLE);
        conversionsExpandableListAdapter = new ConversionFavoritesExpandableListAdapter(this, pSharablesApplication.getUnitManager().getConversionFavoritesDataModel()
                , conversionRankComparator, groupFormatter, conversionFormatter);
        conversionsExpandableListView.setAdapter(conversionsExpandableListAdapter);
    }
    
    private void showSelectButtonIfAppropriate() {
        if (pastSelectedConversionPosition != -1 || pastChildViewSelection != null
                || !pSharablesApplication.getUnitManager().getConversionFavoritesDataModel().getAllFormattedConversions().isEmpty()) {
            selectButton.setVisibility(View.VISIBLE);
            selectButton.setAnimation(AnimationUtils.loadAnimation(ConversionFavoritesActivity.this, android.R.anim.slide_in_left));
        } else {
            selectButton.setVisibility(View.GONE);
            selectButton.setAnimation(AnimationUtils.loadAnimation(ConversionFavoritesActivity.this, android.R.anim.fade_out));
        }
    }
    private void showRemoveButtonIfAppropriate() {
        if (pastSelectedConversionPosition != -1 || pastChildViewSelection != null
                || !pSharablesApplication.getUnitManager().getConversionFavoritesDataModel().getAllFormattedConversions().isEmpty()) {
            removeButton.setVisibility(View.VISIBLE);
            removeButton.setAnimation(AnimationUtils.loadAnimation(ConversionFavoritesActivity.this, android.R.anim.fade_in));
        } else {
            removeButton.setVisibility(View.GONE);
            removeButton.setAnimation(AnimationUtils.loadAnimation(ConversionFavoritesActivity.this, android.R.anim.slide_out_right));
        }
    }

    ///Data Loading Methods
    private void loadConversionFavorites() {
        getSupportLoaderManager().initLoader(FAVORITES_LOADER, null, this).forceLoad();
    }

    ///Button Listeners Methods
    private void addListenerOnConversionFavoritesButtons() {
        selectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String sourceUnits = pSharablesApplication.getUnitManager().getConversionFavoritesDataModel()
                            .getSourceUnitNameFromConversion(selectedConversion);
                    pSharablesApplication.getSourceQuantity().setUnits(serialGroupingQuantityTokenizer.parseSerialGroupingToUnitsList(sourceUnits));

                    String targetUnits = pSharablesApplication.getUnitManager().getConversionFavoritesDataModel()
                            .getTargetUnitNameFromConversion(selectedConversion);
                    pSharablesApplication.getTargetQuantity().setUnits(serialGroupingQuantityTokenizer.parseSerialGroupingToUnitsList(targetUnits));

                    pSharablesApplication.getUnitManager().getConversionFavoritesDataModel()
                            .modifySignificanceRankOfMultipleConversions(ConversionFavoritesDataModel.parseUnitCategoryFromConversion(selectedConversion), true);
                } catch (QuantityException e) {
                    e.printStackTrace();
                }
                finally {
                    finish();
                }
            }
        });

        removeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pSharablesApplication.getUnitManager().getConversionFavoritesDataModel()
                        .removeFormattedConversion(selectedConversion))
                {
                    pastSelectedConversionPosition = -1;
                    pastChildViewSelection = null;

                    repopulateCurrentList();

                    showSelectButtonIfAppropriate();
                    showRemoveButtonIfAppropriate();
                }
            }
        });

        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                pSharablesApplication.saveConversionFavorites();
                finish();
            }

        });
    }

    ///List View Listener Methods
    private void addListenerOnConversionListView() {
        conversionsExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View childView, int categoryIndex, int childIndex
                    , long arg3) {

                alterExpandableListSelection(childView);

                showSelectButtonIfAppropriate();
                showRemoveButtonIfAppropriate();

                return true;
            }
        });

        conversionsRegularListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View view, int currentSelectedConversionPosition,
                                    long arg) {

                alterRegularListSelection(listView, currentSelectedConversionPosition);

                showSelectButtonIfAppropriate();
                showRemoveButtonIfAppropriate();
            }
        });
    }

    ///
    private void alterExpandableListSelection(View currentChildView) {
        if(pastChildViewSelection != currentChildView) {
            currentChildView.setBackgroundColor(Color.CYAN);
        }
        else{
            pastChildViewSelection = null;
        }

        setExpandableListSelection(currentChildView);
    }
    private void setExpandableListSelection(View currentChildView) {
        selectedConversion = ((TextView)currentChildView).getText().toString().toLowerCase();

        //ensures that only one item at a time can be selected, by deselecting a previously selected alternate position.
        if(pastChildViewSelection != null)
            pastChildViewSelection.setBackgroundColor(Color.TRANSPARENT);

        pastChildViewSelection = currentChildView;
    }

    private void alterRegularListSelection(AdapterView<?> listView, int currentSelectedConversionPosition) {
        if (currentSelectedConversionPosition != pastSelectedConversionPosition) {
            setRegularListSelection(listView, (String) listView.getItemAtPosition(currentSelectedConversionPosition), currentSelectedConversionPosition);
            listView.getChildAt(currentSelectedConversionPosition).setBackgroundColor(Color.CYAN);
        } else if (pastSelectedConversionPosition == currentSelectedConversionPosition) {
            setRegularListSelection(listView, null, -1);
            listView.getChildAt(currentSelectedConversionPosition).setBackgroundColor(Color.TRANSPARENT);
        }
    }
    private void setRegularListSelection(AdapterView<?> listView, String formattedConversion, int currentSelectedConversionPosition) {
        selectedConversion = formattedConversion.toLowerCase();

        //ensures that only one item at a time can be selected, by deselecting a previously selected alternate position.
        if (pastSelectedConversionPosition != -1)
            listView.getChildAt(pastSelectedConversionPosition).setBackgroundColor(Color.TRANSPARENT);

        pastSelectedConversionPosition = currentSelectedConversionPosition;
    }

    ///
    private void repopulateCurrentList() {
        if(canCurrentListBeUpdated()) {
            switch (listMode) {
                case BY_CATGEORY:
                    conversionsExpandableListAdapter.refreshUsingBackingDataModel();
                    break;
                case BY_RANK:
                case BY_ALPHABETICAL:
                    conversionsRegularListAdapter.refreshUsingBackingDataModel();
                    break;
            }
        }
    }
    private void setListMode(int listMode){
        this.listMode = listMode;
        switch (listMode){
            case BY_CATGEORY:
                switchToExpandableList();
                break;
            case BY_RANK:
                conversionsRegularListAdapter.sort(conversionRankComparator);
                switchToRegularList();
                break;
            case BY_ALPHABETICAL:
                conversionsRegularListAdapter.sortAlphabetically();
                switchToRegularList();
                break;
        }
    }
    private void switchToRegularList(){
        conversionsRegularListView.setVisibility(View.VISIBLE);
        conversionsRegularListView.setSelection(0);
        conversionsRegularListView.setSelected(true);

        conversionsExpandableListView.setVisibility(View.GONE);
        pastChildViewSelection = null;
    }
    private void switchToExpandableList(){
        conversionsExpandableListView.setVisibility(View.VISIBLE);

        conversionsRegularListView.setVisibility(View.GONE);
        pastSelectedConversionPosition = -1;
    }
    private boolean canCurrentListBeUpdated(){
        switch (listMode){
            case BY_CATGEORY:
                return conversionsExpandableListAdapter.getGroupCount() != 0;
            case BY_RANK:
            case BY_ALPHABETICAL:
                return conversionsRegularListAdapter.getCount() != 0;
            default:
                return false;
        }
    }

    ///Loader Manager Overriden Methods
    @Override
    public Loader<ConversionFavoritesDataModel> onCreateLoader(int id, Bundle bundle) {
        if (id == FAVORITES_LOADER) {
            return new ConversionFavoritesListXmlLocalReader(this, new ConversionFavoritesDataModel());
        } else {
            return null;
        }
    }
    @Override
    public void onLoadFinished(Loader<ConversionFavoritesDataModel> loader, ConversionFavoritesDataModel conversionFavoritesDataModel) {
        if (conversionFavoritesDataModel != null) {
            pSharablesApplication.getUnitManager().getConversionFavoritesDataModel().combineWith(conversionFavoritesDataModel);
            repopulateCurrentList();
        }
    }
    @Override
    public void onLoaderReset(Loader<ConversionFavoritesDataModel> loader) {
    }
}
