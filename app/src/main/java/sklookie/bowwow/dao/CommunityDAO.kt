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
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import sklookie.bowwow.community.EditFragment
import sklookie.bowwow.dto.Comment
import sklookie.bowwow.dto.Post
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.collections.HashMap
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CommunityDAO {
    val TAG="CommunityDAO"

    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val postDbRef: DatabaseReference = db.getReference("post")
    private val userInfoDbRef = db.getReference("userInfo")

    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val postStorageRefence: StorageReference = storage.getReference("post")

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
    fun addPost(title: String, content: String, uid: String?, imageUris: MutableList<Uri>, callback: AddPostCallback) {
        val postId: String? = postDbRef.push().key
        Log.d(TAG, "addPost - postId : ${postId}")

        val post = Post()
        post.pid = postId
        post.title = title
        post.content = content
        post.date = date
        post.uid = uid
        post.views = "0"

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

                    val posted = postId?.let { postDbRef.child(it).setValue(post) }
                    postId?.let { postDbRef.child(it).child("comments") }
                    if (posted == null) {
                        Log.w(TAG, "게시물 저장에 실패했습니다.");
                    }

                    callback.onAddPostCompleted()
                }
            }
        } else {
            val posted = postId?.let { postDbRef.child(it).setValue(post) }
            postId?.let { postDbRef.child(it).child("comments") }
            if (posted == null) {
                Log.w(TAG, "게시물 저장에 실패했습니다.");
            }

            callback.onAddPostCompleted()
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
            postDbRef.child(pid).child("images").removeValue()        // 이미지가 없을 때 실시간 데이터베이스에 있는 이미지 DB를 삭제

            val updatePostTasks = updatePostData(pid, titleUpdate, contentUpdate, imageUpdate, dateUpdate)
            Tasks.whenAllComplete(updatePostTasks).addOnSuccessListener {
                callback.onEditPostCompleted()
            }
        }
    }

    //    파이어베이스 게시글 update 반영 메소드
    fun updatePostData(pid: String, titleUpdate: HashMap<String, Any>, contentUpdate: HashMap<String, Any>, imageUpdate: HashMap<String, Any>, dateUpdate: HashMap<String, Any>) : MutableList<Task<*>> {
        val updatePostTasks = mutableListOf<Task<*>>()

        val titleTask = postDbRef.child(pid).updateChildren(titleUpdate)
        val contentTask = postDbRef.child(pid).updateChildren(contentUpdate)
        val imageTask = postDbRef.child(pid).updateChildren(imageUpdate)
        val dateTask = postDbRef.child(pid).updateChildren(dateUpdate)

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

        val deleteTask = postDbRef.child(pid).removeValue()
        Tasks.whenAllComplete(deleteTask).addOnSuccessListener {
            callback.onDeletePostComplete()
        }
    }

    //        조회수 업데이트 메소드
    suspend fun updateViews(pid: String) {
        try {
            // 해당 게시물을 가져옵니다.
            val postReference = postDbRef.child(pid)
            val dataSnapshot = postReference.get().await()

            if (dataSnapshot.exists()) {
                // 게시물이 존재할 때만 업데이트를 진행합니다.
                val postMap: Map<String, Any?> =
                    dataSnapshot.value as? Map<String, Any?> ?: emptyMap()
                val post = Post(
                    pid = pid,
                    title = postMap["title"] as? String,
                    content = postMap["content"] as? String,
                    date = postMap["date"] as? String,
                    uid = postMap["uid"] as? String,
                    uname = "",
                    views = postMap["views"] as? String,
                    images = (postMap["images"] as? MutableList<String>) ?: null,
                    comments = convertComments(postMap["comments"])
                )
                if (post != null) {
                    val currentViews = post.views?.toInt() ?: 0
                    val updatedViews = currentViews + 1

                    // 업데이트할 데이터를 만듭니다.
                    val viewsUpdate: HashMap<String, Any> = HashMap()
                    viewsUpdate["views"] = updatedViews.toString()

                    // 업데이트를 수행합니다.
                    val result = postReference.updateChildren(viewsUpdate).await()
                    Log.d(TAG, "update views success! $result")
                } else {
                    Log.e(TAG, "Failed to parse post data")
                }
            } else {
                Log.e(TAG, "Post not found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "update views failed", e)
        }
    }


    interface AddCommentCallback {
        fun onAddCommentComplete()
    }

    //        댓글 작성 메소드
    fun addComment(pid: String, comment: String, uid: String, callback: AddCommentCallback): Comment {
        val commentHashMap = HashMap<String, Any>()

        val date = Instant.ofEpochMilli(System.currentTimeMillis())
            .atOffset(ZoneOffset.ofHours(9))
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        commentHashMap.put("pid", pid)
        commentHashMap.put("comment", comment)
        commentHashMap.put("date", date)
        commentHashMap.put("uid", uid)

        val commentKey =
            postDbRef.child(pid).child("comments").push().key
        val comment = Comment(pid, commentKey, comment, date, uid, "")  // uname은 저장되지 않음


        if (commentKey != null) {
            val task = postDbRef.child(pid).child("comments").child(commentKey)
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
        val task = postDbRef.child(pid).child("comments").child(cid).removeValue()
        Tasks.whenAllComplete(task).addOnSuccessListener {
            callback.onDeleteCommentComplete()
        }
    }

    //    게시글 반환 메소드
    suspend fun getAllPosts(): MutableList<Post>? = suspendCoroutine { continuation ->
        val posts = mutableListOf<Post>()

        postDbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    val postMap = childSnapshot.value as? Map<String, Any?>
                    if (postMap != null) {
                        val post = Post(
                            pid = childSnapshot.key,
                            title = postMap["title"] as? String,
                            content = postMap["content"] as? String,
                            date = postMap["date"] as? String,
                            uid = postMap["uid"] as? String,
                            uname = "",
                            views = postMap["views"] as? String,
                            images = postMap["images"] as? MutableList<String>,
                            comments = convertComments(postMap["comments"])
                        )
                        posts.add(post)
                    }
                }
                continuation.resume(posts)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, error.toString())
            }
        })
    }


    //    ID 값으로 게시글 반환 메소드
    suspend fun getPostById(pid: String): Post? = suspendCoroutine { continuation ->
        val postReference = postDbRef.child(pid)

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
                        uname = "",
                        views = postMap["views"] as? String,
                        images = (postMap["images"] as? MutableList<String>) ?: null,
                        comments = convertComments(postMap["comments"])
                    )
                    continuation.resume(post)
                } else {
                    val error: String? = task.exception?.toString()
                    Log.w(TAG, "게시물 불러오기 오류: $error")
                    continuation.resume(null)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "게시물 불러오기 오류", e)
            continuation.resume(null)
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
                        uname = ""
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
    suspend fun getUserNameByUid(uid: String): String = suspendCancellableCoroutine { continuation ->
        val query = userInfoDbRef.orderByChild("uid").equalTo(uid)

        query.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userNames = mutableListOf<String>()

                for (childSnapshot in snapshot.children) {
                    val userInfo = childSnapshot.child("userName").getValue(String::class.java)
                    if (userInfo != null) {
                        userNames.add(userInfo)
                    }
                }

                val result = if (userNames.isEmpty()) {
                    "사용자 없음"
                } else {
                    userNames[0]
                }

                continuation.resume(result)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, error.toString())
            }
        })
    }

    suspend fun getMyPosts(uid: String): List<Post> = suspendCancellableCoroutine { continuation ->
        val query = postDbRef.orderByChild("uid").equalTo(uid)

        query.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val posts = mutableListOf<Post>()

                for (childSnapshot in snapshot.children) {
                    val postMap = childSnapshot.value as? Map<String, Any?>
                    if (postMap != null) {
                        val post = Post(
                            pid = childSnapshot.key,
                            title = postMap["title"] as? String,
                            content = postMap["content"] as? String,
                            date = postMap["date"] as? String,
                            uid = postMap["uid"] as? String,
                            uname = "",
                            views = postMap["views"] as? String,
                            images = postMap["images"] as? MutableList<String>,
                            comments = convertComments(postMap["comments"])
                        )
                        posts.add(post)
                    }
                }

                continuation.resume(posts)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, error.toString())
            }
        })
    }

    suspend fun getMyComments(uid: String): List<Post> = suspendCancellableCoroutine { continuation ->
        val query = postDbRef.orderByChild("comments")

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val posts = mutableListOf<Post>()

                for (childSnapshot in snapshot.children) {
                    val postMap = childSnapshot.value as? Map<String, Any?>
                    if (postMap != null) {
                        val commentsMap = postMap["comments"] as? Map<String, Any?>
                        if (commentsMap != null) {
                            for (commentEntry in commentsMap.entries) {
                                val commentMap = commentEntry.value as? Map<String, Any?>
                                if (commentMap != null) {
                                    val commentUid = commentMap["uid"] as? String
                                    if (commentUid == uid) {
                                        val post = Post(
                                            pid = childSnapshot.key,
                                            title = postMap["title"] as? String,
                                            content = postMap["content"] as? String,
                                            date = postMap["date"] as? String,
                                            uid = postMap["uid"] as? String,
                                            uname = "",
                                            views = postMap["views"] as? String,
                                            images = postMap["images"] as? MutableList<String>,
                                            comments = convertComments(postMap["comments"])
                                        )
                                        posts.add(post)
                                        break
                                    }
                                }
                            }
                        }
                    }
                }

                continuation.resume(posts)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, error.toString())
            }
        })
    }

}