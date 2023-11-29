package common

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis


internal class CommonTest {
    private var s: String? = ""
    private var s1: String? = null

    @Test
    fun takeIfTest(): Unit = runBlocking {
        var a: Int? = 1
        var b: Int? = 2
        takeIf {
            a != null && b != null
        }?.run {
            println("accept")
        } ?: run {
            println("not accept")
        }
        println("end")
        when {
            a != null && b != null -> {
                println("accept")
            }
            else -> {
                println("not accept")
            }
        }
    }

    @Test
    fun takeQueue(): Unit = runBlocking {
        val queue: Queue<FloatArray> = LimitedQueue(3)
        for (i in 1..100) {
            val value = floatArrayOf(i.toFloat(), i.toFloat(), i.toFloat())
            queue.offer(value)
            queue.forEachIndexed { index, v ->
                println("$index: ${v.toList()}")
            }
        }
    }

    suspend fun test1() {
        val s = "aabccssssssssseeeeeeeeeerrr"
        val list: HashSet<String> = HashSet()
        var count = 0
        val time = measureTimeMillis {
            for (i in 0..s.length) {
                for (j in i + 1..s.length) {
                    count++
                    val temp = s.substring(i, j)
                    list.add(temp)
                }
            }
        }
        println("spend:$time ms, length: ${s.length},count: $count, final result size:${list.size}")
        val builderString = if (list.isEmpty()) {
            "()"
        } else {
            StringBuilder().apply {
                append("(")
                append("'${list.first()}'")
                list.forEachIndexed { index, s ->
                    if (index > 0) {
                        append(",")
                        append("'$s'")
                    }
                }
                append(")")
            }.toString()
        }
        val sql = "select * from table where value in $builderString"
        println("sql 语句长度: ${sql.length}")
    }

    suspend fun test2() {
        val set: MutableSet<String> = HashSet()
        for (i in 0..1_000_000) {
            set.add("$i")
        }
    }

    @Test
    fun commonTest(): Unit = runBlocking {
        val time = measureNanoTime {
            test2()
        }
        if (time / 1_000_000 > 0) {
            println("spend ${time / 1_000_000} ms")
        } else {
            println("spend $time nanoseconds")
        }
    }

    @Test
    fun testTime() {
        val fmt = SimpleDateFormat("yyyyMMdd")
        println(fmt.parse("20211105").time)
        println(fmt.format(Date(1635984000000L)))
    }

    @Test
    fun test3() {
        println("0".fixArea("1", "2"))
    }

    val t by lazy {
        TTT()
    }

    internal class TTT()

    @Test
    fun test4() {
        println("0".fixArea("1", "2"))
        println(t)
        println(t)
        println(t)
    }

    @Test
    fun test5() {
        val v = TestValueClass(setOf(1, 2, 3, 4))
        println("$v")
    }

    @Test
    fun flatMapTest() {
        val list = listOf(listOf(1, 2, 3), listOf(1, 2, 3), listOf(1, 2, 3))
        list.apply {
            println(this)
            flatten().apply {
                println(this)
            }
        }
    }

}

@JvmInline
value class TestValueClass(
    val value: Set<*>,
) {
}

fun <T> Comparable<T>.fixArea(min: T, max: T): T {
    return when {
        this < min -> {
            min
        }
        this > max -> {
            max
        }
        else -> {
            this as T
        }
    }
}

internal class LimitedQueue<E>(private val limit: Int) : LinkedList<E>() {
    override fun add(o: E): Boolean {
        super.add(o)
        while (size > limit) {
            super.remove()
        }
        return true
    }

    companion object {
        private const val serialVersionUID = 1L
    }

}