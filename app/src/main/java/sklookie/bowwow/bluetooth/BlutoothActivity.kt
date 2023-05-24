package sklookie.bowwow.bluetooth

import android.Manifest
import android.app.usage.UsageEvents
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import sklookie.bowwow.databinding.ActivityBluetoothBinding
import sklookie.bowwow.databinding.ActivityMainBinding
import java.util.UUID


//권한추가
const val REQUEST_ALL_PERMISSION = 1
val PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION
)
class BlutoothActivity : AppCompatActivity() {
    lateinit var binding : ActivityBluetoothBinding
    private val bluetoothManager: BluetoothManager by lazy {
        getSystemService(BluetoothManager::class.java)
    }
    private val mBluetoothAdapter: BluetoothAdapter? by lazy {
        bluetoothManager.adapter
    }
    var connected: MutableLiveData<Boolean?> = MutableLiveData(null)
    var progressState: MutableLiveData<String> = MutableLiveData("")
    var foundDevice:Boolean = false
    var targetDevice: BluetoothDevice? = null
    val inProgress = MutableLiveData<UsageEvents.Event>()
    var myDevices = mutableMapOf<String?, String?>()
    var devices = mutableMapOf<String?, String?>()
    private val myUUID : UUID = UUID.randomUUID()
    //장치 검색 때 필요한 변수
    private lateinit var broadcastReceiver: BroadcastReceiver


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBluetoothBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //기기 검색
        broadcastReceiver = object : BroadcastReceiver(){
            override fun onReceive(p0: Context?, p1: Intent?) {
                when(intent?.action){
                    BluetoothDevice.ACTION_FOUND -> {
                        val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        if (ActivityCompat.checkSelfPermission(
                                this@BlutoothActivity,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            return
                        }
                        val deviceName = device?.name
                        val addr = device?.address
                        val deviceHardwareAddress = device?.address
                        if (deviceName != null && deviceHardwareAddress != null) {
                            devices.put(deviceName, deviceHardwareAddress)
                        }
                    }
                }
            }
        }


        //검색된 장치 정보 테스트
        Toast.makeText(
            this@BlutoothActivity, "${devices.keys}장치 저장", Toast.LENGTH_SHORT
        ).show()


        //블루투스 권한 묻기
        if (!hasPermissions(this, PERMISSIONS)) {
            requestPermissions(PERMISSIONS, REQUEST_ALL_PERMISSION)
        }

        val devices = bluetoothDao().devices
        val adapter = DeviceAdapter(devices)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL

        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter

        //원하는 장치 클릭시, 연결 시작
        val listener = object : DeviceAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                if(devices[position].isConnect.equals("연결됨")){
                    devices[position].isConnect = "연결안됨"

                    Toast.makeText(
                        this@BlutoothActivity, "${devices[position].deviceName}연결 취소", Toast.LENGTH_SHORT
                    ).show()
                }
                else{
                    devices[position].isConnect = "연결됨"

                    Toast.makeText(
                        this@BlutoothActivity, "${devices[position].deviceName}연결 완료", Toast.LENGTH_SHORT
                    ).show()
                }

                adapter.notifyDataSetChanged()
            }
        }
        adapter.setOnItemClickListener(listener)

        //내 장치 정보 버튼 클릭
        binding.bellConnect.setOnClickListener{
            getPairedDevices()
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
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
                } else {
                    requestPermissions(permissions, REQUEST_ALL_PERMISSION)
                    Toast.makeText(this, "블루투스를 켜주세요!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}