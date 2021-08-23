package com.tajmoti.tulip.ui.season

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.tajmoti.libtulip.model.info.EpisodeInfoWithKey
import com.tajmoti.libtulip.model.info.TulipEpisodeInfo
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.service.HostedTvDataService
import com.tajmoti.libtulip.service.TvDataService
import com.tajmoti.tulip.R
import com.tajmoti.tulip.ui.performStatefulOneshotOperation
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SeasonViewModel @Inject constructor(
    private val hostedTvDataService: HostedTvDataService,
    private val tmdbTvDataService: TvDataService
) : ViewModel() {
    private val _state = MutableLiveData<State>(State.Idle)

    /**
     * State of this ViewModel represented as values of [State]
     */
    val state: LiveData<State> = _state

    /**
     * Text to be shown in case of error or null
     */
    val statusText = Transformations.map(state) {
        when (it) {
            is State.Error -> R.string.something_went_wrong
            else -> null
        }
    }


    fun fetchEpisodes(key: SeasonKey) {
        performStatefulOneshotOperation(_state, State.Loading, State.Idle) {
            fetchEpisodesToState(key)
        }
    }

    private suspend fun fetchEpisodesToState(key: SeasonKey): State {
        return when (key) {
            is SeasonKey.Hosted -> fetchEpisodesToStateHosted(key)
            is SeasonKey.Tmdb -> fetchEpisodesToStateTmdb(key)
        }
    }

    private suspend fun fetchEpisodesToStateHosted(key: SeasonKey.Hosted): State {
        val season = hostedTvDataService.getSeason(key)
            .getOrElse { return State.Error(it.message ?: it.javaClass.name) }
        val episodes = season.episodes.map {
            val tmdbKey = EpisodeKey.Hosted(key, it.key)
            val info = TulipEpisodeInfo(it.number, it.name)
            info to tmdbKey
        }
        return State.Success(episodes)
    }

    private suspend fun fetchEpisodesToStateTmdb(key: SeasonKey.Tmdb): State {
        val seasons = tmdbTvDataService.getSeason(key)
            .getOrElse { return State.Error(it.message ?: it.javaClass.name) }
        val episodes = seasons.episodes.map {
            val tmdbKey = EpisodeKey.Tmdb(key, it.episodeNumber)
            val info = TulipEpisodeInfo(it.episodeNumber, it.name)
            info to tmdbKey
        }
        return State.Success(episodes)
    }

    sealed class State {
        object Idle : State()
        object Loading : State()
        data class Success(val episodes: List<EpisodeInfoWithKey>) : State()
        data class Error(val message: String) : State()

        val success: Boolean
            get() = this is Success
    }
}