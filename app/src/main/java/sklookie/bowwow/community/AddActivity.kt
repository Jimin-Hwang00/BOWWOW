package sklookie.bowwow.community

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import sklookie.bowwow.R
import sklookie.bowwow.dao.CommunityDAO
import sklookie.bowwow.databinding.ActivityAddBinding
import java.io.ByteArrayOutputStream

class AddActivity : AppCompatActivity() {
    val TAG = "AddActivity"

    private lateinit var binding: ActivityAddBinding
    val dao = CommunityDAO()

    lateinit var imageView: ImageView

    var imageUrl: String? = ""
    var imageString: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.addImageView.setOnClickListener {
            val imageMenu = PopupMenu(applicationContext, it)

//            이미지 누를 시 메뉴 생성
            menuInflater?.inflate(R.menu.menu_add_image, imageMenu.menu)
            imageMenu.show()
            imageMenu.setOnMenuItemClickListener {
                when(it.itemId) {
                    R.id.image_edit_change -> {     // 이미지 추가
                        ActivityCompat.requestPermissions(this@AddActivity, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)      // 퍼미션 요구 (1회만)

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
                        if (imageUrl.equals("")) {      // 선택된 이미지가 없는 상태에서 이미지 삭제 버튼 누름
                            Toast.makeText(applicationContext, "선택된 이미지가 없습니다.", Toast.LENGTH_SHORT).show()
                        } else {                               // 이미지 삭제
                            imageUrl = ""
                            binding.addImageView.setImageResource(R.mipmap.camera_icon)
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

//    이미지 선택 완료 시 Glide를 이용하여 이미지 띄우기
    val activityResult: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK && it.data != null) {
            imageUrl = it.data!!.data.toString()

            Glide.with(this)
                .load(imageUrl)
                .into(binding.addImageView)
        }
    }

//    옵션 메뉴 추가
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_post, menu)
        return super.onCreateOptionsMenu(menu)
    }

//    옵션 메뉴 - 게시글 저장 기능
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId) {
        R.id.menu_add_save -> {
            if (binding.titleEditText.text.isNullOrEmpty()) {       // 제목이 입력되지 않았을 경우 Toast 띄움
                Toast.makeText(AddActivity@ this, "제목을 입력하세요.", Toast.LENGTH_SHORT).show()
            } else {
                if (!imageUrl.isNullOrBlank()) {                    // 선택된 이미지가 있는 경우
                    Log.d(TAG, "imageUrl : ${imageUrl}")
                    imageString = bitmapToString(binding.addImageView.drawable.toBitmap())
                } else {                                            // 선택된 이미지가 없는 경우
                    Log.d(TAG, "imageUrl : 없음")
                }

                intent.putExtra("title", binding.titleEditText.text.toString())
                intent.putExtra("content", binding.contentEditText.text.toString())

                setResult(Activity.RESULT_OK, intent)

                dao.addPost(
                    binding.titleEditText.text.toString(),
                    binding.contentEditText.text.toString(),
                    imageString
                )

                finish()
            }
            true
        }
        else -> true
    }

//    bitmap을 String으로 변경 (서버에 저장하기 위함)
    fun bitmapToString(bitmap: Bitmap?): String {
        val stream = ByteArrayOutputStream()
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }

        val bytes = stream.toByteArray()

        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}