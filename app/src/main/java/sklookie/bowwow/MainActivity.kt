package sklookie.bowwow

import android.content.Intent

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import sklookie.bowwow.community.NavigateActivity


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        

        findViewById<ImageView>(R.id.community_image).setOnClickListener{
            val intent = Intent(this, NavigateActivity::class.java)
            startActivity(intent)
        }
    }
}