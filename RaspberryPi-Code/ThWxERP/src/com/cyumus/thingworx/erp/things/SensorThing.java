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
@ThingworxPropertyDefinitions(properties = {	
		@ThingworxPropertyDefinition(name="isEmpty", description="Current Temperature", baseType="NUMBER", category="Status", aspects={"isReadOnly:true"}),
	})

// Event Definitions
@ThingworxEventDefinitions(events = {
	@ThingworxEventDefinition(name="SensorFault", description="Sensor fault", dataShape="Sensor.Fault", category="Faults", isInvocable=true, isPropertyEvent=false)
})

// Steam Thing virtual thing class that simulates a Steam Sensor
public class SensorThing extends VirtualThing implements Runnable {
	
	private Thread _shutdownThread = null;
	
	
	public SensorThing(String name, String description, String identifier, ConnectedThingClient client) {
		super(name,description,identifier,client);
		
		FieldDefinitionCollection faultFields = new FieldDefinitionCollection();
		faultFields.addFieldDefinition(new FieldDefinition(CommonPropertyNames.PROP_MESSAGE,BaseTypes.STRING));
		defineDataShapeDefinition("Sensor.Fault", faultFields);

		super.initializeFromAnnotations();
		this.init();
	}


	public void synchronizeState() {
		super.synchronizeState();
		super.syncProperties();
	}
	
	private void init() 
	{
		initializeFromAnnotations();
		
        FieldDefinitionCollection fields = new FieldDefinitionCollection();
        fields.addFieldDefinition(new FieldDefinition("isEmpty", BaseTypes.BOOLEAN));
        defineDataShapeDefinition("SensorReadings", fields);
	}
	
	@ThingworxServiceDefinition( name="GetSensorReadings", description="Get Sensor Readings")
	@ThingworxServiceResult( name=CommonPropertyNames.PROP_RESULT, description="Result", baseType="INFOTABLE", aspects={"dataShape:SensorReadings"} )
	public InfoTable GetSensorReadings() 
	{		
		InfoTable table = new InfoTable(getDataShapeDefinition("SensorReadings"));

		ValueCollection entry = new ValueCollection();
		
		try 
		{			
			entry.clear();
			entry.SetStringValue("isEmpty", true);
			table.addRow(entry.clone());
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		return table;
	}
	
	@Override
	public void processScanRequest() throws Exception {
		super.processScanRequest();
		this.scanDevice();
	}
	
	public void scanDevice() throws Exception {
		super.setProperty("isEmpty", Math.random() >= 0.5);
	}

	@ThingworxServiceDefinition( name="Shutdown", description="Shutdown the client")
	@ThingworxServiceResult( name=CommonPropertyNames.PROP_RESULT, description="", baseType="NOTHING")
	public synchronized void Shutdown() throws Exception {
		if(this._shutdownThread == null) {
			this._shutdownThread = new Thread(this);
			this._shutdownThread.start();
		}
	}

	@Override
	public void run() {
		try {
			Thread.sleep(1000);
			this.getClient().shutdown();
		} catch (Exception x) {}
	}
}
