package com.tajmoti.tulip.ui.player.subtitles

import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.ItemSubtitleLanguageBinding
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.ExpandableItem
import com.xwray.groupie.databinding.BindableItem
import java.util.*

class SubtitleLanguageHeaderItem(
    private val language: Locale
) : BindableItem<ItemSubtitleLanguageBinding>(), ExpandableItem {
    private lateinit var expandableGroup: ExpandableGroup

    override fun getLayout(): Int {
        return R.layout.item_subtitle_language
    }

    override fun bind(viewBinding: ItemSubtitleLanguageBinding, position: Int) {
        viewBinding.labelSubtitleLanguage.text = language.displayLanguage
        viewBinding.root.setOnClickListener { expandableGroup.onToggleExpanded() }
    }

    override fun setExpandableGroup(eg: ExpandableGroup) {
        expandableGroup = eg
    }
}