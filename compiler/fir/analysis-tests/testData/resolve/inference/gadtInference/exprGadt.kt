fun <T> transform(v: T): T = TODO()
fun <T> consume(v: T): Unit = TODO()
fun <T> produce(): T = TODO()

sealed class Expr<out T> {
    class IntConst(val value: Int) : Expr<Int>()
    class BoolConst(val value: Boolean) : Expr<Boolean>()
    class Add(val left: Expr<Int>, val right: Expr<Int>) : Expr<Int>()
}

fun <T> evaluate(expr: Expr<T>): T = when (expr) {
    is Expr.IntConst -> expr.value
    is Expr.BoolConst -> expr.value
    is Expr.Add -> evaluate(expr.left) + evaluate(expr.right)
}

sealed interface Expr2<out T> {
    open class IntExpr : Expr2<Int>
    interface ConstExpr<out T> : Expr2<T>
}

fun <T> foo(x: Expr2.ConstExpr<T>) {
    when (x) {
        is Expr2.IntExpr -> {
            consume<T>(produce<Int>())
            consume<Int>(<!ARGUMENT_TYPE_MISMATCH!>produce<T>()<!>)
        }
    }
}

sealed class Expr3<T> {
    class NumVal<T : Number> : Expr3<T>()
    class ActualNum : Expr3<Number>()
    object ActualNumObject : Expr3<Number>()
}

fun <T> foo2(e: Expr3<T>) {
    when {
        e is Expr3.NumVal<*> -> {
            consume<T>(<!ARGUMENT_TYPE_MISMATCH!>produce<Number>()<!>)
            consume<Number>(produce<T>())
        }
        e is Expr3.ActualNum -> {
            consume<T>(produce<Number>())
            consume<Number>(produce<T>())
        }
        e == Expr3.ActualNumObject -> {
            consume<T>(<!ARGUMENT_TYPE_MISMATCH!>produce<Number>()<!>)
            consume<Number>(<!ARGUMENT_TYPE_MISMATCH!>produce<T>()<!>)
        }
        Expr3.ActualNumObject == e -> {
            consume<T>(produce<Number>())
            consume<Number>(produce<T>())
        }
        else -> TODO()
    }
}

fun <T> foo2as(e: Expr3<T>) {
    e as Expr3.NumVal<*>
    consume<T>(<!ARGUMENT_TYPE_MISMATCH!>produce<Number>()<!>)
    consume<Number>(produce<T>())
}
