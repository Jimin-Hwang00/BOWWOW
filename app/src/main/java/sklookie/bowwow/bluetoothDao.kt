package sklookie.bowwow

class bluetoothDao {
    val devices = ArrayList<bluetoothDto>()

    //블루투스 디바이스 정보 가져오셈, 추후 bondedDevices 정보로 변경
    init{
        devices.add(bluetoothDto("장치1", "연결됨"))
        devices.add(bluetoothDto("장치2", "연결안됨"))
        devices.add(bluetoothDto("장치4", "연결안됨"))
        devices.add(bluetoothDto("장치3", "연결안됨"))
        devices.add(bluetoothDto("장치10", "연결안됨"))
        devices.add(bluetoothDto("장치7", "연결안됨"))
        devices.add(bluetoothDto("장치9", "연결안됨"))
        devices.add(bluetoothDto("장치7", "연결안됨"))

    }
}