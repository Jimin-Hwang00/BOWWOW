package sklookie.bowwow

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import sklookie.bowwow.databinding.ActivityMyInfoBinding
import sklookie.bowwow.databinding.ActivityMyInfoUpdateBinding

class MyInfoUpdateActivity : AppCompatActivity() {
    lateinit var binding : ActivityMyInfoUpdateBinding
    val database = Firebase.database
    val myRef = database.getReference("userInfo")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyInfoUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pref = getSharedPreferences("save_state", 0)
        val uName = pref.getString("nameValue", null).toString()
        val device = pref.getString("deviceValue", null).toString()
        val bell = pref.getString("bellValue", null).toString()
        val dogName = pref.getString("dogValue", null).toString()

        binding.uNameText.setText("${uName}님의 정보")
        binding.updUName.setText("${uName}")
        binding.updDogName.setText("${dogName}")
        binding.deviceInfoText.setText("설정된 장치: ${device}")
        binding.bellInfoText.setText("설정된 벨: ${bell}")

        var deviceSpinner = binding.deviceSpinner
        deviceSpinner.adapter = ArrayAdapter.createFromResource(this, R.array.itemList, android.R.layout.simple_spinner_item)

        var bellSpinner = binding.bellSpinner
        bellSpinner.adapter = ArrayAdapter.createFromResource(this, R.array.bellList, android.R.layout.simple_spinner_item)

        binding.SaveBt.setOnClickListener{
            val intent = Intent(this@MyInfoUpdateActivity, MyInfoActivity::class.java)
            var updatedBellInfo = bellSpinner.selectedItem.toString()
            var updatedDeviceInfo = deviceSpinner.selectedItem.toString()

            val pref : SharedPreferences = getSharedPreferences("save_state", 0)
            val editor : SharedPreferences.Editor = pref.edit()
            val id = pref.getString("idValue", null).toString()

            editor.putString("nameValue", binding.updUName.text.toString())
            editor.putString("deviceValue", updatedDeviceInfo)
            editor.putString("dogValue", binding.updDogName.text.toString())
            editor.putString("bellValue", updatedBellInfo)
            editor.commit()

            myRef.child(id).child("userName").setValue(binding.updUName.text.toString())
            myRef.child(id).child("userDevice").setValue(updatedDeviceInfo)
            myRef.child(id).child("dogName").setValue(binding.updDogName.text.toString())
            myRef.child(id).child("bell").setValue(updatedBellInfo)

            Toast.makeText(this, "변경 완료됐습니다." , Toast.LENGTH_SHORT).show()
            startActivity(intent)
        }

        binding.cancelBt.setOnClickListener{
            AlertDialog.Builder(this).run {
                setTitle("변경 취소")
                setIcon(R.drawable.warning)
                setMessage("정말로 변경을 취소하겠습니까?")
                setNegativeButton("취소", null)
                setCancelable(false)
                setPositiveButton("확인", object: DialogInterface.OnClickListener{
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        val intent = Intent(this@MyInfoUpdateActivity, MyInfoActivity::class.java)
                        startActivity(intent)
                    }
                })
                show()
            }
        }
    }
}