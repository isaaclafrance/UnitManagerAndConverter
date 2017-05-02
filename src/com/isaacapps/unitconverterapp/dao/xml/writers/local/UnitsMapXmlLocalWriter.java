package com.isaacapps.unitconverterapp.dao.xml.writers.local;

import java.io.IOException;
import java.util.Collection;
import java.util.Map.Entry;

import org.xmlpull.v1.XmlSerializer;

import com.isaacapps.unitconverterapp.dao.xml.writers.XmlWriter;
import com.isaacapps.unitconverterapp.models.Unit;

import android.content.Context;

public class UnitsMapXmlLocalWriter extends XmlWriter<Collection<Unit>> {

	///
	public UnitsMapXmlLocalWriter(Context context){
		super(context);
		destination = "DynamicUnits.xml";
	}
	
	///
	@Override
	protected void writeEntity(XmlSerializer serializer, String namespace, Collection<Unit> units) throws IllegalArgumentException, IllegalStateException, IOException{
		for(Unit unit:units){
			serializer.startTag(namespace, "unit");

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
								serializer.text(entry.getValue().toString());
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
			
			serializer.endTag(namespace, "unit");
		}
	}
	
}
