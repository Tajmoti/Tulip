@file:Suppress("NOTHING_TO_INLINE")

package com.tajmoti.tulip.repository.impl

import com.tajmoti.libtmdb.model.movie.Movie
import com.tajmoti.libtmdb.model.tv.Episode
import com.tajmoti.libtmdb.model.tv.Season
import com.tajmoti.libtmdb.model.tv.SlimSeason
import com.tajmoti.libtmdb.model.tv.Tv
import com.tajmoti.libtulip.model.hosted.HostedEpisode
import com.tajmoti.libtulip.model.hosted.HostedItem
import com.tajmoti.libtulip.model.hosted.HostedMovie
import com.tajmoti.libtulip.model.hosted.HostedSeason
import com.tajmoti.libtulip.model.info.ItemType
import com.tajmoti.libtulip.model.key.ItemKey
import com.tajmoti.libtulip.model.key.MovieKey
import com.tajmoti.libtulip.model.key.TvShowKey
import com.tajmoti.libtulip.model.tmdb.TmdbItemId
import com.tajmoti.libtulip.model.userdata.UserFavorite
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


internal inline fun DbTmdbTv.fromDb(seasons: List<SlimSeason>): Tv {
    return Tv(id, name, seasons, posterPath, backdropPath)
}

internal inline fun Season.fromDb(): SlimSeason {
    return SlimSeason(name, overview, seasonNumber)
}

internal inline fun DbTmdbSeason.fromDb(episodes: List<Episode>): Season {
    return Season(name, overview, seasonNumber, episodes)
}

internal inline fun DbTmdbEpisode.fromDb(): Episode {
    return Episode(episodeNumber, seasonNumber, name, overview)
}

internal inline fun DbTmdbMovie.fromDb(): Movie {
    return Movie(id, name, overview, posterPath, backdropPath)
}

internal inline fun Tv.toDb(): DbTmdbTv {
    return DbTmdbTv(id, name, posterPath, backdropPath)
}

internal inline fun Season.toDb(tvId: Long): DbTmdbSeason {
    return DbTmdbSeason(tvId, name, overview, seasonNumber)
}

internal inline fun Episode.toDb(tvId: Long): DbTmdbEpisode {
    return DbTmdbEpisode(tvId, seasonNumber, episodeNumber, name, overview)
}

internal inline fun Movie.toDb(): DbTmdbMovie {
    return DbTmdbMovie(id, name, overview, posterPath, backdropPath)
}


internal inline fun DbFavoriteTmdbItem.fromDb(): UserFavorite {
    val id = if (type == ItemType.TV_SHOW) {
        TvShowKey.Tmdb(TmdbItemId.Tv(tmdbItemId))
    } else {
        MovieKey.Tmdb(TmdbItemId.Movie(tmdbItemId))
    }
    return UserFavorite(id)
}

internal inline fun DbFavoriteHostedItem.fromDb(): UserFavorite {
    return if (type == ItemType.TV_SHOW) {
        UserFavorite(TvShowKey.Hosted(streamingService, key))
    } else {
        UserFavorite(MovieKey.Hosted(streamingService, key))
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
    return DbFavoriteHostedItem(type, streamingService, key)
}

internal inline fun DbTvShow.fromDb(): HostedItem.TvShow {
    val info = TvItemInfo(key, name, language, firstAirDateYear)
    return HostedItem.TvShow(service, info, tmdbId?.let { TmdbItemId.Tv(it) })
}

internal inline fun DbSeason.fromDb(): HostedSeason {
    return HostedSeason(service, tvShowKey, number)
}

internal inline fun DbEpisode.fromDb(): HostedEpisode {
    return HostedEpisode(service, tvShowKey, seasonNumber, key, number, name)
}

internal inline fun DbMovie.fromDb(): HostedMovie {
    return HostedMovie(service, key, name, language)
}

internal inline fun DbMovie.fromDb2(): HostedItem.Movie {
    val info = TvItemInfo(key, name, language, firstAirDateYear)
    return HostedItem.Movie(service, info, tmdbId?.let { TmdbItemId.Movie(it) })
}

internal inline fun HostedItem.TvShow.toDb(info: TvItemInfo): DbTvShow {
    return DbTvShow(
        service,
        info.key,
        info.name,
        info.language,
        info.firstAirDateYear,
        tmdbId?.id
    )
}

internal inline fun HostedSeason.toDb(): DbSeason {
    return DbSeason(service, tvShowKey, number)
}

internal inline fun HostedEpisode.toDb(): DbEpisode {
    return DbEpisode(service, tvShowKey, seasonNumber, key, number, name)
}

internal inline fun HostedItem.Movie.toDb(info: TvItemInfo): DbMovie {
    return DbMovie(service, info.key, info.name, info.language, info.firstAirDateYear, tmdbId?.id)
}