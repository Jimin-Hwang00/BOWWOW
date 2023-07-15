package sklookie.bowwow.community

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import sklookie.bowwow.R
import sklookie.bowwow.dao.CommunityDAO
import sklookie.bowwow.dto.Comment

class CommentAdapter(private val context: Context, private val commentDeletedListener: CommentDeletedListener) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    var datas = mutableListOf<Comment>()
    val dao = CommunityDAO()

    override fun getItemCount(): Int {
        return datas.size
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private var view: View = v
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.comment_item_view, parent, false)
        return ViewHolder(view)
    }

    interface CommentDeletedListener {
        fun onCommentDeleted()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = datas[position]

        holder.itemView.findViewById<TextView>(R.id.comment_text).text = "${data.comment}\n"
        holder.itemView.findViewById<TextView>(R.id.comment_info_text).text = "${data.uname}  |  ${data.date}\n"

        // 삭제 이미지 클릭 이벤트 설정
        holder.itemView.findViewById<ImageView>(R.id.comment_delete_image).setOnClickListener {
            Log.d("CommentAdapter", "comment delete click -> pid : ${data.pid}, cid : ${data.cid}")
            dao.deleteComment(data.pid!!, data.cid!!, object : CommunityDAO.DeleteCommentCallback {
                override fun onDeleteCommentComplete() {
                    commentDeletedListener.onCommentDeleted()
                }
            })   // 데이터베이스에 삭제 반영
            datas.removeAt(position)                    // 어댑터 데이터에 삭제 반영
            notifyDataSetChanged()
        }
    }
}