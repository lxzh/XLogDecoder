package common

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

@Suppress("DuplicatedCode")
internal class SVGTest {
    @Test
    fun svgTest() = runBlocking {
        SVGTools.svg2xmlFromDir("F:\\Tencent\\1846321902\\FileRecv")
    }

}
