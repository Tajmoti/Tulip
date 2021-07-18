package com.tajmoti.tulip.ui.season

import com.tajmoti.libtvprovider.show.Episode
import com.tajmoti.tulip.BaseAdapter
import com.tajmoti.tulip.databinding.ItemSearchBinding

class SeasonAdapter : BaseAdapter<Episode, ItemSearchBinding>(ItemSearchBinding::inflate) {

    override fun onBindViewHolder(vh: Holder<ItemSearchBinding>, item: Episode) {
        vh.binding.root.text = "Episode ${vh.adapterPosition + 1}: ${item.name}" // TODO
    }
}