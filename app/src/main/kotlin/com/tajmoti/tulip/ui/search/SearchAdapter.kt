package com.tajmoti.tulip.ui.search

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.tajmoti.libtulip.dto.LanguageCodeDto
import com.tajmoti.libtulip.dto.SearchResultDto
import com.tajmoti.libtulip.ui.search.SearchUi.getItemInfoForDisplay
import com.tajmoti.libtulip.ui.search.SearchUi.getLanguagesForItem
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.IconSearchResultLanguageBinding
import com.tajmoti.tulip.databinding.ItemSearchBinding
import com.tajmoti.tulip.ui.base.BaseIdentityAdapter
import com.tajmoti.tulip.ui.languageToIcon

class SearchAdapter(
    onSearchResultClickListener: (SearchResultDto) -> Unit
) : BaseIdentityAdapter<SearchResultDto, ItemSearchBinding>(
    ItemSearchBinding::inflate,
    onSearchResultClickListener
) {

    override fun onBindViewHolder(context: Context, index: Int, binding: ItemSearchBinding, item: SearchResultDto) {
        val name = getNameForItem(context, item)
        val icon = getDrawableForItem(item)
        val languages = getLanguagesForItem(item)
        binding.searchResultName.text = name
        binding.searchResultName.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, 0, 0, 0)
        inflateViewsForLanguages(languages, binding.root)
    }

    private fun getNameForItem(context: Context, item: SearchResultDto): String {
        val info = getItemInfoForDisplay(item)
        return when (item) {
            is SearchResultDto.Movie -> info.name
            is SearchResultDto.TvShow -> info.name
            is SearchResultDto.UnrecognizedTvShow -> context.getString(R.string.other_tv_shows)
            is SearchResultDto.UnrecognizedMovie -> context.getString(R.string.other_movies)
        }
    }

    private fun getDrawableForItem(item: SearchResultDto): Int {
        return when (item) {
            is SearchResultDto.TvShow -> R.drawable.ic_baseline_live_tv_24
            is SearchResultDto.Movie -> R.drawable.ic_baseline_local_movies_24
            else -> R.drawable.ic_baseline_more_horiz_24
        }
    }

    private fun inflateViewsForLanguages(languages: List<LanguageCodeDto>, container: ViewGroup) {
        val inflater = LayoutInflater.from(container.context)
        while (container.childCount > 1) {
            container.removeViewAt(1)
        }
        for (lang in languages) {
            val langIcon = languageToIcon(lang) ?: continue
            val binding = IconSearchResultLanguageBinding.inflate(inflater, container, false)
            binding.root.setImageResource(langIcon)
            container.addView(binding.root)
        }
    }
}