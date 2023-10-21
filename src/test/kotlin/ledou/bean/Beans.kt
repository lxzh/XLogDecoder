package ledou.bean

import kotlinx.serialization.Serializable

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