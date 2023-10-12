package common.coroutine

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

internal class FlowTest2 {
    @Test
    fun flowTest(): Unit = runBlocking {
        val a = MutableSharedFlow<String>()
        val b = MutableSharedFlow<Int>()
        val all: StateFlow<Int> = merge(
            a.map {
                32
            },
            b
        ).stateIn(
            scope = this,
            started = SharingStarted.Lazily,
            initialValue = 0
        )

        all.onEach {
            println("all: $it")
        }.launchIn(this)

        launch {
            while (isActive) {
                delay(1000)
                b.emit(0)
                delay(1000)
                a.emit("")
            }
        }
    }

}