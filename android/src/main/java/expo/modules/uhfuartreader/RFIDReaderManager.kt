package expo.modules.uhfuartreader

import android.util.Log
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class RFIDReaderManager {
    var uhfUartReaderModule: UhfUartReaderModule? = null

    private var mFileInputStream: FileInputStream? = null
    private var mFileOutputStream: FileOutputStream? = null
    private var mFd: FileDescriptor? = null
    private var isScanning = false
    private val cmd = byteArrayOf(0x09, 0x00, 0x01, 0x04, 0x00, 0x00, 0x80.toByte(), 0x0A, 0x22, 0xDA.toByte())
    private val powercmd = ByteArray(6)
    private var thread: Thread? = null
    private val maxPower = 30

    // Used to load the 'native-lib' library on application startup.
    companion object {
        private const val TAG = "UhfUartReader"

        const val PRESET_VALUE = 0xFFFF
        const val POLYNOMIAL = 0x8408

        init {
            System.loadLibrary("native-lib")
        }
    }

    fun connectReader(
        module: UhfUartReaderModule,
        serialPort: String,
    ): Boolean {
        Log.i(
            TAG,
            "Connecting to Scanner with Serial Port: $serialPort",
        )
        uhfUartReaderModule = module
        if (isScanning) return true

        if (mFd == null) {
            mFd = openPath(serialPort)
            if (mFd == null) {
                Log.e(TAG, "native open returns null")
                return false
            }
            if (mFd != null) {
                mFileInputStream = FileInputStream(mFd)
                mFileOutputStream = FileOutputStream(mFd)
            }
        }

        isScanning = true
        thread =
            Thread {
                Log.d(TAG, "Scanning Thread Started")
                while (mFd != null) {
                    send()
                    if (mFileInputStream != null) {
                        val receivedData = ByteArray(64)
                        try {
                            val readInputValue = mFileInputStream!!.read(receivedData)
                            receivedData[readInputValue] = 0
                            val packetLength = receivedData[0] + 1
                            if (readInputValue > 0 && packetLength > 10) {
                                val epcLength = receivedData[6]
                                Log.d(TAG, "Received Data: ${receivedData.joinToString()}")
                                val crc = calculateCrc16(receivedData, packetLength.toByte())
                                Log.d(TAG, "CRC: $crc")
                                // if (crc == 0) {
                                val epc = StringBuilder()
                                for (i in 0 until epcLength) {
                                    epc.append(String.format("%02X", receivedData[i + 7]))
                                }
                                Log.d(TAG, "EPC: $epc")
                                Log.d(TAG, "isScanning: $isScanning")
                                if (isScanning) {
                                    uhfUartReaderModule?.sendEvent(
                                        "onRead",
                                        mapOf(
                                            "epc" to epc.toString(),
                                        ),
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    try {
                        Thread.sleep(200)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
            }
        thread!!.start()

        return true
    }

    private fun send() {
        Log.d(TAG, "Sending Command")
        if (mFileOutputStream != null) {
            try {
                mFileOutputStream!!.write(cmd)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun calculateCrc16(
        data: ByteArray,
        length: Byte,
    ): Int {
        var crcValue: Short = PRESET_VALUE.toShort()

        for (i in 0 until length.toInt()) {
            crcValue = (crcValue.toInt() xor data[i.toInt()].toInt().and(0xFF)).toShort()

            for (j in 0 until 8) {
                crcValue =
                    if (crcValue.toInt() and 0x0001 != 0) {
                        (crcValue.toInt() shr 1 xor POLYNOMIAL).toShort()
                    } else {
                        (crcValue.toInt() shr 1).toShort()
                    }
            }
        }
        return crcValue.toInt() and 0xFFFF
    }

    fun setPower(power: Int) {
        if (power < 0 || power > 100) {
            Log.e(TAG, "Invalid Power Level! Must be between 0 and 100!")
            return
        }
        val power = (power * maxPower / 100).toByte()

        try {
            powercmd[0] = 0x05
            powercmd[1] = 0x00
            powercmd[2] = 0x2F
            powercmd[3] = power
            val crc = calculateCrc16(powercmd, 4.toByte())
            powercmd[4] = (crc and 0xFF).toByte()
            powercmd[5] = ((crc shr 8) and 0xFF).toByte()
            mFileOutputStream?.write(powercmd)
            Log.i(TAG, "Reader Power set to $power")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Failed to set Reader Power to $power")
        }
    }

    fun disconnectReader() {
        if (isScanning) {
            isScanning = false
            close()
            mFileInputStream = null
            mFileOutputStream = null
            mFd = null
            thread!!.interrupt()
        }
    }

    private external fun openPath(path: String): FileDescriptor

    private external fun close()
}
