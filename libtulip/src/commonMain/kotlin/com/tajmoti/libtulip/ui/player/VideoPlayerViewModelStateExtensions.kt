package com.tajmoti.libtulip.ui.player

import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.ui.streams.FailedLink
import com.tajmoti.libtulip.ui.streams.LoadedLink
import com.tajmoti.libtulip.ui.streams.SelectedLink

/**
 * True if the currently playing streamable is a TV show, false if it is a movie.
 */
val VideoPlayerViewModel.State.isTvShow
    get() = streamableKey is EpisodeKey

val VideoPlayerViewModel.State.streamLoadingFinalSuccessState
    get() = (linkListLoadingState as? VideoPlayerViewModel.LinkListLoadingState.Success)?.takeIf { success -> success.final }

/**
 * Whether link list loading is finished and at least one stream was found.
 */
val VideoPlayerViewModel.State.linksAnyResult
    get() = (linkListLoadingState as? VideoPlayerViewModel.LinkListLoadingState.Success)?.streams?.any() ?: false

/**
 * Whether link list loading is finished, but no streams were found.
 */
val VideoPlayerViewModel.State.linksNoResult
    get() = streamLoadingFinalSuccessState?.streams?.none() ?: false

/**
 * Loaded links of the currently selected [VideoPlayerViewModel.State.streamableKey] or null if not yet available.
 * This value is updated in real time as more links are loaded in.
 */
val VideoPlayerViewModel.State.linksResult
    get() = (linkListLoadingState as? VideoPlayerViewModel.LinkListLoadingState.Success)?.streams

/**
 * Whether stream links are being loaded right now and there are no loaded links yet.
 */
val VideoPlayerViewModel.State.linksLoading
    get() = linkListLoadingState is VideoPlayerViewModel.LinkListLoadingState.Loading || (linkListLoadingState is VideoPlayerViewModel.LinkListLoadingState.Success && linkListLoadingState.streams.isEmpty() && !linksNoResult)

/**
 * Whether a direct link is being loaded right now.
 */
val VideoPlayerViewModel.State.loadingStreamOrDirectLink
    get() = linkLoadingState is VideoPlayerViewModel.LinkLoadingState.Loading || linkLoadingState is VideoPlayerViewModel.LinkLoadingState.LoadingDirect

/**
 * Selected item which has redirects resolved, but doesn't support direct link loading.
 */
val VideoPlayerViewModel.State.directLoadingUnsupported
    get() = (linkLoadingState as? VideoPlayerViewModel.LinkLoadingState.DirectLinkUnsupported)?.let {
        SelectedLink(
            it.stream,
            it.download
        )
    }

/**
 * Link that was selected for playback. It might be loading, be already loaded, or errored out.
 */
val VideoPlayerViewModel.State.videoLinkPreparingOrPlaying
    get() = linkLoadingState.stream.takeIf { !linkLoadingState.download }

/**
 * Selected item with direct link loaded.
 */
val VideoPlayerViewModel.State.videoLinkToPlay
    get() = (linkLoadingState as? VideoPlayerViewModel.LinkLoadingState.LoadedDirect)?.takeIf { !it.download }
        ?.let { LoadedLink(it.stream, it.directLink) }

/**
 * Selected item for playing is loaded
 */
val VideoPlayerViewModel.State.videoLinkToDownload
    get() = (linkLoadingState as? VideoPlayerViewModel.LinkLoadingState.LoadedDirect)?.takeIf { it.download }
        ?.let { LoadedLink(it.stream, it.directLink) }

/**
 * Selected item failed to load redirects or failed to load direct link.
 */
val VideoPlayerViewModel.State.linkLoadingError
    get() = (linkLoadingState as? VideoPlayerViewModel.LinkLoadingState.Error)?.let {
        FailedLink(
            it.stream,
            it.languageCode,
            it.download,
            it.captcha
        )
    }

/**
 * Successful result of subtitle loading.
 */
val VideoPlayerViewModel.State.subtitleList
    get() = (loadingSubtitlesState as? VideoPlayerViewModel.SubtitleListLoadingState.Success)?.subtitles ?: emptyList()

/**
 * Whether the list of available subtitles is being loaded right now.
 */
val VideoPlayerViewModel.State.loadingSubtitleList
    get() = subtitleDownloadState is VideoPlayerViewModel.SubtitleDownloadingState.Loading

/**
 * Whether subtitles are loaded and can be selected.
 */
val VideoPlayerViewModel.State.subtitlesReadyToSelect
    get() = loadingSubtitlesState is VideoPlayerViewModel.SubtitleListLoadingState.Loading

/**
 * Whether some subtitles file is being downloaded right now.
 */
val VideoPlayerViewModel.State.downloadingSubtitles
    get() = loadingSubtitlesState is VideoPlayerViewModel.SubtitleListLoadingState.Success

/**
 * Whether there was some error while downloading the selected subtitle file.
 */
val VideoPlayerViewModel.State.subtitleDownloadError
    get() = subtitleDownloadState is VideoPlayerViewModel.SubtitleDownloadingState.Error

/**
 * Subtitles to be applied to the currently playing video
 */
val VideoPlayerViewModel.State.subtitleFile
    get() = (subtitleDownloadState as? VideoPlayerViewModel.SubtitleDownloadingState.Success)?.subtitles

/**
 * How much the subtitles should be offset.
 * Positive values mean that subtitles must be delayed,
 * negative values mean they need to be shown earlier.
 */
val VideoPlayerViewModel.State.subtitleOffset
    get() = (subSyncState as? VideoPlayerViewModel.SubtitleSyncState.OffsetUsed)?.offsetMs ?: 0L