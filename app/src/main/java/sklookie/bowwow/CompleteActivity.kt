package sklookie.bowwow

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import android.widget.Toast
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

        val id : String = intent.getStringExtra("id").toString()
        val pref : SharedPreferences = getSharedPreferences("save_state", 0)
        val editor : SharedPreferences.Editor = pref.edit()
        editor.putString("idValue", id)
        editor.commit()

        //진행상황 100%설정
        var progressbar = binding.progressBar
        progressbar.setProgress(100)


        //3초후 자동 화면 전환
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            val intent = Intent(this@CompleteActivity, MainHomeActivity::class.java)
            intent.putExtra("id", id)
            startActivity(intent)
        }, 2000)
    }
}