package com.meow.runtrack

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.abs

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var acelerometro: Sensor? = null
    private var giroscopio: Sensor? = null

    private var acelerometroTexto by mutableStateOf("Sin datos")
    private var giroscopioTexto by mutableStateOf("Sin datos")

    private var recibidas by mutableStateOf(0)
    private var descartadas by mutableStateOf(0)

    private var ultimoAcelerometro = FloatArray(3)
    private var ultimoGiroscopio = FloatArray(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        giroscopio = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        setContent {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Acelerómetro")
                Text(if (acelerometro != null) acelerometroTexto else "No disponible")
                Text("Giroscopio")
                Text(if (giroscopio != null) giroscopioTexto else "No disponible")
                Text("Lecturas recibidas: $recibidas")
                Text("Lecturas descartadas: $descartadas")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        acelerometro?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        giroscopio?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    // Devuelve true si algún eje cambió más que el umbral
    private fun cambioSignificativo(nuevo: FloatArray, anterior: FloatArray, umbral: Float): Boolean {
        for (i in 0..2) {
            if (abs(nuevo[i] - anterior[i]) > umbral) {
                return true
            }
        }
        return false
    }

    override fun onSensorChanged(event: SensorEvent) {
        recibidas++

        // Se copian los valores porque Android reutiliza el mismo arreglo
        val valores = event.values.clone()
        val texto = "x: %.2f   y: %.2f   z: %.2f".format(valores[0], valores[1], valores[2])

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            if (cambioSignificativo(valores, ultimoAcelerometro, 0.1f)) {
                ultimoAcelerometro = valores
                acelerometroTexto = texto
            } else {
                descartadas++
            }
        } else if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            if (cambioSignificativo(valores, ultimoGiroscopio, 0.05f)) {
                ultimoGiroscopio = valores
                giroscopioTexto = texto
            } else {
                descartadas++
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}