package sklookie.bowwow

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import sklookie.bowwow.databinding.BellBinding
import sklookie.bowwow.databinding.UserinfoBinding


class BellActivity : AppCompatActivity() {


    lateinit var binding : BellBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BellBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //진행상황 30%설정
        var progressbar = binding.progressBar
        progressbar.setProgress(80)

        var spinner = binding.spinner
        spinner.adapter = ArrayAdapter.createFromResource(this, R.array.bellList, android.R.layout.simple_spinner_item)

        binding.nextBtn.setOnClickListener{
            val intent = Intent(this, CompleteActivity::class.java)
            startActivity(intent)
        }
        binding.backBtn.setOnClickListener{
            val intent = Intent(this, DogInfoActivity::class.java)
            startActivity(intent)
        }
    }
}