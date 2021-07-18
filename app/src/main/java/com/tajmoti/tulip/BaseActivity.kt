package com.tajmoti.tulip

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<B : ViewBinding> : AppCompatActivity() {
    protected lateinit var binding: B

    abstract val bindingInflater: (LayoutInflater) -> B

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