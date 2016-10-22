package com.cyumus.thingworx.erp;
import java.util.Collection;

import com.cyumus.thingworx.erp.things.SensorThing;
import com.thingworx.communications.client.ClientConfigurator;
import com.thingworx.communications.client.ConnectedThingClient;
import com.thingworx.communications.client.things.VirtualThing;
import com.thingworx.communications.common.SecurityClaims;

public class RaspberryPiClient extends ConnectedThingClient {
	public RaspberryPiClient(ClientConfigurator config) throws Exception {
		super(config);
	}
	
	public static void main(String[] args) throws Exception {	
		ClientConfigurator config = new ClientConfigurator();
		config.setUri("ws://localhost:80/Thingworx/WS");
		config.setReconnectInterval(15);
		
		SecurityClaims claims = SecurityClaims.fromCredentials("user", "pass");
		config.setSecurityClaims(claims);
		
		config.setName("RaspberryPiGateway");
		config.setAsSDKType();
		
		config.ignoreSSLErrors(true);

		int scanRate = 1000;
		
		RaspberryPiClient client = new RaspberryPiClient(config);
		
		SensorThing sensor = new SensorThing("Sensor", "A sensor", "SensorThing", client);
		client.bindThing(sensor);
		
		try {
			client.start();
		}
		catch(Exception eStart) {
			System.out.println("Initial Start Failed : " + eStart.getMessage());
		}
		
		while(!client.isShutdown()) {
			if(client.isConnected()) {
				sensor.setEmpty(Math.random()>=0.5);
				sensor.processScanRequest();
			}
			Thread.sleep(scanRate);
		}
	}
}
