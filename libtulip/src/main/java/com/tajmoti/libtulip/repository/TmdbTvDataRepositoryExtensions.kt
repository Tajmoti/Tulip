package com.tajmoti.libtulip.repository

import com.tajmoti.commonutils.flatMap
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
import kotlinx.coroutines.flow.map

fun TmdbTvDataRepository.getSeason(key: SeasonKey.Tmdb): NetFlow<TulipSeasonInfo.Tmdb> {
    return getTvShow(key.tvShowKey)
        .map { it.convert { tvShow -> getCorrectSeason(tvShow, key) } }
}

fun TmdbTvDataRepository.getEpisode(key: EpisodeKey.Tmdb): NetFlow<out TulipEpisodeInfo.Tmdb> {
    return getTvShow(key.tvShowKey)
        .map { it.convert { tvShow -> getCorrectEpisode(tvShow, key) } }
}

fun TmdbTvDataRepository.getFullEpisodeData(key: EpisodeKey.Tmdb): Flow<Result<TulipCompleteEpisodeInfo.Tmdb>> {
    return getTvShow(key.tvShowKey)
        .map { tvShowInfoResult -> tvShowInfoResult.toResult().flatMap { tvInfo -> getEpisodeAsResult(tvInfo, key) } }
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
