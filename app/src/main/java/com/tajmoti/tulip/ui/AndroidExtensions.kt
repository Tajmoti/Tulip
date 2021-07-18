package com.tajmoti.tulip.ui

import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.setupWithAdapterAndDivider(adapter: RecyclerView.Adapter<*>) {
    this.adapter = adapter
    addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
}