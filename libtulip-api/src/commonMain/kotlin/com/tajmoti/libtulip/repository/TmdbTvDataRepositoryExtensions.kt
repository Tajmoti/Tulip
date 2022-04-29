package com.tajmoti.libtulip.repository

import com.tajmoti.libtulip.misc.job.NetFlow
import com.tajmoti.libtulip.model.MissingEntityException
import com.tajmoti.libtulip.model.info.Episode
import com.tajmoti.libtulip.model.info.SeasonWithEpisodes
import com.tajmoti.libtulip.model.info.TulipCompleteEpisodeInfo
import com.tajmoti.libtulip.model.info.TvShow
import com.tajmoti.libtulip.model.key.EpisodeKey
import com.tajmoti.libtulip.model.key.tvShowKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

fun TmdbTvDataRepository.getEpisode(key: EpisodeKey.Tmdb): NetFlow<out Episode.Tmdb> {
    return getSeasonWithEpisodes(key.seasonKey)
        .map { it.convert { tvShow -> getCorrectEpisode(tvShow, key) } }
}

fun TmdbTvDataRepository.getFullEpisodeData(key: EpisodeKey.Tmdb): Flow<Result<TulipCompleteEpisodeInfo.Tmdb>> {
    val tv = getTvShow(key.tvShowKey)
    val episode = getEpisode(key)
    return combine(tv, episode) { t, e ->
        val tvThing = t.data
        val eThing = e.data
        if (tvThing != null && eThing != null) {
            val season = tvThing.seasons.firstOrNull { it.key == eThing.key.seasonKey }
                ?: return@combine Result.failure(MissingEntityException)
            Result.success(TulipCompleteEpisodeInfo.Tmdb(tvThing, season, eThing))
        } else {
            Result.failure(MissingEntityException)
        }
    }
}

fun getEpisodeAsResult(
    tvShowInfo: TvShow.Tmdb,
    seasonInfo: SeasonWithEpisodes.Tmdb,
    key: EpisodeKey.Tmdb
): Result<TulipCompleteEpisodeInfo.Tmdb> {
    val episode = getCorrectEpisode(seasonInfo, key)
    return if (episode != null) {
        Result.success(TulipCompleteEpisodeInfo.Tmdb(tvShowInfo, seasonInfo.season, episode))
    } else {
        Result.failure(MissingEntityException)
    }
}

fun getCorrectEpisode(season: SeasonWithEpisodes.Tmdb, key: EpisodeKey.Tmdb): Episode.Tmdb? {
    return season.episodes.firstOrNull { e -> e.key == key }
}
