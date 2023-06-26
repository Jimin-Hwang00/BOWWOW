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
import sklookie.bowwow.community.EditActivity
import sklookie.bowwow.dto.Comment
import sklookie.bowwow.dto.Post
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.collections.HashMap

class CommunityDAO {
    val TAG="CommunityDAO"

    val realtimeDB: FirebaseDatabase = FirebaseDatabase.getInstance()
    val postDBReference: DatabaseReference = realtimeDB.getReference("post")

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
    fun addPost(title: String, content: String, imageUris: MutableList<Uri>, callback: AddPostCallback): Post? {
       val postId: String? = postDBReference.push().key

        val post = Post()
        post.pid = postId
        post.title = title
        post.content = content
        post.date = date
        post.uid = "uid"        // @TODO 로그인 기능 구현 후 수정 필요. (uid)
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

            Tasks.whenAllComplete(uploadImageTasks).addOnSuccessListener {
                post.images = imageList

                val posted = postId?.let { postDBReference.child(it).setValue(post) }
                postId?.let { postDBReference.child(it).child("comments") }
                if (posted == null) {
                    Log.w(TAG, "게시물 저장에 실패했습니다.");
                }

                callback.onAddPostCompleted()
            }

            return post
        }

        val posted = postId?.let { postDBReference.child(it).setValue(post) }
        postId?.let { postDBReference.child(it).child("comments") }
        if (posted == null) {
            Log.w(TAG, "게시물 저장에 실패했습니다.");
        }

        callback.onAddPostCompleted()

        return post
    }

    interface EditPostCallback {
        fun onEditPostCompleted()
    }

//        게시글 수정 메소드
    fun editPost(pid: String, title: String, content: String, newImageUris: MutableList<Uri>, imageUris: MutableList<String>?, postImage: MutableList<String>?, callback: EditPostCallback) {
        val deleteImageTasks = mutableListOf<Task<*>>()

        val imageList = mutableListOf<String>()

        if (!imageUris.isNullOrEmpty()) {
            for (index in 0 until imageUris!!.size) {
                if (EditActivity.deletedImageIndex.get(index)) {
                    deleteImageTasks.add(deleteImage(imageUris[index]))
                    Log.d(TAG, "deleteImageTasks : ${imageUris[index]}")
                } else {
                    imageList.add(postImage!!.get(index))
                    Log.d(TAG, "image add : ${imageUris[index]}")
                }
            }
        }

        Tasks.whenAllComplete(deleteImageTasks).addOnSuccessListener {
            var titleUpdate: HashMap<String, Any> = HashMap()
            var contentUpdate: HashMap<String, Any> = HashMap()
            var imageUpdate: HashMap<String, Any> = HashMap()
            var dateUpdate: HashMap<String, Any> = HashMap()

            titleUpdate.put("title", title)
            contentUpdate.put("content", content)
            dateUpdate.put("date", date)

            if (!newImageUris.isNullOrEmpty()) {
                val uploadImageTasks = mutableListOf<Task<*>>()

                for (i in 0 until newImageUris.size) {
                    val imageName = System.currentTimeMillis()
                    val imgRef = postStorageRefence.child("${pid}/${imageName}")
                    val addImageTask = addImage(imgRef, newImageUris.get(i))

                    uploadImageTasks.add(addImageTask)

                    addImageTask.addOnSuccessListener {
                        imageList.add("${pid}/${imageName}")
                    }
                }

                Tasks.whenAllComplete(uploadImageTasks).addOnSuccessListener {
                    imageUpdate.put("images", imageList)
                    updatePostData(pid, titleUpdate, contentUpdate, imageUpdate, dateUpdate, callback)

                    callback.onEditPostCompleted()
                }
            } else {
                updatePostData(pid, titleUpdate, contentUpdate, imageUpdate, dateUpdate, callback)

                callback.onEditPostCompleted()
            }
        }

        callback.onEditPostCompleted()
    }

//    파이어베이스 게시글 update 반영 메소드
    fun updatePostData(pid: String, titleUpdate: HashMap<String, Any>, contentUpdate: HashMap<String, Any>, imageUpdate: HashMap<String, Any>, dateUpdate: HashMap<String, Any>, callback: EditPostCallback) {
        postDBReference.child(pid).updateChildren(titleUpdate)
        postDBReference.child(pid).updateChildren(contentUpdate)
        postDBReference.child(pid).updateChildren(imageUpdate)
        postDBReference.child(pid).updateChildren(dateUpdate)

        callback.onEditPostCompleted()
    }


    interface DeletePostCallback {
        fun onDeletePostComplete()
    }

//        게시글 삭제 메소드
    fun deletePost(pid: String, images: MutableList<String>?, callback: DeletePostCallback) {
        if (images != null && images.size != 0) {
            for (image in images!!) {
                deleteImage(image)
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
                postDBReference.child(pid).child("comments").push().key
        val comment = Comment(pid, commentKey, comment, date, uid)


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

//    파이어베이스 이미지 삭제 메소드
    fun deleteImage(url: String): Task<Void> {
        val image = postStorageRefence.child(url)
        return image.delete().addOnSuccessListener {
            Log.d(TAG, "이미지 삭제 완료 : post/${url}")
        }.addOnFailureListener {
            Log.d(TAG, "이미지 삭제 실패 : post/${url}")
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

    fun getComments(pid: String, callback: (MutableList<Comment>?) -> Unit) {
        val commentReference = postDBReference.child("post").child(pid).child("comments")

        try {
            commentReference.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val commentsMap = task.result?.value as? Map<String, Map<String, Any?>>
                    val comments = convertComments(commentsMap)
                    callback(comments)
                } else {
                    val error: String? = task.exception?.toString()
                    Log.w(TAG, "댓글 불러오기 오류 : $error")
                    callback(null)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "댓글 불러오기 오류", e)
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
                        uid = it["uid"] as? String
                    )
                }
                Log.d(TAG, "comment: ${comment}")
                comment?.let { comments.add(it) }
            }
            return comments
        }
        return mutableListOf()
    }
}