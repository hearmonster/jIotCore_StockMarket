package commons.init.properties;

import java.util.ArrayList;

import com.google.gson.Gson;
import commons.model.Capability;
import commons.model.Device;
import commons.model.Property;
import commons.model.Sensor;

/**
 * A >>>Device<<<-specific properties implementation of the 'AbstractPropertiesHelperX' class
 */
public class DevicePropertiesX extends AbstractPropertiesHelperX {

	protected static final String propertiesFileType = "device"; //i.e. "device_xx.properties"  e.g. "device_01.properties"

	protected static final String DEVICE_ID = "device.id";
	protected static final String DEVICE_ALT_ID = "device.alt.id";
	protected static final String SENSOR_ID = "sensor.id";
	protected static final String SENSOR_ALT_ID = "sensor.alt.id";
	protected static final String MEASURE_CAPABILITY_ID = "capability.id.measure";
	protected static final String MEASURE_CAPABILITY_ALT_ID = "capability.alt.id.command";
	protected static final String COMMAND_CAPABILITY_ID = "capability.id.measure";
	protected static final String COMMAND_CAPABILITY_ALT_ID = "capability.alt.id.command";
	protected static final String SENSOR_TYPE_ID = "sensor.type.id";
	protected static final String KEYSTORE_SECRET = "keystore.secret";
	protected static final String MEASURE_CAPABILITY_PROPERTIES = "measure.capability.properties";


	public DevicePropertiesX( String deviceSuffix  ) {
		//propertiesFile is specific to device #  e.g. "device_01.properties";
		super( propertiesFileType + deviceSuffix );
		
		HIDDEN_KEYS.add( "IOT_PASSWORD" );
		
		KEYS = new ArrayList<String[]>();

		KEYS.add(new String[] {"DEVICE_ID",			"device.id",	  "OPTIONAL",	"Device ID (e.g. '32' or leave empty): "	});
		KEYS.add(new String[] {"SENSOR_ID",			"sensor.id",	  "OPTIONAL",	"Sensor ID (e.g. '32' or leave empty): "	});
		KEYS.add(new String[] {"SENSOR_TYPE_ID",	"sensor.type.id", "OPTIONAL",	"Sensor Type ID (e.g. '32' or leave empty): "	});
		KEYS.add(new String[] {"CAPABILITY_ID",		"capability.id",  "OPTIONAL",	"Capability ID (e.g. '32' or leave empty): "	});

		//promptProperties();	//will fill in any missing mandatory values
		printProperties();	
}

	// GETTERS
	public String getDeviceId() {
		return properties.getProperty( DEVICE_ID );
	}

	public String getDeviceAltId() {
		return properties.getProperty( DEVICE_ALT_ID);
	}

	public String getSensorId() {
		return properties.getProperty( SENSOR_ID);
	}

	public String getSensorAltId() {
		return properties.getProperty( SENSOR_ALT_ID);
	}

	public String getSensorTypeId() {
		return properties.getProperty( SENSOR_TYPE_ID );
	}

	public String getMeasureCapabilityId() {
		return properties.getProperty( MEASURE_CAPABILITY_ID );
	}

	public String getMeasureCapabilityAltId() {
		return properties.getProperty( MEASURE_CAPABILITY_ALT_ID );
	}

	public String getCommandCapabilityId() {
		return properties.getProperty( COMMAND_CAPABILITY_ID );
	}

	public String getCommandCapabilityAltId() {
		return properties.getProperty( COMMAND_CAPABILITY_ALT_ID );
	}

	public String getKeystoreSecret() {
		return properties.getProperty( KEYSTORE_SECRET );
	}


	// SETTERS
	public void setDeviceId( String deviceId ) {
		properties.setProperty( DEVICE_ID, deviceId ) ;
	}

	public void setDeviceAltId( String deviceAltId ) {
		properties.setProperty( DEVICE_ALT_ID, deviceAltId ) ;
	}

	public void setSensorId( String sensorId ) {
		properties.setProperty( SENSOR_ID, sensorId );
	}

	public void setSensorAltId( String sensorAltId ) {
		properties.setProperty( SENSOR_ALT_ID, sensorAltId );
	}

	public void setSensorTypeId( String sensorTypeId ) {
		properties.setProperty( SENSOR_TYPE_ID, sensorTypeId );
	}

	public void setMeasureCapabilityId( String capabilityId ) {
		properties.setProperty( MEASURE_CAPABILITY_ID, capabilityId );
	}

	public void setMeasureCapabilityAltId( String capabilityAltId ) {
		properties.setProperty( MEASURE_CAPABILITY_ALT_ID, capabilityAltId );
	}

	public void setCommandCapabilityId( String capabilityId ) {
		properties.setProperty( COMMAND_CAPABILITY_ID, capabilityId );
	}

	public void seCommandCapabilityAltId( String capabilityAltId ) {
		properties.setProperty( COMMAND_CAPABILITY_ALT_ID, capabilityAltId );
	}

	public void setKeystoreSecret( String secret ) {
		properties.setProperty( KEYSTORE_SECRET, secret );
	}

	
	/**
	 * @param measureCapability
	 * @param measureCapabilityProperties
	 * @param device
	 * @param sensor
	 * @param keystoreSecret
	 */
	public void writeDeviceProperties( Device device, Sensor sensor, Capability measureCapability, String keystoreSecret) {
		//Grab the Property Values directly from their respective Objects
		String deviceId = device.getId();				//Very useful to look up the Device by its unique ID
		String deviceAltId = device.getAlternateId();	//Alt ID is necessary to send messages via MQTT Gateway
		String sensorId = sensor.getId();				//Very useful to look up the Sensor by its unique ID
		String sensorAltId = sensor.getAlternateId();	//Alt ID is necessary to send messages via MQTT Gateway
		String capabilityAltId = measureCapability.getAlternateId();	//Alt ID is necessary to send messages via MQTT Gateway

		//Set to Properties (in memory) first
		setDeviceId( deviceId );
		setDeviceAltId( deviceAltId );
		setSensorId( sensorId );
		setSensorAltId( sensorAltId );
		setMeasureCapabilityAltId( capabilityAltId );
		setKeystoreSecret( keystoreSecret );
		setMeasureCapabilityProperties(measureCapability);
		
		//now commit all Properties to disk
		super.writeProperties();
	}

	/**
	 * Serializes the array of Properties attached to a Capability into a single string for easy viewing within the device's Properties file
	 * @param measureCapability
	 */
	public void setMeasureCapabilityProperties(Capability measureCapability) {
		Property[] capabilityProperties = measureCapability.getProperties();
		Gson gson = new Gson();
		String capabilityProperties_Json = gson.toJson(capabilityProperties);

		setProperty( MEASURE_CAPABILITY_PROPERTIES, capabilityProperties_Json );
	}

	
}