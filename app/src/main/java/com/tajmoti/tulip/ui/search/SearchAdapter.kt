package com.tajmoti.tulip.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import com.tajmoti.libtulip.model.hosted.HostedItem
import com.tajmoti.libtulip.model.info.LanguageCode
import com.tajmoti.libtulip.model.info.TulipSearchResult
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.IconSearchResultLanguageBinding
import com.tajmoti.tulip.databinding.ItemSearchBinding
import com.tajmoti.tulip.ui.BaseAdapter
import com.tajmoti.tulip.ui.languageToIcon

class SearchAdapter : BaseAdapter<TulipSearchResult, ItemSearchBinding>(
    ItemSearchBinding::inflate
) {

    override fun onBindViewHolder(
        vh: Holder<ItemSearchBinding>,
        item: TulipSearchResult
    ) {
        val fstResult = item.results.first()
        val name = if (item.tmdbId != null) {
            fstResult.name
        } else {
            vh.itemView.context.getString(
                R.string.other_results
            )
        }
        vh.binding.searchResultName.text = name
        val icon = getDrawableByType(fstResult.takeIf { item.tmdbId != null })
        vh.binding.searchResultName.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, 0, 0, 0)

        val languages = item.results
            .map { it.language }
            .distinct()
            .sorted()
            .map { LanguageCode(it) }
        inflateViewsForLanguages(languages, vh.binding.root)
    }

    private fun getDrawableByType(item: HostedItem?): Int {
        return when (item) {
            is HostedItem.TvShow -> R.drawable.ic_baseline_live_tv_24
            is HostedItem.Movie -> R.drawable.ic_baseline_local_movies_24
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