 /*
 PROYECTO: POO3 - TP - ARDUINO
 ALUMNOS: Mendoza Dante, Oyola Ruth, Heredia Fernando.
 FECHA: 21/11/2017

PASOS DE INSTALACION:
1 Importe las librerias:
  Adafruit_Sensor-master
  DHT-sensor-library-master

2 Desconectar el modulo Bluetooth mientras carga el programa al Arduino, luego conecte:
ARDUINO  BLUETOOTH
Pin 0 (RX)  TXD
Pin 1 (Tx)  RXD
5V    VCC
GND   GND

*/

// Prueba de sensor de Humedad - Temperatura DHT11 con PCB.
#include <DHT.h>
//#include <Adafruit_Sensor.h>
 
// Definimos el pin digital donde se conecta el sensor
#define DHTPIN 2
// Dependiendo del tipo de sensor
#define DHTTYPE DHT11
 
// Inicializamos el sensor DHT11
DHT dht(DHTPIN, DHTTYPE);

#define CARACTER_INICIO_CMD '*'
#define CARACTER_FINAL_CMD '#'
#define CARACTER_DIV_CMD '|'
#define ESCRITURA_DIGITAL_CMD 10
#define ESCRITURA_ANALOGA_CMD 11
#define TEXTO_CMD 12
#define LECTURA_ARDUDROID_CMD 13
#define MAX_COMMAND 20  
#define MIN_COMMAND 10 
#define LONGITUD_ENTRADA_STRING 40
#define ESCRITURA_ANALOGICA_MAX 255
#define PIN_ALTO 3
#define PIN_BAJO 2

String inText;

void setup() {
  Serial.begin(9600);
  Serial.println("POO3 - TP - ARDUINO");
  Serial.flush();

  // Comenzamos el sensor DHT
  dht.begin();
}

void loop()
{
  Serial.flush();
  int ard_command = 0;
  int pin_num = 0;
  int pin_value = 0;

  char get_char = ' ';  //lee serial

  // esperar a que los datos entren
  if (Serial.available() < 1) return; // si no hay datos en el serial retornar al Loop().

  // analizar entrada de indicador de inicio de comando
  get_char = Serial.read();
  if (get_char != CARACTER_INICIO_CMD) return; // si no hay indicación de inicio del sistema, volver  loop ().
  ard_command = Serial.parseInt(); // leer el comando.
  
  // analizar el tipo de comando entrante
  pin_num = Serial.parseInt(); // leer el pin
  pin_value = Serial.parseInt();  // leer el valor

  // OBTENER DATOS DE digitalWrite ARDUDROID
  if (ard_command == ESCRITURA_DIGITAL_CMD){  
    if (pin_value == PIN_BAJO) pin_value = LOW;
    else if (pin_value == PIN_ALTO) pin_value = HIGH;
    else return; // error en el valor de PIN. regresar.
    set_digitalwrite( pin_num,  pin_value);
    return;  // regrese al inicio de loop()
  }
}

// seleccionar el pin # solicitado para la acción digitalWrite
void set_digitalwrite(int pin_num, int pin_value)
{
  switch (pin_num) {
  case 13:
    pinMode(13, OUTPUT);
    digitalWrite(13, pin_value);         
    // Colocar el codigo, para este número de pin del Arduino
    break;
  case 2:
    pinMode(2, OUTPUT);
    digitalWrite(2, pin_value); 
    // Colocar el codigo, para este número de pin del Arduino
    Serial.println("");
    Serial.print("CALCULANDO, ESPERE...");
    Serial.println("");

    // Esperamos 5 segundos para que el sensor tome muestras.
    delay(5000);
 
    // Leemos la humedad relativa
    float h = dht.readHumidity();
    // Leemos la temperatura en grados centígrados (por defecto)
    float t = dht.readTemperature();
 
    // Comprobamos si ha habido algún error en la lectura
    if (isnan(h) || isnan(t)) {
      Serial.println("Error obteniendo los datos del sensor DHT11");
      return;
    }
 
    Serial.print("Humedad: ");
    Serial.print(h);
    Serial.print("%");
    delay(3000);
    Serial.println("");
    Serial.print("Temperatura: ");
    Serial.print(t);
    Serial.print("*C ");
    Serial.println("");
  break;      
  } 
}
