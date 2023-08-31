package common

import kotlinx.coroutines.runBlocking

@Suppress("DuplicatedCode")
internal class SVGTest {
    @org.junit.jupiter.api.Test
    fun svgTest() = runBlocking {
        SVGTools.svg2xmlFromDir("F:\\Tencent\\1846321902\\FileRecv")
    }

}
