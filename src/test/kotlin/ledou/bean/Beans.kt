package ledou.bean

import kotlinx.serialization.Serializable
import java.net.URLDecoder

@Serializable
data class FactionSource(
    val id: Int,

    //1野马 2粮草 3强盗 4探子 5阵营 6
    val type: Int,

    val distance: Int,

    val status: Int,

    val quality: Int,

    val left_hp: Int = 0,

    val type_index: Int,
)

@Serializable
data class Jewel(
    val status: Int,
    val level: Int = 0,
    val role_name: String? = "",
    val fac_name: String? = "",
    val is_me: Int = 0,
    val userName: String = URLDecoder.decode(role_name ?: "", "UTF-8"),
) {
    val desc: String = "等级: ${level}, 占领者: [$fac_name][$userName]"
}

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
        val fac_id: String = "",
        val fac_name: String = "",
        val head_img: String = "",
        val sex: Int = 0,
        val role_skin: Int = 0,
        val cold: Int = 0,
        val userName: String = URLDecoder.decode(name, "UTF-8"),
    )
}