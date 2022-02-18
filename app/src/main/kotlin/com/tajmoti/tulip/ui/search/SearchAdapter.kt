package com.tajmoti.tulip.ui.search

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.tajmoti.libtulip.model.info.LanguageCode
import com.tajmoti.libtulip.model.search.GroupedSearchResult
import com.tajmoti.libtulip.ui.search.SearchUi.getItemInfoForDisplay
import com.tajmoti.libtulip.ui.search.SearchUi.getLanguagesForItem
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.IconSearchResultLanguageBinding
import com.tajmoti.tulip.databinding.ItemSearchBinding
import com.tajmoti.tulip.ui.base.BaseIdentityAdapter
import com.tajmoti.tulip.ui.languageToIcon

class SearchAdapter(
    onSearchResultClickListener: (GroupedSearchResult) -> Unit
) : BaseIdentityAdapter<GroupedSearchResult, ItemSearchBinding>(
    ItemSearchBinding::inflate,
    onSearchResultClickListener
) {

    override fun onBindViewHolder(context: Context, index: Int, binding: ItemSearchBinding, item: GroupedSearchResult) {
        val name = getNameForItem(context, item)
        val icon = getDrawableForItem(item)
        val languages = getLanguagesForItem(item)
        binding.searchResultName.text = name
        binding.searchResultName.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, 0, 0, 0)
        inflateViewsForLanguages(languages, binding.root)
    }

    private fun getNameForItem(context: Context, item: GroupedSearchResult): String {
        val info = getItemInfoForDisplay(item)
        return when (item) {
            is GroupedSearchResult.Movie -> info.name
            is GroupedSearchResult.TvShow -> info.name
            is GroupedSearchResult.UnrecognizedTvShow -> context.getString(R.string.other_tv_shows)
            is GroupedSearchResult.UnrecognizedMovie -> context.getString(R.string.other_movies)
        }
    }

    private fun getDrawableForItem(item: GroupedSearchResult): Int {
        return when (item) {
            is GroupedSearchResult.TvShow -> R.drawable.ic_baseline_live_tv_24
            is GroupedSearchResult.Movie -> R.drawable.ic_baseline_local_movies_24
            else -> R.drawable.ic_baseline_more_horiz_24
        }
    }

    private fun inflateViewsForLanguages(languages: List<LanguageCode>, container: ViewGroup) {
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