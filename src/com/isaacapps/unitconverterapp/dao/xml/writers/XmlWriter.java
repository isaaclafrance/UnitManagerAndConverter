package com.isaacapps.unitconverterapp.dao.xml.writers;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;

public abstract class XmlWriter<T> {
	private Context context;
	protected String destination;
	
	///
	public XmlWriter(Context context){
		this.context = context;
	}
	
	///
	public void saveToXML(T entity) throws XmlPullParserException, IllegalArgumentException, IllegalStateException, IOException{
		OutputStream xmlFileOutputStream = new FileOutputStream(context.getFilesDir().getPath().toString() + destination, false);
		Writer xmlStreamWriter = new OutputStreamWriter(xmlFileOutputStream);
		
		try{
			String namespace = "";		
			XmlPullParserFactory xpFactory = XmlPullParserFactory.newInstance();
			XmlSerializer serializer = xpFactory.newSerializer();
			
			serializer.setOutput(xmlStreamWriter);
			serializer.startDocument("UTF-8", true);
			serializer.startTag(namespace, "main");
			
			writeEntity(serializer, namespace, entity);
			
			serializer.endTag(namespace, "main");	
			serializer.endDocument();
			serializer.flush();		
		}finally{
			xmlStreamWriter.flush();
			xmlStreamWriter.close();
			xmlFileOutputStream.flush();
			xmlFileOutputStream.close();	
		}
	}
	protected abstract void writeEntity(XmlSerializer serializer, String namespace, T entity) throws IllegalArgumentException, IllegalStateException, IOException;
}
