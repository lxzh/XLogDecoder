package common.coroutine

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test

class CoroutineContextTest {

    @Test
    fun coroutineContextTest(): Unit = runBlocking {
        val ack = CompletableDeferred<Unit?>()
        println("1 ${this.coroutineContext}")
        launch(Dispatchers.IO) {
            delay(3000)
            println("2 ${this.coroutineContext}")
            ack.complete(null)
        }
        println("3 ${this.coroutineContext}")
        ack.await()
        println("4 ${this.coroutineContext}")
    }

}