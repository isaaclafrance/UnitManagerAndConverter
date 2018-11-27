package com.isaacapps.unitconverterapp.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.ConversionFavoritesDataModel;
import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.isaacapps.unitconverterapp.activities.MainActivity.SOURCE_NAME;
import static com.isaacapps.unitconverterapp.activities.MainActivity.TARGET_NAME;

public class ConversionFavoritesRegularListAdapter extends ArrayAdapter<String> {
    private Context context;
    private IFormatter itemFormatter;
    private ConversionFavoritesDataModel conversionFavoritesDataModel;
    private Comparator<String> alphabeticalComparator;

    public ConversionFavoritesRegularListAdapter(Context context, int resource, ConversionFavoritesDataModel conversionFavoritesDataModel, IFormatter itemFormatter) {
        super(context, resource, new ArrayList<>(Collections.singleton("Loading ... Please Wait ...")));

        this.context = context;
        this.itemFormatter = itemFormatter;
        this.conversionFavoritesDataModel = conversionFavoritesDataModel;

        alphabeticalComparator = new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareTo(rhs);
            }
        };
    }

    @Override
    public String getItem(int position){
        return itemFormatter.format(super.getItem(position));
    }

    public void refreshUsingBackingDataModel(){
        if(getCount()>0)
            clear();

        if(conversionFavoritesDataModel.getAllFormattedConversions().isEmpty()){
            add(String.format("No Saved Conversion Favorites Available. Please Add Some By Navigating to the Home Screen, choosing a '%s' and '%s' unit, then pressing the 'Add Fav' menu button.", SOURCE_NAME, TARGET_NAME));
        }
        else {
            addAll(conversionFavoritesDataModel.getAllFormattedConversions());
        }
    }

    public void sortAlphabetically(){
        sort(alphabeticalComparator);
    }
}
