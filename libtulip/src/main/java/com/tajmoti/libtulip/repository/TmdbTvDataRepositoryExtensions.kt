package com.tajmoti.libtulip.repository

import com.tajmoti.libtulip.misc.job.NetFlow
import com.tajmoti.libtulip.model.info.TulipEpisodeInfo
import com.tajmoti.libtulip.model.info.TulipItem
import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import com.tajmoti.libtulip.model.info.TulipTvShowInfo
import com.tajmoti.libtulip.model.key.*
import kotlinx.coroutines.flow.map

fun TmdbTvDataRepository.getItem(key: ItemKey.Tmdb): NetFlow<out TulipItem.Tmdb> {
    return when (key) {
        is TvShowKey.Tmdb -> getTvShow(key)
        is MovieKey.Tmdb -> getMovie(key)
    }
}

fun TmdbTvDataRepository.getSeason(key: SeasonKey.Tmdb): NetFlow<TulipSeasonInfo.Tmdb> {
    return getTvShow(key.tvShowKey)
        .map { it.convert { tvShow -> getCorrectSeason(tvShow, key) } }
}

fun TmdbTvDataRepository.getEpisode(key: EpisodeKey.Tmdb): NetFlow<out TulipEpisodeInfo.Tmdb> {
    return getTvShow(key.tvShowKey)
        .map { it.convert { tvShow -> getCorrectEpisode(tvShow, key) } }
}

fun getCorrectSeason(all: TulipTvShowInfo.Tmdb, key: SeasonKey.Tmdb): TulipSeasonInfo.Tmdb? {
    return all.seasons.firstOrNull { s -> s.key.seasonNumber == key.seasonNumber }
}

fun getCorrectEpisode(all: TulipTvShowInfo.Tmdb, key: EpisodeKey.Tmdb): TulipEpisodeInfo.Tmdb? {
    return getCorrectSeason(all, key.seasonKey)?.episodes?.firstOrNull { e -> e.key == key }
}
