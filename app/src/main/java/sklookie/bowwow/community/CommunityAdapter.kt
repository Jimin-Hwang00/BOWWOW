package sklookie.bowwow.community

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import sklookie.bowwow.R
import sklookie.bowwow.dto.Post

class CommunityAdapter(private val context: Context, listener: OnCommunityRecylerItemClick) : RecyclerView.Adapter<CommunityAdapter.ViewHolder>() {

    val TAG = "CommunityAdapter"

    var datas: MutableList<Post> = mutableListOf()

    val firebaseStorage = FirebaseStorage.getInstance()
    val rootRef = firebaseStorage.reference

    private val mCallback = listener

    fun updateDatas(newDatas: MutableList<Post>) {
        datas = newDatas
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = datas!![position]

        Log.d(TAG, "onBindViewHolder data : ${data}")

        holder.itemView.findViewById<TextView>(R.id.item_title_txtView).text = data.title
        holder.itemView.findViewById<TextView>(R.id.item_uid_txtView).text = data.uid

        val imageView = holder.itemView.findViewById<ImageView>(R.id.item_image_view)

        if (data.images.isNullOrEmpty()) {
            imageView.isInvisible = true
        } else {
            imageView.setImageBitmap(null)

            val imgRef = rootRef.child("post/${data.images!!.get(0)}")

            if (imgRef != null) {
                imgRef.downloadUrl.addOnSuccessListener {
                    Glide.with(context)
                        .load(it)
                        .into(imageView)
                }
            }
        }

        var date = data.date
        val subDate = date?.substring(0 until 11)
        holder.itemView.findViewById<TextView>(R.id.item_date_txtView).text = subDate

        holder.itemView.setOnClickListener {
            data.pid?.let { pid -> mCallback.onCommunityRecyclerItemClick(pid) }
        }
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private var view: View = v
    }

    //    게시글 검색
    fun findPostsByKeyword(keyword: String): List<Post> {
        var result = mutableListOf<Post>()

        for (post in datas) {
            if (post.content!!.contains(keyword) || post.title!!.contains(keyword)) {
                result.add(post)
            }
        }

        return result
    }

}