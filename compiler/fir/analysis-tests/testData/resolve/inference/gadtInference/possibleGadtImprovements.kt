fun <T> transform(v: T): T = TODO()
fun <T> consume(v: T): Unit = TODO()
fun <T> produce(): T = TODO()

sealed interface ExprSealed<T> {
    class IntExpr : ExprSealed<Int>
    class BoolExpr : ExprSealed<Boolean>
    class AnyExpr : ExprSealed<Any>
}

fun <T> exhaustive(): Int {
    if (produce<T>() is Int) {
        return when (produce<ExprSealed<T>>()) {
            is ExprSealed.IntExpr -> TODO()
            is ExprSealed.AnyExpr -> TODO()
        }
    }
    TODO()
}

interface Expr<T> {
    class IntExpr : Expr<Int>
    class BoolExpr : Expr<Boolean>
    class AnyExpr : Expr<Any>
}

fun <T> `redundant branch`() {
    if (produce<T>() is Int) {
        when (produce<Expr<T>>()) {
            is Expr.IntExpr -> TODO()
            is Expr.BoolExpr -> TODO() // error/warning: redundant
            is Expr.AnyExpr -> TODO()
        }
    }
}

interface I
interface Contra<in T>
interface ContraInt : Contra<Int>
interface ContraI : Contra<I>

fun <T> `impossible inheritance`(ct: Contra<T>, t: T) {
    if (ct is ContraInt) {
        // T <: Int
        if (t is I) { // error: always false
            TODO()
        }
    }
    if (ct is ContraI) {
        // T <: I
        if (t is Int) { // error: always false
            TODO()
        }
    }
}
