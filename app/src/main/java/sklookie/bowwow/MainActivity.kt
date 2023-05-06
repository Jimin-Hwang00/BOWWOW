package sklookie.bowwow

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val database = Firebase.database
        val myRef = database.getReference("UserInfo")
        var userInfo : UserInfoModel = UserInfoModel()

        //시작 화면으로 돌아오면, 정보 초기화 작업
        myRef.setValue(null)

        //3초후 자동 화면 전환
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            val intent = Intent(this@MainActivity, UserInfoActivity::class.java)
            startActivity(intent)
        }, 3000)
    }
}