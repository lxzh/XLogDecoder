package common.rxjava

import io.reactivex.rxjava3.core.Observable
import org.junit.jupiter.api.Test

class RxJavaTest {

    @Test
    fun mergerTest() {
        //连个Observable的数据项相加，输出的为  5,7,9， 不会再操作 4,5
        val odds: Observable<Int> = Observable.just(1)
        val evens: Observable<Int> = Observable.just(4)
        Observable.merge(odds, evens).subscribe { s ->
            println(s)
        }
    }

    @Test
    fun zipTest() {
        //连个Observable的数据项相加，输出的为  5,7,9， 不会再操作 4,5
        val odds: Observable<Int> = Observable.just(1)
        val evens: Observable<Int> = Observable.just(4)
        Observable.zip(odds, evens) { t1, t2 ->
            t1 + t2
        }.subscribe { s ->
            println(s)
        }
    }

    @Test
    fun combineTest() {
        //连个Observable的数据项相加，输出的为  5,7,9， 不会再操作 4,5
        val odds: Observable<Int> = Observable.just(1, 3, 5)
        val evens: Observable<Int> = Observable.just(2, 4, 6)
        Observable.combineLatest(odds, evens) { o1, o2 ->
            listOf(o1, o2)
        }.subscribe {
            println(it)
        }

        Observable.concat(odds, evens).subscribe {
            println(it)
        }
    }
}