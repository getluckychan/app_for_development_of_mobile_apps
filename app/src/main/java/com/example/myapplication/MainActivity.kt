package com.example.myapplication

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.io.OutputStream
import java.util.*

class MainActivity : AppCompatActivity() {

    private val REQUEST_BLUETOOTH_PERMISSION = 1
    private val DEVICE_ADDRESS = "YOUR_BLUETOOTH_DEVICE_ADDRESS"

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothSocket: BluetoothSocket
    private lateinit var outputStream: OutputStream
    private lateinit var accelerometerSensorManager: SensorManager
    private lateinit var accelerometerSensor: Sensor
    private lateinit var handler: Handler
    private var isMeasuring = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check Bluetooth permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH),
                REQUEST_BLUETOOTH_PERMISSION
            )
        }

        // Initialize Bluetooth adapter
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        }

        // Check if Bluetooth is supported on the device

        // Check if Bluetooth is enabled
        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
                !bluetoothAdapter.isEnabled
            } else {
                TODO("VERSION.SDK_INT < ECLAIR")
            }
        ) {
            // Enable Bluetooth
            val enableBluetoothIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            } else {
                TODO("VERSION.SDK_INT < ECLAIR")
            }
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
        }

        // Get Bluetooth device by address
        val device: BluetoothDevice? = bluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS)

        // Establish Bluetooth connection
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                bluetoothSocket = device?.createInsecureRfcommSocketToServiceRecord(MY_UUID)!!
            }
            bluetoothSocket.connect()
            outputStream = bluetoothSocket.outputStream
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // Initialize accelerometer sensor manager
        accelerometerSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Get accelerometer sensor
        accelerometerSensor = accelerometerSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Initialize accelerometer event listener
        handler = Handler(Looper.getMainLooper())
        val accelerometerEventListener = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
            object : SensorEventListener {
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

                override fun onSensorChanged(event: SensorEvent) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]

                    // Process accelerometer data
                    // TODO: Implement your logic for motion detection and sending notifications via Bluetooth
                }
            }
        } else {
            TODO("VERSION.SDK_INT < CUPCAKE")
        }

        val startButton: Button = findViewById(R.id.startButton)

        // Handle click event on the "Start Measurement" button
        startButton.setOnClickListener {
            if (!isMeasuring) {
                accelerometerSensorManager.registerListener(
                    accelerometerEventListener,
                    accelerometerSensor,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
                isMeasuring = true
                startButton.text = "Stop Measurement"
            } else {
                accelerometerSensorManager.unregisterListener(accelerometerEventListener)
                isMeasuring = false
                startButton.text = "Start Measurement"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Close Bluetooth connection
        try {
            outputStream.close()
            bluetoothSocket.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val REQUEST_ENABLE_BLUETOOTH = 2
    }
}
