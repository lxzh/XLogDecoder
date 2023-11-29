package common.extension

import kotlinx.coroutines.runBlocking

internal class ExtensionTest {
    @org.junit.jupiter.api.Test
    fun extensionTest(): Unit = runBlocking {
        val s = "test"
        s.t1(s)
        s t2 s
        s.compareTo("22")
        s compareTo "22"
        mapOf(1 to "1", 2 to "2")
    }

    private fun String.t1(string: String) {
        println(string)
    }

    private infix fun String.t2(string: String) {
        println(string)
    }

    private infix fun String.compareTo(string: String) = this.compareTo(string)


    @org.junit.jupiter.api.Test
    fun functionTest(): Unit = runBlocking {
        payFoo {
            println("in block")
        }
    }

    private inline fun payFoo(crossinline block: () -> Unit) {
        println("before block")
        block()
        println("end block")
    }
}