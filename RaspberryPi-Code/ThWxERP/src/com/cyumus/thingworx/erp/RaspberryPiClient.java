package com.cyumus.thingworx.erp;

import java.util.Random;

import com.cyumus.thingworx.erp.things.BinThing;
import com.cyumus.thingworx.erp.things.ItemThing;
import com.cyumus.thingworx.erp.things.LocationThing;
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
		ItemThing item = new ItemThing("Item A", "An item", "ItemThing", client);
		BinThing bin = new BinThing("Bin A", "A bin", "BinThing", client);
		bin.addItem(item);
		LocationThing location = new LocationThing("Location A", "A location", "LocationThing", client);
		location.addBin(bin);
		
		// We bind the client to the sensor.
		// This is the same as if you give the sensor to someone in order to use it.
		client.bindThing(item);
		client.bindThing(bin);
		client.bindThing(location);
		
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
				client.scan(item);
				// And it updates its values to Thingworx
				item.processScanRequest();
			}
			Thread.sleep(delay);
		}
	}
	/**
	 * This function makes Raspberry Pi to communicate with Arduino board and obtain all
	 * values of the item.
	 * @param item The item binded with the Arduino board
	 */
	private void scan(ItemThing item){
		item.setAmount(new Random().nextInt());
	}
}
