package expo.modules.uhfuartreader

import android.util.Log
import com.UHF.scanlable.UhfData
import java.io.File
import java.util.Timer
import java.util.TimerTask

class RFIDReaderManager {
    var uhfUartReaderModule: UhfUartReaderModule? = null

    private val scanInterval: Long = 100
    private val addr: Byte = 0xff.toByte()
    private val maxPower = 33
    private var scanFlag = false
    private var timer: Timer? = null
    private var isCanceled = false
    private val serialPorts = mutableListOf<String>()

    companion object {
        private const val TAG = "UhfUartReader"
    }

    fun connectReader(
        module: UhfUartReaderModule,
        serialPort: String,
        baudRate: Int,
    ): Boolean {
        Log.i(
            TAG,
            "Connecting to Scanner with Serial Port: $serialPort and Baud Rate: $baudRate",
        )
        uhfUartReaderModule = module

        try {
            val result = UhfData.UhfGetData.OpenUhf(baudRate, addr, serialPort, 0, null)

            if (result == 0) {
                Log.i(TAG, "Connected to Scanner Successfully!")
                uhfUartReaderModule?.isConnected = true

                startScanningHandler()
            } else {
                Log.e(TAG, "Failed To Connect Scanner!")
                Log.e(TAG, "Error Code: $result")
                uhfUartReaderModule?.isConnected = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error Connecting to Scanner!")
            e.printStackTrace()
            uhfUartReaderModule?.isConnected = false
        }

        return uhfUartReaderModule?.isConnected ?: false
    }

    fun setPower(power: Int) {
        if (power < 0 || power > 100) {
            Log.e(TAG, "Invalid Power Level! Must be between 0 and 100!")
            return
        }
        val power = (power * maxPower / 100).toByte()

        UhfData.UhfGetData.setPower(power)
    }

    fun startScanningHandler() {
        if (timer == null) {
            UhfData.Set_sound(true)
            UhfData.SoundFlag = false
            UhfData.scanResult6c = null
            isCanceled = false
            timer = Timer()
            timer?.schedule(
                object : TimerTask() {
                    override fun run() {
                        if (scanFlag) return

                        scanFlag = true
                        UhfData.read6c()
                        if (UhfData.scanResult6c == null || UhfData.scanResult6c == "") {
                            scanFlag = false
                            return
                        }

                        val epc = UhfData.scanResult6c
                        uhfUartReaderModule?.sendEvent(
                            "onRead",
                            mapOf(
                                "epc" to epc,
                            ),
                        )
                        scanFlag = false
                    }
                },
                0,
                scanInterval,
            )
        } else {
            cancelScan()
            UhfData.Set_sound(false)
        }
    }

    fun cancelScan() {
        isCanceled = true
        if (timer != null) {
            timer?.cancel()
            timer = null
        }
        UhfData.scanResult6c = null
    }

    fun disconnectReader() =
        try {
            UhfData.UhfGetData.CloseUhf()
            uhfUartReaderModule?.isConnected = false
            Log.i(TAG, "Disconnected from Scanner Successfully!")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error Disconnecting from Scanner!")
            e.printStackTrace()
            false
        }
}
