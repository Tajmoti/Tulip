package com.tajmoti.tulip.ui

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.progressindicator.BaseProgressIndicator

fun BaseProgressIndicator<*>.setProgressFractionCompat(value: Float, animated: Boolean) {
    val step = (value * max).toInt()
    setProgressCompat(step, animated)
}

fun ImageView.loadImage(
    url: String?,
    @DrawableRes placeholder: Int,
    onLoad: (() -> Unit)? = null
) {
    glideRequest(url, onLoad, placeholder)
        .into(this)
}

fun <T : View> T.loadImageAsBackground(
    url: String?,
    @DrawableRes placeholder: Int,
    onLoad: (() -> Unit)? = null
) {
    glideRequest(url, onLoad, placeholder)
        .into(object : CustomViewTarget<T, Drawable>(this) {
            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                background = resource
            }

            override fun onLoadFailed(errorDrawable: Drawable?) {

            }

            override fun onResourceCleared(placeholder: Drawable?) {
                background = null
            }
        })
}

private fun View.glideRequest(
    url: String?,
    onLoad: (() -> Unit)?,
    placeholder: Int
) = Glide.with(this)
    .load(url)
    .listener(object : RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            return false
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            onLoad?.invoke()
            return false
        }
    })
    .dontTransform()
    .placeholder(placeholder)

fun <T : ViewGroup.LayoutParams> View.mutateLayoutParams(mutator: T.() -> Unit) {
    val params = layoutParams as T
    mutator(params)
    layoutParams = params
}