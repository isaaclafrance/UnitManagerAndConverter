package com.isaacapps.unitconverterapp.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.isaacapps.unitconverterapp.activities.R;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.ConversionFavoritesDataModel;
import com.isaacapps.unitconverterapp.processors.formatters.IFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.isaacapps.unitconverterapp.activities.MainActivity.SOURCE_NAME;
import static com.isaacapps.unitconverterapp.activities.MainActivity.TARGET_NAME;

public class ConversionFavoritesExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private IFormatter childFormatter;
    private IFormatter groupFormatter;
    private ConversionFavoritesDataModel conversionFavoritesDataModel;
    private List<String> conversionCategoriesList;
    private Comparator<String> conversionRankComparator;

    public ConversionFavoritesExpandableListAdapter(Context context, ConversionFavoritesDataModel conversionFavoritesDataModel, Comparator<String> conversionRankComparator, IFormatter groupFormatter, IFormatter childFormatter){
        this.context = context;
        this.conversionFavoritesDataModel = conversionFavoritesDataModel;
        this.groupFormatter = groupFormatter;
        this.childFormatter = childFormatter;

        refreshUsingBackingDataModel();

        this.conversionRankComparator = conversionRankComparator;
    }

    ///
    public void refreshUsingBackingDataModel(){
        setUpConversionCategoriesList();
        notifyDataSetChanged();
    }
    private void setUpConversionCategoriesList(){
        conversionCategoriesList = new ArrayList<>(conversionFavoritesDataModel.getAllConversionCategories());

        if(conversionCategoriesList.isEmpty()){
            conversionCategoriesList.add(String.format("No Saved Conversion Favorites Available. Please Add Some By Navigating to the Home Screen, choosing a '%s' and '%s' unit, then pressing the 'Add Fav' menu button.", SOURCE_NAME, TARGET_NAME));;
        }
        else{
            Collections.sort(conversionCategoriesList);
        }
    }

    ///
    @Override
    public int getGroupCount() {
        return conversionFavoritesDataModel.getAllConversionCategories().size();
    }
    @Override
    public int getChildrenCount(int categoryIndex) {
        return conversionFavoritesDataModel.getFormattedConversionsAssociatedWithCategory(conversionCategoriesList.get(categoryIndex)).size();
    }

    @Override
    public Object getGroup(int categoryIndex) {
        return groupFormatter.format(conversionCategoriesList.get(categoryIndex));
    }
    @Override
    public Object getChild(int categoryIndex, int childIndex) {
        String selectedCategory = conversionCategoriesList.get(categoryIndex);

        List<String> conversionsWithCategoriesList = new ArrayList<>(conversionFavoritesDataModel.getFormattedConversionsAssociatedWithCategory(selectedCategory));
        Collections.sort(conversionsWithCategoriesList, conversionRankComparator);

        String selectedConversionWithCategory = conversionsWithCategoriesList.get(childIndex);
        String selectedConversionWithoutCategory = selectedConversionWithCategory.substring(selectedConversionWithCategory.indexOf(ConversionFavoritesDataModel.CATEGORY_DELIMITER)+1);

        return childFormatter.format(selectedConversionWithoutCategory);
    }

    @Override
    public long getGroupId(int categoryIndex) {
        return categoryIndex;
    }
    @Override
    public long getChildId(int categoryIndex, int childIndex) {
        return childIndex;
    }
    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int categoryIndex, boolean isExpanded, View groupView, ViewGroup viewGroup) {
        String selectedCategoryText = (String) getGroup(categoryIndex);
        if (groupView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            groupView = layoutInflater.inflate(R.layout.conversion_favorites_category, null);
        }

        TextView categoryHeaderTextView = (TextView) groupView.findViewById(R.id.conversion_faves_category_header);
        categoryHeaderTextView.setTypeface(null, Typeface.BOLD);
        categoryHeaderTextView.setText(selectedCategoryText);

        return groupView;
    }
    @Override
    public View getChildView(int categoryIndex, int childIndex, boolean b, View childView, ViewGroup viewGroup) {
        String selectedChildText = (String) getChild(categoryIndex, childIndex);
        if (childView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            childView = layoutInflater.inflate(R.layout.conversion_favorites_child, null);
        }

        TextView childTextView = (TextView) childView.findViewById(R.id.conversion_faves_child_item);
        childTextView.setText(selectedChildText);

        return childView;
    }

    @Override
    public boolean isChildSelectable(int groupIndex, int childIndex) {
        return true;
    }
}
