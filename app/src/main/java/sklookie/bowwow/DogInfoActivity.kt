package sklookie.bowwow

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import sklookie.bowwow.databinding.DoginfoBinding

class DogInfoActivity : AppCompatActivity() {
    lateinit var binding : DoginfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DoginfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var progressbar = binding.progressBar
        progressbar.setProgress(50)

        binding.nextBtn.setOnClickListener{
            val intent = Intent(this, BellActivity::class.java)
            startActivity(intent)
        }
        binding.backBtn.setOnClickListener{
            val intent = Intent(this, UserInfoActivity::class.java)
            startActivity(intent)
        }
    }
}