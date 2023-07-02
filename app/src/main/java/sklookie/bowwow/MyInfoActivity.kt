package sklookie.bowwow

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
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
    }
}