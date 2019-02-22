package commons.init.properties;

import java.util.ArrayList;

import commons.utils.Console;

/**
 * A >>>Device<<<-specific properties implementation of the 'AbstractPropertiesHelperX' class
 */
public class ClusterPropertiesX extends AbstractPropertiesHelperX {

	protected static final String propertiesFileType = "cluster"; //i.e. "cluster.properties"

	protected static final String IOT_HOST = "iot.hostname";
	protected static final String INSTANCE_ID = "instance.id";
	protected static final String TENANT_ID = "tenant.id";
	protected static final String IOT_USER = "iot.user";
	protected static final String IOT_PASSWORD = "iot.password";
	protected static final String GATEWAY_PROTOCOL_ID = "gateway.protocol.id";
	protected static final String PROXY_PORT = "proxy.port";
	protected static final String PROXY_HOST = "proxy.host";

	protected static final String DEVICE_COUNT = "device.count";
	

	public ClusterPropertiesX() {
		super( propertiesFileType );
		HIDDEN_KEYS.add( "IOT_PASSWORD" );
		
		KEYS = new ArrayList<String[]>();
		KEYS.add(new String[] {"IOT_HOST",			"iot.host",		  "MANDATORY",	"Hostname (e.g. 'trial.eu10.cp.iot.sap'): "	});
		KEYS.add(new String[] {"INSTANCE_ID",		"instance.id",	  "OPTIONAL",	"Instance ID (e.g. 'demo'): "				});
		KEYS.add(new String[] {"TENANT_ID",			"tenant.id",	  "OPTIONAL",	"Tenant ID (e.g. '0123456789'): "			});
		KEYS.add(new String[] {"IOT_USER",			"iot.user",		  "MANDATORY",	"Username for IoT Core (e.g. 'root'): "		});
		KEYS.add(new String[] {"IOT_PASSWORD",		"iot.password",	  "MANDATORY",	"Password for IoT Core user: "				});
		KEYS.add(new String[] {"GATEWAY_PROTOCOL_ID",	"gateway.protocol.id",	"MANDATORY",	"Gateway Protocol ('rest' or 'mqtt'): "	});
		KEYS.add(new String[] {"PROXY_PORT",		"proxy.port",	  "OPTIONAL",	"Proxy Port (e.g. '8080' or leave empty): "		});
		KEYS.add(new String[] {"PROXY_HOST",		"proxy.host",	  "OPTIONAL",	"Proxy Host (e.g. 'proxy' or leave empty): "	});

		//promptProperties();	//will fill in any missing mandatory values
		printProperties();	
}

	public String getIotHost() {
		return properties.getProperty( IOT_HOST );
	}

	public String getInstanceId() {
		return properties.getProperty( INSTANCE_ID );
	}

	public String getTenantId() {
		return properties.getProperty( TENANT_ID );
	}

	public String getIotUser() {
		return properties.getProperty( IOT_USER );
	}

	public String getIotPassword() {
		return properties.getProperty( IOT_PASSWORD );
	}

	public String getGatewayProtocolId() {
		return properties.getProperty( GATEWAY_PROTOCOL_ID );
	}

	public String getProxyHost() {
		return properties.getProperty( PROXY_HOST );
	}

	public String getProxyPort() {
		return properties.getProperty( PROXY_PORT );
	}

	public int getDeviceCount() {
		//Number of Devices/Sensors to create
		String s_DeviceCount = properties.getProperty( DEVICE_COUNT );
		int i_DeviceCount = -1;  //initialize with a rogue value
		try {
			i_DeviceCount = Integer.parseInt( s_DeviceCount );
			if ( (i_DeviceCount < 1) || (i_DeviceCount > 10)) {  //put sensible limits on the number of devices
				i_DeviceCount = 1;
			}

		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Console.printWarning( "Setting 'DeviceCount' to 1.");
			i_DeviceCount = 1;
		}

		return i_DeviceCount;
	}


}