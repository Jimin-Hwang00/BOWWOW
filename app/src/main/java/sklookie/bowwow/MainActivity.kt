package sklookie.bowwow

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pref : SharedPreferences = getSharedPreferences("save_state", 0)

        if(pref.getString("bellValue", null).equals(null)){
            //3초후 자동 화면 전환
            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
            }, 3000)
        }else{
            val intent = Intent(this@MainActivity, MainHomeActivity::class.java)
            startActivity(intent)
        }

    }
}