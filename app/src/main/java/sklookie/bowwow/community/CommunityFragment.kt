package sklookie.bowwow.community

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Tasks
import sklookie.bowwow.R
import sklookie.bowwow.dao.CommunityDAO
import sklookie.bowwow.databinding.FragmentCommunityBinding
import sklookie.bowwow.dto.Post

class CommunityFragment : Fragment(), OnCommunityRecylerItemClick {
    private lateinit var binding: FragmentCommunityBinding

    private var posts = mutableListOf<Post>()
    private lateinit var adapter: CommunityAdapter
    private lateinit var mainRecyclerView: RecyclerView

    private var sortedByView: Boolean = false

    private lateinit var task: Tasks

    private val dao = CommunityDAO()

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

        binding.writeBtn.setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.mainFrameLayout, AddFragment()).addToBackStack(null).commit()
        }

        dao.getPosts { posts ->
            if (!posts.isNullOrEmpty()) {
                this.posts = posts as MutableList<Post>
            }
            initRecycler()
        }

//        val sortedByView = arguments?.getBoolean("sortedByView") ?: false
//        if (sortedByView) {
//            sortingByViews()
//        } else {
//            sortingByDate()
//        }

        binding.sortByDateBtn.setOnClickListener {
            sortingByDate()
        }

        binding.sortByViewBtn.setOnClickListener {
            sortingByViews()
        }

        binding.searchBtn.setOnClickListener {
            adapter.datas = posts

            val searchKeyword = binding.searchEditText.text.toString()
            val result = adapter.findPostsByKeyword(searchKeyword)
            adapter.datas = result as MutableList<Post>

            adapter.notifyDataSetChanged()
        }

        binding.swiper.setOnRefreshListener {
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

            binding.swiper.isRefreshing = false
        }
    }

    private fun initRecycler() {
        adapter = CommunityAdapter(requireContext(), this)
        mainRecyclerView = binding.mainRecyclerView

        mainRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        mainRecyclerView.adapter = adapter

        adapter.datas = posts

        adapter.notifyDataSetChanged()
    }

    private fun sortingByDate() {
        sortedByView = false

        adapter.updateDatas(posts)
        posts.sortByDescending { it.date }
        adapter.updateDatas(posts)

        mainRecyclerView.layoutManager?.removeAllViews()
        adapter.notifyDataSetChanged()

        binding.sortByDateBtn.setBackgroundColor(Color.parseColor("#2196F3"))
        binding.sortByViewBtn.setBackgroundColor(Color.parseColor("#BDBEC3"))
    }

    private fun sortingByViews() {
        sortedByView = true

        adapter.updateDatas(posts)
        posts.sortByDescending { it.views?.toDouble() }
        adapter.updateDatas(posts)
    }

    override fun onDestroy() {
        super.onDestroy()
        newInstance(sortedByView)
    }

    companion object {
        fun newInstance(sortedByView: Boolean): CommunityFragment {
            val fragment = CommunityFragment()
            val args = Bundle()
            args.putBoolean("sortedByView", sortedByView)
            fragment.arguments = args
            return fragment
        }
    }

}


