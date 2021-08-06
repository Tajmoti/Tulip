package com.tajmoti.tulip.ui.season

import com.tajmoti.libtvprovider.show.Episode
import com.tajmoti.tulip.BaseAdapter
import com.tajmoti.tulip.databinding.ItemEpisodeBinding

class SeasonAdapter :
    BaseAdapter<Episode, ItemEpisodeBinding>(ItemEpisodeBinding::inflate) {

    override fun onBindViewHolder(vh: Holder<ItemEpisodeBinding>, item: Episode) {
        var label = "Episode ${item.number}"
        if (item.name != null)
            label += ": ${item.name}"
        vh.binding.textEpisodeTitle.text = label // TODO
    }
}