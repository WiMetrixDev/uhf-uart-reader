package expo.modules.uhfuartreader

import android.util.Log
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import java.io.File

class UhfUartReaderModule : Module() {
    companion object {
        private const val TAG = "UhfUartReaderModule"
    }

    val rfid = RFIDReaderManager()
    var isConnected = false
    val serialPorts = mutableListOf<String>()

    // Get list of available serial ports on the device
    fun listSerialPorts(): List<String> {
        if (serialPorts.isEmpty()) {
            val devDirectory = File("/dev")

            if (devDirectory.exists() && devDirectory.isDirectory) {
                val files = devDirectory.listFiles()

                if (files != null) {
                    for (file in files) {
                        if (file.name.startsWith("tty")) {
                            serialPorts.add(file.absolutePath)
                        }
                    }
                }
            } else {
                Log.e(TAG, "Error Listing Serial Ports! /dev Directory does not exist!")
            }
        }

        return serialPorts
    }

    // Pre-defined baud rates
    fun listBaudRates() = listOf(9600, 19200, 38400, 57600, 115200)

    override fun definition() =
        ModuleDefinition {
            Name("UhfUartReader")

            Events("onRead")

            Function("connect") { serialPort: String, baudRate: Int ->
                rfid.connectReader(this@UhfUartReaderModule, serialPort)
            }

            Function("setPower") { power: Int ->
                rfid.setPower(power)
            }

            Function("isConnected") {
                isConnected
            }

            Function("listSerialPorts") {
                listSerialPorts()
            }

            Function("listBaudRates") {
                listBaudRates()
            }

            Function("disconnect") {
                rfid.disconnectReader()
            }
        }
}
