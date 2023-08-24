package io.agora.app.sample.dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.bumptech.glide.Glide
import com.google.android.material.textview.MaterialTextView
import io.agora.app.sample.R
import io.agora.app.sample.databinding.VoiceRoomMoreLayoutBinding
import io.agora.auikit.ui.basic.AUISheetFragmentDialog
import io.agora.auikit.ui.basic.AUIImageView
import io.agora.auikit.utils.DeviceTools
import io.agora.auikit.utils.FastClickTools

class VoiceRoomMoreDialog(
    context: Context,
    itemList:MutableList<VoiceMoreItemBean>,
    itemListener: GridViewItemClickListener
) : AUISheetFragmentDialog<VoiceRoomMoreLayoutBinding>()  {
    private var mContext:Context
    private val mColumns = 4
    private var gridAdapter: VoiceMoreDialogAdapter? = null
    private var data:MutableList<VoiceMoreItemBean>? = null
    private var listener: GridViewItemClickListener?=null

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): VoiceRoomMoreLayoutBinding {
       return VoiceRoomMoreLayoutBinding.inflate(inflater, container, false)
    }

    init {
        this.mContext = context
        this.data = itemList
        this.listener = itemListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view,savedInstanceState)
        binding?.root?.let { setOnApplyWindowInsets(it) }
        initView()
        initListener()
    }

    private fun initView() {
        binding?.gvGridview.let {
            it?.verticalSpacing = DeviceTools.dp2px(mContext, 20F)
            it?.numColumns = mColumns
            it?.verticalSpacing = 40
            gridAdapter = data?.let { it1 -> VoiceMoreDialogAdapter(mContext, 1, it1) }
            it?.adapter = gridAdapter
        }
    }

    private fun initListener(){
        binding?.gvGridview?.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                if (!FastClickTools.isFastClick(view)){
                    listener?.onItemClickListener(position)
                }
            }
    }

    fun updateStatus(position: Int,enable: Boolean){
        gridAdapter?.updateStatus(position,enable)
    }

    interface GridViewItemClickListener{
        fun onItemClickListener(position:Int){}
    }
}

class VoiceMoreDialogAdapter(
    context: Context,
    resource: Int,
    objects: MutableList<VoiceMoreItemBean>
) : ArrayAdapter<VoiceMoreItemBean>(context, resource, objects){
    private var selectPosition:Int = -1

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.voice_room_more_item_layout, null)
        }
        val icon = convertView!!.findViewById<AUIImageView>(R.id.ivItemIcon)
        val title = convertView.findViewById<MaterialTextView>(R.id.mtItemTitle)
        val status = convertView.findViewById<AUIImageView>(R.id.ivItemIconStatus)
        val item: VoiceMoreItemBean? = getItem(position)
        item.let {
            title.text = it?.ItemTitle
            if (item?.ItemStatus == true && selectPosition == position){
                status.visibility = View.VISIBLE
            }else{
                status.visibility = View.GONE
            }
            icon.setBackgroundResource(R.drawable.voice_bg_more_item_gradient)
            Glide.with(context).load(it?.ItemIcon).into(icon)
            return convertView
        }
    }

    fun updateStatus(position:Int,enable:Boolean){
        selectPosition = position
        val item = getItem(position)
        item?.ItemStatus = enable
        notifyDataSetChanged()
    }

}