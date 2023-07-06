package sklookie.bowwow.community

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.provider.ContactsContract.CommonDataKinds.Im
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ImageView
import androidx.core.view.isGone
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import sklookie.bowwow.R

class MultiImageAdapter(private val context: Context, fragmentManager: FragmentManager): RecyclerView.Adapter<MultiImageAdapter.ViewHolder>() {
    var datas = mutableListOf<Uri>()

    private var onItemDeleteListener: OnImageDeleteListener? = null

    private val fragmentManager = fragmentManager

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private var view: View = v
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.image_recycler_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = datas[position]

        if (data != Uri.EMPTY) {
            Log.d("MultiImageAdapter", "data : ${data}")
            Glide.with(context)
                .load(data)
                .into(holder.itemView.findViewById(R.id.image_recycler_view))
        } else {
            holder.itemView.findViewById<ImageView>(R.id.image_recycler_view).isGone
        }

        val fragment = fragmentManager.findFragmentById(R.id.mainFrameLayout)
//        게시글 추가, 수정하는 액티비티일 때 이미지 롱클릭 이벤트 생성 (이미지 삭제 기능)
        if ((fragment is EditFragment) or (fragment is AddFragment)) {
            holder.itemView.findViewById<ImageView>(R.id.image_recycler_view)
                .setOnLongClickListener {
                    val alertBuilder = AlertDialog.Builder(context)

                    alertBuilder.setTitle("이미지 삭제")
                    alertBuilder.setMessage("이미지를 삭제하시겠습니까?")
                    alertBuilder.setPositiveButton(
                        "삭제",
                        DialogInterface.OnClickListener { dialogInterface, i ->

                            if (fragment is EditFragment) {                      // 게시글 수정 액티비티일 경우 삭제하는 이미지가 파이어베이스에 이미 저장된 이미지인지 확인
                                for (i in 0 until EditFragment.postImagesUris.size) {
                                    if (EditFragment.postImagesUris[i].equals(data)) {
                                        EditFragment.deletedImageIndex[i] = true
                                    }
                                }
                            }

                            for (i in position until datas.size - 1) {
                                datas[i] = datas[i + 1]
                            }
                            datas.removeAt(datas.size - 1)

                            notifyDataSetChanged()

                            datas.forEach {
                                Log.d("MultiImageAdapter", "MultiImage after deleted : ${it}")
                            }
                            Log.d("MultiImageAdapter", "data size after deleting image : ${datas.size}")

                        })
                    alertBuilder.setNegativeButton("취소", null)
                    alertBuilder.setCancelable(true)

                    val alertDialog = alertBuilder.create()
                    alertDialog.show()

                    false
                }
        }
    }

    override fun getItemCount(): Int {
        var count = 0
        for (uri in datas) {
            if (uri != Uri.EMPTY) {
                count++
            }
        }
        return count
    }

    interface OnImageDeleteListener {
        fun onImageDeleted(position: Int)
    }

    fun setOnItemDeleteListener(listener: OnImageDeleteListener) {
        onItemDeleteListener = listener
    }
}