package com.tajmoti.tulip.ui.player.streams

import androidx.lifecycle.viewModelScope
import com.tajmoti.libtulip.repository.StreamsRepository
import com.tajmoti.libtulip.service.LanguageMappingStreamService
import com.tajmoti.libtulip.service.VideoDownloadService
import com.tajmoti.libtulip.ui.streams.StreamsViewModel
import com.tajmoti.libtulip.ui.streams.StreamsViewModelImpl
import com.tajmoti.tulip.ui.DelegatingViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AndroidStreamsViewModel @Inject constructor(
    downloadService: VideoDownloadService,
    extractionService: StreamsRepository,
    streamService: LanguageMappingStreamService
) : DelegatingViewModel<StreamsViewModel>() {
    override val impl = StreamsViewModelImpl(
        downloadService,
        extractionService,
        streamService,
        viewModelScope
    )
}