package sklookie.bowwow.community

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import sklookie.bowwow.R
import sklookie.bowwow.dao.CommunityDAO
import sklookie.bowwow.databinding.FragmentPostBinding
import sklookie.bowwow.dto.Comment
import sklookie.bowwow.dto.GoogleInfo
import sklookie.bowwow.dto.Post

class PostFragment : Fragment(), OnCommunityRecylerItemClick {

    private lateinit var binding: FragmentPostBinding

    val TAG = "PostFragment"

    private lateinit var pid: String
    var post: Post? = Post()
    val dao = CommunityDAO()

    var comments = mutableListOf<Comment>()
    lateinit var commentAdapter: CommentAdapter
    lateinit var commentRecyclerView: RecyclerView

    lateinit var images: MutableList<Uri>
    lateinit var imageAdapter: MultiImageAdapter
    lateinit var imageRecyclerView: RecyclerView

    override fun onCommunityRecyclerItemClick(pid: String) {
        this.pid = pid
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPostBinding.inflate(inflater, container, false)
        pid = arguments?.getString("pid").toString()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val arguments = arguments
        if (arguments != null) {
            pid = arguments.getString("pid", "")

            lifecycleScope.launch {
                post = withContext(Dispatchers.IO) {
                    dao.updateViews(pid)
                    dao.getPostById(pid)
                }

                val userNameJob = async(Dispatchers.IO) {
                    post?.uname = dao.getUserNameByUid(post?.uid!!)
                    Log.d(TAG, "userName : ${post?.uname}")
                }

                // 모든 게시글의 사용자 이름을 가져올 때까지 대기
                userNameJob.await()

                if (post?.images != null) {
                    images = MutableList(post?.images!!.size) {Uri.EMPTY}

                    post?.images!!.forEachIndexed() { index, image ->
                        images[index] = post?.images!![index].toUri()
                        Log.d(TAG, "onViewCreated : ${images[index]}")
                    }
                } else {
                    images = mutableListOf()
                }
                initImageRecycler()

                if (!post?.comments.isNullOrEmpty()) {
                    comments = post?.comments as ArrayList<Comment>
                    comments?.sortBy { it.date }
                }

                comments.forEach { comment ->
                    comment.uname = dao.getUserNameByUid(comment.uid!!)
                }
                initCommentRecycler()

                val auth = FirebaseAuth.getInstance()
                if (auth.uid.isNullOrEmpty()) {
                    binding.updateText.isGone = true
                    binding.deleteText.isGone = true
                }

                if (!auth.uid.isNullOrEmpty()) {
                    if (!auth.uid.equals(post?.uid)) {
                        binding.updateText.isGone = true
                        binding.deleteText.isGone = true
                    }
                }

                setView()
            }
        }

        //  댓글 등록 버튼 클릭 이벤트 설정
        binding.commentBtn.setOnClickListener {
            val comment = binding.commentEditText

            val pref : SharedPreferences = requireActivity().getSharedPreferences("save_state", 0)
            val id = pref.getString("idValue", null)


                    val uid = FirebaseAuth.getInstance().uid.toString()

                    if (!comment.text.isNullOrEmpty()) {
                        lateinit var newComment: Comment
                        post?.pid?.let { pid ->
                            newComment = dao.addComment(
                                pid,
                                comment.text.toString(),
                                uid,
                                object : CommunityDAO.AddCommentCallback {
                                    override fun onAddCommentComplete() {
                                        updateDataAndView()
                                    }
                                })
                        }

                        comment.text = null         // 댓글 등록 후 edit 창 비우기
                        val imm =
                            requireActivity().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(binding.commentEditText.windowToken, 0)
                    } else {
                        Toast.makeText(requireContext(), "댓글을 입력하세요.", Toast.LENGTH_SHORT).show()
                    }




        }

        // 게시글 수정 Fragment로 이동 (EditFragment)
        binding.updateText.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("pid", pid)

            val editBundleFragment = EditFragment()
            editBundleFragment.arguments = bundle
            parentFragmentManager.beginTransaction().replace(R.id.mainFrameLayout, editBundleFragment).addToBackStack(null).commit()
        }

        // 게시글 삭제 구현 (삭제 전 AlertDialog로 삭제 의사 물어보기)
        binding.deleteText.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.post_delete_dialog_title))
                .setMessage(getString(R.string.post_delete_dialog_message))
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                    dao.deletePost(post?.pid.toString(), post?.images, object : CommunityDAO.DeletePostCallback {
                        override fun onDeletePostComplete() {
                            val fragmentManager = requireActivity().supportFragmentManager
                            fragmentManager.popBackStack()
                        }
                    })
                })
                .setNegativeButton("취소", null)
                .show()
        }

        // 당겨서 새로고침 구현 (게시글 데이터 다시 가져오기)
        binding.postSwiper.setOnRefreshListener {
            updateDataAndView()
            binding.postSwiper.isRefreshing = false
        }
    }

//    댓글 리사이클러뷰 설정
    private fun initCommentRecycler() {
        commentAdapter = CommentAdapter(requireContext(), object: CommentAdapter.CommentDeletedListener {
            override fun onCommentDeleted() {
                updateDataAndView()
            }
        })
        commentRecyclerView = binding.commentRecyclerView

        commentRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        commentRecyclerView.adapter = commentAdapter

        commentAdapter.datas = comments

        for (comment in commentAdapter.datas) {
            Log.d(TAG, "comment : ${comment}")
        }

        commentAdapter.notifyDataSetChanged()
    }

    private fun initImageRecycler() {
        imageAdapter = MultiImageAdapter(requireContext(), requireFragmentManager())
        imageRecyclerView = binding.postImageRecycler

        imageRecyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        imageRecyclerView.adapter = imageAdapter

        imageAdapter.datas = images

        images.forEach {
            Log.d(TAG, "images에 저장된 uri : $it")
        }

        imageAdapter.datas.forEach {
            Log.d(TAG, "adapter data : ${it}")
        }

        imageAdapter.notifyDataSetChanged()
    }

//    화면 view 설정
    fun setView() {
        binding.titleTextView.text = post?.title
        binding.unameTextView.text = post?.uname
        binding.dateTextView.text = post?.date
        binding.contentTextView.text = post?.content
        binding.viewsTextView.text = post?.views.toString()
    }

//    게시글 내용을 업데이트한 후 화면에 반영
    fun updateDataAndView() {
        Log.d(TAG, "updateDataAndView !!")
        lifecycleScope.launch {
            post = withContext(Dispatchers.IO) {
                dao.updateViews(pid)
                dao.getPostById(pid)
            }

            post?.uname = withContext(Dispatchers.IO) {
                post?.uid?.let { dao.getUserNameByUid(it) }
            }

            if (post?.images != null) {
                images = MutableList(post?.images!!.size) {Uri.EMPTY}

                post?.images!!.forEachIndexed() { index, image ->
                    images[index] = post?.images!![index].toUri()
                    Log.d(TAG, "onViewCreated : ${images[index]}")
                }
            } else {
                images = mutableListOf()
            }
            initImageRecycler()

            if (!post?.comments.isNullOrEmpty()) {
                comments = post?.comments as ArrayList<Comment>
                comments?.sortBy { it.date }
            }
            comments.forEach { comment ->
                comment.uname = dao.getUserNameByUid(comment.uid!!)
            }
            initCommentRecycler()

            setView()

            val auth = FirebaseAuth.getInstance()
            if (auth.uid.isNullOrEmpty()) {
                binding.updateText.isGone = true
                binding.deleteText.isGone = true
            }

            if (!auth.uid.isNullOrEmpty()) {
                if (!auth.uid.equals(post?.uid)) {
                    binding.updateText.isGone = true
                    binding.deleteText.isGone = true
                }
            }
        }
    }
}
