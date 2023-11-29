@file:Suppress("unused", "ClassName", "NonAsciiCharacters", "PackageName", "TestFunctionName")

package Kotlin进阶实战.`2_函数与类`

import org.junit.jupiter.api.Test

class `2_1_函数基本概念` {

    @Test
    //KT 函数没有void类型，默认返回Unit类型的函数，Unit可以被省略
    fun 返回Unit的函数() {
        println("Hello Kotlin")
    }

    @Test
    //函数可以返回Nothing类型，和Unit比较容易混淆。如果函数返回Nothing类型，那么函数体里面的代码就永远不会执行
    //Throw表达式的类型是一个类型的Nothing
    fun 返回Nothing的函数(): Nothing {
        while (true) {
            println("do something...")
        }
    }

    //单表达式函数
    //成员函数
    //局部函数

    //尾递归函数
    private fun sum(n: Int, result: Int=0): Int {
        return if (n <= 0) {
            result
        } else {
            sum(n - 1, result + n)
        }
    }
    //尾递归函数使用 tailrec 进行标记，编译器会优化该递归，从而避免堆栈溢出的风险
    private tailrec fun sumWithTailrec(n: Int, result: Int=0): Int {
        return if (n <= 0) {
            result
        } else {
            sumWithTailrec(n - 1, result + n)
        }
    }

    @Test
    fun 求和(){
        println(sum(10000))
        println(sumWithTailrec(10000))
    }

}