package com.tajmoti.tulip.service.impl

import androidx.room.withTransaction
import com.tajmoti.commonutils.logger
import com.tajmoti.libtvprovider.Episode
import com.tajmoti.libtvprovider.MultiTvProvider
import com.tajmoti.libtvprovider.Season
import com.tajmoti.libtvprovider.TvItem
import com.tajmoti.tulip.db.AppDatabase
import com.tajmoti.tulip.db.MissingEntityException
import com.tajmoti.tulip.model.*
import com.tajmoti.tulip.model.key.*
import com.tajmoti.tulip.service.TvDataService
import javax.inject.Inject

class TvDataServiceImpl @Inject constructor(
    private val db: AppDatabase,
    private val tvProvider: MultiTvProvider<StreamingService>
) : TvDataService {

    override suspend fun getTvShow(key: TvShowKey): Result<TvItem.Show> {
        val dbInfo = db.tvShowDao().getByKey(key)
        if (dbInfo == null) {
            logger.warn("TV Show {} not found", key)
            return Result.failure(MissingEntityException)
        }
        return tvProvider.getShow(key.service, dbInfo.apiInfo)
    }

    override suspend fun getSeason(key: SeasonKey): Result<Season> {
        val dbSeason = db.seasonDao().getSeason(key)
        if (dbSeason == null) {
            logger.warn("Season {} not found", key)
            return Result.failure(MissingEntityException)
        }
        val dbEpisodes = db.episodeDao().getForSeason(key)
        val info = dbSeason.toApiInfo(dbEpisodes)
        return tvProvider.getSeason(key.service, info)
    }

    override suspend fun getStreamable(key: StreamableKey): Result<StreamableInfo> {
        return when (key) {
            is EpisodeKey -> getEpisodeByKey(key)
            is MovieKey -> getMovieByKey(key)
        }
    }

    private suspend fun getMovieByKey(key: MovieKey): Result<StreamableInfo.Movie> {
        val dbMovie = db.movieDao()
            .getByKey(key)
            ?: run {
                logger.warn("Movie {} not found", key)
                return Result.failure(MissingEntityException)
            }
        return tvProvider.getMovie(key.service, dbMovie.apiInfo)
            .map { StreamableInfo.Movie(it) }
    }

    private suspend fun getEpisodeByKey(key: EpisodeKey): Result<StreamableInfo.TvShow> {
        val dbShow = db.tvShowDao()
            .getByKey(key.service, key.tvShowId)
            ?: run {
                logger.warn("Season {} not found", key)
                return Result.failure(MissingEntityException)
            }
        val dbSeason = db.seasonDao()
            .getForShow(key.service, key.tvShowId, key.seasonId)
            ?: run {
                logger.warn("Season {} not found", key)
                return Result.failure(MissingEntityException)
            }
        val dbEpisode = db.episodeDao()
            .getByKey(key.service, key.tvShowId, key.seasonId, key.episodeId)
            ?: run {
                logger.warn("Season {} not found", key)
                return Result.failure(MissingEntityException)
            }
        return tvProvider.getEpisode(key.service, dbEpisode.apiInfo)
            .map { StreamableInfo.TvShow(dbShow, dbSeason, it) }
    }

    override suspend fun searchAndSaveItems(query: String): Result<List<Pair<StreamingService, TvItem>>> {
        val searchResult = tvProvider.search(query)
        for ((service, result) in searchResult) {
            val successfulResult = result.getOrNull() ?: continue
            insertSearchResultsToDb(service, successfulResult)
        }
        if (searchResult.none { it.second.isSuccess }) {
            logger.warn("No successful results for query '{}'", query)
            return Result.failure(NoSuccessfulResultsException)
        }
        val successfulItems = searchResult
            .mapNotNull {
                val res = it.second.getOrNull() ?: return@mapNotNull null
                it.first to res
            }
            .flatMap { a -> a.second.map { a.first to it } }
        return Result.success(successfulItems)
    }

    private suspend fun insertSearchResultsToDb(service: StreamingService, result: List<TvItem>) {
        db.withTransaction {
            for (item in result) {
                insertTvItemIntoDb(item, service)
            }
        }
    }

    private suspend fun insertTvItemIntoDb(item: TvItem, service: StreamingService) {
        when (item) {
            is TvItem.Show -> {
                val dbItem = DbTvShow(service, item)
                db.tvShowDao().insert(dbItem)
            }
            is TvItem.Movie -> {
                val dbItem = DbMovie(service, item)
                db.movieDao().insert(dbItem)
            }
        }
    }

    override suspend fun fetchAndSaveSeasons(
        key: TvShowKey,
        show: TvItem.Show
    ): Result<List<Season>> {
        val result = show.fetchSeasons()
            .getOrElse {
                logger.warn("Failed to fetch seasons for {}", show)
                return Result.failure(it)
            }
        insertSeasonsToDb(key, result)
        return Result.success(result)
    }

    private suspend fun insertSeasonsToDb(key: TvShowKey, result: List<Season>) {
        db.withTransaction {
            for (season in result) {
                insertSeasonIntoDb(key, season)
            }
        }
    }

    private suspend fun insertSeasonIntoDb(key: TvShowKey, season: Season) {
        db.seasonDao().insert(DbSeason(key.service, key.tvShowId, season))
        for (episode in season.episodes) {
            insertEpisodeIntoDb(key, season, episode)
        }
    }

    private suspend fun insertEpisodeIntoDb(key: TvShowKey, season: Season, episode: Episode) {
        val dbEpisode = DbEpisode(key.service, key.tvShowId, season.key, episode)
        db.episodeDao().insert(dbEpisode)
    }
}