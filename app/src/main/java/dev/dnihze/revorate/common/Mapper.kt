package dev.dnihze.revorate.common

interface Mapper<in F: Any, out T: Any?> {
    fun map(from: F): T
}