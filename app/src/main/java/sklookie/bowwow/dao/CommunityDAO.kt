package sklookie.bowwow.dao

import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class CommunityDAO {
    val TAG="PoastDAO"

    val db: FirebaseDatabase = FirebaseDatabase.getInstance()
    val dbReference: DatabaseReference = db.getReference("post")

    val sdf = SimpleDateFormat("yyyy-mm-dd HH:mm:ss")

    // 게시글 생성 메소드
    fun addPost(title: String, content: String) {
        val post = HashMap<String, Any>()

        post.put("title", title)
        post.put("content", content)
        post.put("date", sdf.format(Date(System.currentTimeMillis())))
        post.put("uid", "uid")     // 사용자 관련 구현 전이기 때문에 무조건 "uid" 문자열이 저장되도록 함. (추후에 수정 필요)

        Log.d("addPost data", "title: "+ title + ", content: " + content)
        Log.d("database reference", dbReference.toString())

        val posted = dbReference.push().setValue(post)
        if (posted == null) {
            Log.w(TAG, "게시물 저장에 실패했습니다.");
        }
    }

    // 게시글 수정 메소드
    fun editPost(pid: String, title: String, content: String) {
        var titleUpdate: HashMap<String, Any> = HashMap<String, Any>()
        var contentUpdate: HashMap<String, Any> = HashMap<String, Any>()
        var dateUpdate: HashMap<String, Any> = HashMap<String, Any>()

        titleUpdate.put("title", title)
        contentUpdate.put("content", content)
        dateUpdate.put("date", sdf.format(Date(System.currentTimeMillis())))

        dbReference.child(pid).updateChildren(titleUpdate)
        dbReference.child(pid).updateChildren(contentUpdate)
        dbReference.child(pid).updateChildren(dateUpdate)
    }

    // 게시글 삭제 메소드
    fun deletePost(pid: String) {
        dbReference.child(pid).removeValue()
    }
}