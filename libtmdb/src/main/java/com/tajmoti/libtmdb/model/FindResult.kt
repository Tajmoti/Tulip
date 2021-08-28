package com.tajmoti.libtmdb.model

interface FindResult {
    val id: Long
    val name: String
    val posterPath: String?
    val backdropPath: String?
}