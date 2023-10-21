@file:Suppress("unused", "SpellCheckingInspection")

package ledou.api

import kotlinx.serialization.json.JsonObject
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * @author Virogu
 * @since 2022-12-29 14:52
 **/

interface LeDouApi {

    @FormUrlEncoded
    @POST("/fcgi-bin/petpk?uid=601401&cmd=refresh")
    suspend fun refresh(
        @Field("uid") uid: String,
        @Field("wxcode") wxcode: String,
        @Field("cmd") cmd: String = "refresh",
        @Field("sub") sub: String = "token",
    ): JsonObject

    @FormUrlEncoded
    @POST("/fcgi-bin/petpk")
    suspend fun getKuangZang(
        @Field("uid") uid: String,
        @Field("area_id") area_id: Int,
        @Field("op") op: String = "main_view",
        @Field("cmd") cmd: String = "jewel_war",
    ): JsonObject

    //历练
    @FormUrlEncoded
    @POST("/fcgi-bin/petpk")
    suspend fun startExperience(
        @Field("uid") uid: String,
        @Field("dup") dup: Int,
        @Field("level") level: Int,
        @Field("times") times: Int,
        @Field("cmd") cmd: String = "mappush",
        @Field("subcmd") subcmd: String = "MoppingUp",
    ): JsonObject

    //光明顶
    @FormUrlEncoded
    @POST("/fcgi-bin/petpk")
    suspend fun factionQuery(
        @Field("uid") uid: String,
        @Field("cmd") cmd: String = "faction",
        @Field("op") op: String = "query",
    ): JsonObject

    @FormUrlEncoded
    @POST("/fcgi-bin/petpk")
    suspend fun factionFinish(
        @Field("uid") uid: String,
        @Field("move_index") moveIndex: Int,
        @Field("cmd") cmd: String = "faction",
        @Field("op") op: String = "brighttop",
        @Field("subcmd") subcmd: String = "finish",
    ): JsonObject

    @FormUrlEncoded
    @POST("/fcgi-bin/petpk")
    suspend fun factionAddMove(
        @Field("uid") uid: String,
        @Field("source_id") sourceId: Int,
        @Field("cmd") cmd: String = "faction",
        @Field("op") op: String = "brighttop",
        @Field("subcmd") subcmd: String = "add_move",
    ): JsonObject

    @FormUrlEncoded
    @POST("/fcgi-bin/petpk")
    suspend fun servantReward(
        @Field("uid") uid: String,
        @Field("idx") idx: Int,
        @Field("cmd") cmd: String = "servant",
        @Field("op") op: String = "reward",
    ): JsonObject

    @FormUrlEncoded
    @POST("/fcgi-bin/petpk")
    suspend fun qualifying(
        @Field("uid") uid: String,
        @Field("cmd") cmd: String = "qualifying",
    ): JsonObject

    @FormUrlEncoded
    @POST("/fcgi-bin/petpk")
    suspend fun qualifyingRank(
        @Field("uid") uid: String,
        @Field("cmd") cmd: String = "qualifying",
        @Field("op") op: String = "rank",
    ): JsonObject

    @FormUrlEncoded
    @POST("/fcgi-bin/petpk")
    suspend fun qualifyingFight(
        @Field("uid") uid: String,
        @Field("cmd") cmd: String = "qualifying",
        @Field("op") op: String = "fight",
    ): JsonObject


}