package com.simple.commonutils

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

fun Window.addHorizontalContentView(vararg triple: Triple) {
    addHorizontalContentView(triple.toMutableList())
}

fun Window.addHorizontalContentView(
    configs: MutableList<Triple>
) {
    val content: ViewGroup = FrameLayout(this.context).apply {
        (this@addHorizontalContentView.decorView as FrameLayout)
            .addView(
                this,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.gravity = Gravity.BOTTOM
                    this.bottomMargin = (resources.displayMetrics.scaledDensity * 50).toInt()
                })
    }
    val recyclerView = RecyclerView(context).apply {
        this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        this.setHasFixedSize(true)
        this.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): RecyclerView.ViewHolder {
                return CusViewHolder(
                    LinearLayout(context).apply {
                        this.layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        this.orientation = LinearLayout.VERTICAL
                        this.addView(
                            SeekBar(context).apply { this.visibility = View.GONE },
                            LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                        )
                        this.addView(
                            Button(context).apply {
                                this.textSize = 16f
                                this.isAllCaps = false
                                this.gravity = Gravity.CENTER
                                this.minWidth = resources.displayMetrics.widthPixels / 5
                                this.setPadding(24)
                            },
                            LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                        )
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
                        triple.second?.invoke(Unit)
                    }
                    holder.text.text = triple.first
                    holder.seekBar.setOnSeekBarChangeListener(object :
                        SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean
                        ) {
                            triple.third?.invoke(progress / 100f, triple.any)
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        }

                        override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        }
                    })
                }
            }

        }
    }
    content.addView(
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
    val content: ViewGroup = this.decorView as? FrameLayout ?: return
    val recyclerView = RecyclerView(context).apply {
        this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        this.setHasFixedSize(true)
        this.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): RecyclerView.ViewHolder {
                return CusViewHolder(
                    LinearLayout(context).apply {
                        this.layoutParams = ViewGroup.MarginLayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            this.bottomMargin =
                                (16 * context.resources.displayMetrics.scaledDensity).toInt()
                        }
                        this.orientation = LinearLayout.VERTICAL
                        this.addView(
                            SeekBar(context).apply {
                                this.setPadding(24, 16, 24, 0)
                            },
                            LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                        )
                        this.addView(
                            Button(context).apply {
                                this.textSize = 16f
                                this.isAllCaps = false
                                this.gravity = Gravity.CENTER
                                this.minWidth = resources.displayMetrics.widthPixels / 5
                                this.setPadding((20 * context.resources.displayMetrics.scaledDensity).toInt())
                            },
                            LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                        )
                    }
                )
            }

            override fun getItemCount(): Int {
                return configs.size
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                holder as CusViewHolder
                configs[position].let { triple ->
                    holder.text.setOnClickListener {
                        triple.any = triple.second?.invoke(Unit)
                    }
                    holder.text.text = triple.first
                    holder.seekBar.isVisible = triple.third != null
                    holder.seekBar.progress = triple.progress
                    log("progress", triple.progress)
                    holder.seekBar.setOnSeekBarChangeListener(object :
                        SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean
                        ) {
                            log("progress", progress)
                            if (fromUser) {
                                triple.progress = progress
                                triple.third?.invoke(progress / 100f, triple.any)
                            }
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        }

                        override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        }
                    })
                }
            }

        }
    }
    content.addView(
        recyclerView,
        FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            if (isBottomHalf) {
                recyclerView.resources.displayMetrics.heightPixels / 2
            } else {
                FrameLayout.LayoutParams.MATCH_PARENT
            },
            Gravity.BOTTOM
        ).apply {
            this.bottomMargin = context.resources.getDimensionPixelSize(
                context.resources.getIdentifier(
                    "status_bar_height",
                    "dimen",
                    "android"
                )
            ) * 2
        }
    )
}

data class Triple(
    var first: String = "",
    var second: ((Any?) -> Any?)? = null,
    var third: ((Float, Any?) -> Unit)? = null,
    var any: Any? = null,
    var progress: Int = 0
)

private class CusViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val seekBar: SeekBar = (view as ViewGroup)[0] as SeekBar
    val text: TextView = (view as ViewGroup)[1] as TextView
}