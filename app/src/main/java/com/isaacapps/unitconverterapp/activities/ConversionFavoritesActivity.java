package com.isaacapps.unitconverterapp.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import com.isaacapps.unitconverterapp.adapters.ConversionFavoritesExpandableListAdapter;
import com.isaacapps.unitconverterapp.adapters.ConversionFavoritesRegularListAdapter;
import com.isaacapps.unitconverterapp.models.measurables.quantity.QuantityException;
import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;
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
import java.util.List;
import java.util.Locale;

public class ConversionFavoritesActivity extends FragmentActivity{
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

    private final int BY_RANK = 1, BY_CATEGORY = 2, BY_ALPHABETICAL = 3; //Unfortunately enums are not recommended on android

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
                setListMode(BY_CATEGORY);
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
        if (selectedConversion != null
                || !pSharablesApplication.getUnitManager().getConversionFavoritesDataModel().getAllFormattedConversions().isEmpty()) {
            selectButton.setVisibility(View.VISIBLE);
            selectButton.setAnimation(AnimationUtils.loadAnimation(ConversionFavoritesActivity.this, android.R.anim.slide_in_left));
        } else {
            selectButton.setVisibility(View.GONE);
            selectButton.setAnimation(AnimationUtils.loadAnimation(ConversionFavoritesActivity.this, android.R.anim.fade_out));
        }
    }
    private void showRemoveButtonIfAppropriate() {
        if (selectedConversion != null
                || !pSharablesApplication.getUnitManager().getConversionFavoritesDataModel().getAllFormattedConversions().isEmpty()) {
            removeButton.setVisibility(View.VISIBLE);
            removeButton.setAnimation(AnimationUtils.loadAnimation(ConversionFavoritesActivity.this, android.R.anim.fade_in));
        } else {
            removeButton.setVisibility(View.GONE);
            removeButton.setAnimation(AnimationUtils.loadAnimation(ConversionFavoritesActivity.this, android.R.anim.slide_out_right));
        }
    }
    
    ///Button Listeners Methods
    private void addListenerOnConversionFavoritesButtons() {
        selectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean hasMultipleUnits = false;
                try {
                    String sourceUnitNames = pSharablesApplication.getUnitManager().getConversionFavoritesDataModel()
                            .getSourceUnitNameFromConversion(selectedConversion);
                    List<Unit> sourceUnits = serialGroupingQuantityTokenizer.parseSerialGroupingToUnitsList(sourceUnitNames);
                    pSharablesApplication.getSourceQuantity().setUnits(sourceUnits);

                    String targetUnitNames = pSharablesApplication.getUnitManager().getConversionFavoritesDataModel()
                            .getTargetUnitNameFromConversion(selectedConversion);
                    List<Unit> targetUnits = serialGroupingQuantityTokenizer.parseSerialGroupingToUnitsList(targetUnitNames);
                    pSharablesApplication.getTargetQuantity().setUnits(targetUnits);

                    pSharablesApplication.getUnitManager().getConversionFavoritesDataModel()
                            .modifySignificanceRankOfMultipleConversions(ConversionFavoritesDataModel.parseUnitCategoryFromConversion(selectedConversion), true);


                    hasMultipleUnits = sourceUnits.size()>1 || targetUnits.size()>1;
                } catch (QuantityException e) {
                    e.printStackTrace();
                }
                finally {
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    i.putExtra(PersistentSharablesApplication.MULTI_UNIT_MODE_BUNDLE_NAME, hasMultipleUnits);
                    startActivity(i);

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
                    selectedConversion = null;

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

                alterExpandableListSelection(((ViewGroup)childView).getChildAt(childIndex));

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
        currentChildView.setBackgroundColor(Color.CYAN);
        setExpandableListSelection(currentChildView);
    }
    private void setExpandableListSelection(View currentChildView) {
        selectedConversion = ((TextView)currentChildView).getText().toString().toLowerCase();

        //ensures that only one item at a time can be selected, by deselecting a previously selected alternate position.
        if(pastChildViewSelection != null && pastChildViewSelection != currentChildView)
            pastChildViewSelection.setBackgroundColor(Color.TRANSPARENT);

        pastChildViewSelection = currentChildView;
    }

    private void alterRegularListSelection(AdapterView<?> listView, int currentSelectedConversionPosition) {
        listView.getChildAt(currentSelectedConversionPosition).setBackgroundColor(Color.CYAN);
        setRegularListSelection(listView, currentSelectedConversionPosition);
    }
    private void setRegularListSelection(AdapterView<?> listView, int currentSelectedConversionPosition) {
        selectedConversion = ((String) listView.getItemAtPosition(currentSelectedConversionPosition)).toLowerCase();

        //ensures that only one item at a time can be selected, by deselecting a previously selected alternate position.
        if (pastSelectedConversionPosition != -1 && pastSelectedConversionPosition != currentSelectedConversionPosition)
            listView.getChildAt(pastSelectedConversionPosition).setBackgroundColor(Color.TRANSPARENT);

        pastSelectedConversionPosition = currentSelectedConversionPosition;
    }

    ///
    private void repopulateCurrentList() {
        if(canCurrentListBeUpdated()) {
            switch (listMode) {
                case BY_CATEGORY:
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
            case BY_CATEGORY:
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
            case BY_CATEGORY:
                return conversionsExpandableListAdapter.getGroupCount() != 0;
            case BY_RANK:
            case BY_ALPHABETICAL:
                return conversionsRegularListAdapter.getCount() != 0;
            default:
                return false;
        }
    }
}
