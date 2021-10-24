package com.tajmoti.tulip.gui.tvshow

import com.tajmoti.libtulip.model.key.StreamableKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.repository.*
import com.tajmoti.libtulip.service.*
import com.tajmoti.libtulip.ui.library.LibraryViewModel
import com.tajmoti.libtulip.ui.library.LibraryViewModelImpl
import com.tajmoti.libtulip.ui.player.VideoPlayerViewModel
import com.tajmoti.libtulip.ui.player.VideoPlayerViewModelImpl
import com.tajmoti.libtulip.ui.search.SearchViewModel
import com.tajmoti.libtulip.ui.search.SearchViewModelImpl
import com.tajmoti.libtulip.ui.tvshow.TvShowViewModel
import com.tajmoti.libtulip.ui.tvshow.TvShowViewModelImpl
import kotlinx.coroutines.CoroutineScope
import java.io.File
import javax.inject.Inject

class ViewModelFactory @Inject constructor(
    private val hostedTvDataRepository: HostedTvDataRepository,
    private val tmdbRepo: TmdbTvDataRepository,
    private val favoritesRepository: FavoritesRepository,
    private val subtitleRepository: SubtitleRepository,
    private val playingHistoryRepository: PlayingHistoryRepository,
    private val videoDownloadService: VideoDownloadService,
    private val streamsRepository: StreamService,
    private val streamService: StreamExtractionService,
    private val mappingSearchService: MappingSearchService,
    private val subtitleService: SubtitleService,
) {
    fun getLibraryViewModel(scope: CoroutineScope): LibraryViewModel {
        return LibraryViewModelImpl(favoritesRepository,
            playingHistoryRepository,
            hostedTvDataRepository,
            tmdbRepo,
            scope)
    }

    fun getSearchViewModel(scope: CoroutineScope): SearchViewModel {
        return SearchViewModelImpl(mappingSearchService, scope)
    }

    fun getTvShowViewModel(scope: CoroutineScope, key: TvShowKey): TvShowViewModel {
        return TvShowViewModelImpl(
            hostedTvDataRepository,
            tmdbRepo,
            favoritesRepository,
            playingHistoryRepository,
            scope,
            key
        )
    }

    fun getPlayerViewModel(scope: CoroutineScope, key: StreamableKey): VideoPlayerViewModel {
        return VideoPlayerViewModelImpl(
            subtitleRepository,
            playingHistoryRepository,
            tmdbRepo,
            hostedTvDataRepository,
            videoDownloadService,
            streamService,
            streamsRepository,
            subtitleService,
            File("."),
            scope,
            key
        )
    }
}