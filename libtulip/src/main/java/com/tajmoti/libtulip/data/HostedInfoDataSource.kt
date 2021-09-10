package com.tajmoti.libtulip.data

import com.tajmoti.libtulip.model.hosted.HostedEpisode
import com.tajmoti.libtulip.model.hosted.HostedItem
import com.tajmoti.libtulip.model.hosted.HostedMovie
import com.tajmoti.libtulip.model.hosted.HostedSeason
import com.tajmoti.libtulip.model.key.*

interface HostedInfoDataSource {

    suspend fun getItemByKey(key: ItemKey.Hosted): HostedItem? {
        return when (key) {
            is TvShowKey.Hosted -> getTvShowByKey(key)
            is MovieKey.Hosted -> getMovieByKey(key)
        }
    }


    suspend fun getTvShowByKey(key: TvShowKey.Hosted): HostedItem.TvShow?

    suspend fun getTvShowsByTmdbId(key: TvShowKey.Tmdb): List<HostedItem.TvShow>

    suspend fun insertTvShow(show: HostedItem.TvShow)


    suspend fun getSeasonsByTvShow(key: TvShowKey.Hosted): List<HostedSeason>

    suspend fun getSeasonByKey(key: SeasonKey.Hosted): HostedSeason?

    suspend fun insertSeasons(seasons: List<HostedSeason>)


    suspend fun getEpisodesBySeason(key: SeasonKey.Hosted): List<HostedEpisode>

    suspend fun getEpisodeByKey(key: EpisodeKey.Hosted): HostedEpisode?

    suspend fun getEpisodeByTmdbId(key: EpisodeKey.Tmdb): List<HostedEpisode>

    suspend fun insertEpisodes(episodes: List<HostedEpisode>)


    suspend fun getMovieByKey(key: MovieKey.Hosted): HostedItem.Movie?

    suspend fun getMovieByTmdbKey(key: MovieKey.Tmdb): List<HostedMovie>

    suspend fun insertMovie(movie: HostedItem.Movie)
}
