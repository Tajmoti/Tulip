package com.tajmoti.tulip.ui.search

import com.tajmoti.libtvprovider.TvItem
import com.tajmoti.tulip.BaseAdapter
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.ItemSearchBinding

class SearchAdapter : BaseAdapter<TvItem, ItemSearchBinding>(ItemSearchBinding::inflate) {

    override fun onBindViewHolder(vh: Holder<ItemSearchBinding>, item: TvItem) {
        vh.binding.root.text = item.name
        val icon = getDrawableByType(item)
        vh.binding.root.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, 0, 0, 0)
    }

    private fun getDrawableByType(item: TvItem): Int {
        return if (item is TvItem.Show)
            R.drawable.ic_baseline_live_tv_24
        else
            R.drawable.ic_baseline_local_movies_24
    }
}