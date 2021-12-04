package com.tajmoti.tulip.ui;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
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

    @BindingAdapter("progressFraction")
    public static void setProgressFraction(SeekBar view, @Nullable Float progress) {
        if (progress == null)
            return;
        var oldProgress = (int) ((float) view.getProgress() / (float) view.getMax());
        var newProgress = (int) (view.getMax() * progress);
        if (oldProgress != newProgress) {
            view.setProgress(newProgress);
        }
    }

    @BindingAdapter("onProgressFractionChanged")
    public static void setOnSeekBarChangeListener(
            SeekBar view,
            final OnProgressFractionalChanged progressFractionalChanged
    ) {
        if (progressFractionalChanged == null) {
            view.setOnSeekBarChangeListener(null);
            return;
        }
        view.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser)
                    return;
                var progressFractional = (float) progress / (float) seekBar.getMax();
                progressFractionalChanged.onProgressFractionalChanged(progressFractional);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public interface OnProgressFractionalChanged {
        void onProgressFractionalChanged(Float progress);
    }
}
