package com.tajmoti.tulip.ui.search

import com.tajmoti.libtvprovider.TvItem
import com.tajmoti.tulip.BaseAdapter
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.ItemSearchBinding
import com.tajmoti.tulip.model.StreamingService

class SearchAdapter : BaseAdapter<Pair<StreamingService, TvItem>, ItemSearchBinding>(
    ItemSearchBinding::inflate
) {

    override fun onBindViewHolder(
        vh: Holder<ItemSearchBinding>,
        item: Pair<StreamingService, TvItem>
    ) {
        vh.binding.searchResultName.text = item.second.name
        val icon = getDrawableByType(item.second)
        vh.binding.searchResultName.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, 0, 0, 0)
        vh.binding.searchResultService.text = item.first.name
    }

    private fun getDrawableByType(item: TvItem): Int {
        return if (item is TvItem.Show)
            R.drawable.ic_baseline_live_tv_24
        else
            R.drawable.ic_baseline_local_movies_24
    }
}