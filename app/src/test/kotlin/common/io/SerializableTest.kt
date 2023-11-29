package common.io

import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Test

class SerializableTest {

    @Serializable
    data class Project(val name: String, val language: String)

    @Test
    fun serializableTest() {
        val data = Project("qwww", "qaqqqq")
//        val s = Json.encodeToString(data)
    }
}