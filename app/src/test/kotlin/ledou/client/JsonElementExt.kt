package ledou.client

import kotlinx.serialization.json.*

/**
 * @author Virogu
 * @since 2022-12-29 15:42
 **/

private fun JsonObject.getValue(key: String) = get(key)?.jsonPrimitive?.content

fun JsonObject.getString(
    key: String,
    default: String = ""
): String = getValue(key) ?: default

fun JsonObject.getInt(
    key: String,
    default: Int = 0
): Int = getValue(key)?.toIntOrNull() ?: default

fun JsonObject.getLong(
    key: String,
    default: Long = 0L
): Long = getValue(key)?.toLongOrNull() ?: default

fun JsonElement.getString(key: String, default: String = "") = jsonObject.getString(key, default)

fun JsonElement.getInt(key: String, default: Int = 0) = jsonObject.getInt(key, default)

fun JsonElement.getLong(key: String, default: Long = 0L) = jsonObject.getLong(key, default)

fun JsonElement.getJsonObject(key: String) = jsonObject[key]?.jsonObject

fun JsonElement.getJsonArray(key: String) = jsonObject[key]?.jsonArray