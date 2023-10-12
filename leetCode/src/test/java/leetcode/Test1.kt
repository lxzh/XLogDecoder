@file:Suppress("TestFunctionName", "NonAsciiCharacters")

package leetcode

import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis

internal class Test1 {

    private fun IntIterator.nextIntOrNull() = if (hasNext()) {
        nextInt()
    } else {
        null
    }

    private fun findMedianSortedArrays(nums1: IntArray, nums2: IntArray): Double {
        val total = nums1.size + nums2.size
        val mid = total / 2
        val midMinus = mid - 1
        val isEven = total % 2 == 0
        var sum = 0.0

        val it1 = nums1.iterator()
        val it2 = nums2.iterator()

        var temp1: Int? = null
        var temp2: Int? = null
        repeat(mid + 1) {
            if (temp1 == null) {
                temp1 = it1.nextIntOrNull()
            }
            if (temp2 == null) {
                temp2 = it2.nextIntOrNull()
            }
            temp1.also { t1 ->
                temp2.also { t2 ->
                    val v = when {
                        t1 != null && t2 != null -> {
                            if (t1 < t2) {
                                temp1 = null
                                t1
                            } else {
                                temp2 = null
                                t2
                            }
                        }
                        t1 != null -> {
                            temp1 = null
                            t1
                        }
                        t2 != null -> {
                            temp2 = null
                            t2
                        }
                        else -> {
                            throw IllegalStateException("This will not happen")
                        }
                    }
                    if ((isEven && it == midMinus) || it == mid) {
                        sum += v
                    }
                }
            }
        }
        val r = if (isEven) {
            sum / 2.0
        } else {
            sum
        }
        println("result: $r")
        return r
    }

    @Test
    fun test寻找两个正序数组的中位数() {
        measureTimeMillis {
            findMedianSortedArrays(intArrayOf(1, 3), intArrayOf(2))
        }.also {
            println("sample1, time: $it ms")
        }

        measureTimeMillis {
            findMedianSortedArrays(intArrayOf(1, 2), intArrayOf(3, 4))
        }.also {
            println("sample2, time: $it ms")
        }

        measureTimeMillis {
            findMedianSortedArrays(intArrayOf(1, 2, 2, 3, 4, 5, 7, 8), intArrayOf(1, 3, 4, 4, 9, 9, 10))
        }.also {
            println("sample3, time: $it ms")
        }

    }

}
