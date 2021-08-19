package com.tajmoti.tulip.ui.search

import com.tajmoti.libtvprovider.TvItem
import com.tajmoti.tulip.BaseAdapter
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.ItemSearchBinding
import com.tajmoti.libtulip.model.StreamingService

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
        val langIcon = languageToIcon(item.second.language)
        vh.binding.imageLanguageIcon.setImageResource(langIcon ?: 0)
    }

    private fun getDrawableByType(item: TvItem): Int {
        return if (item is TvItem.Show)
            R.drawable.ic_baseline_live_tv_24
        else
            R.drawable.ic_baseline_local_movies_24
    }

    private fun languageToIcon(language: String): Int? {
        return when (language) {
            "en" -> R.drawable.ic_flag_uk
            "de" -> R.drawable.ic_flag_de
            else -> null
        }
    }
}