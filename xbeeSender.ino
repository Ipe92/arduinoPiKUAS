#include <SimpleZigBeeRadio.h>
#include <SoftwareSerial.h>

SimpleZigBeeRadio xbee = SimpleZigBeeRadio();

SoftwareSerial xbeeSerial(2, 3); // (RX=>DOUT, TX=>DIN)
SimpleZigBeePacket zbp = SimpleZigBeePacket();

int val = 0;

unsigned long time = 0;
unsigned long last_sent = 0;

void setup() {
    // Start the serial ports ...
    Serial.begin( 9600 );
    
    xbeeSerial.begin( 9600 );
    // ... and set the serial port for the XBee radio.
    xbee.setSerial( xbeeSerial );
    // Set a non-zero frame id to receive Status and Response packets.
    xbee.setAcknowledgement(true);
    
    uint8_t exFrame[] = { 0x10,0x01,     0x00,0x00,0x00,0x00,  0x00,0x00,0xFF,0xFF,      0xff,0xff,0x00,0x00,0xff,0xff };
    
    zbp.setFrameData(0, exFrame, sizeof(exFrame));
}

void loop() {
    // If data is waiting in the XBee serial port ...
    if( xbee.available() ){
        // ... read the data.
        xbee.read();
        // If a complete message is available, display the contents
        if( xbee.isComplete() ){
            Serial.print("\nIncoming Message: ");
            printPacket( xbee.getIncomingPacketObject() );
        }
    }
    time = millis();
    if(time > (last_sent+5000))
    {
      zbp.setFrameData(zbp.getFrameLength()-2, val >> 8 & 0xff);
      zbp.setFrameData(zbp.getFrameLength()-1, val & 0xff);
      Serial.print("\nSend Message: ");
      //printPacket( zbp );
      
      xbee.send(zbp);
      val = (val+10)%500;
    }
    delay(10); // Small delay for stability
}

void printPacket(SimpleIncomingZigBeePacket & p){
    /*Serial.print( START, HEX );
    Serial.print(' ');
    Serial.print( p.getLengthMSB(), HEX );
    Serial.print(' ');
    Serial.print( p.getLengthLSB(), HEX );
    Serial.print(' ');
    // Frame Type and Frame ID are stored in Frame Data
    uint8_t checksum = 0;
    for( int i=0; i<p.getFrameLength(); i++){
        Serial.print( p.getFrameData(i), HEX );
        Serial.print(' ');
        checksum += p.getFrameData(i);
    }
    // Calculate checksum based on summation of frame bytes
    checksum = 0xff - checksum;
    Serial.print(checksum, HEX );
    Serial.println();*/
    //Serial.print(p.getRXPayloadLength());
    for(int i = 0; i < p.getRXPayloadLength(); i++)
    {
      //tee Ascii-muunnos tai pythonille koodi
       Serial.print(p.getRXPayload(i));
    }
}
