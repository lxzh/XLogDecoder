package advanced.practice.math

import org.junit.jupiter.api.Test
import java.math.BigInteger

class BigIntTest {
    @Test
    fun bigIntTest() {
        BigInteger("FF", 16).also {
            println(it)
        }
        val a = byteArrayOf(0xFF.toByte())
        val s = BigInteger(1, a).toString(16)
        println(s)
    }
}