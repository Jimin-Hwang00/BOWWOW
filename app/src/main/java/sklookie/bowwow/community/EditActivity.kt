package sklookie.bowwow.community

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import sklookie.bowwow.R
import sklookie.bowwow.dao.CommunityDAO
import sklookie.bowwow.databinding.ActivityAddBinding
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

    lateinit var adapterImages : MutableList<Uri>
    var newImages = mutableListOf<Uri>()

    lateinit var imageAdapter: MultiImageAdapter
    lateinit var imageRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        intentPid = intent.getStringExtra("pid").toString()

        Log.d(TAG, "intentPid : ${intentPid}")

        binding.addImageBtn.setOnClickListener {
            ActivityCompat.requestPermissions(this@EditActivity, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)      // 퍼미션 요구 (1회만)

            if (ContextCompat.checkSelfPermission(this@EditActivity.applicationContext, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(Intent.ACTION_PICK)
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE)
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, 2222)
            } else {
                Toast.makeText(this@EditActivity, "갤러리 접근 권한이 거부돼 있습니다. 설정에서 접근을 허용해 주세요.", Toast.LENGTH_SHORT).show()
            }

        }

        dao.getPostById(intentPid) { post ->
            if (post != null) {
                this.post = post

                val imageTasks = ArrayList<Task<Uri>>() // 이미지 다운로드 Task들을 저장하기 위한 리스트

//                 이미지 내용 images 변수에 넣기 (댓글 리사이클러뷰에 사용하기 위함)
                if (post?.images != null) {
                    val firebaseStorage = FirebaseStorage.getInstance()
                    val rootRef = firebaseStorage.reference

                    adapterImages = MutableList(post?.images!!.size) {Uri.EMPTY}

                    post?.images!!.forEachIndexed { index, image ->
                        val imgRef = rootRef.child("post/$image")
                        postImagesUris = MutableList(post?.images!!.size) { Uri.EMPTY}
                        if (imgRef != null) {
                            val imageUrlTask = imgRef.downloadUrl.addOnSuccessListener { uri ->
                                adapterImages[index] = uri
                                postImagesUris[index] = uri
                            }
                            imageTasks.add(imageUrlTask)    // 이미지 다운로드 Task를 리스트에 추가
                        }
                    }

//                     모든 이미지 다운로드 Task가 완료되었을 때 initImageRecycler() 함수 호출
                    Tasks.whenAllSuccess<Uri>(imageTasks)
                        .addOnSuccessListener {
                            initImageRecycler()
                        }
                        .addOnFailureListener { exception ->
                            // 이미지 다운로드 중에 오류가 발생한 경우 처리할 내용 추가
                            Log.e(TAG, "이미지 다운로드 실패: ${exception.message}")
                        }

                    deletedImageIndex = MutableList(post?.images!!.size) {false}
                } else {
                    adapterImages = mutableListOf()
                    deletedImageIndex = mutableListOf()
                    postImagesUris = mutableListOf()
                }

                initImageRecycler()

                setView()
            } else {
                Toast.makeText(applicationContext, "게시글이 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        binding.addImageBtn.setOnClickListener {
            ActivityCompat.requestPermissions(this@EditActivity, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)      // 퍼미션 요구 (1회만)

            if (ContextCompat.checkSelfPermission(this@EditActivity.applicationContext, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(Intent.ACTION_PICK)
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE)
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, 2222)
            } else {
                Toast.makeText(this@EditActivity, "갤러리 접근 권한이 거부돼 있습니다. 설정에서 접근을 허용해 주세요.", Toast.LENGTH_SHORT).show()
            }

        }

    }

//    옵션 메뉴 생성
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_post, menu)
        return super.onCreateOptionsMenu(menu)
    }

//    옵션 메뉴 - 게시글 수정
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId){
        R.id.menu_add_save -> {
            val title = binding.titleEditText.text.toString()
            val content = binding.contentEditText.text.toString()

            dao.editPost(post?.pid.toString(), title, content, adapterImages, post?.images, post!!.images, object :
                CommunityDAO.EditPostCallback {
                override fun onEditPostCompleted() {
                    // 수정이 완료된 후에 호출되는 콜백 함수
                    finish() // 액티비티를 닫습니다 (CommunityActivity로 돌아감)
                }
            })

            true
        }
        else -> true
    }

//    화면 view 설정
    fun setView() {
        binding.titleEditText.setText(post?.title)
        binding.contentEditText.setText(post?.content)
    }

//    MultiImageAdapter와 이미지 RecyclerView 연결
    fun initImageRecycler() {
        imageAdapter = MultiImageAdapter(this)
        imageRecyclerView = binding.addImageRecycler

        imageRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        imageRecyclerView.adapter = imageAdapter

        imageAdapter.datas = adapterImages

        imageAdapter.notifyDataSetChanged()
    }

//    게시글 이미지 선택 후
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data == null) {
            Toast.makeText(this@EditActivity, "이미지를 선택하지 않았습니다.", Toast.LENGTH_SHORT).show()
        } else {
            if (data.getClipData() == null) {
                val imageUri = data.data
                imageUri?.let {
                    adapterImages.add(it)
                    newImages.add(it)
                }
            } else {
                val clipData = data.clipData

                if (clipData!!.itemCount + adapterImages.size > 3) {        // 선택된 이미지가 총 4장 이상일 때
                    Toast.makeText(applicationContext, "사진은 최대 3장까지 선택 가능합니다.", Toast.LENGTH_SHORT).show()
                } else {
                    for (i in 0 until clipData.itemCount) {
                        val imageUri = clipData.getItemAt(i).uri

                        try {
                            adapterImages.add(imageUri)
                        } catch (e: Exception) {
                            Log.e(TAG, "File select error", e)
                        }
                    }
                }
            }

            imageAdapter.datas = adapterImages
            imageAdapter.notifyDataSetChanged()

            for (imageUri in imageAdapter.datas) {
                Log.d(TAG, "imageURI : ${imageUri}")
            }
        }
    }

    companion object {
        lateinit var deletedImageIndex : MutableList<Boolean>       // DB에 저장된 이미지 중 삭제된 이미지 인덱스 저장
        lateinit var postImagesUris : MutableList<Uri>              // DB 이미지 Uri
    }
}