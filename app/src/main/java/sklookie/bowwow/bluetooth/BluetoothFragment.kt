package sklookie.bowwow.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.registerReceiver
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import sklookie.bowwow.BluetoothDao
import sklookie.bowwow.R
import sklookie.bowwow.databinding.FragmentBluetoothBinding
import sklookie.bowwow.databinding.FragmentMainHomeBinding

//권한추가
const val REQUEST_ALL_PERMISSION = 1
val PERMISSIONS = arrayOf(
    android.Manifest.permission.BLUETOOTH,
    android.Manifest.permission.BLUETOOTH_SCAN,
    android.Manifest.permission.BLUETOOTH_ADVERTISE,
    android.Manifest.permission.BLUETOOTH_CONNECT,
    android.Manifest.permission.ACCESS_FINE_LOCATION
)
class BluetoothFragment : Fragment() {
    lateinit var binding : FragmentBluetoothBinding
    private val bluetoothManager: BluetoothManager by lazy {
        requireActivity().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }
    private val mBluetoothAdapter: BluetoothAdapter? by lazy {
        bluetoothManager.adapter
    }
    var myDevices = mutableMapOf<String?, String?>()
    var devices = mutableMapOf<String?, String?>()
    //장치 검색 때 필요한 변수
    private lateinit var broadcastReceiver: BroadcastReceiver
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = arguments

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBluetoothBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val devices = BluetoothDao().devices
        val adapter = DeviceAdapter(devices)

        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.orientation = LinearLayoutManager.VERTICAL

        //블루투스 권한 묻기
        if (!hasPermissions(requireContext(), sklookie.bowwow.bluetooth.PERMISSIONS)) {
            requestPermissions(sklookie.bowwow.bluetooth.PERMISSIONS, sklookie.bowwow.bluetooth.REQUEST_ALL_PERMISSION)
        }

        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter

        //원하는 장치 클릭시, 연결 시작
        val listener = object : DeviceAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                if (devices[position].isConnect.equals("연결됨")) {
                    devices[position].isConnect = "연결안됨"

                    Toast.makeText(
                        requireContext(),
                        "${devices[position].deviceName}연결 취소",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    devices[position].isConnect = "연결됨"

                    Toast.makeText(
                        requireContext(),
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
            override fun onReceive(p0: Context?, p1: Intent?) {
                when (p1?.action) {
                    //찾은 기기가 있으면, bluetoothDao에 저장, broadcastReceiver에 기기 저장
                    BluetoothDevice.ACTION_FOUND -> {
                        val device =
                            p1.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

                        Log.d(TAG, "들어옴")
                        //임시방편 : device?.name으로 가져와야하는데.. permission 오류 발생
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                            if (ActivityCompat.checkSelfPermission(
                                    requireContext(),
                                    Manifest.permission.BLUETOOTH_CONNECT
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
                            }else {
                                val deviceName = device?.name
                                Log.d(TAG, device?.name.toString())
                                val deviceHardwareAddress = device?.address
                                Log.d(TAG, deviceHardwareAddress.toString())

                                if (deviceName != null && deviceHardwareAddress != null) {
                                    devices.add(BluetoothDto(deviceName, deviceHardwareAddress, "연결안됨"))
                                }
                            }
                        }else{
                            //안드로이드 버전마다 체크해야할 권한이 달라서..페어링기기도 이 작업필요
                            if (ActivityCompat.checkSelfPermission(
                                    requireContext(),
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) != PackageManager.PERMISSION_GRANTED){
                                return
                            }else{
                                val deviceName = device?.name
                                Log.d(TAG, device?.name.toString())
                                val deviceHardwareAddress = device?.address
                                Log.d(TAG, deviceHardwareAddress.toString())

                                if (deviceName != null && deviceHardwareAddress != null) {
                                    devices.add(BluetoothDto(deviceName, deviceHardwareAddress, "연결안됨"))
                                    adapter.notifyDataSetChanged()
                                }
                            }
                        }
//                        val deviceHardwareAddress = device?.address
//                        Log.d(TAG, deviceHardwareAddress.toString())
//                        if (deviceName != null && deviceHardwareAddress != null) {
//                            devices.add(bluetoothDto(deviceName, deviceHardwareAddress, "연결안됨"))
//                        }
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED ->{
                        adapter.notifyDataSetChanged()
                    }
                    else -> {
                        Log.d(TAG, "저장된 기기 없음")
                        Toast.makeText(
                            requireContext(),
                            "블루투스 기기가 없습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

        }
        // BroadcastReceiver 등록
        var filter: IntentFilter = IntentFilter(BluetoothDevice.ACTION_FOUND);
        requireContext().registerReceiver(broadcastReceiver, filter)

        adapter.notifyDataSetChanged()
        //검색된 장치 정보 테스트
//        Toast.makeText(
//            this@MainActivity, devices.toString(), Toast.LENGTH_SHORT
//        ).show()

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
                                requireContext(),
                                android.Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            return
                        }else{
                            Log.d(TAG, "기기 저장함")
                            myDevices.put(device.name, device.address)
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "페어링된 기기가 없습니다.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "블루투스가 비활성화 되어 있습니다.", Toast.LENGTH_SHORT).show()
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
                            requireContext(),
                            android.Manifest.permission.BLUETOOTH_SCAN
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
                    Toast.makeText(requireContext(), "기기검색이 중단되었습니다.", Toast.LENGTH_SHORT).show()
                    return
                }
                // ArrayAdapter clear
//                    devices.clear()
                // 검색시작
                it.startDiscovery()
                Toast.makeText(requireContext(), "기기 검색을 시작하였습니다", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "블루투스가 비활성화되어 있습니다", Toast.LENGTH_SHORT).show()
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
            sklookie.bowwow.bluetooth.REQUEST_ALL_PERMISSION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(requireContext(), "블루투스가 켜졌습니다!", Toast.LENGTH_SHORT).show()
                    deviceDiscovering()
                    Log.d(TAG, "getPairedDevices")
                    getPairedDevices()
                } else {
                    requestPermissions(permissions, sklookie.bowwow.bluetooth.REQUEST_ALL_PERMISSION)
                    Toast.makeText(requireContext(), "블루투스를 켜주세요!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}