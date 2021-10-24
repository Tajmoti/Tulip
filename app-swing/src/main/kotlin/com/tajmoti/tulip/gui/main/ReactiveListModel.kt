package com.tajmoti.tulip.gui.main

import javax.swing.AbstractListModel

class ReactiveListModel<T> : AbstractListModel<T>() {
    private var _items: List<T> = emptyList()
    var items: List<T>
        get() = _items
        set(value) {
            _items = value
            fireContentsChanged(this, 0, value.size)
        }

    override fun getSize(): Int {
        return _items.size
    }

    override fun getElementAt(p0: Int): T {
        return _items[p0]
    }
}