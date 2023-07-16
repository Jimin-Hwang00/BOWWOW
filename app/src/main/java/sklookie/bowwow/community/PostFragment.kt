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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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

            // pid를 통해 게시글 가져오기
            dao.getPostById(pid) { post ->
                if (post != null) {
                    this.post = post

                    Log.d("PostFragment", "post data : ${post}, pid : ${pid}")

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

                    dao.updateViews(pid, post.views!!.toInt())
                    val updatedViews = post.views!!.toInt() + 1
                    post.views = updatedViews.toString()

                    // 이미지 내용 images 변수에 넣기 (댓글 리사이클러뷰에 사용하기 위함)
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

                    // 댓글 내용 comments 변수에 넣기 (댓글 리사이클러뷰에 사용하기 위함)
                    if (!post?.comments.isNullOrEmpty()) {
                        comments = post?.comments as ArrayList<Comment>
                        comments?.sortBy { it.date }
                    }
                    initCommentRecycler()

                    setView()
                } else {
                    Toast.makeText(requireContext(), "게시글이 존재하지 않습니다.", Toast.LENGTH_SHORT).show()

                    val fragmentManager = requireActivity().supportFragmentManager
                    fragmentManager.popBackStack()
                }

            }
        }

        //  댓글 등록 버튼 클릭 이벤트 설정
        binding.commentBtn.setOnClickListener {
            val comment = binding.commentEditText

            val pref : SharedPreferences = requireActivity().getSharedPreferences("save_state", 0)
            val id = pref.getString("idValue", null)

            var googleInfo = GoogleInfo()
            dao.getGoogleInfoByID(id!!) {
                if (googleInfo != null) {
                    googleInfo = it

                    val uid = FirebaseAuth.getInstance().uid.toString()
                    val uname = "${googleInfo.familyName}${googleInfo.givenName}"

                    if (!comment.text.isNullOrEmpty()) {
                        lateinit var newComment: Comment
                        post?.pid?.let { pid ->
                            newComment = dao.addComment(
                                pid,
                                comment.text.toString(),
                                uid,
                                uname,
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
                } else {
                    Log.d(TAG, "Failed to get googleInfo")
                    Toast.makeText(requireContext(), "로그인이 제대로 되어 있는지 확인해주시기 바랍니다.", Toast.LENGTH_SHORT).show()
                }
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
        binding.swiper.setOnRefreshListener {
            updateDataAndView()
            binding.swiper.isRefreshing = false
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
        dao.getPostById(pid) {
            if (it != null) {
                post = it

                // 이미지 내용 images 변수에 넣기 (댓글 리사이클러뷰에 사용하기 위함)
                if (post?.images != null) {
                    images = MutableList(post?.images!!.size) {Uri.EMPTY}

                    post?.images!!.forEachIndexed() { index, image ->
                        images[index] = post?.images!![index].toUri()
                    }
                } else {
                    images = mutableListOf()
                }

                imageAdapter.datas = images
                imageAdapter.notifyDataSetChanged()

                // 댓글 내용 comments 변수에 넣기 (댓글 리사이클러뷰에 사용하기 위함)
                if (!post?.comments.isNullOrEmpty()) {
                    comments = post?.comments as ArrayList<Comment>
                    comments?.sortBy { it.date }
                }

                commentAdapter.datas = comments
                commentAdapter.notifyDataSetChanged()

                setView()
            } else {
                Toast.makeText(
                    requireContext(),
                    "게시글이 존재하지 않습니다.",
                    Toast.LENGTH_SHORT
                ).show()

                val fragmentManager = requireActivity().supportFragmentManager
                fragmentManager.popBackStack()
            }
        }
    }
}
