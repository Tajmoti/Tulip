package com.tajmoti.libtulip.ui.streams

import com.tajmoti.libtulip.model.info.StreamableInfo
import com.tajmoti.libtulip.model.stream.StreamableInfoWithLangLinks
import com.tajmoti.libtulip.model.stream.UnloadedVideoStreamRef
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface StreamsViewModel {
    /**
     * Whether the stream links are being loaded right now and there are no loaded links yet.
     */
    val linksLoading: StateFlow<Boolean>

    /**
     * Streamable that is being loaded or null if still fetching.
     */
    val streamableInfo: StateFlow<StreamableInfo?>

    /**
     * Flow containing the results to show in the list.
     */
    val linksResult: StateFlow<StreamableInfoWithLangLinks?>

    /**
     * True when loading is finished, but no streams were found.
     */
    val linksNoResult: StateFlow<Boolean>


    /**
     * The user has clicked a link, it needs to be resolved and played.
     */
    fun onStreamClicked(stream: UnloadedVideoStreamRef, download: Boolean)

    /**
     * Whether redirects are being resolved or a direct link is being loaded right now.
     */
    val loadingStreamOrDirectLink: StateFlow<Boolean>

    /**
     * Selected item which has redirects resolved, but doesn't support direct link loading.
     */
    val directLoadingUnsupported: SharedFlow<SelectedLink>

    /**
     * Selected item with direct link loaded.
     */
    val directLoaded: StateFlow<LoadedLink?>

    /**
     * Selected item failed to load redirects or failed to load direct link.
     */
    val linkLoadingError: SharedFlow<FailedLink>
}