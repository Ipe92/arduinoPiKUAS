import serial
from xbee import XBee
from xbee import ZigBee
import time

'''
Tee tästä moduuli. Nyt odottaa vastausta arduinolta, joka sisältää sensoridatan
'''

TRANSFER_OK = b'\x00'

def print_data(data):
    print(data)


serial_port = serial.Serial('COM5', 9600)
xbee = ZigBee(serial_port, callback=print_data)

#xbee = ZigBee(serial_port)
dest_long = b'\x00\x13\xa2\x00A\x04\x08V'

#xbee.tx(dest_addr_long = dest_long, dest_addr = b'\xFF\xFE', data=b"Haudi haudi")


while True:
    try:
        resp = xbee.wait_read_frame()
        #xbee.tx(dest_addr_long = dest_long, dest_addr = b'\xFF\xFE', data=b"sensorvalue")
        print(resp)
        #print(resp['rf_data'])
    except KeyboardInterrupt:
        print("Homma ei toimi")
        break

    #time.sleep(0.1)
