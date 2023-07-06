package sklookie.bowwow.community

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import sklookie.bowwow.R
import sklookie.bowwow.dao.CommunityDAO
import sklookie.bowwow.databinding.FragmentAddBinding
import kotlin.concurrent.fixedRateTimer

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
        setHasOptionsMenu(true)

        initImageRecycler()

        imageAdapter.setOnItemDeleteListener(object : MultiImageAdapter.OnImageDeleteListener {
            override fun onImageDeleted(position: Int) {
                imageUris = imageAdapter.datas
            }
        })

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
    }

    fun initImageRecycler() {
        imageAdapter = MultiImageAdapter(requireContext(), requireFragmentManager())
        imageRecyclerView = binding.addImageRecycler

        imageRecyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        imageRecyclerView.adapter = imageAdapter

        imageAdapter.datas = imageUris

        imageAdapter.notifyDataSetChanged()
    }

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

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.menu_add_post, menu)
    }

    //    옵션 메뉴 - 게시글 저장 기능
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId) {
        R.id.menu_add_save -> {
            if (binding.titleEditText.text.isNullOrEmpty()) {       // 제목이 입력되지 않았을 경우 Toast 띄움
                Toast.makeText(requireContext(), "제목을 입력하세요.", Toast.LENGTH_SHORT).show()
            } else {
                dao.addPost(
                    binding.titleEditText.text.toString(),
                    binding.contentEditText.text.toString(),
                    imageUris,
                    object: CommunityDAO.AddPostCallback {
                        override fun onAddPostCompleted() {
                            fragmentManager?.popBackStack()
                        }

                    }
                )
            }
            true
        }
        else -> true
    }
}