package common.coroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.junit.jupiter.api.Test
import kotlin.coroutines.resume

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
                delay(100)
                emit(it)
            }
        }.onEach {
            println(Thread.currentThread().name)
        }.launchIn(this)
        delay(10000)
    }

    @Test
    fun flowWithTimeOut(): Unit = runBlocking {
        val r = withContext(Dispatchers.IO) {
            getSuspendCancellableCoroutineResult(this)
        }
        println("result: $r")
    }

    private suspend fun getSuspendCancellableCoroutineResult(
        scope: CoroutineScope
    ) = suspendCancellableCoroutine<Int?> { cont ->
        fun resume(data: Int?) {
            if (cont.isActive) {
                cont.resume(data)
            }
        }

        val f = flow {
            repeat(10) {
                delay(1000)
                emit(it)
            }
        }

        scope.launch {
            try {
                withTimeout(300){
                    f.collect {
                        println("collect it: $it")
                        resume(it)
                        //this@launch.cancel()
                        cancel()
                    }
                }
            } catch (e: Throwable) {
                println(e)
                resume(null)
                //this.cancel()
            }
        }
    }

}