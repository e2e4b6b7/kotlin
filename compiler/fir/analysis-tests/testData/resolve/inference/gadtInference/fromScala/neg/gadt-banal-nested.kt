interface I

sealed class T<A>
open class StrLit(val v: String) : T<String>()
open class IntLit(val v: Int) : T<Int>()

fun <A> eval(t: T<A>): A =
    <!RETURN_TYPE_MISMATCH!>when (t) {
        is I -> when (t) {
            is StrLit -> t.v
            is IntLit -> "" // error
        }
        else -> TODO()
    }<!>
