package com.hc.subway_station_locator.app.utils

import android.widget.TextView
import androidx.annotation.StringRes

object Utils {
    fun getIntList(a: Int, b: Int, range: IntRange? = null): List<Int> {
        return if (range == null) {
            getIntList(a, b)
        } else {
            getIntList(a, b).map { range.modulo(it) }
        }
    }

    private fun getIntList(a: Int, b: Int): List<Int> {
        return if (a < b) {
            (a .. b)
        } else {
            (a downTo b)
        }.toList()
    }

    val IntRange.size get() = this.last - this.first + 1

    fun IntRange.modulo(i: Int) =  (i - first) % size + first
}