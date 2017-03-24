package com.isaacapps.unitconverterapp.dao.xml.writers.local;

import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlSerializer;

import com.isaacapps.unitconverterapp.dao.xml.writers.XmlWriter;

import android.content.Context;

public class ConversionFavoritesListXmlLocalWriter extends XmlWriter<ArrayList<String>>{

	///
	public ConversionFavoritesListXmlLocalWriter(Context context){
		super(context);
		destination = "ConversionFavorites.xml";
	}
	
	///
	@Override
	protected void writeEntity(XmlSerializer serializer, String namespace, ArrayList<String> favoriteConversions) throws IllegalArgumentException, IllegalStateException, IOException{
		String[] categoryNconversionUnitsNames;
		String[] conversionUnitNames;
		
		for(String conversionGroup:favoriteConversions){
			categoryNconversionUnitsNames = conversionGroup.split(": ");
			conversionUnitNames = categoryNconversionUnitsNames[1].split(" --> ");
			
			serializer.startTag(namespace, "conversion");
				serializer.startTag(namespace, "unitCategory");
					serializer.text(categoryNconversionUnitsNames[0]);
				serializer.endTag(namespace, "unitCategory");
				serializer.startTag(namespace, "fromUnit");
					serializer.text(conversionUnitNames[0]);
				serializer.endTag(namespace, "fromUnit");
				serializer.startTag(namespace, "toUnit");
					serializer.text(conversionUnitNames[1]);
				serializer.endTag(namespace, "toUnit");
			serializer.endTag(namespace, "conversion");	
		}
	}
}
