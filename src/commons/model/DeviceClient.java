package commons.model;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.DecimalFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSocketFactory;

import commons.init.properties.DevicePropertiesX;
import commons.SampleException;
import commons.api.GatewayCloudMqttX;
import commons.model.AuthenticationX;
import commons.model.Device;
import commons.model.gateway.Measure;
import commons.utils.Console;

//A DeviceClient is an Uber-"Device"
//It's a regular Device object but also hangs on additional stuff necessary to connect to the IoT Core Service such as
// * It's keystore/secret and Truststore
// * The hostname of the IoT Core Service it needs to point to
// * (And it's associated Properties that ultimately end up stuffed into the relevant fields in the object)
//It's a threaded-object.  The init "spins it up", the "run()/start()" puts it into "listen" mode.  Calling it's "stop()" method - signals the thread to come to an eventual stop
public class DeviceClient extends Device implements Runnable {

	protected DevicePropertiesX deviceProperties;
	protected GatewayCloudMqttX gatewayCloud;
	protected String iotCoreUrlHostname;
	protected String suffix;  //e.g. "_01" - used to construct Device names, Sensor names and Keystore names
	protected AuthenticationX authentication;	//stores all my security info (Keystores, secrets, Truststores etc)
	protected SSLSocketFactory sslSocketFactory;
	protected String deviceAlternateId;
	protected String sensorAlternateId;
	protected String measureCapabilityAlternateId;
	protected String commandCapabilityAlternateId;
	protected String keystoreSecret;
	protected int deviceInstanceNo;	//used during logging for identification of this particular instance

	//Make dummy artifacts (because the MQTTClient and GatewayCloudMqttX classes expect them)
	//Note: Only the 'xxxAlternateId' fields in each one need to be populated!
	//protected Device device;  THIS CLASS ALREADY EXTENDS ONE!
	//protected Sensor sensor;
	//protected Capability measureCapability;
	//protected Capability commandCapability;

    private boolean threadKeepRunning = true;

    public synchronized void doStopThread() {
    	Console.printText( "DeviceClient::doStopThread::Signal STOP.");
        this.threadKeepRunning = false;
    }

    private synchronized boolean isKeepRunning() {
        return (this.threadKeepRunning == true);
    }

    //initialize the DeviceTwin and send some test measures.  But don't put it into "listen" mode yet
	public DeviceClient( int deviceInstanceNo, String iotCoreUrlHostname ) throws SampleException {
		Console.printSeparator();
		Console.printText( String.format( "DeviceClient::Constructor::>>>>>>>>> Initializing DeviceClient # %1$s... <<<<<<<<<", deviceInstanceNo ) );

		//set object fields
		this.iotCoreUrlHostname = iotCoreUrlHostname;
		this.suffix = buildSuffix( deviceInstanceNo );
		this.deviceInstanceNo = deviceInstanceNo;
		
		//read the RESPECTIVE 'device_XX.properties' Properties file
		this.deviceProperties  = new DevicePropertiesX( this.suffix );  //passing in just "_01" will read the "devices_01.properties" file

		// Grab the essential Properties from the device_xx.properties file
		this.deviceAlternateId     			= deviceProperties.getDeviceAltId();
		this.sensorAlternateId     			= deviceProperties.getSensorAltId();
		this.measureCapabilityAlternateId	= deviceProperties.getMeasureCapabilityAltId();
		this.keystoreSecret        			= deviceProperties.getKeystoreSecret();

		/*
		//Make a dummy Device, Sensor and Capability object (because the MQTTClient and GatewayCloudMqttX classes expect one)
		//Only the 'AlternateId' fields need to be populated!
		//device = new Device();  THIS CLASS ALREADY EXTENDS 'Device'!
		this.setAlternateId( deviceAlternateId );
		
		Sensor[] sensors = new Sensor[0];	//create a new Sensor array (of one)
		this.setSensors( sensors );  		// hang the sensor array on the Device
		
		sensors[0].setAlternateId( sensorAlternateId );  //set the AltId of the one and only sensor within the Device
		
		this.measureCapability = new Capability();
		this.measureCapability.setAlternateId( capabilityAlternateId );
		*/
		
		this.authentication = new AuthenticationX( this.suffix, this.keystoreSecret );
		
		try {
			this.sslSocketFactory = this.authentication.buildSSLSocketFactory();  //may throw 'GeneralSecurityException'
			
			//Assuming MQTT only (no REST)
			this.gatewayCloud = new GatewayCloudMqttX( this.deviceAlternateId, this.sslSocketFactory ); 

			Console.printSeparator();
			Console.printText( "Connect to URL...");
			gatewayCloud.connect( iotCoreUrlHostname );

			Console.printSeparator();

			//Fire off ~5 test messages -- DISABLED!
			//sendDemoMeasures( this.iotCoreUrlHostname, this.sensorAlternateId, this.measureCapabilityAlternateId );

			///receiveAmbientMeasures(measureCapability, device);


		} catch (IOException | GeneralSecurityException /* | IllegalStateException */ e ) {
			throw new SampleException(e.getMessage());
		}
	} //end: Constructor

	
	
	public void shutdown() {
		gatewayCloud.disconnect();
		Console.printSeparator();
		Console.printText( "Disconnected from URL.");
	}

	
	//called by MqTT Gateway
	public SSLSocketFactory getSslSocketFactory() {
		return this.sslSocketFactory;
	}
	
	
	@Override
	public void run() {
		String threadName = Thread.currentThread().getName();
		Console.printText( String.format( "DeviceClient::run::Starting thread '%1$s'.", threadName ) );
		
		/* should be already connected...
		try {
			gatewayCloud.connect( iotCoreUrlHostname );
		} catch (IOException e) {
			Console.printError( String.format( "DeviceClient::run::Unable to connect to the Gateway Cloud at $1$s, Details: %2$s", iotCoreUrlHostname, e) );
			doStopThread();
		}
		*/

		while( isKeepRunning() ) {
            // keep doing what this thread should do.
            System.out.println("DeviceClient::run::Thread Running");

            try {
                Thread.sleep(3L * 1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } //end: while

		gatewayCloud.disconnect();

		Console.printText( String.format( "DeviceClient::run::Done running thread '%1$s'.", threadName ) );

	} //end: run()

	
	public void receiveCsvRecord( String[] csvRecord ) {
/* debug...
		String formattedRecord = "";
		for (int i = 0; i < record.length; i++) {
			//Console.printText( String.format( "Field %1$s\tValue: %2$s", i, csvRecord[ i ]) );
			formattedRecord += csvRecord[ i ].concat("\t");
		}
		Console.printText( String.format( "Message received by instance # %1$s: %2$s", this.deviceInstanceNo, formattedRecord ) );;
*/
		//TODO convert it into a measure for *this* Device instance, and propagate into the IoT Core Service
		Measure demoMeasure = ArtifactFactory.buildDemoMeasure( sensorAlternateId,  measureCapabilityAlternateId);
		Measure measure = ArtifactFactory.buildMeasure( sensorAlternateId,  measureCapabilityAlternateId, csvRecord);
		
		try {
			gatewayCloud.sendMeasure(measure);
		} catch (IOException e) {
			Console.printError(e.getMessage());
		} finally {
			Console.printSeparator();
		}

	}
	
	
	public void sendDemoMeasures( String iotCoreUrlHostname, String sensorAlternateId, String measureCapabilityAlternateId )
	throws IOException {
	
		Console.printSeparator();
		Console.printText( "DeviceClient::sendDemoMeasures::Sending several test measures..." );

		/*
		try {
			gatewayCloud.connect( iotCoreUrlHostname );
		} catch (IOException e) {
			throw new IOException( String.format( "DeviceClient::sendDemoMeasures::Unable to connect to the Gateway Cloud at $1$s", iotCoreUrlHostname), e);
		}
		*/
	
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(new Runnable() {
	
			@Override
			public void run() {
				Measure measure = ArtifactFactory.buildDemoMeasure( sensorAlternateId, measureCapabilityAlternateId );
	
				try {
					gatewayCloud.sendMeasure(measure);
				} catch (IOException e) {
					Console.printError(e.getMessage());
				} finally {
					Console.printSeparator();
				}
			}
	
		}, 0, 1000, TimeUnit.MILLISECONDS);
	
		try {
			executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			throw new IOException("DeviceClient::sendDemoMeasures::Interrupted exception", e);
		} finally {
			executor.shutdown();
			//gatewayCloud.disconnect();
		} //end: try/catch/finally
		
		Console.printText( "DeviceClient::sendDemoMeasures::Done Sample Measuures." );

	} //end: sendAmbientMeasures()

	
	
	//Used by Properties and Keystores to create a unique filename for each device e.g.
	// "device_02.properties" or "device_05.pkcs12"
	public static String buildSuffix( int deviceInstanceNo ) {
		String suffix = new DecimalFormat("00").format( deviceInstanceNo ); 
		return "_" + suffix;
	}

	
	
	public static void main(String[] args) {
		//test initialization with a single device "xxxx_01"
		final String iotCoreUrlHostname = "2f7241c1-8671-4591-9de0-8c64ed90e10e.canary.cp.iot.sap";
		//ClusterPropertiesX clusterProperties = new ClusterPropertiesX();
		//String iotCoreUrlHostname = clusterProperties.getIotHost();
		
		final int deviceCount = 1;

		DeviceClient[] deviceTwin = null;
		Thread[] deviceTwinThreads = null;
		
		try {
			//int deviceCount = clusterProperties.getDeviceCount();
			
			Console.printText( "DeviceClient::Main::Starting DeviceTwin cluster initialization...");
			Console.printSeparator();
			
			deviceTwin = new DeviceClient[ deviceCount+1 ];  //ignore zero, start from one...
			for (int i = 1; i <= deviceCount; i++) {  //starting at 1 (not zero)
				deviceTwin[ i ] = new DeviceClient( i, iotCoreUrlHostname );
			} //end: for

			Console.printSeparator();
			Console.printText( "DeviceClient::Main::Spin up DeviceTwin cluster...");
			deviceTwinThreads = new Thread[ deviceCount+1 ];  //ignore zero, start from one...
			for (int i = 1; i <= deviceCount; i++) {  //starting at 1 (not zero)
				deviceTwinThreads[ i ] = new Thread( deviceTwin[ i ], String.format( "DeviceTwin Thread: # %1$s", i ) );
				deviceTwinThreads[ i ].start();   // start(). not run()!
			} //end: for

/*					
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(1);
*/
		} catch (SampleException e) {
			Console.printError(String.format("DeviceClient::Main::Unable to re-initialize the IoT Core artifacts - %1$s", e.getMessage()));
			System.exit(1);
		}


		Console.printText( "DeviceClient::Main::Sleeping for 10 secs.." );
		try {
			Thread.sleep(10*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = 1; i <= deviceCount; i++) {  //starting at 1 (not zero)
			deviceTwin[ i ].doStopThread();
		} //end: for
		
		
		//TODO Disabled (Cluster Properties don't change)
		//clusterProperties.writeProperties();
		Console.printSeparator();
		Console.printText( "DeviceClient::Main::Finished Main." );
		
	}//end: main()

}
