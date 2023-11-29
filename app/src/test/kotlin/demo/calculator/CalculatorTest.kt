package demo.calculator

import com.mpobjects.bdparsii.eval.Parser
import com.ql.util.express.ExpressRunner
import com.singularsys.jep.Jep
import demo.calculator.tool.Calculator
import kotlinx.coroutines.runBlocking
import net.objecthunter.exp4j.ExpressionBuilder
import net.sourceforge.jeval.Evaluator
import org.cheffo.jeplite.JEP
import org.cheffo.jeplite.util.DoubleStack
import org.junit.jupiter.api.Test


internal class CalculatorTest {
    @Test
    fun test(): Unit = runBlocking {
        listOf(
            "1.5E3*2",
            "1.5*10^3*2",
            "1.5/10^3*2",
            "0*10^18*0",
            "(((9-6)*3)/2)",
            "7-6.3",
        ).also(::testExpression)
    }

    private fun testExpression(list: List<String>) {
        list.forEach { s ->
            println("\ntest: $s")
            functions.mapIndexed { index, function ->
                val r = kotlin.runCatching {
                    val r = function(s)
                    println("test${index + 1}, $s = $r")
                }.onFailure {
                    println("test${index + 1}, error: $it")
                }
                "test${index + 1} success: ${r.isSuccess}"
            }
        }
    }

    private val functions = listOf<(String) -> Any>(
        { s ->//1
            Calculator.conversion(s)
        },
        { s ->//2
            ExpressionBuilder(s).build().evaluate()
        },
        { s ->//3
            Jep().let {
                it.parse(s)
                it.evaluate()
            }
        },
        { s ->//4
            Parser.parse(s).evaluate().toDouble()
        },
        { s ->//5
            ExpressRunner().execute(s, null, null, true, false)
        },
        { s ->//6
            Evaluator().evaluate(s)
        },
        { s ->//7
            JEP().let {
                it.parseExpression(s)
                it.getValue(DoubleStack())
            }
        }
    )
}