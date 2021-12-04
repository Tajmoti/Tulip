package com.tajmoti.tulip.ui

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.setupWithAdapterAndDivider(adapter: RecyclerView.Adapter<*>) {
    this.adapter = adapter
    addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
}

fun <T : RecyclerView.Adapter<*>> T.setToRecyclerWithDividers(rv: RecyclerView): T {
    rv.setupWithAdapterAndDivider(this)
    return this
}

fun Fragment.toast(@StringRes stringResId: Int) {
    Toast.makeText(requireContext(), stringResId, Toast.LENGTH_SHORT).show()
}

fun AppCompatActivity.toast(@StringRes stringResId: Int) {
    Toast.makeText(this, stringResId, Toast.LENGTH_SHORT).show()
}

fun AppCompatActivity.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Fragment.toast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}