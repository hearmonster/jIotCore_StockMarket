package StockTickerIOT;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import com.opencsv.CSVReader;

import commons.SampleException;
import commons.init.properties.ClusterPropertiesX;
import commons.model.DeviceClient;
import commons.utils.Console;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendMeasures {

	public SendMeasures() {
		// TODO Auto-generated constructor stub
	}

    private static final String CSVFILE1 = "resources/stock-trades.csv";

    static Logger logger = LoggerFactory.getLogger( SendMeasures.class );

	public static void main(String[] args) {

		ClusterPropertiesX clusterProperties = new ClusterPropertiesX();
		String iotCoreUrlHostname = clusterProperties.getIotHost();
		Console.printText( String.format( "SendMeasures::Main::IoT Core Service URL: %1$s", iotCoreUrlHostname ) );
		
		int deviceCount = clusterProperties.getDeviceCount();  //limited to between 1 and 10
		Console.printText( String.format( "SendMeasures::Main::Number of devices: %1$s", deviceCount ) );

		DeviceClient[] deviceTwin = null;
		Thread[] deviceTwinThreads = null;
		
		try {
			Console.printText( "SendMeasures::Main::Starting DeviceTwin cluster initialization...");
			Console.printSeparator();
			
			deviceTwin = new DeviceClient[ deviceCount+1 ];  //ignore zero, start from one...
			for (int i = 1; i <= deviceCount; i++) {  //starting at 1 (not zero)
				deviceTwin[ i ] = new DeviceClient( i, iotCoreUrlHostname );
			} //end: for

/* Disable multi-threaded Devices
			//loop through DeviceTwin cluster, spinning up Devices
			Console.printSeparator();
			Console.printText( "SendMeasures::Main::Spin up DeviceTwin cluster...");
			deviceTwinThreads = new Thread[ deviceCount+1 ];  //ignore zero, start from one...
			for (int i = 1; i <= deviceCount; i++) {  //starting at 1 (not zero)
				deviceTwinThreads[ i ] = new Thread( deviceTwin[ i ], String.format( "DeviceTwin Thread: # %1$s", i ) );
				//deviceTwinThreads[ i ].start();   //(multi-threaded version) Note: "start()", not "run()"!
			} //end: for
*/
		} catch (SampleException e) {
			Console.printError(String.format("SendMeasures::Main::Unable to re-initialize the IoT Core artifacts - %1$s", e.getMessage()));
			System.exit(1);
		}

		Console.printSeparator();
		Console.printText( "SendMeasures::Main::Sending several test measures from CSV file..." );

		CSVProducer csvProducer1 = new CSVProducer();
		Thread tProducer1 = new Thread( csvProducer1, "CSVProducer"  );

		StockTick dummyTick = new StockTick();	//all I need is the 'setTimestamp' method to parse the Timestamp string

		Integer iReplayRate = 1;

		try {
			FileReader csvFile = new FileReader(CSVFILE1);
        	CSVReader reader = new CSVReader( csvFile, ',', '\'', 1 );  //comma-separated, escape, skip 1st line (header)
/*
	        String[] sFirstLine;
	        Date dFirstTs = null;

	        sFirstLine = reader.readNext();
        	dFirstTs = dummyTick.setTimestamp( sFirstLine[0], "SendMeasures::Main::First line" );
        	logger.debug( "::Main:: First Ts: [" + dFirstTs + "]");
*/
        	csvProducer1.setFile( reader );
        	csvProducer1.setReplayRate( iReplayRate );
        	csvProducer1.setDeviceTwinCluster( deviceTwin );
        	tProducer1.start();
        }
        catch (FileNotFoundException fileNotFoundException) {
    		logger.error( "SendMeasures::Main::File not found @'new FileReader(".concat(CSVFILE1).concat(")'" ) );
        }
		catch (IOException ioe) {
    		logger.error( "SendMeasures::Main::File not found @'reader.readNext()'" );
		}
		
		while( tProducer1.getState() != Thread.State.TERMINATED ) { 
		
			Console.printText( "SendMeasures::Main::Waiting for CSV thread to complete. Sleeping for 10 secs.." );
			try {
				Thread.sleep(10*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} //end: while

		Console.printText( "SendMeasures::Main::Done sending CSV Measuures." );
		Console.printSeparator();
		Console.printText( "SendMeasures::Main::Shutting down cluster." );

		
		//loop through DeviceTwin cluster, shutting down Devices
		for (int i = 1; i <= deviceCount; i++) {  //starting at 1 (not zero)
			//deviceTwin[ i ].doStopThread();  //multi-threaded version
			deviceTwin[ i ].shutdown();
		} //end: for
		
		
		//TODO Disabled (Cluster Properties don't change)
		//clusterProperties.writeProperties();
		Console.printSeparator();
		Console.printText( "SendMeasures::Main::Finished Main." );

				
	}//end: main()
	
} //end: class
