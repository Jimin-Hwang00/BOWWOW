package sklookie.bowwow.community

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import sklookie.bowwow.R
import sklookie.bowwow.dao.CommunityDAO
import sklookie.bowwow.databinding.FragmentCommunityBinding
import sklookie.bowwow.databinding.FragmentMyPostBinding
import sklookie.bowwow.dto.Post

const val TAG = "MyPostFragment"
class MyPostFragment : Fragment(), OnCommunityRecylerItemClick {
    private lateinit var binding: FragmentMyPostBinding

    private var posts = mutableListOf<Post>()
    private var adapter: CommunityAdapter? = null
    private lateinit var mainRecyclerView: RecyclerView

    private val dao = CommunityDAO()

    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser

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
        binding = FragmentMyPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 파이어베이스에서 게시글 전체 불러오기
        lifecycleScope.launch {
            if (currentUser != null) {
                posts = withContext(Dispatchers.IO) {
                    dao.getMyPosts(currentUser.uid)!!
                } as MutableList<Post>

                val userNameJobs = posts.map { post ->
                    async(Dispatchers.IO) {
                        post.uname = dao.getUserNameByUid(post.uid!!)
                    }
                }

                // 모든 게시글의 사용자 이름을 가져올 때까지 대기
                userNameJobs.awaitAll()

                initRecycler()
            } else {
                Toast.makeText(context, "로그인 상태를 확인해주세요.", Toast.LENGTH_LONG).show()
            }
        }

        // 당겨서 새로고침 구현 (게시글 전부 다시 가져오기)
        binding.myPostSwiper.setOnRefreshListener {
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
            }

            adapter?.notifyDataSetChanged()

            binding.myPostSwiper.isRefreshing = false
        }
    }

    //    리사이클러뷰와 어댑터 연결 작업
    private fun initRecycler() {
        adapter = CommunityAdapter(requireContext(), this)
        mainRecyclerView = binding.rcMyPost

        mainRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        mainRecyclerView.adapter = adapter

        adapter!!.datas = posts

        adapter!!.notifyDataSetChanged()
    }
}


