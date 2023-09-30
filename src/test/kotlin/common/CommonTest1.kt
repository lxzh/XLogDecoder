package common

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test

class CommonTest1 {
    @Test
    fun testNonCancellable(): Unit = runBlocking {
        val l = launch {
            println("1")
            withContext(NonCancellable) {
                delay(500)
                println("2")
            }
            delay(500)
            println("3")
        }
        delay(100)
        l.cancel()
        delay(5_000)
    }

    internal class A {
        @Synchronized
        fun a() {
            println("a start")
            Thread.sleep(100)
            println("a end")
        }

        @Synchronized
        fun b() {
            println("b start")
            Thread.sleep(100)
            println("b end")
        }

        companion object {
            @Synchronized
            fun c() {
                println("c start")
                Thread.sleep(100)
                println("c end")
            }
        }

        fun d() {
            synchronized(Unit) {
                println("d start")
                Thread.sleep(100)
                println("d end")
            }
        }
    }

    @Test
    fun aClassTest(): Unit = runBlocking {
        val a = A()
        launch {
            a.a()
        }
        launch {
            a.d()
        }
    }
}