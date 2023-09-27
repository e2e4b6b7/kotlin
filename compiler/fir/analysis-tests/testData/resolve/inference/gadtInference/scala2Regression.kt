open class C<out T>
data class D<S>(var s: S) : C<S>()

fun f() {
    val x = D(" ")
    val y: C<Any> = x
    when (y) {
        is D<*> -> {
            y.s = <!ASSIGNMENT_TYPE_MISMATCH!>Integer(1)<!>
        }
    }
}
