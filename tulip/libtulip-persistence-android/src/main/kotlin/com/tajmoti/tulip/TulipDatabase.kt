package com.tajmoti.tulip

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tajmoti.tulip.dao.ItemMappingDao
import com.tajmoti.tulip.dao.hosted.HostedEpisodeDao
import com.tajmoti.tulip.dao.hosted.HostedMovieDao
import com.tajmoti.tulip.dao.hosted.HostedSeasonDao
import com.tajmoti.tulip.dao.hosted.HostedTvShowDao
import com.tajmoti.tulip.dao.tmdb.TmdbEpisodeDao
import com.tajmoti.tulip.dao.tmdb.TmdbMovieDao
import com.tajmoti.tulip.dao.tmdb.TmdbSeasonDao
import com.tajmoti.tulip.dao.tmdb.TmdbTvShowDao
import com.tajmoti.tulip.dao.user.FavoriteDao
import com.tajmoti.tulip.dao.user.PlayingProgressDao
import com.tajmoti.tulip.entity.ItemMapping
import com.tajmoti.tulip.entity.hosted.HostedEpisode
import com.tajmoti.tulip.entity.hosted.HostedMovie
import com.tajmoti.tulip.entity.hosted.HostedSeason
import com.tajmoti.tulip.entity.hosted.HostedTvShow
import com.tajmoti.tulip.entity.tmdb.TmdbEpisode
import com.tajmoti.tulip.entity.tmdb.TmdbMovie
import com.tajmoti.tulip.entity.tmdb.TmdbSeason
import com.tajmoti.tulip.entity.tmdb.TmdbTvShow
import com.tajmoti.tulip.entity.user.*

@Database(
    entities = [
        HostedTvShow::class,
        HostedSeason::class,
        HostedEpisode::class,
        HostedMovie::class,

        TmdbTvShow::class,
        TmdbSeason::class,
        TmdbEpisode::class,
        TmdbMovie::class,

        ItemMapping::class,

        FavoriteTmdbItem::class,
        FavoriteHostedItem::class,

        PlayingProgressTmdbTvShow::class,
        PlayingProgressTmdbMovie::class,

        PlayingProgressHostedTvShow::class,
        PlayingProgressHostedMovie::class
    ],
    version = 1,
)
abstract class TulipDatabase : RoomDatabase() {
    abstract fun tmdbTvShowDao(): TmdbTvShowDao
    abstract fun tmdbSeasonDao(): TmdbSeasonDao
    abstract fun tmdbEpisodeDao(): TmdbEpisodeDao
    abstract fun tmdbMovieDao(): TmdbMovieDao

    abstract fun hostedTvShowDao(): HostedTvShowDao
    abstract fun hostedSeasonDao(): HostedSeasonDao
    abstract fun hostedEpisodeDao(): HostedEpisodeDao
    abstract fun hostedMovieDao(): HostedMovieDao

    abstract fun tmdbMappingDao(): ItemMappingDao

    abstract fun favoriteDao(): FavoriteDao
    abstract fun playingProgressDao(): PlayingProgressDao
}
