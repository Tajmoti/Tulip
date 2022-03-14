package com.tajmoti.tulip.ui.base

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
    private lateinit var context: Context

    /**
     * Binds the ViewHolder - [Context], [B] and [T] are provided.
     */
    protected abstract fun onBindViewHolder(context: Context, index: Int, binding: B, item: T)

    /**
     * Compares items for equality in regard to their identity.
     */
    protected open fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem == newItem
    }

    /**
     * Compares items for equality in regard to their identity & contents.
     */
    protected open fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem == newItem
    }


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
        onBindViewHolder(context, holder.adapterPosition, holder.binding, item)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class Holder<B : ViewBinding>(val binding: B) : RecyclerView.ViewHolder(binding.root)

    private inner class EqualityDiffCallback(
        private var newVideos: List<T>,
        private var oldVideos: List<T>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = oldVideos.size

        override fun getNewListSize() = newVideos.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return this@BaseAdapter.areItemsTheSame(oldVideos[oldItemPosition], newVideos[newItemPosition])
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return this@BaseAdapter.areContentsTheSame(oldVideos[oldItemPosition], newVideos[newItemPosition])
        }
    }
}