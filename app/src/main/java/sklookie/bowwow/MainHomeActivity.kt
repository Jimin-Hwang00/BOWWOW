package sklookie.bowwow

import android.content.Intent
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
import sklookie.bowwow.community.CommunityActivity
import sklookie.bowwow.databinding.MainHomeBinding

class MainHomeActivity : AppCompatActivity() {
    private lateinit var binding: MainHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = Firebase.database
        val myRef = database.getReference("userInfo")
        val id : String = intent.getStringExtra("id").toString()

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot?.child(id)?.child("userName")
                val dogValue = snapshot?.child(id)?.child("dogName")


                binding.userName.setText("${value?.value}님")
                binding.bellText.setText("${dogValue?.value}이의 \n벨훈련")
            }

            override fun onCancelled(error: DatabaseError) {
                println("Failed to read value.")
            }
        })

        binding.btmMenu.setOnNavigationItemSelectedListener  { menuItem ->
            when (menuItem.itemId) {
                R.id.menu1 -> {
                    navigateToActivity(MainHomeActivity::class.java)
                    true
                }
                R.id.menu4 -> {
                    navigateToActivity(CommunityActivity::class.java)
                    true
                }
                else -> false
            }
        }
    }

//    현재 하단바 메뉴 아이템 클릭 시 Activity 전환이 이뤄짐. @TODO 프래그먼트 전환
    private fun navigateToActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        finish()
        startActivity(intent)
    }

}
