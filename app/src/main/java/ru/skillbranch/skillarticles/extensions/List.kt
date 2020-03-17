package ru.skillbranch.skillarticles.extensions

import kotlin.math.*

fun List<Pair<Int,Int>>.groupByBounds(bounds: List<Pair<Int,Int>>): List<List<Pair<Int, Int>>> {
    val result  = mutableListOf<List<Pair<Int,Int>>>()
    bounds.forEach {(intervalLow, intervalHigh) ->
        result.add(this
            .filter{(foundLow, foundHigh) -> foundLow < intervalHigh && foundHigh > intervalLow}
            .map{(low, high) -> Pair(max(intervalLow, low), min(intervalHigh, high))})}
    return result
}
