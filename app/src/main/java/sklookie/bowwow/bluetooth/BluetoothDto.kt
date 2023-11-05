package sklookie.bowwow.bluetooth

class BluetoothDto(val deviceName: String, val deviceAddress : String, var isConnect: String){
    override fun toString() = "이름 : ${deviceName}, 연결 : ${isConnect}"
}