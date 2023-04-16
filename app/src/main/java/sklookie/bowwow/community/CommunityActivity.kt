package sklookie.bowwow.community

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.database.*
import sklookie.bowwow.R
import sklookie.bowwow.dto.Post

class CommunityActivity : AppCompatActivity() {
    var datas = mutableListOf<Post>()
    lateinit var adapter: PostAdapter
    lateinit var mainRecyclerView: RecyclerView

    val db: FirebaseDatabase = FirebaseDatabase.getInstance()
    val dbReference: DatabaseReference = db.getReference("post")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community)

        val writeBtn = findViewById<ExtendedFloatingActionButton>(R.id.write_Btn)
        writeBtn.setOnClickListener {
            val writeIntent = Intent(this, AddActivity::class.java)
            startActivity(writeIntent)
        }

        // 리사이클러뷰 adapter 연결
        initRecycler()

        dbReference.addValueEventListener(postListener)
    }

    private fun initRecycler() {
        adapter = PostAdapter(this)
        mainRecyclerView = findViewById<RecyclerView>(R.id.main_recyclerView)

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
                    val post = data.getValue(Post::class.java)
                    val key = data.key

                    if (post != null) {
                        post.pid = key
                        datas.add(post)

                        Log.d("data check", post.toString())
                    }
                }
            } catch(e: Exception) {
                Log.d("MainActivity", "게시글 불러오기 오류")
            }

            for (i in 1..datas.size) {
                Log.d("datas check", datas.get(i - 1).toString())
            }

            datas.sortByDescending { it.date }

            adapter.datas = datas
            adapter.notifyDataSetChanged()
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("MainActivity", "게시글 불러오기 취소")
        }
    }
}