# jIotCore_StockMarket
StockMarket Ticker for IoT (*with* CSV integration)

I finally caved and grabbed his entire source code and copied it into a *Java* Eclipse Project.  Not using Maven (currently).
(I caved because "overlaying" my classes on top of his 'commons.jar' was killing me!)

The classes of his I've touched, I've added a 'X' suffix. e.g. AuthenticationX
If I play my cards right, it should allow his original code to work and even evolve.  I just have to carefull mark what I've changed shoiuld I need to re-implement my changes onto any new releases of his.

## RUNNING
-------
### Cluster
It's capable of taking building a cluster of DeviceTwins.  It will look for "device_xx.properties" and corresponding 'device_xx.pkcs12" files to initialize DeviceTwins (they are "uber" Devices - they contain all the Device's fields but also all the other attributes (e.g. keystores) that a Device needs in order to spin up)
I wrote them as multi-threaded..but I really don't think they need to be  (I can start their .run() methods but then so what?...what should they do?!...aprt from just "listen"!!!).  I think it's more important that the class instance simply exists - then I can call a method to feed the CSV values in.

### CSV (and 'abstraction')
It also reads a StockTicker CSV file.  (That part is multi-threaded, and again I'm not sure it needs to be because it means I have to implement logic in order for it to wait until the CSV file has been read and the CSVProducer thread has 'Terminated').
OpenCSV 4.x provides really cool bean implementations - but I don't think I need it.  In my case my 'StockTickerArtifactFactory' performs the mapping.  In details:
1. The CSVProducer class pushes the String[] blindly over to the DeviceClient's 'receiveCsvRecord()' method
2. who in turn, blindly hands the String[] record over to my "very-generic" 'ArtifactFactory' class 
3. who in turn passes it over to my Device-specific 'StockTickerArtifactFactory' who performs the mapping.  
That abstraction (keeping the 'ArtifactFactory' as uber-generic) just...feels right.

### Round Robin
I current use _TWO_ DeviceTwins.  I've implemented a crazyily simply-but-inefficient RoundRobin algorithm.  But it's simplicity is it's beauty - and it should be able to easily handle up to tens of Devices.
  

INITIALIZATION
--------------
I *totally broke* the artifact creation routines.  But I wanted to prove that the CSV records could make it all the way through to the cloud...which I've done.  =oD

//TODO:   Anyhow I need to ensure that the Devices and Sensors get created with their suffixes.


TODO
----
* It would be good to push some of the Console.printText() messages over to logging (since a whole bunch of stuff is now working)
* I could strip out the multi-threading (of DeviceTwin and CSVProducer)...perhaps.  (not sure I want to do that.)
* I'm not sure I even need the 'StockTick' class anymore.  When I look at it it seems to only provide 'casting/parsing' methods anyway.


Eclipse
-------
I'm using SLF4J along with the 'Simple' implementation
That means it's super-easy to use, but 'debug' levels don't appear by default

However, configuring Eclipse's "Run COnfigurations" to add the following property to the "VM Arguments" pane enables this:

```
	-Dorg.slf4j.simpleLogger.defaultLogLevel=debug
```


 


## Properties files

### cluster.properties

Stored in the root of the Project
e.g. C:\Users\i817399\eclipse-workspace\jStockTickerIoT\cluster.properties

```
iot.hostname = aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee.canary.cp.iot.sap
iot.certbundle = ./canary_cp_iot_sap_BUNDLE.crt
iot.user = xxxx
iot.password = xxxxxxxxxxxx
gateway.protocol.id = mqtt
instance.id = 
tenant.id = 
proxy.port = 
proxy.host = 
device.count = x
```

### device.properties

Stored in the root of the Project
e.g. C:\Users\i817399\eclipse-workspace\jStockTickerIoT\device.properties

Don't bother creating these by hand - my code will generate them


## PKCS#12 Keystores
Stored as C:\Users\i817399\eclipse-workspace\jStockTickerIoT\*.pkcs12

Don't bother creating these by hand - my code will generate them

