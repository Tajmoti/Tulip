package com.tajmoti.tulip.ui.season

import com.tajmoti.libtvprovider.show.Episode
import com.tajmoti.tulip.BaseAdapter
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.ItemEpisodeBinding

class SeasonAdapter :
    BaseAdapter<Episode, ItemEpisodeBinding>(ItemEpisodeBinding::inflate) {

    override fun onBindViewHolder(vh: Holder<ItemEpisodeBinding>, item: Episode) {
        val numberText = if (item.number != null) {
            vh.itemView.context.getString(R.string.episode_number, item.number)
        } else {
            vh.itemView.context.getString(R.string.episode_special)
        }
        vh.binding.textEpisodeNumber.text = numberText
        vh.binding.textEpisodeTitle.text = item.name ?: ""
    }
}