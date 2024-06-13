// WITH_STDLIB

sealed class Exp<T>
class Lit(val value: Int) : Exp<Int>()
class Pair<A, B>(val fst: Exp<A>, val snd: Exp<B>) : Exp<Pair<A, B>>()

fun <T> eval(e: Exp<T>): T = <!RETURN_TYPE_MISMATCH!>when (e) {
    is Lit -> e.value
    is Pair<*, *> -> Pair(eval(e.fst), eval(e.fst)) // error
}<!>

fun <T> eval2(e: Exp<T>): T = <!RETURN_TYPE_MISMATCH!>when (e) {
    is Lit -> e.value
    is Pair<*, *> -> Pair(eval(e.snd), eval(e.snd)) // error
}<!>
