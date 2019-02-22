package StockTickerIOT;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.opencsv.CSVReader;
//import com.opencsv.CSVWriter;

import commons.model.DeviceClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// 'CVSProducer' is a multi-threaded Object (i.e. has a 'run' method) that represents an event source
// It has two private attributes
//	- a pointer to a CSV file object
//	- a Replay Rate

// It is responsible for iterating through the CSF file and firing events at the appropriate times
//	(taken from a specific column within record in the file)

// It was originally designed to be generic so that each CSV file would get its own instance, but that hasn't worked out  :o(
// It maps the schema of the CSV file onto a Class.
// That means it's tied to the StockTicker class, so really it should be named, "StockTickerProducer")

public class CSVProducer implements Runnable {

	CSVReader 		csvFile = null;
	Integer 		iReplayRate = null;
	DeviceClient[]	deviceTwin = null;

    static Logger logger = LoggerFactory.getLogger( CSVProducer.class );

	public void setFile (CSVReader csvFileParam ) {
		this.csvFile = csvFileParam;
    }

	public void setReplayRate(Integer iReplayRateParam) {
		this.iReplayRate = iReplayRateParam;
	}

	public void setDeviceTwinCluster( DeviceClient[] deviceTwinParam ) {
		this.deviceTwin = deviceTwinParam;
	}
	
	//CONSTRUCTOR
	/*
	CSVProducer( CSVReader csvFileParam, Integer iReplayRateParam, DeviceClient[] deviceTwinParam ) {
		this.csvFile = csvFileParam;
		this.iReplayRate = iReplayRateParam;
		this.deviceTwin = deviceTwinParam;
	}
	*/


	private Integer calcAndSleep(Date dLastTs, Date dTs ) {
		if ( dLastTs == null) dLastTs = dTs;

		Integer iSleepOriginalMS, iSleepAdjustedMS;

		iSleepOriginalMS = (int) ( dTs.getTime() - dLastTs.getTime() );
		iSleepAdjustedMS =  iSleepOriginalMS / iReplayRate;
		logger.debug("::calcAndSleep Sleep (Original): [" + iSleepOriginalMS + " milliseconds]\t Sleep (Replay Adjusted): [" + iSleepAdjustedMS + " milliseconds]" );
		try {
			//if ( iSleep ) > 0
			Thread.sleep( iSleepAdjustedMS );
		}
		catch (Exception interuppException) {
			logger.error( "::calcAndSleep  Sleep exception");
		}
		return iSleepAdjustedMS;

	}

	public void run() {
        try {
        	//CSVReader reader = new CSVReader(new FileReader(ADDRESS_FILE));
	        String[] nextLine;
	        Date dLastTs = null;

	        Integer iRowNum = 2;

	        //Prepare DeviceTwin RoundRobin (clumsy, but incredibly simple)
    		List<Integer> deviceTwinList = Arrays.asList(1, 2);

	        while ((nextLine = csvFile.readNext()) != null) {
	        	Date dTs;
	        	Double dPrice;
	        	Integer iVolume, iSleep;
	        	String sTicker;

	        	StockTick tick = new StockTick();

	        	dTs = tick.setTimestamp( nextLine[0], iRowNum.toString() );
	        	iSleep = calcAndSleep( dLastTs, dTs );
	        	dPrice = tick.setPrice( nextLine[2], iRowNum.toString() );
	        	iVolume = tick.setVolume( nextLine[3], iRowNum.toString() );
	        	sTicker = tick.setSymbol( nextLine[1] );

	        	dLastTs = dTs;
	        	iRowNum++;

	        	//Firing off the event will go here...
	        	int nextDeviceTwinIdx = deviceTwinList.get( 0 );
	    		deviceTwin[ nextDeviceTwinIdx ].receiveCsvRecord( nextLine );
	    				
    			//System.out.println( deviceTwinList.get( 0 ) );
	            logger.info( "::run  Ts: [" + dTs + "]\tTicker: [" + sTicker + "]\tPrice: [" + dPrice + "]\tVolume: [" + iVolume + "]");

    			//Rotate the list (Round Robin)
    			Collections.rotate( deviceTwinList, -1 );
	        }

        }
        catch (IOException IOException) {
        	logger.error( "::run  File IOException");
        }

        logger.info( "::run  Completed CSV file read." );
        //Note:  no need to terminate the thread - it will end naturally here

	}


}

