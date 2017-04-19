package com.isaacapps.unitconverterapp.dao.xml.writers.local;

import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

import com.isaacapps.unitconverterapp.dao.xml.writers.XmlWriter;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.ConversionFavoritesDataModel;

import android.content.Context;

public class ConversionFavoritesListXmlLocalWriter extends XmlWriter<ConversionFavoritesDataModel>{

	///
	public ConversionFavoritesListXmlLocalWriter(Context context){
		super(context);
		destination = "ConversionFavorites.xml";
	}
	
	///
	@Override
	protected void writeEntity(XmlSerializer serializer, String namespace, ConversionFavoritesDataModel conversionFavoritesModelData) throws IllegalArgumentException, IllegalStateException, IOException{
		for(String formattedConversion:conversionFavoritesModelData.getAllFormattedConversions()){
			serializer.startTag(namespace, "favorite");
				serializer.startTag(namespace, "sourceUnit");
					serializer.text(ConversionFavoritesDataModel.getSourceUnitNameFromConversion(formattedConversion));
				serializer.endTag(namespace, "sourceUnit");
				serializer.startTag(namespace, "targetUnit");
					serializer.text(ConversionFavoritesDataModel.getTargetUnitNameFromConversion(formattedConversion));
				serializer.endTag(namespace, "targetUnit");
				serializer.startTag(namespace, "unitCategory");
					serializer.text(ConversionFavoritesDataModel.getTargetUnitNameFromConversion(formattedConversion));
					serializer.endTag(namespace, "unitCategory");
				serializer.startTag(namespace, "significanceRank");
					serializer.text(String.valueOf(conversionFavoritesModelData.getSignificanceRankOfConversion(formattedConversion)));
				serializer.endTag(namespace, "significanceRank");
			serializer.endTag(namespace, "favorite");	
		}
	}
}
