package sklookie.bowwow.community

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.*
import sklookie.bowwow.dao.CommunityDAO
import sklookie.bowwow.databinding.ActivityCommunityBinding
import sklookie.bowwow.dto.Comment
import sklookie.bowwow.dto.Post

class CommunityActivity : AppCompatActivity() {
    val TAG = "CommunityActivity"

    private lateinit var binding: ActivityCommunityBinding

    var posts = mutableListOf<Post>()
    lateinit var adapter: CommunityAdapter
    lateinit var mainRecyclerView: RecyclerView

    var sortedByView: Boolean = false

    lateinit var task: Tasks

    val dao = CommunityDAO()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommunityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.writeBtn.setOnClickListener {
            val writeIntent = Intent(this, AddActivity::class.java)
            startActivity(writeIntent)
        }

        dao.getPosts { posts ->
            if (!posts.isNullOrEmpty()) {
                this.posts = posts as MutableList<Post>
            }

            //        리사이클러뷰 adapter 연결
            initRecycler()
        }

        binding.sortByDateBtn.setOnClickListener {      // 게시글 날짜 순으로 정렬
            sortingByDate()
        }

        binding.sortByViewBtn.setOnClickListener {      // 게시글 조회수 순으로 정렬
            sortingByViews()
        }

        binding.searchBtn.setOnClickListener {          // 게시글 검색
            adapter.datas = posts

            val searchKeyword = binding.searchEditText.text.toString()
            val result = adapter.findPostsByKeyword(searchKeyword)
            adapter.datas = result as MutableList<Post>

            adapter.notifyDataSetChanged()
        }

        binding.swiper.setOnRefreshListener {                      // 당겨서 새로고침
            dao.getPosts { posts ->                                // 데이터 다시 불러오기
                if (!posts.isNullOrEmpty()) {                      // 게시글이 1개 이상 있을 때
                    for (post in posts) {
                        Log.d(TAG, "resume data update : ${post.toString()}")
                    }

                    this.posts = posts as MutableList<Post>

                    if (sortedByView) {
                        sortingByViews()
                    } else {
                        sortingByDate()
                    }

                    adapter.updateDatas(this.posts)
                    adapter.notifyDataSetChanged()
                } else {                                            // 게시글이 존재하지 않을 때
                    this.posts = mutableListOf<Post>()
                    adapter.updateDatas(this.posts)
                    adapter.notifyDataSetChanged()
                }
            }

            // 작업 완료 후 새로고침 상태 해제
            binding.swiper.isRefreshing = false
        }
    }

//    CommunityAdapter와 RecyclerView 연결
    private fun initRecycler() {
        adapter = CommunityAdapter(this)
        mainRecyclerView = binding.mainRecyclerView

        mainRecyclerView.layoutManager = LinearLayoutManager(this)
        mainRecyclerView.adapter = adapter

        adapter.datas = posts

        adapter.notifyDataSetChanged()
    }

//    날짜 순으로 게시글 정렬
    fun sortingByDate() {
        sortedByView = false

        adapter.updateDatas(posts)
        posts.sortByDescending { it.date }
        adapter.updateDatas(posts)

        mainRecyclerView.layoutManager?.removeAllViews()
        adapter.notifyDataSetChanged()

        binding.sortByDateBtn.setBackgroundColor(Color.parseColor("#2196F3"))
        binding.sortByViewBtn.setBackgroundColor(Color.parseColor("#BDBEC3"))
    }

//    조회수 순으로 게시글 정렬
    fun sortingByViews() {
        sortedByView = true

        adapter.updateDatas(posts)
        posts.sortByDescending { it.views?.toDouble() }
        adapter.updateDatas(posts)

        mainRecyclerView.layoutManager?.removeAllViews()
        adapter.notifyDataSetChanged()

        binding.sortByDateBtn.setBackgroundColor(Color.parseColor("#BDBEC3"))
        binding.sortByViewBtn.setBackgroundColor(Color.parseColor("#2196F3"))
    }

//    stop 상태일 때 게시글 정렬 기준 저장하기
    override fun onStop() {
        val bundle = Bundle()
        bundle.putBoolean("sortedByView", sortedByView)     // bundle에 정렬 방법(날짜 혹은 조회수) 저장

        intent.putExtra("bundle", bundle)
        super.onStop()
    }

//    resume 상태일 때 게시글 정렬 기준 불러오기 및 게시글 다시 불러오기
    override fun onResume() {

        val bundle = intent.getBundleExtra("bundle")

        if (bundle != null) {                               // bundle에 저장된 정렬 방법 확인
            if (bundle.getBoolean("sortedByView")) {
                sortingByViews()
            } else {
                sortingByDate()
            }
        }

        dao.getPosts { posts ->
            if (!posts.isNullOrEmpty()) {
                this.posts = posts as MutableList<Post>

                if (sortedByView) {
                    sortingByViews()
                } else {
                    sortingByDate()
                }

                adapter.updateDatas(this.posts)
                adapter.notifyDataSetChanged()
            } else {
                this.posts = mutableListOf<Post>()
                adapter.updateDatas(this.posts)
                adapter.notifyDataSetChanged()
            }
        }


        super.onResume()
    }
}