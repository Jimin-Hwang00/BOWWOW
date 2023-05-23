package sklookie.bowwow

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import sklookie.bowwow.community.CommunityActivity
import sklookie.bowwow.databinding.MainHomeBinding

class MainHomeActivity : AppCompatActivity() {
    lateinit var binding : MainHomeBinding

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

        binding.btmMenu.setOnNavigationItemReselectedListener {menuItem ->
            BottomNavigate(menuItem.itemId)
        }
    }

    fun BottomNavigate(id : Int) {
        val tag = id.toString()
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val currentFragment = fragmentManager.primaryNavigationFragment
        if (currentFragment != null) {
            fragmentTransaction.hide(currentFragment)
        }

        var fragment = fragmentManager.findFragmentByTag(tag)
        if (fragment == null) {
            if (id == R.id.menu1) {
                val intent = Intent(this, MainHomeActivity::class.java)
                startActivity(intent)
            }
            if (id == R.id.menu4) {
                val intent = Intent(this, CommunityActivity::class.java)
                startActivity(intent)
            }
        }
    }
}