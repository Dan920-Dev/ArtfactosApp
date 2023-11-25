package com.jder00138218.pdapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage


// Este codigo esta realizado para recibir y enviar señales para un encender foco y abrir una ventana(Conectado con Arduino)
// Coneccion mediante Adafruit con protocolo MQTT
class MainActivity : AppCompatActivity() {

    private lateinit var mqttAndroidClient: MqttAndroidClient

    // Estados por defecto
    private var estadoFoco: String = "OFF"
    private var estadoVentana: String = "OFF"

    private lateinit var button1 : Button
    private lateinit var button2 : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupMqttClient()

        button1 = findViewById(R.id.button1)
        button2 = findViewById(R.id.button2)


        actualizarEstadoBotones()

        button1.setOnClickListener {
            if(estadoFoco == "OFF"){
                publishMessage("foco", "ON")
            }else{
                publishMessage("foco", "OFF")
            }
        }

        button2.setOnClickListener {
            if(estadoVentana == "OFF"){
                publishMessage("ventana", "ON")
            }else{
                publishMessage("ventana", "OFF")
            }
        }

    }

    private fun actualizarEstadoBotones() {
        button1.text = "Foco está $estadoFoco"
        button2.text = "Ventana está $estadoVentana"
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mqttAndroidClient.isConnected) {
            try {
                mqttAndroidClient.disconnect()
            } catch (e: MqttException) {
                e.printStackTrace()
            }
        }
    }

    private fun publishMessage(feed: String, message: String) {
        val topic = "YourUser/feeds/$feed"
        val qos = 1
        try {
            mqttAndroidClient.publish(topic, message.toByteArray(), qos, false)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    private fun setupMqttClient() {
        val serverUri = "tcp://io.adafruit.com:1883"
        val userNameT = "YourUser"
        val userPasswd = "Your aoi Key"

        val clientId = MqttClient.generateClientId()
        mqttAndroidClient = MqttAndroidClient(this.applicationContext, serverUri, clientId)

        val options = MqttConnectOptions()
        options.userName = userNameT
        options.password = userPasswd.toCharArray()



        try {

            mqttAndroidClient.setCallback(object : MqttCallbackExtended {
                override fun connectComplete(reconnect: Boolean, serverURI: String) {
                    // Llamado cuando la conexión se completa
                    Log.d("MainActivity", "Conexión MQTT completada. Reconnect: $reconnect")

                   /* if (!reconnect) { // Si es una reconexión, es posible que no necesites solicitar el estado nuevamente
                        publishMessage("foco", "ON")
                        publishMessage("ventana", "ON")
                    } */

                }

                override fun messageArrived(topic: String, message: MqttMessage) {
                    Log.d("MainActivity", "Mensaje recibido: ${String(message.payload)}")

                    val msg = String(message.payload)
                    // Actualizar el estado según el topic y el mensaje
                    when (topic) {
                        "YourUser/feeds/nameFeed" -> estadoFoco = msg
                        "YourUser/feeds/nameFeed" -> estadoVentana = msg
                    }
                    Log.d("foco :" , estadoFoco)
                    Log.d("ventana :" , estadoVentana)
                    runOnUiThread {
                        actualizarEstadoBotones() // Actualiza la UI con el nuevo estado
                    }
                }

                override fun connectionLost(cause: Throwable?) {
                    // Llamado cuando se pierde la conexión
                }

                override fun deliveryComplete(token: IMqttDeliveryToken) {
                    // Llamado cuando un mensaje es entregado al servidor correctamente
                }
            })


            mqttAndroidClient.connect(options, null, object : IMqttActionListener {

                override fun onSuccess(asyncActionToken: IMqttToken) {


                    runOnUiThread {
                        try {
                            subscribeToTopic("YourUser/feeds/nameFeed")
                            subscribeToTopic("YourUser/feeds/nameFeed")

                            Toast.makeText(this@MainActivity, "Conectado a MQTT", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Error al conectar a MQTT: ${exception.message}", Toast.LENGTH_LONG).show()

                    }
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }


    private fun subscribeToTopic(topic: String) {

        try {
            val qos = 1
            mqttAndroidClient.subscribe(topic, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    // Suscripción exitosa, puedes hacer algo aquí si es necesario
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    // La suscripción falló, manejar el error
                    Toast.makeText(this@MainActivity, "Suscripción fallida: ${exception?.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }


}