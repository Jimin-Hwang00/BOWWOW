package sklookie.bowwow

import android.content.Intent
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
        val myRef = database.getReference("userInfo").push()
        var userInfo : UserInfoModel = UserInfoModel()

        //진행상황 30%설정
        var progressbar = binding.progressBar
        progressbar.setProgress(10)

        var spinner = binding.spinner
        spinner.adapter = ArrayAdapter.createFromResource(this, R.array.itemList, android.R.layout.simple_spinner_item)

        //유저이름, 장치정보 저장
        binding.nextBtn.setOnClickListener{
            val intent = Intent(this, DogInfoActivity::class.java)
            userInfo.userName = binding.userName.text.toString()
            userInfo.userDevice = spinner.selectedItem.toString()
            myRef.child("id").setValue(myRef.key)
            myRef.child("userDevice").setValue(userInfo.userDevice)
            myRef.child("userName").setValue(userInfo.userName)
            intent.putExtra("id", myRef.key)
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