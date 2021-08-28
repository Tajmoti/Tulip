package com.tajmoti.tulip.ui.show

import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.ItemSeasonHeaderBinding
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.ExpandableItem
import com.xwray.groupie.databinding.BindableItem

class ExpandableHeaderItem(
    private val seasonTitle: String
) : BindableItem<ItemSeasonHeaderBinding>(), ExpandableItem {
    private lateinit var expandableGroup: ExpandableGroup

    override fun getLayout(): Int {
        return R.layout.item_season_header
    }

    override fun bind(viewBinding: ItemSeasonHeaderBinding, position: Int) {
        viewBinding.title = seasonTitle
        viewBinding.root.setOnClickListener { expandableGroup.onToggleExpanded() }
    }

    override fun setExpandableGroup(eg: ExpandableGroup) {
        expandableGroup = eg
    }
}