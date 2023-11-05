package sklookie.bowwow

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import sklookie.bowwow.databinding.FragmentMyInfoBinding
import sklookie.bowwow.databinding.FragmentMyInfoUpdateBinding

class MyInfoUpdateFragment : Fragment() {
    lateinit var MyInfoBinding: FragmentMyInfoUpdateBinding
    val database = Firebase.database
    val myRef = database.getReference("userInfo")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = arguments
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        MyInfoBinding = FragmentMyInfoUpdateBinding.inflate(inflater, container, false)
        return MyInfoBinding.root
    }
}