package sklookie.bowwow.dao

import android.util.Log
import com.google.firebase.database.*
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.collections.HashMap

class CommunityDAO {
    val TAG="PoastDAO"

    val realtimeDB: FirebaseDatabase = FirebaseDatabase.getInstance()
    val postDBReference: DatabaseReference = realtimeDB.getReference("post")

    // 게시글 생성 메소드
    fun addPost(title: String, content: String, imageString: String) {
        val post = HashMap<String, Any>()

        val postId: String? = postDBReference.push().key

        post.put("title", title)
        post.put("content", content)
        post.put("date", Instant.ofEpochMilli(System.currentTimeMillis())
            .atOffset(ZoneOffset.ofHours(9))
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
        post.put("uid", "uid")     // TODO@ 사용자 관련 파트 구현 전이기 때문에 무조건 "uid" 문자열이 저장되도록 함. (추후에 수정 필요)
        post.put("views", "0")
        post.put("image", imageString)

        val posted = postId?.let { postDBReference.child(it).setValue(post) }
        postId?.let { postDBReference.child(it).child("comments") }
        if (posted == null) {
            Log.w(TAG, "게시물 저장에 실패했습니다.");
        }
    }

    // 게시글 수정 메소드
    fun editPost(pid: String, title: String, content: String, image: String) {
        var titleUpdate: HashMap<String, Any> = HashMap<String, Any>()
        var contentUpdate: HashMap<String, Any> = HashMap<String, Any>()
        var imageUpdate: HashMap<String, Any> = HashMap<String, Any>()
        var dateUpdate: HashMap<String, Any> = HashMap<String, Any>()

        titleUpdate.put("title", title)
        contentUpdate.put("content", content)
        imageUpdate.put("image", image)
        dateUpdate.put("date", Instant.ofEpochMilli(System.currentTimeMillis())
            .atOffset(ZoneOffset.ofHours(9))
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))

        postDBReference.child(pid).updateChildren(titleUpdate)
        postDBReference.child(pid).updateChildren(contentUpdate)
        postDBReference.child(pid).updateChildren(imageUpdate)
        postDBReference.child(pid).updateChildren(dateUpdate)
    }

    // 게시글 삭제 메소드
    fun deletePost(pid: String) {
        postDBReference.child(pid).removeValue()
    }

    // 조회수 업데이트 메소드
    fun updateViews(pid: String, views: Int) {
        var v = views

        var viewsUpdate: HashMap<String, Any> = HashMap<String, Any>()

        viewsUpdate.put("views", v++.toString())

        postDBReference.child(pid).updateChildren(viewsUpdate)
    }

    // 댓글 작성 메소드
    fun addComment(pid: String, comment: String, uid: String) {
        val commentHashMap = HashMap<String, Any>()

//        val commentId = postDBReference.child(pid).child("comments").push().key

        commentHashMap.put("pid", pid)
        commentHashMap.put("comment", comment)
        commentHashMap.put("date", Instant.ofEpochMilli(System.currentTimeMillis())
                .atOffset(ZoneOffset.ofHours(9))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
        commentHashMap.put("uid", uid)

        val commented = postDBReference.child(pid).child("comments").push().setValue(commentHashMap)
        if (commented == null) {
            Log.w(TAG, "게시물 저장에 실패했습니다.");
        }
    }

    fun deleteComment(pid: String, cid: String) {
        val result = postDBReference.child(pid).child("comments").child(cid).removeValue()
    }
}