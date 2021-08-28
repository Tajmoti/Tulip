package com.tajmoti.tulip.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseAdapter<T, B : ViewBinding>(
    val bindingCreator: (LayoutInflater, ViewGroup, Boolean) -> B
) : RecyclerView.Adapter<BaseAdapter.Holder<B>>() {
    var items = listOf<T>()
        set(value) {
            field = value
            notifyDataSetChanged()
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
}