package com.tajmoti.tulip.ui.streams

import android.text.Spannable
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.widget.TextView
import com.tajmoti.tulip.BaseAdapter
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.ItemSearchBinding


class StreamsAdapter(
    val downloadCallback: (UnloadedVideoStreamRef) -> Unit
) : BaseAdapter<UnloadedVideoStreamRef, ItemSearchBinding>(ItemSearchBinding::inflate) {

    override fun onBindViewHolder(vh: Holder<ItemSearchBinding>, item: UnloadedVideoStreamRef) {
        val string = "#${vh.adapterPosition + 1}: ${item.info.serviceName}"
        setTextConsiderBrokenLink(item.info.working, vh, string)
        vh.binding.root.setCompoundDrawablesRelativeWithIntrinsicBounds(getDrawable(item), 0, 0, 0)
        vh.binding.root.setOnLongClickListener { downloadCallback(item); true }
    }

    private fun setTextConsiderBrokenLink(working: Boolean, vh: Holder<ItemSearchBinding>, string: String) {
        if (working) {
            vh.binding.root.text = string
        } else {
            vh.binding.root.setStrikethroughText(string)
        }
    }

    private fun TextView.setStrikethroughText(string: String) {
        setText(string, TextView.BufferType.SPANNABLE)
        val spannable = text as Spannable
        spannable.setSpan(STRIKE_THROUGH_SPAN, 0, string.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private fun getDrawable(ref: UnloadedVideoStreamRef): Int {
        return if (ref.linkExtractionSupported) {
            R.drawable.ic_baseline_ondemand_video_24
        } else {
            R.drawable.ic_baseline_web_24
        }
    }

    companion object {
        private val STRIKE_THROUGH_SPAN = StrikethroughSpan()
    }
}