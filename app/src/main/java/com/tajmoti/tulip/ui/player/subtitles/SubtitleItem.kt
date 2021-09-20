package com.tajmoti.tulip.ui.player.subtitles

import com.tajmoti.libtulip.model.subtitle.SubtitleInfo
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.ItemSubtitleFileBinding
import com.xwray.groupie.databinding.BindableItem

class SubtitleItem(
    private val index: Int,
    private val subtitle: SubtitleInfo?,
    val callback: (SubtitleInfo?) -> Unit
) : BindableItem<ItemSubtitleFileBinding>() {

    override fun getLayout(): Int {
        return R.layout.item_subtitle_file
    }

    override fun bind(viewBinding: ItemSubtitleFileBinding, position: Int) {
        viewBinding.root.setOnClickListener { callback(subtitle) }
        viewBinding.labelSubtitleNumber.text = if (subtitle != null) {
            "${index + 1}"
        } else {
            viewBinding.root.context.getString(R.string.label_no_subtitles)
        }
    }
}