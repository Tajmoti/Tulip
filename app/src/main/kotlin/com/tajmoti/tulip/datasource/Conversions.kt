@file:Suppress("NOTHING_TO_INLINE")

package com.tajmoti.tulip.datasource

import com.tajmoti.libtulip.model.history.LastPlayedPosition
import com.tajmoti.libtulip.model.hosted.TvItemInfo
import com.tajmoti.libtulip.model.info.*
import com.tajmoti.libtulip.model.key.*
import com.tajmoti.tulip.db.entity.hosted.DbEpisode
import com.tajmoti.tulip.db.entity.hosted.DbMovie
import com.tajmoti.tulip.db.entity.hosted.DbSeason
import com.tajmoti.tulip.db.entity.hosted.DbTvShow
import com.tajmoti.tulip.db.entity.tmdb.*
import com.tajmoti.tulip.db.entity.userdata.*

internal inline fun DbTmdbTvWithSeasons.fromDb(
    key: TvShowKey.Tmdb
): TvShow.Tmdb {
    val seasons = seasons.map { it.fromDb(key) }
    return TvShow.Tmdb(key, tvShow.name, null, tvShow.posterPath, tvShow.backdropPath, seasons)
}

internal inline fun DbTmdbSeason.fromDbWithEpisodes(
    key: SeasonKey.Tmdb,
    episodes: List<Episode.Tmdb>
): SeasonWithEpisodes.Tmdb {
    val season = Season.Tmdb(key, name, seasonNumber, overview)
    return SeasonWithEpisodes.Tmdb(season, episodes)
}

internal inline fun DbTmdbSeason.fromDb(tvShowKey: TvShowKey.Tmdb): Season.Tmdb {
    val key = SeasonKey.Tmdb(TvShowKey.Tmdb(tvShowKey.id), seasonNumber)
    return Season.Tmdb(key, name, seasonNumber, overview)
}

internal inline fun DbTmdbEpisode.fromDb(seasonKey: SeasonKey.Tmdb): Episode.Tmdb {
    val key = EpisodeKey.Tmdb(seasonKey, episodeNumber)
    return Episode.Tmdb(key, name, overview, stillPath, voteAverage)
}

internal inline fun DbTmdbEpisode.fromDb(key: EpisodeKey.Tmdb): Episode.Tmdb {
    return Episode.Tmdb(key, name, overview, stillPath, voteAverage)
}

internal inline fun DbTmdbMovie.fromDb(): TulipMovie.Tmdb {
    return TulipMovie.Tmdb(
        MovieKey.Tmdb(id),
        name,
        overview,
        posterPath,
        backdropPath
    )
}

internal inline fun TvShow.Tmdb.toDb(): DbTmdbTv {
    return DbTmdbTv(key.id, name, posterUrl, backdropUrl)
}

internal inline fun Season.Tmdb.toDb(): DbTmdbSeason {
    return DbTmdbSeason(key.tvShowKey.id, name, overview, key.seasonNumber)
}

internal inline fun Episode.Tmdb.toDb(tvId: Long): DbTmdbEpisode {
    return DbTmdbEpisode(tvId, key.seasonNumber, key.episodeNumber, name, overview, stillPath, voteAverage)
}

internal inline fun TulipMovie.Tmdb.toDb(): DbTmdbMovie {
    return DbTmdbMovie(key.id, name, overview, posterUrl, backdropUrl)
}


internal inline fun DbFavoriteTmdbItem.fromDb(): ItemKey {
    val id = if (type == ItemType.TV_SHOW) {
        TvShowKey.Tmdb(tmdbItemId)
    } else {
        MovieKey.Tmdb(tmdbItemId)
    }
    return id
}

internal inline fun DbFavoriteHostedItem.fromDb(): ItemKey {
    return if (type == ItemType.TV_SHOW) {
        TvShowKey.Hosted(streamingService, key)
    } else {
        MovieKey.Hosted(streamingService, key)
    }
}

internal inline fun ItemKey.Tmdb.toDb(): DbFavoriteTmdbItem {
    val type = when (this) {
        is TvShowKey.Tmdb -> ItemType.TV_SHOW
        is MovieKey.Tmdb -> ItemType.MOVIE
    }
    return DbFavoriteTmdbItem(type, id)
}

internal inline fun ItemKey.Hosted.toDb(): DbFavoriteHostedItem {
    val type = when (this) {
        is TvShowKey.Hosted -> ItemType.TV_SHOW
        is MovieKey.Hosted -> ItemType.MOVIE
    }
    return DbFavoriteHostedItem(type, streamingService, id)
}

internal inline fun DbTvShow.fromDb(
    tvShowKey: TvShowKey.Hosted,
    tmdbId: Long?,
    seasons: List<Season.Hosted>
): TvShow.Hosted {
    return TvShow.Hosted(tvShowKey, name, LanguageCode(language), firstAirDateYear, tmdbId?.let { TvShowKey.Tmdb(it) }, seasons)
}

internal inline fun DbSeason.fromDbWithEpisodes(
    tvShowKey: TvShowKey.Hosted,
    episodes: List<Episode.Hosted>
): SeasonWithEpisodes.Hosted {
    val key = SeasonKey.Hosted(tvShowKey, number)
    val season = Season.Hosted(key, number)
    return SeasonWithEpisodes.Hosted(season, episodes)
}

internal inline fun DbSeason.fromDb(
    tvShowKey: TvShowKey.Hosted
): Season.Hosted {
    val key = SeasonKey.Hosted(tvShowKey, number)
    return Season.Hosted(key, number)
}

internal inline fun DbEpisode.fromDb(): Episode.Hosted {
    val tvShowKey = TvShowKey.Hosted(service, tvShowKey)
    val seasonKey = SeasonKey.Hosted(tvShowKey, seasonNumber)
    return fromDb(seasonKey)
}

internal inline fun DbEpisode.fromDb(seasonKey: SeasonKey.Hosted): Episode.Hosted {
    val key = EpisodeKey.Hosted(seasonKey, key)
    return Episode.Hosted(key, number, name, overview, stillPath)
}

internal inline fun DbMovie.fromDb(tmdbId: Long?): TulipMovie.Hosted {
    val movieKey = MovieKey.Hosted(service, key)
    return fromDb(movieKey, tmdbId)
}

internal inline fun DbMovie.fromDb(movieKey: MovieKey.Hosted, tmdbId: Long?): TulipMovie.Hosted {
    val info = TvItemInfo(name, language, firstAirDateYear)
    return TulipMovie.Hosted(movieKey, info, tmdbId?.let { MovieKey.Tmdb(it) })
}

internal inline fun TvShow.Hosted.toDb(info: TvShow.Hosted): DbTvShow {
    return DbTvShow(
        key.streamingService,
        key.id,
        info.name,
        info.language.code,
        info.firstAirDateYear
    )
}

internal inline fun Season.Hosted.toDb(): DbSeason {
    return DbSeason(key.streamingService, key.tvShowKey.id, key.seasonNumber)
}

internal inline fun Episode.Hosted.toDb(): DbEpisode {
    return DbEpisode(
        key.streamingService,
        key.tvShowKey.id,
        key.seasonKey.seasonNumber,
        key.id,
        episodeNumber,
        name,
        overview,
        stillPath
    )
}

internal inline fun TulipMovie.Hosted.toDb(info: TvItemInfo): DbMovie {
    return DbMovie(
        key.streamingService,
        key.id,
        info.name,
        info.language,
        info.firstAirDateYear
    )
}

internal inline fun DbLastPlayedPositionTvShowTmdb.fromDb(): LastPlayedPosition.Tmdb {
    val tvShowKey = TvShowKey.Tmdb(tvShowId)
    val seasonKey = SeasonKey.Tmdb(tvShowKey, seasonNumber)
    val key = EpisodeKey.Tmdb(seasonKey, episodeNumber)
    return LastPlayedPosition.Tmdb(key, progress)
}

internal inline fun DbLastPlayedPositionTvShowHosted.fromDb(): LastPlayedPosition.Hosted {
    val tvShowKey = TvShowKey.Hosted(streamingService, tvShowId)
    val seasonKey = SeasonKey.Hosted(tvShowKey, seasonNumber)
    val key = EpisodeKey.Hosted(seasonKey, episodeId)
    return LastPlayedPosition.Hosted(key, progress)
}

internal inline fun DbLastPlayedPositionMovieTmdb.fromDb(): LastPlayedPosition.Tmdb {
    val key = MovieKey.Tmdb(movieId)
    return LastPlayedPosition.Tmdb(key, progress)
}

internal inline fun DbLastPlayedPositionMovieHosted.fromDb(): LastPlayedPosition.Hosted {
    val key = MovieKey.Hosted(streamingService, movieId)
    return LastPlayedPosition.Hosted(key, progress)
}

internal inline fun EpisodeKey.Tmdb.toLastPositionDb(progress: Float): DbLastPlayedPositionTvShowTmdb {
    return DbLastPlayedPositionTvShowTmdb(
        tvShowKey.id,
        seasonNumber,
        episodeNumber,
        progress
    )
}

internal inline fun EpisodeKey.Hosted.toLastPositionDb(progress: Float): DbLastPlayedPositionTvShowHosted {
    return DbLastPlayedPositionTvShowHosted(
        tvShowKey.streamingService,
        tvShowKey.id,
        seasonKey.seasonNumber,
        id,
        progress
    )
}

internal inline fun MovieKey.Tmdb.toLastPositionDb(progress: Float): DbLastPlayedPositionMovieTmdb {
    return DbLastPlayedPositionMovieTmdb(
        id,
        progress
    )
}

internal inline fun MovieKey.Hosted.toLastPositionDb(progress: Float): DbLastPlayedPositionMovieHosted {
    return DbLastPlayedPositionMovieHosted(
        streamingService,
        id,
        progress
    )
}