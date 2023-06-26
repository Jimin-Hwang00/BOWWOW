package sklookie.bowwow.community

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
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
import sklookie.bowwow.databinding.ActivityPostBinding
import sklookie.bowwow.dto.Comment
import sklookie.bowwow.dto.Post

class PostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostBinding

    val TAG = "PostActivity"

    var post: Post? = Post()
    val dao = CommunityDAO()

    var comments = mutableListOf<Comment>()
    lateinit var commentAdapter: CommentAdapter
    lateinit var commentRecyclerView: RecyclerView

    lateinit var images: MutableList<Uri>
    lateinit var imageAdapter: MultiImageAdapter
    lateinit var imageRecyclerView: RecyclerView

    val db: FirebaseDatabase = FirebaseDatabase.getInstance()
    lateinit var postReference: DatabaseReference

    lateinit var intentPid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = getIntent()

        intentPid = intent.getStringExtra("pid").toString()

        dao.getPostById(intentPid) { post ->
            if (post != null) {
                this.post = post

                dao.updateViews(intentPid, post.views!!.toInt())
                val updatedViews = post.views!!.toInt() + 1
                post.views = updatedViews.toString()

                val imageTasks = ArrayList<Task<Uri>>() // 이미지 다운로드 Task들을 저장하기 위한 리스트

//                 이미지 내용 images 변수에 넣기 (댓글 리사이클러뷰에 사용하기 위함)
                if (post?.images != null) {
                    val firebaseStorage = FirebaseStorage.getInstance()
                    val rootRef = firebaseStorage.reference

                    images = MutableList(post.images!!.size) {Uri.EMPTY}

                    post?.images!!.forEachIndexed() { index, image ->
                        val imgRef = rootRef.child("post/$image")
                        Log.d(TAG, "이미지 foreach : post/$image")
                        if (imgRef != null) {
                            val imageUrlTask = imgRef.downloadUrl.addOnSuccessListener { uri ->
                                images[index] = uri
                                Log.d(TAG, "uri 가져옴 : ${uri.toString()}")
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
                } else {
                    images = mutableListOf()
                }

                initImageRecycler()

//            댓글 내용 comments 변수에 넣기 (댓글 리사이클러뷰에 사용하기 위함)
                if (!post?.comments.isNullOrEmpty()) {
                    comments = post?.comments as ArrayList<Comment>
                    comments?.sortBy { it.date }
                }
                initCommentRecycler()

                setView()
            } else {
                Toast.makeText(applicationContext, "게시글이 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }

        }

        //                    댓글 등록 버튼 클릭 이벤트 설정
        binding.commentBtn.setOnClickListener {
            val comment = binding.commentEditText
            if (!comment.text.isNullOrEmpty()) {
                lateinit var newComment : Comment
                post?.pid?.let { it1 -> newComment = dao.addComment(it1, comment.text.toString(), "uid", object : CommunityDAO.AddCommentCallback {
                    override fun onAddCommentComplete() {
                        updateDataAndView()
                    }
                }) }

                comment.text = null         // 댓글 등록 후 edit 창 비우기
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.commentEditText.windowToken, 0)
            } else {
                Toast.makeText(this@PostActivity, "댓글을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.swiper.setOnRefreshListener {
            updateDataAndView()
            binding.swiper.isRefreshing = false
        }
    }


//    옵션 메뉴 생성
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_extra, menu)
        return super.onCreateOptionsMenu(menu)
    }

//    옵션 메뉴 - 수정, 삭제 기능
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId){
        R.id.menu_edit -> {
            val editIntent = Intent(this, EditActivity::class.java)
            editIntent.putExtra("pid", post?.pid)
            startActivity(editIntent)

            finish()

            true
        }
        R.id.menu_delete -> {
            dao.deletePost(post?.pid.toString(), post?.images, object : CommunityDAO.DeletePostCallback {
                override fun onDeletePostComplete() {
                    finish()
                }
            })

            true
        }
        else -> true
    }

//    댓글 리사이클러뷰 설정
    private fun initCommentRecycler() {
        commentAdapter = CommentAdapter(this, object: CommentAdapter.CommentDeletedListener {
            override fun onCommentDeleted() {
                updateDataAndView()
            }
        })
        commentRecyclerView = binding.commentRecyclerView

        commentRecyclerView.layoutManager = LinearLayoutManager(this)
        commentRecyclerView.adapter = commentAdapter

        commentAdapter.datas = comments

        for (comment in commentAdapter.datas) {
            Log.d(TAG, "comment : ${comment}")
        }

        commentAdapter.notifyDataSetChanged()
    }

    private fun initImageRecycler() {
        imageAdapter = MultiImageAdapter(this)
        imageRecyclerView = binding.postImageRecycler

        imageRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        imageRecyclerView.adapter = imageAdapter

        imageAdapter.datas = images

        images.forEach {
            Log.d(TAG, "images에 저장된 uri : $it")
        }

        imageAdapter.datas.forEach {
            Log.d(TAG, "adapter data : ${it}")
        }

        imageAdapter.notifyDataSetChanged()
    }

//    화면 view 설정
    fun setView() {
        binding.titleTextView.text = post?.title
        binding.uidTextView.text = post?.uid
        binding.dateTextView.text = post?.date
        binding.contentTextView.text = post?.content
        binding.viewsTextView.text = post?.views.toString()
    }

    fun updateDataAndView() {
        Log.d(TAG, "updateDataAndView !!")
        dao.getPostById(intentPid) {
            if (it != null) {
                post = it

                val imageTasks =
                    ArrayList<Task<Uri>>() // 이미지 다운로드 Task들을 저장하기 위한 리스트

//                 이미지 내용 images 변수에 넣기 (댓글 리사이클러뷰에 사용하기 위함)
                if (post?.images != null) {
                    val firebaseStorage = FirebaseStorage.getInstance()
                    val rootRef = firebaseStorage.reference

                    post?.images!!.forEachIndexed() { index, image ->
                        val imgRef = rootRef.child("post/$image")
                        Log.d(TAG, "이미지 foreach : post/$image")
                        if (imgRef != null) {
                            val imageUrlTask = imgRef.downloadUrl.addOnSuccessListener { uri ->
                                images[index] = uri
                                Log.d(TAG, "uri 가져옴 : ${uri.toString()}")
                            }
                            imageTasks.add(imageUrlTask)    // 이미지 다운로드 Task를 리스트에 추가
                        }
                    }

//                     모든 이미지 다운로드 Task가 완료되었을 때 initImageRecycler() 함수 호출
                    Tasks.whenAllSuccess<Uri>(imageTasks)
                        .addOnSuccessListener {
                            imageAdapter.datas = images
                            imageAdapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener { exception ->
                            // 이미지 다운로드 중에 오류가 발생한 경우 처리할 내용 추가
                            Log.e(TAG, "이미지 다운로드 실패: ${exception.message}")
                        }
                }

//            댓글 내용 comments 변수에 넣기 (댓글 리사이클러뷰에 사용하기 위함)
                if (!post?.comments.isNullOrEmpty()) {
                    comments = post?.comments as ArrayList<Comment>
                    comments?.sortBy { it.date }
                }

                commentAdapter.datas = comments
                commentAdapter.notifyDataSetChanged()

                setView()
            } else {
                Toast.makeText(
                    applicationContext,
                    "게시글이 존재하지 않습니다.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

}