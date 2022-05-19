package com.tajmoti.libtulip.dto

import com.tajmoti.libtulip.model.key.EpisodeKey

sealed interface EpisodeInfoDto : StreamableInfoDto {
    val tvShowName: String
    val seasonNumber: Int
    val episodeNumber: Int
    val episodeName: String?

    data class Hosted(
        override val tvShowName: String,
        override val seasonNumber: Int,
        override val episodeNumber: Int,
        override val key: EpisodeKey.Hosted,
        override val episodeName: String?,
        override val language: LanguageCodeDto,
    ) : EpisodeInfoDto, StreamableInfoDto.Hosted {
        override val name = episodeName
    }

    data class Tmdb(
        override val tvShowName: String,
        override val seasonNumber: Int,
        override val episodeNumber: Int,
        override val key: EpisodeKey.Tmdb,
        override val episodeName: String?,
    ) : EpisodeInfoDto, StreamableInfoDto.Tmdb {
        override val name = episodeName
    }
}