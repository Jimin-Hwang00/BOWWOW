package sklookie.bowwow

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

        val database = Firebase.database
        val myRef = database.getReference("UserInfo")

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot?.child("userName")
                binding.userName.setText("${value?.value}님")
            }

            override fun onCancelled(error: DatabaseError) {
                println("Failed to read value.")
            }
        })
    }
}