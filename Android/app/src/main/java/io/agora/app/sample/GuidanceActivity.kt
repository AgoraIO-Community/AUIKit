package io.agora.app.sample

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GuidanceActivity: AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private val itemList = listOf(
        CptItem("基础组件", "Basic components"),
        CptItem("语聊房组件", "Components-Voice"),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.guidance_list_activity)

        recyclerView = findViewById(R.id.rlCptList)

        // 设置 RecyclerView 的布局管理器
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 初始化 Adapter 并设置给 RecyclerView
        val adapter = ListAdapter(itemList) { item,position -> onItemClick(item,position) }
        recyclerView.adapter = adapter
    }

    private fun onItemClick(item: CptItem,position:Int) {
        when(position){
            0 -> { startActivity(Intent(this, BasicUiListActivity::class.java)) }
            1 -> { startActivity(Intent(this, VoiceCptListActivity::class.java)) }
        }
    }
}

data class CptItem(val title: String, val subtitle: String)

class ListAdapter(private val items: List<CptItem>,private val clickListener: (CptItem,Int) -> Unit ) : RecyclerView.Adapter<ListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.guidance_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item,position,clickListener)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val subtitleTextView: TextView = itemView.findViewById(R.id.subtitleTextView)

        fun bind(item: CptItem,position:Int,clickListener: (CptItem,Int) -> Unit) {
            titleTextView.text = item.title
            subtitleTextView.text = item.subtitle

            itemView.setOnClickListener { clickListener(item,position) }
        }
    }
}