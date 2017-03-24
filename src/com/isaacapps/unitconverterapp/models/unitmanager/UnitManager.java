package com.isaacapps.unitconverterapp.models.unitmanager;


//A class that manages and converts among different units. 
public final class UnitManager{
	public static enum UNIT_TYPE{MASS, LENGTH, TIME, AMT_OF_SUBSTANCE, TEMPERATURE, CURRENT, LUMINOUS_INTENSITY, DERIVED_SINGLE_UNIT, DERIVED_MULTI_UNIT, UNKNOWN, CURRENCY};
	
	private DataMaps dataMaps;
	private UnitsClassifier unitsClassifier;
	
	private ContentModifier contentModifier;
	private QueryExecutor queryExecutor;
	private Converter converter;
	private Utility utility;
	
	//Can only be invoked by the UnitManagerBuilder
	UnitManager(){

	}

	//Ideally, all interaction with this data object should be done through the Content Modifier object
	DataMaps getDataMaps() {
		return dataMaps;
	}
	void setDataMaps(DataMaps dataMaps){
		this.dataMaps = dataMaps;
	}

	public ContentModifier getContentModifier(){
		return contentModifier;
	}
	void setContentModifiers(ContentModifier contentModifier){
		this.contentModifier = contentModifier;
	}
	
	public QueryExecutor getQueryExecutor() {
		return queryExecutor;
	}
	void setQueryExecutor(QueryExecutor queryExecutor){
		this.queryExecutor = queryExecutor;
	}

	public Converter getConverter() {
		return converter;
	}
	void setConverter(Converter converter){
		this.converter = converter;
	}

	public Utility getUtility() {
		return utility;
	}
	void setUtility(Utility utility){
		this.utility = utility;
	}

	public UnitsClassifier getUnitsClassifier() {
		return unitsClassifier;
	}
	void setUnitsClassifier(UnitsClassifier unitsClassifier){
		this.unitsClassifier = unitsClassifier;
		this.unitsClassifier.setDataMaps(dataMaps);
	}
	
}

