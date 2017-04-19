package com.isaacapps.unitconverterapp.dao.xml.readers.local;

import java.io.IOException;
import org.xmlpull.v1.*;

import com.isaacapps.unitconverterapp.dao.xml.readers.AsyncXmlReader;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.ConversionFavoritesDataModel;

import android.content.Context;

///According to official Google Android documentation, the XmlPullParser that reads one tag at a time is the most efficient way of parsing especially in situations where there are a large number of tags.
public class ConversionFavoritesListXmlLocalReader extends AsyncXmlReader<ConversionFavoritesDataModel,ConversionFavoritesDataModel>{

	///
	public ConversionFavoritesListXmlLocalReader(Context context){
		super(context);
	}
	
	///
	@Override
	protected ConversionFavoritesDataModel readEntity(XmlPullParser parser) throws XmlPullParserException, IOException{
		ConversionFavoritesDataModel conversionFavoritesDataModel = new ConversionFavoritesDataModel();
		Double significanceRank = 0.0;
		String unitCategory, sourceUnit, targetUnit, tagName;
		unitCategory = sourceUnit = targetUnit = "";
		tagName = parser.getName();
	
		if(tagName.equalsIgnoreCase("main")){
			parser.require(XmlPullParser.START_TAG, null, "main");
			while(parser.next() != XmlPullParser.END_TAG){
				if(parser.getEventType() != XmlPullParser.START_TAG){
					continue;
				}
				tagName = parser.getName();
				if(tagName.equalsIgnoreCase("favorite")){
					parser.require(XmlPullParser.START_TAG, null, "favorite");
					while(parser.next() != XmlPullParser.END_TAG){
						if(parser.getEventType() != XmlPullParser.START_TAG){
							continue;
						}
						tagName = parser.getName();
						if(tagName.equalsIgnoreCase("significanceRank")){
							significanceRank = readDouble(parser);
						}
						else if(tagName.equalsIgnoreCase("sourceUnit")){
							sourceUnit = readText(parser).toLowerCase();
						}
						else if(tagName.equalsIgnoreCase("targetUnit")){							
							targetUnit = readText(parser).toLowerCase();
						}
						else if(tagName.equalsIgnoreCase("unitCategory")){							
							unitCategory = readText(parser).toLowerCase();
						}
						else{
							skip(parser);
						}
					}
					conversionFavoritesDataModel.addConversion(unitCategory, sourceUnit, targetUnit);
					for(int i=0;i<significanceRank;i++)
						conversionFavoritesDataModel.modifySignificanceRankOfConversion(ConversionFavoritesDataModel
								                                                        .convertToFormattedConversion(unitCategory, sourceUnit, targetUnit)
								                                                        ,true);
				}
				else{
					skip(parser);
				}
			}
		}
		else{
			skip(parser);
		}		
		return conversionFavoritesDataModel;
	}
	
	///
	@Override
	public ConversionFavoritesDataModel loadInBackground() {
		ConversionFavoritesDataModel conversionFavoritesDataModel = new ConversionFavoritesDataModel();
		try {
			conversionFavoritesDataModel = parseXML(openXmlFile(getContext().getFilesDir().getPath().toString() + "ConversionFavorites.xml", false));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return conversionFavoritesDataModel;
	}
	
}
