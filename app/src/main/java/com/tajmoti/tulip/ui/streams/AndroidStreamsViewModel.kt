package com.tajmoti.tulip.ui.streams

import androidx.lifecycle.*
import com.tajmoti.libtulip.repository.StreamsRepository
import com.tajmoti.libtulip.service.*
import com.tajmoti.libtulip.ui.streams.StreamsViewModel
import com.tajmoti.libtulip.ui.streams.StreamsViewModelImpl
import com.tajmoti.tulip.ui.DelegatingViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AndroidStreamsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    downloadService: VideoDownloadService,
    extractionService: StreamsRepository,
    streamService: LanguageMappingStreamService
) : DelegatingViewModel<StreamsViewModel>() {
    override val impl = StreamsViewModelImpl(
        downloadService,
        extractionService,
        streamService,
        StreamsFragmentArgs.fromSavedStateHandle(savedStateHandle).streamableKey,
        viewModelScope
    )
}