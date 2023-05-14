package sklookie.bowwow.community

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import sklookie.bowwow.R
import sklookie.bowwow.dao.CommunityDAO
import sklookie.bowwow.dto.Post
import java.io.ByteArrayOutputStream

class EditActivity : AppCompatActivity() {

    lateinit var post: Post

    lateinit var titleEditText: TextView
    lateinit var contentEditText: TextView
    lateinit var imageView: ImageView

    val dao = CommunityDAO()

    var imageUrl: String = ""
    var imageString: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        val intent = intent

        post = intent.getSerializableExtra("post") as Post

        titleEditText = findViewById<TextView>(R.id.title_edit_text)
        contentEditText = findViewById<TextView>(R.id.content_edit_text)
        imageView = findViewById<ImageView>(R.id.add_image_view)

        titleEditText.setText(post.title)
        contentEditText.setText(post.content)

        if (!post.image.isNullOrBlank()) {
            imageView.setImageBitmap(StringToBitmap(post.image!!))
            imageString = post.image!!
        }

        imageView.setOnClickListener {
            val imageMenu = PopupMenu(applicationContext, it)

            menuInflater?.inflate(R.menu.menu_edit_image, imageMenu.menu)
            imageMenu.show()
            imageMenu.setOnMenuItemClickListener {
                when(it.itemId) {
                    R.id.image_edit_change -> {
                        ActivityCompat.requestPermissions(this@EditActivity, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)

                        if (ContextCompat.checkSelfPermission(this@EditActivity.applicationContext, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                            val intent = Intent(Intent.ACTION_PICK)
                            intent.type = "image/*"

                            activityResult.launch(intent)
                        } else {
                            Toast.makeText(this@EditActivity, "갤러리 접근 권한이 거부돼 있습니다. 설정에서 접근을 허용해 주세요.", Toast.LENGTH_SHORT).show()
                        }

                        true
                    }
                    R.id.image_edit_delete -> {
                        if (imageString.isNullOrBlank()) {
                            Toast.makeText(applicationContext, "선택된 이미지가 없습니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            imageUrl = ""
                            imageString = ""
                            imageView.setImageResource(R.mipmap.camera_icon)
                            Toast.makeText(applicationContext, "이미지가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                        }
                        true
                    }
                    else -> {
                        true
                    }
                }
            }
        }
    }

    val activityResult: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK && it.data != null) {
            imageUrl = it.data!!.data.toString()

            Glide.with(this)
                .load(imageUrl)
                .into(imageView)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_post, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId){
        R.id.menu_add_save -> {
            val title = titleEditText.text.toString()
            val content = contentEditText.text.toString()

            if (!imageUrl.isNullOrBlank()) {
                imageString = bitmapToString(imageView.drawable.toBitmap())
            }

            dao.editPost(post.pid.toString(), title, content, imageString)       // 수정 메소드 실행.

            finish()

            true
        }
        else -> true
    }

    fun bitmapToString(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }

        val bytes = stream.toByteArray()

        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun StringToBitmap(string: String): Bitmap? {
        try {
            val encodeByte = Base64.decode(string, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)

            return bitmap
        } catch (e: java.lang.Exception) {
            Log.e("StringToBitmap", e.message.toString())
            return null;
        }
    }
}