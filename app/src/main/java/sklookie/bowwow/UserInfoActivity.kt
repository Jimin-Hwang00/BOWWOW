package sklookie.bowwow

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import sklookie.bowwow.databinding.UserinfoBinding


class UserInfoActivity : AppCompatActivity() {

    lateinit var binding : UserinfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UserinfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //진행상황 30%설정
        var progressbar = binding.progressBar
        progressbar.setProgress(30)

        var spinner = binding.spinner
        spinner.adapter = ArrayAdapter.createFromResource(this, R.array.itemList, android.R.layout.simple_spinner_item)

    }
}