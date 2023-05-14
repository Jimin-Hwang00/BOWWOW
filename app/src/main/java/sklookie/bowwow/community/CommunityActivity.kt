package sklookie.bowwow.community

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.database.*
import sklookie.bowwow.R
import sklookie.bowwow.dto.Post

class CommunityActivity : AppCompatActivity() {

    val TAG = "MainActivity"

    var datas = mutableListOf<Post>()
    lateinit var adapter: PostAdapter
    lateinit var mainRecyclerView: RecyclerView

    val db: FirebaseDatabase = FirebaseDatabase.getInstance()
    val dbReference: DatabaseReference = db.getReference("post")

    lateinit var sortByDateBtn: Button
    lateinit var sortByViewsBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community)

        val writeBtn = findViewById<ExtendedFloatingActionButton>(R.id.write_Btn)
        writeBtn.setOnClickListener {
            val writeIntent = Intent(this, AddActivity::class.java)
            startActivity(writeIntent)
        }

        sortByDateBtn = findViewById(R.id.sortByDateBtn)
        sortByViewsBtn = findViewById(R.id.sortByViewBtn)

        // 리사이클러뷰 adapter 연결
        initRecycler()

        dbReference.addValueEventListener(postListener)

        sortByDateBtn.setOnClickListener {
            datas.sortByDescending { it.date }

            adapter.datas = datas

            for (data in adapter.datas) {
                Log.d(TAG, "sorted by date: " + data.toString())
            }

            mainRecyclerView.layoutManager?.removeAllViews()
            adapter.notifyDataSetChanged()

            sortByDateBtn.setBackgroundColor(Color.parseColor("#2196F3"))
            sortByViewsBtn.setBackgroundColor(Color.parseColor("#BDBEC3"))
        }

        sortByViewsBtn.setOnClickListener {
            Log.d(TAG, "sortByViewsBtn Clicked!")
            datas.sortByDescending { it.views?.toDouble() }

            adapter.datas = datas

            for (data in datas) {
                Log.d(TAG, "sorted by vies: " + data.toString())
            }
            mainRecyclerView.layoutManager?.removeAllViews()
            adapter.notifyDataSetChanged()

            sortByDateBtn.setBackgroundColor(Color.parseColor("#BDBEC3"))
            sortByViewsBtn.setBackgroundColor(Color.parseColor("#2196F3"))
        }
    }

    override fun onResume() {
        super.onResume()
        sortByDateBtn.setBackgroundColor(Color.parseColor("#2196F3"))
        sortByViewsBtn.setBackgroundColor(Color.parseColor("#BDBEC3"))
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
                    }
                }
            } catch(e: Exception) {
                Toast.makeText(this@CommunityActivity, "현재 게시물을 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "게시글 불러오기 오류: " + e.toString())
            }

            datas.sortByDescending { it.date }

            adapter.datas = datas
            mainRecyclerView.layoutManager?.removeAllViews()
            adapter.notifyDataSetChanged()
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("MainActivity", "게시글 불러오기 취소")
        }
    }
}