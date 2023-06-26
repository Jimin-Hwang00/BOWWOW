package sklookie.bowwow.community

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    var imageUris = mutableListOf<Uri>()
    var imageStrings = mutableListOf<String>()

    lateinit var imageAdapter: MultiImageAdapter
    lateinit var imageRecyclerView: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initImageRecycler()

        imageAdapter.setOnItemDeleteListener(object: MultiImageAdapter.OnImageDeleteListener {
            override fun onImageDeleted(position: Int) {
                imageUris = imageAdapter.datas
            }
        })

        binding.addImageBtn.setOnClickListener {
            ActivityCompat.requestPermissions(this@AddActivity, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)      // 퍼미션 요구 (1회만)

            if (ContextCompat.checkSelfPermission(this@AddActivity.applicationContext, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(Intent.ACTION_PICK)
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE)
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, 2222)
            } else {
                Toast.makeText(this@AddActivity, "갤러리 접근 권한이 거부돼 있습니다. 설정에서 접근을 허용해 주세요.", Toast.LENGTH_SHORT).show()
            }

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

                if (!imageUris.isNullOrEmpty()) {
                    for (imageUri in imageUris) {
                        imageStrings.add(bitmapToString(MediaStore.Images.Media.getBitmap(contentResolver, imageUri)))
                    }
                }

                for (i in 0 until  imageStrings.size) {
                    Log.d(TAG, "imageStrings: ${imageStrings.get(i)}")
                }

                dao.addPost(
                    binding.titleEditText.text.toString(),
                    binding.contentEditText.text.toString(),
                    imageUris,
                    object: CommunityDAO.AddPostCallback {
                        override fun onAddPostCompleted() {
                            finish()
                        }

                    }
                )
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

    fun initImageRecycler() {
        imageAdapter = MultiImageAdapter(this)
        imageRecyclerView = binding.addImageRecycler

        imageRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        imageRecyclerView.adapter = imageAdapter

        imageAdapter.datas = imageUris

        imageAdapter.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data == null) {
            Toast.makeText(this@AddActivity, "이미지를 선택하지 않았습니다.", Toast.LENGTH_SHORT).show()
        } else {
            if (data.getClipData() == null) {
                val imageUri = data.data
                imageUri?.let { imageUris.add(it) }
            } else {
                val clipData = data.clipData

                if (clipData!!.itemCount + imageUris.size > 3) {
                    Toast.makeText(applicationContext, "사진은 최대 3장까지 선택 가능합니다.", Toast.LENGTH_SHORT).show()
                } else {
                    for (i in 0 until clipData.itemCount) {
                        val imageUri = clipData.getItemAt(i).uri

                        try {
                            imageUris.add(imageUri)
                        } catch (e: Exception) {
                            Log.e(TAG, "File select error", e)
                        }
                    }
                }
            }

            imageAdapter.datas = imageUris
            imageAdapter.notifyDataSetChanged()

            for (imageUri in imageAdapter.datas) {
                Log.d(TAG, "imageURI : ${imageUri}")
            }
        }
    }
}