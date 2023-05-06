package sklookie.bowwow

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import sklookie.bowwow.databinding.CompleteBinding
import sklookie.bowwow.databinding.UserinfoBinding


class CompleteActivity : AppCompatActivity() {
    lateinit var binding : CompleteBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CompleteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = Firebase.database
        val myRef = database.getReference("UserInfo")
        var userInfo : UserInfoModel = UserInfoModel()

        //진행상황 100%설정
        var progressbar = binding.progressBar
        progressbar.setProgress(100)


        //3초후 자동 화면 전환
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            val intent = Intent(this@CompleteActivity, MainHomeActivity::class.java)
            startActivity(intent)
        }, 2000)
    }
}