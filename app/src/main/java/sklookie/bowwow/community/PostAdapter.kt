package sklookie.bowwow.community

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import sklookie.bowwow.R
import sklookie.bowwow.dao.CommunityDAO
import sklookie.bowwow.dto.Post

class PostAdapter(private val context: Context) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    val TAG = "PostAdapter"

    lateinit var datas: MutableList<Post>

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
        val data = datas[position]

        holder.itemView.findViewById<TextView>(R.id.item_title_txtView).text = data.title
        holder.itemView.findViewById<TextView>(R.id.item_uid_txtView).text = data.uid

        val imageView = holder.itemView.findViewById<ImageView>(R.id.item_image_view)

        if (data.image.isNullOrBlank()) {
            imageView.isInvisible = true
        } else {
            imageView.setImageBitmap(null)

            val decodedByte = StringToBitmap(data.image!!)
            Glide.with(context)
                .load(decodedByte)
                .into(imageView)
        }

        var date = data.date
        val subDate = date?.substring(0 until 11)
        holder.itemView.findViewById<TextView>(R.id.item_date_txtView).text = subDate

        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, PostActivity::class.java)
            intent.putExtra("pid", data.pid)        // pid 값만 넘김 (PostActivity에서 해당 pid로 게시글 상세 내용 읽어옴)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private var view: View = v
    }

    fun StringToBitmap(string: String): Bitmap? {
        try {
            val encodeByte = Base64.decode(string, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)

            return bitmap
        } catch (e: java.lang.Exception) {
            Log.e("StringToBitmap", e.message.toString())
            return null;
        }
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