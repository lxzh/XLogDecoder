@file:Suppress("SpellCheckingInspection")

package ledou

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import ledou.api.LeDouApi
import ledou.bean.*
import ledou.client.*
import okhttp3.MediaType.Companion.toMediaType
import org.junit.jupiter.api.Test
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDateTime
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
//        val r = server.checkRequest {
//            common(
//                "cmd" to "refresh",
//                "sub" to "token",
//                "wxcode" to "083iCa000V85cP1AXf300voCl20iCa0o",
//            )
//        }
//        val token = r.getString("token")
//        log(token)
//        log(r)
        foodParty()
    }

    @Test
    fun daily(): Unit = runBlocking {
        //每日活动奖励
        dailyGif()
        //每日乐斗获取胜点
        dailyFight()
        //黄金轮盘
        turntable()
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
        //组队争霸
        qualifyingTeam()
        //斗神排名
        doushen()
        //光明顶
        faction()
        //帮派（捐献、武馆、洞穴）
        guild()
        //游历
        marryHangup()
        //押镖
        dartCar()
        //每日任务奖励
        taskReward()
    }

    //扫描矿蔵
    @Test
    fun getKuangZang(): Unit = runBlocking {
        getKuangZangImpl()
    }

    //扫描练功房
    @Test
    fun viewRoom(): Unit = runBlocking {
        viewRoomImpl()
    }

    //密卷免看广告直接加祝福值
    @Test
    fun denseVolume(): Unit = runBlocking {
        // 1 战吼
        // 3 击破
        // 5 烈焰
        // 7 恶疾
        // 9 符咒
        // 11 暗影
        //看广告加祝福
        reinforcement(7, 1)
    }

    @Test
    fun dailyGif(): Unit = runBlocking {
        log("----每日奖励----")
        //企鹅闹钟
        alarmClock()
        //每日便当
        bento()
        //抽签
        draw()
        //农场
        farm()

        log("----轮换活动----")
        //菜菜厨房
        kitchen()
        //猜一猜
        guess()
        //乐斗点球
        football()
        //牧场
        pasture()
    }

    private suspend fun alarmClock(): Any = when (LocalDateTime.now().hour) {
        in 11 until 13 -> {
            getGiftImpl("企鹅闹钟1", 7, 0)
        }

        in 16 until 18 -> {
            getGiftImpl("企鹅闹钟1", 7, 1)
        }

        in 19 until 21 -> {
            getGiftImpl("企鹅闹钟1", 7, 2)
        }

        else -> {
            log("企鹅闹钟时间未到")
        }
    }

    private suspend fun bento(): Any = when (LocalDateTime.now().hour) {
        in 11 until 14, in 16 until 21 -> {
            getGiftImpl("每日便当", 19, 0)
        }

        else -> {
            log("每日便当时间未到")
        }
    }

    private suspend fun taskReward() {
        log("----每日任务奖励----")
        var r = common("task", "needreload" to "1", "subcmd" to "GetUser")
        if (!r.isSuccess) {
            log("获取任务信息失败, ${r.msg}")
            return
        }
        val gifts: List<ActiveGift> = r.getJsonObject("userinfo")?.getJsonArray("activegift")?.let {
            jsonDefault.decodeFromJsonElement(it)
        } ?: return

        val unget = gifts.count { it.status == 0 }
        val getable = gifts.filter { it.status == 1 }
        val geted = gifts.count { it.status == 2 }
        log("可领取奖励数量: ${getable.size}, 已领取奖励数量: ${geted}, 未获取的奖励数量: $unget")
        getable.forEach { g ->
            delay(10)
            r = common("task", "id" to "${g.id}", "subcmd" to "GetPrize")
            log("${g.desc}奖励: ${r.msg}")
        }

    }

    private suspend fun getKuangZangImpl() {
        log("开始寻找空闲矿蔵")
        val list = ArrayList<String>()
        for (i in 1..100) {
            delay(10)
            findKuangZang(area_id = i) { area_id, index, jewel ->
                if (jewel.status == 0) {
                    val r1 = server.checkRequest { fightJewelWar(uid, area_id, index) }
                    log("尝试占领矿蔵 [${area_id}_${index}], 等级: ${jewel.level}, ${r1.msg}")
                    if (r1.getInt("is_win") != 1) {
                        list.add("区域[${area_id}_${index}], 等级: ${jewel.level}, state: 0")
                    }
                }
                if (jewel.is_me == 1) {
                    log("我占领的矿蔵, 区域[${area_id}_${index}], ${jewel.desc}")
                }
            }
        }
        log("找到${list.size}个空闲矿蔵")
        list.forEach(::log)
    }

    private suspend fun findKuangZang(
        area_id: Int,
        onFind: suspend (Int, Int, Jewel) -> Unit = { _, _, _ -> }
    ) {
        server.checkRequest {
            getKuangZang(uid = uid, area_id = area_id)
        }.getJsonArray("jewel_list")?.forEachIndexed { index, je ->
            val jewel: Jewel = jsonDefault.decodeFromJsonElement(je)
            onFind(area_id, index, jewel)
        }
    }

    private suspend fun dailyMap() {
        //历练
        log("----开始历练----")
        val r = common("mappush", "subcmd" to "GetUser", "dup" to "0")
        val times = r.getInt("energy") //high_energy
        val highEnergy = r.getInt("high_energy") //high_energy
        if (times <= 0) {
            log("江湖令数量不足")
        } else {
            val userinfo = r.getJsonObject("userinfo")
            val info = userinfo?.getJsonObject("info")
            val map = userinfo?.getInt("curdup", 22) ?: 22
            val level = info?.getInt("curlevel", 15) ?: 15
            log("当前地图: $map, 关卡: $level, 江湖令次数: $times")
            mapUp(map, level, times)
        }
        log("----开始英雄历练----")
        if (highEnergy <= 0) {
            log("高级江湖令数量不足")
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
        //历练
        val r = server.checkRequest { startExperience(uid, map, level, times) }
        val gifts = r.getJsonArray("gifts")
        if (gifts == null) {
            log("扫荡失败: ${r.msg}")
            return
        }
        gifts.forEachIndexed { index, gift ->
            val award = gift.getJsonObject("award") ?: return@forEachIndexed
            val attrs = award.getJsonArray("attrs") ?: return@forEachIndexed
            val items = award.getJsonArray("items") ?: return@forEachIndexed
            val s = buildString {
                attrs.forEach {
                    append("${it.getString("name")}x${it.getString("num")} ")
                }
                items.forEach {
                    append("${it.getString("name")}x${it.getString("num")} ")
                }
            }
            log("扫荡第${index + 1}次 => $s")
        }
    }

    //光明顶
    private suspend fun faction() {
        log("----光明顶----")
        var result = server.checkRequest {
            factionQuery(uid, op = "brighttop")
        }
        var userInfo = result.getJsonObject("user_info") ?: run {
            log("获取用户信息失败: ${result.msg}")
            return
        }

        run {
            val moves = userInfo.getJsonArray("move_info") ?: return@run
            var notFinishedTime = 0
            var finishedTimes = 0
            log("当前行动列表: ")
            moves.forEachIndexed { index, move ->
                val startTime = move.getLong("begin_time")
                val start = simpleDateFormat.format(Date(startTime * 1000))
                val needTime = move.getLong("need_time")
                val remainTime = ((startTime + needTime) - (System.currentTimeMillis() / 1000)).coerceAtLeast(0)
                log("[${index + 1}] 开始时间: $start, 需要时间: ${needTime.secondToFormat()}, 剩余时间: ${remainTime.secondToFormat()}")
                if (remainTime <= 0) {
                    finishedTimes++
                } else {
                    notFinishedTime++
                }
            }
            log("当前行动 已完成: ${finishedTimes}, 未完成: $notFinishedTime")
            repeat(finishedTimes) {
                result = server.checkRequest { factionFinish(uid, 0) }
                log("完成行动: ${result.msg}")
            }
        }

        run {
            userInfo = result.getJsonObject("user_info") ?: run {
                log("获取用户信息失败: ${result.msg}")
                return
            }
            result.getJsonObject("func_info")?.apply {
                val totalUseTimes = userInfo.getInt("total_use_times")
                val moveUseTime = userInfo.getLong("move_use_time").secondToFormat()
                val maxMoveTime = this.getLong("max_move_time").secondToFormat()
                log("总行动次数: $totalUseTimes, 当前列表总时间: ${moveUseTime}, 最大行动时间: $maxMoveTime")
            }?.getJsonArray("building")?.joinToString("\n") {
                "等级: ${it.getString("level")} 描述: ${it.getString("desc")}"
            }?.also(::log)
        }

        run {
            val source = userInfo.getJsonArray("source_info") ?: return@run
            val notFinishedTime = userInfo.getJsonArray("move_info")?.size ?: 0
            val sources = jsonDefault.decodeFromJsonElement<List<FactionSource>>(source)
            val list = sources.filter {
                it.status == 0 || (it.status == 1 && it.left_hp > 0)
            }.sortedWith { o1, o2 ->
                if (o1.sort == o2.sort) {
                    if (o1.quality == o2.quality) {
                        o1.distance.compareTo(o2.distance)
                    } else {
                        o2.quality.compareTo(o1.quality)
                    }
                } else {
                    o1.sort.compareTo(o2.sort)
                }
            }
            var times = (5 - notFinishedTime).coerceAtLeast(0)
            //log(list.joinToString("\n") { it.showInfo })
            list.forEach {
                if (times <= 0) return@run
                val r1 = server.checkRequest { factionAddMove(uid, it.id) }
                val msg = r1.msg
                if (r1.isSuccess) {
                    log("添加行动${it.showInfo}成功")
                    times--
                } else {
                    log("添加行动${it.showInfo}失败, (${r1.result}): $msg")
                }
                //log(r)
            }
        }
    }

    //帮派
    private suspend fun guild() {
        //捐赠
        run {
            log("----帮派捐献----")
            val query = server.checkRequest { factionQuery(uid) }
            if (!query.isSuccess) {
                log("查询帮派信息失败: ${query.msg}")
                return@run
            }
            val donationFlags = query.getJsonObject("user_faction")?.getInt("donation_flags") ?: 0
            if (donationFlags == 0) {
                val donation = common("faction", "op" to "donation", "type" to "0", "contrib" to "3000")
                log("每日捐献: ${donation.msg}")
            } else {
                log("今日已经捐献, flag: $donationFlags")
            }
        }

        //武馆
        run {
            println("----武馆----")
            val getclub = common("faction", "op" to "getclub")
            if (!getclub.isSuccess) {
                log("查询武馆信息失败: ${getclub.msg}")
                return@run
            }
            val fightCount = getclub.getInt("fight_count")
            val fightMaxcount = getclub.getInt("fight_maxcount")
            val remain = fightMaxcount - fightCount
            if (remain <= 0) {
                log("没有挑战次数了")
                return@run
            }
            repeat(remain) { i ->
                val clubfight = common("faction", "op" to "clubfight", "club_type" to "$i")
                log("武馆挑战[$i]: ${clubfight.msg}")
            }
        }

        //洞穴
        run {
            println("----神秘洞穴----")
            val caveQuery = common("faction", "op" to "cave_query")
            if (!caveQuery.isSuccess) {
                log("查询洞穴信息失败: ${caveQuery.msg}")
                return@run
            }
            val cave = caveQuery.getJsonObject("cave") ?: return@run
            val monster = cave.getJsonArray("monster") ?: JsonArray(emptyList())
            val boss = cave.getJsonObject("boss")?.getJsonArray("monster") ?: JsonArray(emptyList())
            val all = monster.plus(boss)

            val seq = cave.getLong("seq")
            val times = cave.getJsonObject("fighter")?.getInt("times") ?: 0
            var remain = 3 - times
            if (remain <= 0) {
                log("没有挑战次数了")
                return@run
            }
            all.forEach {
                val hp = it.getLong("hp")
                if (hp <= 0) {
                    return@forEach
                }
                while (true) {
                    if (remain <= 0) {
                        log("没有挑战次数了")
                        return@run
                    }
                    val id = it.getString("id")
                    val maxhp = it.getLong("maxhp")
                    val name = it.getString("name")
                    val caveFight = common("faction", "op" to "cave_fight", "seq" to "$seq", "monster" to id)
                    if (!caveFight.isSuccess) {
                        log("挑战[$name], 剩余血量:[$hp/$maxhp]失败: ${caveFight.msg}")
                        break
                    }
                    remain--
                    val killed = caveFight.getInt("killed") == 1
                    val deductHp = caveFight.getLong("deduct_hp")
                    val remainHp = caveFight.getLong("remain_hp")
                    log("挑战[$name], 造成伤害: $deductHp, 剩余血量: [$remainHp/$maxhp], 是否击败: $killed")
                    if (killed) break
                }
            }
        }

    }

    //练功房
    private suspend fun viewRoomImpl() {
        val list = ArrayList<RoomEntity>().apply {
            val spend = measureTimeMillis {
                getRoomList(RoomType.Type39_1).also(::addAll)
                getRoomList(RoomType.Type39_2).also(::addAll)
                getRoomList(RoomType.Type59_1).also(::addAll)
                getRoomList(RoomType.Type59_2).also(::addAll)
                getRoomList(RoomType.Type60_1).also(::addAll)
                getRoomList(RoomType.Type60_2).also(::addAll)
            }
            log("查找了${size}个床位, 耗时${spend / 1000}秒")
        }
        list.apply {
            val enemyList: MutableSet<Long> = mutableSetOf(
                29247250,
                59180128,
            )
            val friendlyList: MutableSet<Long> = mutableSetOf(
                601401,
                35814365,
                12267343
            )
            val friendFacList: MutableSet<Long> = mutableSetOf(
                171171,
                829821,
                482511,
            )
            val enemyFacList: MutableSet<Long> = mutableSetOf(
                629411,
                367901
            )
            filter {
                it.isEmpty
            }.also {
                log("找到${it.size}个空床位")
                //it.forEach { room ->
                //    log("${room.levelDesc} ${room.typeDesc} 第${room.pager}页")
                //}
            }

            filter {
                it.room.uid in friendlyList
            }.also {
                log("找到${it.size}个友军床位:")
                it.forEach { room ->
                    log(room)
                    friendlyList.remove(room.room.uid)
                }
                log("${friendlyList.size}个友军床位掉了: \n${friendlyList}")
            }
            filter {
                it.room.uid in enemyList
            }.also {
                log("找到${it.size}个敌军床位:")
                it.forEach(::log)
            }
            //filter {
            //    it.room.fac_id in friendFacList
            //}.also {
            //    log("找到${it.size}个友帮床位:")
            //    it.forEach(::log)
            //}
            filter {
                it.room.fac_id in enemyFacList
            }.also {
                log("找到${it.size}个敌帮床位:")
                it.forEach(::log)
            }
        }
    }

    private suspend fun getRoomList(roomType: RoomType): ArrayList<RoomEntity> {
        val list = ArrayList<RoomEntity>()
        log("正在查找床位 ${roomType.desc}, 总页数: ${roomType.totalPager}")
        for (pager in 1..roomType.totalPager) {
            delay(10)
            server.checkRequest {
                viewRoom(uid, pager, roomType.level, roomType.type)
            }.getJsonArray("room_array")?.let {
                jsonDefault.decodeFromJsonElement<List<RoomEntity.Room>>(it)
            }?.forEach { room ->
                list.add(RoomEntity(pager = pager + 1, roomType = roomType, room = room))
            }
        }
        return list
    }

    //家丁
    private suspend fun servant() {
        var r: JsonObject
        log("----家丁----")
        repeat(3) {
            delay(50)
            r = server.checkRequest { servantReward(uid, it) }
            log("收取第${it + 1}个家丁家财: ${r.msg.decoded}")
        }
    }

    //王者争霸
    private suspend fun qualifying() {
        log("----王者争霸----")
        var r: JsonObject
        run {
            r = server.checkRequest { qualifying(uid) }
            val freeTimes = r.getInt("free_times")
            log("免费争霸次数： $freeTimes")
            val now = LocalDateTime.now()
            val nowHour = now.hour
            if (now.dayOfWeek == DayOfWeek.SUNDAY && nowHour >= 12) {
                log("已经是周日${nowHour}点了，防止掉段，不自动进行王者争霸")
                return@run
            }
            repeat(freeTimes) {
                delay(50)
                r = server.checkRequest { qualifyingFight(uid) }
                log("第${it + 1}次争霸 ${if (r.getInt("win") == 1) "胜利" else "失败"}, ${r.msg}")
            }
        }

        //领取奖励
        run {
            r = server.checkRequest { qualifying(uid) }
            val winTimes = r.getInt("win_times")
            val dw = "【${r.getString("sname")} ${r.getString("star")}/${r.getString("full_star")} 颗星】"
            log("今日胜利次数: ${winTimes}, 当前段位: $dw")
            r.getJsonArray("win_award")?.forEachIndexed { index, award ->
                //"times": 1,"flag": 0,"adflag": 0,
                val times = award.getInt("times")
                val flag = award.getInt("flag")
                if (winTimes >= times && flag != 1) {
                    val r1 = common("qualifying", "op" to "reward", "idx" to "$index", "ad" to "1")
                    log("领取${times}胜奖励: ${r1.msg}")
                }
            }
        }

        //巅峰王者点赞
        run {
            r = common("qualifying", "op" to "rank", "type" to "0", "start" to "1", "end" to "10")
            //"selfrank": 0,
            //"totalrank": 100,
            //"reward_flag": 0,
            if (r.isSuccess && r.getInt("reward_flag") != 1) {
                r = common("qualifying", "op" to "reward", "idx" to "3")
                log("巅峰王者点赞: ${r.msg}")
            }
        }

        //领取好友首胜
        run {
            r = common("qualifying", "op" to "rank", "type" to "2")
            //"award_num": 0,
            //"max_award": 150,
            val awardNum = r.getInt("award_num")
            val maxAward = r.getInt("max_award")
            if (awardNum < maxAward) {
                val friuid = mutableListOf<String>()
                r.getJsonArray("friend_array")?.forEach { f ->
                    if (f.getInt("win") == 1 && f.getInt("flag") == 0) {
                        f.getString("uid").takeIf { it.isNotEmpty() }?.also { friuid.add(it) }
                    }
                }
                if (friuid.isNotEmpty()) {
                    val friuids = friuid.joinToString("|")
                    r = common("qualifying", "op" to "reward", "idx" to "100", "friuid" to friuids)
                    log("一键领取好友首胜奖励: ${r.msg}")
                }
            }
        }

    }

    private suspend fun qualifyingTeam() {
        log("----王者组队争霸----")
        var r: JsonObject
        r = common("teamqua")
        if (!r.isSuccess) {
            log("查询组队争霸信息失败: ${r.msg}")
            return
        }
        //组队争霸
        run {
            val teamJson = r.getJsonArray("team_member") ?: let {
                log("获取队伍信息失败")
                return@run
            }

            val teamMember: List<QualifyingTeamMember> = jsonDefault.decodeFromJsonElement(teamJson)
            if (teamMember.size < 3) {
                log("当前未组队")
                return@run
            }

            log("队伍ID: ${r.getLong("team_id")}, 名称: ${r.getString("team_name").decoded}, 人数: ${teamMember.size}")
            val freeTimes = r.getInt("free_times")
            log("免费争霸次数： $freeTimes")
            repeat(freeTimes) {
                delay(10)
                qualifyingTeamFight(teamMember)
            }
        }

        //领取胜利奖励
        run {
            r = common("teamqua")
            if (!r.isSuccess) {
                log("查询组队争霸信息失败: ${r.msg}")
                return@run
            }
            val winTimes = r.getInt("win_times")
            r.getJsonArray("win_award")?.forEachIndexed { index, award ->
                //"times": 1,"flag": 0,"adflag": 0,
                val times = award.getInt("times")
                val flag = award.getInt("flag")
                if (winTimes >= times && flag != 1) {
                    val r1 = common("teamqua", "op" to "reward", "idx" to "$index", "ad" to "1")
                    log("领取组队赛${times}胜奖励: ${r1.msg}")
                }
            }
        }

        //王者组队巅峰王者点赞
        run {
            r = common("teamqua", "op" to "rank", "type" to "0", "start" to "1", "end" to "10")
            if (r.isSuccess && r.getInt("reward_flag") != 1) {
                r = common("teamqua", "op" to "reward", "idx" to "3")
                log("王者组队巅峰王者点赞: ${r.msg}")
            }
        }

        //领取好友首胜
        run {
            r = common("teamqua", "op" to "rank", "type" to "2")
            //"award_num": 0,
            //"max_award": 150,
            val awardNum = r.getInt("award_num")
            val maxAward = r.getInt("max_award")
            if (awardNum < maxAward) {
                val friuid = mutableListOf<String>()
                r.getJsonArray("friend_array")?.forEach { f ->
                    if (f.getInt("win") == 1 && f.getInt("flag") == 0) {
                        f.getString("uid").takeIf { it.isNotEmpty() }?.also { friuid.add(it) }
                    }
                }
                if (friuid.isNotEmpty()) {
                    val friuids = friuid.joinToString("|")
                    r = common("teamqua", "op" to "reward", "idx" to "100", "friuid" to friuids)
                    log("一键领取好友组队赛首胜奖励: ${r.msg}")
                }
            }
        }

    }

    private suspend fun qualifyingTeamFight(teamMember: List<QualifyingTeamMember>) {
        common("teamqua", "op" to "match").getJsonArray("team_member")?.also {
            val teamMember1: List<QualifyingTeamMember> = jsonDefault.decodeFromJsonElement(it)
            //log("我方队伍： $teamMember")
            //log("对方队伍： $teamMember1")
            val t = teamMember.mapIndexed { i, m ->
                Pair(i, m)
            }.sortedByDescending { p -> p.second.power }

            val sorted = teamMember1.asSequence().mapIndexed { i, m ->
                Pair(i, m)
            }.sortedByDescending { p -> p.second.power }.mapIndexed { i, pair ->
                val fix = ((t.size - 1) + i) % t.size
                //对方队伍出战顺序  我方出战人员  对方出战人员
                Triple(pair.first, t[fix], pair.second)
            }.sortedBy { tri -> tri.first }.map { tri ->
                //我方出战人员  对方出战人员
                Pair(tri.second, tri.third)
            }.joinToString("|") { p -> "${p.first.first}" }
            val r = common("teamqua", "op" to "fight", "userlist" to sorted)
            log("组队争霸 ${if (r.getInt("win") == 1) "胜利" else "失败"} ${r.msg}")
        }
    }

    //斗神排名
    private suspend fun doushen() {
        log("----斗神排名赛----")
        var r = common("doushen")
        //"result": 0,"point": 6520,"history_record": 4836,"self_rank": 4836,"rank_award": 1330,
        //"win_times": 0,"free_times": 5,"max_free_times": 5,
        //"cash_times": 0,"max_cash_times": 10,"cash_cost": 10,
        if (r.isSuccess) {
            val historyRecord = r.getInt("history_record")
            val selfRank = r.getInt("self_rank")
            val rankAward = r.getInt("rank_award")
            log("当前排名: $selfRank, 排名奖励: $rankAward, 历史最佳排名: $historyRecord")
            r.getJsonArray("day_award")?.forEach {
                val idx = it.getInt("idx")
                val flag = it.getInt("flag")
                if (flag != 1) {
                    r = common("doushen", "op" to "reward", "idx" to "$idx", "ad" to "1")
                    log("领取每日奖励: ${r.msg}")
                }
            }
        } else {
            log("获取信息失败: ${r.msg}")
        }

        //斗神点赞
        //r = server.checkRequest {
        //    common( "doushen", "op" to "rank", "type" to "1", "start" to "1", "end" to "10")
        //}

        //好友首胜奖励
        run {
            r = common("doushen", "op" to "rank", "type" to "2")
            var awardNum = r.getInt("award_num")
            var maxAward = r.getInt("max_award")
            log("好友首胜奖励已领取: $awardNum/$maxAward")
            val need = (maxAward - awardNum) / 30
            if (need > 0) {
                val friuid = mutableListOf<String>()
                r.getJsonArray("friend_array")?.forEach { f ->
                    if (f.getInt("win") == 1 && f.getInt("flag") == 0) {
                        f.getString("uid").takeIf { it.isNotEmpty() }?.also { friuid.add(it) }
                    }
                }
                friuid.take(need).apply {
                    if (isEmpty()) {
                        log("没有好友首胜奖励可领取了")
                        return@run
                    }
                }.forEach {
                    r = common("doushen", "op" to "reward", "idx" to "100", "friuid" to it)
                    log("领取好友首胜奖励: ${r.msg}")
                }
                r = common("doushen", "op" to "rank", "type" to "2")
                awardNum = r.getInt("award_num")
                maxAward = r.getInt("max_award")
                log("好友首胜奖励已领取: $awardNum/$maxAward")
            }
        }
    }

    private suspend fun getGiftImpl(
        desc: String? = null,
        aid: Int,
        vararg params: Pair<String, Any>,
    ): JsonObject {
        val r = activity(aid, "is_double" to "1", *params)
        //getGift(uid = uid, params = params)
        desc?.apply {
            log("[$desc] ${r.msg}")
        }
        return r
    }

    private suspend fun getGiftImpl(
        desc: String? = null,
        aid: Int,
        idx: Int = 0,
    ) = getGiftImpl(desc, aid, "idx" to idx, "subcmd" to "GetGift")

    private suspend fun kitchen() {
        for (i in 0 until 10) {
            val r = getGiftImpl(null, 5, "subcmd" to "Add", "is_double" to "1")
            val meiwei = r.getInt("meiwei")
            if (meiwei <= 0) {
                //log("菜菜厨房: ${r.msg}")
                return
            }
            log("[菜菜厨房加菜] 美味度: $meiwei")
            if (meiwei >= 80) {
                break
            }
            delay(10)
        }
        getGiftImpl("菜菜厨房出餐", 5, "subcmd" to "Make")
    }

    //千层塔
    private suspend fun towel(
        autoRebirth: Boolean = false
    ) {
        log("----千层塔----")
        var info = getTowelInfo() ?: return
        if (info.giftInfo.giftStatus == 1) {
            val r = common("tower", "op" to "award")
            log("领取闯关奖励: ${r.msg}")
        }
        while (true) {
            delay(100)
            info = getTowelInfo() ?: break
            val baseInfo = info.baseInfo
            log("当前在 ${baseInfo.layer}层 第${baseInfo.barrier}关, 是否存活: ${baseInfo.alive}, 免费复活次数: ${baseInfo.revive}")
            if (baseInfo.alive > 0) {
                val win = info.monsterInfo.run {
                    forEach { monsterInfo ->
                        if (!fightTowel(monsterInfo)) {
                            return@run false
                        }
                    }
                    true
                }
                if (!win && !autoRebirth) {
                    log("战败, 终止挑战")
                    break
                }
            } else if (baseInfo.revive > 0) {
                if (autoRebirth) {
                    buyLife()
                } else {
                    log("当前处于待复活状态")
                    break
                }
            } else {
                log("没有复活次数了")
                break
            }
        }
    }

    private suspend fun fightTowel(monsterInfo: TowelInfo.MonsterInfo): Boolean {
        val r = common("tower", "op" to "battle", "needreload" to "1", "index" to "${monsterInfo.index}")
        log("挑战[id: ${monsterInfo.id}, 序号: ${monsterInfo.index}, 等级: ${monsterInfo.level}] 结果: ${r.msg}")
        return r.isSuccess && r.getInt("win", -1) == 1
    }

    private suspend fun getTowelInfo(): TowelInfo? {
        val r = common("tower", "op" to "mainpage", "needreload" to "1")
        return if (r.isSuccess) {
            jsonDefault.decodeFromJsonElement<TowelInfo>(r)
        } else {
            log("获取千层塔信息失败, $r")
            null
        }
    }

    private suspend fun buyLife() {
        val r = common("tower", "op" to "buylife", "type" to "free")
        if (r.isSuccess) {
            log("复活成功")
        } else {
            log("复活失败, $r")
        }
    }

    //游历
    private suspend fun marryHangup(
        savedTimes: Int = 3,
    ) {
        log("----游历----")
        var r: JsonObject
        common("marry_hangup", "op" to "query")

        //一键熔炼
        run {
            while (true) {
                delay(10)
                r = common("marry_hangup", "op" to "ronglian", "grid_id" to "0")
                log("一键熔炼: ${r.msg}")
                if (r.isSuccess) {
                    val bagSize = r.getJsonArray("bag")?.size ?: 0
                    log("背包里面还有${bagSize}件装备")
                    if (bagSize <= 0) {
                        break
                    }
                } else {
                    break
                }
            }
        }

        r = common("marry_hangup", "op" to "query")
        if (!r.isSuccess) {
            return
        }
        //log(r)
        //开宝箱
        run {
            r.getJsonArray("selfbox")?.forEach {
                if (it.getInt("locked", -1) == 0) {
                    common(
                        "marry_hangup",
                        "op" to "unlock",
                        "type" to "0",
                        "idx" to it.getInt("idx")
                    ).also { r1 ->
                        log("开自己宝箱: ${r1.msg}")
                    }
                }
            }
            r.getJsonArray("oppbox")?.forEach {
                if (it.getInt("locked", -1) == 1) {
                    common(
                        "marry_hangup",
                        "op" to "unlock",
                        "type" to "1",
                        "idx" to it.getInt("idx")
                    ).also { r1 ->
                        log("开对方宝箱: ${r1.msg}")
                    }
                }
            }
        }

        //互动
        run {
            val encourage = r.getInt("encourage")
            if (encourage <= 0) {
                //互动
                common("marry_hangup", "op" to "encourage").also { r1 ->
                    log("互动: ${r1.msg}")
                }
            }
        }

        val rlcoin = r.getString("rlcoin")
        val stone = r.getString("stone")
        val point = (r.getString("point").toLongOrNull() ?: 0L) / 10000
        val amethyst = r.getInt("amethyst")

        log("[仙缘: ${point}W] [熔炼币: $rlcoin] [强化石: $stone] [紫水晶: $amethyst]")

        //闯关
        run {
            val fight = r.getInt("fight")
            val maxfight = r.getInt("maxfight")
            var remain = maxfight - fight
            log("剩余闯关次数: ${remain}, 主动保留次数: $savedTimes")
            remain -= savedTimes
            var stagename = r.getString("stagename")
            while (remain > 0) {
                log("开始闯关: $stagename")
                r = common("marry_hangup", "op" to "fight")
                log(r.msg)
                if (r.getInt("win") != 1) {
                    remain--
                    log("剩余闯关次数: $remain")
                    continue
                }
                r = common("marry_hangup", "op" to "query")
                stagename = r.getString("stagename")
            }
        }
    }

    private suspend fun ddp() {
        //cmd=activity&aid=129&
        //subcmd=begin&key0=1672559697&key1=10058790159996
        //subcmd=finish&key0=1551614580&key1=1672559948
        //subcmd=getaward&key0=1672560031&key1=10058792565600

        //subcmd=begin&key0=1672560219&key1=10058793768402
        //subcmd=finish&key0=982689234&key1=1672582340
        //subcmd=getaward&key0=1672582341&key1=10058926678023

        //subcmd=begin&key0=1672584751&key1=10058941111647
        //subcmd=finish&key0=879248262&key1=1672584805   2,551,833,067
        //subcmd=getaward&key0=1672584852&key1=10058941713048


        ddp(
            "begin",
            "${System.currentTimeMillis() / 1000}",
            "10058790159996"
        )
        ddp(
            "finish",
            "1551614580",
            "${System.currentTimeMillis() / 1000}"
        )
    }

    private suspend fun ddp(
        subcmd: String,
        key0: String,
        key1: String
    ) {
        val r = activity(129, "subcmd" to subcmd, "key0" to key0, "key1" to key1)
        log(r.msg)
    }

    //乐斗抽签
    private suspend fun draw() {
        log("----乐斗上上签----")
        drawImpl()
    }

    private suspend fun drawImpl() {
        //aid 9 "status": 1,"seconds": 28638,"choose_idx": 1,"multiple": 4,
        var r = activity(9)
        if (!r.isSuccess) {
            log("result: ${r.result}, msg: ${r.msg}")
            return
        }
        val status = r.getInt("status")
        val seconds = r.getLong("seconds")
        log("状态: $status, 距离领取时间: ${seconds.secondToFormat()}")
        if (status == 0) {
            //0大体力药水 1黄金卷轴 2小威望旗 3江湖令礼包 4中体力药水
            r = activity(9, "sub" to "1", "idx" to "1")
            log("选择奖励: ${r.msg}")
            drawImpl()
            return
        }
        if (seconds <= 0) {
            r = activity(9, "sub" to "2")
            log("抽签, 数量: ${r.getInt("multiple")}")
            r = activity(9, "sub" to "3", "is_double" to "1")
            log("领取奖励: ${r.msg}")
            drawImpl()
            return
        }
    }

    //农场
    private suspend fun farm() {
        // "status":1,"fertilize":1,  不能施肥  不能领取
        log("----乐斗农场----")
        farmImpl()
    }

    private suspend fun farmImpl() {
        var r = activity(10)
        if (!r.isSuccess) {
            log("result: ${r.result}, msg: ${r.msg}")
            return
        }
        val status = r.getInt("status")
        val seconds = r.getLong("seconds")
        val fertilize = r.getInt("fertilize")
        log("状态: $status, 距离领取时间: ${seconds.secondToFormat()}, 是否已经施肥: $fertilize")
        if (seconds <= 0) {
            r = activity(10, "sub" to "3", "is_double" to "1")
            log("领取奖励: ${r.msg}")
            farmImpl()
            return
        }
        if (fertilize != 1) {
            r = activity(10, "sub" to "2")
            log("施肥 ${r.msg}")
            farmImpl()
            return
        }
    }

    //镖车
    private suspend fun dartCar() {
        log("----押镖----")
        val hour = LocalDateTime.now().hour
        if (hour !in 16 until 18) {
            log("当前是${hour}点, 每日下午16点到18点之间再执行押镖")
            return
        }
        var r = common("faction", "op" to "escort")

        val scortTimes = r.getJsonObject("user_info")?.getInt("scort_times") ?: 0
        log("已押镖次数: $scortTimes, 剩余次数: ${3 - scortTimes}")
        repeat(3 - scortTimes) {
            r = common("faction", "op" to "escort", "subcmd" to "add", "type" to "$it")
            if (!r.isSuccess) {
                log("押镖失败: ${r.msg}")
                return
            }
            log("领取镖车: ${r.msg}")
            repeat(2) {
                r = common("faction", "op" to "escort", "subcmd" to "update")
                log("升级品质: ${r.msg}, 是否成功: ${r.getInt("is_update")}")
            }
            r = common("faction", "op" to "escort", "subcmd" to "begin")
            log("开始押镖: ${r.msg}")
        }
        r.getJsonObject("user_info")?.getJsonArray("trans")?.takeIf {
            it.isNotEmpty()
        }?.joinToString("\n") {
            val begin = simpleDateFormat.format(it.getLong("begin_time"))
            "开始时间: ${begin}, 类型: ${it.getInt("type")}, 品质: ${it.getInt("quality")}"
        }?.also {
            log("当前镖车列表:\n$it")
        }
    }

    //每日拜访
    private suspend fun meridian() {
        log("----经脉造访----")
        var r = common("meridian", "op" to "visitpage")
        useSnowLotus(r)
        while (true) {
            delay(10)
            r = common("meridian", "op" to "visit", "id" to "0")
            log("一键造访: ${r.msg}")
            if (!r.isSuccess) {
                break
            }
            useSnowLotus(r)
        }
        r = common("meridian", "op" to "visitpage")
        log("当前修为池: ${r.getLong("spirit")}")
    }

    private suspend fun useSnowLotus(result: JsonObject) {
        result.getJsonArray("awards")?.let {
            jsonDefault.decodeFromJsonElement<List<MeridianAwards>>(it)
        }?.filter {
            it.id == 100001L || it.id == 100002L
        }?.forEach { award ->
            delay(10)
            val r = common("meridian", "op" to "award", "index" to "${award.index}")
            log("自动服用${award.name}: ${r.msg}")
        }
    }

    //每日好友乐斗
    private suspend fun dailyFight() {
        log("----每日乐斗----")
        var r = common("detail", "op" to "", "needreload" to "1")
        if (!r.isSuccess) {
            log("获取个人信息失败: ${r.msg}")
            return
        }
        val myPower = r.getLong("attack_power")
        log("我的战力 [$myPower]")
        r = common(
            "sns",
            "op" to "query",
            "needreload" to "1",
            "type" to "0",
        )
        if (!r.isSuccess) {
            log("获取好友信息失败: ${r.msg}")
            return
        }
        jsonDefault.decodeFromJsonElement<FriendInfo>(r).apply {
            var r1 = common("sns", "op" to "getvit", "target_uid" to "0")
            log("已获得好友体力: [$getvit/$maxvit] 胜点: [$getwinpoints/$maxwinpoints] 黄金钥匙: [$getkey/$maxkey]")
            if (getvit < maxvit) {
                log("一键接受体力: ${r1.msg}")
                r1 = common("sns", "op" to "sendvit", "target_uid" to "0")
            }
            log("一键赠送体力: ${r1.msg}")
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
                    delay(10)
                    r1 = common(
                        "sns",
                        "op" to "fight",
                        "target_uid" to friend.uid,
                        "type" to "0",
                    )
                    val msg = r1.msg.decoded
                    log("挑战好友 [${friend.realName}, 战力: ${friend.power}]: $msg")
                    if (r1.result == 10021) {
                        log("体力不足，停止乐斗")
                        return@run
                    }
                    if (msg.contains("战胜")) {
                        win++
                    }
                    time++
                }
            }
            r1 = common("sns", "op" to "getvit", "target_uid" to "0")
            log("一键接受体力: ${r1.msg}")
        }
    }

    //黄金轮盘
    private suspend fun turntable() {
        log("----黄金轮盘----")
        var r = activity(24, "sub" to "0")
        if (!r.isSuccess) {
            log("获取黄金轮盘信息失败: ${r.msg}")
            return
        }
        // "keynum": 0,"daynum": 6, "extranum": 3,
        val keynum = r.getInt("keynum")
        val daynum = r.getInt("daynum")
        val extranum = r.getInt("extranum")
        log("当前可转次数: $keynum, 当日获取次数: $daynum, 可额外获取次数: $extranum")
        repeat(extranum) {
            r = activity(24, "sub" to "2")
            log("获取额外次数: ${r.msg}")
        }
        r = activity(24, "sub" to "0")
        if (!r.isSuccess) {
            log("获取黄金轮盘信息失败: ${r.msg}")
            return
        }
        repeat(r.getInt("keynum")) {
            r = activity(24, "sub" to "1")
            val award = r.getJsonObject("award")?.getJsonArray("items")?.joinToString(", ") {
                "${it.getString("name")}x${it.getInt("num")}"
            }
            log("转动黄金轮盘: ${r.msg}, 获得: [$award]")
        }
    }

    //密卷强化
    private suspend fun reinforcement(
        chainId: Int,
        max: Int,
    ) {
        val isSkill = chainId % 2 == 0
        val infoTag = if (isSkill) "skill_info" else "chain_info"
        val times = run {
            val info: JsonObject =
                common("chain", "op" to "viewupgrade", "chain_id" to "$chainId").getJsonObject(infoTag)
                    ?: return

            val name = info.getString("name")
            val costNum = info.getString("cost_num")
            val goodsNum = info.getString("goods_num")

            val extraName = info.getString("extra_name")
            val extraCostNum = info.getInt("extra_cost_num")
            val extraGoodsNum = info.getInt("extra_goods_num")
            val rate = info.getString("rate")

            log("强化消耗 [${name}x${costNum}($goodsNum), ${extraName}x${extraCostNum}($extraGoodsNum)], 成功概率: $rate")

            val adTime = info.getInt("ad_time")
            val adMax = info.getInt("ad_max")
            val remain = adMax - adTime

            val bless = info.getInt("bless")
            val maxBless = info.getInt("max_bless")
            log("可以加祝福次数: ${remain}, 当前祝福值: [$bless/$maxBless]")
            if (bless > maxBless - 10) {
                log("距离满祝福只剩 ${maxBless - bless} 点了, 不浪费广告祝福机会了")
                return
            }

            remain.coerceAtMost(max)
        }
        repeat(times) {
            common("chain", "op" to "ad", "chain_id" to "$chainId").also {
                if (!it.isSuccess) {
                    log("看广告加祝福失败: ${it.msg}")
                    return
                }
                val info = it.getJsonObject(infoTag)
                val bless = info?.getInt("bless") ?: 0
                val maxBless = info?.getInt("max_bless") ?: 0
                log("看广告加祝福: ${it.msg}, 当前祝福值: [$bless/$maxBless]")
                if (bless > maxBless - 10) {
                    log("距离满祝福只剩 ${maxBless - bless} 点了, 不浪费广告祝福机会了")
                    return
                }
            }
        }
        //server.checkRequest {
        //    common( "chain", "op" to "upgrade", "chain_id" to "$chainId", "auto_pay" to "0")
        //}.also {
        //    log("强化密卷, id: $chainId, msg: ${it.msg}")
        //}
        //"bless": 46,"ad_time": 0,"ad_max": 3,"ad_skip": 0,
        //"chain_info":
        //"ad_time": 0, "ad_max": 3, "ad_skip": 0, "level": 29, "max_level": 50,
        // "bless": 46,  "ext_bless": 0,"max_bless": 150
    }

    //乐斗猜一猜
    private suspend fun guess() {
        var r = activity(118, "sub" to "0")
        if (!r.isSuccess) {
            //log("r: ${r.result}, msg: ${r.msg}")
            return
        }
        log("----乐斗猜一猜----")
        val restPics = r.getInt("rest_pics")
        if (restPics <= 0) {
            log("今日猜一猜已经全部完成")
            return
        }
        repeat(restPics) {
            r = activity(118, "sub" to "1")
            if (!r.isSuccess) {
                log("猜一猜请求失败: ${r.msg}")
                return@repeat
            }
            val answer = r.getInt("answer")
            val curPics = r.getInt("cur_pics")
            var answerId = -1
            var answerName = ""
            r.getJsonArray("choices")?.run {
                forEachIndexed { index, jsonElement ->
                    val id = jsonElement.getInt("id")
                    val name = jsonElement.getString("name")
                    if (id == answer) {
                        answerId = index
                        answerName = name
                        return@run
                    }
                }
            }
            r = activity(118, "sub" to "3", "answer_id" to "$answerId")
            log("第${curPics}题, 答案: 第${answerId + 1}个[${answerName}]: ${r.msg}")
        }
    }

    //乐斗点球
    private suspend fun football() {
        val r = activity(106)
        if (!r.isSuccess) {
            //log("result: ${r.result}, msg: ${r.msg}")
            return
        }
        log("----乐斗点球----")
        val playTimes = r.getInt("play_times")
        log("已点球次数: $playTimes")
        for (i in playTimes + 1..5) {
            val play = activity(106, "subcmd" to "kick", "result" to 1)
            if (play.isSuccess) {
                log("第${i}次点球成功: ${play.msg}, 斗鱼+${play.getInt("douyu")}")
            } else {
                log("第${i}次点球失败: ${play.msg}")
            }
        }
    }

    //开心牧场
    private suspend fun pasture() {
        val r = activity(189)
        if (!r.isSuccess) {
            //log("result: ${r.result}, msg: ${r.msg}")
            return
        }
        log("----开心牧场----")
        pastureImpl()
    }

    private suspend fun pastureImpl() {
        var r = activity(189)
        if (!r.isSuccess) {
            log("result: ${r.result}, msg: ${r.msg}")
            return
        }
        val status = r.getInt("status")
        val seconds = r.getLong("seconds")
        val feed = r.getInt("feed")
        log("状态: $status, 距离领取时间: ${seconds.secondToFormat()}, 是否已经喂养: $feed")
        if (seconds <= 0 || status == 2) {
            r = activity(189, "sub" to "3", "is_double" to "1")
            log("领取奖励: ${r.msg}")
            pastureImpl()
            return
        }
        if (feed != 1) {
            r = activity(189, "sub" to "2")
            log("投喂 ${r.msg}")
            pastureImpl()
            return
        }
    }

    //美食派对
    private suspend fun foodParty() {
        val r = activity(81)
        if (!r.isSuccess) {
            return
        }
        log("----美食派对----")
        val remainPkCount = r.getInt("remain_pk_count")
        val curLev = r.getInt("cur_lev")
        val curGoodFeeling = r.getInt("cur_good_feeling")
        val nextGoodFeeling = r.getInt("next_good_feeling")
        val awardState = r.getInt("award_state")
        log("当前等级: $curLev, 剩余挑战次数: $remainPkCount, 好感度: [${curGoodFeeling}/${nextGoodFeeling}], 奖励是否可领取: $awardState")

        //pk  id=3/6/9  subcmd=do_pk
        //cook  id=100231  subcmd=cook
        val food = r.getJsonArray("food")?.map {
            Pair(it.getInt("id"), it.getString("name"))
        } ?: run {
            log("获取食物信息失败")
            return
        }
        food.filter {
            it.first != 100238
        }.forEach {
            //val cook = activity(81, "subcmd" to "cook", "id" to it.first)
            //log("烹饪食物[${it.second}]: ${cook.msg}")
        }
    }

    private val simpleDateFormat = SimpleDateFormat("MM/dd HH:mm:ss", Locale.getDefault())

    private fun Long.secondToFormat(): String = if (this >= 86400L) {
        val d = this / 86400L
        val h = (this % 86400L) / 3600L
        val m = (this % 86400L) % 3600L / 60L
        val s = (this % 86400L) % 3600L % 60
        "${d}天 ${h.toDateString()}:${m.toDateString()}:${s.toDateString()}"
    } else {
        val h = this / 3600L
        val m = this % 3600L / 60L
        val s = this % 3600L % 60
        "${h.toDateString()}:${m.toDateString()}:${s.toDateString()}"
    }

    private fun Long.toDateString() = if (this >= 10) "$this" else "0$this"

    private suspend fun <T> LeDouApi.checkRequest(request: suspend LeDouApi.() -> T): T {
        delay(10)
        var r = request()
        if (r is JsonObject && r.result == 110) {
            log("登录超时: $r")
            throw IllegalStateException("登录超时: $r")
            //val r1 = server.refresh(uid, wxcode)
            //log("重试: $r1")
            //h5token = r1.getString("token")
            //r = request()
        }
        return r
    }

    private suspend fun activity(
        aid: Int,
        vararg params: Pair<String, Any>
    ) = common("activity", "aid" to aid, *params)

    private suspend fun common(
        cmd: String,
        vararg params: Pair<String, Any>
    ) = server.checkRequest {
        common(uid = uid, cmd = cmd, params = params.associate {
            it.first to it.second.toString()
        })
    }

    private val JsonObject.isSuccess get() = result == 0

    private val JsonObject.result get() = getInt("result", -1)

    private val JsonObject.msg get() = getString("msg")

    private fun log(message: Any?) {
        println(message)
    }

}
