package com.tajmoti.tulip.mapper

interface Mapper<R, D> {

    fun fromDb(db: D): R

    fun toDb(repo: R): D
}