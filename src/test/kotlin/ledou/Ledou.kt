@file:Suppress("SpellCheckingInspection")

package ledou


import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import ledou.api.LeDouApi
import ledou.bean.*
import ledou.client.*
import okhttp3.MediaType.Companion.toMediaType
import org.junit.jupiter.api.Test
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.measureTimeMillis


internal class LeDou {

    @OptIn(ExperimentalSerializationApi::class)
    private val server = Retrofit.Builder()
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(jsonDefault.asConverterFactory("application/json".toMediaType()))
        .baseUrl("https://zone4.ledou.qq.com")
        .client(client)
        .build()
        .create(LeDouApi::class.java)

    private val uid: String = "601401"

    @Test
    fun test(): Unit = runBlocking {

    }

    @Test
    fun daily(): Unit = runBlocking {
        //每日奖励
        dailyGif()
        //每日乐斗胜点
        dailyFight()
        //历练
        dailyMap()
        //经脉造访
        meridian()
        //家丁
        servant()
        //千层塔
        towel()
        //王者争霸
        qualifying()
        //光明顶
        faction()
        //游历
        marryHangup()
        //押镖
        dartCar()
    }

    @Test
    fun getKuangZang(): Unit = runBlocking {
        println("开始寻找空闲矿蔵")
        val list = ArrayList<String>()
        for (i in 1..100) {
            delay(10)
            findKuangZang(area_id = i) { area_id, index, jewel ->
                if (jewel.status == 0) {
                    val r1 = server.checkRequest { fightJewelWar(uid, area_id, index) }
                    println("尝试占领矿蔵 [${area_id}_${index}], 等级: ${jewel.level}, ${r1.getString("msg")}")
                    if (r1.getInt("is_win") != 1) {
                        list.add("区域[${area_id}_${index}], 等级: ${jewel.level}, state: 0")
                    }
                }
                if (jewel.is_me == 1) {
                    println("我占领的矿蔵, 区域[${area_id}_${index}], ${jewel.desc}")
                }
            }
        }
        println("找到${list.size}个空闲矿蔵")
        list.forEach(::println)
    }

    @Test
    fun viewRoomTest(): Unit = runBlocking {
        viewRoom()
    }

    @Test
    fun dailyGif(): Unit = runBlocking {
        println("----每日奖励----")
        getGiftImpl("企鹅闹钟1", 7, 0)
        getGiftImpl("企鹅闹钟2", 7, 1)
        getGiftImpl("企鹅闹钟3", 7, 2)
        getGiftImpl("每日便当", 19)
        //菜菜厨房
        kitchen()
        //抽签
        draw()
        //农场
        farm()
    }

    private suspend fun findKuangZang(
        area_id: Int,
        onFind: suspend (Int, Int, Jewel) -> Unit = { _, _, _ -> }
    ) {
        val r = server.checkRequest {
            getKuangZang(uid = uid, area_id = area_id)
        }
        //println("result: $r")
        val jewelList = r.jsonObject["jewel_list"]?.jsonArray
        jewelList?.forEachIndexed { index, je ->
            val jewel: Jewel = jsonDefault.decodeFromJsonElement(je)
            onFind(area_id, index, jewel)
        }
    }

    private suspend fun dailyMap() {
        //历练
        println("----开始历练----")
        val r = server.checkRequest {
            common(uid, mapOf("cmd" to "mappush", "subcmd" to "GetUser", "dup" to "0"))
        }
        val times = r.getInt("energy") //high_energy
        val highEnergy = r.getInt("high_energy") //high_energy
        if (times <= 0) {
            println("江湖令数量不足")
        } else {
            val userinfo = r.getJsonObject("userinfo")
            val info = userinfo?.getJsonObject("info")
            val map = userinfo?.getInt("curdup", 22) ?: 22
            val level = info?.getInt("curlevel", 15) ?: 15
            println("当前地图: $map, 关卡: $level, 江湖令次数: $times")
            mapUp(map, level, times)
        }
        println("----开始英雄历练----")
        if (highEnergy <= 0) {
            println("高级江湖令数量不足")
        } else {
            //英雄试练
            mapUp(10019, 6, 3) //反击魂珠
            mapUp(10019, 9, 3) //暴击魂珠
            mapUp(10018, 6, 3) //吸血魂珠
            mapUp(10017, 9, 1) //暴击魂珠
        }
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
            println("扫荡失败: ${r.msg}")
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
                        val msg = r.msg
                        println("添加行动, id: ${it.id}, 类型: ${it.type}, 品质: ${it.quality}, 距离: ${it.distance}, 结果: $msg")
                        if (r.isSuccess) {
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

    //练功房
    private suspend fun viewRoom() {
        val list = ArrayList<RoomEntity>().apply {
            val spend = measureTimeMillis {
                //getRoomList(RoomType.Type39_1).also(::addAll)
                //getRoomList(RoomType.Type39_2).also(::addAll)
                getRoomList(RoomType.Type59_1).also(::addAll)
                getRoomList(RoomType.Type59_2).also(::addAll)
                getRoomList(RoomType.Type60_1).also(::addAll)
                getRoomList(RoomType.Type60_2).also(::addAll)
            }
            println("查找了${size}个床位, 耗时${spend / 1000}秒")
        }
        list.apply {
            val enemyList: List<Long> = listOf(
                29247250,
                59180128,
            )
            val friendlyList: MutableSet<Long> = mutableSetOf(
                601401,
                35814365,
                12267343
            )
            filter {
                it.isEmpty
            }.also {
                println("找到${it.size}个空床位")
                //it.forEach { room ->
                //    println("${room.levelDesc} ${room.typeDesc} 第${room.pager}页")
                //}
            }

            filter {
                it.room.uid in friendlyList
            }.also {
                println("找到${it.size}个友军床位:")
                it.forEach { room ->
                    println(room)
                    friendlyList.remove(room.room.uid)
                }
                println("${friendlyList.size}个友军床位掉了: \n${friendlyList}")
            }
            filter {
                it.room.uid in enemyList
            }.also {
                println("找到${it.size}个敌军床位:")
                it.forEach(::println)
            }
            filter {
                it.room.fac_name == "天府" || it.room.fac_name == "萌萌大乐斗"
            }.also {
                println("找到${it.size}个敌帮床位:")
                it.forEach(::println)
            }
        }
    }

    private suspend fun getRoomList(roomType: RoomType): ArrayList<RoomEntity> {
        val list = ArrayList<RoomEntity>()
        println("正在查找床位 ${roomType.desc}, 总页数: ${roomType.totalPager}")
        for (pager in 1..roomType.totalPager) {
            delay(10)
            server.checkRequest {
                viewRoom(uid, pager, roomType.level, roomType.type)
            }.getJsonArray("room_array")?.also {
                jsonDefault.decodeFromJsonElement<List<RoomEntity.Room>>(it).forEach { room ->
                    list.add(RoomEntity(pager = pager + 1, roomType = roomType, room = room))
                }
            }
        }
        return list
    }

    //家丁
    private suspend fun servant() {
        var r: JsonObject
        println("----家丁----")
        repeat(3) {
            delay(50)
            r = server.checkRequest { servantReward(uid, it) }
            println("收取第${it + 1}个家丁家财: ${r.msg.decoded}")
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
            println("第${it + 1}次争霸 ${if (r.getInt("win") == 1) "胜利" else "失败"}, ${r.msg}")
        }
        println("当前段位 【${r.getString("sname")} ${r.getString("star")}/${r.getString("full_star")} 颗星】")
    }

    private suspend fun getGiftImpl(
        desc: String? = null,
        params: Map<String, String>,
    ): JsonObject {
        val r = server.checkRequest {
            getGift(uid = uid, params = params)
        }
        desc?.apply {
            println("[$desc] ${r.msg}")
        }
        return r
    }

    private suspend fun getGiftImpl(
        desc: String? = null,
        aid: Int,
        idx: Int = 0,
    ) = getGiftImpl(
        desc, mapOf(
            "aid" to "$aid",
            "idx" to "$idx",
            "subcmd" to "GetGift"
        )
    )

    private suspend fun kitchen() {
        for (i in 0 until 10) {
            val r = getGiftImpl(
                null, mapOf(
                    "aid" to "5",
                    "subcmd" to "Add",
                    "cmd" to "activity",
                    "is_double" to "1",
                )
            )
            val meiwei = r.getInt("meiwei")
            if (meiwei <= 0) {
                break
            }
            println("[菜菜厨房加菜] 美味度: $meiwei")
            if (meiwei >= 100) {
                break
            }
            delay(10)
        }
        getGiftImpl(
            "菜菜厨房", mapOf(
                "aid" to "5",
                "subcmd" to "Make",
            )
        )
    }

    //千层塔
    private suspend fun towel() {
        println("----千层塔----")
        while (true) {
            delay(10)
            val info = getTowelInfo() ?: break
            val baseInfo = info.baseInfo
            println("当前在 ${baseInfo.layer}层 第${baseInfo.barrier}关, 是否存活: ${baseInfo.alive}, 免费复活次数: ${baseInfo.revive}")
            if (baseInfo.alive > 0) {
                info.monsterInfo.run {
                    forEach { monsterInfo ->
                        if (!fightTowel(monsterInfo)) {
                            return@run
                        }
                    }
                }
            } else if (baseInfo.revive > 0) {
                buyLife()
            } else {
                println("没有挑战次数了")
                break
            }
        }
    }

    private suspend fun fightTowel(monsterInfo: TowelInfo.MonsterInfo): Boolean {
        val r = server.checkRequest {
            common(
                uid, mapOf(
                    "cmd" to "tower",
                    "op" to "battle",
                    "needreload" to "1",
                    "index" to "${monsterInfo.index}",
                )
            )
        }
        println(
            "挑战[id: ${monsterInfo.id}, 序号: ${monsterInfo.index}, 等级: ${monsterInfo.level}] 结果: ${
                r.getString(
                    "msg"
                )
            }"
        )
        return r.isSuccess && r.getInt("win", -1) == 1
    }

    private suspend fun getTowelInfo(): TowelInfo? {
        val r = server.checkRequest {
            common(
                uid, mapOf(
                    "cmd" to "tower",
                    "op" to "mainpage",
                    "needreload" to "1",
                )
            )
        }
        return if (r.isSuccess) {
            jsonDefault.decodeFromJsonElement<TowelInfo>(r)
        } else {
            println("获取千层塔信息失败, $r")
            null
        }
    }

    private suspend fun buyLife() {
        val r = server.checkRequest {
            common(
                uid, mapOf(
                    "cmd" to "tower",
                    "op" to "buylife",
                    "type" to "free",
                )
            )
        }
        if (r.isSuccess) {
            println("复活成功")
        } else {
            println("复活失败, $r")
        }
    }

    //游历
    private suspend fun marryHangup() {
        println("----游历----")
        var r: JsonObject
        while (true) {
            delay(10)
            r = server.checkRequest {
                common(uid, mapOf("cmd" to "marry_hangup", "op" to "ronglian", "grid_id" to "0"))
            }
            println("一键熔炼: ${r.msg}")
            if (r.isSuccess) {
                val bagSize = r.getJsonArray("bag")?.size ?: 0
                println("背包里面还有${bagSize}件装备")
                if (bagSize <= 0) {
                    break
                }
            } else {
                break
            }
        }

        r = server.checkRequest {
            common(uid, mapOf("cmd" to "marry_hangup", "op" to "query"))
        }
        if (r.isSuccess) {
            val encourage = r.getInt("encourage")
            if (encourage <= 0) {

            }

            val rlcoin = r.getString("rlcoin")
            val stone = r.getString("stone")
            val point = (r.getString("point").toLongOrNull() ?: 0L) / 10000
            val amethyst = r.getInt("amethyst")

            println("[仙缘: ${point}W] [熔炼币: $rlcoin] [强化石: $stone] [紫水晶: $amethyst]")
            val fight = r.getInt("fight")
            val maxfight = r.getInt("maxfight")
            var remain = maxfight - fight
            println("剩余闯关次数: $remain")
            while (remain > 0) {
                println("开始闯关: ${r.getString("stagename")}")
                r = server.checkRequest {
                    common(uid, mapOf("cmd" to "marry_hangup", "op" to "fight"))
                }
                println(r.msg)
                if (r.getInt("win") != 1) {
                    remain--
                    println("剩余闯关次数: $remain")
                }
            }
        }
    }

    private suspend fun ddp() {
        ddp(
            "begin",
            "${System.currentTimeMillis() / 1000}",
            ""
        )
        ddp(
            "finish",
            "982689234",
            "${System.currentTimeMillis() / 1000}",
        )
        ddp(
            "getaward",
            "${System.currentTimeMillis() / 1000}",
            "",
        )
    }

    private suspend fun ddp(
        subcmd: String,
        key0: String,
        key1: String
    ) {
        val r = server.checkRequest {
            common(
                uid, mapOf(
                    "cmd" to "activity",
                    "aid" to "129",
                    "subcmd" to subcmd,
                    "key0" to key0,
                    "key1" to key1,
                )
            )
        }
        println(r.msg)
    }

    private suspend fun draw() {
        //aid 9
        //"status": 1,
        //"seconds": 28638,
        //"choose_idx": 1,
        //"multiple": 4,

        println("----乐斗上上签----")
        val r = server.checkRequest {
            common(uid, mapOf("cmd" to "activity", "aid" to "9"))
        }
        if (!r.isSuccess) {
            println("result: ${r.getInt("result")}, msg: ${r.msg}")
            return
        }
        val status = r.getInt("status")
        val seconds = r.getLong("seconds")
        val multiple = r.getInt("multiple")
        println("status: $status, multiple: $multiple, 距离领取时间: ${seconds.secondToFormat()}")
        if (seconds <= 0) {
            println("领取")
        }

        //抽签
        //双倍领取
        //选择
    }

    private suspend fun farm() {
        // "status":1,"fertilize":1,  不能施肥  不能领取
        //双倍领取
        //施肥
        println("----乐斗农场----")
        val r = server.checkRequest {
            common(uid, mapOf("cmd" to "activity", "aid" to "10"))
        }
        if (!r.isSuccess) {
            println("result: ${r.getInt("result")}, msg: ${r.msg}")
            return
        }
        val status = r.getInt("status")
        val seconds = r.getLong("seconds")
        val fertilize = r.getInt("fertilize")
        println("status: $status, 已经施肥: $fertilize, 距离领取时间: ${seconds.secondToFormat()}")
        if (seconds <= 0) {
            println("领取")
        }
        if (fertilize != 1) {
            println("施肥")
        }
    }

    private suspend fun dartCar() {
        println("----押镖----")
        var r = server.checkRequest {
            common(uid, mapOf("cmd" to "faction", "op" to "escort"))
        }
        val robTimes = r.getJsonObject("user_info")?.getInt("rob_times") ?: 0
        val scortTimes = r.getJsonObject("user_info")?.getInt("scort_times") ?: 0
        println("已押镖次数: $scortTimes, 剩余次数: $robTimes")
        repeat(robTimes) {
            r = server.checkRequest {
                common(
                    uid, mapOf(
                        "cmd" to "faction",
                        "op" to "escort",
                        "subcmd" to "add",
                        "type" to "0",
                    )
                )
            }
            if (!r.isSuccess) {
                println("押镖失败: ${r.msg}")
                return
            }
            repeat(2) {
                r = server.checkRequest {
                    common(
                        uid, mapOf(
                            "cmd" to "faction",
                            "op" to "escort",
                            "subcmd" to "update",
                        )
                    )
                }
                println("升级品质: ${r.msg}")
            }
            r = server.checkRequest {
                common(
                    uid, mapOf(
                        "cmd" to "faction",
                        "op" to "escort",
                        "subcmd" to "begin",
                    )
                )
            }
            println("开始押镖: ${r.msg}")
        }
    }

    //每日拜访
    private suspend fun meridian() {
        println("----经脉造访----")
        var r = server.checkRequest {
            common(uid, mapOf("cmd" to "meridian", "op" to "visitpage"))
        }
        useSnowLotus(r)
        while (true) {
            delay(10)
            r = server.checkRequest {
                common(uid, mapOf("cmd" to "meridian", "op" to "visit", "id" to "0"))
            }
            println("一键造访: ${r.msg}")
            if (!r.isSuccess) {
                break
            }
            useSnowLotus(r)
        }

    }

    private suspend fun useSnowLotus(result: JsonObject) {
        result.getJsonArray("awards")?.apply {
            val awards = jsonDefault.decodeFromJsonElement<List<MeridianAwards>>(this)
            awards.filter {
                it.id == 100001L
            }.also {
                it.map { award ->
                    "${award.id}"
                }.also(::println)
            }.forEach { award ->
                val r = server.checkRequest {
                    common(uid, mapOf("cmd" to "meridian", "op" to "award", "index" to "${award.index}"))
                }
                println("自动服用雪莲: ${r.msg}")
            }
        }
    }

    private suspend fun dailyFight() {
        println("----每日乐斗----")
        var r = server.checkRequest {
            common(uid, mapOf("cmd" to "detail", "op" to "", "needreload" to "1"))
        }
        if (!r.isSuccess) {
            println("获取个人信息失败: ${r.msg}")
            return
        }
        val myPower = r.getLong("attack_power")
        println("我的战力 [$myPower]")
        r = server.checkRequest {
            common(
                uid, mapOf(
                    "cmd" to "sns",
                    "op" to "query",
                    "needreload" to "1",
                    "type" to "0",
                )
            )
        }
        if (!r.isSuccess) {
            println("获取好友信息失败: ${r.msg}")
            return
        }
        jsonDefault.decodeFromJsonElement<FriendInfo>(r).apply {
            var r1 = server.checkRequest {
                common(uid, mapOf("cmd" to "sns", "op" to "getvit", "target_uid" to "0"))
            }
            println("已获得好友体力: [$getvit/$maxvit] 胜点: [$getwinpoints/$maxwinpoints] 黄金钥匙: [$getkey/$maxkey]")
            println("一键接受体力: ${r1.msg}")
            r1 = server.checkRequest {
                common(uid, mapOf("cmd" to "sns", "op" to "sendvit", "target_uid" to "0"))
            }
            println("一键赠送体力: ${r1.msg}")
            if (getwinpoints >= maxwinpoints && getkey >= maxkey) {
                return
            }
            var win = 0
            var time = 0
            friendlist.filter {
                it.can_fight == 1 && it.power < (myPower + 5000)
            }.sortedByDescending {
                it.level
            }.run {
                forEach { friend ->
                    if (win >= 6 && time >= 10) {
                        return@run
                    }
                    r1 = server.checkRequest {
                        common(
                            uid, mapOf(
                                "cmd" to "sns",
                                "op" to "fight",
                                "target_uid" to friend.uid,
                                "type" to "0",
                            )
                        )
                    }
                    val msg = r.msg.decoded
                    println("挑战好友 [${friend.realName}, 战力: ${friend.power}]: $msg")
                    if (msg.contains("战胜")) {
                        win++
                    }
                    time++
                }
            }
            r1 = server.checkRequest {
                common(uid, mapOf("cmd" to "sns", "op" to "getvit", "target_uid" to "0"))
            }
            println("一键接受体力: ${r1.msg}")
        }
    }

    private suspend fun <T> LeDouApi.checkRequest(request: suspend LeDouApi.() -> T): T {
        var r = request()
        if (r is JsonObject && r.getInt("result") == 110) {
            println("登录超时: $r")
            //val r1 = server.refresh(uid, wxcode)
            //println("重试: $r1")
            //h5token = r1.getString("token")
            //r = request()
        }
        return r
    }

    private val JsonObject.isSuccess get() = getInt("result", -1) == 0

    private val JsonObject.msg get() = getString("msg")

}

