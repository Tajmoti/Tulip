package com.tajmoti.tulip.ui.show

import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.ItemEpisodeBinding
import com.xwray.groupie.databinding.BindableItem

class EpisodeItem(
    private val episodeNumber: Int,
    private val episodeTitle: String?,
    val callback: () -> Unit
) : BindableItem<ItemEpisodeBinding>() {

    override fun getLayout(): Int {
        return R.layout.item_episode
    }

    override fun bind(viewBinding: ItemEpisodeBinding, position: Int) {
        viewBinding.root.setOnClickListener { callback() }
        viewBinding.episodeNumber = episodeNumber
        viewBinding.episodeTitle = episodeTitle
    }
}