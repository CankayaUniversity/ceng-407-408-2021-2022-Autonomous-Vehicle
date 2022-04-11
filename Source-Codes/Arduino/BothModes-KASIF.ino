// CENG 407 - Autonomous Vehicle - Manual Driving Implementation

 
  const int motorA1  = 5;  
  const int motorA2  = 6;  
  const int motorB1  = 10; 
  const int motorB2  = 9;  


  int i=0;
  int j=0; 
  int state;
  int vSpeed=255;     

  int time_sensor = 0;
  int distance = 0;
  
  const int trig_pin = 3; 
  const int echo_pin = 2;  
  const int forward_right = 6;     // INPUT 1
  const int backward_right = 10;   // INPUT 2
  const int forward_left = 5;      // INPUT 3
  const int backward_left = 9;     // INPUT 4

void setup() {
    
    pinMode(motorA1, OUTPUT);
    pinMode(motorA2, OUTPUT);
    pinMode(motorB1, OUTPUT);
    pinMode(motorB2, OUTPUT);    

      pinMode(trig_pin, OUTPUT); 
      pinMode(echo_pin, INPUT ); 
      pinMode(forward_right, OUTPUT); 
      pinMode(backward_right, OUTPUT);
      pinMode(forward_left, OUTPUT);
      pinMode(backward_left, OUTPUT);
     
      Serial.begin(9600);
}
 
void loop() {

    if(Serial.available() > 0){     
      state = Serial.read();   
    }

if(state == 'A')
{
       digitalWrite(trig_pin, HIGH); 
  delayMicroseconds(10);   
  digitalWrite(trig_pin, LOW);
  time_sensor = pulseIn(echo_pin, HIGH);
  distance = (time_sensor / 2) / 29.1;
  Serial.println(distance);

  if (distance < 20) {  // if distance smaller than 20 cm
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
  }

  else { // if distance grather than 20 cm
    /* FORWARD */
    digitalWrite(forward_right, HIGH);
    digitalWrite(backward_right, LOW);
    digitalWrite(forward_left, HIGH);
    digitalWrite(backward_left, LOW); 
  }
}
else
{
if (state == '0'){
      vSpeed=0;}
    else if (state == '1'){
      vSpeed=100;}
    else if (state == '2'){
      vSpeed=180;}
    else if (state == '3'){
      vSpeed=200;}
    else if (state == '4'){
      vSpeed=255;}

    if (state == 'F') {
      analogWrite(motorA1, vSpeed); analogWrite(motorA2, 0);
        analogWrite(motorB1, vSpeed);      analogWrite(motorB2, 0); 
    }

    else if (state == 'G') {
      analogWrite(motorA1,vSpeed ); analogWrite(motorA2, 0);  
        analogWrite(motorB1, 100);    analogWrite(motorB2, 0); 
    }

    else if (state == 'I') {
        analogWrite(motorA1, 100); analogWrite(motorA2, 0); 
        analogWrite(motorB1, vSpeed);      analogWrite(motorB2, 0); 
    }

    else if (state == 'B') {
      analogWrite(motorA1, 0);   analogWrite(motorA2, vSpeed); 
        analogWrite(motorB1, 0);   analogWrite(motorB2, vSpeed); 
    }

    else if (state == 'H') {
      analogWrite(motorA1, 0);   analogWrite(motorA2, 100); 
        analogWrite(motorB1, 0); analogWrite(motorB2, vSpeed); 
    }

    else if (state == 'J') {
      analogWrite(motorA1, 0);   analogWrite(motorA2, vSpeed); 
        analogWrite(motorB1, 0);   analogWrite(motorB2, 100); 
    }

    else if (state == 'L') {
      analogWrite(motorA1, vSpeed);   analogWrite(motorA2, 150); 
        analogWrite(motorB1, 0); analogWrite(motorB2, 0); 
    }

    else if (state == 'R') {
      analogWrite(motorA1, 0);   analogWrite(motorA2, 0); 
        analogWrite(motorB1, vSpeed);   analogWrite(motorB2, 150);     
    }
  
    else if (state == 'S'){
        analogWrite(motorA1, 0);  analogWrite(motorA2, 0); 
        analogWrite(motorB1, 0);  analogWrite(motorB2, 0);
    }  
}
}
