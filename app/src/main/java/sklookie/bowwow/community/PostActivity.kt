package sklookie.bowwow.community

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import sklookie.bowwow.R
import sklookie.bowwow.dao.CommunityDAO
import sklookie.bowwow.databinding.ActivityPostBinding
import sklookie.bowwow.dto.Comment
import sklookie.bowwow.dto.Post

class PostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostBinding

    val TAG = "PostActivity"

    lateinit var post: Post
    val dao = CommunityDAO()

    var comments : ArrayList<Comment> = ArrayList()
    lateinit var commentAdapter: CommentAdapter
    lateinit var commentRecyclerView: RecyclerView

    val db: FirebaseDatabase = FirebaseDatabase.getInstance()
    val postReference: DatabaseReference = db.getReference("post")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = getIntent()

        post = intent.getSerializableExtra("post") as Post

        var views = post.views!!.toInt()
        post.views = (++views).toString()
        dao.updateViews(post.pid.toString(), views)

        binding.titleTextView.text = post.title
        binding.uidTextView.text = post.uid
        binding.dateTextView.text = post.date
        binding.contentTextView.text = post.content
        binding.viewsTextView.text = post.views.toString()
        if (post.image.isNullOrEmpty()) {
            binding.postImageView.isGone = true
            if (post.content.isNullOrEmpty()) {
                binding.contentTextView.isGone = true
            }
        } else {
            binding.postImageView.setImageBitmap(StringToBitmap(post.image!!))
        }

        if (post.comments != null) {
            comments = post.comments as ArrayList<Comment>
        }
        initRecycler()

        findViewById<Button>(R.id.comment_btn).setOnClickListener {
            val comment = findViewById<EditText>(R.id.comment_edit_text)
            if (comment != null) {
                post.pid?.let { it1 -> dao.addComment(it1, comment.text.toString(), "uid") }
            } else {
                Toast.makeText(this, "댓글을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        post.pid?.let { postReference.child(it).child("comments").addValueEventListener(commentListener) }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_extra, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId){
        R.id.menu_edit -> {
            val editIntent = Intent(this, EditActivity::class.java)
            editIntent.putExtra("post", post)
            startActivity(editIntent)

            finish()

            true
        }
        R.id.menu_delete -> {
            dao.deletePost(post.pid.toString())

            finish()

            true
        }
        else -> true
    }

//    //    String을 Bitmap으로 변경 (서버에 저장된 이미지 String을 이미지 뷰에 띄우기 위함)
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

    private fun initRecycler() {
        commentAdapter = CommentAdapter(this)
        commentRecyclerView = binding.commentRecyclerView

        commentRecyclerView.layoutManager = LinearLayoutManager(this)
        commentRecyclerView.adapter = commentAdapter

        commentAdapter.datas = comments as MutableList<Comment>

        commentAdapter.notifyDataSetChanged()
    }

    val commentListener = object: ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            try {
                comments?.clear()

                for (data in snapshot.children) {
                    val comment = data.getValue(Comment::class.java)
                    val key = data.key

                    if (comment != null) {
                        comment.cid = key
                        comments?.add(comment)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@PostActivity, "댓글을 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "댓글 불러오기 오류: " + e.toString())
            }

            comments?.sortBy { it.date }

            commentAdapter.datas = comments as MutableList<Comment>
            commentRecyclerView.layoutManager?.removeAllViews()
            commentAdapter.notifyDataSetChanged()
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("MainActivity", "게시글 불러오기 취소")
        }
    }
}