/* CENG 408 - KASİF UGV / Eylül ERDOĞAN / Aleyna DEDE / Ali BOZDOĞAN */
#include <Arduino.h>
#include <Wire.h>
#include <SoftwareSerial.h>


int time_sensor[3] = {0,0,0};
int distance[3]= {0,0,0};

const int trig_pin[3] = {3,8,12}; 
const int echo_pin[3] = {2,7,11};  
const int forward_right = 6;     // INPUT 1
const int backward_right = 10;   // INPUT 2
const int forward_left = 5;      // INPUT 3
const int backward_left = 9;     // INPUT 4




void setup(){
  pinMode(trig_pin[0], OUTPUT); 
  pinMode(echo_pin[0], INPUT ); 
  pinMode(trig_pin[1], OUTPUT); 
  pinMode(echo_pin[1], INPUT );
  pinMode(trig_pin[2], OUTPUT); 
  pinMode(echo_pin[2], INPUT );
  pinMode(forward_right, OUTPUT); 
  pinMode(backward_right, OUTPUT);
  pinMode(forward_left, OUTPUT);
  pinMode(backward_left, OUTPUT);
  Serial.begin(9600);
}

void loop(){
  for(int i=0;i<3;i++){
    
  
    digitalWrite(trig_pin[i], HIGH); 
    delayMicroseconds(10);   
    digitalWrite(trig_pin[i], LOW);
    time_sensor[i] = pulseIn(echo_pin[i], HIGH);
    distance[i] = (time_sensor[i] / 2) / 29.1;
    Serial.println(distance[i]);
  
  if (distance[i] < 20) {  // if distance smaller than 20 cm
    /* STOP THE UGV */
    digitalWrite(forward_right, LOW); 
    digitalWrite(backward_right, LOW);
    digitalWrite(forward_left, LOW);
    digitalWrite(backward_left, LOW);
    delay(500);
    
    /* BACKWARD */
    digitalWrite(forward_right, LOW);
    digitalWrite(backward_right, HIGH);
    digitalWrite(forward_left, LOW);
    digitalWrite(backward_left, HIGH);
    delay(500);
    
    /* TURN LEFT */
    digitalWrite(forward_right, HIGH);
    digitalWrite(backward_right, LOW);
    digitalWrite(forward_left, LOW);
    digitalWrite(backward_left, HIGH);
    delay(500);
    if (i==2){
      i=0;
    }
  }

  else { // if distance grather than 20 cm
    /* FORWARD */
    digitalWrite(forward_right, HIGH);
    digitalWrite(backward_right, LOW);
    digitalWrite(forward_left, HIGH);
    digitalWrite(backward_left, LOW); 
    if (i==2){
      i=0;
    }
  }
}
}
