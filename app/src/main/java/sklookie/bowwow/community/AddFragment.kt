package sklookie.bowwow.community

import android.content.Intent
import android.content.SharedPreferences
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import sklookie.bowwow.dao.CommunityDAO
import sklookie.bowwow.databinding.FragmentAddBinding
import sklookie.bowwow.dto.GoogleInfo

class AddFragment : Fragment() {
    val TAG = "AddFragment"

    private lateinit var binding: FragmentAddBinding
    private val dao = CommunityDAO()

    private var imageUris = mutableListOf<Uri>()

    private lateinit var imageAdapter: MultiImageAdapter
    private lateinit var imageRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initImageRecycler()

        //        이미지를 삭제했을 때 동작할 내용
        imageAdapter.setOnItemDeleteListener(object : MultiImageAdapter.OnImageDeleteListener {
            override fun onImageDeleted(position: Int) {
                imageUris = imageAdapter.datas
            }
        })

        // 이미지 추가 버튼 클릭 리스너 구현 (갤러리 접근)
        binding.addImageBtn.setOnClickListener {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )      // 퍼미션 요구 (1회만)

            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val intent = Intent(Intent.ACTION_PICK)
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE)
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, 2222)
            } else {
                Toast.makeText(
                    requireContext(),
                    "갤러리 접근 권한이 거부돼 있습니다. 설정에서 접근을 허용해 주세요.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // 게시글 저장 버튼 클릭 리스너 구현 (파이어베이스에 게시글 저장)
        binding.postSaveBtn.setOnClickListener {
            val auth = FirebaseAuth.getInstance()

            Log.d("AddFragment", "auth.uid : ${auth.uid}")

            val pref : SharedPreferences = requireActivity().getSharedPreferences("save_state", 0)
            val id = pref.getString("idValue", null)

            var googleInfo = GoogleInfo()

            val title = binding.titleEditText.text

            if (title.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                dao.getGoogleInfoByID(id!!) {result ->
                    if (googleInfo != null) {
                        googleInfo = result

                        val uname = "${googleInfo?.familyName}${googleInfo?.givenName}"
                        dao.addPost(
                            binding.titleEditText.text.toString(),
                            binding.contentEditText.text.toString(),
                            auth.uid,
                            uname,
                            imageUris,
                            object: CommunityDAO.AddPostCallback {
                                override fun onAddPostCompleted() {
                                    fragmentManager?.popBackStack()
                                }
                            }
                        )
                    } else {
                        Log.d(TAG, "Failed to get googleInfo")
                        Toast.makeText(requireContext(), "로그인이 제대로 되어 있는지 확인해주시기 바랍니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

//    이미지 리사이클러뷰와 어댑터 연결 작업
    fun initImageRecycler() {
        imageAdapter = MultiImageAdapter(requireContext(), requireFragmentManager())
        imageRecyclerView = binding.addImageRecycler

        imageRecyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        imageRecyclerView.adapter = imageAdapter

        imageAdapter.datas = imageUris

        imageAdapter.notifyDataSetChanged()
    }

//    갤러리에서 이미지 선택 후 동작
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data == null) {
            Toast.makeText(requireContext(), "이미지를 선택하지 않았습니다.", Toast.LENGTH_SHORT).show()
        } else {
            if (data.getClipData() == null) {
                val imageUri = data.data
                imageUri?.let { imageUris.add(it) }
            } else {
                val clipData = data.clipData

                if (clipData!!.itemCount + imageUris.size > 3) {
                    Toast.makeText(requireContext(), "사진은 최대 3장까지 선택 가능합니다.", Toast.LENGTH_SHORT).show()
                } else {
                    for (i in 0 until clipData.itemCount) {
                        val imageUri = clipData.getItemAt(i).uri

                        try {
                            imageUris.add(imageUri)
                        } catch (e: Exception) {
                            Log.e(TAG, "File select error", e)
                        }
                    }
                }
            }

            imageAdapter.datas = imageUris
            imageAdapter.notifyDataSetChanged()

            for (imageUri in imageAdapter.datas) {
                Log.d(TAG, "imageURI : ${imageUri}")
            }
        }
    }
}