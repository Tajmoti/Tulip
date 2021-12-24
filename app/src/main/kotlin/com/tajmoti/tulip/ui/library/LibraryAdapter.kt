package com.tajmoti.tulip.ui.library

import android.content.Context
import android.util.TypedValue
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.tajmoti.libtulip.model.key.*
import com.tajmoti.libtulip.ui.library.LibraryItem
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.ItemLibraryBinding
import com.tajmoti.tulip.ui.BaseIdentityAdapter
import com.tajmoti.tulip.ui.loadImage

class LibraryAdapter(
    onImageClickListener: (LibraryItem) -> Unit,
    private val onDetailsClickListener: (ItemKey) -> Unit
) : BaseIdentityAdapter<LibraryItem, ItemLibraryBinding>(
    ItemLibraryBinding::inflate,
    onImageClickListener
) {

    override fun onBindViewHolder(context: Context, index: Int, binding: ItemLibraryBinding, item: LibraryItem) {
        // Item name if not image is available
        binding.imageLibraryPoster.scaleType = ImageView.ScaleType.CENTER
        binding.textLibraryName.isVisible = true
        binding.textLibraryName.text = item.name
        // Image
        val placeholder = getPlaceholderForItem(item)
        binding.imageLibraryPoster.loadImage(item.posterPath, placeholder) {
            binding.imageLibraryPoster.scaleType = ImageView.ScaleType.FIT_XY
            binding.textLibraryName.isVisible = false
        }
        // Label
        val label = getEpisodeNumberLabelOrNull(context, item)
        binding.textEpisodeNumber.isVisible = label != null
        if (label != null) {
            val (large, text) = label
            val textSize = if (large) 14.0F else 12.0F
            binding.textEpisodeNumber.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
            binding.textEpisodeNumber.text = text
        }
        // Details button
        binding.buttonDetails.setOnClickListener { onDetailsClickListener(item.key) }
        // Playing progress indicator
        val progress = item.lastPlayedPosition?.progress
        val indicator = binding.progressIndicatorLibrary
        indicator.isVisible = progress != null
        if (progress != null) {
            indicator.layoutParams = indicator.layoutParams
                .let { it as ConstraintLayout.LayoutParams }
                .also { it.matchConstraintPercentWidth = progress }
        }
    }

    private fun getEpisodeNumberLabelOrNull(context: Context, item: LibraryItem): Pair<Boolean, String>? {
        val text = when (val key = item.lastPlayedPosition?.key) {
            is EpisodeKey.Hosted -> null
            is EpisodeKey.Tmdb -> true to "S${key.seasonNumber}:E${key.episodeNumber}"
            is MovieKey.Hosted -> null
            is MovieKey.Tmdb -> null
            null -> false to context.getString(R.string.not_yet_watched)
        }
        return text
    }

    private fun getPlaceholderForItem(item: LibraryItem): Int {
        val placeholder = if (item.key is TvShowKey) {
            R.drawable.ic_baseline_live_tv_128
        } else {
            R.drawable.ic_baseline_local_movies_128
        }
        return placeholder
    }
}