package commons.model;

import java.text.DecimalFormat;

import StockTickerIOT.StockTickerArtifactFactory;
import commons.model.Capability;
import commons.model.Device;
import commons.model.Gateway;
import commons.model.Sensor;
import commons.model.SensorType;
import commons.model.gateway.Measure;

public class ArtifactFactory extends StockTickerArtifactFactory {
//Provides generically-named 'BuildXXX' methods for the calling class (CreateArtifacts) to call
//It then translates the calls into the Device-specific method calls to the extended class 'below'

	public ArtifactFactory() {
		super();
	}

	//**********************************************************
	//CAPABILITIES
			//CONSTRUCTION
			public static String getMeasureCapabilityName() {
				return StockTickerArtifactFactory.getStockMarketMeasureCapabilityName();
			}
		
			public static Capability buildMeasureCapability() {
				return StockTickerArtifactFactory.buildStockMarketMeasureCapability();
			}

			//DEMO RUN
			public static Capability buildDemoMeasureCapability() {
				return StockTickerArtifactFactory.buildStockMarketMeasureCapability();
			}
			
	//MEASURE - DEMO RUN
			public static Measure buildDemoMeasure(String sensorAlternateId, String measureCapabilityAlternateId) {
				return StockTickerArtifactFactory.buildDemoTickerMeasure(sensorAlternateId, measureCapabilityAlternateId);
			}
			
	//MEASURE - REAL RUN
			public static Measure buildMeasure(String sensorAlternateId, String measureCapabilityAlternateId, String[] csvRecord) {
				return StockTickerArtifactFactory.buildTickerMeasure(sensorAlternateId, measureCapabilityAlternateId, csvRecord);
			}
			

		//COMMAND
		public static String getCommandCapabilityName() {
			return StockTickerArtifactFactory.getStockMarketCommandCapabilityName();
		}

		public static Capability buildCommandCapability() {
			return StockTickerArtifactFactory.buildStockMarketCommandCapability();
		}

	//SENSOR TYPE
	public static String getSensorTypeName() {
		return StockTickerArtifactFactory.getStockMarketSensorTypeName();
	}

	public static SensorType buildSensorType( Capability measureCapability, Capability commandCapability ) {
		return StockTickerArtifactFactory.buildStockMarketSensorType( measureCapability, commandCapability );
	}

	//DEVICE
	public static String getDeviceName( String deviceSuffix ) {
		return StockTickerArtifactFactory.getStockMarketDeviceName( deviceSuffix );
	}

	public static Device buildDevice(  String gatewayId, String nameSuffix  ) {
		return StockTickerArtifactFactory.buildStockMarketDevice( gatewayId, nameSuffix );
	}

	//SENSOR
	public static String getSensorName( int deviceInstanceNo ) {
		String baseName = StockTickerArtifactFactory.getStockMarketSensorName();
		return baseName + addSuffix( deviceInstanceNo );
	}

	public static Sensor buildSensor( String deviceId, String sensorTypeId, String nameSuffix ) {
		return StockTickerArtifactFactory.buildStockMarketSensor( deviceId, sensorTypeId, nameSuffix );
	}

	//General
	
	//needs to be 'protected' because: called by subclass 'CreateArtifacts' directly
	protected static String addSuffix( int deviceInstanceNo ) {
		String suffix = new DecimalFormat("00").format( deviceInstanceNo ); 
		return "_" + suffix;
	}
}
