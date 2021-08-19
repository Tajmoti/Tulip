package com.tajmoti.tulip.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import com.tajmoti.libtulip.model.TmdbId
import com.tajmoti.libtulip.model.TulipMovie
import com.tajmoti.libtulip.model.TulipSearchResult
import com.tajmoti.libtulip.model.TulipTvShow
import com.tajmoti.tulip.BaseAdapter
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.IconSearchResultLanguageBinding
import com.tajmoti.tulip.databinding.ItemSearchBinding

class SearchAdapter : BaseAdapter<Pair<TmdbId?, List<TulipSearchResult>>, ItemSearchBinding>(
    ItemSearchBinding::inflate
) {

    override fun onBindViewHolder(
        vh: Holder<ItemSearchBinding>,
        item: Pair<TmdbId?, List<TulipSearchResult>>
    ) {
        val fstResult = item.second.first()
        val name = if (item.first != null) {
            fstResult.name
        } else {
            vh.itemView.context.getString(
                R.string.other_results
            )
        }
        vh.binding.searchResultName.text = name
        val icon = getDrawableByType(fstResult.takeIf { item.first != null })
        vh.binding.searchResultName.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, 0, 0, 0)

        val languages = item.second
            .map { it.language }
            .distinct()
            .sorted()
        inflateViewsForLanguages(languages, vh.binding.root)
    }

    private fun getDrawableByType(item: TulipSearchResult?): Int {
        return when (item) {
            is TulipTvShow -> R.drawable.ic_baseline_live_tv_24
            is TulipMovie -> R.drawable.ic_baseline_local_movies_24
            else -> R.drawable.ic_baseline_more_horiz_24
        }
    }

    private fun inflateViewsForLanguages(languages: List<String>, container: ViewGroup) {
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

    private fun languageToIcon(language: String): Int? {
        return when (language) {
            "en" -> R.drawable.ic_flag_uk
            "de" -> R.drawable.ic_flag_de
            else -> null
        }
    }
}