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
    private val callback: ((T) -> Unit)? = null,
    /**
     * Optional header view.
     */
    private val headerCreator: ((LayoutInflater, ViewGroup, Boolean) -> ViewBinding)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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

    override fun getItemViewType(position: Int): Int {
        return if (headerCreator != null && position == 0) {
            ITEM_TYPE_HEADER
        } else {
            ITEM_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder<*> {
        val inflater = LayoutInflater.from(parent.context)
        if (viewType == ITEM_TYPE_HEADER) {
            return Holder(headerCreator!!.invoke(inflater, parent, false))
        }
        val binding = bindingCreator(inflater, parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (headerCreator != null && position == 0) {
            return
        }
        @Suppress("UNCHECKED_CAST")
        holder as Holder<B>
        val pos = adjustPosHeader(position)
        val item = items[pos]
        callback?.let { cb -> holder.itemView.setOnClickListener { cb(item) } }
        onBindViewHolder(context, holder.adapterPosition, holder.binding, item)
    }

    override fun getItemCount(): Int {
        return adjustByHeader(items.size)
    }

    class Holder<B : ViewBinding>(val binding: B) : RecyclerView.ViewHolder(binding.root)

    private inner class EqualityDiffCallback(
        private var newVideos: List<T>,
        private var oldVideos: List<T>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int {
            return adjustByHeader(oldVideos.size)
        }

        override fun getNewListSize(): Int {
            return adjustByHeader(newVideos.size)
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            if (oldItemPosition == 0 || newItemPosition == 0) {
                return oldItemPosition == 0 && newItemPosition == 0
            }
            val oldItemPos = adjustPosHeader(oldItemPosition)
            val newItemPos = adjustPosHeader(newItemPosition)
            return this@BaseAdapter.areItemsTheSame(oldVideos[oldItemPos], newVideos[newItemPos])
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            if (oldItemPosition == 0 || newItemPosition == 0) {
                return oldItemPosition == 0 && newItemPosition == 0
            }
            val oldItemPos = adjustPosHeader(oldItemPosition)
            val newItemPos = adjustPosHeader(newItemPosition)
            return this@BaseAdapter.areContentsTheSame(oldVideos[oldItemPos], newVideos[newItemPos])
        }
    }

    private fun adjustByHeader(size: Int): Int {
        var count = size
        if (headerCreator != null)
            count++
        return count
    }

    private fun adjustPosHeader(size: Int): Int {
        var pos = size
        if (headerCreator != null)
            pos--
        return pos
    }

    companion object {
        private const val ITEM_TYPE_HEADER = 0
        private const val ITEM_TYPE_ITEM = 1
    }
}