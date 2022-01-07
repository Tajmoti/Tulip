package com.tajmoti.tulip.ui

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator

object DataBindingAdapters {
    @JvmStatic
    @BindingAdapter("srcCompat")
    fun setFabDrawableCompat(view: FloatingActionButton, drawable: Drawable?) {
        view.setImageDrawable(drawable)
    }

    @JvmStatic
    @BindingAdapter("android:src")
    fun setImageResource(imageView: ImageView, resource: Int) {
        imageView.setImageResource(resource)
    }

    @JvmStatic
    @BindingAdapter("srcUrl")
    fun setImageResourceUrl(imageView: ImageView, srcUrl: String?) {
        Glide.with(imageView).load(srcUrl).into(imageView)
    }

    @JvmStatic
    @BindingAdapter("indicatorColor")
    fun setIndicatorColor(indicator: CircularProgressIndicator, @ColorInt color: Int) {
        indicator.setIndicatorColor(color)
    }

    @JvmStatic
    @BindingAdapter("android:visibility")
    fun setVisibility(view: View, value: Boolean?) {
        view.isVisible = value!!
    }

    @JvmStatic
    @BindingAdapter("progressFraction")
    fun setProgressFraction(view: SeekBar, progress: Float?) {
        if (progress == null) return
        val oldProgress = (view.progress.toFloat() / view.max.toFloat()).toInt()
        val newProgress = (view.max * progress).toInt()
        if (oldProgress != newProgress) {
            view.progress = newProgress
        }
    }

    @JvmStatic
    @BindingAdapter("onProgressFractionChanged")
    fun setOnSeekBarChangeListener(
        view: SeekBar,
        progressFractionalChanged: OnProgressFractionalChanged?
    ) {
        if (progressFractionalChanged == null) {
            view.setOnSeekBarChangeListener(null)
            return
        }
        view.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                val progressFractional = progress.toFloat() / seekBar.max.toFloat()
                progressFractionalChanged.onProgressFractionalChanged(progressFractional)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    interface OnProgressFractionalChanged {
        fun onProgressFractionalChanged(progress: Float?)
    }
}