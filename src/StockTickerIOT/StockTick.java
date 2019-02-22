package StockTickerIOT;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// The StockTick class is nothing more than a Java Class representation of the Event's 'schema type'
// It only has setters (not even getters) because...
// The setters all return their casted values anyway (It's not even an Object representation, just a bunch of mapping/interpreting methods)

//But... The StockTick class *could*
//		a) inherit the CSVProducer Class, (in order to Overload's runnable method) to
//		b) "interpret" the CSV file's columns and MAP them into each object's schema

public class StockTick {

	private Date dTs;
	private double dPrice;
	private int iVolume;
	private String sTickSymbol;

    static Logger logger = LoggerFactory.getLogger( SendMeasures.class );

	public Date setTimestamp(String sDate, String sLogInfo ) {
		return setTimestamp( sDate, sLogInfo, "yyyy/MM/dd HH:mm:ss" );
	}

	public Date setTimestamp(String sDate, String sLogInfo, String sFormat ) {
		SimpleDateFormat ft = new SimpleDateFormat( sFormat );

		try {
			dTs = ft.parse( sDate );
		}
		catch (Exception parseException) {
			logger.error( "Cannot parse datetime: [" + sLogInfo + "]");
			dTs = null;
		}

		return dTs;
	}

	public Double setPrice(String sPrice, String sLogInfo ) {

		try {
    		dPrice = Double.parseDouble( sPrice );
    	}
    	catch (Exception parseException) {
    		logger.error( "Cannot parse price: [" + sLogInfo + "]");
    		dPrice = -1;
    	}

		return dPrice;
	}

	public Integer setVolume(String sVolume, String sLogInfo ) {

		try {
    		iVolume = Integer.parseInt( sVolume );
    	}
    	catch (Exception parseException) {
    		logger.error( "Cannot parse Volume: [" + sLogInfo + "]");
    		iVolume = -1;
    	}
		return iVolume;
	}

	public String setSymbol(String sSymbol) {
		sTickSymbol = sSymbol;
		
		return sTickSymbol;
	}

}
