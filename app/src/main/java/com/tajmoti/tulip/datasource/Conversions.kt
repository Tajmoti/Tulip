@file:Suppress("NOTHING_TO_INLINE")

package com.tajmoti.tulip.datasource

import com.tajmoti.libtulip.model.history.LastPlayedPosition
import com.tajmoti.libtulip.model.info.*
import com.tajmoti.libtulip.model.key.*
import com.tajmoti.libtulip.model.tmdb.TmdbItemId
import com.tajmoti.libtvprovider.TvItemInfo
import com.tajmoti.tulip.db.entity.hosted.DbEpisode
import com.tajmoti.tulip.db.entity.hosted.DbMovie
import com.tajmoti.tulip.db.entity.hosted.DbSeason
import com.tajmoti.tulip.db.entity.hosted.DbTvShow
import com.tajmoti.tulip.db.entity.tmdb.DbTmdbEpisode
import com.tajmoti.tulip.db.entity.tmdb.DbTmdbMovie
import com.tajmoti.tulip.db.entity.tmdb.DbTmdbSeason
import com.tajmoti.tulip.db.entity.tmdb.DbTmdbTv
import com.tajmoti.tulip.db.entity.userdata.DbFavoriteHostedItem
import com.tajmoti.tulip.db.entity.userdata.DbFavoriteTmdbItem
import com.tajmoti.tulip.db.entity.userdata.DbLastPlayedPositionHosted
import com.tajmoti.tulip.db.entity.userdata.DbLastPlayedPositionTmdb

internal inline fun DbTmdbTv.fromDb(
    key: TvShowKey.Tmdb,
    seasons: List<TulipSeasonInfo.Tmdb>
): TulipTvShowInfo.Tmdb {
    return TulipTvShowInfo.Tmdb(key, name, null, posterPath, backdropPath, seasons)
}

internal inline fun DbTmdbSeason.fromDb(
    key: SeasonKey.Tmdb,
    episodes: List<TulipEpisodeInfo.Tmdb>
): TulipSeasonInfo.Tmdb {
    return TulipSeasonInfo.Tmdb(key, name, overview, episodes)
}

internal inline fun DbTmdbEpisode.fromDb(seasonKey: SeasonKey.Tmdb): TulipEpisodeInfo.Tmdb {
    val key = EpisodeKey.Tmdb(seasonKey, episodeNumber)
    return TulipEpisodeInfo.Tmdb(key, name, overview, stillPath, voteAverage)
}

internal inline fun DbTmdbEpisode.fromDb(key: EpisodeKey.Tmdb): TulipEpisodeInfo.Tmdb {
    return TulipEpisodeInfo.Tmdb(key, name, overview, stillPath, voteAverage)
}

internal inline fun DbTmdbMovie.fromDb(): TulipMovie.Tmdb {
    return TulipMovie.Tmdb(
        MovieKey.Tmdb(TmdbItemId.Movie(id)),
        name,
        overview,
        posterPath,
        backdropPath
    )
}

internal inline fun TulipTvShowInfo.Tmdb.toDb(): DbTmdbTv {
    return DbTmdbTv(key.id.id, name, posterPath, backdropPath)
}

internal inline fun TulipSeasonInfo.Tmdb.toDb(tvId: Long): DbTmdbSeason {
    return DbTmdbSeason(tvId, name, overview, key.seasonNumber)
}

internal inline fun TulipEpisodeInfo.Tmdb.toDb(tvId: Long): DbTmdbEpisode {
    return DbTmdbEpisode(tvId, key.seasonNumber, key.episodeNumber, name, overview, stillPath, voteAverage)
}

internal inline fun TulipMovie.Tmdb.toDb(): DbTmdbMovie {
    return DbTmdbMovie(key.id.id, name, overview, posterPath, backdropPath)
}


internal inline fun DbFavoriteTmdbItem.fromDb(): ItemKey {
    val id = if (type == ItemType.TV_SHOW) {
        TvShowKey.Tmdb(TmdbItemId.Tv(tmdbItemId))
    } else {
        MovieKey.Tmdb(TmdbItemId.Movie(tmdbItemId))
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
    val type = if (id is TmdbItemId.Tv) {
        ItemType.TV_SHOW
    } else {
        ItemType.MOVIE
    }
    return DbFavoriteTmdbItem(type, id.id)
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
    seasons: List<TulipSeasonInfo.Hosted>
): TulipTvShowInfo.Hosted {
    val info = TvItemInfo(key, name, language, firstAirDateYear)
    return TulipTvShowInfo.Hosted(tvShowKey, info, tmdbId?.let { TmdbItemId.Tv(it) }, seasons)
}

internal inline fun DbSeason.fromDb(
    tvShowKey: TvShowKey.Hosted,
    episodes: List<TulipEpisodeInfo.Hosted>
): TulipSeasonInfo.Hosted {
    val key = SeasonKey.Hosted(tvShowKey, number)
    return TulipSeasonInfo.Hosted(key, episodes)
}

internal inline fun DbEpisode.fromDb(): TulipEpisodeInfo.Hosted {
    val tvShowKey = TvShowKey.Hosted(service, tvShowKey)
    val seasonKey = SeasonKey.Hosted(tvShowKey, seasonNumber)
    return fromDb(seasonKey)
}

internal inline fun DbEpisode.fromDb(seasonKey: SeasonKey.Hosted): TulipEpisodeInfo.Hosted {
    val key = EpisodeKey.Hosted(seasonKey, key)
    return TulipEpisodeInfo.Hosted(key, number, name, overview)
}

internal inline fun DbMovie.fromDb(tmdbId: Long?): TulipMovie.Hosted {
    val movieKey = MovieKey.Hosted(service, key)
    return fromDb(movieKey, tmdbId)
}

internal inline fun DbMovie.fromDb(movieKey: MovieKey.Hosted, tmdbId: Long?): TulipMovie.Hosted {
    val info = TvItemInfo(key, name, language, firstAirDateYear)
    return TulipMovie.Hosted(movieKey, info, tmdbId?.let { TmdbItemId.Movie(it) })
}

internal inline fun TulipTvShowInfo.Hosted.toDb(info: TvItemInfo): DbTvShow {
    return DbTvShow(
        key.streamingService,
        info.id,
        info.name,
        info.language,
        info.firstAirDateYear
    )
}

internal inline fun TulipSeasonInfo.Hosted.toDb(): DbSeason {
    return DbSeason(key.streamingService, key.tvShowKey.id, key.seasonNumber)
}

internal inline fun TulipEpisodeInfo.Hosted.toDb(): DbEpisode {
    return DbEpisode(
        key.streamingService,
        key.tvShowKey.id,
        key.seasonNumber,
        key.id,
        episodeNumber,
        name,
        overview
    )
}

internal inline fun TulipMovie.Hosted.toDb(info: TvItemInfo): DbMovie {
    return DbMovie(
        key.streamingService,
        info.id,
        info.name,
        info.language,
        info.firstAirDateYear
    )
}

internal inline fun DbLastPlayedPositionTmdb.fromDb(): LastPlayedPosition.Tmdb {
    val tvShowKey = TvShowKey.Tmdb(TmdbItemId.Tv(tvShowId))
    val seasonKey = SeasonKey.Tmdb(tvShowKey, seasonNumber)
    val key = EpisodeKey.Tmdb(seasonKey, episodeNumber)
    return LastPlayedPosition.Tmdb(key, progress)
}

internal inline fun DbLastPlayedPositionHosted.fromDb(): LastPlayedPosition.Hosted {
    val tvShowKey = TvShowKey.Hosted(streamingService, tvShowId)
    val seasonKey = SeasonKey.Hosted(tvShowKey, seasonNumber)
    val key = EpisodeKey.Hosted(seasonKey, episodeId)
    return LastPlayedPosition.Hosted(key, progress)
}

internal inline fun EpisodeKey.Tmdb.toLastPositionDb(progress: Float?): DbLastPlayedPositionTmdb {
    return DbLastPlayedPositionTmdb(
        tvShowKey.id.id,
        seasonNumber,
        episodeNumber,
        progress
    )
}

internal inline fun EpisodeKey.Hosted.toLastPositionDb(progress: Float?): DbLastPlayedPositionHosted {
    return DbLastPlayedPositionHosted(
        tvShowKey.streamingService,
        tvShowKey.id,
        seasonNumber,
        id,
        progress
    )
}