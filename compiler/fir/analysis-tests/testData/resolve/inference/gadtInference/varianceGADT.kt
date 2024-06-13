fun <T> transform(v: T): T = TODO()
fun <T> consume(v: T): Unit = TODO()
fun <T> produce(): T = TODO()

abstract class ListInt : List<Int>
abstract class ListString : List<String>

fun <T> `list basic test`(v: List<T>): T {
    return when (v) {
        is ListInt -> 1
        is ListString -> "1"
        else -> TODO()
    }
}

fun <T> `list basic reversed test`(v: List<T>) {
    when (v) {
        is ListInt -> consume<Int>(<!ARGUMENT_TYPE_MISMATCH!>produce<T>()<!>)
        is ListString -> consume<String>(<!ARGUMENT_TYPE_MISMATCH!>produce<T>()<!>)
    }
}

fun <T> `list without variable test`(v: List<T>): T {
    return when (transform(v)) {
        is ListInt -> 1
        is ListString -> "1"
        else -> TODO()
    }
}

fun <T> `list without variable reversed test`(v: List<T>) {
    when (transform(v)) {
        is ListInt -> consume<Int>(<!ARGUMENT_TYPE_MISMATCH!>produce<T>()<!>)
        is ListString -> consume<String>(<!ARGUMENT_TYPE_MISMATCH!>produce<T>()<!>)
    }
}

interface Func<in A, out B>

class Identity<X> : Func<X, X>

fun <A, B> foo(func: Func<A, B>) {
    when (func) {
        is Identity<*> -> {
            consume<A>(<!ARGUMENT_TYPE_MISMATCH!>produce<B>()<!>)
            consume<B>(produce<A>())
        }
    }
}