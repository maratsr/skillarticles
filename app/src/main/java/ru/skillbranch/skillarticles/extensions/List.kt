package ru.skillbranch.skillarticles.extensions

import kotlin.math.max
import kotlin.math.min

fun List<Pair<Int,Int>>.groupByBounds(bounds: List<Pair<Int,Int>>): List<List<Pair<Int, Int>>> {
    val result  = mutableListOf<List<Pair<Int,Int>>>()
    bounds.forEach {(intervalLow, intervalHigh) ->
        result.add(this
            .filter{(foundLow, foundHigh) -> foundLow < intervalHigh && foundHigh > intervalLow}
            .map{(low, high) -> Pair(max(intervalLow, low), min(intervalHigh, high))})}
    return result
}

//    val outList  = mutableListOf<List<Pair<Int,Int>>>()
//    bounds.forEach { (lb,hb) ->
//        run {
//            val insideBounds =
//                this.filter { (lbound, hbound) -> lbound >= lb && hbound <= hb }
//            outList.add(insideBounds)
//        }
//    }
//    return  outList

