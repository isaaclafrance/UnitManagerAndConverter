package com.isaacapps.unitconverterapp.models.unitmanager;

import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.*;

//A class that manages and converts among different units. 
public final class UnitManager{
	public static enum DATA_MODEL_CATEGORY{CORE, DYNAMIC, UNKNOWN};
	
	//This only makes sense within the context of a unit manager. A unit may be of type mass in one unit manager but something else in another unit manager 
	public static enum UNIT_TYPE{MASS, LENGTH, TIME, AMT_OF_SUBSTANCE, TEMPERATURE, CURRENT, LUMINOUS_INTENSITY, DERIVED_SINGLE_UNIT, DERIVED_MULTI_UNIT, UNKNOWN, CURRENCY};
	
	private PrefixesDataModel prefixesDataModel;
	private UnitsDataModel unitsDataModel;
	private FundamentalUnitsDataModel fundamentalUnitsDataModel;
	private UnitsClassifierDataModel unitsClassifierDataModel;
	private ConversionFavoritesDataModel conversionFavoritesDataModel;
	
	private Converter converter;
	private Utility utility;
	
	///Should only be invoked by the UnitManagerBuilder
	UnitManager(){}

	///
	public PrefixesDataModel getPrefixesDataModel() {
		return prefixesDataModel;
	}
	void setPrefixesModelData(PrefixesDataModel modelData){
		this.prefixesDataModel = modelData;
		this.prefixesDataModel.setUnitManagerRef(this);
	}
	
	public UnitsDataModel getUnitsDataModel() {
		return unitsDataModel;
	}
	void setUnitsModelData(UnitsDataModel modelData){ //Should only be invoked by the UnitManagerBuilder
		this.unitsDataModel = modelData;
		this.unitsDataModel.setUnitManagerRef(this);
	}
	
	public UnitsClassifierDataModel getUnitsClassifierDataModel() {
		return unitsClassifierDataModel;
	}
	void setUnitsClassifierDataModel(UnitsClassifierDataModel unitsClassifierDataModel){ //Should only be invoked by the UnitManagerBuilder
		this.unitsClassifierDataModel = unitsClassifierDataModel;
	}
	
	public FundamentalUnitsDataModel getFundamentalUnitsDataModel() {
		return fundamentalUnitsDataModel;
	}
	void setFundamentalUnitsModelData(FundamentalUnitsDataModel modelData){  //Should only be invoked by the UnitManagerBuilder
		this.fundamentalUnitsDataModel = modelData;
	}	
	
	public ConversionFavoritesDataModel getConversionFavoritesDataModel(){
		return conversionFavoritesDataModel;
	}
	void setConversionFavoritesDataModel(ConversionFavoritesDataModel conversionFavoritesDataModel) {
		this.conversionFavoritesDataModel = conversionFavoritesDataModel;
	}
	
	///

	public Converter getConverter() {
		return converter;
	}
	void setConverter(Converter converter){
		this.converter = converter;
		this.converter.setUnitManagerRef(this);
	}

	public Utility getUtility() {
		return utility;
	}
	void setUtility(Utility utility){
		this.utility = utility;
		this.utility.setUnitManagerRef(this);
	}
}

