package sklookie.bowwow

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import java.util.*

@SuppressLint("MissingPermission")
class ConnectThread(
    private val myUUID: UUID,
    private val device: BluetoothDevice,
) : Thread() {
    companion object {
        private const val TAG = "CONNECT_THREAD"
    }
}