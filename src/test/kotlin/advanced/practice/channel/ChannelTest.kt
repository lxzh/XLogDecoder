package advanced.practice.channel

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.TickerMode
import kotlinx.coroutines.channels.ticker
import org.junit.jupiter.api.Test
import java.math.BigInteger

class ChannelTest {
    @Test
    fun channelTest(): Unit = runBlocking {
        val channel = Channel<Int>(10)

        val j1 = launch {
            repeat(10) {
                delay(200)
                channel.send(it)
            }
            channel.close()
        }
        val j2 = launch {
            delay(5000)
            for (i in channel) {
                println("2 channel receive: $i")
            }
            println("2 channel receive end")
        }
        val j3 = launch {
            for (i in channel) {
                println("3 channel receive: $i")
            }
            println("3 channel receive end")
        }
        while (j1.isActive && j2.isActive && j3.isActive) {
            delay(1000)
        }
    }

    @OptIn(ObsoleteCoroutinesApi::class)
    @Test
    fun tickerChannel(): Unit = runBlocking {
        val tickerChannel = ticker(
            delayMillis = 1000,
            initialDelayMillis = 0,
            mode = TickerMode.FIXED_PERIOD
        )
        var t = System.currentTimeMillis()
        for (i in tickerChannel) {
            println("${System.currentTimeMillis() - t}: tickerChannel receive: $i")
            delay(2000)
            t = System.currentTimeMillis()
        }
    }

    @OptIn(ObsoleteCoroutinesApi::class)
    @Test
    fun withTimeout(): Unit = runBlocking {
        val t1 = withTimeoutOrNull(100) {
            delay(1000)
        }
        println("t1: $t1")
        val t2 = withTimeoutOrNull(100) {
            delay(50)
        }
        println("t2: $t2")
    }

}
