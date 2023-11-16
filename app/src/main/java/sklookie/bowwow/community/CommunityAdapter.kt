package sklookie.bowwow.community

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import sklookie.bowwow.R
import sklookie.bowwow.dto.Post

class CommunityAdapter(private val context: Context, listener: OnCommunityRecylerItemClick) : RecyclerView.Adapter<CommunityAdapter.ViewHolder>() {

    val TAG = "CommunityAdapter"

    var datas: MutableList<Post> = mutableListOf()

    private val mCallback = listener

    fun updateDatas(newDatas: MutableList<Post>) {
        datas = newDatas
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.community_item_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = datas!![position]

        Log.d(TAG, "onBindViewHolder data : ${data}")

        holder.itemView.findViewById<TextView>(R.id.tv_item_title).text = data.title
        holder.itemView.findViewById<TextView>(R.id.tv_item_content).text = data.content
        holder.itemView.findViewById<TextView>(R.id.tv_item_uname).text = data.uname

        if (!data.images.isNullOrEmpty()) {     // 해당 게시글에 이미지가 있을 경우
            holder.itemView.findViewById<ImageView>(R.id.iv_item_image).visibility = View.VISIBLE
        } else {                                // 해당 게시글에 이미지가 없을 경우
            holder.itemView.findViewById<ImageView>(R.id.iv_item_image).visibility = View.INVISIBLE
        }

        var date = data.date
        val subDate = date?.substring(0 until 11)
        holder.itemView.findViewById<TextView>(R.id.tv_item_date).text = subDate

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