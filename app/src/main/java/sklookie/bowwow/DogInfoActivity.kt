package sklookie.bowwow

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import sklookie.bowwow.databinding.DoginfoBinding

class DogInfoActivity : AppCompatActivity() {
    lateinit var binding : DoginfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DoginfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = Firebase.database
        val myRef = database.getReference("userInfo")
        var userInfo : UserInfoModel = UserInfoModel()
        val id : String = intent.getStringExtra("id").toString()

        //진행상황: 50%
        var progressbar = binding.progressBar
        progressbar.setProgress(50)


        //다음버튼 클릭시, 반려견 이름 저장
        binding.nextBtn.setOnClickListener{
            val intent = Intent(this, BellActivity::class.java)
            userInfo.dogName = binding.dogName.text.toString()
            myRef.child(id).child("dogName").setValue(userInfo.dogName)
            intent.putExtra("id", id)
            startActivity(intent)
        }
        binding.backBtn.setOnClickListener{
            val intent = Intent(this, UserInfoActivity::class.java)
            myRef.child("dogName").setValue(null)
            startActivity(intent)
        }
    }
}