package common.coroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
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

    private val aFlow = flow {
        repeat(10) {
            println("prepare emit: $it")
            delay(100)
            emit(it)
            println("end emit: $it")
        }
    }

    @Test
    fun flowLaunchInTest(): Unit = runBlocking {
        (this + Dispatchers.IO).launch {
            println(Thread.currentThread().name)
        }
        this.launch(Dispatchers.IO) {
            println(Thread.currentThread().name)
        }
        val useLaunchIn = true
        if (useLaunchIn) {
            aFlow.buffer(
                onBufferOverflow = BufferOverflow.DROP_OLDEST
            ).onEach {
                delay(500)
                println("$it: " + Thread.currentThread().name)
            }.launchIn(this + Dispatchers.IO)
        } else {
            launch(Dispatchers.IO) {
                aFlow.buffer(
                    onBufferOverflow = BufferOverflow.DROP_OLDEST
                ).collect {
                    delay(500)
                    println("$it: " + Thread.currentThread().name)
                }
            }
        }
    }

    @Test
    fun flowWithTimeOut(): Unit = runBlocking {
        val r = withContext(Dispatchers.IO) {
            getSuspendCancellableCoroutineResult(this)
        }
        println("result: $r")
    }

    private val aStateFlow: MutableStateFlow<Int?> = MutableStateFlow(null)

    @Test
    fun flowWithTimeOut2(): Unit = runBlocking {
        val aFlow = flow {
            println("wait emit")
            delay(1000)
            emit(null)
            println("success emit")
        }
        val j = launch {
            var i = 0
            while (isActive) {
                delay(1000)
                aStateFlow.emit(null)
            }
        }

        val t = System.currentTimeMillis()
        val r = withTimeoutOrNull(2000) {
            aFlow.firstOrNull {
                it != null
            }
        }
        j.cancel()
        println("${System.currentTimeMillis() - t}ms, result: $r")
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
                withTimeout(300) {
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