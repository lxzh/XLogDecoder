package common.regex

import kotlinx.coroutines.runBlocking
import java.util.regex.Pattern

@Suppress("DuplicatedCode")
internal class RegexTest {
    @org.junit.jupiter.api.Test
    fun regexTest() = runBlocking {
        val s = """   """
        val pattern = "^(?!.*(/|\\\\|:|\"|<|>|[?]|[*]|[|])).*\$".toRegex()
        val result = pattern.matches(s)
        println(result)
    }

    @org.junit.jupiter.api.Test
    fun regexReplaceTest() = runBlocking {
        val s = "QQQQQ-21-12-30_24.25.22"
        val p = Pattern.compile("\\d{2}-(0\\d|1[012])-([012]\\d|3[01])_([01]\\d|2[0-4])[.][0-5]\\d[.][0-5]\\d")
        val ss = p.matcher(s).replaceAll("22.06.13_12.22.22")
        println(ss)
    }

    @org.junit.jupiter.api.Test
    fun regexReplaceSpaceTest() = runBlocking {
        val s = "QQ Q Q Q -2 f f ffffffff"
        val p = Pattern.compile("[\\s\\p{Zs}]")
        val ss = p.matcher(s).replaceAll(" ")
        println(ss)
    }

}
