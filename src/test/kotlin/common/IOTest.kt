package common

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

@Suppress("DuplicatedCode")
internal class IOTest {

    companion object {
        const val TEST_PATH = "E:\\Note\\MarkDown\\Kotlin\\test"
    }

    @org.junit.jupiter.api.Test
    fun main() {
    }

    @org.junit.jupiter.api.Test
    fun copyFileCommon() {
        val originFile = File("$TEST_PATH\\copytest1.txt")
        val dstFile = File("$TEST_PATH\\copytest2.txt")
        val inputStream = FileInputStream(originFile)
        val os = FileOutputStream(dstFile)
        try {
            val buffer = ByteArray(1024)
            var len = inputStream.read(buffer)
            while (len != -1) {
                os.write(buffer, 0, len)
                len = inputStream.read(buffer)
            }
        } finally {
            os.flush()
            os.close()
            inputStream.close()
        }
    }

    @org.junit.jupiter.api.Test
    fun copyFileUseCopyTo() {
        val originFile = File("$TEST_PATH\\copytest1.txt")
        val dstFile = File("$TEST_PATH\\copytest2.txt")
        val inputStream = FileInputStream(originFile)
        val os = FileOutputStream(dstFile)
        inputStream.use {
            os.use { output ->
                it.copyTo(output)
            }
        }
    }

    @org.junit.jupiter.api.Test
    fun appendFile() {
        val originFile = File("$TEST_PATH\\copytest1.txt")
        val dstFile = File("$TEST_PATH\\copytest2.txt")
        val inputStream = FileInputStream(originFile)
        val os = FileOutputStream(dstFile, true)
        inputStream.use {
            os.use { output ->
                output.write(originFile.readBytes())
            }
        }
    }

    @org.junit.jupiter.api.Test
    fun copyFileDirect() {
        val originFile = File("$TEST_PATH\\copytest1.txt")
        val dstFile = File("$TEST_PATH\\copytest2.txt")
        try {
            originFile.copyTo(dstFile, true)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    @org.junit.jupiter.api.Test
    fun iterateOverFiles() {
        val path = File("E:\\Note\\MarkDown")
        val files = path.walkBottomUp().onEnter {
            !it.name.contains("Kotlin")
        }
        println("walkBottomUp")
        files.forEach {
            println(it.path)
        }
        println("walkTopDown")
        //val files2 = path.walkTopDown()
        //files2.forEach {
        //    println(it.path)
        //}
    }

    @org.junit.jupiter.api.Test
    fun deleteFiles() {
        val path = File("$TEST_PATH\\testDelete - 副本\\")
        val result = path.deleteRecursively()
        println("Delete result: $result")
    }

}