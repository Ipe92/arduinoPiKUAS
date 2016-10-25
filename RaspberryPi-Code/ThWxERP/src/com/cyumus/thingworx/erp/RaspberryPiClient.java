package com.cyumus.thingworx.erp;

import com.cyumus.thingworx.erp.things.SensorThing;
import com.thingworx.communications.client.ClientConfigurator;
import com.thingworx.communications.client.ConnectedThingClient;
import com.thingworx.communications.common.SecurityClaims;

public class RaspberryPiClient extends ConnectedThingClient {
	public RaspberryPiClient(ClientConfigurator config) throws Exception {
		super(config);
	}
	
	public static void main(String[] args) throws Exception {
		// We create the configuration stuff and blablabla~
		ClientConfigurator config = new ClientConfigurator();
		// Websocket~
		config.setUri("ws://localhost:80/Thingworx/WS");
		// Reconnect every 15 seconds...
		config.setReconnectInterval(15);
		
		// Using of credentials...
		SecurityClaims claims = SecurityClaims.fromCredentials("Arduino", "1234");
		config.setSecurityClaims(claims);
		
		// The name of the Gateway
		config.setName("RaspberryPiGateway");
		// It's a SDK Type
		config.setAsSDKType();
		
		// We ignore all these bothering SSL errors. 
		config.ignoreSSLErrors(true);
		
		// The delay of the Thing.
		int delay = 1000;
		
		// We create  client that will use the Sensor.
		// In this case, this is a virtual representation of the Raspberry Pi.
		RaspberryPiClient client = new RaspberryPiClient(config);
		
		// We create a sensor, with its name, description, the id and the client that will use it.
		SensorThing sensor = new SensorThing("Sensor", "A sensor", "SensorThing", client);
		// We bind the client to the sensor.
		// This is the same as if you give the sensor to someone in order to use it.
		client.bindThing(sensor);
		
		try {
			// We say to the client to start working.
			client.start();
		}
		catch(Exception eStart) {
			System.out.println("Initial Start Failed : " + eStart.getMessage());
		}
		
		// While the client is working
		while(!client.isShutdown()) {
			// If the client is connected to Thingworx
			if(client.isConnected()) {
				// It makes the sensor to scan
				client.scan(sensor);
				// And it updates its values to Thingworx
				sensor.processScanRequest();
			}
			Thread.sleep(delay);
		}
	}
	/**
	 * This function makes Raspberry Pi to communicate with Arduino board and obtain all
	 * values of the sensor.
	 * @param sensor The sensor
	 */
	private void scan(SensorThing sensor){
		sensor.setEmpty(Math.random()>=0.5);
	}
}
