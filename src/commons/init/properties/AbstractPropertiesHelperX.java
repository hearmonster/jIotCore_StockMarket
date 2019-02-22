package commons.init.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import commons.connectivity.ProxySelector;
import commons.utils.Console;
import commons.utils.Constants;
import commons.utils.FileUtilX;

/**
 * An abstraction helper for all Properties file management.
 * The constructor takes a filename and will attempt to read the associated properties file.
 * Provides 
 * 		- A 'promptProperties()' method for any missing mandatory properties, or in the event that the file doesn't exist/cannot be read
 * 		- A 'printProperties()' method for spitting out all the properties within a properties bag
 * 			(optional: can take a set of fields that should remain hidden i.e. passwords ) 
 */
public abstract class AbstractPropertiesHelperX {

	//Class fields
	protected Properties properties;
	protected String propertiesFilename;  // filename only e.g. "device.properties" - no path expected

	protected ArrayList<String[]> KEYS = new ArrayList<String[]>();  //String ArrayList (UNknown number of fields) of Primitive String arrays (known number of fields)
	protected Set<String> HIDDEN_KEYS = new HashSet<String>();
	
	/*
	enum KeyType	//Enumeration defined
	{
	 MANDATORY, OPTIONAL
	}
	*/
	

	// Constructor 
	// 'propertiesType' will include the suffix (in the case of a Device) but not in the case of a Cluster
	public AbstractPropertiesHelperX( String propertiesType ) {
		//Construct the filename to read
		String propertiesFilename = String.format( "%1$s.properties", propertiesType );
		this.propertiesFilename = propertiesFilename;
		readFile();
	}





	/**
	 * Reads the configuration properties from the file located in the same directory to JAR archive. Sticks to the
	 * empty properties collection if the configuration file does not exist.
	 */
	private void readFile() {
		String propertiesFilePath = FileUtilX.getRuntimePath().concat( propertiesFilename );
		Console.printNewLine();
		Console.printText( String.format("Looking for Properties file: %1$s", propertiesFilePath ) );

		/*
		File jar = new File(AbstractPropertiesHelperX.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		String path = jar.getParentFile().getAbsolutePath().concat(System.getProperty( "file.separator" )).concat( propertiesFilename );
		Console.printText( String.format( "Looking for properties file here: %1$s", path ) );
		
		try {
			propertiesFilePath = URLDecoder.decode(path, Constants.DEFAULT_ENCODING.name());
		} catch (UnsupportedEncodingException e) {
			Console.printWarning("Unable to decode config file path.");
		}
		*/
		File config = new File( propertiesFilePath);

		properties = new Properties();

		try {
			if (config.exists()) {
				properties = FileUtilX.readProperties(new FileInputStream(config));
				Console.printText( "Properties read." );
			}
		} catch ( IOException e ) {
			// do nothing - assume that we'll prompt for any properties we'll need (using the 'promptProperties()' method)
		} finally {
			//promptProperties();
			//printProperties();
		}
	}

	/**
	 * Prompts the user for missing configuration properties.
	 * For each instance of a Properties file, it determines which properties are mandatory and which are not
	 */
	public void promptProperties() {
		Console console = Console.getInstance();

		for (String[] propertyStringArray : KEYS) {
			String propKey = propertyStringArray[1];
			boolean propIsMandatory = propertyStringArray[2].contentEquals("MANDATORY");
			String propHelpText = propertyStringArray[3];
			String propValue = properties.getProperty( propKey );
			if ( propIsMandatory ) 
				//propValue = console.awaitNextLine( propValue, propHelpText );
				propValue = console.nextLine( propKey, propHelpText );
			else
				propValue = console.nextLine( propValue, propHelpText );
			properties.setProperty( propKey, propValue );
		}
	}
	

	public void setProxy() {
		String proxyHost = properties.getProperty( "proxy.port" );
		String proxyPort = properties.getProperty( "proxy.host" );

		ProxySelector.setProxy(proxyHost, proxyPort);
	}

	
	/**
	 * Prints out the resulting configuration properties to the console. Skips 'hidden' field values
	 *  (DISABLED: user password and properties having empty)
	 */
	public void printProperties() {
		Console.printNewLine();
		Console.printText("Properties:");
		
		for (Object key : properties.keySet()) {
			//TODO HIDDEN_KEYS doesn't have any effect  =o(
			if ( HIDDEN_KEYS.contains( key ) ) // don't print out keys which should remain hidden (e.g. passwords)
				continue;
			//if (IOT_PASSWORD.equals(key) || properties.get(key).toString().trim().isEmpty()) {
			//	continue;
			//}
			Console.printProperty(key, properties.get(key));
		}
		
		Console.printNewLine();
	}

	
	public void setProperty( String key, String value ) {
		properties.put( key, value );
	}
	

	public String getStringProperty( String key ) {
		return properties.getProperty( key );
	}

	public void writeProperties() {
		String propertiesFilePath = FileUtilX.getRuntimePath().concat( propertiesFilename );
		Console.printNewLine();
		Console.printText( String.format("Persisting Properties file: %1$s", propertiesFilePath ) );

		try(OutputStream propertiesFileWriter = new FileOutputStream( propertiesFilePath )){
			properties.store( propertiesFileWriter, "save to properties file" );
		}catch( IOException ioe ){
			ioe.printStackTrace();
		}

	}
	
}