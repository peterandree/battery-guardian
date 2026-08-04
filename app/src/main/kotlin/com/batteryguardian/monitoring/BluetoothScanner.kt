package com.batteryguardian.monitoring

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.batteryguardian.domain.model.DeviceType
import com.batteryguardian.domain.repository.DeviceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scans for Bluetooth devices and manages device discovery.
 * 
 * Supports both Classic Bluetooth and BLE scanning.
 */
@Singleton
class BluetoothScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deviceRepository: DeviceRepository
) {

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }

    private val scanScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val deviceChannel = Channel<BluetoothDevice>(Channel.UNLIMITED)
    
    private var isScanning = false
    private var bleScanner: android.bluetooth.le.BluetoothLeScanner? = null
    private var scanCallback: BleScanCallback? = null

    /**
     * Flow of discovered Bluetooth devices.
     */
    val discoveredDevices: Flow<BluetoothDevice> = deviceChannel.receiveAsFlow()

    /**
     * Start scanning for Bluetooth devices.
     */
    fun startScanning() {
        if (isScanning) return
        if (bluetoothAdapter == null) return
        
        isScanning = true
        
        // Check Bluetooth permission
        if (!hasBluetoothPermission()) {
            isScanning = false
            return
        }
        
        // Check if Bluetooth is enabled
        if (!bluetoothAdapter!!.isEnabled) {
            isScanning = false
            return
        }
        
        // Start BLE scanning
        startBleScanning()
        
        // Start Classic Bluetooth discovery (deprecated but still used)
        startClassicDiscovery()
    }

    /**
     * Stop scanning for Bluetooth devices.
     */
    fun stopScanning() {
        if (!isScanning) return
        
        isScanning = false
        stopBleScanning()
        stopClassicDiscovery()
        
        scanScope.launch {
            deviceChannel.close()
        }
    }

    /**
     * Start BLE scanning.
     */
    private fun startBleScanning() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return
        
        bleScanner = bluetoothAdapter?.bluetoothLeScanner
        scanCallback = BleScanCallback()
        
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
            .build()
        
        bleScanner?.startScan(null, scanSettings, scanCallback)
    }

    /**
     * Stop BLE scanning.
     */
    private fun stopBleScanning() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return
        
        bleScanner?.stopScan(scanCallback)
        bleScanner = null
        scanCallback = null
    }

    /**
     * Start Classic Bluetooth discovery.
     */
    private fun startClassicDiscovery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // On Android 12+, we need BLUETOOTH_CONNECT permission
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }
        
        val handler = Handler(Looper.getMainLooper())
        
        // Discovery is deprecated but still works for some devices
        bluetoothAdapter?.startDiscovery()
        
        // Register for discovery results
        // In a real implementation, this would use a BroadcastReceiver
    }

    /**
     * Stop Classic Bluetooth discovery.
     */
    private fun stopClassicDiscovery() {
        bluetoothAdapter?.cancelDiscovery()
    }

    /**
     * Check if we have Bluetooth permissions.
     */
    private fun hasBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Get a list of already paired Bluetooth devices.
     */
    fun getPairedDevices(): List<BluetoothDevice> {
        return bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
    }

    /**
     * Add a discovered device to the repository if it's not already there.
     */
    suspend fun addDiscoveredDevice(device: BluetoothDevice) {
        val deviceExists = deviceRepository.deviceExists(device.address)
        
        if (!deviceExists) {
            val deviceType = DeviceClassifier.classify(device.name ?: "")
            
            deviceRepository.saveDevice(
                com.batteryguardian.domain.model.Device(
                    id = device.address,
                    name = device.name ?: "Unknown",
                    alias = null,
                    type = deviceType,
                    manufacturer = extractManufacturer(device.name ?: ""),
                    bluetoothClass = device.bluetoothClass,
                    lastSeen = Instant.now(),
                    currentBatteryLevel = null,
                    isCharging = null,
                    isConnected = false,
                    isMonitored = true,
                    isIgnored = false,
                    batteryHealth = null,
                    alertState = com.batteryguardian.domain.model.AlertState.Normal,
                    capabilities = null
                )
            )
        } else {
            // Update last seen timestamp
            deviceRepository.updateDeviceStatus(
                deviceId = device.address,
                batteryLevel = null,
                isCharging = null,
                isConnected = true,
                lastSeen = Instant.now()
            )
        }
    }

    /**
     * Extract manufacturer from device name.
     */
    private fun extractManufacturer(deviceName: String): String? {
        return when {
            deviceName.contains("Sennheiser", ignoreCase = true) -> "Sennheiser"
            deviceName.contains("Sony", ignoreCase = true) -> "Sony"
            deviceName.contains("Bose", ignoreCase = true) -> "Bose"
            deviceName.contains("JBL", ignoreCase = true) -> "JBL"
            deviceName.contains("Apple", ignoreCase = true) -> "Apple"
            deviceName.contains("Samsung", ignoreCase = true) -> "Samsung"
            deviceName.contains("LG", ignoreCase = true) -> "LG"
            deviceName.contains("ThinkPlus", ignoreCase = true) -> "ThinkPlus"
            else -> null
        }
    }

    /**
     * BLE scan callback.
     */
    private inner class BleScanCallback : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            
            val device = result.device
            scanScope.launch {
                deviceChannel.send(device)
                addDiscoveredDevice(device)
            }
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            super.onBatchScanResults(results)
            
            results.forEach { result ->
                val device = result.device
                scanScope.launch {
                    deviceChannel.send(device)
                    addDiscoveredDevice(device)
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            // Handle scan failure
        }
    }
}

/**
 * Utility class for classifying Bluetooth device types.
 */
object DeviceClassifier {
    
    /**
     * Classify a device based on its name.
     */
    fun classify(deviceName: String): DeviceType {
        val lowerName = deviceName.lowercase()
        
        return when {
            // Headphones
            lowerName.contains("headphone") ||
            lowerName.contains("headset") ||
            lowerName.contains("earbud") ||
            lowerName.contains("earphone") ||
            lowerName.contains("buds") ||
            lowerName.contains("wh-") ||
            lowerName.contains("mh") ||
            lowerName.contains("mtw") ||
            lowerName.contains("wf-") ||
            lowerName.contains("airpod") ||
            lowerName.contains("galaxy buds") ||
            lowerName.contains("sennheiser") ||
            lowerName.contains("sony wh") ||
            lowerName.contains("bose qc") ||
            lowerName.contains("jbl live") ||
            lowerName.contains("accentum") -> DeviceType.HEADPHONES
            
            // Speakers
            lowerName.contains("speaker") ||
            lowerName.contains("soundbar") ||
            lowerName.contains("homepod") ||
            lowerName.contains("echo") ||
            lowerName.contains("google home") ||
            lowerName.contains("sonos") ||
            lowerName.contains("jbl charge") ||
            lowerName.contains("jbl party") ||
            lowerName.contains("bose sound") -> DeviceType.SPEAKER
            
            // Smartwatches
            lowerName.contains("watch") ||
            lowerName.contains("galaxy watch") ||
            lowerName.contains("apple watch") ||
            lowerName.contains("wear os") ||
            lowerName.contains("fitbit") ||
            lowerName.contains("garmin") ||
            lowerName.contains("amazfit") ||
            lowerName.contains("huawei watch") -> DeviceType.SMARTWATCH
            
            // Keyboards
            lowerName.contains("keyboard") ||
            lowerName.contains("kb") ||
            lowerName.contains("keychron") ||
            lowerName.contains("logitech k") ||
            lowerName.contains("microsoft keyboard") -> DeviceType.KEYBOARD
            
            // Mice
            lowerName.contains("mouse") ||
            lowerName.contains("m") ||
            lowerName.contains("mx") ||
            lowerName.contains("logitech m") ||
            lowerName.contains("microsoft mouse") -> DeviceType.MOUSE
            
            // Game Controllers
            lowerName.contains("controller") ||
            lowerName.contains("gamepad") ||
            lowerName.contains("joystick") ||
            lowerName.contains("xbox") ||
            lowerName.contains("playstation") ||
            lowerName.contains("dualsense") ||
            lowerName.contains("dualshock") ||
            lowerName.contains("8bitdo") -> DeviceType.GAME_CONTROLLER
            
            // Hearing Aids
            lowerName.contains("hearing aid") ||
            lowerName.contains("hearing") ||
            lowerName.contains("oticon") ||
            lowerName.contains("phonak") ||
            lowerName.contains("rexton") -> DeviceType.HEARING_AID
            
            // Medical Devices
            lowerName.contains("medical") ||
            lowerName.contains("health") ||
            lowerName.contains("glucose") ||
            lowerName.contains("blood pressure") -> DeviceType.MEDICAL_DEVICE
            
            else -> DeviceType.OTHER
        }
    }
}
