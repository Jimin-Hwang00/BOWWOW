package sklookie.bowwow

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import sklookie.bowwow.databinding.UserinfoBinding


class UserInfoActivity : AppCompatActivity() {

    lateinit var binding : UserinfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UserinfoBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val database = Firebase.database
        val myRef = database.getReference("userInfo")
        val id : String = intent.getStringExtra("id").toString()
        var userInfo : UserInfoModel = UserInfoModel()

        //진행상황 30%설정
        var progressbar = binding.progressBar
        progressbar.setProgress(10)

        var spinner = binding.spinner
        spinner.adapter = ArrayAdapter.createFromResource(this, R.array.itemList, android.R.layout.simple_spinner_item)

        //유저이름, 장치정보 저장
        binding.nextBtn.setOnClickListener{
            val intent = Intent(this, DogInfoActivity::class.java)
            val pref : SharedPreferences = getSharedPreferences("save_state", 0)
            val editor : SharedPreferences.Editor = pref.edit()
            userInfo.userName = binding.userName.text.toString()
            userInfo.userDevice = spinner.selectedItem.toString()

            editor.putString("nameValue", userInfo.userName).commit()
            myRef.child(id).child("userDevice").setValue(userInfo.userDevice)
            myRef.child(id).child("userName").setValue(userInfo.userName)
            intent.putExtra("id", id)
            startActivity(intent)
        }
        binding.backBtn.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)

            //시작 화면으로 돌아오면, 정보 초기화 작업
            myRef.setValue(null)

            startActivity(intent)
        }
    }
}