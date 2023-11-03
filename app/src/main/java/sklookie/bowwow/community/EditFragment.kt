package sklookie.bowwow.community

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sklookie.bowwow.dao.CommunityDAO
import sklookie.bowwow.databinding.FragmentEditBinding
import sklookie.bowwow.dto.Post

class EditFragment : Fragment() {
    private lateinit var binding: FragmentEditBinding

    private val TAG = "EditFragment"

    private lateinit var post: Post
    private lateinit var pid: String

    private val dao = CommunityDAO()

    private lateinit var images: MutableList<Uri>

    private lateinit var imageAdapter: MultiImageAdapter
    private lateinit var imageRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEditBinding.inflate(inflater, container, false)
        pid = arguments?.getString("pid").toString()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "intentPid : ${pid}")

        // 이미지 추가 버튼 클릭 리스너 구현 (갤러리 접근)
        binding.addImageBtn.setOnClickListener {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)      // 퍼미션 요구 (1회만)

            if (ContextCompat.checkSelfPermission(requireContext().applicationContext, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(Intent.ACTION_PICK)
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE)
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, 2222)
            } else {
                Toast.makeText(requireContext(), "갤러리 접근 권한이 거부돼 있습니다. 설정에서 접근을 허용해 주세요.", Toast.LENGTH_SHORT).show()
            }

        }

        // pid를 통해 게시글 불러오기
        lifecycleScope.launch {
            post = withContext(Dispatchers.IO) {
                dao.getPostById(pid)!!
            }

            if (post?.images != null) {
                images = MutableList(post?.images!!.size) {Uri.EMPTY}

                post?.images!!.forEachIndexed() { index, image ->
                    images[index] = post?.images!![index].toUri()
                }
            } else {
                images = mutableListOf()
            }

            initImageRecycler()

            setView()
        }


        // 수정된 게시글 저장 버튼 클릭 리스너 구현 (파이어베이스 데이터 수정)
        binding.postUpdateBtn.setOnClickListener {
            val title = binding.titleEditText.text.toString()
            val content = binding.contentEditText.text.toString()

            if (title.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "제목을 작성해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                // 게시글 수정
                dao.editPost(post?.pid.toString(), title, content, images, object :
                    CommunityDAO.EditPostCallback {
                    override fun onEditPostCompleted() {
                        // 수정이 완료된 후에 호출되는 콜백 함수
                        fragmentManager?.popBackStack()
                    }
                }
                )
            }
        }
    }

//     화면 view 설정
    fun setView() {
        binding.titleEditText.setText(post?.title)
        binding.contentEditText.setText(post?.content)
    }

//     MultiImageAdapter와 이미지 RecyclerView 연결
    fun initImageRecycler() {
        imageAdapter = MultiImageAdapter(requireContext(), requireFragmentManager())
        imageRecyclerView = binding.addImageRecycler

        imageRecyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        imageRecyclerView.adapter = imageAdapter

        imageAdapter.datas = images

        imageAdapter.notifyDataSetChanged()
    }

//    게시글 이미지 선택 후
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data == null) {
            Toast.makeText(requireContext(), "이미지를 선택하지 않았습니다.", Toast.LENGTH_SHORT).show()
        } else {
            if (data.getClipData() == null) {
                val imageUri = data.data
                imageUri?.let {
                    images.add(it)
                }
            } else {
                val clipData = data.clipData

                if (clipData!!.itemCount + images.size > 3) {        // 선택된 이미지가 총 4장 이상일 때
                    Toast.makeText(requireContext(), "사진은 최대 3장까지 선택 가능합니다.", Toast.LENGTH_SHORT).show()
                } else {
                    for (i in 0 until clipData.itemCount) {
                        val imageUri = clipData.getItemAt(i).uri

                        try {
                            images.add(imageUri)
                        } catch (e: Exception) {
                            Log.e(TAG, "File select error", e)
                        }
                    }
                }
            }

            imageAdapter.datas = images
            imageAdapter.notifyDataSetChanged()

            for (imageUri in imageAdapter.datas) {
                Log.d(TAG, "imageURI : ${imageUri}")
            }
        }
    }

    companion object {
        var deletedImageUri : MutableList<String> = mutableListOf()     // 삭제된 이미지 Uri 담는 변수
    }
}