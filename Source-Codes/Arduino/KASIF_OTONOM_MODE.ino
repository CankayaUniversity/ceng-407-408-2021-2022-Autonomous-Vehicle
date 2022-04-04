/* CENG 408 - KASİF UGV / Eylül ERDOĞAN / Aleyna DEDE / Ali BOZDOĞAN */
int time_sensor = 0;
int distance = 0;

const int trig_pin = 3; 
const int echo_pin = 2;  
const int forward_right = 6;     // INPUT 1
const int backward_right = 10;   // INPUT 2
const int forward_left = 5;      // INPUT 3
const int backward_left = 9;     // INPUT 4

void setup(){
  pinMode(trig_pin, OUTPUT); 
  pinMode(echo_pin, INPUT ); 
  pinMode(forward_right, OUTPUT); 
  pinMode(backward_right, OUTPUT);
  pinMode(forward_left, OUTPUT);
  pinMode(backward_left, OUTPUT);
  Serial.begin(9600);
}

void loop(){
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
