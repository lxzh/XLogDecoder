package ledou.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ledou.decoded

//光明顶资源
@Serializable
data class FactionSource(
    val id: Int,

    //1野马 2粮草 3强盗 4探子 5阵营 6总阵营
    val type: Int,

    val distance: Int,

    val status: Int,

    val quality: Int,

    val left_hp: Int = -1,

    val type_index: Int,
) {
    val sort = when (type) {
        1 -> 1
        2 -> 2
        3 -> 4
        4 -> 4
        5 -> 3
        6 -> 3
        else -> type
    }
}

//矿藏
@Serializable
data class Jewel(
    val status: Int,
    val level: Int = 0,
    val role_name: String? = "",
    val fac_name: String? = "",
    val is_me: Int = 0,
    val userName: String = role_name.orEmpty().decoded,
) {
    val desc: String = "等级: ${level}, 占领者: [$fac_name][$userName]"
}

//练功房床位
enum class RoomType(
    val level: Int,
    val type: Int,
    val totalPager: Int,
    val desc: String,
) {
    Type39_1(0, 1, 20, "10-39 百年"),
    Type39_2(0, 2, 4, "10-39 千年"),
    Type59_1(1, 1, 40, "40-59 百年"),
    Type59_2(1, 2, 8, "40-59 千年"),
    Type60_1(2, 1, 80, "无限制 百年"),
    Type60_2(2, 2, 16, "无限制 千年"),
}

@Serializable
data class RoomEntity(
    val pager: Int,
    val roomType: RoomType,
    val room: Room,
) {
    val isEmpty: Boolean = room.uid == 0L

    override fun toString(): String {
        return "${roomType.desc} 第${pager}页 [${room.fac_name}][${room.userName}(${room.uid})][战力: ${room.fight_power}]}"
    }

    @Serializable
    data class Room(
        val uid: Long,
        val room_id: Int,
        val name: String = "",
        //战力
        val fight_power: Long = 0L,
        val fac_id: Long = 0L,
        val fac_name: String = "",
        val head_img: String = "",
        val sex: Int = 0,
        val role_skin: Int = 0,
        val cold: Int = 0,
        val userName: String = name.decoded,
    )
}

//千层塔
@Serializable
data class TowelInfo(
    val monsterInfo: List<MonsterInfo>,
    val baseInfo: BaseInfo,
    val giftInfo: GiftInfo,
) {
    @Serializable
    data class MonsterInfo(
        val id: Int,
        val index: Int,
        val status: Int,
        val name: String,
        val level: Int,
    )

    @Serializable
    data class BaseInfo(
        val layer: Int,
        val barrier: Int,
        val bg: Int,
        val barrierType: Int,
        val revive: Int,
        val buyNum: Int,
        val cost: Int,
        val alive: Int,
    )

    @Serializable
    data class GiftInfo(
        @SerialName("gift_status")
        val giftStatus: Int,
    )

}

//拜访奖励
@Serializable
data class MeridianAwards(
    //100001 雪莲
    val id: Long = 0,
    val type: String = "",
    val index: Long = 0,
    val num: Int = 0,
    val name: String = "",
)

//乐斗好友
@Serializable
data class FriendInfo(
    val can_skip: Int,
    val type: Int,
    val allow_add_friend: Int,
    val auto_add_friend: Int,
    val getvit: Int = 0,
    val maxvit: Int = 0,
    val getwinpoints: Int = 0,
    val maxwinpoints: Int = 0,
    val getkey: Int = 0,
    val maxkey: Int = 0,
    val friendlist: List<Friend> = emptyList(),
) {
    @Serializable
    data class Friend(
        val uid: String,
        val vip_lvl: Int,
        val month_vip: Int,
        val sex: Int,
        val level: Int,
        val power: Long,
        val uin: String,
        val can_fight: Int,
        val name: String,
        val plat_friend: Int,
        val picture_frame_artid: String,
        val title_frame_artid: String,
        val cancel_status: Int,
        val can_sendvit: Int,
        val can_getvit: Int,
        val getvit_status: Int,
        val login_time: Long,
    ) {
        val realName = name.decoded
    }

}

//王者组队匹配队伍成员
@Serializable
data class QualifyingTeamMember(
    val uid: String,
    val level: Int,
    @SerialName("attack_power")
    val power: Int,
)

//每日任务奖励
@Serializable
class ActiveGift(
    val id: Int,
    val name: String,
    val desc: String,
    val icon: Int = 0,
    val command: String,
    val recommend: Int,
    val curnum: Int,
    val targetnum: Int,
    val status: Int
)