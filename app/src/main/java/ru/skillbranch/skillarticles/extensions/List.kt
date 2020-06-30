package ru.skillbranch.skillarticles.extensions


/*
fun List<Pair<Int,Int>>.groupByBounds(bounds: List<Pair<Int,Int>>): List<List<Pair<Int, Int>>> {
    val result  = mutableListOf<List<Pair<Int,Int>>>()
    bounds.forEach {(intervalLow, intervalHigh) ->
        result.add(this
            .filter{(foundLow, foundHigh) -> foundLow < intervalHigh && foundHigh > intervalLow}
            .map{(low, high) -> Pair(max(intervalLow, low), min(intervalHigh, high))})}
    return result
}

 */

fun List<Pair<Int, Int>>.groupByBounds(bounds: List<Pair<Int, Int>>): List<List<Pair<Int, Int>>> {

    val results= List<MutableList<Pair<Int, Int>>>(bounds.size){mutableListOf()}

    var lastResult = 0

    bounds@ for ((index, bound) in bounds.withIndex()) {
        var lastIndex = bound.first
        results@ for (result in subList(lastResult, size)) {
            val boundRange = lastIndex..bound.second

            when {
                result.first in boundRange && result.second in boundRange -> {
                        results[index].add(result.first to result.second)
                        lastResult++
                        lastIndex = result.second
                }

                result.first in boundRange && result.second !in boundRange -> {
                    if(result.first != bound.second){
                        results[index].add(result.first to bound.second)
                    }
                    continue@bounds
                }

                result.first !in boundRange && result.second in boundRange -> {
                    if(bound.first != result.second){
                        results[index].add(bound.first to result.second)
                    }
                    lastResult++
                    continue@results
                }
            }
        }
    }

    return results
}