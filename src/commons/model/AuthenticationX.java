package commons.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import commons.utils.Console;
import commons.utils.FileUtilX;


public class AuthenticationX {

	//Opened scope from 'private' to 'protected'
	protected String secret;
	protected String pem;
	protected String password;

	public String getSecret() {
		return secret;
	}

	public String getPem() {
		return pem;
	}

	public String getPassword() {
		return password;
	}

	//My extension starts here...
	
	private static final String CIPHER_ALGORITHM = "PBEWithSHA1AndDESede";
	private static final String DIRECTORY_NAME = "keystores";
	//private static final String SSL_KEYSTORE_SECRET = "hkRPusjglo";  each keystore for now stores its own secret in the device.properties
	//TODO encrypt the string in the .properties file with this key!
	private static final String deviceP12KeystoreFilenameTemplate = "device%1$s.pkcs12";  //e.g. "device_01.pkcs12"

	private String p12;	//Base64-encoded string of the downloaded PKCS#12 Keystore
	private KeyStore ks;
	private KeyManager[] keyManagers;	//Basically an "unlocked" Keystore
	private TrustManager[] trustManagers;
	private SSLSocketFactory sslSocketFactory;	//not sure I need it here (I also hang it under the DeviceClient instance)
	private String suffix;

	public AuthenticationX( String suffix, String keystoreSecret ) {
		// No real constructor, actually
		super();
		this.suffix = suffix;
		this.secret = keystoreSecret;
		
	}

	public String getP12() {
		return p12;
	}

	public SSLSocketFactory getSSLSocketFactory() {
		return this.sslSocketFactory;
	}

	// init #1
	// (Assumes 'suffix' and 'secret' fields have been set)
	public SSLSocketFactory buildSSLSocketFactory()
	throws GeneralSecurityException, IOException {

		keyManagers = getKeyManagers();			// init #1.1
		trustManagers = getTrustManagers();		// init #1.2

		SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
		sslContext.init(keyManagers, trustManagers, new java.security.SecureRandom());
		this.sslSocketFactory = sslContext.getSocketFactory();
		
		return this.sslSocketFactory;
	}
	
	
	// init #1.1
	// (Assumes 'suffix' and 'secret' fields have been set)
	protected KeyManager[] getKeyManagers()    //Device device, String pem, String encryptedPrivateKey, String secret)
			throws GeneralSecurityException, IOException {

		KeyStore keyStore = openPkcs12Keystore();	// init #1.1.1
		
		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init( keyStore, this.secret.toCharArray());
	
		return keyManagerFactory.getKeyManagers();
	}

	
	// init #1.1.1
	// (Assumes 'suffix' and 'secret' fields have been set)
	public KeyStore openPkcs12Keystore() throws IOException, FileNotFoundException {
		KeyStore keystore = null;
		try {
			String deviceP12KeystoreFilename = String.format( deviceP12KeystoreFilenameTemplate, this.suffix );
            
    		String keystorePath = FileUtilX.getRuntimePath().concat( deviceP12KeystoreFilename );
    		Console.printText( String.format( "Looking for Keystore file here: %1$s", keystorePath ) );

			File deviceP12KeystoreFile = new File( keystorePath );
			if ( deviceP12KeystoreFile.exists() ) {
				keystore = KeyStore.getInstance( "PKCS12" );
	            char[] keystoreSecret_char = this.secret.toCharArray();

	            InputStream readStream = new FileInputStream( keystorePath );

	            keystore.load(
	            	readStream,  //Assumes on classpath:  this.getClass().getClassLoader().getResourceAsStream("the.p12"),
	            	keystoreSecret_char );

	            //Test it fully by opening the key from the keystore
	            //Downloaded PKCS#12 files from IoT Core appear to store their Private Key with an alias of "1"
	            PrivateKey key = (PrivateKey) keystore.getKey("1", keystoreSecret_char );
	            if ( key != null ) {
	            	Console.printText( "Private Key successfully retrieved from keystore: " + deviceP12KeystoreFile);
	            }
			}

        } catch (Exception e) {
            Console.printError( "Exception while trying to obtain private key. \tFurther details: " + e );
            return null;
        }
		return keystore; 
	}

	
	
	
	// Almost a straight copy from SecurityUtil 
	// ...except a) relaxed scope from 'private' to 'protected' and b) no longer a 'static' method
/*
	protected PrivateKey decryptPrivateKey(String encryptedPrivateKey, String secret)
	throws GeneralSecurityException, IOException {

		byte[] encodedPrivateKey = Base64.getMimeDecoder()
			.decode(encryptedPrivateKey.getBytes(Constants.DEFAULT_ENCODING));

		EncryptedPrivateKeyInfo encryptPKInfo = new EncryptedPrivateKeyInfo(encodedPrivateKey);
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
		PBEKeySpec pbeKeySpec = new PBEKeySpec(secret.toCharArray());
		SecretKeyFactory secretFactory = SecretKeyFactory.getInstance(CIPHER_ALGORITHM);
		Key pbeKey = secretFactory.generateSecret(pbeKeySpec);
		AlgorithmParameters algorithmParameters = encryptPKInfo.getAlgParameters();
		cipher.init(Cipher.DECRYPT_MODE, pbeKey, algorithmParameters);
		KeySpec pkcsKeySpec = encryptPKInfo.getKeySpec(cipher);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");

		return keyFactory.generatePrivate(pkcsKeySpec);
	}
*/

	
	// init #1.2
	/*
	 * Do not use in production! This trust manager trusts whatever certificate is provided.
	 * 
	 * When connecting through wss with a broker which uses a self-signed certificate or a
	 * certificate that is not trusted by default, there are two options.
	 * 
	 * 1. Disable host verification. This should only be used for testing. It is not recommended in
	 * productive environments.
	 * 
	 * options.setSocketFactory(getTrustManagers()); // will trust all certificates
	 * 
	 * 2. Add the certificate to your keystore. The default keystore is located in the JRE in <jre
	 * home>/lib/security/cacerts. The certificate can be added with
	 * 
	 * "keytool -import -alias my.broker.com -keystore cacerts -file my.broker.com.pem".
	 * 
	 * It is also possible to point to a custom keystore:
	 * 
	 * Properties properties = new Properties();
	 * properties.setProperty("com.ibm.ssl.trustStore","my.cacerts");
	 * options.setSSLProperties(properties);
	 */

	// Almost a straight copy from SecurityUtil 
	// ...except a) relaxed scope from 'private' to 'protected' and b) no longer a 'static' method
	protected TrustManager[] getTrustManagers() {
		return new TrustManager[] { new X509TrustManager() {

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
			throws java.security.cert.CertificateException {
				// empty implementation
			}

			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
			throws java.security.cert.CertificateException {
				// empty implementation
			}

		} };
	}


	

		
	/**
	 * Download and persist the Device's PKCS#12 Truststore to disk
	 * @param coreServiceM
	 * @param device
	 * @param suffix
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	/*TODO DISABLED (UNTIL I FIX THE BUILD-ARTIFACTS AGAIN)
	public static String downloadPkcs12Keystore(CoreServiceX coreServiceM, Device device, String suffix )
			throws IOException, FileNotFoundException {
		//Basic cleanup
		String deviceP12KeystoreFilename = String.format( "device%1$s.pkcs12", suffix );
		File deviceP12KeystoreFile = new File( deviceP12KeystoreFilename );
        if ( deviceP12KeystoreFile.exists() ) {
        	deviceP12KeystoreFile.delete();
        }
        
        //Download PKCS#12 via API
		AuthenticationX authP12 = coreServiceM.getAuthenticationX( device );
		
		String base64p12 = authP12.getP12();
		ByteArrayInputStream in = new ByteArrayInputStream(
				Base64.getMimeDecoder().decode( base64p12.getBytes(Constants.DEFAULT_ENCODING)));
		OutputStream out = new FileOutputStream( deviceP12KeystoreFile );

		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
		    out.write(buf, 0, len);
		}
		in.close();
		out.close();
		
		String keystoreSecret = authP12.getSecret();
		return keystoreSecret;
	}
	*/
	
}
