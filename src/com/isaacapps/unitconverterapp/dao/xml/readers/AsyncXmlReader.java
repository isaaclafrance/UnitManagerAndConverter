package com.isaacapps.unitconverterapp.dao.xml.readers;

import java.io.*;
import java.net.*;

import org.xmlpull.v1.*;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Xml;

///According to official Google Android documentation, the XmlPullParser that reads one tag at a time is the most efficient way of parsing especially in situations where there are a large number of tags.
public abstract class AsyncXmlReader<S, T> extends AsyncTaskLoader<T> {

	///
	public AsyncXmlReader(Context context) {
		super(context);
	}

	///According to official Google Android documentation, the XmlPullParser that reads one tag at a time is the most efficient way of parsing especially in situations where there are a large number of tags.
	public S parseXML(InputStream in) {
		try{
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);			
			parser.setInput(in, null);
			parser.nextTag();
			return readEntity(parser);
		} catch (XmlPullParserException e) {		
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		finally{
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	
	///
	public InputStream openAssetFile(String fileName) throws IOException{
		return getContext().getAssets().open(fileName);
	}
	public InputStream openXmlFile(String fileLocation, boolean isOnlineSource) throws MalformedURLException, IOException{
		if(isOnlineSource){
			return new URL(fileLocation).openStream();	
		}
		else{
			File xmlFile = new File(fileLocation);		
			if(!xmlFile.exists()){
				xmlFile.createNewFile();
			}
			
			return new FileInputStream(xmlFile);
		}
	}
	
	///
	abstract protected S readEntity(XmlPullParser parser)throws IOException, XmlPullParserException;
	
	///
	protected String readText(XmlPullParser parser)throws IOException, XmlPullParserException{
		String text = "";
		if(parser.next() == XmlPullParser.TEXT){
			text = parser.getText().trim();
			parser.nextTag();
		}
		return text;
	}
	protected double readDouble(XmlPullParser parser) throws IOException, XmlPullParserException{
		return readDoubleArray(parser)[0];
	}
	protected double[] readDoubleArray(XmlPullParser parser) throws IOException, XmlPullParserException{
		double[] doubles = new double[2];
		String[] doublesTexts = readText(parser).split(" ", 0);
		
		doubles[0] = Double.valueOf(doublesTexts[0]);
		if (doublesTexts.length > 1){
			doubles[1] = Double.valueOf(doublesTexts[1]);
		}
		return doubles;
	}
	protected String readAttribute(XmlPullParser parser, String attributeName) throws XmlPullParserException, IOException{
		String attributeValue = parser.getAttributeValue(null, attributeName);
		return (attributeValue==null)?"":attributeValue.toLowerCase();
	}
 	protected void skip(XmlPullParser parser) throws XmlPullParserException, IOException{
		if(parser.getEventType() != XmlPullParser.START_TAG){
			throw new IllegalStateException();
		}
		int depth = 1;
		while(depth != 0){
			switch(parser.next()){
				case XmlPullParser.END_TAG:
					depth--;
					break;
				case XmlPullParser.START_TAG:
					depth++;
					break;				
			}
		}
	}
}
