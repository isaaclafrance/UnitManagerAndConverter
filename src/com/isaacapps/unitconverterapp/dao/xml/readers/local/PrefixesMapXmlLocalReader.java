package com.isaacapps.unitconverterapp.dao.xml.readers.local;

import java.io.IOException;

import org.xmlpull.v1.*;

import com.isaacapps.unitconverterapp.dao.xml.readers.AsyncXmlReader;
import com.isaacapps.unitconverterapp.models.unitmanager.UnitManager.*;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.*;
import com.isaacapps.unitconverterapp.models.unitmanager.*;

import android.content.Context;

///According to official Google Android documentation, the XmlPullParser that reads one tag at a time is the most efficient way of parsing especially in situations where there are a large number of tags.
public class PrefixesMapXmlLocalReader extends AsyncXmlReader<PrefixesDataModel, UnitManagerBuilder>{
	private DATA_MODEL_CATEGORY category;
	
	///
	public PrefixesMapXmlLocalReader(Context context){
		super(context);
	}
		
	///
	@Override
	protected PrefixesDataModel readEntity(XmlPullParser parser) throws XmlPullParserException, IOException{
		String tagName = "", prefixName = "", abbreviation = "";
		double prefixValue = 0.0;
		PrefixesDataModel prefixesDataModel = new PrefixesDataModel();
		
		//
		tagName = parser.getName();
		if(tagName.equalsIgnoreCase("main")){
			parser.require(XmlPullParser.START_TAG, null, "main");
			while(parser.next() != XmlPullParser.END_TAG){
				if(parser.getEventType() != XmlPullParser.START_TAG){
					continue;
				}
				tagName = parser.getName();
				if(tagName.equalsIgnoreCase("prefix")){
					while(parser.next() != XmlPullParser.END_TAG){
						if(parser.getEventType() != XmlPullParser.START_TAG){
							continue;
						}
						tagName = parser.getName();
						if(tagName.equalsIgnoreCase("name")){
							prefixName = readText(parser).toLowerCase();
						}else if(tagName.equalsIgnoreCase("abbreviation")){
							abbreviation = readText(parser);
						}else if(tagName.equalsIgnoreCase("value")){
							prefixValue = readDouble(parser);
						}else{
							skip(parser);
						}	
					}
					if(category == DATA_MODEL_CATEGORY.CORE){
						prefixesDataModel.addCorePrefix(prefixName, abbreviation, prefixValue);
					}
					else{
						prefixesDataModel.addDynamicPrefix(prefixName, abbreviation, prefixValue);
					}
				}
				else{
					skip(parser);
				}
			}
		}

		return prefixesDataModel;
	}
	
	///
	@Override
	public UnitManagerBuilder loadInBackground() {
		UnitManagerBuilder unitManagerBuilderBundle = new UnitManagerBuilder();
		try {
			category = DATA_MODEL_CATEGORY.CORE;
			unitManagerBuilderBundle.addPrefixDataModel(parseXML(openAssetFile("StandardCorePrefixes.xml")));
			category = DATA_MODEL_CATEGORY.DYNAMIC;
			unitManagerBuilderBundle.addPrefixDataModel(parseXML(openXmlFile(getContext().getFilesDir().getPath().toString() + "DynamicPrefixes.xml", false)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return unitManagerBuilderBundle;
	}
	    
}
