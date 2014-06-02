
// Arduino standard library imports
#include <Arduino.h>
//#include <Wire.h>
//#include <EEPROM.h>

#include <LowPower_Teensy3.h>


#include "CycleTacho5.h"

//TEENSY3_LP LP = TEENSY3_LP();

#define MOTORS 8
#define IRQspeed 1
#define IRQcadence 2

// Create an IntervalTimer object 
IntervalTimer myTimer;
int iWheelStopped = 0;  // Wheel Stopped
int iPeddalStopped = 0;  // Peddals Stopped

float fSpeed = 0;
int CadenceRPM = 0;
boolean bPedaling = false;



//*************************************************************************************************************************

void setup() {
  myTimer.begin(blinkLED, 1000000);  // blinkLED to run every x seconds
  pinMode(ledPin, OUTPUT);
  Serial.begin(115200);
  setupFTM0();
  //setupFTM1();
  delay(100);
  fSpeed = 60;
  updateApp();  
  digitalWrite(ledPin, HIGH);

}

void loop() {

  //Serial.println(doProcess);
  //Serial.flush();

  
  if(doProcess==IRQspeed)
  {
    int temp = FTM0Count;
    if(timerOverflow) 
    {
        fSpeed = 0;
    }
    else              
    {      if(temp>=1500)   
            {
              fSpeed = (116631.6/temp);      // kph
            }
    }
    iWheelStopped = 4;
  }
  
  if(doProcess==IRQcadence)
  {
    int temp = FTM0Count1;
    if(timerOverflow) 
    {
        CadenceRPM = 0;
    }
    else              
    {      if(temp>=1500)   
            {
              CadenceRPM = 60/(temp/(31250.0/2)); // Captured time of wheel 
            }
    }
    iPeddalStopped = 10;
  }
 
  if(doProcess!=0)
  { 
    doProcess = 0;
    updateApp();
  }

   //LP.Sleep();
}


// functions called by IntervalTimer should be short, run as quickly as
// possible, and should avoid calling other functions if possible.
void blinkLED(void) {
  //digitalWrite(ledPin, !digitalRead(ledPin));
  iWheelStopped--;
  if(iWheelStopped<=0)  
  {    
    //digitalWrite(ledPin, !digitalRead(ledPin));  
    fSpeed = 0; 
    CadenceRPM = 0;
    updateApp();
    digitalWrite(ledPin, LOW);
    iWheelStopped = 4;
    iPeddalStopped = 10;
  }
  
}



// functions called to transmitt update to SmartPhone App
void updateApp(void) {
    
    Serial.print("{");                // Start JSON
                  
    Serial.print("'speed': ");
    Serial.print(fSpeed);      // kph
    
    Serial.print(",");
    
    Serial.print("'pedaling': ");
    Serial.print(bPedaling);         // is rider peddaling 
    
    Serial.print(",");            
    
    Serial.print("'bat': ");         // 48V divided by 10
    Serial.print(analogRead(A0)/10.23);
    
    Serial.print(",");            
    
    Serial.print("'AssistLevelV': "); // Low=1.5v / Med = 2.0v / High =2.5v
    Serial.print(analogRead(A1)/1023.0f);
    
    Serial.print(",");            
    
    Serial.print("'ThrottleV': ");     // User Throttle 0 - 5V
    Serial.print(analogRead(A2)/204.6);
    
    Serial.print(",");            
    
    Serial.print("'CadenceRPM': ");    // peddal RPM
    Serial.print(CadenceRPM);

    Serial.print(",");            
    
    Serial.print("'WheelRotations': ");    // back Wheel RPM
    Serial.print(WheelRotations);
    
    Serial.println("}");
    
    Serial.flush();

}
