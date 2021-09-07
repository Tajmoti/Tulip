package com.tajmoti.tulip.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseAdapter<T, B : ViewBinding>(
    val bindingCreator: (LayoutInflater, ViewGroup, Boolean) -> B
) : RecyclerView.Adapter<BaseAdapter.Holder<B>>() {
    var items = listOf<T>()
        set(value) {
            val diffResult = DiffUtil.calculateDiff(EqualityDiffCallback(value, field))
            field = value
            diffResult.dispatchUpdatesTo(this)
        }
    var callback: ((T) -> Unit)? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder<B> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = bindingCreator(inflater, parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder<B>, position: Int) {
        val item = items[position]
        callback?.let { cb -> holder.itemView.setOnClickListener { cb(item) } }
        onBindViewHolder(holder, item)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    abstract fun onBindViewHolder(vh: Holder<B>, item: T)

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