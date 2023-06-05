package sklookie.bowwow.community

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
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
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.*
import sklookie.bowwow.R
import sklookie.bowwow.dao.CommunityDAO
import sklookie.bowwow.databinding.ActivityAddBinding
import sklookie.bowwow.dto.Comment
import sklookie.bowwow.dto.Post
import java.io.ByteArrayOutputStream

class EditActivity : AppCompatActivity() {
    val TAG = "EditActivity"

    private lateinit var binding: ActivityAddBinding

    var post: Post? = Post()
    lateinit var intentPid: String

    val dao = CommunityDAO()

    var imageUrl: String = ""
    var imageString: String = ""

    val db: FirebaseDatabase = FirebaseDatabase.getInstance()
    lateinit var postReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        intentPid = intent.getStringExtra("pid").toString()

        Log.d(TAG, "intentPid : ${intentPid}")

        val asyncTask = object : AsyncTask<String, Void, Post?>() {
            override fun doInBackground(vararg p0: String?): Post? {
                postReference = db.getReference("post/${intentPid}")

                val result: Post? = Post()
                val titleTask = postReference.child("title").get()
                val imageTask = postReference.child("image").get()
                val contentTask = postReference.child("content").get()

                try {
                    result?.pid = intentPid

                    val titleSnapshot = Tasks.await(titleTask)
                    result?.title = titleSnapshot.value.toString()

                    val imageSnapshot = Tasks.await(imageTask)
                    result?.image = imageSnapshot.value.toString()

                    val contentSnapshot = Tasks.await(contentTask)
                    result?.content = contentSnapshot.value.toString()

                    return result
                } catch (e: Exception) {
                    Log.e(TAG, "데이터 가져오기 오류: ${e.message}")
                    return null
                }
            }

            override fun onPostExecute(result: Post?) {
                super.onPostExecute(result)
                post = result

                setView()
            }
        }

        asyncTask.execute(intentPid)

        binding.addImageView.setOnClickListener {
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_post, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId){
        R.id.menu_add_save -> {
            val title = binding.titleEditText.text.toString()
            val content = binding.contentEditText.text.toString()

            if (!imageUrl.isNullOrBlank()) {
                imageString = bitmapToString(binding.addImageView.drawable.toBitmap())
            }

            dao.editPost(post?.pid.toString(), title, content, imageString)       // 수정 메소드 실행.

            finish()

            true
        }
        else -> true
    }

//    bitmap을 String으로 변경 (서버에 저장하기 위함)
    fun bitmapToString(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }

        val bytes = stream.toByteArray()

        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

//    String을 Bitmap으로 변경 (서버에 저장된 이미지 String을 이미지 뷰에 띄우기 위함)
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

//    화면 view 설정
    fun setView() {
        binding.titleEditText.setText(post?.title)
        binding.contentEditText.setText(post?.content)

        if (!post?.image.isNullOrBlank()) {
            binding.addImageView.setImageBitmap(StringToBitmap(post?.image!!))
            imageString = post?.image!!
        }
    }
}