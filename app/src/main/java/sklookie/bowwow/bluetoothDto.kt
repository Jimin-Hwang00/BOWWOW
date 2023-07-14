package sklookie.bowwow

class bluetoothDto(val deviceName: String, val deviceAddress : String, var isConnect: String){
    override fun toString() = "이름 : ${deviceName}, 연결 : ${isConnect}"
}