package sklookie.bowwow.community

import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import sklookie.bowwow.R
import sklookie.bowwow.dao.CommunityDAO

class AddActivity : AppCompatActivity() {
    val dao = CommunityDAO()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)
    }

    //선택된 메뉴 처리하기
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add, menu)
        return super.onCreateOptionsMenu(menu)
    }

    //intent 활용한 이벤트 처리
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId){
        R.id.menu_add_save -> {
            val title = findViewById<TextView>(R.id.title_edit_text)
            val content = findViewById<TextView>(R.id.content_edit_text)

            intent.putExtra("title", title.text.toString())
            intent.putExtra("content", content.text.toString())

            setResult(Activity.RESULT_OK, intent)

            dao.addPost(title.text.toString(), content.text.toString())

            finish()

            true
        }
        else -> true
    }
}