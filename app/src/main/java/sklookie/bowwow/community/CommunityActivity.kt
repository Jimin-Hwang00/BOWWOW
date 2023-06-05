package sklookie.bowwow.community

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import sklookie.bowwow.databinding.ActivityCommunityBinding
import sklookie.bowwow.dto.Comment
import sklookie.bowwow.dto.Post

class CommunityActivity : AppCompatActivity() {
    val TAG = "CommunityActivity"

    private lateinit var binding: ActivityCommunityBinding

    var datas = mutableListOf<Post>()
    lateinit var adapter: PostAdapter
    lateinit var mainRecyclerView: RecyclerView

    val db: FirebaseDatabase = FirebaseDatabase.getInstance()
    val dbReference: DatabaseReference = db.getReference("post")

    var sortedByView: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommunityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.writeBtn.setOnClickListener {
            val writeIntent = Intent(this, AddActivity::class.java)
            startActivity(writeIntent)
        }

//        리사이클러뷰 adapter 연결
        initRecycler()

        dbReference.addValueEventListener(postListener)

        binding.sortByDateBtn.setOnClickListener {
            sortingByDate()
        }

        binding.sortByViewBtn.setOnClickListener {
            sortingByViews()
        }

        binding.searchBtn.setOnClickListener {
            adapter.datas = datas

            val searchKeyword = binding.searchEditText.text.toString()
            val result = adapter.findPostsByKeyword(searchKeyword)
            adapter.datas = result as MutableList<Post>

            adapter.notifyDataSetChanged()
        }
    }

    private fun initRecycler() {
        adapter = PostAdapter(this)
        mainRecyclerView = binding.mainRecyclerView

        mainRecyclerView.layoutManager = LinearLayoutManager(this)
        mainRecyclerView.adapter = adapter

        adapter.datas = datas

        adapter.notifyDataSetChanged()
    }

    val postListener = object: ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            try {
                datas.clear()

                for (data in snapshot.children) {

                    val post = Post().apply {
                        pid = data.key
                        title = data.child("title").getValue(String::class.java)
                        content = data.child("content").getValue(String::class.java)
                        date = data.child("date").getValue(String::class.java)
                        uid = data.child("uid").getValue(String::class.java)
                        views = data.child("views").getValue(String::class.java)
                        image = data.child("image").getValue(String::class.java)
                        comments = ArrayList<Comment>()

                        val commentsSnapshot = data.child("comments")
                        for (commentSnapshot in commentsSnapshot.children) {
                            val comment = Comment().apply {
                                cid = commentSnapshot.key
                                comment = commentSnapshot.child("comment").getValue(String::class.java)
                                date = commentSnapshot.child("date").getValue(String::class.java)
                                uid = commentSnapshot.child("uid").getValue(String::class.java)
                            }
                            (comments as ArrayList<Comment>)?.add(comment)
                        }
                    }

                    datas.add(post)

                    Log.d(TAG, post.toString())
                }
            } catch(e: Exception) {
                Toast.makeText(this@CommunityActivity, "현재 게시물을 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "게시글 불러오기 오류: " + e.toString())
            }

            datas.sortByDescending { it.date }

            adapter.updateDatas(datas)
            mainRecyclerView.layoutManager?.removeAllViews()
            adapter.notifyDataSetChanged()
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("MainActivity", "게시글 불러오기 취소")
        }
    }

//    날짜 순으로 게시글 정렬
    fun sortingByDate() {
        sortedByView = false

        adapter.updateDatas(datas)
        datas.sortByDescending { it.date }
        adapter.updateDatas(datas)

        mainRecyclerView.layoutManager?.removeAllViews()
        adapter.notifyDataSetChanged()

        binding.sortByDateBtn.setBackgroundColor(Color.parseColor("#2196F3"))
        binding.sortByViewBtn.setBackgroundColor(Color.parseColor("#BDBEC3"))
    }

//    조회수 순으로 게시글 정렬
    fun sortingByViews() {
        sortedByView = true

        adapter.updateDatas(datas)
        datas.sortByDescending { it.views?.toDouble() }
        adapter.updateDatas(datas)

        mainRecyclerView.layoutManager?.removeAllViews()
        adapter.notifyDataSetChanged()

        binding.sortByDateBtn.setBackgroundColor(Color.parseColor("#BDBEC3"))
        binding.sortByViewBtn.setBackgroundColor(Color.parseColor("#2196F3"))
    }

    override fun onStop() {
        val bundle: Bundle = Bundle()
        bundle.putBoolean("sortedByView", sortedByView)     // bundle에 정렬 방법(날짜 혹은 조회수) 저장

        intent.putExtra("bundle", bundle)
        super.onStop()
    }

    override fun onResume() {
        val bundle = intent.getBundleExtra("bundle")

        if (bundle != null) {                               // bundle에 저장된 정렬 방법 확인
            if (bundle.getBoolean("sortedByView")) {
                sortingByViews()
            } else {
                sortingByDate()
            }
        }

        super.onResume()
    }
}