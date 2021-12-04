package com.tajmoti.tulip

import android.app.Application
import com.bumptech.glide.Glide
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@HiltAndroidApp
class Tulip : Application() {

    override fun onCreate() {
        super.onCreate()
        // Speed up first use of Glide
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch(Dispatchers.Default) {
            Glide.get(applicationContext)
        }
    }
}