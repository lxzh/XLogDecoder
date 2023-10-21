@file:Suppress("SpellCheckingInspection")

package ledou


import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import ledou.api.LeDouApi
import ledou.bean.FactionSource
import ledou.client.*
import okhttp3.MediaType.Companion.toMediaType
import org.junit.jupiter.api.Test
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.text.SimpleDateFormat
import java.util.*


internal class LeDou {

    private val server = Retrofit.Builder()
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(jsonDefault.asConverterFactory("application/json".toMediaType()))
        .baseUrl("https://zone4.ledou.qq.com")
        .client(client)
        .build()
        .create(LeDouApi::class.java)

    private val uid: String = "601401"

    @Test
    fun getKuangZang(): Unit = runBlocking {
        var i = 0
        repeat(100) {
            delay(10)
            findKuangZang(it + 1) { i++ }
        }
        println("找到${i}个空闲矿蔵")
    }

    @Test
    fun test(): Unit = runBlocking {
        faction()
    }

    @Test
    fun daily(): Unit = runBlocking {
        dailyMap()
        servant()
        qualifying()
        faction()
    }

    private suspend fun findKuangZang(
        area_id: Int,
        onFind: () -> Unit = {}
    ) {
        val r = server.checkRequest {
            getKuangZang(uid = uid, area_id = area_id)
        }
        //println("result: $r")
        val jewelList = r.jsonObject["jewel_list"]?.jsonArray
        jewelList?.forEachIndexed { index, jewel ->
            val state = jewel.getInt("status")
            val level = jewel.getInt("level")
            val name = jewel.getString("fac_name")
            val isMe = jewel.getInt("is_me")
            if (state == 0) {
                println("区域: ${area_id}_${index}, 等级: $level, 占领者帮派: $name, isMe: $isMe, state: $state")
                onFind()
            }
            if (isMe == 1) {
                println("我占领的矿蔵 区域: ${area_id}_${index}, 等级: $level, 占领者帮派: $name")
            }
        }
    }

    private suspend fun dailyMap() {
        println("----开始历练----")
        //历练
        mapUp(22, 15, 10)

        println("----开始英雄历练----")
        //英雄试练
        mapUp(10019, 6, 3) //反击魂珠
        mapUp(10019, 9, 3) //暴击魂珠
        mapUp(10018, 6, 3) //吸血魂珠
        mapUp(10017, 9, 1) //暴击魂珠
    }

    private suspend fun mapUp(
        map: Int,
        level: Int,
        times: Int,
    ) {
        delay(50)
        //历练
        val r = server.checkRequest { startExperience(uid, map, level, times) }
        val gifts = r.jsonObject["gifts"]?.jsonArray
        if (gifts == null) {
            println("扫荡失败: ${r.getString("msg")}")
            return
        }
        gifts.forEachIndexed { index, gift ->
            val award = gift.jsonObject["award"] ?: return@forEachIndexed
            val attrs = award.jsonObject["attrs"]?.jsonArray ?: return@forEachIndexed
            val items = award.jsonObject["items"]?.jsonArray ?: return@forEachIndexed
            val s = buildString {
                attrs.forEach {
                    append("${it.getString("name")}x${it.getString("num")} ")
                }
                items.forEach {
                    append("${it.getString("name")}x${it.getString("num")} ")
                }
            }
            println("扫荡第${index + 1}次 => $s")
        }
    }

    private val simpleDateFormat = SimpleDateFormat("MM/dd HH:mm:ss", Locale.getDefault())

    private fun Long.secondToFormat(): String {
        val h = this / 3600L
        val m = this % 3600L / 60L
        val s = this % 3600L % 60
        return "${h.toDateString()}:${m.toDateString()}:${s.toDateString()}"
    }

    private fun Long.toDateString() = if (this >= 10) "$this" else "0$this"

    //光明顶
    private suspend fun faction() {
        println("----光明顶----")
        server.checkRequest {
            factionQuery(uid)
        }.getJsonObject("user_faction")?.getJsonObject("brighttop")?.also { brighttop ->
            //println(brighttop)
            val totalUseTimes = brighttop.getInt("total_use_times")
            val moveUseTime = brighttop.getLong("move_use_time")
            println("总行动次数: $totalUseTimes, 当前列表总时间: ${moveUseTime.secondToFormat()}")
            var notFinishedTime = 0
            var finishedTimes = 0
            brighttop.getJsonArray("moves")?.also { moves ->
                println("当前行动列表: ")
                moves.forEachIndexed { index, move ->
                    val startTime = move.getLong("begin_time")
                    val start = simpleDateFormat.format(Date(startTime * 1000))
                    val needTime = move.getLong("need_time")
                    val remainTime = ((startTime + needTime) - (System.currentTimeMillis() / 1000)).coerceAtLeast(0)
                    println("[${index + 1}] 开始时间: $start, 需要时间: ${needTime.secondToFormat()}, 剩余时间: ${remainTime.secondToFormat()}")
                    if (remainTime <= 0) {
                        finishedTimes++
                    } else {
                        notFinishedTime++
                    }
                }
                println("当前行动 已完成: ${finishedTimes}, 未完成: $notFinishedTime")
                var r1: JsonObject? = null
                repeat(finishedTimes) {
                    server.checkRequest { factionFinish(uid, 0) }.also {
                        if (it.containsKey("msg")) {
                            println("完成行动, 信息: ${it.getString("msg")}")
                        }
                        r1 = it
                    }
                }
                r1?.getJsonObject("func_info")?.getJsonArray("building")?.also { building ->
                    buildString {
                        building.forEach {
                            appendLine("等级: ${it.getString("level")} 描述: ${it.getString("desc")}")
                        }
                    }.also(::println)
                }
            }
            brighttop.getJsonArray("source")?.also { source ->
                val sources = jsonDefault.decodeFromJsonElement<List<FactionSource>>(source)
                sources.filter {
                    it.status == 0
                }.sortedWith { o1, o2 ->
                    if (o1.type == o2.type) {
                        if (o1.quality == o2.quality) {
                            o1.distance.compareTo(o2.distance)
                        } else {
                            o2.quality.compareTo(o1.quality)
                        }
                    } else {
                        o1.type.compareTo(o2.type)
                    }
                }.run {
                    var times = (5 - notFinishedTime).coerceAtLeast(0)
                    forEach {
                        if (times <= 0) return@run
                        val r = server.checkRequest { factionAddMove(uid, it.id) }
                        val msg = r.getString("msg")
                        println("添加行动, id: ${it.id}, 类型: ${it.type}, 品质: ${it.quality}, 距离: ${it.distance}, 结果: $msg")
                        val ret = r.getInt("result")
                        if (ret == 0) {
                            times--
                        }
                        //println(r)
                    }
                }
            }
        }
        //r = server.checkRequest { factionAddMove(uid, "23") }
        //println(r)
    }

    //家丁
    private suspend fun servant() {
        var r: JsonObject
        println("----家丁----")
        repeat(3) {
            delay(50)
            r = server.checkRequest { servantReward(uid, it) }
            println("收取第${it + 1}个家丁家财: ${r.get("msg")}")
        }
    }

    //王者争霸
    private suspend fun qualifying() {
        println("----王者争霸----")
        var r: JsonObject
        r = server.checkRequest { qualifying(uid) }
        val freeTimes = r.getInt("free_times")
        println("免费争霸次数： $freeTimes")
        repeat(freeTimes) {
            delay(50)
            r = server.checkRequest { qualifyingFight(uid) }
            println("第${it + 1}次争霸 ${if (r.getInt("win") == 1) "胜利" else "失败"}, ${r.getString("msg")}")
        }
        println("当前段位 【${r.getString("sname")} ${r.getString("star")}/${r.getString("full_star")} 颗星】")
    }

    private suspend fun <T> LeDouApi.checkRequest(request: suspend LeDouApi.() -> T): T {
        var r = request()
        if (r is JsonObject && r.getInt("result") == 110) {
            println("登录超时: $r")
            val r1 = server.refresh(uid, wxcode)
            println("重试: $r1")
            h5token = r1.getString("token")
            r = request()
        }
        return r
    }

}

