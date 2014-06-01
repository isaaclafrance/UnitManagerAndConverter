package com.heatIntegration.internals;

public enum UNIT_SYSTEMS{
	SI, US, BRITISH_IMPERIAL, METRIC_LEGACY;	
	
	private UNIT_SYSTEMS(){
		
	}
	
	public static final class SI_SYSTEM{
		private SI_SYSTEM(){
			
		}

		public enum PREFIXES{
			MILLI, CENTI, DECI, DECA, HECTA, KILO;

			private PREFIXES(){
			}
			
			public Float getPrefixValue(){
				return UnitManager.getInstance().getPrefixValue(this.name());
			}
		}
		
		public enum TRIGONOMETRIC_UNITS{
			HERTZ, PERIOD, RADIAN, DEGREE;

			private TRIGONOMETRIC_UNITS(){
				
			}
			
			public UnitClass getUnit(){
				return UnitManager.getInstance().getUnit(this.name());
			}
		}			
		
		public enum ELECTRICITY_N_MAGNETISM_UNITS{
			OHM;
			
			private ELECTRICITY_N_MAGNETISM_UNITS(){
				
			}
			
			public UnitClass getUnit(){
				return UnitManager.getInstance().getUnit(this.name());
			}
		}
		public enum OPTICS_UNITS{
			LUMENS, CANDELA;
			
			private OPTICS_UNITS(){
				
			}
			
			public UnitClass getUnit(){
				return UnitManager.getInstance().getUnit(this.name());
			}			
		}
		
		public enum TEMPERATURE_UNITS{
			KELVIN, CELSIUS;
			
			private TEMPERATURE_UNITS(){
				
			}
			
			public UnitClass getUnit(){
				return UnitManager.getInstance().getUnit(this.name());
			}
		}
		public enum ENERGY_UNITS{
			JOULE;
		
			private ENERGY_UNITS(){
				
			}
			
			public UnitClass getUnit(){
				return UnitManager.getInstance().getUnit(this.name());
			}
		}
		public enum POWER_UNITS{
			WATT;
		
			private POWER_UNITS(){
				
			}
			
			public UnitClass getUnit(){
				return UnitManager.getInstance().getUnit(this.name());
			}
		}
		public enum PRESSURE{
			PASCAL;
			
			private PRESSURE(){
				
			}
			
			public UnitClass getUnit(){
				return UnitManager.getInstance().getUnit(this.name());
			}
		}
	
		public enum MASS_UNITS{
			GRAM;

			private MASS_UNITS(){
				
			}
			
			public UnitClass getUnit(){
				return UnitManager.getInstance().getUnit(this.name());
			}
		}
		public enum LENGTH_UNITS{
			METER;

			private LENGTH_UNITS(){
				
			}
			
			public UnitClass getUnit(){
				return UnitManager.getInstance().getUnit(this.name());
			}
		}
		public enum TIME_UNITS{
			SECOND, MINUTE, HOUR, DAY;

			private TIME_UNITS(){
				
			}
			
			public UnitClass getUnit(){
				return UnitManager.getInstance().getUnit(this.name());
			}
		}			
	
		public enum AREA_UNITS{
			SQUARE_METER;

			private AREA_UNITS(){
				
			}
			
			public UnitClass getUnit(){
				return UnitManager.getInstance().getUnit(this.name());
			}
		}
		public enum VOLUME_UNITS{
			LITER, CUBIC_METER;

			private VOLUME_UNITS(){
				
			}
			
			public UnitClass getUnit(){
				return UnitManager.getInstance().getUnit(this.name());
			}		
		}		
	}
	public static final class US_SYSTEM{
		private US_SYSTEM(){
			
		}
		
		public enum VOLUME_UNITS{
			FLUID_OUNCE, CUP, PINT, QUART, GALLON;

			private VOLUME_UNITS(){
				
			}
			
			public UnitClass getUnit(){
				return UnitManager.getInstance().getUnit(this.name());
			}	
		}			
	}
	public static final class BRITISH_IMPERIAL_SYSTEM{
		private BRITISH_IMPERIAL_SYSTEM(){
			
		}
		
		public enum PREFIXES{
			DOZEN;
			
			private PREFIXES(){
				
			}
			
			public Float getPrefixValue(){
				return UnitManager.getInstance().getPrefixValue(this.name());
			}	
		}
		
		public enum TEMPERATURE_UNITS{
			FAHRENHEIT, RANKINE;
			
			private TEMPERATURE_UNITS(){
				
			}
			
			public UnitClass getUnit(){
				return UnitManager.getInstance().getUnit(this.name());
			}
		}
		public enum ENERGY_UNITS{
			BTU;
		
			private ENERGY_UNITS(){
				
			}
			
			public UnitClass getUnit(){
				return UnitManager.getInstance().getUnit(this.name());
			}
		}
		public enum POWER_UNITS{
			HORSEPOWER;
		
			private POWER_UNITS(){
				
			}
			
			public UnitClass getUnit(){
				return UnitManager.getInstance().getUnit(this.name());
			}
		}			
		public enum PRESSURE{
			BAR, TORR;
			
			private PRESSURE(){
				
			}
			
			public UnitClass getUnit(){
				return UnitManager.getInstance().getUnit(this.name());
			}
		}
	
		public enum MASS_UNITS{
			OUNCE, POUND;

			private MASS_UNITS(){
				
			}
			
			public UnitClass getUnit(){
				return UnitManager.getInstance().getUnit(this.name());
			}
		}
		public enum LENGTH_UNITS{
			INCH, FOOT, YARD, MILE;

			private LENGTH_UNITS(){
				
			}
			
			public UnitClass getUnit(){
				return UnitManager.getInstance().getUnit(this.name());
			}
		}
		public enum TIME_UNITS{
			SECOND, MINUTE, HOUR, DAY;

			private TIME_UNITS(){
				
			}
			
			public UnitClass getUnit(){
				return UnitManager.getInstance().getUnit(this.name());
			}
		}			
	
		public enum AREA_UNITS{
			ARE;

			private AREA_UNITS(){
				
			}
			
			public UnitClass getUnit(){
				return UnitManager.getInstance().getUnit(this.name());
			}
		}
		public enum VOLUME_UNITS{
			FLUID_OUNCE, PINT, QUART, GALLON;

			private VOLUME_UNITS(){
				
			}
			
			public UnitClass getUnit(){
				return UnitManager.getInstance().getUnit(this.name());
			}		
		}			
	}
	public static final class METRIC_LEGACY_SYSTEM{
		private METRIC_LEGACY_SYSTEM(){
			
		}

		public enum ENERGY_UNITS{
			ERGS;

			private ENERGY_UNITS(){
				
			}
			
			public UnitClass getUnit(){
				return UnitManager.getInstance().getUnit(this.name());
			}
		}
	
		public enum AREA_UNITS{
			ARE, HECTARE;

			private AREA_UNITS(){
				
			}
			
			public UnitClass getUnit(){
				return UnitManager.getInstance().getUnit(this.name());
			}
		}
	}
	public static final class UNKNOWN_SYSTEM{
		private UNKNOWN_SYSTEM(){
			
		}

		public enum UNKNOWN_UNITS{
			UNKNOWN_UNIT;
			
			UNKNOWN_UNITS(){
				
			}
			
			public UnitClass getUnit(){
				return UnitManager.getInstance().getUnit(this.name());
			}
		}
	}
}
