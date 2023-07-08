package sklookie.bowwow

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import sklookie.bowwow.databinding.MainHomeBinding


class MainHomeActivity : AppCompatActivity() {
    lateinit var binding : MainHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id : String = intent.getStringExtra("id").toString()
        val pref : SharedPreferences = getSharedPreferences("save_state", 0)

        binding.userName.setText("${pref.getString("nameValue", null)}님")
        binding.bellText.setText("${pref.getString("dogValue", null)}이의 \n벨훈련")

        binding.profile.setOnClickListener{
            val intent = Intent(this, MyInfoActivity::class.java)
            intent.putExtra("id", id)
            startActivity(intent)
        }
    }
}