package com.tajmoti.libtulip.dto

import com.tajmoti.libtulip.model.key.MovieKey

sealed interface TulipMovieDto : StreamableInfoDto {
    override val name: String

    data class Tmdb(
        override val key: MovieKey.Tmdb,
        override val name: String,
    ) : TulipMovieDto, StreamableInfoDto.Tmdb

    class Hosted(
        override val key: MovieKey.Hosted,
        val info: TvItemInfoDto,
    ) : TulipMovieDto, StreamableInfoDto.Hosted {
        override val name = info.name
        override val language = LanguageCodeDto(info.language)
    }
}