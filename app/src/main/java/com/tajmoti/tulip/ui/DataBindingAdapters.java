package com.tajmoti.tulip.ui;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DataBindingAdapters {

    @BindingAdapter("srcCompat")
    public static void setFabDrawableCompat(FloatingActionButton view, Drawable drawable) {
        view.setImageDrawable(drawable);
    }

    @BindingAdapter("android:src")
    public static void setImageResource(ImageView imageView, int resource) {
        imageView.setImageResource(resource);
    }
}
