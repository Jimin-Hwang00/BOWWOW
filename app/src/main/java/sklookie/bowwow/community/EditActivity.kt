package sklookie.bowwow.community

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import sklookie.bowwow.R
import sklookie.bowwow.dao.CommunityDAO
import sklookie.bowwow.dto.Post

class EditActivity : AppCompatActivity() {

    lateinit var post: Post

    lateinit var titleEditText: TextView
    lateinit var contentEditText: TextView

    val dao = CommunityDAO()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        val intent = intent

        post = intent.getSerializableExtra("post") as Post

        titleEditText = findViewById<TextView>(R.id.title_edit_text)
        contentEditText = findViewById<TextView>(R.id.content_edit_text)

        titleEditText.setText(post.title)
        contentEditText.setText(post.content)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId){
        R.id.menu_add_save -> {
            val title = titleEditText.text.toString()
            val content = contentEditText.text.toString()

            dao.editPost(post.pid.toString(), title, content)       // 수정 메소드 실행.

            finish()

            true
        }
        else -> true
    }
}