package com.tajmoti.tulip.ui.season

import com.tajmoti.libtulip.model.info.EpisodeInfoWithKey
import com.tajmoti.tulip.BaseAdapter
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.ItemEpisodeBinding

class SeasonAdapter : BaseAdapter<EpisodeInfoWithKey, ItemEpisodeBinding>(
    ItemEpisodeBinding::inflate
) {

    override fun onBindViewHolder(vh: Holder<ItemEpisodeBinding>, pair: EpisodeInfoWithKey) {
        val item = pair.first
        val numberText = vh.itemView.context.getString(R.string.episode_number, item.number)
        vh.binding.textEpisodeNumber.text = numberText
        vh.binding.textEpisodeTitle.text = item.name ?: ""
    }
}