package com.tajmoti.tulip.ui

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

abstract class BaseActivity<B : ViewDataBinding>(
    @LayoutRes
    private val bindingInflater: Int
) : AppCompatActivity() {
    /**
     * View binding belonging to this activity
     */
    protected lateinit var binding: B


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, bindingInflater)
        binding.lifecycleOwner = this
    }
}