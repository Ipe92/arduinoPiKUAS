package com.cyumus.thingworx.erp.things;

import java.util.HashMap;

import com.thingworx.communications.client.ConnectedThingClient;
import com.thingworx.communications.client.things.VirtualThing;
import com.thingworx.metadata.FieldDefinition;
import com.thingworx.metadata.annotations.ThingworxPropertyDefinition;
import com.thingworx.metadata.annotations.ThingworxPropertyDefinitions;
import com.thingworx.metadata.annotations.ThingworxServiceDefinition;
import com.thingworx.metadata.annotations.ThingworxServiceResult;
import com.thingworx.metadata.collections.FieldDefinitionCollection;
import com.thingworx.types.BaseTypes;
import com.thingworx.types.constants.CommonPropertyNames;

@SuppressWarnings("serial")

/**
 * These annotations defines all the Thing virtual representation properties.
 */
@ThingworxPropertyDefinitions(properties = {	
	@ThingworxPropertyDefinition(name="LocationCode", description="The identification string of the location", baseType="STRING", category="Status", aspects={"isReadOnly:false"}),
	@ThingworxPropertyDefinition(name="BinCode", description="The identification string of the bin", baseType="STRING", category="Status", aspects={"isReadOnly:false"}),
	
})

/**
 * This class simulates a bin. It's, in fact, a virtual representation of a bin.
 */
public class BinThing extends VirtualThing implements Runnable {
	private String LocationCode,BinCode;
	private LocationThing location;
	private HashMap<String,ItemThing> items;
	private Thread _shutdownThread = null;
	
	/**
	 * The constructor of the Bin. It creates the sensor with the name, description, id, and the client that uses it.
	 * Then, it creates all the fault events that can occur in the future, if it fails.
	 * It initialize the annotations and creates the default properties, events and services.
	 * At the end, it initialize itself. 
	 * @param name The name of the item.
	 * @param description A simple description.
	 * @param identifier A string of characters to identify it on Thingworx
	 * @param client Who uses this Sensor.
	 */
	public BinThing(String name, String description, String identifier, ConnectedThingClient client) {
		super(name,description,identifier,client);
		this.BinCode = identifier;
		this.items = new HashMap<String,ItemThing>();

		super.initializeFromAnnotations();
		this.init();
	}

	/**
	 * This function synchronize everything with Thingworx server.
	 */
	public void synchronizeState() {
		super.synchronizeState();
		super.syncProperties();
	}
	
	/**
	 * This function initializes the custom properties, events and services.
	 * Then, it defines a new Data Shape, called ItemAmount, that can be returned to
	 * Thingworx if called from there.
	 */
	private void init() 
	{
		initializeFromAnnotations();
		
        FieldDefinitionCollection fields = new FieldDefinitionCollection();
        fields.addFieldDefinition(new FieldDefinition("LocationCode", BaseTypes.STRING));
        fields.addFieldDefinition(new FieldDefinition("BinCode", BaseTypes.STRING));
        defineDataShapeDefinition("BinShape", fields);
	}
	
	/**
	 * This function can be used by the client to set the values that it scans.
	 * @param iE
	 */
	public void setLocation(LocationThing loc){
		if (loc==null) this.LocationCode = "No Location";
		else this.LocationCode=loc.getIdentifier();
		this.location = loc;
		for (ItemThing item:this.items.values()){
			item.setLocation(loc);
		}
	}
	public void addItem(ItemThing item){
		this.items.put(item.getName(), item);
		item.setBin(this);
	}
	public void removeItem(ItemThing item){
		this.items.remove(item.getName());
		item.setBin(null);
	}
	public boolean hasItem(ItemThing item){return this.items.containsKey(item.getName());}
	
	/**
	 * This function is used in a loop with a delay.
	 * It updates all the values in the Thingworx server.
	 */
	@Override
	public void processScanRequest() throws Exception {
		super.processScanRequest();
		this.update();
	}
	
	/**
	 * This function updates all the values in Thigworx.
	 * @throws Exception
	 */
	public void update() throws Exception {
		super.setProperty("LocationCode", this.LocationCode);
		super.setProperty("BinCode", this.BinCode);
		super.updateSubscribedProperties(15000);
		super.updateSubscribedEvents(60000);
	}

	
	/**
	 * You can use this service to remotely shutdown the device.
	 * @throws Exception
	 */
	@ThingworxServiceDefinition( name="Shutdown", description="Shutdown the client")
	@ThingworxServiceResult( name=CommonPropertyNames.PROP_RESULT, description="", baseType="NOTHING")
	/**
	 * This function is linked with the Shutdown service.
	 * It starts the shutdown Thread, and runs it.
	 * @throws Exception
	 */
	public synchronized void Shutdown() throws Exception {
		if(this._shutdownThread == null) {
			this._shutdownThread = new Thread(this);
			this._shutdownThread.start();
		}
	}

	/**
	 * When you run the shutdown Thread,
	 * it sleeps 1 second, it gets the client of itself
	 * and shuts down it.
	 */
	@Override
	public void run() {
		try {
			Thread.sleep(1000);
			this.getClient().shutdown();
		} catch (Exception x) {}
	}
}
