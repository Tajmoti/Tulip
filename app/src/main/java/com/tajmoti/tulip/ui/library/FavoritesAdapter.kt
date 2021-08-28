package com.tajmoti.tulip.ui.library

import com.bumptech.glide.Glide
import com.tajmoti.libtulip.model.info.TulipItemInfo
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.ItemLibraryBinding
import com.tajmoti.tulip.ui.BaseAdapter

class FavoritesAdapter : BaseAdapter<TulipItemInfo, ItemLibraryBinding>(
    ItemLibraryBinding::inflate
) {

    override fun onBindViewHolder(
        vh: Holder<ItemLibraryBinding>,
        item: TulipItemInfo
    ) {
        val placeholder = if (item.key is TvShowKey) {
            R.drawable.ic_baseline_live_tv_24
        } else {
            R.drawable.ic_baseline_local_movies_24
        }
        Glide.with(vh.itemView.context)
            .load(item.posterPath)
            .dontTransform()
            .placeholder(placeholder)
            .into(vh.binding.imageLibraryPoster)
        vh.binding.textLibraryName.text = item.name
    }
}