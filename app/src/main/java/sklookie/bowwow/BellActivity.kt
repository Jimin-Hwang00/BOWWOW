package sklookie.bowwow

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import sklookie.bowwow.databinding.BellBinding
import sklookie.bowwow.databinding.UserinfoBinding


class BellActivity : AppCompatActivity() {
    lateinit var binding : BellBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BellBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = Firebase.database
        val myRef = database.getReference("userInfo")
        var userInfo : UserInfoModel = UserInfoModel()
        val id : String = intent.getStringExtra("id").toString()

        //진행상황 30%설정
        var progressbar = binding.progressBar
        progressbar.setProgress(80)

        var spinner = binding.spinner
        spinner.adapter = ArrayAdapter.createFromResource(this, R.array.bellList, android.R.layout.simple_spinner_item)

        //완료버튼 : 모든 유저정보값이 들어간 경우 가능
        binding.nextBtn.setOnClickListener{
            val intent = Intent(this, CompleteActivity::class.java)
            val pref : SharedPreferences = getSharedPreferences("save_state", 0)
            val editor : SharedPreferences.Editor = pref.edit()
            userInfo.bell = spinner.selectedItem.toString()

            editor.putString("bellValue", userInfo.bell).commit()
            myRef.child(id).child("bell").setValue(userInfo.bell)
            intent.putExtra("id", id)
            startActivity(intent)
        }
        binding.backBtn.setOnClickListener{
            val intent = Intent(this, DogInfoActivity::class.java)
            myRef.child("bell").setValue(null)
            startActivity(intent)
        }
    }
}