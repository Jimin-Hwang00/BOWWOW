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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.database.*
import sklookie.bowwow.MainHomeActivity
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

    var sortedByView: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community)

        findViewById<BottomNavigationView>(R.id.btmMenu).setOnNavigationItemReselectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu1 -> {
                    navigateToActivity(MainHomeActivity::class.java)
                    true
                }
                R.id.menu4 -> {
                    navigateToActivity(CommunityActivity::class.java)
                    true
                }
                else -> false
            }
        }

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
            sortingByDate()
        }

        sortByViewsBtn.setOnClickListener {
            sortingByViews()
        }

        findViewById<Button>(R.id.searchBtn).setOnClickListener {
            adapter.datas = datas

            val searchKeyword = findViewById<EditText>(R.id.searchEditText).text.toString()
            val result = adapter.findPostsByKeyword(searchKeyword)
            adapter.datas = result as MutableList<Post>

            adapter.notifyDataSetChanged()
        }

    }

    private fun initRecycler() {
        adapter = PostAdapter(this)
        mainRecyclerView = findViewById<RecyclerView>(R.id.main_recyclerView)

        mainRecyclerView.layoutManager = LinearLayoutManager(this)
        mainRecyclerView.adapter = adapter

        adapter.datas = datas

        adapter.notifyDataSetChanged()
    }

    val postListener = object : ValueEventListener {
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
            } catch (e: Exception) {
                Toast.makeText(this@CommunityActivity, "현재 게시물을 불러올 수 없습니다.", Toast.LENGTH_SHORT)
                    .show()
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

    //    날짜 순으로 게시글 정렬
    fun sortingByDate() {
        sortedByView = false

        adapter.datas = datas
        datas.sortByDescending { it.date }
        adapter.datas = datas

        mainRecyclerView.layoutManager?.removeAllViews()
        adapter.notifyDataSetChanged()

        sortByDateBtn.setBackgroundColor(Color.parseColor("#2196F3"))
        sortByViewsBtn.setBackgroundColor(Color.parseColor("#BDBEC3"))
    }

    //    조회수 순으로 게시글 정렬
    fun sortingByViews() {
        sortedByView = true

        adapter.datas = datas
        datas.sortByDescending { it.views?.toDouble() }
        adapter.datas = datas

        mainRecyclerView.layoutManager?.removeAllViews()
        adapter.notifyDataSetChanged()

        sortByDateBtn.setBackgroundColor(Color.parseColor("#BDBEC3"))
        sortByViewsBtn.setBackgroundColor(Color.parseColor("#2196F3"))
    }

    override fun onStop() {
        val bundle: Bundle = Bundle()
        bundle.putBoolean("sortedByView", sortedByView)     // bundle에 정렬 방법(날짜 혹은 조회수) 저장

        getIntent().putExtra("bundle", bundle)
        super.onStop()
    }

    override fun onResume() {
        findViewById<BottomNavigationView>(R.id.btmMenu).selectedItemId = R.id.menu4

        val bundle = getIntent().getBundleExtra("bundle")

        if (bundle != null) {                               // bundle에 저장된 정렬 방법 확인
            if (bundle.getBoolean("sortedByView")) {
                sortingByViews()
            } else {
                sortingByDate()
            }
        }

        super.onResume()
    }

    private fun navigateToActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        finish()
        startActivity(intent)
    }
}