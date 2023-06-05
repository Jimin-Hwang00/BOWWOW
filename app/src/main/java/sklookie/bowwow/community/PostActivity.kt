package sklookie.bowwow.community

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.*
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

    var comments : ArrayList<Comment> = ArrayList()
    lateinit var commentAdapter: CommentAdapter
    lateinit var commentRecyclerView: RecyclerView

    val db: FirebaseDatabase = FirebaseDatabase.getInstance()
    lateinit var postReference: DatabaseReference

    lateinit var intentPid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = getIntent()

        intentPid = intent.getStringExtra("pid").toString()

        val asyncTask = object : AsyncTask<String, Void, Post?>() {
            override fun doInBackground(vararg p0: String?): Post? {
                postReference = db.getReference("post/${intentPid}")

//                파이어베이스에서 데이터 읽어오기
                val result: Post? = Post()
                val titleTask = postReference.child("title").get()
                val uidTask = postReference.child("uid").get()
                val dateTask = postReference.child("date").get()
                val viewsTask = postReference.child("views").get()
                val imageTask = postReference.child("image").get()
                val contentTask = postReference.child("content").get()
                val commentTask = postReference.child("comments").get()

//                파이어베이스에서 읽어온 데이터 변수에 저장
                try {
                    result?.pid = intentPid

                    val titleSnapshot = Tasks.await(titleTask)
                    result?.title = titleSnapshot.value.toString()

                    val uidSnapshot = Tasks.await(uidTask)
                    result?.uid = uidSnapshot.value.toString()

                    val dateSnapshot = Tasks.await(dateTask)
                    result?.date = dateSnapshot.value.toString()

                    val viewsSnapshot = Tasks.await(viewsTask)
                    result?.views = (viewsSnapshot.value.toString().toInt() + 1).toString()
                    dao.updateViews(intentPid, result?.views!!.toInt())

                    val imageSnapshot = Tasks.await(imageTask)
                    result?.image = imageSnapshot.value.toString()

                    val contentSnapshot = Tasks.await(contentTask)
                    result?.content = contentSnapshot.value.toString()

                    val commentSnapshot = Tasks.await(commentTask)
                    val commentsList: MutableList<Comment> = mutableListOf()

                    commentSnapshot.children.forEach { commentDataSnapshot ->
                        val comment = Comment().apply {
                            cid = commentDataSnapshot.key
                            this.comment = commentDataSnapshot.child("comment").value.toString()
                            this.date = commentDataSnapshot.child("date").value.toString()
                            this.uid = commentDataSnapshot.child("uid").value.toString()
                            this.pid = result?.pid
                        }
                        commentsList.add(comment)
                    }

                    result?.comments = commentsList

                    result?.toString()?.let { Log.d(TAG, it) }

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

//                댓글 내용 comments 변수에 넣기 (댓글 리사이클러뷰에 사용하기 위함)
                if (post?.comments != null) {
                    comments = post?.comments as ArrayList<Comment>
                    comments?.sortBy { it.date }
                }

                initCommentRecycler()

                //        댓글 등록 버튼 클릭 이벤트 설정
                binding.commentBtn.setOnClickListener {
                    val comment = binding.commentEditText
                    if (!comment.text.isNullOrEmpty()) {
                        lateinit var newComment : Comment
                        post?.pid?.let { it1 -> newComment = dao.addComment(it1, comment.text.toString(), "uid") }
                        comments.add(newComment)        // 댓글 리사이클러뷰에 사용되는 comments 변수에 새 댓글 추가
                        comments?.sortBy { it.date }    // 날짜 순으로 정렬 (오름차순)
                        commentAdapter.notifyDataSetChanged()
                        comment.text = null         // 댓글 등록 후 edit 창 비우기
                    } else {
                        Toast.makeText(this@PostActivity, "댓글을 입력하세요.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        asyncTask.execute(intentPid)
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
            dao.deletePost(post?.pid.toString())

            finish()

            true
        }
        else -> true
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

//    댓글 리사이클러뷰 설정
    private fun initCommentRecycler() {
        commentAdapter = CommentAdapter(this)
        commentRecyclerView = binding.commentRecyclerView

        commentRecyclerView.layoutManager = LinearLayoutManager(this)
        commentRecyclerView.adapter = commentAdapter

        commentAdapter.datas = comments as MutableList<Comment>

        for (comment in commentAdapter.datas) {
            Log.d(TAG, "comment : ${comment.toString()}")
        }

        commentAdapter.notifyDataSetChanged()
    }

//    화면 view 설정
    fun setView() {

        binding.titleTextView.text = post?.title
        binding.uidTextView.text = post?.uid
        binding.dateTextView.text = post?.date
        binding.contentTextView.text = post?.content
        binding.viewsTextView.text = post?.views.toString()
        if (post?.image.isNullOrEmpty()) {
            binding.postImageView.isGone = true
            if (post?.content.isNullOrEmpty()) {
                binding.contentTextView.isGone = true
            }
        } else {
            binding.postImageView.setImageBitmap(StringToBitmap(post?.image!!))
        }
    }

}