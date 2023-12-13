data class Box<A>(val a: A)

sealed class T<A>
data class StrLit(val v: String) : T<String>()
data class IntLit(val v: Int) : T<Int>()

fun <A> evul(t: T<A>): A = <!RETURN_TYPE_MISMATCH!>when (t) {
    is StrLit -> t.v as Any // error
    is IntLit -> TODO()
}<!>

fun <A> noeval(t: T<A>): Box<A> = <!RETURN_TYPE_MISMATCH!>when (t) {
    is StrLit -> Box<Any>(t.v) // error
    is IntLit -> Box<Nothing>(TODO()) // error
}<!>
