package sklookie.bowwow.community

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import sklookie.bowwow.R
import sklookie.bowwow.dao.CommunityDAO
import java.io.ByteArrayOutputStream

class AddActivity : AppCompatActivity() {
    val TAG = "AddActivity"

    val dao = CommunityDAO()

    lateinit var imageView: ImageView

    var imageUrl: String? = ""
    var imageString: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        imageView = findViewById<ImageView>(R.id.add_image_view)

        imageView.setOnClickListener {
            val imageMenu = PopupMenu(applicationContext, it)

            menuInflater?.inflate(R.menu.menu_add_image, imageMenu.menu)
            imageMenu.show()
            imageMenu.setOnMenuItemClickListener {
                when(it.itemId) {
                    R.id.image_edit_change -> {
                        ActivityCompat.requestPermissions(this@AddActivity, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)

                        if (ContextCompat.checkSelfPermission(this@AddActivity.applicationContext, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                            val intent = Intent(Intent.ACTION_PICK)
                            intent.type = "image/*"

                            activityResult.launch(intent)
                        } else {
                            Toast.makeText(this@AddActivity, "갤러리 접근 권한이 거부돼 있습니다. 설정에서 접근을 허용해 주세요.", Toast.LENGTH_SHORT).show()
                        }

                        true
                    }
                    R.id.image_edit_delete -> {
                        if (imageUrl.equals("")) {
                            Toast.makeText(applicationContext, "선택된 이미지가 없습니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            imageUrl = ""
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

    //선택된 메뉴 처리하기
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_post, menu)
        return super.onCreateOptionsMenu(menu)
    }

    //intent 활용한 이벤트 처리
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId){
        R.id.menu_add_save -> {
            val title = findViewById<TextView>(R.id.title_edit_text)
            val content = findViewById<TextView>(R.id.content_edit_text)

            if (!imageUrl.isNullOrBlank()) {
                imageString = bitmapToString(imageView.drawable.toBitmap())
            }

            intent.putExtra("title", title.text.toString())
            intent.putExtra("content", content.text.toString())

            setResult(Activity.RESULT_OK, intent)

            dao.addPost(title.text.toString(), content.text.toString(), imageString)

            finish()

            true
        }
        else -> true
    }

    fun bitmapToString(bitmap: Bitmap?): String {
        val stream = ByteArrayOutputStream()
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }

        val bytes = stream.toByteArray()

        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}