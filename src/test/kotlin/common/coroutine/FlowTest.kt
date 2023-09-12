package common.coroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.junit.jupiter.api.Test

@Suppress("DuplicatedCode")
internal class FlowTest {

    @Test
    fun dispatchersTest(): Unit = runBlocking {
        launch(Dispatchers.Default) {
            println(Thread.currentThread().name)
        }
        launch(Dispatchers.IO) {
            println(Thread.currentThread().name)
        }
        val j = launch(Dispatchers.Unconfined, CoroutineStart.LAZY) {
            delay(200)
            println(Thread.currentThread().name)
        }
        withContext(Dispatchers.Unconfined) {
            println(1)
            withContext(Dispatchers.Unconfined) { // Nested unconfined
                delay(1000)
                println(2)
            }
            println(3)
        }
        j.start()
        delay(2000)
        println("Done")
    }

    @Test
    fun flowLaunchInTest(): Unit = runBlocking {
        flow {
            repeat(10) {
                kotlinx.coroutines.delay(100)
                emit(it)
            }
        }.onEach {
            println(Thread.currentThread().name)
        }.launchIn(this)
        delay(10000)
    }

}