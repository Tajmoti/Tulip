package com.tajmoti.tulip.ui.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.tajmoti.libtulip.model.IdentityItem

abstract class BaseIdentityAdapter<T : IdentityItem<*>, B : ViewBinding>(
    bindingCreator: (LayoutInflater, ViewGroup, Boolean) -> B,
    /**
     * Callback to use for when the item is clicked.
     */
    callback: ((T) -> Unit)? = null
) : BaseAdapter<T, B>(bindingCreator, callback) {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.key == newItem.key
    }
}