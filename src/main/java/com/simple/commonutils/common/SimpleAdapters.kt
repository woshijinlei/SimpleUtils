package com.simple.commonutils.common

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simple.commonutils.R

class SimpleImageAdapter(
    private val data: MutableList<Uri>,
    private val itemClickListener: Uri.() -> Unit
) :
    RecyclerView.Adapter<SimpleImageAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            LayoutInflater.from(parent.context).inflate(R.layout.item_simple_image, parent, false)
        )
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val image = view.findViewById<ImageView>(R.id.simple_image)
        private var bindData: Uri? = null

        init {
            itemView.setOnClickListener { bindData?.let { itemClickListener.invoke(it) } }
        }

        fun bind(uri: Uri) {
            bindData = uri
            this.image.setImageURI(uri)
        }
    }
}

class SimpleTextAdapter(
    private val data: MutableList<String>,
    private val itemClickListener: String.() -> Unit
) :
    RecyclerView.Adapter<SimpleTextAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            LayoutInflater.from(parent.context).inflate(R.layout.item_simple_text, parent, false)
        )
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val text = view.findViewById<TextView>(R.id.simple_text)
        private var bindData: String? = null

        init {
            itemView.setOnClickListener { bindData?.let { itemClickListener.invoke(it) } }
        }

        fun bind(text: String) {
            bindData = text
            this.text.text = text
        }
    }
}