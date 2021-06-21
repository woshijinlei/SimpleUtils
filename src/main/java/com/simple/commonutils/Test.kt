package com.simple.commonutils

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

fun Window.addHorizontalContentView(vararg triple: Triple) {
    addHorizontalContentView(triple.toMutableList())
}

fun Window.addHorizontalContentView(
    configs: MutableList<Triple>
) {
    val content: ViewGroup? = this.decorView.findViewById<FrameLayout>(android.R.id.content)
        ?: return
    val recyclerView = RecyclerView(context).apply {
        this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        this.setHasFixedSize(true)
        this.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): RecyclerView.ViewHolder {
                return CusViewHolder(
                    Button(context).apply {
                        this.textSize = 16f
                        this.isAllCaps = false
                        this.gravity = Gravity.CENTER
                        this.minWidth = resources.displayMetrics.widthPixels / 5
                        this.setPadding(24)
                    }
                )
            }

            override fun getItemCount(): Int {
                return configs.size
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                holder as CusViewHolder
                configs[position].let { triple ->
                    holder.itemView.setOnClickListener {
                        triple.second?.invoke {

                        }
                    }
                    holder.text.text = triple.first
                }
            }

        }
    }
    content?.addView(
        recyclerView,
        FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM
        )
    )
}

fun Window.addVerticalContentView(vararg triple: Triple) {
    addVerticalContentView(triple.toMutableList())
}

fun Window.addVerticalContentView(
    configs: MutableList<Triple>,
    isBottomHalf: Boolean = true
) {
    val content: ViewGroup? = this.decorView.findViewById<FrameLayout>(android.R.id.content)
        ?: return
    val recyclerView = RecyclerView(context).apply {
        this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        this.setHasFixedSize(true)
        this.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): RecyclerView.ViewHolder {
                return CusViewHolder(
                    Button(context).apply {
                        this.textSize = 16f
                        this.isAllCaps = false
                        this.gravity = Gravity.CENTER
                        this.minWidth = resources.displayMetrics.widthPixels / 5
                        this.setPadding(24)
                    }
                )
            }

            override fun getItemCount(): Int {
                return configs.size
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                holder as CusViewHolder
                configs[position].let { triple ->
                    holder.itemView.setOnClickListener {
                        triple.second?.invoke {
                        }
                    }
                    holder.text.text = triple.first
                }
            }

        }
    }
    content?.addView(
        recyclerView,
        FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            if (isBottomHalf) {
                recyclerView.resources.displayMetrics.heightPixels / 2
            } else {
                FrameLayout.LayoutParams.MATCH_PARENT
            },
            Gravity.BOTTOM
        )
    )
}

data class Triple(
    var first: String = "",
    var second: (((Any) -> Unit) -> Unit)? = null,
    var third: Any? = null
)

private class CusViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val text: TextView = view as TextView
}