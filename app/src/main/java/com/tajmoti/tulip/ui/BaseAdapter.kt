package com.tajmoti.tulip.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseAdapter<T, B : ViewBinding>(
    private val bindingCreator: (LayoutInflater, ViewGroup, Boolean) -> B,
    /**
     * Callback to use for when the item is clicked.
     */
    private val callback: ((T) -> Unit)? = null
) : RecyclerView.Adapter<BaseAdapter.Holder<B>>() {
    /**
     * Items to display in this adapter.
     * Diffs are calculated automatically.
     */
    var items = listOf<T>()
        set(value) {
            val diffResult = DiffUtil.calculateDiff(EqualityDiffCallback(value, field))
            field = value
            diffResult.dispatchUpdatesTo(this)
        }

    /**
     * Context of the recycler.
     */
    protected lateinit var context: Context


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        context = recyclerView.context
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder<B> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = bindingCreator(inflater, parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder<B>, position: Int) {
        val item = items[position]
        callback?.let { cb -> holder.itemView.setOnClickListener { cb(item) } }
        onBindViewHolder(holder, item)
        onBindViewHolder(holder.binding, item)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    open fun onBindViewHolder(vh: Holder<B>, item: T) {

    }

    open fun onBindViewHolder(binding: B, item: T) {

    }

    class Holder<B : ViewBinding>(val binding: B) : RecyclerView.ViewHolder(binding.root)

    class EqualityDiffCallback<T>(
        private var newVideos: List<T>,
        private var oldVideos: List<T>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = oldVideos.size

        override fun getNewListSize() = newVideos.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldVideos[oldItemPosition] == newVideos[newItemPosition]
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldVideos[oldItemPosition] == newVideos[newItemPosition]
        }
    }
}