package com.isaacapps.unitconverterapp.dao.xml.writers.local.units;

import android.content.Context;

import com.isaacapps.unitconverterapp.dao.xml.writers.XmlWriter;
import com.isaacapps.unitconverterapp.models.measurables.unit.Unit;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class UnitsMapXmlLocalWriter extends XmlWriter<Collection<Unit>> {

    public UnitsMapXmlLocalWriter(Context context, String fileName) {
        super(context);
        destination = fileName;
    }

    ///
    @Override
    protected void writeEntity(XmlSerializer xmlSerializer, String namespace, Collection<Unit> units) throws IllegalArgumentException, IllegalStateException, IOException {
        for (Unit unit : units) {
            xmlSerializer.startTag(namespace, "unit");

            xmlSerializer.startTag(namespace, "unitName");
            xmlSerializer.text(unit.getName());
            xmlSerializer.endTag(namespace, "unitName");

            xmlSerializer.startTag(namespace, "unitNameAliases");
            for (String alias : unit.getAliases()) {
                xmlSerializer.startTag(namespace, "alias");
                xmlSerializer.text(alias);
                xmlSerializer.endTag(namespace, "alias");
            }
            xmlSerializer.endTag(namespace, "unitNameAliases");

            xmlSerializer.startTag(namespace, "unitSystem");
            xmlSerializer.text(unit.getUnitSystem());
            xmlSerializer.endTag(namespace, "unitSystem");

            xmlSerializer.startTag(namespace, "abbreviation");
            xmlSerializer.text(unit.getAbbreviation());
            xmlSerializer.endTag(namespace, "abbreviation");

            xmlSerializer.startTag(namespace, "unitCategory");
            xmlSerializer.text(unit.getCategory());
            xmlSerializer.endTag(namespace, "unitCategory");

            xmlSerializer.startTag(namespace, "unitDescription");
            xmlSerializer.text(unit.getDescription());
            xmlSerializer.endTag(namespace, "unitDescription");

            xmlSerializer.startTag(namespace, "componentUnits");
            for (Map.Entry<String, Double> entry : unit.getComponentUnitsDimension().entrySet()) {
                xmlSerializer.startTag(namespace, "component");
                xmlSerializer.startTag(namespace, "unitName");
                xmlSerializer.text(entry.getKey());
                xmlSerializer.endTag(namespace, "unitName");

                xmlSerializer.startTag(namespace, "exponent");
                xmlSerializer.text(entry.getValue().toString());
                xmlSerializer.endTag(namespace, "exponent");
                xmlSerializer.endTag(namespace, "component");
            }
            xmlSerializer.endTag(namespace, "componentUnits");

            xmlSerializer.startTag(namespace, "baseConversionPolyCoeffs");
            xmlSerializer.startTag(namespace, "baseUnit");
            xmlSerializer.text(unit.getBaseUnit().getName());
            xmlSerializer.endTag(namespace, "baseUnit");

            xmlSerializer.startTag(namespace, "polynomialCoeffs");
            StringBuilder polyCoeffsString = new StringBuilder();
            for (int i = 0; i < unit.getBaseConversionPolyCoeffs().length; i++) {
                polyCoeffsString.append(Double.toString(unit.getBaseConversionPolyCoeffs()[i])).append(" ");
            }
            xmlSerializer.text(polyCoeffsString.toString());
            xmlSerializer.endTag(namespace, "polynomialCoeffs");
            xmlSerializer.endTag(namespace, "baseConversionPolyCoeffs");

            xmlSerializer.endTag(namespace, "unit");
        }
    }
}
