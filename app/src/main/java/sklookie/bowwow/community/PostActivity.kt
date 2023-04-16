package sklookie.bowwow.community

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import sklookie.bowwow.R
import sklookie.bowwow.dao.CommunityDAO
import sklookie.bowwow.dto.Post

class PostActivity : AppCompatActivity() {

    lateinit var post: Post
    val dao = CommunityDAO()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        val intent = getIntent()

        post = intent.getSerializableExtra("post") as Post

        findViewById<TextView>(R.id.title_text_view).text = post.title
        findViewById<TextView>(R.id.uid_text_view).text = post.uid
        findViewById<TextView>(R.id.date_text_view).text = post.date
        findViewById<TextView>(R.id.content_text_view).text = post.content
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_extra, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId){
        R.id.menu_edit -> {
            val editIntent = Intent(this, EditActivity::class.java)
            editIntent.putExtra("post", post)
            startActivity(editIntent)

            finish()

            true
        }
        R.id.menu_delete -> {
            dao.deletePost(post.pid.toString())

            finish()

            true
        }
        else -> true
    }
}