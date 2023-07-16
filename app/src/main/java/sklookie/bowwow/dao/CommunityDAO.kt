package sklookie.bowwow.dao

import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import sklookie.bowwow.community.EditFragment
import sklookie.bowwow.dto.Comment
import sklookie.bowwow.dto.GoogleInfo
import sklookie.bowwow.dto.Post
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.collections.HashMap

class CommunityDAO {
    val TAG="CommunityDAO"

    val realtimeDB: FirebaseDatabase = FirebaseDatabase.getInstance()

    val postDBReference: DatabaseReference = realtimeDB.getReference("post")
    val userInfoDB = realtimeDB.getReference("userInfo")

    val storage: FirebaseStorage = FirebaseStorage.getInstance()
    val postStorageRefence: StorageReference = storage.getReference("post")

    val date = Instant.ofEpochMilli(System.currentTimeMillis())
        .atOffset(ZoneOffset.ofHours(9))
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

//    파이어베이스 이미지 저장 메소드
    fun addImage(imgRef: StorageReference, uri: Uri): StorageTask<UploadTask.TaskSnapshot> {
        return imgRef.putFile(uri)
            .addOnSuccessListener {
                Log.d(TAG, "이미지 업로드 성공")
            }
            .addOnFailureListener {
                Log.d(TAG, "이미지 업로드 실패")
            }
    }

    interface AddPostCallback {
        fun onAddPostCompleted()
    }

//        게시글 생성 메소드
    fun addPost(title: String, content: String, uid: String?, uname: String?, imageUris: MutableList<Uri>, callback: AddPostCallback) {
       val postId: String? = postDBReference.push().key

        val post = Post()
        post.pid = postId
        post.title = title
        post.content = content
        post.date = date
        post.uid = uid
        post.uname = uname
        post.views = "0"

        Log.d(TAG, "addPost -> uname : ${uname}")

        val imageList = mutableListOf<String>()

        if (!imageUris.isNullOrEmpty()) {
            val uploadImageTasks = mutableListOf<Task<*>>()
            for (i in 0 until imageUris.size) {
                val imageName = System.currentTimeMillis()
                val imgRef = postStorageRefence.child("${postId}/${imageName}")
                Log.d(TAG, "이미지 저장 : ${postId}/${imageName}" )
                val addImageTask = addImage(imgRef, imageUris.get(i))

                uploadImageTasks.add(addImageTask)

                addImageTask.addOnSuccessListener {
                    imageList.add("${postId}/${imageName}")
                }
            }

            val downloadedUris = mutableListOf<String>()
            val downloadImageUriTasks = ArrayList<Task<Uri>>()
            Tasks.whenAllComplete(uploadImageTasks).addOnSuccessListener {
                imageList.forEach { imageName ->
                    val uriTask = postStorageRefence.child(imageName).downloadUrl.addOnSuccessListener {uri ->
                        downloadedUris.add(uri.toString())
                        Log.d(TAG, "downloaded uris : ${uri}")
                    }
                    downloadImageUriTasks.add(uriTask)
                }

                Tasks.whenAllComplete(downloadImageUriTasks).addOnSuccessListener {
                    post.images = downloadedUris

                    post.images!!.forEach {
                        Log.d(TAG, "post.images : ${it}")
                    }

                    val posted = postId?.let { postDBReference.child(it).setValue(post) }
                    postId?.let { postDBReference.child(it).child("comments") }
                    if (posted == null) {
                        Log.w(TAG, "게시물 저장에 실패했습니다.");
                    }

                    callback.onAddPostCompleted()
                }
            }
        }
    }

    interface EditPostCallback {
        fun onEditPostCompleted()
    }

//        게시글 수정 메소드
    fun editPost(pid: String, title: String, content: String, imageUris: MutableList<Uri>, callback: EditPostCallback) {
        val deleteImageTasks = mutableListOf<Task<*>>()

        val imageList = mutableListOf<String>()

        // 삭제한 이미지 Uri 처리
        EditFragment.deletedImageUri.forEach { uri ->
            try {
                val imgRef = storage.getReferenceFromUrl(uri)
                if (imgRef != null) {                               // 삭제한 이미지가 Storage에 있을 때
                    val deleteTask = imgRef.delete().addOnSuccessListener {
                        Log.d(TAG, "이미지 삭제 완료 : ${uri}")
                    }.addOnFailureListener {
                        Log.d(TAG, "이미지 삭제 실패 : ${uri}")
                    }
                    deleteImageTasks.add(deleteTask)
                }
            } catch (e: java.lang.IllegalArgumentException) {       // 삭제된 이미지가 서버에 올라간 이미지가 아닐 때 오류 catch
                Log.d(TAG, "editPost : 이미지 삭제 에러 (${e})")
            }
        }

        var titleUpdate: HashMap<String, Any> = HashMap()
        var contentUpdate: HashMap<String, Any> = HashMap()
        var imageUpdate: HashMap<String, Any> = HashMap()
        var dateUpdate: HashMap<String, Any> = HashMap()

        titleUpdate.put("title", title)
        contentUpdate.put("content", content)
        dateUpdate.put("date", date)

        if (!imageUris.isNullOrEmpty()) {
            val uploadImageTasks = mutableListOf<Task<*>>()

            imageUris.forEach { uri ->
                try {
                    val imgRef = storage.getReferenceFromUrl(uri.toString())
                    if (imgRef != null) {                               // 이미지가 이미 Storage에 있을 때
                        imageList.add(uri.toString())
                    }
                } catch (e: java.lang.IllegalArgumentException) {       // 이미지가 Storage에 없을 때 (새로운 이미지 등록)
                    val imageName = System.currentTimeMillis()
                    val newImgRef = postStorageRefence.child("${pid}/${imageName}")
                    val uploadTask = addImage(newImgRef, uri)
                    uploadImageTasks.add(uploadTask)

                    Tasks.whenAllComplete(uploadTask).addOnSuccessListener {        // 이미지가 Storage에 업로드 완료되었을 때
                        val uriTask = postStorageRefence.child("${pid}/${imageName}").downloadUrl.addOnSuccessListener {uri ->
                            imageList.add(uri.toString())
                        }

                        Tasks.whenAllComplete(uriTask).addOnSuccessListener {       // 이미지의 Uri를 다운로드 완료했을 때
                            if (imageList.size == imageUris.size) {                                        // 모든 이미지를 처리했는지 확인
                                imageUpdate.put("images", imageList)

                                val updatePostTask = updatePostData(pid, titleUpdate, contentUpdate, imageUpdate, dateUpdate)

                                Tasks.whenAllComplete(updatePostTask).addOnSuccessListener {
                                    callback.onEditPostCompleted()
                                }
                            }
                        }
                    }
                }
            }
            if (imageList.size == imageUris.size) {               // 모든 이미지를 처리했는지 확인
                imageUpdate.put("images", imageList)

                val updatePostTasks = updatePostData(pid, titleUpdate, contentUpdate, imageUpdate, dateUpdate)

                Tasks.whenAllComplete(updatePostTasks).addOnSuccessListener {
                    callback.onEditPostCompleted()
                }
            }
        } else {
            postDBReference.child(pid).child("images").removeValue()        // 이미지가 없을 때 실시간 데이터베이스에 있는 이미지 DB를 삭제

            val updatePostTasks = updatePostData(pid, titleUpdate, contentUpdate, imageUpdate, dateUpdate)
            Tasks.whenAllComplete(updatePostTasks).addOnSuccessListener {
                callback.onEditPostCompleted()
            }
        }
    }

//    파이어베이스 게시글 update 반영 메소드
    fun updatePostData(pid: String, titleUpdate: HashMap<String, Any>, contentUpdate: HashMap<String, Any>, imageUpdate: HashMap<String, Any>, dateUpdate: HashMap<String, Any>) : MutableList<Task<*>> {
        val updatePostTasks = mutableListOf<Task<*>>()

        val titleTask = postDBReference.child(pid).updateChildren(titleUpdate)
        val contentTask = postDBReference.child(pid).updateChildren(contentUpdate)
        val imageTask = postDBReference.child(pid).updateChildren(imageUpdate)
        val dateTask = postDBReference.child(pid).updateChildren(dateUpdate)

        updatePostTasks.add(titleTask)
        updatePostTasks.add(contentTask)
        updatePostTasks.add(imageTask)
        updatePostTasks.add(dateTask)

        return updatePostTasks
    }


    interface DeletePostCallback {
        fun onDeletePostComplete()
    }

//        게시글 삭제 메소드
    fun deletePost(pid: String, images: MutableList<String>?, callback: DeletePostCallback) {
        if (images != null) {
            images.forEach { uri ->
                val imgRef = storage.getReferenceFromUrl(uri)
                if (imgRef != null) {
                    imgRef.delete().addOnSuccessListener {
                        Log.d(TAG, "이미지 삭제 완료 : ${uri}")
                    }.addOnFailureListener {
                        Log.d(TAG, "이미지 삭제 실패 : ${uri}")
                    }
                }
            }
        }

        val deleteTask = postDBReference.child(pid).removeValue()
        Tasks.whenAllComplete(deleteTask).addOnSuccessListener {
            callback.onDeletePostComplete()
        }
   }

//        조회수 업데이트 메소드
    fun updateViews(pid: String, views: Int) {
        var v = views

        val viewsUpdate: HashMap<String, Any> = HashMap()

        viewsUpdate["views"] = (++v).toString()

        val result = postDBReference.child(pid).updateChildren(viewsUpdate)
        result.addOnSuccessListener {
            Log.d(TAG, "update views success! ${it.toString()}")
        }
    }

    interface AddCommentCallback {
         fun onAddCommentComplete()
    }

//        댓글 작성 메소드
    fun addComment(pid: String, comment: String, uid: String, uname: String, callback: AddCommentCallback): Comment {
        val commentHashMap = HashMap<String, Any>()

        val date = Instant.ofEpochMilli(System.currentTimeMillis())
            .atOffset(ZoneOffset.ofHours(9))
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        commentHashMap.put("pid", pid)
        commentHashMap.put("comment", comment)
        commentHashMap.put("date", date)
        commentHashMap.put("uid", uid)

        val commentKey =
                postDBReference.child(pid).child("comments").push().key
        val comment = Comment(pid, commentKey, comment, date, uid, uname)


        if (commentKey != null) {
            val task = postDBReference.child(pid).child("comments").child(commentKey)
                .setValue(comment)

            Tasks.whenAllComplete(task).addOnSuccessListener {
                callback.onAddCommentComplete()
            }
        } else {
            Log.d(TAG, "게시글 저장 실패")
        }

        return comment
    }

    interface DeleteCommentCallback {
        fun onDeleteCommentComplete()
    }

//        댓글 삭제 메소드
    fun deleteComment(pid: String, cid: String, callback : DeleteCommentCallback) {
       val task = postDBReference.child(pid).child("comments").child(cid).removeValue()
        Tasks.whenAllComplete(task).addOnSuccessListener {
            callback.onDeleteCommentComplete()
        }
    }

//    게시글 반환 메소드
    fun getPosts(callback: (List<Post>?) -> Unit) {
        val posts = mutableListOf<Post>()

        try {
            postDBReference.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val dataSnapshot: DataSnapshot? = task.result
                    if (dataSnapshot != null && dataSnapshot.exists()) {
                        for (postSnapshot in dataSnapshot.children) {
                            val postMap: Map<String, Any?> =
                                postSnapshot.value as? Map<String, Any?>
                                    ?: continue
                            val post = Post(
                                pid = postSnapshot.key,
                                title = postMap["title"] as? String,
                                content = postMap["content"] as? String,
                                date = postMap["date"] as? String,
                                uid = postMap["uid"] as? String,
                                uname = postMap["uname"] as? String,
                                views = postMap["views"] as? String,
                                images = (postMap["images"] as? MutableList<String>)
                                    ?: null,
                                comments = convertComments(postMap["comments"])
                            )
                            posts?.add(post)
                        }
                        callback(posts)
                    } else {
                        Log.d(TAG, "게시글이 없습니다.")
                        callback(null)
                    }
                } else {
                    val error: String? = task.exception?.toString()
                    Log.d(TAG, "게시글 읽기 실패: $error")
                    callback(null)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "게시글 읽기 오류", e)
            callback(null)
        }
    }

//    ID 값으로 게시글 반환 메소드
    fun getPostById(pid: String, callback: (Post?) -> Unit) {
        val postReference = postDBReference.child(pid)

        try {
            postReference.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val dataSnapshot = task.result
                    val postMap: Map<String, Any?> =
                        dataSnapshot.value as? Map<String, Any?> ?: emptyMap()
                    val post = Post(
                        pid = pid,
                        title = postMap["title"] as? String,
                        content = postMap["content"] as? String,
                        date = postMap["date"] as? String,
                        uid = postMap["uid"] as? String,
                        uname = postMap["uname"] as? String,
                        views = postMap["views"] as? String,
                        images = (postMap["images"] as? MutableList<String>) ?: null,
                        comments = convertComments(postMap["comments"])
                    )
                    callback(post)
                } else {
                    val error: String? = task.exception?.toString()
                    Log.w(TAG, "게시물 불러오기 오류: $error")
                    callback(null)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "게시물 불러오기 오류", e)
            callback(null)
        }
    }

//   HashMap을 Comment 객체로 치환
    private fun convertComments(commentsMap: Any?): MutableList<Comment> {
        if (commentsMap is Map<*, *>) {
            val comments = mutableListOf<Comment>()
            for (commentEntry in commentsMap.entries) {
                val commentId = commentEntry.key as? String
                val commentMap = commentEntry.value as? Map<String, Any?>
                val comment = commentMap?.let {
                    Comment(
                        cid = commentId,
                        comment = it["comment"] as? String,
                        date = it["date"] as? String,
                        pid = it["pid"] as? String,
                        uid = it["uid"] as? String,
                        uname = it["uname"] as? String
                    )
                }
                Log.d(TAG, "comment: ${comment}")
                comment?.let { comments.add(it) }
            }
            return comments
        }
        return mutableListOf()
    }

//    ID 값을 통한 GoogleInfo 반환 메소드
    fun getGoogleInfoByID(id: String, callback: (GoogleInfo) -> Unit) {
        userInfoDB.child(id).child("googleInfo").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val dataSnapshot = task.result

                val familyName = dataSnapshot.child("familyName").getValue(String::class.java)
                val givenName = dataSnapshot.child("givenName").getValue(String::class.java)
                val uid = dataSnapshot.child("uid").getValue(String::class.java)

                Log.d(TAG, "googleInfo: $uid, $familyName, $givenName")

                val googleInfo = GoogleInfo()
                googleInfo.familyName = familyName.toString()
                googleInfo.givenName = givenName.toString()
                googleInfo.uid = uid.toString()

                Log.d(TAG, "return googleInfo: ${googleInfo.uid}, ${googleInfo.familyName}, ${googleInfo.givenName}")

                callback(googleInfo)
            } else {
                Log.d(TAG, "구글 계정 정보 불러오기 실패")
            }
        }
    }
}