// TODO: Check

sealed class Option<out T> {
    data class Some<out T>(val value: T) : Option<T>()
    object None : Option<Nothing>()
}

sealed class Expr<T> {
    data class StrLit(val s: String) : Expr<String>()
    data class IntLit(val i: Int) : Expr<Int>()
}

fun <T> eval(e: Expr<T>): T =
    when (e) {
        is Expr.StrLit -> {
            val a: String = TODO() as T
            e.s
        }
        is Expr.IntLit -> {
            val a: Int = TODO() as T
            e.i
        }
    }

fun <T> nested(o: Option<Expr<T>>): T? =
    when (o) {
        is Option.Some -> when (val e = o.value) {
            is Expr.StrLit -> {
                val a: String = TODO() as T
                e.s
            }
            is Expr.IntLit -> {
                val a: Int = TODO() as T
                e.i
            }
        }
        is Option.None -> TODO()
    }

fun <T> local(e: Expr<T>): T {
    fun <T> eval(e: Expr<T>): T =
        when (e) {
            is Expr.StrLit -> {
                val a: String = TODO() as T
                e.s
            }
            is Expr.IntLit -> {
                val a: Int = TODO() as T
                e.i
            }
        }

    return eval(e)
}
