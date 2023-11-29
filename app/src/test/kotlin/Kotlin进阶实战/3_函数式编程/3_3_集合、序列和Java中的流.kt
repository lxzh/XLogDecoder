@file:Suppress("unused", "ClassName", "NonAsciiCharacters", "PackageName", "TestFunctionName")

package Kotlin进阶实战.`3_函数式编程`

import org.junit.jupiter.api.Test

class `3_3_集合、序列和Java中的流` {

    //flatMap
    //遍历所有元素，为每个元素创建一个集合，最后把所有的集合放在一个集合中
    @Test
    fun flatMapTest() {
        listOf(5, 12, 8, 33).apply {
            println(this)
            map {
                it + 1
            }.also(::println)
            flatMap {
                listOf(it, it + 1)
            }.also(::println)
            flatMap {
                listOf(1)
            }.also(::println)
        }
    }

}