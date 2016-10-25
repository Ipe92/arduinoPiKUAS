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

@SuppressWarnings("serial")

/**
 * These annotations defines all the Thing virtual representation properties.
 */
@ThingworxPropertyDefinitions(properties = {	
		@ThingworxPropertyDefinition(name="isEmpty", description="Is Empty?", baseType="BOOLEAN", category="Status", aspects={"isReadOnly:false"}),
	})

/**
 * These annotations defines all the Thing virtual representation events.
 */
@ThingworxEventDefinitions(events = {
	@ThingworxEventDefinition(name="SensorFault", description="Sensor fault", dataShape="Sensor.Fault", category="Faults", isInvocable=true, isPropertyEvent=false)
})

/**
 * This class simulates a Sensor. It's, in fact, a virtual representation of a Sensor.
 */
public class SensorThing extends VirtualThing implements Runnable {
	private boolean isEmpty;
	private Thread _shutdownThread = null;
	
	/**
	 * The constructor of the Sensor. It creates the sensor with the name, description, id, and the client that uses it.
	 * Then, it creates all the fault events that can occur in the future, if it fails.
	 * It initialize the annotations and creates the default properties, events and services.
	 * At the end, it initialize itself. 
	 * @param name The name of the sensor.
	 * @param description A simple description.
	 * @param identifier A string of characters to identify it on Thingworx
	 * @param client Who uses this Sensor.
	 */
	public SensorThing(String name, String description, String identifier, ConnectedThingClient client) {
		super(name,description,identifier,client);
		
		FieldDefinitionCollection faultFields = new FieldDefinitionCollection();
		faultFields.addFieldDefinition(new FieldDefinition(CommonPropertyNames.PROP_MESSAGE,BaseTypes.STRING));
		defineDataShapeDefinition("Sensor.Fault", faultFields);

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
	 * Then, it defines a new Data Shape, called SensorReadings, that can be returned to
	 * Thingworx if called from there.
	 */
	private void init() 
	{
		initializeFromAnnotations();
		
        FieldDefinitionCollection fields = new FieldDefinitionCollection();
        fields.addFieldDefinition(new FieldDefinition("isEmpty", BaseTypes.BOOLEAN));
        defineDataShapeDefinition("SensorReadings", fields);
	}
	
	/**
	 * This kind of annotation defines the services. In this case, we are defining the GetSensorReadings
	 * service, and we link it to the function GetSensorReadings().
	 * Then, you will be able to call this Remote Service from Thingworx.
	 * When you call this Remote Service, Thingworx will send an order to this Thread to run the
	 * function linked to the Remote Service that it called, in this case, the Thread will run this function.
	 * @return
	 */
	@ThingworxServiceDefinition( name="GetSensorReadings", description="Get Sensor Readings")
	
	/**
	 * We define the return format. As we said before, we defined a DataShape called 'SensorReadings'.
	 * Then, we use it as the return dataShape type
	 */
	@ThingworxServiceResult( name=CommonPropertyNames.PROP_RESULT, description="Result", baseType="INFOTABLE", aspects={"dataShape:SensorReadings"} )
	
	/**
	 * This function is linked to the GetSensorReadings Remote Service.
	 * It creates a SensorReadings InfoTable to send back to Thingworx.
	 * It fills the InfoTable with the name of the Sensor and the value that
	 * it scanned at that time.
	 * @return The SensorReadings InfoTable filled with the name of the Sensor and its readings.
	 */
	public InfoTable GetSensorReadings(){		
		InfoTable table = new InfoTable(getDataShapeDefinition("SensorReadings"));
		ValueCollection entry = new ValueCollection();
		try{			
			entry.clear();
			entry.SetStringValue("Name", this.getName());
			entry.SetBooleanValue("isEmpty", this.isEmpty);
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
	public void setEmpty(boolean iE){
		this.isEmpty = iE;
	}
	
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
		super.setProperty("isEmpty", this.isEmpty);
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
