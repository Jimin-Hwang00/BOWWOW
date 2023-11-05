package sklookie.bowwow

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import sklookie.bowwow.databinding.FragmentMainHomeBinding

const val TAG_MAINHOME = "main_home_fragment"

class MainHomeFragment : Fragment() {
    private lateinit var binding: FragmentMainHomeBinding
    private lateinit var id: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = arguments
        if (bundle != null) {
            id = bundle.getString("key").toString()
        }

        Log.d("MainHomeFragment", "전달 받은 key: ${id}")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = Firebase.database
        val myRef = database.getReference("userInfo")

        Log.d("MainHomeFragment", "id : ${id}")

        val pref: SharedPreferences = requireContext().getSharedPreferences("save_state", 0)
        binding.userName.setText("${pref.getString("nameValue", null)}님")
        binding.bellText.setText("${pref.getString("dogValue", null)}이의 \n벨훈련")
    }
}