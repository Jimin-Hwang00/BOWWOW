package sklookie.bowwow

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import sklookie.bowwow.databinding.ActivityMyInfoBinding

class MyInfoActivity : AppCompatActivity() {
    lateinit var MyInfoBinding : ActivityMyInfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyInfoBinding = ActivityMyInfoBinding.inflate(layoutInflater)
        setContentView(MyInfoBinding.root)

        val pref = getSharedPreferences("save_state", 0)
        val bellInfo = pref.getString("bellValue", null)
        val prefUNameInfo = pref.getString("nameValue", null)

        MyInfoBinding.uNameText.setText("${prefUNameInfo}님의 정보")
        MyInfoBinding.bellInfoText.setText("설정된 벨 : ${bellInfo}")

        var spinner = MyInfoBinding.spinner
        spinner.adapter = ArrayAdapter.createFromResource(this, R.array.bellList, android.R.layout.simple_spinner_item)

        MyInfoBinding.bellSaveBt.setOnClickListener{
            var updatedBellInfo = spinner.selectedItem.toString()

            val pref : SharedPreferences = getSharedPreferences("save_state", 0)
            val editor : SharedPreferences.Editor = pref.edit()
            editor.putString("bellValue", updatedBellInfo)
            editor.commit()

            Toast.makeText(this, "변경 완료됐습니다." , Toast.LENGTH_SHORT).show()
            MyInfoBinding.bellInfoText.setText("설정된 벨 : ${updatedBellInfo}")
        }

        val database = Firebase.database
        val myRef = database.getReference("userInfo")
        val id : String = intent.getStringExtra("id").toString()

        fun onDeleteContent(position: Int) {
            myRef.child(id).removeValue()
                .addOnSuccessListener(object : OnSuccessListener<Void?>{
                    override fun onSuccess(p0: Void?) {

                    }
                }).addOnFailureListener(object : OnFailureListener {
                    override fun onFailure(p0: java.lang.Exception) {

                    }
                })
        }
    }
}