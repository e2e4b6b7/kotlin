fun <T> transform(v: T): T = TODO()
fun <T> consume(v: T): Unit = TODO()
fun <T> produce(): T = TODO()

fun <T> basic1(t: T) {
    if (t is Int) {
        consume<T>(produce<Int>())
        consume<Int>(<!ARGUMENT_TYPE_MISMATCH!>produce<T>()<!>)
    }
}

fun <T> basic2(t: T) {
    if (t is List<*>) {
        consume<T>(<!ARGUMENT_TYPE_MISMATCH!>produce<List<*>>()<!>)
        consume<List<*>>(<!ARGUMENT_TYPE_MISMATCH!>produce<T>()<!>)
    }
}

inline fun <reified U> `basic reified final class`(u: U) {
    if (produce<Int>() is U) {
        consume<Int>(<!ARGUMENT_TYPE_MISMATCH!>produce<U>()<!>)
        consume<U>(produce<Int>())
    }
}

open class A

inline fun <reified U> `basic reified open class`(u: U) {
    if (produce<A>() is U) {
        consume<A>(<!ARGUMENT_TYPE_MISMATCH!>produce<U>()<!>)
        consume<U>(<!ARGUMENT_TYPE_MISMATCH!>produce<A>()<!>)
    }
}

class Invariant<T>(val value: T)

fun <T> unsound1(invariant: Invariant<T>) {
    if (invariant.value is Int) {
        consume<T>(produce<Int>()) // todo: error in scala, analyze
        consume<T>(<!ARGUMENT_TYPE_MISMATCH!>produce<Any>()<!>)
        consume<Int>(<!ARGUMENT_TYPE_MISMATCH!>produce<T>()<!>)
        consume<Invariant<Int>>(<!ARGUMENT_TYPE_MISMATCH!>produce<Invariant<T>>()<!>)
        consume<Invariant<T>>(<!ARGUMENT_TYPE_MISMATCH!>produce<Invariant<Int>>()<!>)
    }
}
