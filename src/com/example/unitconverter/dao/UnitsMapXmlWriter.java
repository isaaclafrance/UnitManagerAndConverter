package com.example.unitconverter.dao;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Map.Entry;

import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import com.example.unitconverter.Unit;

import android.content.Context;

public class UnitsMapXmlWriter {
	//Fields
	private static String unitsXmlDestination = "DynamicUnits.xml";;
	
	//Constructor
	public UnitsMapXmlWriter(){
	}
	
	//TODO: Write unit to XML file 
	public static void saveUnitsToXML(Context context, ArrayList<Unit> unitsDictionary) throws XmlPullParserException, IllegalArgumentException, IllegalStateException, IOException{
		OutputStream xmlFileOutputStream = new FileOutputStream(context.getFilesDir().getPath().toString() + unitsXmlDestination, false);
		Writer xmlStreamWriter = new OutputStreamWriter(xmlFileOutputStream);
		
		try{
			String namespace = "";		
			XmlPullParserFactory xpFactory = XmlPullParserFactory.newInstance();
			XmlSerializer serializer = xpFactory.newSerializer();
			
			serializer.setOutput(xmlStreamWriter);
			serializer.startDocument("UTF-8", true);
			serializer.startTag(namespace, "main");
			
			for(Unit unit:unitsDictionary){
				serializer.startTag(namespace, "unit");
				writeUnitXML(serializer, namespace, unit);	
				serializer.endTag(namespace, "unit");
			}
			
			serializer.endTag(namespace, "main");	
			serializer.endDocument();
			serializer.flush();		
		}finally{
			
		}
	}
	private static void writeUnitXML(XmlSerializer serializer, String namespace, Unit unit) throws IllegalArgumentException, IllegalStateException, IOException{
		serializer.startTag(namespace, "unitName");
			serializer.text(unit.getName());
		serializer.endTag(namespace, "unitName");
		
		serializer.startTag(namespace, "unitSystem");
			serializer.text(unit.getUnitSystem());
		serializer.endTag(namespace, "unitSystem");
		
		serializer.startTag(namespace, "abbreviation");
			serializer.text(unit.getAbbreviation());
		serializer.endTag(namespace, "abbreviation");
		
		serializer.startTag(namespace, "unitCategory");
			serializer.text(unit.getCategory());
		serializer.endTag(namespace, "unitCategory");
		
		serializer.startTag(namespace, "unitDescription");
			serializer.text(unit.getDescription());
		serializer.endTag(namespace, "unitDescription");
		
		serializer.startTag(namespace, "componentUnits");
			for(Entry<String, Double> entry:unit.getComponentUnitsDimension().entrySet()){
				serializer.startTag(namespace, "component");
					serializer.startTag(namespace, "unitName");
						serializer.text(entry.getKey());
					serializer.endTag(namespace, "unitName");
					
					serializer.startTag(namespace, "exponent");
						serializer.text(Double.toString(entry.getValue()));
					serializer.endTag(namespace, "exponent");
				serializer.endTag(namespace, "component");
			}
		serializer.endTag(namespace, "componentUnits");
		
		serializer.startTag(namespace, "baseConversionPolyCoeffs");
			serializer.startTag(namespace, "baseUnit");
				serializer.text(unit.getBaseUnit().getName());
			serializer.endTag(namespace, "baseUnit");
			
			serializer.startTag(namespace, "polynomialCoeffs");
				String polyCoeffsString = "";
				for(int i=0;i<unit.getBaseConversionPolyCoeffs().length;i++){
					polyCoeffsString += Double.toString(unit.getBaseConversionPolyCoeffs()[i])+" ";
				}
				serializer.text(polyCoeffsString);
			serializer.endTag(namespace, "polynomialCoeffs");		
		serializer.endTag(namespace, "baseConversionPolyCoeffs");
	}
	
}
