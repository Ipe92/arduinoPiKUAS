package com.cyumus.thingworx.erp.things;

import com.thingworx.communications.client.ConnectedThingClient;
import com.thingworx.communications.client.things.VirtualThing;
import com.thingworx.metadata.FieldDefinition;
import com.thingworx.metadata.annotations.ThingworxEventDefinition;
import com.thingworx.metadata.annotations.ThingworxEventDefinitions;
import com.thingworx.metadata.annotations.ThingworxPropertyDefinition;
import com.thingworx.metadata.annotations.ThingworxPropertyDefinitions;
import com.thingworx.metadata.annotations.ThingworxServiceDefinition;
import com.thingworx.metadata.annotations.ThingworxServiceResult;
import com.thingworx.metadata.collections.FieldDefinitionCollection;
import com.thingworx.types.BaseTypes;
import com.thingworx.types.InfoTable;
import com.thingworx.types.collections.ValueCollection;
import com.thingworx.types.constants.CommonPropertyNames;
import com.thingworx.types.primitives.IntegerPrimitive;
import com.thingworx.types.primitives.StringPrimitive;

@SuppressWarnings("serial")

/**
 * These annotations defines all the Thing virtual representation properties.
 */
@ThingworxPropertyDefinitions(properties = {	
	@ThingworxPropertyDefinition(name="ItemID", description="The identification string of the item", baseType="STRING", category="Status", aspects={"isReadOnly:false"}),
	@ThingworxPropertyDefinition(name="LocationCode", description="The identification string of the item", baseType="STRING", category="Status", aspects={"isReadOnly:false"}),
	@ThingworxPropertyDefinition(name="BinCode", description="The bin where the item is.", baseType="STRING", category="Status", aspects={"isReadOnly:false"}),
	@ThingworxPropertyDefinition(name="ItemAmount", description="The amount of this item in the location", baseType="INTEGER", category="Status", aspects={"isReadOnly:false"}),
})

/**
 * These annotations defines all the Thing virtual representation events.
 */
@ThingworxEventDefinitions(events = {
	@ThingworxEventDefinition(name="SensorFault", description="Called when the sensor fails", dataShape="Item.Fault", category="Faults", isInvocable=true, isPropertyEvent=false),
	@ThingworxEventDefinition(name="ThereIsNoItem", description="Called when there is no item in the bin.", dataShape="Item.NoItemFault", category="Faults", isInvocable=true, isPropertyEvent=true),
})

/**
 * This class simulates a collection of items. It's, in fact, a virtual representation of an item collection.
 */
public class ItemThing extends VirtualThing implements Runnable {
	private String ItemID,LocationCode,BinCode;
	private BinThing bin;
	private LocationThing location;
	private int ItemAmount;
	private Thread _shutdownThread = null;
	
	/**
	 * The constructor of the Item. It creates the sensor with the name, description, id, and the client that uses it.
	 * Then, it creates all the fault events that can occur in the future, if it fails.
	 * It initialize the annotations and creates the default properties, events and services.
	 * At the end, it initialize itself. 
	 * @param name The name of the item.
	 * @param description A simple description.
	 * @param identifier A string of characters to identify it on Thingworx
	 * @param client Who uses this Sensor.
	 */
	public ItemThing(String name, String description, String identifier, ConnectedThingClient client) {
		super(name,description,identifier,client);
		this.ItemID = identifier;
		
		FieldDefinitionCollection faultFields = new FieldDefinitionCollection();
		faultFields.addFieldDefinition(new FieldDefinition(CommonPropertyNames.PROP_MESSAGE,BaseTypes.STRING));
		defineDataShapeDefinition("Item.Fault", faultFields);
		
		faultFields = new FieldDefinitionCollection();
		faultFields.addFieldDefinition(new FieldDefinition(CommonPropertyNames.PROP_MESSAGE,BaseTypes.STRING));
		defineDataShapeDefinition("Item.NoItemFault", faultFields);

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
        fields.addFieldDefinition(new FieldDefinition("ItemID", BaseTypes.STRING));
        fields.addFieldDefinition(new FieldDefinition("LocationCode", BaseTypes.STRING));
        fields.addFieldDefinition(new FieldDefinition("BinCode", BaseTypes.STRING));
        fields.addFieldDefinition(new FieldDefinition("ItemAmount", BaseTypes.INTEGER));
        defineDataShapeDefinition("ItemAmount", fields);
	}
	
	/**
	 * This kind of annotation defines the services. In this case, we are defining the GetSensorReadings
	 * service, and we link it to the function GetSensorReadings().
	 * Then, you will be able to call this Remote Service from Thingworx.
	 * When you call this Remote Service, Thingworx will send an order to this Thread to run the
	 * function linked to the Remote Service that it called, in this case, the Thread will run this function.
	 * @return
	 */
@ThingworxServiceDefinition( name="GetItemAmount", description="Gets the amount of this item in the given location/bin.")
	
	/**
	 * We define the return format. As we said before, we defined a DataShape called 'ItemAmount'.
	 * Then, we use it as the return dataShape type
	 */
	@ThingworxServiceResult( name=CommonPropertyNames.PROP_RESULT, description="Result", baseType="INFOTABLE", aspects={"dataShape:ItemAmount"} )
	
	/**
	 * This function is linked to the GetItemAmount Remote Service.
	 * It creates an ItemAmount InfoTable to send back to Thingworx.
	 * It fills the InfoTable with the itemID, locationCode, binCode and its amount
	 * scanned at that time.
	 * @return The ItemAmount InfoTable filled with all the data.
	 */
	public InfoTable GetItemAmount(){		
		InfoTable table = new InfoTable(getDataShapeDefinition("ItemAmount"));
		ValueCollection entry = new ValueCollection();
		try{			
			entry.clear();
			entry.SetStringValue("ItemID", new StringPrimitive(this.ItemID));
			entry.SetStringValue("LocationCode", new StringPrimitive(this.LocationCode));
			entry.SetStringValue("BinCode", new StringPrimitive(this.BinCode));
			entry.SetIntegerValue("ItemAmount", new IntegerPrimitive(this.ItemAmount));
			table.addRow(entry.clone());
		} 
		catch (Exception e){
			e.printStackTrace();
		}
		return table;
	}
	
	/**
	 * This function can be used by the client to set the values that it scans.
	 * @param iE
	 */
	public void setLocation(LocationThing loc){
		if (loc==null) this.LocationCode = "No Location";
		else this.LocationCode=loc.getIdentifier();
		this.location=loc;}
	public void setBin(BinThing bin){
		if (bin==null) this.BinCode = "No Bin";
		else this.BinCode=bin.getIdentifier();
		this.bin=bin;}
	public void setAmount(int a){this.ItemAmount=a;}
	
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
		super.setProperty("ItemID", this.ItemID);
		super.setProperty("LocationCode", this.LocationCode);
		super.setProperty("BinCode", this.BinCode);
		super.setProperty("ItemAmount", this.ItemAmount);
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
