package com.tajmoti.tulip.ui.streams

import android.os.Bundle
import android.view.LayoutInflater
import com.tajmoti.libprimewiretvprovider.PrimewireStreamableId
import com.tajmoti.tulip.BaseActivity
import com.tajmoti.tulip.databinding.ActivityTvShowBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StreamsActivity : BaseActivity<ActivityTvShowBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityTvShowBinding =
        ActivityTvShowBinding::inflate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val streamableId = intent.getSerializableExtra(ARG_STREAMABLE_ID)!!
        val frag = StreamsFragment.newInstance(streamableId)
        frag.show(supportFragmentManager, "Streams")
        // TODO fix this up
        val primewireStreamable = streamableId as PrimewireStreamableId
        title = primewireStreamable.name
    }

    companion object {
        const val ARG_STREAMABLE_ID = "streamable_id"
    }
}