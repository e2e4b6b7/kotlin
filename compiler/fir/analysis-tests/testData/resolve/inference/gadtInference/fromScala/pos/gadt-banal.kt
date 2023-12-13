sealed class T<A>
class StrLit(val v: String) : T<String>()
class IntLit(val v: Int) : T<Int>()

fun <A> eval(t: T<A>): A = when (t) {
    is StrLit -> t.v
    is IntLit -> t.v
}

fun <A> evul(t: T<A>): A = when (t) {
    is StrLit -> ""
    is IntLit -> 0
}
