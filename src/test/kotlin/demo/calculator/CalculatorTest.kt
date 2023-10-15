package demo.calculator

import com.mpobjects.bdparsii.eval.Parser
import com.ql.util.express.ExpressRunner
import com.singularsys.jep.Jep
import demo.calculator.tool.Calculator
import kotlinx.coroutines.runBlocking
import net.objecthunter.exp4j.ExpressionBuilder
import org.junit.jupiter.api.Test


internal class CalculatorTest {
    @Test
    fun test(): Unit = runBlocking {
        var s = "(((9*6)-3)/2)"
        test1(s)
        test2(s)
        test3(s)
        test4(s)
        test5(s)
        s = "7-6.3"
        test1(s)
        test2(s)
        test3(s)
        test4(s)
        test5(s)
    }

    private fun test1(s: String) = runCatching {
        Calculator.conversion(s).also {
            println("test1: $s = $it")
        }
    }.onFailure {
        println("test1: error: $it")
    }

    private fun test2(s: String) = runCatching {
        ExpressionBuilder(s).build().evaluate().also {
            println("test2: $s = $it")
        }
    }.onFailure {
        println("test2: error: $it")
    }

    private fun test3(s: String) = runCatching {
        Jep().let {
            it.parse(s)
            it.evaluate()
        }.also {
            println("test3: $s = $it")
        }
    }.onFailure {
        println("test3: error: $it")
    }

    private fun test4(s: String) = runCatching {
        Parser.parse(s).evaluate().toDouble().also {
            println("test4: $s = $it")
        }
    }.onFailure {
        println("test4: error: $it")
    }

    private fun test5(s: String) = runCatching {
        ExpressRunner().execute(s, null, null, true, false).also {
            println("test5: $s = $it")
        }
    }.onFailure {
        println("test5: error: $it")
    }
}