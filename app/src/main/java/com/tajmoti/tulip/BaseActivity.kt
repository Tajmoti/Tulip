package com.tajmoti.tulip

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

    /**
     * Controls whether the back button will be shown
     */
    open val shouldShowBackButton = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (shouldShowBackButton)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding = DataBindingUtil.setContentView(this, bindingInflater)
        binding.lifecycleOwner = this
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}