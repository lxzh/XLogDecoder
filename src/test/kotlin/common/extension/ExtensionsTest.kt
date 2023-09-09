package common

@Suppress("DuplicatedCode")
internal class ExtensionsTest {
    fun <T> MutableList<T>.swap(index1: Int, index2: Int) {
        if (index1 > this.size - 1 || index2 > this.size - 1) {
            throw IllegalArgumentException("index is more than list's size")
        }
        val temp = this[index1]
        this[index1] = this[index2]
        this[index2] = temp
    }

    @org.junit.jupiter.api.Test
    fun listSwap() {
        val list = mutableListOf(1, 2, 3, 4, 5)
        println("origin: $list")
        list.swap(2, 3)
        println("after swap:${list}")
    }


    operator fun String.times(int: Int): String {
        return with(StringBuilder()) {
            repeat(int) {
                append(this@times)
            }
            toString()
        }
    }

    @org.junit.jupiter.api.Test
    fun operatorTest() {
        val s = "hello " * 5
        println(s)
    }

}
