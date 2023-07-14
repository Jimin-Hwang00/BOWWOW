package sklookie.bowwow

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import sklookie.bowwow.databinding.ActivityItemBinding

class DeviceAdapter(val devices: ArrayList<bluetoothDto>) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> (){

    interface OnItemClickListener{
        fun onItemClick(view : View, position: Int)
    }
    lateinit var listener : OnItemClickListener

    fun setOnItemClickListener(listener : OnItemClickListener){
        this.listener = listener
    }

    class   DeviceViewHolder(val itemBinding: ActivityItemBinding, listener: OnItemClickListener) : RecyclerView.ViewHolder(itemBinding.root){
        val deviceName = itemBinding.deviceName
        val mac = itemBinding.Mac
        val isConnect = itemBinding.isConnect

        init{
            val TAG = "ViewHolder"
            itemBinding.root.setOnClickListener{
                Log.d(TAG, "${adapterPosition}클릭")
                //가장 바깥 layout : itemBinding.root
                listener.onItemClick(itemBinding.root, adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val itemBinding = ActivityItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeviceViewHolder(itemBinding, listener)
    }

    // override fun getItemCount() = foods.size
    override fun getItemCount(): Int {
        return devices.size
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.deviceName.text = devices[position].deviceName
        holder.mac.text = devices[position].deviceAddress
        holder.isConnect.text = devices[position].isConnect
    }
}