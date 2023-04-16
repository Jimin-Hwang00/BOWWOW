package sklookie.bowwow.community

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import sklookie.bowwow.R
import sklookie.bowwow.dto.Post

class PostAdapter(private val context: Context) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    var datas = mutableListOf<Post>()
    override fun getItemCount(): Int {
        Log.d("PostAdapter", datas.size.toString())
        return datas.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = datas[position]

        holder.itemView.findViewById<TextView>(R.id.item_title_txtView).text = data.title
        holder.itemView.findViewById<TextView>(R.id.item_uid_txtView).text = data.uid

        var date = data.date
        val subDate = date?.substring(0 until 11)
        holder.itemView.findViewById<TextView>(R.id.item_date_txtView).text = subDate

        holder.itemView.setOnClickListener{
            val post = Post(data.pid, data.title, data.content, data.date, data.uid)

            val intent = Intent(it.context, PostActivity::class.java)
            intent.putExtra("post", post)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private var view: View = v
    }
}
