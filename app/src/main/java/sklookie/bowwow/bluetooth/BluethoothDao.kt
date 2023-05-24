package sklookie.bowwow.bluetooth

class bluetoothDao {
    val devices = ArrayList<BluetoothDto>()

    //블루투스 디바이스 정보 가져오셈, 추후 bondedDevices 정보로 변경
    init{
        devices.add(BluetoothDto("장치1", "연결됨"))
        devices.add(BluetoothDto("장치2", "연결안됨"))
        devices.add(BluetoothDto("장치4", "연결안됨"))
        devices.add(BluetoothDto("장치3", "연결안됨"))
        devices.add(BluetoothDto("장치10", "연결안됨"))
        devices.add(BluetoothDto("장치7", "연결안됨"))
        devices.add(BluetoothDto("장치9", "연결안됨"))
        devices.add(BluetoothDto("장치7", "연결안됨"))

    }
}