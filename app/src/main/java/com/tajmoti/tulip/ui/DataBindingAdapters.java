package com.tajmoti.tulip.ui;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.core.view.ViewKt;
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
    public static void setIndicatorColor(CircularProgressIndicator indicator, @ColorInt int color) {
        indicator.setIndicatorColor(color);
    }

    @BindingAdapter("android:visibility")
    public static void setVisibility(View view, Boolean value) {
        ViewKt.setVisible(view, value);
    }
}
