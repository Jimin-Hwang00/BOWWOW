package sklookie.bowwow

import android.Manifest
import android.annotation.SuppressLint
import android.app.usage.UsageEvents
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import sklookie.bowwow.databinding.ActivityMainBinding
import java.util.UUID


//권한추가(안드12이상..권한 3개 더 추가)
const val REQUEST_ALL_PERMISSION = 1
val PERMISSIONS = arrayOf(
    Manifest.permission.BLUETOOTH,
    Manifest.permission.BLUETOOTH_SCAN,
    Manifest.permission.BLUETOOTH_ADVERTISE,
    Manifest.permission.BLUETOOTH_CONNECT
)
class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private val bluetoothManager: BluetoothManager by lazy {
        getSystemService(BluetoothManager::class.java)
    }
    private val mBluetoothAdapter: BluetoothAdapter? by lazy {
        bluetoothManager.adapter
    }
    var myDevices = mutableMapOf<String?, String?>()
    var serchDevices = mutableMapOf<String?, String?>()

    //장치 검색 때 필요한 변수
    private lateinit var broadcastReceiver: BroadcastReceiver


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val devices = bluetoothDao().devices
        val adapter = DeviceAdapter(devices)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL

        //블루투스 권한 묻기
        if (!hasPermissions(this, PERMISSIONS)) {
            requestPermissions(PERMISSIONS, REQUEST_ALL_PERMISSION)
        }

        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter

        //원하는 장치 클릭시, 연결 시작
        val listener = object : DeviceAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                if (devices[position].isConnect.equals("연결됨")) {
                    devices[position].isConnect = "연결안됨"

                    Toast.makeText(
                        this@MainActivity,
                        "${devices[position].deviceName}연결 취소",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    devices[position].isConnect = "연결됨"

                    Toast.makeText(
                        this@MainActivity,
                        "${devices[position].deviceName}연결 완료",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                adapter.notifyDataSetChanged()
            }
        }
        adapter.setOnItemClickListener(listener)

        //내 장치 정보 버튼 클릭
        binding.bellConnect.setOnClickListener {
            getPairedDevices()
        }

        //기기 검색 저장
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    //찾은 기기가 있으면, bluetoothDao에 저장, broadcastReceiver에 기기 저장
                    BluetoothDevice.ACTION_FOUND -> {
                        val device =
                            intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)


                        //임시방편 : device?.name으로 가져와야하는데.. permission 오류 발생
                        val deviceName = "블루투스 장치"
                        val deviceHardwareAddress = device?.address
                        if (deviceName != null && deviceHardwareAddress != null) {
                            devices.add(bluetoothDto(deviceName, deviceHardwareAddress, "연결안됨"))
                        }
                    }
                    else -> Toast.makeText(
                        this@MainActivity,
                        "블루투스 기기가 없습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        // BroadcastReceiver 등록
        var filter: IntentFilter = IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(broadcastReceiver, filter)

        adapter.notifyDataSetChanged()
        //검색된 장치 정보 테스트
//        Toast.makeText(
//            this@MainActivity, devices.toString(), Toast.LENGTH_SHORT
//        ).show()
    }

    //기기 검색 후, 해제 필수
    override fun onDestroy() {
        super.onDestroy()
        // BroadcastReceiver 등록해제
        unregisterReceiver(broadcastReceiver)
    }

    // (06.04) 아두이노 디바이스에 연결
    private fun connectDevice(deviceAddress: String) {
        mBluetoothAdapter?.let { adapter ->
            // 기기 검색을 수행중이라면 취소
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            if (adapter.isDiscovering) {
                adapter.cancelDiscovery()
            }

            // 서버의 역할을 수행 할 Device 획득
            val device = adapter.getRemoteDevice(deviceAddress)
            // UUID 선언
            val myUUID: UUID = UUID.randomUUID()
            try {
                val thread = ConnectThread(myUUID, device)

                thread.run()
                Toast.makeText(
                    this@MainActivity, "${device.name}과 연결되었습니다." +
                            " 기기를 확인해주세요.", Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) { // 연결에 실패할 경우 호출됨
                Toast.makeText(
                    this@MainActivity, "기기의 전원이 꺼져 있습니다." +
                            " 기기를 확인해주세요.", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    //페어링 장치 가져오기
    fun getPairedDevices() {
        mBluetoothAdapter?.let {
            // 블루투스 활성화 상태라면
            if (it.isEnabled) {
                // ArrayAdapter clear
                myDevices.clear()
                // 페어링된 기기 확인

                @SuppressWarnings("MissingPermission")
                //페어링된 기기 set에 저장
                val pairedDevices: Set<BluetoothDevice> = it.bondedDevices
                // 페어링된 기기가 존재하는 경우
                if (pairedDevices.isNotEmpty()) {
                    pairedDevices.forEach { device ->
                        // 페어링 장치 mac주소, 이름 Map에 저장
                        if (ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            return
                        }
                        myDevices.put(device.name, device.address)
                    }
                } else {
                    Toast.makeText(this, "페어링된 기기가 없습니다.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "블루투스가 비활성화 되어 있습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //기기 검색
    private fun deviceDiscovering(){
        mBluetoothAdapter?.let {
            if (it.isEnabled) {
                // 현재 검색중이라면
                if (it.isDiscovering) {
                    // 검색 취소
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.BLUETOOTH_SCAN
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return
                    }
                    it.cancelDiscovery()
                    Toast.makeText(this, "기기검색이 중단되었습니다.", Toast.LENGTH_SHORT).show()
                    return
                }
                // ArrayAdapter clear
//                    devices.clear()
                // 검색시작
                it.startDiscovery()
                Toast.makeText(this, "기기 검색을 시작하였습니다", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "블루투스가 비활성화되어 있습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }


    //권한추가1
    private fun hasPermissions(context: Context?, permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (context?.let { ActivityCompat.checkSelfPermission(it, permission) }
                != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    // Permission check (권한추가2)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_ALL_PERMISSION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "블루투스가 켜졌습니다!", Toast.LENGTH_SHORT).show()
                    deviceDiscovering()
                } else {
                    requestPermissions(permissions, REQUEST_ALL_PERMISSION)
                    Toast.makeText(this, "블루투스를 켜주세요!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}