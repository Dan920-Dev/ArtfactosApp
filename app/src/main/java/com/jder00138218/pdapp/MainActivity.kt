package com.jder00138218.pdapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException

class MainActivity : AppCompatActivity() {

    private lateinit var mqttAndroidClient: MqttAndroidClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupMqttClient()

        val button1: Button = findViewById(R.id.button1)
        val button2: Button = findViewById(R.id.button2)

        button1.setOnClickListener { publishMessage("foco", "ON") }
        button2.setOnClickListener { publishMessage("ventana", "OFF") }


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
        val topic = "Txgerjd/feeds/$feed"
        val qos = 1
        try {
            mqttAndroidClient.publish(topic, message.toByteArray(), qos, false)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    private fun setupMqttClient() {
        val serverUri = "tcp://io.adafruit.com:1883"
        val userNameT = "Txgerjd"
        val userPasswd = "aio_SGSG60RMJ1jDoSmF98K2dr1fdKaR"

        val clientId = MqttClient.generateClientId()
        mqttAndroidClient = MqttAndroidClient(this.applicationContext, serverUri, clientId)

        val options = MqttConnectOptions()
        options.userName = userNameT
        options.password = userPasswd.toCharArray()

        try {
            mqttAndroidClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    runOnUiThread {
                        try {
                            Toast.makeText(this@MainActivity, "Conectado a MQTT", Toast.LENGTH_SHORT).show()
                            // Habilitar botones aquí si es necesario.
                        } catch (e: Exception) {
                            e.printStackTrace()
                            // Manejar la excepción aquí.
                        }
                    }
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Error al conectar a MQTT: ${exception.message}", Toast.LENGTH_LONG).show()
                        // Aquí puedes deshabilitar ciertas funcionalidades o permitir al usuario reintentar la conexión.
                    }
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }


}