package com.simple.commonutils.common

object TimeUtils {

    /**
     * 0:10
     * 44:25
     * 110:35:23
     */
    fun convertTime(count: Long): String {
        if (count < 0) throw IllegalArgumentException("count can't < 0")
        return when (count) {
            in 0 until 60 -> {//0s-60s
                if (count > 9) {
                    "0:$count"
                } else {
                    "0:0$count"
                }
            }
            in 60 * 1 until 60 * 60 -> {//1m-1h
                val m = count / 60
                val s = count % 60
                if (s > 9) {
                    "$m:$s"
                } else {
                    "$m:0$s"
                }
            }
            else -> {//>=1h
                val h = count / 60 / 60
                val temp = count - 60 * 60 * h
                val m = temp / 60
                val s = temp % 60
                "$h:${if (m > 9) "$m" else "0$m"}:${if (s > 9) "$s" else "0$s"}"
            }
        }
    }
}
