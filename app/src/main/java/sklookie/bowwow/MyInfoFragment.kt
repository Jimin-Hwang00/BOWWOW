package sklookie.bowwow

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import sklookie.bowwow.community.EditFragment
import sklookie.bowwow.databinding.FragmentMyInfoBinding

class MyInfoFragment : Fragment() {
    lateinit var MyInfoBinding : FragmentMyInfoBinding
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
        MyInfoBinding = FragmentMyInfoBinding.inflate(inflater, container, false)
        return MyInfoBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pref: SharedPreferences = requireContext().getSharedPreferences("save_state", 0)
        val uName = pref.getString("nameValue", null).toString()
        val device = pref.getString("deviceValue", null).toString()
        val bell = pref.getString("bellValue", null).toString()
        val dogName = pref.getString("dogValue", null).toString()

        MyInfoBinding.uNameText.setText("${uName}님의 정보")
        MyInfoBinding.bellInfoText.setText("설정된 벨 : ${bell}")
        MyInfoBinding.deviceInfoText.setText("설정된 장치: ${device}")
        MyInfoBinding.uNameInfoText.setText("반려인 이름: ${uName}")
        MyInfoBinding.dogNameInfoText.setText("반려견 이름: ${dogName}")

        MyInfoBinding.updateInfoBt.setOnClickListener{
            val bundle = Bundle()

            val myInfoUpdateFragment = MyInfoUpdateFragment()
            myInfoUpdateFragment.arguments = bundle // bundle을 MyInfoUpdateFragment로 전달

            parentFragmentManager.beginTransaction()
                .replace(R.id.mainFrameLayout, myInfoUpdateFragment) // myInfoUpdateFragment를 사용
                .addToBackStack(null)
                .commit()
        }

        MyInfoBinding.resetBt.setOnClickListener{
            AlertDialog.Builder(requireContext()).run {
                setTitle("계정 삭제")
                setIcon(R.drawable.warning)
                setMessage("정말로 계정을 삭제하시겠습니까?")
                setNegativeButton("취소", null)
                setCancelable(false)
                setPositiveButton("확인", object: DialogInterface.OnClickListener{
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        onDeleteAccount()
                    }
                })
                show()
            }
        }
    }

    fun onDeleteAccount() {
        val pref: SharedPreferences = requireContext().getSharedPreferences("save_state", 0)
        val id = pref.getString("idValue", null).toString()

        myRef.child(id).removeValue()
            .addOnSuccessListener(object : OnSuccessListener<Void?> {
                override fun onSuccess(p0: Void?) {
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    val pref: SharedPreferences = requireContext().getSharedPreferences("save_state", 0)
                    val editor : SharedPreferences.Editor = pref.edit()
                    editor.remove("nameValue")
                    editor.remove("bellValue")
                    editor.remove("idValue")
                    editor.remove("dogValue")
                    editor.commit()

                    Toast.makeText(requireContext(), "계정 삭제가 완료됐습니다.", Toast.LENGTH_SHORT).show()
                    startActivity(intent)
                }
            }).addOnFailureListener(object : OnFailureListener {
                override fun onFailure(p0: java.lang.Exception) {
                    Toast.makeText(requireContext(), "계정 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            })
    }
}