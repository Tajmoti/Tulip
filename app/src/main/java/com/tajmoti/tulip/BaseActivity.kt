package com.tajmoti.tulip

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<B : ViewBinding>(
    private val bindingInflater: (LayoutInflater) -> B
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
        binding = bindingInflater(layoutInflater)
        setContentView(binding.root)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}