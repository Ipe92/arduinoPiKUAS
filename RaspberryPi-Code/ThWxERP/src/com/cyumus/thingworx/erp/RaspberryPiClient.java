package com.cyumus.thingworx.erp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.cyumus.thingworx.erp.things.BinThing;
import com.cyumus.thingworx.erp.things.ItemThing;
import com.cyumus.thingworx.erp.things.LocationThing;
import com.thingworx.communications.client.ClientConfigurator;
import com.thingworx.communications.client.ConnectedThingClient;
import com.thingworx.communications.common.SecurityClaims;

public class RaspberryPiClient extends ConnectedThingClient {
	private HashMap<String,LocationThing> locs;
	private HashMap<String,BinThing> bins;
	private HashMap<String,ItemThing> items;
	
	public RaspberryPiClient(ClientConfigurator config) throws Exception {
		super(config);
		this.locs = new HashMap<String,LocationThing>();
		this.bins = new HashMap<String,BinThing>();
		this.items = new HashMap<String,ItemThing>();
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
		
		// We create all things from the config file.
		client.getThingsFromConfig();
		
		// We bind all things
		client.bindThings();
		
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
				for (ItemThing item:client.items.values()){
					// It makes the sensor to scan
					client.scan(item);
					// And it updates its values to Thingworx
					item.processScanRequest();
				}
			}
			Thread.sleep(delay);
		}
	}
	
	/**
	 * This function gets all the configuration from a file and creates the things.
	 * @throws Exception 
	 */
	private void getThingsFromConfig() throws Exception{
		List<String> lines = Files.readAllLines(Paths.get("./things/things.txt"));
		LocationThing currLocation = null;
		BinThing currBin = null;
		ItemThing currItem = null;
		
		for (String line:lines){
			String [] str = line.split("\t");
			String [] argv;
			switch(str.length){
				case 1: // If you don't use the tab, it means that it's a Location
					argv = str[0].split("/");
					currLocation = new LocationThing(argv[0], argv[1], argv[2], this);
					this.locs.put(currLocation.getName(), currLocation);
					break;
				case 2: // If you use the tab once, it means that it's a Bin
					argv = str[1].split("/");
					currBin = new BinThing(argv[0], argv[1], argv[2], this);
					this.bins.put(currBin.getName(), currBin);
					currLocation.addBin(currBin);
					break;
				case 3: // If you use the tab twice, it means that it's an Item
					argv = str[2].split("/");
					currItem = new ItemThing(argv[0], argv[1], argv[2], this);
					this.items.put(currItem.getName(), currItem);
					currBin.addItem(currItem);
					break;
				default:
					throw new Exception("Error when parsing config file");
			}
		}
	}
	
	/**
	 * This function binds all things to the client
	 * @throws Exception 
	 */
	private void bindThings() throws Exception{
		for (LocationThing loc:this.locs.values()) this.bindThing(loc);
		for (BinThing bin:this.bins.values()) this.bindThing(bin);
		for (ItemThing item:this.items.values()) this.bindThing(item);
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
