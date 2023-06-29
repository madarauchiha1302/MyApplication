package com.example.beacon

import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
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
    private var txPower: Int = 0

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
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val filters: MutableList<ScanFilter> = ArrayList()
        filters.add(
            ScanFilter.Builder()
                .setServiceUuid(serviceUid)
                .build()
        )
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
           .build()
        // Connect to ble service
        val bm = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bm.adapter
        if (checkPermission()) {
            bluetoothAdapter.bluetoothLeScanner.startScan(filters, settings, scanCallback)
        }
    }

    private fun checkPermission(): Boolean {
        return true
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            val rssi = result!!.rssi // rssi of the device

            val record = result.scanRecord!!.bytes
            val structures = ADPayloadParser.getInstance().parse(record)

            // inspired from: http://darutk-oboegaki.blogspot.com/2015/08/eddystone-android.html
            for (structure in structures) {
                // If the AD structure represents Eddystone UID.
                if (structure is EddystoneUID) {
                    // Eddystone UID
                    beaconId = structure.beaconIdAsString
                    txPower = structure.txPower

                    // Path-loss exponent is approx. 2 in free space
                    // formula taken from lecture 5
                    // 62 taken from https://stackoverflow.com/questions/52962218/beacon-distance-shows-wrong-values-with-eddystone
                    val distance = "%.4f".format(10.0.pow((rssi + 62) / -(10.0 * 2)))
                    runOnUiThread {
                        tvBeaconID.text = "BeaconID: $beaconId"
                        tvDistance.text = "Distance: $distance"
                    }

                    Log.d(TAG, "Tx Power = " + structure.txPower);
                    Log.d(TAG, "Namespace ID = " + structure.namespaceIdAsString);
                    Log.d(TAG, "Instance ID = " + structure.instanceIdAsString);
                    Log.d(TAG, "Beacon ID = " + structure.beaconIdAsString);
                } else if (structure is EddystoneURL) {
                    // Eddystone URL
                    val es = structure
                    url = es.url
                    runOnUiThread {
                        tvURL.text = "URL: $url"
                    }

                    Log.d(TAG, "Tx Power = " + es.txPower);
                    Log.d(TAG, "URL = " + es.url);
                } else if (structure is EddystoneTLM) {
                    // Eddystone TLM
                    val es = structure
                    voltage = es.batteryVoltage
                    temperature = es.beaconTemperature
                    runOnUiThread {
                        tvVoltage.text = "Voltage: $voltage"
                        tvTemperature.text = "Temperature: $temperature"
                    }

                    Log.d(TAG, "TLM Version = " + es.tlmVersion);
                    Log.d(TAG, "Battery Voltage = " + es.batteryVoltage);
                    Log.d(TAG, "Beacon Temperature = " + es.beaconTemperature);
                    Log.d(TAG, "Advertisement Count = " + es.advertisementCount);
                    Log.d(TAG, "Elapsed Time = " + es.elapsedTime);
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.i("scan", "failed")
        }
    }
}