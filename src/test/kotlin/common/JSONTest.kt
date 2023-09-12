package common

import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.math.ceil

@Suppress("DuplicatedCode")
internal class JSONTest {
    @Test
    fun jsonTest() = runBlocking {
        val s =
            """“[["TEMP",31.64,""],["HUMI",34.75,"ppb"],["CO",0,"ppm"],["H2S",0,"ppm"],["O2",21.1,"%VOL"],["Ex",0,"%LEL"]]”
"""
        val jsonString = s.replace("“", "").replace("”", "")
        val parser = JsonParser()
        val jsonObject = parser.parse(jsonString).asJsonArray
        jsonObject.forEach {
            val obj = it.asJsonArray
            val first = obj[0].asString
            val secord = obj[1].asInt
            val third = obj[2].asString
            println(first + secord + third)
        }
    }

    @Test
    fun floatTest() = runBlocking {
        val f = -1.00f
        println(ceil(f.toDouble()).toLong())
    }

//    @org.junit.jupiter.api.Test
//    fun jsonParseTest() = runBlocking {
//        val dts = common.DataClassSimple(1,2)
//        val jsons = JSON.toJSONString(dts)
//        println(jsons)
//        val clzs = common.DataClassSimple::class
//        println(clzs.javaObjectType)
//        val dt2 = JSON.parseObject(jsons,clzs.javaObjectType)
//        println(dt2)
//    }

}

data class DataClassSimple(var a: Int = 1, var b: Int = 4)