package com.tajmoti.libtulip.facade

import com.tajmoti.libtulip.dto.*
import com.tajmoti.libtulip.model.Episode
import com.tajmoti.libtulip.model.SeasonWithEpisodes
import com.tajmoti.libtulip.model.TvShow
import com.tajmoti.libtulip.model.key.*
import com.tajmoti.libtulip.model.result.*
import com.tajmoti.libtulip.repository.UserFavoriteRepository
import com.tajmoti.libtulip.service.HostedTvDataRepository
import com.tajmoti.libtulip.service.TmdbTvDataRepository
import com.tajmoti.libtulip.service.getSeasons
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class TvShowInfoFacadeImpl(
    private val hostedTvDataRepository: HostedTvDataRepository,
    private val tmdbRepo: TmdbTvDataRepository,
    private val favoritesRepository: UserFavoriteRepository
) : TvShowInfoFacade {
    override fun getTvShowInfo(key: TvShowKey): Flow<NetworkResult<TvShowDto>> {
        val isFavoriteFlow = favoritesRepository.isFavorite(key)
        val baseInfoFlow = getTvShowByKey(key)
        val seasonFlow = getTvShowSeasons(key)
        return combine(baseInfoFlow, seasonFlow, isFavoriteFlow) { info, seasons, isFavorite ->
            combineResults(info, seasons, NetworkResult.Success(isFavorite)) { a, b, c ->
                toResultDto(a, b, c)
            }
        }
    }

    override fun getSeason(key: SeasonKey): Flow<NetworkResult<SeasonDto>> {
        return when (key) {
            is SeasonKey.Hosted -> hostedTvDataRepository.getSeasonWithEpisodes(key).mapEach(::seasonToDto)
            is SeasonKey.Tmdb -> tmdbRepo.getSeasonWithEpisodes(key).mapEach(::seasonToDto)
        }
    }

    override fun getStreamableInfo(key: StreamableKey): Flow<Result<StreamableInfoDto>> {
        return when (key) {
            is EpisodeKey.Tmdb -> tmdbRepo.getFullEpisodeData(key)
            is MovieKey.Tmdb -> tmdbRepo.getMovie(key)
                .map { it.toResult().map { movie -> TulipMovieDto.Tmdb(movie.key, movie.name) } }
            is EpisodeKey.Hosted -> hostedTvDataRepository.getEpisodeInfo(key)
            is MovieKey.Hosted -> hostedTvDataRepository.getMovie(key)
                .map {
                    it.toResult().map { movie ->
                        TulipMovieDto.Hosted(
                            movie.key,
                            TvItemInfoDto(
                                name = movie.info.name,
                                language = movie.info.language,
                                firstAirDateYear = movie.info.firstAirDateYear
                            )
                        )
                    }
                }
        }
    }

    private fun seasonToDto(it: SeasonWithEpisodes): SeasonDto {
        return SeasonDto(it.season.key, it.season.seasonNumber, it.episodes.map { ep -> episodeToDto(ep) })
    }

    private fun episodeToDto(it: Episode): SeasonEpisodeDto {
        val voteAverage = (it as? Episode.Tmdb)?.voteAverage
        return SeasonEpisodeDto(it.key, it.episodeNumber, it.name, it.overview, it.stillPath, voteAverage)
    }

    private fun getTvShowByKey(key: TvShowKey) = when (key) {
        is TvShowKey.Hosted -> hostedTvDataRepository.getTvShow(key)
        is TvShowKey.Tmdb -> tmdbRepo.getTvShow(key)
    }

    private fun getTvShowSeasons(key: TvShowKey) = when (key) {
        is TvShowKey.Hosted -> hostedTvDataRepository.getSeasons(key)
        is TvShowKey.Tmdb -> tmdbRepo.getTvShow(key).mapEach { it.seasons } // TODO performance
    }.mapEachInList { season -> TvShowSeasonDto(season.key, season.seasonNumber) }

    private fun toResultDto(tvShow: TvShow, seasons: List<TvShowSeasonDto>, isFavorite: Boolean) = when (tvShow) {
        is TvShow.Tmdb ->
            // TODO languages, firstAirDateYear
            TvShowDto(tvShow.name, emptyList(), null, tvShow.backdropUrl, seasons, isFavorite)
        is TvShow.Hosted ->
            TvShowDto(tvShow.name, listOf(tvShow.language.code), tvShow.firstAirDateYear, null, seasons, isFavorite)
        else -> throw IllegalStateException("Unknown TV Show type. This shouldn't ever happen.")
    }
}