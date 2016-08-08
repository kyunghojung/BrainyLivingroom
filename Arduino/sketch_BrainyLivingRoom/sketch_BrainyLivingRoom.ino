#include <SPI.h>
#include <Wire.h>
#include "DHT.h"
#include <QueueList.h>
#include "rgb_lcd.h"
#include "Adafruit_BLE_UART.h"

// Bluetooth LE
#define ADAFRUITBLE_REQ 10
#define ADAFRUITBLE_RDY 2
#define ADAFRUITBLE_RST 9

Adafruit_BLE_UART BTLEserial = Adafruit_BLE_UART(ADAFRUITBLE_REQ, ADAFRUITBLE_RDY, ADAFRUITBLE_RST);

String rcvDataStr = "";
String sendDataStr = "";
String lastSendDataStr = "";

// DHT21(AM2301) Temp/Humi sensor
#define DHTPIN A7
#define DHTTYPE DHT21
DHT dht(DHTPIN, DHTTYPE);


// LCD
rgb_lcd lcd;

#define DIMMING_WAKE_PIN 30
boolean dimmingStatus = false;

// LED
#define LED_OFF 0
#define LED_RED 1
#define LED_GREEN 2
#define LED_BLUE 3

#define LED_RED_PIN 11
#define LED_GREEN_PIN 12
#define LED_BLUE_PIN 13

// Measurment interval
#define SEND_BT_INTERVALS 1000
#define LONG_MEASUREMENT_INTERVALS 60000
#define DIMMING_INTERVALS 30000

unsigned long sendBTTime = 0;
unsigned long longMeasurementTime = 0;
unsigned long dimmingTime = 0;

#define LUX_PIN   A0

float tempValue = 0;
float humiValue = 0;
int luxValue = 0;

QueueList <String> BTQueue;

void setup() {
  Serial.begin(9600);
  
  dht.begin();
  
  lcd.begin(16, 2);
  lcd.setRGB(255, 255, 255);

  pinMode(LED_RED_PIN,OUTPUT);
  pinMode(LED_GREEN_PIN,OUTPUT);
  pinMode(LED_BLUE_PIN,OUTPUT);
  ledON(LED_OFF);
  
  longMeasurementTime = millis();
  sendBTTime = millis();
  
  setupBT();
  
  lcd.setCursor(0, 0);
  lcd.print("Brainy Livingroom");
  delay(1500);
  
  updateValues();
  
  Serial.println("Brainy Livingroom Started");
}

void loop() 
{
  int rcvBTLen = readBT();

  if(digitalRead(DIMMING_WAKE_PIN) == HIGH) 
  {
    updateValues();
    backlightControl(true);
  }
  
  if(dimmingStatus == true && GetETime(dimmingTime) > DIMMING_INTERVALS) 
  {
    backlightControl(false);
  }

  if(BTLEserial.getState() == ACI_EVT_CONNECTED)
  {
    ledON(LED_GREEN);
  }
  else
  {
    ledON(LED_RED);
  }
  
  if(rcvBTLen > 0) 
  {
    Serial.print("rcvDataStr: ");
    Serial.println(rcvDataStr);

    processCommand(rcvDataStr);
    
    rcvDataStr = "";
  }
  
  if(GetETime(longMeasurementTime) > LONG_MEASUREMENT_INTERVALS)
  {
    updateValues();
    longMeasurementTime = millis();  
  }
  
  if(GetETime(sendBTTime) > SEND_BT_INTERVALS)
  {
    sendBT();
    sendBTTime = millis();
  }
}

void updateValues()
{
  float temp = getTempture();
  float humi = getHumidity();
  int lux = getLuxValue();

  int val_int;
  int val_fra;
      
  char tempBuff[10];
  char humiBuff[10];

  // Get Temp
  val_int = (int) temp;
  val_fra = (int) ((temp - (float)val_int) * 10);
  snprintf (tempBuff, sizeof(tempBuff), "%d.%d", val_int, val_fra); 
        
  //if(tempValue != temp)
  {
    sendDataStr = "";
    sendDataStr.concat("T:"+String(tempBuff));
    writeBT(sendDataStr);
    
    lcd.setCursor(0, 0);
    lcd.print("Temp: "+String(tempBuff)+"*C");
        
    tempValue = temp;
  }
      
  // Get Humi
  val_int = (int) humi;
  val_fra = (int) ((humi - (float)val_int) * 10);
  snprintf (humiBuff, sizeof(humiBuff), "%d.%d", val_int, val_fra); 
        
  //if(humiValue != humi)
  {
    sendDataStr = "";
    sendDataStr.concat("H:"+String(humiBuff));
    writeBT(sendDataStr);
       
    lcd.setCursor(0, 1);
    lcd.print("Humi: "+String(humiBuff)+"%");
    
    humiValue = humi;
  }

  //if(luxValue != lux)
  {
    sendDataStr = "";
    sendDataStr.concat("L:"+String(lux));
    writeBT(sendDataStr);
    luxValue = lux;
  }

}


void backlightControl(boolean onOff) 
{
  if(onOff == true)
  {
    if(dimmingStatus == true) {
      dimmingTime = millis();
    }
    else{
      lcd.setRGB(255, 255, 255);
      dimmingStatus = true;
      dimmingTime = millis();
    }
  }
  else if(onOff == false && dimmingStatus == true)
  {
    dimmingStatus = false;
      lcd.setRGB(0, 0, 0);
  }
  
}


unsigned long GetETime(unsigned long referenceTime)
{
  unsigned long returnValue;
  unsigned long currentMillis = millis();
  if (referenceTime > currentMillis)
  {
    returnValue = 4294967295 + (currentMillis - referenceTime);
    returnValue++;
  }
  else
  {
    returnValue = currentMillis - referenceTime;
  }
  return returnValue;
}

unsigned long GetEmicroTime(unsigned long referenceTime)
{
  unsigned long returnValue;
  unsigned long currentMicros = micros();
  if (referenceTime > currentMicros)
  {
    returnValue = 4294967295 + (currentMicros - referenceTime);
    returnValue++;
  }
  else
  {
    returnValue = currentMicros - referenceTime;
  }
  return returnValue;
}

void setupBT()
{
  // BLE
  BTLEserial.setDeviceName("BrainyL"); /* 7 characters max! */
  BTLEserial.begin();
}

int readBT()
{
  BTLEserial.pollACI();
  aci_evt_opcode_t status = BTLEserial.getState();
  
  if (status == ACI_EVT_CONNECTED) 
  {
    if(BTLEserial.available() > 0)
    {
      while (BTLEserial.available()) 
      {
        char c = BTLEserial.read();
        rcvDataStr.concat(c);
      }
      Serial.println(rcvDataStr);
    }
  }
  else
  {
    return -1;
  }
  return rcvDataStr.length();
}

void writeBT(String str)
{
  Serial.print("writeBT: ");
  Serial.println(str);

  BTQueue.push(str);
}


void sendBT()
{
  if(BTQueue.isEmpty() != true)
  {
    uint8_t sendbuffer[20];
    String sendStr = BTQueue.pop ();
    sendStr.getBytes(sendbuffer, 20);
    char sendbuffersize = min(20, sendStr.length());
  
    //Serial.print("BTLEserial.getState(): "); Serial.println(BTLEserial.getState());
    
    // write the data
    if(BTLEserial.getState() == ACI_EVT_CONNECTED)
    {
      Serial.print(F("\n* Sending -> \"")); Serial.print((char *)sendbuffer); Serial.println("\"");
      BTLEserial.write(sendbuffer, sendbuffersize);
    }
    delay(100);
  }
}

void processCommand(String command)
{
  Serial.print("processCommand(): "); Serial.println(command);
  if(command.equals("UV"))  
  {
    updateValues();
  }
  if(command.equals("BO"))
  {
    //backlightControl(true);
  }
}

float getTempture()
{
  float temp = dht.readTemperature();
  
  //Serial.print("Temperature: ");
  //Serial.print(temp);
  //Serial.println("*C ");
  
  return temp;
}

float getHumidity()
{
  float humi = dht.readHumidity();
  
  //Serial.print("Humidity: ");
  //Serial.print(humi);
  //Serial.println("%");
  
  return humi;
}

int getLuxValue()
{
  int readValue = analogRead(LUX_PIN);   // Read the analogue pin
  float Vout=readValue*0.0048828125;      // calculate the voltage
  float res=10.0;
  
  int lux = 500/(res*((5-Vout)/Vout));           // calculate the Lux
  
  //Serial.print("lux: ");
  //Serial.println(lux);
  /*
  0.0001 lux  Moonless, overcast night sky (starlight)[3]
  0.002 lux Moonless clear night sky with airglow[3]
  0.27–1.0 lux  Full moon on a clear night[3][4]
  3.4 lux Dark limit of civil twilight under a clear sky[5]
  50 lux  Family living room lights (Australia, 1998)[6]
  80 lux  Office building hallway/toilet lighting[7][8]
  100 lux Very dark overcast day[3]
  320–500 lux Office lighting[6][9][10][11]
  400 lux Sunrise or sunset on a clear day.
  1000 lux  Overcast day;[3] typical TV studio lighting
  10000–25000 lux Full daylight (not direct sun)[3]
  32000–100000 lux  Direct sunlight
  */
  return lux;
}

void ledON(int color)
{
  switch(color)
  {
    case LED_OFF:
      analogWrite(LED_RED_PIN, 255);
      analogWrite(LED_GREEN_PIN, 255);
      analogWrite(LED_BLUE_PIN, 255);
      break;
    case LED_RED: 
      analogWrite(LED_RED_PIN, 0);
      analogWrite(LED_GREEN_PIN, 255);
      analogWrite(LED_BLUE_PIN, 255);
      break;
    case LED_GREEN: 
      analogWrite(LED_RED_PIN, 255);
      analogWrite(LED_GREEN_PIN, 0);
      analogWrite(LED_BLUE_PIN, 255);
      break;
    case LED_BLUE:
      analogWrite(LED_RED_PIN, 255);
      analogWrite(LED_GREEN_PIN, 255);
      analogWrite(LED_BLUE_PIN, 0);
      break;
  }
}

