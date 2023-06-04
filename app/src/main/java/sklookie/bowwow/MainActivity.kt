package sklookie.bowwow

import android.content.Intent

import android.view.View
import sklookie.bowwow.community.CommunityActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        

        findViewById<ImageView>(R.id.community_image).setOnClickListener{
            val intent = Intent(this, CommunityActivity::class.java)
            startActivity(intent)
        }
    }
}