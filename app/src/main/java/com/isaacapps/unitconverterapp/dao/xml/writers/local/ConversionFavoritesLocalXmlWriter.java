package com.isaacapps.unitconverterapp.dao.xml.writers.local;

import android.content.Context;

import com.isaacapps.unitconverterapp.dao.xml.writers.XmlWriter;
import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.ConversionFavoritesDataModel;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

public class ConversionFavoritesLocalXmlWriter extends XmlWriter<ConversionFavoritesDataModel> {

    ///
    public ConversionFavoritesLocalXmlWriter(Context context) {
        super(context);
        destination = "ConversionFavorites.xml";
    }

    ///
    @Override
    protected void writeEntity(XmlSerializer xmlSerializer, String namespace, ConversionFavoritesDataModel conversionFavoritesDataModel) throws IllegalArgumentException, IllegalStateException, IOException {
        for (String formattedConversion : conversionFavoritesDataModel.getAllFormattedConversions()) {
            xmlSerializer.startTag(namespace, "favorite");

            xmlSerializer.startTag(namespace, "sourceUnit");
                xmlSerializer.text(conversionFavoritesDataModel.getSourceUnitNameFromConversion(formattedConversion));
            xmlSerializer.endTag(namespace, "sourceUnit");

            xmlSerializer.startTag(namespace, "targetUnit");
                xmlSerializer.text(conversionFavoritesDataModel.getTargetUnitNameFromConversion(formattedConversion));
            xmlSerializer.endTag(namespace, "targetUnit");

            xmlSerializer.startTag(namespace, "unitCategory");
                xmlSerializer.text(ConversionFavoritesDataModel.parseUnitCategoryFromConversion(formattedConversion));
            xmlSerializer.endTag(namespace, "unitCategory");

            xmlSerializer.startTag(namespace, "significanceRank");
                xmlSerializer.text(String.valueOf(conversionFavoritesDataModel.getSignificanceRankOfConversion(formattedConversion)));
            xmlSerializer.endTag(namespace, "significanceRank");

            xmlSerializer.endTag(namespace, "favorite");
        }
    }
}
