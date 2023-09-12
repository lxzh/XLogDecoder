package common.coroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import org.junit.jupiter.api.Test
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Suppress("DuplicatedCode")
internal class ContinuationTest {

    companion object {
        const val TEST_PATH = "E:\\Note\\MarkDown\\Kotlin\\test"
    }

    @Test
    fun suspendCoroutineTest() = runBlocking {
        println("111")
        val j = launch {
            val r = test(this)
            println(r)
        }
        delay(100)
        j.cancel()
        println("333")
        delay(5000)
    }

    private suspend fun suspendCoroutineGetTest(): String? = suspendCoroutine {
        //delay(1000L)
        it.resume("hhhhh")
        //it.resumeWithException(IllegalStateException("test"))
    }


    @Test
    fun flowTest(): Unit = runBlocking {
        println("start")
        foo().transform {
            println("emit $it")
            if (it > 7) {
                emit("transform to string :$it")
                emit("emit second :$it")
            }
        }.conflate().onEach {
            println("onEach $it")
        }.onCompletion {

        }.catch {

        }.retry {
            it.printStackTrace()
            false
        }.collect {
            delay(100L)
            println(it)
        }
        println("end")
    }

    private fun getDevices() = flow {
        val list: MutableList<Int> = ArrayList()
        repeat(1000) { i ->
            list.add(i)
        }
        repeat(20) {
            delay(100)
            //Timber.i("emit: $it")
            emit(list)
        }
    }

    private fun foo() = flow {
        repeat(10) {
            delay(100)
            emit(it)
        }
    }

    @Test
    fun launchTest(): Unit = runBlocking {
        val job = launch {
            repeat(1000) { i ->
                println("job: I'm sleeping $i ...")
                delay(500L)
            }
        }
        delay(1300L) // 延迟一段时间
        println("main: I'm tired of waiting!")
        job.cancel() // 取消该作业
        job.join() // 等待作业执行结束
        println("main: Now I can quit.")
    }

    @Test
    fun zipTest() = runBlocking {
        val nums = (1..6).asFlow().onEach {
            delay(10)
        } // 数字 1..3
        val strs = flowOf("one", "two", "three").onEach {
            delay(50)
        } // 字符串
        nums.combine(strs) { a, b ->
            "$a -> $b"
        }.collect {
            println(it)
        }
    }

    @Test
    fun asyncTest(): Unit = runBlocking {
        launch(Dispatchers.Default) {
            println("11111")
            withContext(Dispatchers.IO) {
                delay(5000)
                println("22222")
            }
            println("33333")
            println("44444")
        }
    }

    @Test
    fun withContextTest(): Unit = runBlocking {
        launch(Dispatchers.Default) {
            println("11111")
            launch(Dispatchers.IO) {
                //println(test())
            }
            println("33333")
            println("44444")
        }
    }

    suspend fun test(scope: CoroutineScope): String = suspendCancellableCoroutine {
        println(Thread.currentThread().name)
        scope.launch {
            delay(1000)
            println(" start resume 22222")
            it.resume("22222")
        }
    }

    @Test
    fun flowTest2(): Unit = runBlocking {
        val flowData: MutableSharedFlow<Int> = MutableSharedFlow(1, 0, BufferOverflow.DROP_LATEST)
        launch {
            for (i in 0..100) {
                delay(10)
                if (flowData.tryEmit(i)) {
                    println("emit $i")
                }
            }
        }
        launch {
            flowData.onEach {
                println("onEach: $it")
            }.collect {
                delay(1000)
                println("collect: $it")
            }
        }
    }


}