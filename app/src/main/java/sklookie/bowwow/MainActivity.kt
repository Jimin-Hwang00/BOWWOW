package sklookie.bowwow

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import sklookie.bowwow.community.CommunityActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onClick(v: View) {
        when (v.id) {
            R.id.community_image -> {
                val intent = Intent(this, CommunityActivity::class.java)
                startActivity(intent)
            }
        }
    }
}