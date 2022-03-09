package com.tajmoti.libtulip.repository

import com.tajmoti.commonutils.LibraryDispatchers
import com.tajmoti.commonutils.flatMap
import com.tajmoti.commonutils.mapWithContext
import com.tajmoti.libtulip.misc.job.NetFlow
import com.tajmoti.libtulip.model.MissingEntityException
import com.tajmoti.libtulip.model.info.TulipCompleteEpisodeInfo
import com.tajmoti.libtulip.model.info.TulipEpisodeInfo
import com.tajmoti.libtulip.model.info.TulipSeasonInfo
import com.tajmoti.libtulip.model.info.TulipTvShowInfo
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.SeasonKey
import com.tajmoti.libtulip.model.key.tvShowKey
import kotlinx.coroutines.flow.Flow

fun TmdbTvDataRepository.getSeason(key: SeasonKey.Tmdb): NetFlow<TulipSeasonInfo.Tmdb> {
    return getTvShow(key.tvShowKey)
        .mapWithContext(LibraryDispatchers.libraryContext) { it.convert { tvShow -> getCorrectSeason(tvShow, key) } }
}

fun TmdbTvDataRepository.getEpisode(key: EpisodeKey.Tmdb): NetFlow<out TulipEpisodeInfo.Tmdb> {
    return getTvShow(key.tvShowKey)
        .mapWithContext(LibraryDispatchers.libraryContext) { it.convert { tvShow -> getCorrectEpisode(tvShow, key) } }
}

fun TmdbTvDataRepository.getFullEpisodeData(key: EpisodeKey.Tmdb): Flow<Result<TulipCompleteEpisodeInfo.Tmdb>> {
    return getTvShow(key.tvShowKey)
        .mapWithContext(LibraryDispatchers.libraryContext) { tvShowInfoResult -> tvShowInfoResult.toResult().flatMap { tvInfo -> getEpisodeAsResult(tvInfo, key) } }
}

fun getEpisodeAsResult(tvInfo: TulipTvShowInfo.Tmdb, key: EpisodeKey.Tmdb): Result<TulipCompleteEpisodeInfo.Tmdb> {
    val episode = getCorrectEpisode(tvInfo, key)
    return if (episode != null) {
        Result.success(TulipCompleteEpisodeInfo.Tmdb(tvInfo, episode))
    } else {
        Result.failure(MissingEntityException)
    }
}

fun getCorrectSeason(all: TulipTvShowInfo.Tmdb, key: SeasonKey.Tmdb): TulipSeasonInfo.Tmdb? {
    return all.seasons.firstOrNull { s -> s.key.seasonNumber == key.seasonNumber }
}

fun getCorrectEpisode(all: TulipTvShowInfo.Tmdb, key: EpisodeKey.Tmdb): TulipEpisodeInfo.Tmdb? {
    return getCorrectSeason(all, key.seasonKey)?.episodes?.firstOrNull { e -> e.key == key }
}
