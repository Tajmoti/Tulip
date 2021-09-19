package com.tajmoti.tulip.ui;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import androidx.databinding.BindingAdapter;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

public class DataBindingAdapters {

    @BindingAdapter("srcCompat")
    public static void setFabDrawableCompat(FloatingActionButton view, Drawable drawable) {
        view.setImageDrawable(drawable);
    }

    @BindingAdapter("android:src")
    public static void setImageResource(ImageView imageView, int resource) {
        imageView.setImageResource(resource);
    }

    @BindingAdapter("indicatorColor")
    public static void setIndicatorColor(CircularProgressIndicator indicator, @ColorRes int colorRes) {
        int color = ContextCompat.getColor(indicator.getContext(), colorRes);
        indicator.setIndicatorColor(color);
    }
}
