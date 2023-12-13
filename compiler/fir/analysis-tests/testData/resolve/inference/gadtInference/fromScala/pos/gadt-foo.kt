sealed class Exp<T>
class Var<T>(val name: String) : Exp<T>()

fun <T> env(x: Var<T>): T = TODO()

fun <S> eval(e: Exp<S>): S = when (e) {
    is Var -> env(e)
}
