package sklookie.bowwow.community

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import sklookie.bowwow.LoginActivity
import sklookie.bowwow.R
import sklookie.bowwow.dao.CommunityDAO
import sklookie.bowwow.databinding.FragmentCommunityBinding
import sklookie.bowwow.dto.Post

const val TAG_COMMUNITY = "community_fragment"
class CommunityFragment : Fragment(), OnCommunityRecylerItemClick {
    private lateinit var binding: FragmentCommunityBinding

    private var posts = mutableListOf<Post>()
    private var adapter: CommunityAdapter? = null
    private lateinit var mainRecyclerView: RecyclerView

    private var sortedByView: Boolean = false

    private val dao = CommunityDAO()

//    게시글 항목 중 하나 클릭 시 게시글 상세 보기 가능 (PostFragment로 전환)
    override fun onCommunityRecyclerItemClick(pid: String) {
        val bundle = Bundle()
        bundle.putString("pid", pid)

        val postBundleFragment = PostFragment()
        postBundleFragment.arguments = bundle
        parentFragmentManager.beginTransaction().replace(R.id.mainFrameLayout, postBundleFragment).addToBackStack(null).commit()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 게시글 추가 버튼 클릭 리스너 구현 (AddFragment로 전환)
        binding.writeBtn.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            if (auth.uid.isNullOrEmpty()) {
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.google_login_dialog_title))
                    .setMessage(getString(R.string.google_login_dialog_message))
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog , id ->
                        val intent = Intent(requireContext(), LoginActivity::class.java)
                        startActivity(intent)
                    })
                    .setNegativeButton("취소", null)
                    .show()
            } else {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.mainFrameLayout, AddFragment()).addToBackStack(null).commit()
            }
        }

        // 파이어베이스에서 게시글 전체 불러오기
        lifecycleScope.launch {
            posts = withContext(Dispatchers.IO) {
                dao.getAllPosts()!!
            }

            val userNameJobs = posts.map { post ->
                async(Dispatchers.IO) {
                    post.uname = dao.getUserNameByUid(post.uid!!)
                }
            }

            // 모든 게시글의 사용자 이름을 가져올 때까지 대기
            userNameJobs.awaitAll()

            initRecycler()

            if (sortedByView) {
                sortingByViews()
            } else {
                sortingByDate()
            }
        }

        // 게시글 날짜 순으로 정렬
        binding.sortByDateBtn.setOnClickListener {
            sortingByDate()
        }

        // 게시글 조회수 순으로 정렬
        binding.sortByViewBtn.setOnClickListener {
            sortingByViews()
        }

        // 게시글 검색 버튼 클릭 리스너 구현
        binding.searchBtn.setOnClickListener {
            adapter!!.datas = posts

            val searchKeyword = binding.searchEditText.text.toString()
            val result = adapter?.findPostsByKeyword(searchKeyword)
            adapter!!.datas = result as MutableList<Post>

            adapter!!.notifyDataSetChanged()
        }

        // 당겨서 새로고침 구현 (게시글 전부 다시 가져오기)
        binding.swiper.setOnRefreshListener {
            lifecycleScope.launch {
                posts = withContext(Dispatchers.IO) {
                    dao.getAllPosts()!!
                }

                val userNameJobs = posts.map { post ->
                    async(Dispatchers.IO) {
                        post.uname = dao.getUserNameByUid(post.uid!!)
                    }
                }

                // 모든 게시글의 사용자 이름을 가져올 때까지 대기
                userNameJobs.awaitAll()

                initRecycler()

                if (sortedByView) {
                    sortingByViews()
                } else {
                    sortingByDate()
                }
            }

            adapter?.notifyDataSetChanged()

            binding.swiper.isRefreshing = false
        }
    }

//    리사이클러뷰와 어댑터 연결 작업
    private fun initRecycler() {
        adapter = CommunityAdapter(requireContext(), this)
        mainRecyclerView = binding.mainRecyclerView

        mainRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        mainRecyclerView.adapter = adapter

        adapter!!.datas = posts

        adapter!!.notifyDataSetChanged()
    }

//    날짜 순으로 게시글 정렬
    private fun sortingByDate() {
        sortedByView = false

        posts.sortByDescending { it.date }
        adapter!!.updateDatas(posts)

        mainRecyclerView.layoutManager?.removeAllViews()
        adapter!!.notifyDataSetChanged()

        binding.sortByDateBtn.setBackgroundColor(Color.parseColor("#6078F0"))
        binding.sortByViewBtn.setBackgroundColor(Color.parseColor("#656770"))
    }

//    조회수 순으로 게시글 정렬
    fun sortingByViews() {
        sortedByView = true

        posts.sortByDescending { it.views?.toDouble() }
        adapter!!.updateDatas(posts)

        mainRecyclerView.layoutManager?.removeAllViews()
        adapter!!.notifyDataSetChanged()

        binding.sortByDateBtn.setBackgroundColor(Color.parseColor("#656770"))
        binding.sortByViewBtn.setBackgroundColor(Color.parseColor("#6078F0"))
    }
}


