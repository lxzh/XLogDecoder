package common

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.python.core.PyArray
import org.python.core.PyFunction
import org.python.util.PythonInterpreter


internal class JPythonTest {

    @Test
    fun jPythonTest() = runBlocking {
        val pyInterpreter = PythonInterpreter()
        pyInterpreter.execfile("F:/log/decode_mars_nocrypt_log_file.py")
        val func = pyInterpreter.get("main", PyFunction::class.java)
        val arg = PyArray(
            String::class.java,
            arrayOf(
                "F:/log/test/2.2.190320.debug.mvs64_20210714.xlog",
                "F:/log/test/2.2.190320.debug.mvs64_20210714.log"
            )
        )
        val pyobj = func.__call__(arg)
        println(pyobj)
    }
}