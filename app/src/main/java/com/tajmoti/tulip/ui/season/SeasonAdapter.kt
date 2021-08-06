package com.tajmoti.tulip.ui.season

import com.tajmoti.libtvprovider.show.Episode
import com.tajmoti.tulip.BaseAdapter
import com.tajmoti.tulip.databinding.ItemEpisodeBinding

class SeasonAdapter :
    BaseAdapter<Episode, ItemEpisodeBinding>(ItemEpisodeBinding::inflate) {

    override fun onBindViewHolder(vh: Holder<ItemEpisodeBinding>, item: Episode) {
        vh.binding.textEpisodeTitle.text = "Episode ${vh.adapterPosition + 1}: ${item.name}" // TODO
    }
}