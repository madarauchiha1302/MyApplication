package com.example.beacon

import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser
import com.neovisionaries.bluetooth.ble.advertising.EddystoneTLM
import com.neovisionaries.bluetooth.ble.advertising.EddystoneUID
import com.neovisionaries.bluetooth.ble.advertising.EddystoneURL
import java.net.URL
import java.util.*
import kotlin.experimental.and
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.roundToLong


private const val REQUEST_CODE_PERMISSION_LOCATION = 2
private const val TAG = "TAG"

class MainActivity : AppCompatActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private val serviceUid = ParcelUuid.fromString("0000feaa-0000-1000-8000-00805f9b34fb")

    private lateinit var beaconId: String
    private lateinit var url: URL
    private var voltage: Int = 0
    private var temperature: Float = 0f

    private lateinit var tvBeaconID: TextView
    private lateinit var tvURL: TextView
    private lateinit var tvVoltage: TextView
    private lateinit var tvTemperature: TextView
    private lateinit var tvDistance: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvBeaconID = findViewById(R.id.tvBeaconID)
        tvURL = findViewById(R.id.tvURL)
        tvVoltage = findViewById(R.id.tvVoltage)
        tvTemperature = findViewById(R.id.tvTemperature)
        tvDistance = findViewById(R.id.tvDistance)

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE_PERMISSION_LOCATION)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val filters: MutableList<ScanFilter> = ArrayList()
        filters.add(ScanFilter.Builder().setServiceUuid(serviceUid).build())
        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        // Connect to ble service
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) !=
            PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onRequestPermissionsResult: no permission")
        }
        bluetoothAdapter.bluetoothLeScanner.startScan(filters, settings, scanCallback)
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            val rssi = result!!.rssi

            val record = result.scanRecord!!.bytes
            val structures = ADPayloadParser.getInstance().parse(record)

            // inspired from: http://darutk-oboegaki.blogspot.com/2015/08/eddystone-android.html
            for (structure in structures) {
                if (structure is EddystoneUID) {
                    beaconId = structure.beaconIdAsString

                    // formula from https://stackoverflow.com/questions/65124232/how-to-get-distance-from-beacons
                    // Path-loss exponent is approx. 2 in free space
                    // -44 is the measured power at 1m
                    val distance = "%.4f".format(10.0.pow(((-44) - rssi) / (10.0 * 2)))
                    runOnUiThread {
                        tvBeaconID.text = "BeaconID: $beaconId"
                        tvDistance.text = "Distance: $distance"
                    }
                } else if (structure is EddystoneURL) {
                    val es = structure
                    url = es.url
                    runOnUiThread {
                        tvURL.text = "URL: $url"
                    }
                } else if (structure is EddystoneTLM) {
                    val es = structure
                    voltage = es.batteryVoltage
                    temperature = es.beaconTemperature
                    runOnUiThread {
                        tvVoltage.text = "Voltage: $voltage"
                        tvTemperature.text = "Temperature: $temperature"
                    }
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.i("scan", "failed")
        }
    }
}