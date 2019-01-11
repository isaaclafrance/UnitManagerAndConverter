package com.isaacapps.unitconverterapp.dao.xml.writers;

import android.content.Context;

import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public abstract class XmlWriter<T> {
    protected String destination;
    private final Context context;

    ///
    public XmlWriter(Context context) {
        this.context = context;
    }

    ///
    public void saveToXML(T entity) throws XmlPullParserException, IllegalArgumentException, IllegalStateException, IOException {
        File xmlFile = new File(context.getFilesDir().getPath() + destination);
        OutputStream xmlFileOutputStream = new FileOutputStream(xmlFile, false);
        Writer xmlStreamWriter = new OutputStreamWriter(xmlFileOutputStream);

        try {
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
        } finally {
            xmlStreamWriter.flush();
            xmlStreamWriter.close();
            xmlFileOutputStream.flush();
            xmlFileOutputStream.close();
        }
    }

    protected abstract void writeEntity(XmlSerializer xmlSerializer, String namespace, T entity) throws IllegalArgumentException, IllegalStateException, IOException;
}
