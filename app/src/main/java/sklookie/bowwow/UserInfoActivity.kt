package sklookie.bowwow

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
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
        val myRef = database.getReference("UserInfo")
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
            myRef.child("userDevice").setValue(userInfo.userDevice)
            myRef.child("userName").setValue(userInfo.userName)
            startActivity(intent)
        }
        binding.backBtn.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}