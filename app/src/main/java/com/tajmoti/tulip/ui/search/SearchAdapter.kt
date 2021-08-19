package com.tajmoti.tulip.ui.search

import com.tajmoti.libtulip.model.TulipSearchResult
import com.tajmoti.libtulip.model.TulipTvShow
import com.tajmoti.tulip.BaseAdapter
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.ItemSearchBinding

class SearchAdapter : BaseAdapter<TulipSearchResult, ItemSearchBinding>(
    ItemSearchBinding::inflate
) {

    override fun onBindViewHolder(vh: Holder<ItemSearchBinding>, item: TulipSearchResult) {
        vh.binding.searchResultName.text = item.name
        val icon = getDrawableByType(item)
        vh.binding.searchResultName.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, 0, 0, 0)
        vh.binding.searchResultService.text = item.service.name
        val langIcon = languageToIcon(item.language)
        vh.binding.imageLanguageIcon.setImageResource(langIcon ?: 0)
    }

    private fun getDrawableByType(item: TulipSearchResult): Int {
        return if (item is TulipTvShow)
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