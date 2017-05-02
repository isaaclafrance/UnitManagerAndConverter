package com.isaacapps.unitconverterapp.models.unitmanager;

import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.*;

//This class acts almost like a service locator that contains accessible references to service-like objects that share a particular environment.
public final class UnitManager{
	public static enum DATA_MODEL_CATEGORY{CORE, DYNAMIC, UNKNOWN};
		
	private PrefixesDataModel prefixesDataModel;
	private UnitsDataModel unitsDataModel;
	private FundamentalUnitsDataModel fundamentalUnitsDataModel;
	private UnitsClassifierDataModel unitsClassifierDataModel;
	private ConversionFavoritesDataModel conversionFavoritesDataModel;
	
	///Should ideally only be invoked by the UnitManagerBuilder
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
	void setUnitsModelData(UnitsDataModel modelData){ //Should ideally only be invoked by the UnitManagerBuilder
		this.unitsDataModel = modelData;
		this.unitsDataModel.setUnitManagerRef(this);
	}
	
	public UnitsClassifierDataModel getUnitsClassifierDataModel() {
		return unitsClassifierDataModel;
	}
	void setUnitsClassifierDataModel(UnitsClassifierDataModel unitsClassifierDataModel){ //Should ideally only be invoked by the UnitManagerBuilder
		this.unitsClassifierDataModel = unitsClassifierDataModel;
	}
	
	public FundamentalUnitsDataModel getFundamentalUnitsDataModel() {
		return fundamentalUnitsDataModel;
	}
	void setFundamentalUnitsModelData(FundamentalUnitsDataModel modelData){  //Should ideally only be invoked by the UnitManagerBuilder
		this.fundamentalUnitsDataModel = modelData;
		this.fundamentalUnitsDataModel.setUnitManagerRef(this);
	}	
	
	public ConversionFavoritesDataModel getConversionFavoritesDataModel(){
		return conversionFavoritesDataModel;
	}
	void setConversionFavoritesDataModel(ConversionFavoritesDataModel conversionFavoritesDataModel) {
		this.conversionFavoritesDataModel = conversionFavoritesDataModel;
	}
}

