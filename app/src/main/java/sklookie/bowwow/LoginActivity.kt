package sklookie.bowwow

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import sklookie.bowwow.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    var mGoogleSignInClient : GoogleSignInClient? = null
    lateinit var binding : ActivityLoginBinding
    var TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        var googleLoginLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == -1) {
                val data = result.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                getGoogleInfo(task)
            }
        }

        binding.googleLoginBt.setOnClickListener{
            val signInIntent = mGoogleSignInClient!!.signInIntent
            googleLoginLauncher.launch(signInIntent)
        }

        binding.notLoginBt.setOnClickListener{
            AlertDialog.Builder(this).run {
                setTitle("계정 없이 진행")
                setMessage("커뮤니티 기능 사용을 원할 시,\n로그인이 필요합니다. 괜찮습니까?")
                setNegativeButton("취소", null)
                setCancelable(false)
                setPositiveButton("확인", object: DialogInterface.OnClickListener{
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        val intent = Intent(this@LoginActivity, UserInfoActivity::class.java)
                        val database = Firebase.database
                        val myRef = database.getReference("userInfo").push()
                        intent.putExtra("id", myRef.key)
                        startActivity(intent)
                    }
                })
                show()
            }
        }
    }

    fun getGoogleInfo(completedTask: Task<GoogleSignInAccount>) {
        try {
            val TAG = "구글 로그인 결과"
            val account = completedTask.getResult(ApiException::class.java)
            val database = Firebase.database
            val myRef = database.getReference("userInfo").push()

            myRef.child("googleInfo").child("gId").setValue(account.id)
            myRef.child("googleInfo").child("familyName").setValue(account.familyName)
            myRef.child("googleInfo").child("givenName").setValue(account.givenName)
            myRef.child("googleInfo").child("email").setValue(account.email)

            Log.d(TAG, account.id!!)
            Log.d(TAG, account.familyName!!)
            Log.d(TAG, account.givenName!!)
            Log.d(TAG, account.email!!)

            //결과값이 존재하면 정보설정 화면으로 넘어감
            if(account.id.equals(null)) {
                Toast.makeText(this@LoginActivity, "로그인 실패했습니다.", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this@LoginActivity, "${account.familyName + account.givenName}님 환영합니다!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@LoginActivity, UserInfoActivity::class.java)
                intent.putExtra("id", myRef.key)
                startActivity(intent)
            }
        }
        catch (e: ApiException) {
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
        }
    }
}