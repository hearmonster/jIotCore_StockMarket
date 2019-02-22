package StockTickerIOT;

import java.io.IOException;
import java.security.KeyStore;

import commons.ArtifactFactory;
import commons.Helpers;
import commons.SampleException;
import commons.SecurityUtilX;
import commons.init.properties.DeviceProperties;
import commons.model.Capability;
import commons.model.Device;
import commons.model.Gateway;
import commons.model.Sensor;
import commons.model.SensorType;
import commons.utils.Console;


public class CreateArtifacts extends ArtifactFactory {
	//'ArtifactFactory' in turn extends the '<Device-specific>ArtifactFactory' class
	protected DeviceProperties deviceProperties;
	protected CoreServiceX coreService;
	int deviceCount;

	public CreateArtifacts( CoreServiceX coreService, int deviceCount, Gateway gateway ) throws SampleException {

		// number of devices to create
		this.deviceCount = deviceCount;
		this.coreService = coreService;
		
		try {
			Console.printSeparator();
			Console.printText( "Searching for Measure Capability...");

			Capability measureCapability = coreService.getOrAddMeasureCapability();

			
			Console.printSeparator();
			Console.printText( "Searching for Command Capability...");
			
			Capability commandCapability = coreService.getOrAddCommandCapability();
			
			Console.printSeparator();
			Console.printText( "Searching for SensorType...");
			
			SensorType sensortype = coreService.getOrAddSensorType( measureCapability, commandCapability);

			int deviceInstanceNo = 0;
			while ( deviceInstanceNo < deviceCount ) {
				Device device = null;
				Sensor sensor = null;
				
				deviceInstanceNo ++;
				String deviceSuffix = Helpers.buildSuffix( deviceInstanceNo );
				
				Console.printSeparator();
				Console.printText( String.format( ">>>>>>>>> Configuring Device # %1$s... <<<<<<<<<", deviceInstanceNo ) );
				
				//read the RESPECTIVE 'device_XX.properties' Properties file
				deviceProperties  = new DeviceProperties( deviceSuffix );

				// Look for a Device ID >> If you find one, look up the device BY *ID*
				String deviceId = deviceProperties.getDeviceId();
				if ( deviceId != null )
					device = coreService.getDeviceById( deviceId, gateway );

				// If the device is still null, then we were unable to find it by ID, then fall back to below...
				if ( device == null ) {
					device = coreService.getOrAddDevice( gateway, deviceSuffix );
				}

				// If the device is *still null*, then we've really hit a problem...
				if ( device == null ) {
					Console.printError( "Unable to neither find nor create the device!" );
					throw new SampleException();
				}

				Console.printNewLine();
				Console.printText( String.format("IoT Core <Device ID>: %1$s", device.getId() ) );

				Console.printSeparator();
				Console.printText( "Searching for Device's keystore..." );

				// Test open the Device's keystore
				String keystoreSecret = deviceProperties.getKeystoreSecret(); 
				//A successful open, will return the Keystore object
				KeyStore ks = SecurityUtilX.openPkcs12Keystore( deviceInstanceNo, keystoreSecret );
				///Otherwise...
				if ( ks == null ) {
					// Create the Device's Keystore
					// grab the downloaded secret, because you'll need this, to persist in the Device's Properties file later
					keystoreSecret = SecurityUtilX.downloadPkcs12Keystore( coreService, device, deviceInstanceNo);
				}
				
				Console.printSeparator();
				Console.printText( "Searching for Sensor (to add to my Device)..." );
				
				// Look for a Sensor ID >> If you find one, look up the sensor BY *ID*
				String sensorId = deviceProperties.getSensorId();
				if ( sensorId != null ) {
					sensor = coreService.getSensorById( sensorId );
				}
						
				// If you can't find it by ID, then fall back to below...
				if ( sensor == null ) {
					sensor = coreService.getOrAddSensor( device, sensortype, deviceInstanceNo );
				}

				// If the sensor is *still null*, then we've really hit a problem...
				if ( sensor == null ) {
					Console.printError( "Unable to neither find nor create the sensor!" );
					throw new SampleException();
				}

				Console.printNewLine();
				Console.printText( String.format("IoT Core <Sensor ID>: %1$s", sensor.getId() ) );

				//Persist all the DeviceID, SensorID, and Keystore Secret properties for easy look up, next time
				//(BTW Keystore File Name == '<DeviceName>.pkcs12')
				deviceProperties.writeDeviceProperties( device, sensor, measureCapability, keystoreSecret);

			} //end while
			Console.printSeparator();
			Console.printText( "Created/Validated all Devices." );

	//Finishing with catch block from 'SampleApp'...
		//import java.security.GeneralSecurityException;

		} catch (IOException /*| GeneralSecurityException */ | IllegalStateException e) {
			throw new SampleException(e.getMessage());
		}

	}


	
}

