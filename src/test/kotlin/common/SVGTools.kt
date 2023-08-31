package common

import java.io.File
import java.io.FileWriter
import java.io.FileReader
import java.lang.Exception
import java.lang.StringBuilder
import java.util.ArrayList

internal object SVGTools {
    /**
     * 将.svg文件转换为安卓可用的.xml
     *
     * @param file 文件路径
     */
    fun svg2xml(file: File) {
        if (!file.exists() && file.isDirectory) {
            return
        }
        var fw: FileWriter? = null
        var fr: FileReader? = null
        val paths = ArrayList<String>()
        try {
            fr = FileReader(file)

            //字符数组循环读取
            val buf = CharArray(1024)
            var len = 0
            val sb = StringBuilder()
            while (fr.read(buf).also { len = it } != -1) {
                sb.append(String(buf, 0, len))
            }

            //收集所有path
            collectPaths(sb.toString(), paths)
            //拼接字符串
            val outSb = contactStr(paths)
            //写出到磁盘
            val outFile = File(file.parentFile, file.name.substring(0, file.name.lastIndexOf(".")) + ".xml")
            fw = FileWriter(outFile)
            fw.write(outSb.toString())
            println("OK:" + outFile.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fw?.close()
                fr?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 拼接字符串
     *
     * @param paths
     * @return
     */
    private fun contactStr(paths: ArrayList<String>): StringBuilder {
        val outSb = StringBuilder()
        outSb.append(
            """<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
        android:width="48dp"
        android:height="48dp"
        android:viewportWidth="1024"
        android:viewportHeight="1024">
"""
        )
        for (path in paths) {
            outSb.append(
                """    <path
        android:fillColor="#FF7F47"
android:pathData="""
            )
            outSb.append(path)
            outSb.append("/>")
        }
        outSb.append("</vector>")
        return outSb
    }

    /**
     * 收集所有path
     *
     * @param result
     * @param paths
     */
    private fun collectPaths(result: String, paths: ArrayList<String>) {
        val split = result.split("<path").toTypedArray()
        for (s in split) {
            if (s.contains("path")) {
                var endIndex: Int
                endIndex = if (!s.contains("fill")) {
                    s.indexOf("p")
                } else {
                    Math.min(s.indexOf("f"), s.indexOf("p"))
                }
                val path = s.substring(s.indexOf("\""), endIndex)
                paths.add(path)
            }
        }
    }

    /**
     * 将一个文件夹里的所有svg转换为xml
     *
     * @param filePath
     */
    fun svg2xmlFromDir(filePath: String?) {
        val file = File(filePath)
        if (file.isDirectory) {
            val files = file.listFiles()
            for (f in files) {
                if (f.name.endsWith(".svg")) {
                    println(f)
                    svg2xml(f)
                }
            }
        } else {
            svg2xml(file)
        }
    }
}