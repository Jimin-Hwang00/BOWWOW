package sklookie.bowwow

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import sklookie.bowwow.databinding.ActivityMyInfoBinding

class MyInfoActivity : AppCompatActivity() {
    lateinit var MyInfoBinding : ActivityMyInfoBinding
    val database = Firebase.database
    val myRef = database.getReference("userInfo")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyInfoBinding = ActivityMyInfoBinding.inflate(layoutInflater)
        setContentView(MyInfoBinding.root)

        val pref = getSharedPreferences("save_state", 0)
        val uName = pref.getString("nameValue", null).toString()
        val device = pref.getString("deviceValue", null).toString()
        val bell = pref.getString("bellValue", null).toString()
        val dogName = pref.getString("dogValue", null).toString()

        MyInfoBinding.uNameText.setText("${uName}님의 정보")
        MyInfoBinding.bellInfoText.setText("설정된 벨 : ${bell}")
        MyInfoBinding.deviceInfoText.setText("설정된 장치: ${device}")
        MyInfoBinding.uNameInfoText.setText("반려인 이름: ${uName}")
        MyInfoBinding.dogNameInfoText.setText("반려견 이름: ${dogName}")

        MyInfoBinding.bluetoothButton.setOnClickListener{
            val intent = Intent(this@MyInfoActivity, BluetoothMainActivity::class.java)
            startActivity(intent)
        }

        MyInfoBinding.bellSaveBt.setOnClickListener{
            val intent = Intent(this@MyInfoActivity, MyInfoUpdateActivity::class.java)
            startActivity(intent)
        }

        MyInfoBinding.resetBt.setOnClickListener{
            AlertDialog.Builder(this).run {
                setTitle("계정 삭제")
                setIcon(R.drawable.warning)
                setMessage("정말로 계정을 삭제하시겠습니까?")
                setNegativeButton("취소", null)
                setCancelable(false)
                setPositiveButton("확인", object: DialogInterface.OnClickListener{
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        onDeleteAccount()
                    }
                })
                show()
            }
        }
    }
    fun onDeleteAccount() {
        val pref : SharedPreferences = getSharedPreferences("save_state", 0)
        val id = pref.getString("idValue", null).toString()

        myRef.child(id).removeValue()
            .addOnSuccessListener(object : OnSuccessListener<Void?>{
                override fun onSuccess(p0: Void?) {
                    val intent = Intent(this@MyInfoActivity, MainActivity::class.java)
                    val pref : SharedPreferences = getSharedPreferences("save_state", 0)
                    val editor : SharedPreferences.Editor = pref.edit()
                    editor.remove("nameValue")
                    editor.remove("bellValue")
                    editor.remove("idValue")
                    editor.remove("dogValue")
                    editor.commit()

                    Toast.makeText(this@MyInfoActivity, "계정 삭제가 완료됐습니다.", Toast.LENGTH_SHORT).show()
                    startActivity(intent)
                }
            }).addOnFailureListener(object : OnFailureListener {
                override fun onFailure(p0: java.lang.Exception) {
                    Toast.makeText(this@MyInfoActivity, "계정 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            })
    }
}